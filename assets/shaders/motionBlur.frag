#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float sampleDist;
uniform float sampleStrength;
uniform float originX;
uniform float originY;

void main()
{
	float samples[10];
    samples[0] = -0.07;
    samples[1] = -0.05;
    samples[2] = -0.03;
    samples[3] = -0.02;
    samples[4] = -0.01;
    samples[5] =  0.01;
    samples[6] =  0.02;
    samples[7] =  0.03;
    samples[8] =  0.05;
    samples[9] =  0.07;

    vec2 dir = v_texCoords - vec2(originX, originY);
    float dist = sqrt(dir.x*dir.x + dir.y*dir.y);
    dir = dir/dist;

    vec4 color = texture2D(u_texture,v_texCoords);
    vec4 sum = color;

    for (int i = 0; i < 10; i++)
        sum += texture2D( u_texture, v_texCoords + dir * samples[i] * sampleStrength );

    sum *= 1.0/11.0;
    float t = dist * sampleDist;
    t = clamp( t ,0.0,1.0);

	gl_FragColor = mix( color, sum, t );
}