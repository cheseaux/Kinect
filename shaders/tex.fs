
uniform sampler2D texture;
uniform float reflection;
uniform float saturation;

void main()
{	
	float a = texture2D(texture, gl_TexCoord[0].xy).a;

	float r = texture2D(texture, gl_TexCoord[0].xy).r;
	float g = texture2D(texture, gl_TexCoord[0].xy).g;
	float b = texture2D(texture, gl_TexCoord[0].xy).b;
	float gray = (r+g+b);
	gray = gray * 0.333;

	vec3 grayColor = vec3(gray, gray, gray);
	vec3 color = texture2D(texture, gl_TexCoord[0].xy).rgb;

	float alpha = 0.0;
	if(reflection != 0.0){
		alpha = 0.3;
	} else {
		alpha = 1.0;
	}

	vec3 finalCol = mix(grayColor, color, saturation);

	gl_FragColor = vec4( finalCol, alpha * a);
}