uniform sampler2D texture1; // the toon-shading texture
uniform sampler2D texture2; // the edge texture

		
void main()
{	
	vec4 cartoonColor = texture2D(texture1, gl_TexCoord[0].xy );
	vec4 edgeColor = texture2D(texture2, gl_TexCoord[0].xy );
	
	//gl_FragColor = cartoonColor;
	gl_FragColor = edgeColor;
	
}