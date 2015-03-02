import java.util.ArrayList;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;

public class Icosahedron
{
	private ArrayList<Vertex> vertices;
	private ArrayList<Triangle> triangles;
	Icosahedron()
	{
		vertices = new ArrayList<Vertex>();
		triangles = new ArrayList<Triangle>();
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
		v1.isPole = true;	
		v2.isPole = true;
		v13.isPole = true;
		v3.isPole = true;
		v4.isPole = true;
		v16.isPole = true;

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

		v1.adjustHeight();
		v2.adjustHeight();
		v3.adjustHeight();
		v4.adjustHeight();
		v5.adjustHeight();
		v6.adjustHeight();
		v7.adjustHeight();
		v8.adjustHeight();
		v9.adjustHeight();
		v10.adjustHeight();
		v11.adjustHeight();
		v12.adjustHeight();
		v13.adjustHeight();
		v14.adjustHeight();
		v15.adjustHeight();
		v16.adjustHeight();

		vertices.add(v1);
		vertices.add(v2);
		vertices.add(v3);
		vertices.add(v4);
		vertices.add(v5);
		vertices.add(v6);
		vertices.add(v7);
		vertices.add(v8);
		vertices.add(v9);
		vertices.add(v10);
		vertices.add(v11);
		vertices.add(v12);
		vertices.add(v13);
		vertices.add(v14);
		vertices.add(v15);
		vertices.add(v16);

		triangles.add(new Triangle(v1,v12,v6));
		triangles.add(new Triangle(v1,v6,v2));// pole
		triangles.add(new Triangle(v13,v2,v8));// pole
		triangles.add(new Triangle(v11,v8,v13));//triangles.add(new Triangle(v1,v8,v11));
		triangles.add(new Triangle(v1,v15,v12));//triangles.add(new Triangle(v1,v11,v12));

		triangles.add(new Triangle(v4,v10,v9));
		triangles.add(new Triangle(v4,v9,v7));
		triangles.add(new Triangle(v4,v7,v16));//pole
		triangles.add(new Triangle(v4,v3,v5));//pole
		triangles.add(new Triangle(v4,v5,v10));

		triangles.add(new Triangle(v6,v5,v12));
		triangles.add(new Triangle(v5,v12,v3));
		triangles.add(new Triangle(v3,v12,v15));//triangles.add(new Triangle(v3,v12,v11));
		triangles.add(new Triangle(v11,v7,v8));
		triangles.add(new Triangle(v8,v7,v9));

		triangles.add(new Triangle(v8,v9,v2));
		triangles.add(new Triangle(v2,v9,v10));
		triangles.add(new Triangle(v10,v2,v6));
		triangles.add(new Triangle(v10,v6,v5));
		triangles.add(new Triangle(v11,v7,v16));//triangles.add(new Triangle(v11,v7,v3));

		triangles.add(new Triangle(v13,v11,v14));
		triangles.add(new Triangle(v16,v11,v14));

		subdivide();
	}

	void subdivide()
	{
		if(triangles.isEmpty())
			return;
		ArrayList<Triangle> newTriangles = new ArrayList<Triangle>();
		for(int i=0;i<triangles.size();)
		{
			Triangle aTriangle = triangles.get(i);
			subdivideTri(aTriangle, newTriangles);
		}
		triangles = new ArrayList<Triangle>(newTriangles);
	}

	void subdivideTri(Triangle tri, ArrayList<Triangle> nTriangles)
	{
		triangles.remove(tri);
		Vertex v1 = new Vertex(tri.v1);
		v1.normalize();
		Vertex v2 = new Vertex(tri.v2);
		v2.normalize();
		Vertex v3 = new Vertex(tri.v3);
		v3.normalize();

		Vertex v12 = v1.getMidV(v2);
		v12.normalize();
		v12.adjustHeight();
		Vertex v23 = v2.getMidV(v3);
		v23.normalize();
		v23.adjustHeight();
		Vertex v31 = v3.getMidV(v1);
		v31.normalize();
		v31.adjustHeight();

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

		if(!vertices.contains(v12))
			vertices.add(v12);
		else
			v12 = vertices.get(vertices.indexOf(v12));

		if(!vertices.contains(v23))
			vertices.add(v23);
		else
			v23 = vertices.get(vertices.indexOf(v23));

		if(!vertices.contains(v31))
			vertices.add(v31);
		else
			v31 = vertices.get(vertices.indexOf(v31));

		nTriangles.add(new Triangle(tri.v1,v12,v31));
		nTriangles.add(new Triangle(tri.v2,v12,v23));
		nTriangles.add(new Triangle(tri.v3,v23,v31));
		nTriangles.add(new Triangle(v12,v23,v31));
	}

	void setPole(Vertex v1, Vertex v2, Vertex v12)
	{
		if(!(v1.isPole && v2.isPole))
			return;
		v12.isPole = true;
		v12.u = (v1.u + v2.u)/2;
	}

	void draw(int texture)
	{
		float[] vert = new float[vertices.size()*3];
		float[] vertTex = new float[vertices.size()*2];
		int[] idx = new int[triangles.size()*3];
		//normals = new float[ind.size()*3];
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

		glBindTexture(GL_TEXTURE_2D, texture);	
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glEnableClientState(GL_NORMAL_ARRAY);
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glColor3f(1f,1f,1f);
		glTexCoordPointer(2, 0, vt);
		//glNormalPointer(0,n);
		glVertexPointer(3, 0, vb);
		glDrawElements(GL_TRIANGLES, ind);

		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glEnable(GL_POLYGON_OFFSET_LINE);
		glPolygonOffset( -2, -2 );
		glColor3f( 0, 0, 0 );
		glVertexPointer(3, 0, vb);
		glDrawElements(GL_TRIANGLES, ind);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glDisable(GL_POLYGON_OFFSET_LINE);
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_NORMAL_ARRAY);
		glDisable(GL_LIGHTING);
		glDisable(GL_POINT_SMOOTH);
		glDisable(GL_TEXTURE_2D);
	}
}