import java.util.ArrayList;
import java.util.Iterator;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import java.lang.Thread;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;

public class Icosahedron
{
	private ArrayList<Vertex> baseVertices;
	private ArrayList<Triangle> baseTriangles;
	private ArrayList<Vertex> vertices;
	private ArrayList<Triangle> triangles;
	private ArrayList<Triangle> unseenTriangles;
	private int texture;
	private float height;
	private TextureColor textureColor;
	Icosahedron(long seed, float h, int textureCSeed)
	{
		textureColor = new TextureColor(textureCSeed);
		texture = TextureHeightMap.createHeightMap(seed, textureColor);
		height = h;
		baseVertices = new ArrayList<Vertex>();
		baseTriangles = new ArrayList<Triangle>();
		float t = (float)(1 + Math.sqrt(5.0)) / 2.0f;
		float ang = (float)(Math.atan(-1/t));
		Vertex v1 = new Vertex(-1, t, 0.0f);
		Vertex v2 = new Vertex(1, t, 0.0f);
		Vertex v3 = new Vertex(-1, -t, 0.0f);
		Vertex v4 = new Vertex(1, -t, 0.0f);

		Vertex v5 = new Vertex(0.0f, -1, t);
		Vertex v6 = new Vertex(0.0f, 1, t);
		Vertex v7 = new Vertex(0.0f, -1, -t);
		Vertex v8 = new Vertex(0.0f, 1, -t);

		Vertex v9 = new Vertex(t,0.0f,-1);
		Vertex v10 = new Vertex(t, 0.0f, 1);
		Vertex v11 = new Vertex(-t, 0.0f, -1);
		Vertex v12 = new Vertex(-t, 0.0f, 1);

		Vertex v13 = new Vertex(v1);
		Vertex v14 = v11.getMidV(v12);
		Vertex v15 = new Vertex(v14);
		Vertex v16 = new Vertex(v3);

		v1.normalize();
		v2.normalize();
		v3.normalize();
		v4.normalize();
		v5.normalize();
		v6.normalize();
		v7.normalize();
		v8.normalize();
		v9.normalize();
		v10.normalize();
		v11.normalize();
		v12.normalize();
		v13.normalize();
		v13.u = 0;
		v14.normalize();
		v14.u = 0;
		v15.normalize();
		v15.u = 1;
		v16.normalize();
		v16.u = 0;

		v1.adjustHeight(height);
		v2.adjustHeight(height);
		v3.adjustHeight(height);
		v4.adjustHeight(height);
		v5.adjustHeight(height);
		v6.adjustHeight(height);
		v7.adjustHeight(height);
		v8.adjustHeight(height);
		v9.adjustHeight(height);
		v10.adjustHeight(height);
		v11.adjustHeight(height);
		v12.adjustHeight(height);
		v13.adjustHeight(height);
		v14.adjustHeight(height);
		v15.adjustHeight(height);
		v16.adjustHeight(height);

		baseVertices.add(v1);
		baseVertices.add(v2);
		baseVertices.add(v3);
		baseVertices.add(v4);
		baseVertices.add(v5);
		baseVertices.add(v6);
		baseVertices.add(v7);
		baseVertices.add(v8);
		baseVertices.add(v9);
		baseVertices.add(v10);
		baseVertices.add(v11);
		baseVertices.add(v12);
		baseVertices.add(v13);
		baseVertices.add(v14);
		baseVertices.add(v15);
		baseVertices.add(v16);

		baseTriangles.add(new Triangle(v1,v6,v12));
		baseTriangles.add(new Triangle(v1,v2,v6));// pole
		baseTriangles.add(new Triangle(v13,v8,v2));// pole
		baseTriangles.add(new Triangle(v11,v8,v13));//baseTriangles.add(new Triangle(v1,v8,v11));
		baseTriangles.add(new Triangle(v1,v12,v15));//baseTriangles.add(new Triangle(v1,v11,v12));

		baseTriangles.add(new Triangle(v4,v10,v9));
		baseTriangles.add(new Triangle(v4,v9,v7));
		baseTriangles.add(new Triangle(v4,v7,v16));//pole
		baseTriangles.add(new Triangle(v4,v3,v5));//pole
		baseTriangles.add(new Triangle(v4,v5,v10));

		baseTriangles.add(new Triangle(v6,v5,v12));
		baseTriangles.add(new Triangle(v5,v3,v12));
		baseTriangles.add(new Triangle(v3,v15,v12));//baseTriangles.add(new Triangle(v3,v12,v11));
		baseTriangles.add(new Triangle(v11,v7,v8));
		baseTriangles.add(new Triangle(v8,v7,v9));

		baseTriangles.add(new Triangle(v8,v9,v2));
		baseTriangles.add(new Triangle(v2,v9,v10));
		baseTriangles.add(new Triangle(v10,v6,v2));
		baseTriangles.add(new Triangle(v10,v5,v6));
		baseTriangles.add(new Triangle(v11,v16,v7));//baseTriangles.add(new Triangle(v11,v7,v3));

		baseTriangles.add(new Triangle(v13,v11,v14));
		baseTriangles.add(new Triangle(v16,v14,v11));
		subdivide();
		subdivide();
		vertices = new ArrayList<Vertex>(baseVertices);
		triangles = new ArrayList<Triangle>(baseTriangles);
		unseenTriangles = new ArrayList<Triangle>();
	}

