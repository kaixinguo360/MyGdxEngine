#line 1

// Extensions required for WebGL and some Android versions

#ifdef GLSL3
#define textureCubeLodEXT textureLod
#else
#ifdef USE_TEXTURE_LOD_EXT
#extension GL_EXT_shader_texture_lod: enable
#else
// Note : "textureCubeLod" is used for compatibility but should be "textureLod" for GLSL #version 130 (OpenGL 3.0+)
#define textureCubeLodEXT textureCubeLod
#endif
#endif

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
#define textureCube texture
#define texture2D texture
#else
#define out_FragColor gl_FragColor
#endif

// Utilities
#define saturate(_v) clamp((_v), 0.0, 1.0)

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#ifdef textureFlag
varying MED vec2 v_texCoord0;
#endif // textureFlag

// texCoord unit mapping

#ifndef v_emissiveUV
#define v_emissiveUV v_texCoord0
#endif

// #################### My Code Begin #################### //

uniform vec2 u_viewPortSize;
uniform sampler2D u_gBuffer0;
uniform sampler2D u_gBuffer1;
uniform sampler2D u_gBuffer2;
uniform sampler2D u_gBuffer3;

// #################### My Code End #################### //

#ifdef fogFlag
uniform vec4 u_fogColor;

#ifdef fogEquationFlag
uniform vec3 u_fogEquation;
#endif

#endif // fogFlag


#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif // ambientLightFlag


#ifdef USE_IBL
uniform samplerCube u_DiffuseEnvSampler;

#ifdef diffuseSpecularEnvSeparateFlag
uniform samplerCube u_SpecularEnvSampler;
#else
#define u_SpecularEnvSampler u_DiffuseEnvSampler
#endif

#ifdef brdfLUTTexture
uniform sampler2D u_brdfLUT;
#endif

#ifdef USE_TEX_LOD
uniform float u_mipmapScale; // = 9.0 for resolution of 512x512
#endif

#endif

#if numDirectionalLights > 0
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif // numDirectionalLights


#if numPointLights > 0
struct PointLight
{
	vec3 color;
	vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif // numPointLights

#if numSpotLights > 0
struct SpotLight
{
	vec3 color;
	vec3 position;
	vec3 direction;
	float cutoffAngle;
	float exponent;
};
uniform SpotLight u_spotLights[numSpotLights];
#endif // numSpotLights


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

// Calculation of the lighting contribution from an optional Image Based Light source.
// Precomputed Environment Maps are required uniform inputs and are computed as outlined in [1].
// See our README.md on Environment Maps [3] for additional discussion.
#ifdef USE_IBL
vec3 getIBLContribution(PBRSurfaceInfo pbrSurface, vec3 n, vec3 reflection)
{
    // retrieve a scale and bias to F0. See [1], Figure 3
#ifdef brdfLUTTexture
	vec2 brdf = SRGBtoLINEAR(texture2D(u_brdfLUT, vec2(pbrSurface.NdotV, 1.0 - pbrSurface.perceptualRoughness))).xy;
#else // TODO not sure about how to compute it ...
	vec2 brdf = vec2(pbrSurface.NdotV, pbrSurface.perceptualRoughness);
#endif
    
    vec3 diffuseLight = SRGBtoLINEAR(textureCube(u_DiffuseEnvSampler, n)).rgb;

#ifdef USE_TEX_LOD
    float lod = (pbrSurface.perceptualRoughness * u_mipmapScale);
    vec3 specularLight = SRGBtoLINEAR(textureCubeLodEXT(u_SpecularEnvSampler, reflection, lod)).rgb;
#else
    vec3 specularLight = SRGBtoLINEAR(textureCube(u_SpecularEnvSampler, reflection)).rgb;
#endif

    vec3 diffuse = diffuseLight * pbrSurface.diffuseColor;
    vec3 specular = specularLight * (pbrSurface.specularColor * brdf.x + brdf.y);

    return diffuse + specular;
}
#endif

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

#if numDirectionalLights > 0
vec3 getDirectionalLightContribution(PBRSurfaceInfo pbrSurface, DirectionalLight light)
{
    vec3 l = normalize(-light.direction);  // Vector from surface point to light
    return getLightContribution(pbrSurface, l) * light.color;
}
#endif

#if numPointLights > 0
vec3 getPointLightContribution(PBRSurfaceInfo pbrSurface, PointLight light)
{
	// light direction and distance
	vec3 d = light.position - v_position.xyz;
	float dist2 = dot(d, d);
	d *= inversesqrt(dist2);

	return getLightContribution(pbrSurface, d) * light.color / (1.0 + dist2);
}
#endif

#if numSpotLights > 0
vec3 getSpotLightContribution(PBRSurfaceInfo pbrSurface, SpotLight light)
{
	// light distance
	vec3 d = light.position - v_position.xyz;
	float dist2 = dot(d, d);
	d *= inversesqrt(dist2);

	// light direction
	vec3 l = normalize(-light.direction);  // Vector from surface point to light

	// from https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md#inner-and-outer-cone-angles
	float lightAngleOffset = light.cutoffAngle;
	float lightAngleScale = light.exponent;

	float cd = dot(l, d);
	float angularAttenuation = saturate(cd * lightAngleScale + lightAngleOffset);
	angularAttenuation *= angularAttenuation;

	return getLightContribution(pbrSurface, d) * light.color * (angularAttenuation / (1.0 + dist2));
}
#endif

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

