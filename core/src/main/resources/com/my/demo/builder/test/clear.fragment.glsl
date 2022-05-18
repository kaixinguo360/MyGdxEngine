#version 330 core

#ifdef GL_ES
precision mediump float;
#endif

uniform float u_depth;
uniform vec4 u_color;

void main()
{
    gl_FragDepth = u_depth;
    gl_FragColor = u_color;
}
