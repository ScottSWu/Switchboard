package scwu.switchboard;

public class Dim {
	public float x;
	public float y;
	public float z;
	public float w;
	public float h;
	
	public Dim() {
		x = 0;
		y = 0;
		z = 0;
		w = 0;
		h = 0;
	}
	public Dim(float a,float b,float c,float d,float e) {
		x = a;
		y = b;
		z = c;
		w = d;
		h = e;
	}
	
	public String toString() {
		return x + "," + y + "," + z + ":" + w + "," + h;
	}
}
