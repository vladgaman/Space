import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

public class Tree
{
	private Node root;
	public ArrayList<Vertex> vert;
	public ArrayList<Triangle> tris;
	public Tree(Node aRoot, int depth)
	{
		root = aRoot;
		makeTree(root, depth, root.getHWidth());

		vert = new ArrayList<Vertex>();
		tris = new ArrayList<Triangle>();

		float t = (float)(1 + Math.sqrt(5.0)) / 2.0f;
		Vertex v1 = new Vertex(-1, t, 0.0f);
		v1.normalize();
		Vertex v2 = new Vertex(1, t, 0.0f);
		v2.normalize();
		Vertex v3 = new Vertex(-1, -t, 0.0f);
		v3.normalize();
		Vertex v4 = new Vertex(1, -t, 0.0f);
		v4.normalize();
		Vertex v5 = new Vertex(0.0f, -1, t);
		v5.normalize();
		Vertex v6 = new Vertex(0.0f, 1, t);
		v6.normalize();
		Vertex v7 = new Vertex(0.0f, -1, -t);
		v7.normalize();
		Vertex v8 = new Vertex(0.0f, 1, -t);
		v8.normalize();
		Vertex v9 = new Vertex(t,0.0f,-1);
		v9.normalize();
		Vertex v10 = new Vertex(t, 0.0f, 1);
		v10.normalize();
		Vertex v11 = new Vertex(-t, 0.0f, -1);
		v11.normalize();
		Vertex v12 = new Vertex(-t, 0.0f, 1);
		v12.normalize();
		vert.add(v1);
		vert.add(v2);
		vert.add(v3);
		vert.add(v4);
		vert.add(v5);
		vert.add(v6);
		vert.add(v7);
		vert.add(v8);
		vert.add(v9);
		vert.add(v10);
		vert.add(v11);
		vert.add(v12);

		root.tris.add(new Triangle(v1,v12,v6));
		root.tris.add(new Triangle(v1,v6,v2));
		root.tris.add(new Triangle(v1,v2,v8));
		root.tris.add(new Triangle(v1,v8,v11));
		root.tris.add(new Triangle(v1,v11,v12));

		root.tris.add(new Triangle(v4,v10,v9));
		root.tris.add(new Triangle(v4,v9,v7));
		root.tris.add(new Triangle(v4,v7,v3));
		root.tris.add(new Triangle(v4,v3,v5));
		root.tris.add(new Triangle(v4,v5,v10));

		root.tris.add(new Triangle(v6,v5,v12));
		root.tris.add(new Triangle(v5,v12,v3));
		root.tris.add(new Triangle(v3,v12,v11));
		root.tris.add(new Triangle(v11,v7,v8));
		root.tris.add(new Triangle(v8,v7,v9));

		root.tris.add(new Triangle(v8,v9,v2));
		root.tris.add(new Triangle(v2,v9,v10));
		root.tris.add(new Triangle(v10,v2,v6));
		root.tris.add(new Triangle(v10,v6,v5));
		root.tris.add(new Triangle(v11,v7,v3));
		root.subdivide(vert);
		tris.addAll(root.getTriangles());
	}

	public void LOD(Node currN, Frustum cam) // de lucru
	{
		if(currN == null)
			return;
		if(!checkNodeBound(root,cam))
		{
			tris = new ArrayList<Triangle>();
			tris.addAll(root.getTriangles());
			Game.drawCube(currN.getCoord(),currN.getHWidth());
			return;
		}
		if(currN.isRoot())
			tris = new ArrayList<Triangle>();

		ArrayList<Node> children = currN.getChildren();
		Node tempNode = new Node(currN.getCoord(), currN.getHWidth()*2);
		tempNode.setTriangles((ArrayList<Triangle>)(currN.getTriangles().clone()));
		tempNode.subdivide(vert);
		ArrayList<Triangle> tempTri = (ArrayList<Triangle>)(tempNode.getTriangles().clone());
		if(!children.isEmpty())
			for(Node child : children)
			{
				addTrianglesToNode(tempTri,child);
				if(checkNodeBound(child,cam) && !child.isLeaf())
				{
					LOD(child,cam);
				}
				else
				{
					Game.drawCube(child.getCoord(),child.getHWidth());
					tris.addAll(child.getTriangles());
				}
			}
		if(!tempTri.isEmpty())
		{
			ArrayList<Node> brothers = currN.getParent().getChildren();
			tris.addAll(tempTri);
			for(Node bro : brothers)
			{
				addTrianglesToNode(tempTri,bro);
			}
		}
	}

