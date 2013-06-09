package draft;

/**
 * This class is part of the openGL framework we first started to implement
 * We now use LWJGL and don't need this class anymore
 * @author jonathan
 *
 */
public class Vector3 {
	public double x, y, z;
	
	public Vector3(){
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vector3(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	// Vector3 Methods
	//////////////////

	public Vector3 add(Vector3 v) {
		return new Vector3(x + v.x, y + v.y, z + v.z);
	}
	
	public Vector3 substract(Vector3 v) {
		return new Vector3(x - v.x, y - v.y, z - v.z);
	}
	
	public boolean equals(Vector3 v) {
		return x == v.x && y == v.y && z == v.z;
	}

	public Vector3 multiply(double f) {
		return new Vector3(f*x, f*y, f*z);
	}
	
	public Vector3 divide(double f) {
		assert(f!=0);
		double inv = 1 / f;
		return new Vector3(x * inv, y * inv, z * inv);
	}

	public Vector3 cross(Vector3 v) {
		return new Vector3(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y * v.x);
	}

	public double dot(Vector3 v) {
		return x*v.x + y*v.y + z*v.z;
	}

	public void normalize() { 
		double l = length();
		assert(l != 0);
		x /= l;
		y /= l;
		z /= l;
	}

	public void clamp01() { 
		if (x>1.f) x=1.f;
			else if (x<0.f) x=0.f;
		if (y>1.f) y=1.f;
			else if (y<0.f) y=0.f;
		if (z>1.f) z=1.f;
			else if (z<0.f) z=0.f;
	}

	public double lengthSquared() { 
		return x*x + y*y + z*z; 
	}

	public double length()  { 
		return (double)Math.sqrt(lengthSquared());
	}
	
	public void print() {
		System.out.println("[" + x + ", " + y + ", " + ", " + z + "]");
	}

}
