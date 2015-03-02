class Triangle
{
	Vertex v1,v2,v3;
	Vertex mid;
	Triangle(Vertex vert1, Vertex vert2, Vertex vert3)
	{
		v1 = vert1;
		v2 = vert2;
		v3 = vert3;
	}

	Vertex setMid()
	{
		mid = new Vertex((v1.x+v2.x+v3.x)/3,(v1.y+v2.y+v3.y)/3,(v1.z+v2.z+v3.z)/3);
		return mid;
	}

	Vertex getMid()
	{
		return mid;
	}

	Vertex getV(Vertex v)
	{
		if(v.equals(v1))
			return v1;
		if(v.equals(v2))
			return v2;
		if(v.equals(v3))
			return v3;
		return null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
		{
			Vertex vert = (Vertex) o;
			if(vert.equals(v1) || vert.equals(v2) || vert.equals(v3))
			{
				return true;
			}
			else
				return false;
		}
		else
		{
			Triangle tri = (Triangle) o;
			if(tri.v1.equals(v1) || tri.v2.equals(v2) || tri.v3.equals(v3))
				return true;
			else
				return false;
		}
	}
}