	void subdivide()
	{
		ArrayList<Triangle> newTriangles = new ArrayList<Triangle>();
		ArrayList<Vertex> newVertices = new ArrayList<Vertex>(baseVertices);
		for(int i=0;i<baseTriangles.size();)
		{
			Triangle aTriangle = baseTriangles.get(i);
			baseTriangles.remove(aTriangle);
			subdivideTri(aTriangle, newTriangles, newVertices);
		}
		baseTriangles = newTriangles;
		baseVertices = newVertices;
		triangles = new ArrayList<Triangle>(baseTriangles);
		vertices = new ArrayList<Vertex>(baseVertices);
	}

	void subdivideTri(Triangle tri, ArrayList<Triangle> nTriangles, ArrayList<Vertex> nVertices)
	{
		Vertex v1 = new Vertex(tri.v1);
		v1.normalize();
		Vertex v2 = new Vertex(tri.v2);
		v2.normalize();
		Vertex v3 = new Vertex(tri.v3);
		v3.normalize();

		Vertex v12 = v1.getMidV(v2);
		v12.normalize();
		v12.adjustHeight(height);
		Vertex v23 = v2.getMidV(v3);
		v23.normalize();
		v23.adjustHeight(height);
		Vertex v31 = v3.getMidV(v1);
		v31.normalize();
		v31.adjustHeight(height);

		if(tri.v1.u == 0 && tri.v2.u == 0)
			v12.u = 0;
		if(tri.v2.u == 0 && tri.v3.u == 0)
			v23.u = 0;
		if(tri.v1.u == 0 && tri.v3.u == 0)
			v31.u = 0;

		if(tri.v1.u == 1 && tri.v2.u == 1)
			v12.u = 1;
		if(tri.v2.u == 1 && tri.v3.u == 1)
			v23.u = 1;
		if(tri.v1.u == 1 && tri.v3.u == 1)
			v31.u = 1;

		setPole(tri.v1,tri.v2,v12);
		setPole(tri.v2,tri.v3,v23);
		setPole(tri.v1,tri.v3,v31);

		if(!nVertices.contains(v12))
			nVertices.add(v12);
		else
			v12 = nVertices.get(nVertices.indexOf(v12));

		if(!nVertices.contains(v23))
			nVertices.add(v23);
		else
			v23 = nVertices.get(nVertices.indexOf(v23));

		if(!nVertices.contains(v31))
			nVertices.add(v31);
		else
			v31 = nVertices.get(nVertices.indexOf(v31));

		nTriangles.add(new Triangle(tri.v1,v12,v31));
		nTriangles.add(new Triangle(tri.v2,v23,v12));
		nTriangles.add(new Triangle(tri.v3,v31,v23));
		nTriangles.add(new Triangle(v12,v23,v31));
	}

