uniform mat4 ModelWorldTransform;

varying vec4 color;

void main()
{
	gl_Position = gl_ModelViewProjectionMatrix * ModelWorldTransform * gl_Vertex;
	color = gl_Color;

	// need this line so OpenGL doesn't optimize out the variables -- remove in your solution
    // {
	ModelWorldTransform; color;
    // }
}