package draft;

import java.util.Arrays;

/**
 * This class is part of the openGL framework we first started to implement
 * We now use LWJGL and don't need this class anymore
 * @author jonathan
 *
 */
public class Matrix4 {

	public double[][] m;

	// Matrix4 Constructors
	////////////////////////

	public Matrix4() {
		m = new double[4][4];
	}

	public Matrix4(	double m_00, double m_01, double m_02, double m_03,
			double m_10, double m_11, double m_12, double m_13,
			double m_20, double m_21, double m_22, double m_23,
			double m_30, double m_31, double m_32, double m_33) {
		m = new double[4][4];
		m[0][0] = m_00; m[0][1] = m_01; m[0][2] = m_02;  m[0][3] = m_03;
		m[1][0] = m_10; m[1][1] = m_11; m[1][2] = m_12;  m[1][3] = m_13;
		m[2][0] = m_20; m[2][1] = m_21; m[2][2] = m_22;  m[2][3] = m_23;
		m[3][0] = m_30; m[3][1] = m_31; m[3][2] = m_32;  m[3][3] = m_33;
	}

	public Matrix4(double[][] n) {
		if (n.length != 4 || n[0].length != 4) {
			throw new IllegalArgumentException("Matrix dimension should be 4x4 but is " + n.length + "x" + n[0].length);
		} else {
			m = new double[4][4];
			for (int a=0;a<n.length;a++)
			{
				System.arraycopy(n[a],0,m[a],0,n[a].length);
			}
		}
	}

	//	public Matrix4(double[][] n) {
	//		if (n.length != 4 || n[0].length != 4) {
	//			System.err.println("Matrix Size not 4x4");
	//		} else {
	//			m = new double[4][4];
	//			m[0][0]=n[0][0]; m[0][1]=n[0][1]; m[0][2]=n[0][2]; m[0][3]=n[0][3];
	//			m[1][0]=n[1][0]; m[1][1]=n[1][1]; m[1][2]=n[1][2]; m[1][3]=n[1][3];
	//			m[2][0]=n[2][0]; m[2][1]=n[2][1]; m[2][2]=n[2][2]; m[2][3]=n[2][3];
	//			m[3][0]=n[3][0]; m[3][1]=n[3][1]; m[3][2]=n[3][2]; m[3][3]=n[3][3];
	//		}
	//	}

	public Matrix4(Matrix4 n) {
		m = new double[4][4];
		m[0][0]=n.m[0][0]; m[0][1]=n.m[0][1]; m[0][2]=n.m[0][2]; m[0][3]=n.m[0][3];
		m[1][0]=n.m[1][0]; m[1][1]=n.m[1][1]; m[1][2]=n.m[1][2]; m[1][3]=n.m[1][3];
		m[2][0]=n.m[2][0]; m[2][1]=n.m[2][1]; m[2][2]=n.m[2][2]; m[2][3]=n.m[2][3];
		m[3][0]=n.m[3][0]; m[3][1]=n.m[3][1]; m[3][2]=n.m[3][2]; m[3][3]=n.m[3][3];
	}

	// Matrix4-Matrix4 Operations
	/////////////////////////////

	public void add(Matrix4 n)  {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				m[i][j] = m[i][j] + n.m[i][j];
			}
		}
	}

	public void substract(Matrix4 n) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				m[i][j] = m[i][j] - n.m[i][j];
			}
		}
	}

	public Matrix4 multiply(Matrix4 n) {
		Matrix4 o = new Matrix4(0,0,0,0,
				0,0,0,0,
				0,0,0,0,
				0,0,0,0);
		for(int i = 0; i  < 4; i++) {
			for(int j = 0; j < 4; j++) {
				double v = 0;
				for(int k = 0; k < 4; k++) {
					v += m[i][k] * n.m[k][j];
				}
				o.m[i][j] = v;
			}
		}
		return o;
	}

	//	public boolean equals(Matrix4 n) {
	//		return m[0][0]==n.m[0][0] && m[0][1]==n.m[0][1] && m[0][2]==n.m[0][2] && m[0][3]==n.m[0][3] &&
	//				m[1][0]==n.m[1][0] && m[1][1]==n.m[1][1] && m[1][2]==n.m[1][2] && m[1][3]==n.m[1][3] &&
	//				m[2][0]==n.m[2][0] && m[2][1]==n.m[2][1] && m[2][2]==n.m[2][2] && m[2][3]==n.m[2][3] &&
	//				m[3][0]==n.m[3][0] && m[3][1]==n.m[3][1] && m[3][2]==n.m[3][2] && m[3][3]==n.m[3][3];
	//	}




	// Matrix4-Vector operations
	////////////////////////////

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(m);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Matrix4 other = (Matrix4) obj;
		if (!Arrays.equals(m, other.m))
			return false;
		return true;
	}

	public Vector3 multiply(Vector3 v) {
		Vector3 u = new Vector3(m[0][0]*v.x + m[0][1]*v.y + m[0][2]*v.z + m[0][3],
				m[1][0]*v.x + m[1][1]*v.y + m[1][2]*v.z + m[1][3],
				m[2][0]*v.x + m[2][1]*v.y + m[2][2]*v.z + m[2][3]);
		double w = m[3][0]*v.x + m[3][1]*v.y + m[3][2]*v.z + m[3][3];
		return u.divide(w);
	}
	
	public double[][] getM() {
		return new Matrix4(m).m;
	}

	// Matrix4-double operations
	///////////////////////////

	public Matrix4 multiply(double f) {
		Matrix4 o = Matrix4.loadZeros();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				o.m[i][j] = m[i][j] * f;
			}
		}
		return o;
	}

	public void divide(double f) {
		if (f == 0) {
			throw new ArithmeticException("Division by zero");
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				m[i][j] = m[i][j] / f;
			}
		}
	}

	// Matrix4 Self Operations
	/////////////////////////////

	//public void print(){
	//	for(int a=0; a<4 ; a++){
	//		for(int b=0; b<4 ; b++){
	//			System.out.print(m[a][b] + "|");
	//		}System.out.println();
	//	}
	//	System.out.println();
	//}

	public void print(){
		for(int a=0; a<m.length ; a++){
			System.out.print(Arrays.toString(m[a]));
			System.out.println();
		}
		System.out.println();
	}

	public static Matrix4 loadIdentity(){
		return new Matrix4(	1,0,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1);
	}

	//Pas besoin, pas défaut c'est toujours zéro
	public static Matrix4 loadZeros(){
		return new Matrix4(	0,0,0,0,
				0,0,0,0,
				0,0,0,0,
				0,0,0,0);
	}
}
