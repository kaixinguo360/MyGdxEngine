#line 1

in vec3 a_position;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

void main() {
    gl_Position = vec4(a_position.xy, 1, 1.0);
}
