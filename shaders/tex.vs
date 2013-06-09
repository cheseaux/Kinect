
void main()
{
	
	gl_Position = ftransform();
	
	// get texture coordinate
	gl_TexCoord[0]  = gl_MultiTexCoord0;
}