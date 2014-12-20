import java.util.ArrayList;
import java.util.List;
public class Vertex
{
	public float x,y,z;
	public float u,v;
	public float height;
	public Vertex(float X, float Y, float Z)
	{
		x = X; y = Y; z = Z;
	}

	public void add(Vertex v)
	{
		x += v.x; y += v.y; z += v.z;
	}

	public void sub(Vertex v)
	{
		x -= v.x; y -= v.y; z -= v.z;
	}

	public void div(float n)
	{
		x /= n; y /= n; z /= n;
	}

	public void mult(float n)
	{
		x *= n; y *= n; z *= n;
	}

	public float getMagnitude()
	{
		return (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
	}

	public Vertex getMidV(Vertex v)
	{
		return new Vertex((x + v.x)/2f,(y + v.y)/2f,(z + v.z)/2f);
	}

	public void normalize()
	{
		float len = (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
		float t = (float)(1.0 + Math.sqrt(5.0)) / 2.0f;
		x /= len;
		y /= len;
		z /= len;
		setTex(len);
		int U = (int)(u * 639);
		int V = (int)(v * 639);
		height = TextureHeightMap.height[U][V];
	} 
	public void setHeight(float h)
	{
		height = h;
	}
	public void adjustHeight()
	{
		double min = 1/3 + 1;
		float h = (height + 1)/4 + 1;
		if(height>=0)
		{
			x *= h;
			y *= h;
			z *= h;
		}
		else
		{
			x *= 1.20;
			y *= 1.20;
			z *= 1.20;
		}
		
	}
	public void setTex(float len)
	{
		u = (float)(0.5 + Math.atan2(z,x)/(Math.PI *2));
		v = (float)(0.5 - Math.asin(y)/Math.PI);
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


}