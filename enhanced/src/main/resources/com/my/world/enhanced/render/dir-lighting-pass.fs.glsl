#line 1

// required to have same precision in both shader for light structure
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#ifdef GLSL3
#define varying in
out vec4 out_FragColor;
#else
#define out_FragColor gl_FragColor
#endif

// Utilities
#define saturate(_v) clamp((_v), 0.0, 1.0)

// #################### My Code Begin #################### //

uniform vec2 u_viewPortSize;
uniform sampler2D u_gBuffer0;
uniform sampler2D u_gBuffer1;
uniform sampler2D u_gBuffer2;
uniform sampler2D u_gBuffer3;

// #################### My Code End #################### //

struct DirectionalLight
{
    vec3 color;
    vec3 direction;
};
uniform DirectionalLight u_dirLights[1];

uniform vec4 u_cameraPosition;

vec3 v_position;

// Encapsulate the various inputs used by the various functions in the shading equation
// We store values in structs to simplify the integration of alternative implementations
// PBRSurfaceInfo contains light independant information (surface/material only)
// PBRLightInfo contains light information (incident rays)
struct PBRSurfaceInfo
{
	vec3 n;						  // Normal vector at surface point
	vec3 v;						  // Vector from surface point to camera
	float NdotV;                  // cos angle between normal and view direction
	float perceptualRoughness;    // roughness value, as authored by the model creator (input to shader)
	float metalness;              // metallic value at the surface
	vec3 reflectance0;            // full reflectance color (normal incidence angle)
	vec3 reflectance90;           // reflectance color at grazing angle
	float alphaRoughness;         // roughness mapped to a more linear change in the roughness (proposed by [2])
	vec3 diffuseColor;            // color contribution from diffuse lighting
	vec3 specularColor;           // color contribution from specular lighting
};

struct PBRLightInfo
{
    float NdotL;                  // cos angle between normal and light direction
    float NdotH;                  // cos angle between normal and half vector
    float LdotH;                  // cos angle between light direction and half vector
    float VdotH;                  // cos angle between view direction and half vector
};

const float M_PI = 3.141592653589793;
const float c_MinRoughness = 0.04;

vec4 SRGBtoLINEAR(vec4 srgbIn)
{
    #ifdef MANUAL_SRGB
    #ifdef SRGB_FAST_APPROXIMATION
    vec3 linOut = pow(srgbIn.xyz,vec3(2.2));
    #else //SRGB_FAST_APPROXIMATION
    vec3 bLess = step(vec3(0.04045),srgbIn.xyz);
    vec3 linOut = mix( srgbIn.xyz/vec3(12.92), pow((srgbIn.xyz+vec3(0.055))/vec3(1.055),vec3(2.4)), bLess );
    #endif //SRGB_FAST_APPROXIMATION
    return vec4(linOut,srgbIn.w);;
    #else //MANUAL_SRGB
    return srgbIn;
    #endif //MANUAL_SRGB
}

// Basic Lambertian diffuse
// Implementation from Lambert's Photometria https://archive.org/details/lambertsphotome00lambgoog
// See also [1], Equation 1
vec3 diffuse(PBRSurfaceInfo pbrSurface)
{
    return pbrSurface.diffuseColor / M_PI;
}

// The following equation models the Fresnel reflectance term of the spec equation (aka F())
// Implementation of fresnel from [4], Equation 15
vec3 specularReflection(PBRSurfaceInfo pbrSurface, PBRLightInfo pbrLight)
{
    return pbrSurface.reflectance0 + (pbrSurface.reflectance90 - pbrSurface.reflectance0) * pow(clamp(1.0 - pbrLight.VdotH, 0.0, 1.0), 5.0);
}

// This calculates the specular geometric attenuation (aka G()),
// where rougher material will reflect less light back to the viewer.
// This implementation is based on [1] Equation 4, and we adopt their modifications to
// alphaRoughness as input as originally proposed in [2].
float geometricOcclusion(PBRSurfaceInfo pbrSurface, PBRLightInfo pbrLight)
{
    float NdotL = pbrLight.NdotL;
    float NdotV = pbrSurface.NdotV;
    float r = pbrSurface.alphaRoughness;

    float attenuationL = 2.0 * NdotL / (NdotL + sqrt(r * r + (1.0 - r * r) * (NdotL * NdotL)));
    float attenuationV = 2.0 * NdotV / (NdotV + sqrt(r * r + (1.0 - r * r) * (NdotV * NdotV)));
    return attenuationL * attenuationV;
}

