public class Vector
{
	float x,y,z;
	Vector(float X, float Y, float Z)
	{
		x = X; y = Y; z = Z;
	}

	Vector(Vector v)
	{
		x = v.x; y = v.y; z = v.z;
	}

	void add(Vector v)
	{
		x += v.x; y += v.y; z += v.z;
	}

	void sub(Vector v)
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

	float dot(Vector v)
	{
		return x*v.x + y*v.y + z*v.z;
	}

	Vector cross(Vector v)
	{
		return new Vector(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x);
	}

	float getMagnitude()
	{
		return (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
	}

	void normalize()
	{
		float len = (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
		x /= len;
		y /= len;
		z /= len;
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
		Vector vert = (Vector) o;
		if(x == vert.x && y == vert.y && z == vert.z)
			return true;
		else
			return false;
	}
}