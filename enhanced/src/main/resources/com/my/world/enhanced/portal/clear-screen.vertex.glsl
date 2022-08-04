#version 330 core

in vec3 a_position;
in vec2 a_texCoord0;

out vec2 texCoord0;

void main()
{
    gl_Position = vec4(a_position.xyz, 1.0);
    texCoord0 = a_texCoord0;
}
