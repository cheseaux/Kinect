uniform mat4 modelworld;
uniform mat4 worldcamera;
uniform mat4 projection;
uniform mat3 worldcameraNormal;
uniform mat3 modelworldNormal;
uniform vec3 lightposition; // in camera coordinates
uniform vec3 indirectlightposition;

varying vec3 normal, lightDir, indirectLightDir;

void main()
{	  
	// transform normal to camera coordinates
	//normal = (worldcamera * modelworld * vec4( gl_Normal, 0.0 ) ).xyz;
	normal = normalize( worldcameraNormal * modelworldNormal * gl_Normal );
	
	// transform vertex to camera coordinates
	vec3 vertex = vec3( worldcamera * modelworld * gl_Vertex );
	
	//Exercise 4.4: Calculate direct and indirect light directions
	lightDir = normalize( vertex - lightposition);
	indirectLightDir = normalize( vertex - indirectlightposition);

	// get texture coordinate
	gl_TexCoord[0]  = gl_MultiTexCoord0;
	
	// project the point into the camera
	gl_Position = projection * vec4( vertex, 1.0 );
	
	// need this line so OpenGL doesn't optimize out the variables -- remove in your solution
	lightposition; indirectlightposition; lightDir; indirectLightDir;
	
}
