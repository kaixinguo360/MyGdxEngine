#version 330 core

in vec3 a_position;
in vec3 a_normal;
in vec4 a_color;
in vec2 a_texCoord0;

out vec3 v_position;
out vec3 v_normal;
out vec4 v_vertexColor;
out vec2 v_texCoord0;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

void main() {
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
    v_position = vec3(u_worldTrans * vec4(a_position, 1.0));
    v_normal = mat3(transpose(inverse(u_worldTrans))) * a_normal;
    v_vertexColor = a_color;
    v_texCoord0 = a_texCoord0;
}
