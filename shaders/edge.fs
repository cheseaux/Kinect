uniform sampler2D texture; //the depth texture that need to be post processed with the sobel operator

uniform float dx; //use this uniform to move 1 pixel in x
uniform float dy; //use this uniform to move 1 pixel in y
		
void main()
{	
	//
	// calculate prewitt edges
	//					-1  0  1
	// x derivative:	-1  0  1
	//					-1  0  1
	// 
	//					 1  1  1
	// y derivative:	 0  0  0
	//					-1 -1 -1
	//
	
	gl_FragColor = texture2D(texture, gl_TexCoord[0].xy);
	
	for(int m=-1; m < 2; m++){
		for(int n=-1; n < 2; n++){
			gl_FragColor += texture2D(texture, gl_TexCoord[0].xy +  vec2(m,n));
		}
	}
	
}