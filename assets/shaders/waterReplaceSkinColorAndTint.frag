#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D u_texture;

uniform vec2 pixelScreenSize;
uniform vec2 cameraPos;

uniform float timeScale;
uniform float waveHeightScale;
uniform float waveWidthScale;
uniform vec3 tintedColor;
uniform float alpha;

uniform float time;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform vec3 outlineColor;
uniform vec3 darkerBaseColor;
uniform vec3 baseColor;

uniform vec3 newOutlineColor;
uniform vec3 newDarkerBaseColor;
uniform vec3 newBaseColor;

uniform vec4 u_emissive;

void main() {
	vec2 origPixel = v_texCoords;
	
	vec2 coords = vec2(origPixel.x / pixelScreenSize.x + cameraPos.x, origPixel.y / pixelScreenSize.y + cameraPos.y);
	
	vec4 color = v_color * texture2D(u_texture, origPixel + vec2(sin(time * timeScale + coords.y/waveHeightScale) * pixelScreenSize.x * waveWidthScale, 0.0));
	if (color.rgb == outlineColor.rgb) {
		color.rgb = newOutlineColor.rgb;
	}
	if (color.rgb == darkerBaseColor.rgb) {
		color.rgb = newDarkerBaseColor.rgb;
	}
	if (color.rgb == baseColor.rgb) {
		color.rgb = newBaseColor.rgb;
	}
	color += vec4(tintedColor, 0.0);
	
	gl_FragColor = color;
	color.a *= alpha;

	gl_FragColor = color + u_emissive;
}