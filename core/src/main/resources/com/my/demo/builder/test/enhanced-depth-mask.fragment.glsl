#version 330 core

#ifdef GL_ES
precision mediump float;
#endif

in vec2 texCoord0;

uniform sampler2D u_colorMap;
uniform sampler2D u_depthMap;

void main()
{
    gl_FragDepth = texture(u_depthMap, texCoord0).r;
    gl_FragColor = texture(u_colorMap, texCoord0);
}