    vec3 color = vec3(0.0);

#if (numDirectionalLights > 0)
    // Directional lights calculation
    for(int i=0 ; i<numDirectionalLights ; i++){
    	color += getDirectionalLightContribution(pbrSurface, u_dirLights[i]);
    }
#endif

    // Calculate lighting contribution from image based lighting source (IBL)
#if defined(USE_IBL) && defined(ambientLightFlag)
    vec3 ambientColor = getIBLContribution(pbrSurface, n, reflection) * u_ambientLight;
#elif defined(USE_IBL)
    vec3 ambientColor = getIBLContribution(pbrSurface, n, reflection);
#elif defined(ambientLightFlag)
    vec3 ambientColor = u_ambientLight;
#else
    vec3 ambientColor = vec3(0.0, 0.0, 0.0);
#endif

    color += ambientColor;

#if (numPointLights > 0)
    // Point lights calculation
    for(int i=0 ; i<numPointLights ; i++){
    	color += getPointLightContribution(pbrSurface, u_pointLights[i]);
    }
#endif // numPointLights

#if (numSpotLights > 0)
    // Spot lights calculation
    for(int i=0 ; i<numSpotLights ; i++){
    	color += getSpotLightContribution(pbrSurface, u_spotLights[i]);
    }
#endif // numSpotLights


    // Apply optional PBR terms for additional (optional) shading
#ifdef occlusionTextureFlag
    float ao = gBuffer3.b;
    color = mix(color, color * ao, gBuffer3.a);
#endif

    // Add emissive
#if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
    vec3 emissive = SRGBtoLINEAR(texture2D(u_emissiveTexture, v_emissiveUV)).rgb * u_emissiveColor.rgb;
#elif defined(emissiveTextureFlag)
    vec3 emissive = SRGBtoLINEAR(texture2D(u_emissiveTexture, v_emissiveUV)).rgb;
#elif defined(emissiveColorFlag)
    vec3 emissive = u_emissiveColor.rgb;
#endif

#if defined(emissiveTextureFlag) || defined(emissiveColorFlag)
    color += emissive;
#endif

    
    // final frag color
#ifdef MANUAL_SRGB
    out_FragColor = vec4(pow(color,vec3(1.0/2.2)), baseColor.a);
#else
    out_FragColor = vec4(color, baseColor.a);
#endif
    
#ifdef fogFlag
#ifdef fogEquationFlag
    float fog = (eyeDistance - u_fogEquation.x) / (u_fogEquation.y - u_fogEquation.x);
    fog = clamp(fog, 0.0, 1.0);
    fog = pow(fog, u_fogEquation.z);
#else
	float fog = min(1.0, eyeDistance * eyeDistance * u_cameraPosition.w);
#endif
	out_FragColor.rgb = mix(out_FragColor.rgb, u_fogColor.rgb, fog * u_fogColor.a);
#endif

    // Blending and Alpha Test
#ifdef blendedFlag
	out_FragColor.a = baseColor.a * v_opacity;
	#ifdef alphaTestFlag
		if (out_FragColor.a <= v_alphaTest)
			discard;
	#endif
#else
	out_FragColor.a = 1.0;
#endif

}
