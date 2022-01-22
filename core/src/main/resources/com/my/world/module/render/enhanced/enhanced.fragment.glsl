#version 330 core

#ifdef GL_ES
precision mediump float;
#endif

struct Material { vec4 ambient; vec4 diffuse; vec4 specular; };
struct DirectionalLight { vec3 direction; Material material; };
struct PointLight { vec3 position; float constant; float linear; float quadratic; Material material; };

in vec3 v_position;
in vec3 v_normal;
in vec2 v_texCoord0;

uniform vec3 u_viewPosition;

//out vec4 gl_FragColor;

// ----- Calc Global Ambient Light ----- //

uniform vec4 u_globalAmbientLight;

vec3 calcGlobalAmbientLight(Material material) {
	return u_globalAmbientLight.rgb * material.diffuse.rgb;
}

// ----- Calc Directional Lights ----- //

uniform int u_numDirectionalLights;
uniform DirectionalLight u_directionalLights[10];

// ----- Calc Point Lights ----- //

uniform int u_numPointLights;
uniform PointLight u_pointLights[30];

vec3 calcPointLight(PointLight light, Material material) {
	vec3 ambient = light.material.ambient.rgb * material.ambient.rgb;

	vec3 normalDirection = normalize(v_normal);
	vec3 lightDirection = normalize(light.position - v_position);
	vec3 diffuse = material.diffuse.rgb * light.material.diffuse.rgb * max(dot(normalDirection, lightDirection), 0.0);

	vec3 viewDirection = normalize(u_viewPosition - v_position);
	vec3 reflectDirection = reflect(-lightDirection, normalDirection);
	vec3 specular = material.specular.rgb * light.material.specular.rgb * pow(max(dot(viewDirection, reflectDirection), 0.0), 32);

	float distance = length(light.position - v_position);
	float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));

	return (ambient + diffuse + specular) * attenuation;
}

// ----- Calc Object Material Color ----- //

uniform int u_ambientType;
uniform int u_diffuseType;
uniform int u_specularType;

uniform sampler2D u_ambientTexture;
uniform sampler2D u_diffuseTexture;
uniform sampler2D u_specularTexture;

uniform vec4 u_materialAmbientColor;
uniform vec4 u_materialDiffuseColor;
uniform vec4 u_materialSpecularColor;

in vec4 v_vertexColor;

Material getMaterial() {

	vec4 diffuse;
	if (u_diffuseType == 2) {
		diffuse = texture(u_diffuseTexture, v_texCoord0);
	} else if (u_diffuseType == 1) {
		diffuse = u_materialDiffuseColor;
	} else {
		diffuse = v_vertexColor;
	}

	vec4 ambient;
	if (u_ambientType == 2) {
		ambient = texture(u_ambientTexture, v_texCoord0);
	} else if (u_ambientType == 1) {
		ambient = u_materialAmbientColor;
	} else {
		ambient = diffuse;
	}

	vec4 specular;
	if (u_specularType == 2) {
		specular = texture(u_specularTexture, v_texCoord0);
	} else if (u_specularType == 1) {
		specular = u_materialSpecularColor;
	} else {
		specular = vec4(0.5f);
	}

	return Material(ambient, diffuse, specular);
}

// ----- Main ----- //

void main() {
	Material material = getMaterial();

	vec3 result = calcGlobalAmbientLight(material);
	for (int i = 0; i < u_numPointLights; i++) {
		result += calcPointLight(u_pointLights[i], material);
	}

	gl_FragColor.rgb = result;
	gl_FragColor.a = material.diffuse.a;
}
