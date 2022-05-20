#version 330 core

#ifdef GL_ES
precision mediump float;
#endif

in vec2 texCoord0;

uniform int u_colorFlag;
uniform vec4 u_colorValue;
uniform sampler2D u_colorMap;

uniform int u_depthFlag;
uniform float u_depthValue;
uniform sampler2D u_depthMap;

void main()
{
    // clear color buffer
    if (u_colorFlag == 1) {
        gl_FragColor = u_colorValue;
    } else if (u_colorFlag == 2) {
        gl_FragColor = texture(u_colorMap, texCoord0);
    } else {
        gl_FragColor = vec4(0, 0, 0, 0);
    }

    // clear depth buffer
    if (u_depthFlag == 1) {
        gl_FragDepth = u_depthValue;
    } else if (u_depthFlag == 2) {
        gl_FragDepth = texture(u_depthMap, texCoord0).r;
    } else {
        gl_FragDepth = gl_FragCoord.z;
    }
}