// The following equation(s) model the distribution of microfacet normals across the area being drawn (aka D())
// Implementation from "Average Irregularity Representation of a Roughened Surface for Ray Reflection" by T. S. Trowbridge, and K. P. Reitz
// Follows the distribution function recommended in the SIGGRAPH 2013 course notes from EPIC Games [1], Equation 3.
float microfacetDistribution(PBRSurfaceInfo pbrSurface, PBRLightInfo pbrLight)
{
    float roughnessSq = pbrSurface.alphaRoughness * pbrSurface.alphaRoughness;
    float f = (pbrLight.NdotH * roughnessSq - pbrLight.NdotH) * pbrLight.NdotH + 1.0;
    return roughnessSq / (M_PI * f * f);
}

// Light contribution calculation independent of light type
// l is a unit vector from surface point to light
vec3 getLightContribution(PBRSurfaceInfo pbrSurface, vec3 l)
{
	vec3 n = pbrSurface.n;
	vec3 v = pbrSurface.v;
	vec3 h = normalize(l+v);               // Half vector between both l and v

	float NdotV = pbrSurface.NdotV;
	float NdotL = clamp(dot(n, l), 0.001, 1.0);
	float NdotH = clamp(dot(n, h), 0.0, 1.0);
	float LdotH = clamp(dot(l, h), 0.0, 1.0);
	float VdotH = clamp(dot(v, h), 0.0, 1.0);

	PBRLightInfo pbrLight = PBRLightInfo(
		NdotL,
		NdotH,
		LdotH,
		VdotH
	);

	// Calculate the shading terms for the microfacet specular shading model
	vec3 F = specularReflection(pbrSurface, pbrLight);
	float G = geometricOcclusion(pbrSurface, pbrLight);
	float D = microfacetDistribution(pbrSurface, pbrLight);

	// Calculation of analytical lighting contribution
	vec3 diffuseContrib = (1.0 - F) * diffuse(pbrSurface);
	vec3 specContrib = F * G * D / (4.0 * NdotL * NdotV);
	// Obtain final intensity as reflectance (BRDF) scaled by the energy of the light (cosine law)
	return NdotL * (diffuseContrib + specContrib);
}

vec3 getDirectionalLightContribution(PBRSurfaceInfo pbrSurface, DirectionalLight light)
{
    vec3 l = normalize(-light.direction);  // Vector from surface point to light
    return getLightContribution(pbrSurface, l) * light.color;
}

void main() {

    // My Code Begin

    vec2 fragCoord = gl_FragCoord.xy / u_viewPortSize;
    vec4 gBuffer0 = texture2D(u_gBuffer0, fragCoord);
    vec4 gBuffer1 = texture2D(u_gBuffer1, fragCoord);
    vec4 gBuffer2 = texture2D(u_gBuffer2, fragCoord);
    vec4 gBuffer3 = texture2D(u_gBuffer3, fragCoord);

    v_position = gBuffer0.xyz;
    vec4 baseColor = vec4(gBuffer2.rgb, 1.0);
    float perceptualRoughness = gBuffer3.r;
    float metallic = gBuffer3.g;
    float alphaRoughness = perceptualRoughness * perceptualRoughness;

    // My Code End
    
    vec3 f0 = vec3(0.04);
    vec3 diffuseColor = baseColor.rgb * (vec3(1.0) - f0);
    diffuseColor *= 1.0 - metallic;
    vec3 specularColor = mix(f0, baseColor.rgb, metallic);

    // Compute reflectance.
    float reflectance = max(max(specularColor.r, specularColor.g), specularColor.b);

    // For typical incident reflectance range (between 4% to 100%) set the grazing reflectance to 100% for typical fresnel effect.
    // For very low reflectance range on highly diffuse objects (below 4%), incrementally reduce grazing reflecance to 0%.
    float reflectance90 = clamp(reflectance * 25.0, 0.0, 1.0);
    vec3 specularEnvironmentR0 = specularColor.rgb;
    vec3 specularEnvironmentR90 = vec3(1.0, 1.0, 1.0) * reflectance90;

    vec3 surfaceToCamera = u_cameraPosition.xyz - v_position;
    float eyeDistance = length(surfaceToCamera);

    vec3 n = gBuffer1.xyz;                             // normal at surface point
    vec3 v = surfaceToCamera / eyeDistance;        // Vector from surface point to camera
    vec3 reflection = -normalize(reflect(v, n));

    float NdotV = clamp(abs(dot(n, v)), 0.001, 1.0);

    PBRSurfaceInfo pbrSurface = PBRSurfaceInfo(
    	n,
		v,
		NdotV,
		perceptualRoughness,
		metallic,
		specularEnvironmentR0,
		specularEnvironmentR90,
		alphaRoughness,
		diffuseColor,
		specularColor
    );

    // Directional lights calculation
    vec3 color = getDirectionalLightContribution(pbrSurface, u_dirLights[0]);

    // final frag color
#ifdef MANUAL_SRGB
    out_FragColor = vec4(pow(color,vec3(1.0/2.2)), baseColor.a);
#else
    out_FragColor = vec4(color, baseColor.a);
#endif

    out_FragColor.a = 1.0;

}
