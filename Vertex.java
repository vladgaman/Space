import java.util.ArrayList;
import java.util.List;
public class Vertex
{
	float x,y,z;
	float u,v;
	float height;
	boolean isPole = false;

	Vertex(float X, float Y, float Z)
	{
		x = X; y = Y; z = Z;
	}

	Vertex(Vertex v)
	{
		x = v.x; y = v.y; z = v.z;
	}

	void add(Vertex v)
	{
		x += v.x; y += v.y; z += v.z;
	}

	void sub(Vertex v)
	{
		x -= v.x; y -= v.y; z -= v.z;
	}

	void div(float n)
	{
		x /= n; y /= n; z /= n;
	}

	void mult(float n)
	{
		x *= n; y *= n; z *= n;
	}

	Vertex cross(Vertex v)
	{
		return new Vertex(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x);
	}

	float getMagnitude()
	{
		return (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
	}

	Vertex getMidV(Vertex v)
	{
		return new Vertex((x + v.x)/2f,(y + v.y)/2f,(z + v.z)/2f);
	}

	void normalize()
	{
		float len = (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
		x /= len;
		y /= len;
		z /= len;
		setTex();
		int U = (int)(u * 639);
		int V = (int)(v * 639);
		height = TextureHeightMap.height[U][V];
	} 
	void setHeight(float h)
	{
		height = h;
	}
	void adjustHeight(float h)
	{
		double min = 1/3 + 1;
		height = (height/5 + 1)/2*h ;
		x *= height;
		y *= height;
		z *= height;
	}

	void setTex()
	{
		u = (float)(0.5 + Math.atan2(z,x)/(Math.PI *2));
		v = (float)(0.5 - Math.asin(y)/Math.PI);
	}

	void rotateAX(float angle)
	{
		y = (float)(Math.cos(Math.toRadians(angle)) * y - Math.sin(Math.toRadians(angle)) * z);
		z = (float)(Math.sin(Math.toRadians(angle)) * y + Math.cos(Math.toRadians(angle)) * z);
	}

	void rotateAY(float angle)
	{
		x = (float)(Math.cos(Math.toRadians(angle)) * x - Math.sin(Math.toRadians(angle)) * z);
		z = (float)(Math.sin(Math.toRadians(angle)) * x + Math.cos(Math.toRadians(angle)) * z);
	}

	void rotateAZ(float angle)
	{
		x = (float)(Math.cos(angle) * x - Math.sin(angle) * y);
		y = (float)(Math.sin(angle) * x + Math.cos(angle) * y);
	}

	void rotateAXR(float angle)
	{
		y = (float)(Math.cos(angle) * y - Math.sin(angle) * z);
		z = (float)(Math.sin(angle) * y + Math.cos(angle) * z);
	}

	void rotateAYR(float angle)
	{
		x = (float)(Math.cos(angle) * x - Math.sin(angle) * z);
		z = (float)(Math.sin(angle) * x + Math.cos(angle) * z);
	}

	void rotateAZR(float angle)
	{
		x = (float)(Math.cos(angle) * x - Math.sin(angle) * y);
		y = (float)(Math.sin(angle) * x + Math.cos(angle) * y);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		Vertex vert = (Vertex) o;
		if(x == vert.x && y == vert.y && z == vert.z && u == vert.u && v == vert.v)
		{
			return true;
		}
		else
			return false;
	}

	String getPrint()
	{
		return x + " " + y + " " + z;
	} 

	String getPrint2()
	{
		return u + " " + v;
	}
}