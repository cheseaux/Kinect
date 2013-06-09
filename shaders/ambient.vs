uniform mat4 modelworld;
uniform mat4 projection;

void main()
{	  
	// transform vertex to camera coordinates
	gl_Position = projection * modelworld * gl_Vertex;
	
}
