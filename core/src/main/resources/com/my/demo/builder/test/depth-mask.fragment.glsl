#version 330 core

#ifdef GL_ES
precision mediump float;
#endif

void main() {
    gl_FragColor.rgba = vec4(1, 1, 1, 0.5);
}
