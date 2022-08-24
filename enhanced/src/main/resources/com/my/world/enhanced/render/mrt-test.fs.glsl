#version 330 core

#ifdef GL_ES
precision mediump float;
#endif

layout(location = 0) out vec3 gPosition;
layout(location = 1) out vec3 gNormal;
layout(location = 2) out vec4 gAlbedoSpec;

in vec3 fragPositon;
in vec3 normal;
in vec2 texCoord0;

uniform sampler2D texture_diffuse1;
uniform sampler2D texture_specular1;

void main() {
    gPosition = fragPositon;
    gNormal = normalize(normal);
    gAlbedoSpec.rgb = texture(texture_diffuse1, texCoord0).rgb;
    gAlbedoSpec.a = texture(texture_specular1, texCoord0).r;
}
