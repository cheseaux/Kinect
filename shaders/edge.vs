
void main()
{	  
	// keep texture coor
	gl_TexCoord[0] = gl_MultiTexCoord0;
	
	// vertices already in opengl coordinates
	gl_Position = gl_Vertex;
}
