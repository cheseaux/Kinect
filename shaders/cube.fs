varying vec4 color;



void main()
{
	gl_FragColor = color;
	
	// need this line so OpenGL doesn't optimize out the variables -- remove in your solution
    // {
	color;
    // }
}