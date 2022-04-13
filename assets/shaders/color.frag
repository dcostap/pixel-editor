#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 u_emissive;

void main() {
    gl_FragColor = vec4(u_emissive.rgb, texture2D(u_texture, v_texCoords).a * u_emissive.a);
}