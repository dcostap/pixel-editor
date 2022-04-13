#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D u_texture;

// The inverse of the viewport dimensions along X and Y
uniform vec2 pixelScreenSize;
uniform vec2 pixelAtlasSize;
uniform vec2 cameraPos;
uniform float textureHeight;

// U and V of sprite
uniform vec2 u_u;
uniform vec2 v1_v2;

uniform float originY;

varying vec4 v_color;
varying vec2 v_texCoords;

float round(float num, float f) {
    return float(int(num / f)) * f;
}

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
	vec2 origPixel = v_texCoords;
	
	float distance = 0.0;//(origPixel.y - v1_v2.x);
	
	vec4 color = texture2D(u_texture, origPixel - vec2(distance / 3.0, 0.0));
	// color.r = 0.3;
	// color.g = 0.3;
	// color.b = 0.4;
	// if (color.a >= 0.01) {
		// color.a = 0.5;
	// }

	gl_FragColor = color;
}