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


#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
#ifdef tangentFlag
varying mat3 v_TBN;
#else
varying vec3 v_normal;
#endif

#endif //normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#ifdef textureFlag
varying MED vec2 v_texCoord0;
#endif // textureFlag

#ifdef textureCoord1Flag
varying MED vec2 v_texCoord1;
#endif // textureCoord1Flag

// texCoord unit mapping

#ifndef v_diffuseUV
#define v_diffuseUV v_texCoord0
#endif

#ifndef v_specularUV
#define v_specularUV v_texCoord0
#endif

#ifndef v_emissiveUV
#define v_emissiveUV v_texCoord0
#endif

#ifndef v_normalUV
#define v_normalUV v_texCoord0
#endif

#ifndef v_occlusionUV
#define v_occlusionUV v_texCoord0
#endif

#ifndef v_metallicRoughnessUV
#define v_metallicRoughnessUV v_texCoord0
#endif

// #################### My Code Begin #################### //

layout(location = 0) out vec3 gBuffer0;
layout(location = 1) out vec3 gBuffer1;
layout(location = 2) out vec4 gBuffer2;
layout(location = 3) out vec4 gBuffer3;

// #################### My Code End #################### //

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef baseColorFactorFlag
uniform vec4 u_BaseColorFactor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
uniform float u_NormalScale;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
#endif

#ifdef occlusionTextureFlag
uniform sampler2D u_OcclusionSampler;
uniform float u_OcclusionStrength;
#endif

#ifdef metallicRoughnessTextureFlag
uniform sampler2D u_MetallicRoughnessSampler;
#endif

uniform vec4 u_cameraPosition;

uniform vec2 u_MetallicRoughnessValues;

varying vec3 v_position;

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

#ifndef unlitFlag
// Find the normal for this fragment, pulling either from a predefined normal map
// or from the interpolated mesh normal and tangent attributes.
vec3 getNormal()
{
#ifdef tangentFlag
#ifdef normalTextureFlag
    vec3 n = texture2D(u_normalTexture, v_normalUV).rgb;
    n = normalize(v_TBN * ((2.0 * n - 1.0) * vec3(u_NormalScale, u_NormalScale, 1.0)));
#else
    vec3 n = normalize(v_TBN[2].xyz);
#endif
#else
    vec3 n = normalize(v_normal);
#endif

    return n;
}
#endif

#ifdef unlitFlag

void main() {
#ifdef baseColorFactorFlag
	vec4 baseColorFactor = u_BaseColorFactor;
#else
	vec4 baseColorFactor = vec4(1.0, 1.0, 1.0, 1.0);
#endif

#ifdef diffuseTextureFlag
    vec4 baseColor = SRGBtoLINEAR(texture2D(u_diffuseTexture, v_diffuseUV)) * baseColorFactor;
#else
    vec4 baseColor = baseColorFactor;
#endif

#ifdef colorFlag
    baseColor *= v_color;
#endif

	// My Code Begin
    gBuffer0 = v_position;
    gBuffer1 = vec3(0, 0, 0);
    gBuffer2.rgb = baseColor.rgb;
    gBuffer3.r = 0.5f;
    gBuffer3.g = 0.5f;
    gBuffer3.b = 1;
    gBuffer3.a = 1;
	// My Code End
}

#else

void main() {
	
    // Metallic and Roughness material properties are packed together
    // In glTF, these factors can be specified by fixed scalar values
    // or from a metallic-roughness map
    float perceptualRoughness = u_MetallicRoughnessValues.y;
    float metallic = u_MetallicRoughnessValues.x;
#ifdef metallicRoughnessTextureFlag
    // Roughness is stored in the 'g' channel, metallic is stored in the 'b' channel.
    // This layout intentionally reserves the 'r' channel for (optional) occlusion map data
    vec4 mrSample = texture2D(u_MetallicRoughnessSampler, v_metallicRoughnessUV);
    perceptualRoughness = mrSample.g * perceptualRoughness;
    metallic = mrSample.b * metallic;
#endif
    perceptualRoughness = clamp(perceptualRoughness, c_MinRoughness, 1.0);
    metallic = clamp(metallic, 0.0, 1.0);
    // Roughness is authored as perceptual roughness; as is convention,
    // convert to material roughness by squaring the perceptual roughness [2].
    float alphaRoughness = perceptualRoughness * perceptualRoughness;

    // The albedo may be defined from a base texture or a flat color
#ifdef baseColorFactorFlag
	vec4 baseColorFactor = u_BaseColorFactor;
#else
	vec4 baseColorFactor = vec4(1.0, 1.0, 1.0, 1.0);
#endif

#ifdef diffuseTextureFlag
    vec4 baseColor = SRGBtoLINEAR(texture2D(u_diffuseTexture, v_diffuseUV)) * baseColorFactor;
#else
    vec4 baseColor = baseColorFactor;
#endif

#ifdef colorFlag
    baseColor *= v_color;
#endif

    vec3 f0 = vec3(0.04);
    vec3 diffuseColor = baseColor.rgb * (vec3(1.0) - f0);
    diffuseColor *= 1.0 - metallic;
    vec3 specularColor = mix(f0, baseColor.rgb, metallic);

    // My Code Begin
    gBuffer0 = v_position;
    gBuffer1 = getNormal();
    gBuffer2.rgb = baseColor.rgb;
    gBuffer3.r = perceptualRoughness;
    gBuffer3.g = metallic;
    #ifdef occlusionTextureFlag
    gBuffer3.b = texture2D(u_OcclusionSampler, v_occlusionUV).r;
    gBuffer3.a = u_OcclusionStrength;
    #else
    gBuffer3.b = 1;
    gBuffer3.a = 1;
    #endif
    // My Code End
}

#endif