	void subdivide(Frustum cam, float distance, int levelOfSub, Vector coord)
	{
		ArrayList<Triangle> newTriangles = new ArrayList<Triangle>();
		ArrayList<Triangle> temp = new ArrayList<Triangle>(triangles);
		ArrayList<Triangle> tmp;
		temp.addAll(unseenTriangles);

		triangles = new ArrayList<Triangle>();
		unseenTriangles = new ArrayList<Triangle>();

		for(Triangle aTriangle : temp)
		{
			if(aTriangle.isCloseTo(cam,distance,coord) && aTriangle.level != levelOfSub)
			{
				tmp = new ArrayList<Triangle>();
				subdivideTri(aTriangle,tmp,vertices);
				for(Triangle t : tmp)
				{
					t.level = aTriangle.level + 1;
					if(t.isCloseTo(cam,distance,coord))
					{
						triangles.add(t);
						t.isSeen = true;
					}
					else
					{
						unseenTriangles.add(t);
						t.isSeen = false;
					}
				}
			}
			else
			{
				if(aTriangle.isCloseTo(cam,distance,coord))
				{
					triangles.add(aTriangle);
					aTriangle.isSeen = true;
				}
				else
				{
					unseenTriangles.add(aTriangle);
					aTriangle.isSeen = false;
				}
			}
		}
	}

	void resetSubdivion()
	{
		triangles = new ArrayList<Triangle>(baseTriangles);
		for(Triangle triangle : triangles)
			triangle.isDivided = false;
		vertices = new ArrayList<Vertex>(baseVertices);
		unseenTriangles = new ArrayList<Triangle>();
	}

	void setPole(Vertex v1, Vertex v2, Vertex v12)
	{
		if(!(v1.isPole && v2.isPole))
			return;
		v12.isPole = true;
		v12.u = (v1.u + v2.u)/2;
	}

	void draw()
	{
		float[] vert = new float[vertices.size()*3];
		float[] vertTex = new float[vertices.size()*2];
		int[] idx = new int[triangles.size()*3];
		//float[] normals = new float[vertices.size()*3];
		for(int i=0;i<triangles.size();i++)
		{
			Triangle tr = triangles.get(i);
			idx[i*3] = vertices.indexOf(tr.v1);
			idx[i*3+1] = vertices.indexOf(tr.v2);
			idx[i*3+2] = vertices.indexOf(tr.v3);
		}
		for(int i=0;i<vertices.size();i++)
		{
			Vertex v = vertices.get(i);
			vert[i*3] = v.x;
			vert[i*3+1] = v.y;
			vert[i*3+2] = v.z;
			vertTex[i*2] = v.u;
			vertTex[i*2+1] = v.v;
		}

		FloatBuffer vb = BufferUtils.createFloatBuffer(vert.length);
		vb.put(vert);
		vb.flip();
		FloatBuffer vt = BufferUtils.createFloatBuffer(vertTex.length);
		vt.put(vertTex);
		vt.flip();
		/*FloatBuffer n = BufferUtils.createFloatBuffer(normals.length);
		n.put(normals);
		n.flip();*/
		IntBuffer ind = BufferUtils.createIntBuffer(idx.length);
		ind.put(idx);
		ind.flip();
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_POINT_SMOOTH);
		//glEnable(GL_LIGHTING);
		glBindTexture(GL_TEXTURE_2D, texture);	
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		//glEnableClientState(GL_NORMAL_ARRAY);
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glColor3f(1f,1f,1f);
		glVertexPointer(3, 0, vb);
		//glNormalPointer(0,n);
		glTexCoordPointer(2, 0, vt);
		glDrawElements(GL_TRIANGLES, ind);

		/*glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glEnable(GL_POLYGON_OFFSET_LINE);
		glPolygonOffset( -2, -2 );
		glColor3f( 0, 0, 0);
		glVertexPointer(3, 0, vb);
		glDrawElements(GL_TRIANGLES, ind);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);*/

		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);
		//glDisableClientState(GL_NORMAL_ARRAY);
		//glDisable(GL_LIGHTING);
		glDisable(GL_POINT_SMOOTH);
		glDisable(GL_TEXTURE_2D);
	}
}