	public static boolean checkNodeBound(Node node, Frustum cam) // rename it better
	{
		Vertex coordCam = cam.getCoord();
		Vertex coordNode = node.getCoord();
		if(coordCam.x >= coordNode.x - node.getHWidth() && coordCam.x <= coordNode.x + node.getHWidth() &&
			coordCam.y >= coordNode.y - node.getHWidth() && coordCam.y <= coordNode.y + node.getHWidth() &&
			coordCam.z >= coordNode.z - node.getHWidth() && coordCam.z <= coordNode.z + node.getHWidth())
			return true;
		return false;
	}

	public static void addTrianglesToNode(ArrayList<Triangle> triangles, Node node)
	{
		ArrayList<Triangle> newTriang = new ArrayList<Triangle>();
		for(int i = 0 ; i < triangles.size();)
		{
			Triangle aTri = triangles.get(i);
			Vertex mid = aTri.setMid();
			if(node.hasVert(mid))
			{
				newTriang.add(aTri);
				triangles.remove(i);
			}
			else
				i++;
		}
		node.setTriangles(newTriang);
	}

	public ArrayList<Triangle> getTriangles()
	{
		return tris;
	}

	private void makeTree(Node currN, int depth, float hWidth)
	{
		if(depth == 1)
		{
			currN.setAsLeaf();
			return;
		}

		Vertex currCoord = currN.getCoord();
		currN.addChild(new Node(currN, new Vertex(currCoord.x - hWidth/2,currCoord.z - hWidth/2,currCoord.z - hWidth/2), hWidth));
		makeTree(currN.getChild(0),depth-1,hWidth/2);

		currN.addChild(new Node(currN, new Vertex(currCoord.x + hWidth/2,currCoord.z - hWidth/2,currCoord.z - hWidth/2), hWidth));
		makeTree(currN.getChild(1),depth-1,hWidth/2);

		currN.addChild(new Node(currN, new Vertex(currCoord.x - hWidth/2,currCoord.z + hWidth/2,currCoord.z - hWidth/2), hWidth));
		makeTree(currN.getChild(2),depth-1,hWidth/2);

		currN.addChild(new Node(currN, new Vertex(currCoord.x + hWidth/2,currCoord.z + hWidth/2,currCoord.z - hWidth/2), hWidth));
		makeTree(currN.getChild(3),depth-1,hWidth/2);

		currN.addChild(new Node(currN, new Vertex(currCoord.x - hWidth/2,currCoord.z - hWidth/2,currCoord.z + hWidth/2), hWidth));
		makeTree(currN.getChild(4),depth-1,hWidth/2);

		currN.addChild(new Node(currN, new Vertex(currCoord.x + hWidth/2,currCoord.z - hWidth/2,currCoord.z + hWidth/2), hWidth));
		makeTree(currN.getChild(5),depth-1,hWidth/2);

		currN.addChild(new Node(currN, new Vertex(currCoord.x - hWidth/2,currCoord.z + hWidth/2,currCoord.z + hWidth/2), hWidth));
		makeTree(currN.getChild(6),depth-1,hWidth/2);

		currN.addChild(new Node(currN, new Vertex(currCoord.x + hWidth/2,currCoord.z + hWidth/2,currCoord.z + hWidth/2), hWidth));
		makeTree(currN.getChild(7),depth-1,hWidth/2);
	}

	private LinkedList<Node> q = new LinkedList<Node>();
	public void printTree()
	{
		q.clear();
		q.add(root);
		int offsetNewSpace = 2;
		int incr = 0;
		while(q.peek() != null)
		{
			Node temp = q.remove();
			ArrayList<Node> children = temp.getChildren();
			if(temp.isRoot())
				System.out.println(temp.getCoord().getPrint());
			for(Node child : children)
			{
				q.add(child);
				System.out.print(child.getCoord().getPrint() + " | ");
				incr++;
				if(incr == offsetNewSpace)
				{
					incr = 0;
					offsetNewSpace *= 2;
					System.out.println();
				}
			}
		}
	}

	public static void main(String[] args)
	{
		Node n = new Node(new Vertex(0,0,0), 2);
		Triangle t1 = new Triangle(new Vertex(1,1.1f,1),new Vertex(1,1.1f,1),new Vertex(1,1.1f,1));
		Triangle t2 = new Triangle(new Vertex(2,1,1),new Vertex(2,1,1),new Vertex(2,1,1));
		Triangle t3 = new Triangle(new Vertex(1,1,0.9f),new Vertex(1,1,0.9f),new Vertex(1,1,0.9f));
		Triangle t4 = new Triangle(new Vertex(1,1.1f,1),new Vertex(1,1.1f,1),new Vertex(1,1.1f,1));
		ArrayList<Triangle> ts = new ArrayList<Triangle>();
		ts.add(t1);
		ts.add(t2);
		ts.add(t3);
		ts.add(t4);
		addTrianglesToNode(ts,n);
		System.out.println(n.getTriangles().size() + " " + ts.size());
	}
}