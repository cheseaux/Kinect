varying vec3 normal, lightDir, indirectLightDir;

uniform int useTexture;
uniform sampler2D texture;
uniform vec3 diffuseColor;
uniform float specularExp;
uniform vec3 indirectlightcolor;
uniform vec3 lightcolor;
		
void main()
{	
	//Exercise 4.4: Calculate the reflected intensities for the direct sun light (using lightDir and lightcolor) 
	//and indirect light (using indirectLightDir and indirectlightcolor)
	
	vec3 color = diffuseColor * lightcolor;
	vec3 indcolor = diffuseColor * lightcolor;
	
	vec3 N = normalize( normal );
	vec3 Ldir = normalize( -lightDir );
	vec3 Lind = normalize( -indirectLightDir );
	
	float dirLambertTerm = dot(N, Ldir);
	float indLambertTerm = dot(N, Lind);
	
	color *= max(0.0, dirLambertTerm);
	indcolor *= max(0.0, indLambertTerm);
	
	if(useTexture != 0){
        color = color * vec3(texture2D(texture, gl_TexCoord[0].xy));
		indcolor = indcolor * vec3(texture2D(texture, gl_TexCoord[0].xy));
    }
	
	vec4 finalcolor = vec4(color, 1.0) + vec4(indcolor, 1.0);
	

	gl_FragColor = finalcolor;	
	
	// need this line so OpenGL doesn't optimize out the variables -- remove in your solution
	useTexture; texture; indirectlightcolor; specularExp;
}