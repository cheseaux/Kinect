package gl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import draft.Matrix4;

/**
 * Ce code n'est pas de nous, nous l'avons juste un peu adapté à notre programme
 * Sets up the main shader program and links in and compiles the vertex
 * and fragment shader files.
 * @author ste3e
 */
public class Shader {
	private boolean useShader=true;
	private int programObj=0;
	private int vertShader=0;
	private int fragShader=0;

	public Shader(String vertexFileName, String fragmentFileName){
		// create Shader executable object
		programObj = ARBShaderObjects.glCreateProgramObjectARB();

		/* If the executable is being well created,  we can create the vertex and fragment
		 * shaders. Otherwise we don't use shaders (and we just have a white screen)
		 */
		if(programObj!=0){
			vertShader=createVertShader(vertexFileName);
			fragShader=createFragShader(fragmentFileName);
		}else {
			useShader=false;
		}

		/* If the vertex and fragment shaders are well created, we can insert them into 
		 * the executable. Otherwise we don't use the shader (and we just have a white screen)
		 */
		if(vertShader !=0 && fragShader !=0){
			ARBShaderObjects.glAttachObjectARB(programObj, vertShader);
			ARBShaderObjects.glAttachObjectARB(programObj, fragShader);
			ARBShaderObjects.glLinkProgramARB(programObj);
			ARBShaderObjects.glValidateProgramARB(programObj);
			useShader=printLogInfo(programObj);
		}else {
			useShader=false;
		}
	}

	/*
	 * With the exception of syntax, setting up vertex and fragment shaders
	 * is the same.
	 * @param the name and path to the vertex shader
	 */
	private int createVertShader(String filename){
		
		//vertShader will be non zero if succefully created
		vertShader=ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
		assert(vertShader == 0);
		
		//if created, convert the vertex shader code to a String
		String vertexCode="";
		String line;
		try{
			BufferedReader reader=new BufferedReader(new FileReader(filename));
			while((line=reader.readLine())!=null){
				vertexCode+=line + "\n";
			}
		}catch(Exception e){
			System.out.println("Fail reading vertex shading code");
			return 0;
		}
		/*
		 * associate the vertex code String with the created vertex shader
		 * and compile
		 */
		ARBShaderObjects.glShaderSourceARB(vertShader, vertexCode);
		ARBShaderObjects.glCompileShaderARB(vertShader);
		//if there was a problem compiling, reset vertShader to zero
		if (ARBShaderObjects.glGetObjectParameteriARB(vertShader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
			printLogInfo(vertShader);
			vertShader=0;
		}
		//if zero we won't be using the shader
		return vertShader;
	}

	//same as per the vertex shader except for method syntax
	private int createFragShader(String filename){

		fragShader=ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		if(fragShader==0){return 0;}
		String fragCode="";
		String line;
		try{
			BufferedReader reader=new BufferedReader(new FileReader(filename));
			while((line=reader.readLine())!=null){
				fragCode+=line + "\n";
			}
		}catch(Exception e){
			return 0;
		}
		ARBShaderObjects.glShaderSourceARB(fragShader, fragCode);
		ARBShaderObjects.glCompileShaderARB(fragShader);
		if (ARBShaderObjects.glGetObjectParameteriARB(fragShader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
			printLogInfo(fragShader);
			fragShader=0;
		}
		return fragShader;
	}

	private static boolean printLogInfo(int obj){
		IntBuffer iVal = BufferUtils.createIntBuffer(1);
		ARBShaderObjects.glGetObjectParameterARB(obj,ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB, iVal);

		int length = iVal.get();
		if (length > 1) {
			// We have some info we need to output.
			ByteBuffer infoLog = BufferUtils.createByteBuffer(length);
			iVal.flip();
			ARBShaderObjects.glGetInfoLogARB(obj, iVal, infoLog);
			byte[] infoBytes = new byte[length];
			infoLog.get(infoBytes);
		}
		else return true;
		return false;
	}

	public boolean useShader(){
		return useShader;
	}
	public int getShader(){
		return programObj;
	}

	public void bind(){
		assert(programObj != 0);
		ARBShaderObjects.glUseProgramObjectARB(programObj);
	}
	
	public void unbind(){
		ARBShaderObjects.glUseProgramObjectARB(0);
	}
	
	
	public void setFloatUniform(String name, float f)
	{
		assert(programObj != 0);
		ARBShaderObjects.glUniform1fARB(getUniformLocation(name), f);
	}

	public void setVector3fUniform(String name, float v0, float v1, float v2)
	{
		assert(programObj != 0);
		ARBShaderObjects.glUniform3fARB(getUniformLocation(name), v0, v1, v2);
	}
	
	public void setMatrix4x4Uniform(String name, Matrix4 matrix) {
		assert(programObj != 0);
		FloatBuffer mat = BufferUtils.createFloatBuffer(16);
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				mat.put(i*4+j, (float)matrix.getM()[i][j]);
			}
		}
		mat.rewind();
		ARBShaderObjects.glUniformMatrix4ARB(getUniformLocation(name), true, mat);
	}
	
	private int getUniformLocation(String name)
	{
		assert(programObj != 0);
		int loc = ARBShaderObjects.glGetUniformLocationARB(programObj, name);
		if(loc == -1) {
			System.out.println("No uniform " + name);
		}
		return loc;
	}
}