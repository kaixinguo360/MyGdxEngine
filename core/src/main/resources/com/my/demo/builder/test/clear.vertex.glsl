#version 330 core

in vec3 a_position;

void main()
{
    gl_Position = vec4(a_position.xyz, 1.0);
}
