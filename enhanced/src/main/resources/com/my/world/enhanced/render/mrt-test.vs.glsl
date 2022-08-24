#version 330 core

in vec3 a_position;
in vec3 a_normal;
in vec2 a_texCoord0;

out vec3 fragPositon;
out vec3 normal;
out vec2 texCoord0;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

void main() {
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
    fragPositon = gl_Position.xyz;
    normal = a_normal;
    texCoord0 = a_texCoord0;
}
