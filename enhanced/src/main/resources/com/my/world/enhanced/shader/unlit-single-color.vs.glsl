#version 330 core

in vec3 a_position;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

void main() {
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}
