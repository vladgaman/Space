import java.util.ArrayList;
public class Node
{
	private ArrayList<Node> children;
	public ArrayList<Triangle> tris; // triangle
	private Node parent;
	private Vertex coord;
	private float hWidth; // half width
	private boolean isRoot = false;
	private boolean isLeaf = false;
	public Node(Vertex coordinates, float width)
	{
		children = new ArrayList<Node>();
		tris = new ArrayList<Triangle>();
		isRoot = true;
		coord = coordinates;
		hWidth = width/2;
	}

	public Node(Node theParent, Vertex coordinates, float width)
	{
		children = new ArrayList<Node>();
		tris = new ArrayList<Triangle>();
		coord = coordinates;
		hWidth = width/2;
		parent = theParent;
	}

	public void subdivide(ArrayList<Vertex> vert)
	{
		if(tris.isEmpty())
			return;
		ArrayList<Triangle> newTriangles = new ArrayList<Triangle>();
		for(int i=0;i<tris.size();)
		{
			Triangle aTriangle = tris.get(i);
			subdivideTri(aTriangle, vert, newTriangles);
		}
		setTriangles(newTriangles);
	}

	public void subdivideTri(Triangle tri, ArrayList<Vertex> vert, ArrayList<Triangle> triangles) // de lucru
	{
		tris.remove(tri);
		Vertex mid = tri.getMid();
		if(mid == null)
			mid = tri.setMid();
		double dist = Math.sqrt(Math.pow(mid.x-Game.cam.getCoord().x,2) + Math.pow(mid.y-Game.cam.getCoord().y,2) 
					+ Math.pow(mid.z-Game.cam.getCoord().z,2));

		float ax,ay,bx,by;
		if(tri.v1.u < tri.v2.u && tri.v1.u < tri.v3.u)
		{
			ax = tri.v2.u - tri.v1.u;
			ay = tri.v2.v - tri.v1.v;

			bx = tri.v3.u - tri.v1.u;
			by = tri.v3.v - tri.v1.v;
		}
		else if(tri.v2.u < tri.v1.u && tri.v2.u < tri.v3.u)
		{
			ax = tri.v1.u - tri.v2.u;
			ay = tri.v1.v - tri.v2.v;

			bx = tri.v3.u - tri.v2.u;
			by = tri.v3.v - tri.v2.v;
		}
		else
		{
			ax = tri.v1.u - tri.v3.u;
			ay = tri.v1.v - tri.v3.v;

			bx = tri.v2.u - tri.v3.u;
			by = tri.v2.v - tri.v3.v;
		}
		double p,s1,s2;
		s1 = Math.sqrt(Math.pow(ax,2) + Math.pow(ay,2));
		s2 = Math.sqrt(Math.pow(bx,2) + Math.pow(by,2));
		p = s2;
		if(s1 > s2)
			p = s1;

		/*if(Math.abs(p) >= 0.51)
		{
			tris.remove(tri);
		}*/

		Vertex v12 = tri.v1.getMidV(tri.v2);
		v12.normalize();
		Vertex v23 = tri.v2.getMidV(tri.v3);
		v23.normalize();
		Vertex v31 = tri.v3.getMidV(tri.v1);
		v31.normalize();
		if(!vert.contains(v12))
			vert.add(v12);
		else
			v12 = vert.get(vert.indexOf(v12));

		if(!vert.contains(v23))
			vert.add(v23);
		else
			v23 = vert.get(vert.indexOf(v23));

		if(!vert.contains(v31))
			vert.add(v31);
		else
			v31 = vert.get(vert.indexOf(v31));

		triangles.add(new Triangle(tri.v1,v12,v31));
		triangles.add(new Triangle(tri.v2,v12,v23));
		triangles.add(new Triangle(tri.v3,v23,v31));
		triangles.add(new Triangle(v12,v23,v31));
	}

	public void addChild(Node child)
	{
		children.add(child);
	}

	public void addTriangle(Triangle aTriangle)
	{
		tris.add(aTriangle);
	}

	public boolean isRoot()
	{
		return isRoot;
	}

	public boolean isLeaf()
	{
		return isLeaf;
	}

	public void setAsLeaf()
	{
		isLeaf = true;
	}

	public Node getChild(int pos)
	{
		return children.get(pos);
	}

	public ArrayList<Node> getChildren()
	{
		return children;
	}

	public ArrayList<Triangle> getTriangles()
	{
		return tris;
	}

	public Node getParent()
	{
		return parent;
	}

	public void setTriangles(ArrayList<Triangle> newTri)
	{
		tris = new ArrayList<Triangle>(newTri);
	}

	public Vertex getCoord()
	{
		return coord;
	}

	public float getHWidth()
	{
		return hWidth;
	}

	public boolean hasVert(Vertex v)
	{
		if(v.x >= coord.x - hWidth && v.x <= coord.x + hWidth &&
			v.y >= coord.y - hWidth && v.y <= coord.y + hWidth &&
			v.z >= coord.z - hWidth && v.z <= coord.z + hWidth)
			return true;
		return false;
	}

	public static void main(String[] args)
	{
		Node n = new Node(new Vertex(-1,-1,-1),2);
		Vertex v3 = new Vertex(-0.45879397f,-0.45879397f,-0.45879397f);
		System.out.println(n.hasVert(v3));

	}
}