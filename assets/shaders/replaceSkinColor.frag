#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec3 outlineColor;
uniform vec3 darkerBaseColor;
uniform vec3 baseColor;

uniform vec3 newOutlineColor;
uniform vec3 newDarkerBaseColor;
uniform vec3 newBaseColor;

void main()
{
    vec4 color = v_color * texture2D(u_texture, v_texCoords);
	if (color.rgb == outlineColor.rgb) {
		color.rgb = newOutlineColor.rgb;
	}
	if (color.rgb == darkerBaseColor.rgb) {
		color.rgb = newDarkerBaseColor.rgb;
	}
	if (color.rgb == baseColor.rgb) {
		color.rgb = newBaseColor.rgb;
	}
	
	gl_FragColor = color;
}