import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.BufferUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
public class Game 
{
	private static final float DEG_TO_RAD = 0.017453293f;
	private static final float MOUSE_TURN_ANGLE = 0.5f;
	private static float lat,lon,mlat,mlon;
	public static Vertex camV,center;
	public static Frustum cam;

	private static int texture;
	public static float distance;
	public static float angleX, angleY, angleZ;
	public static float white_light[]     = { 1.0f, 1.0f, 1.0f, 0.0f }; // default color of light
	public static float light_position0[] = { 0.0f, 10.0f, 0.0f, 1.0f };
	public static DisplayMode display;
	public static int shaderProgram;
	public static int vertexShader;
	public static int fragmentShader;
	public static Tree tree;
	public static Node root;
	static SolarSystem theSolarSystem;

	public Icosahedron ico;

	public void start() 
	{
		try 
		{
			display = new DisplayMode(800,600);
			Display.setDisplayMode(display);
			Display.create();
		} 
		catch (LWJGLException e) 
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		init();
		
		while (!Display.isCloseRequested()) 
		{
			pollInput();
			render();
		}
		
		Display.destroy();
	}

	public void calculateLookpoint()
	{
		float latitude = lat+mlat;
		float longitude = lon+mlon;
		center.x = cam.getCoord().x - (float)(Math.cos((latitude)*DEG_TO_RAD)*Math.sin((longitude)*DEG_TO_RAD) * 1000000);
		center.z = cam.getCoord().z - (float)(Math.cos((latitude)*DEG_TO_RAD)*Math.cos((longitude)*DEG_TO_RAD) * 1000000);
		// this is for the y value of the looking point using the latitude, along the planes xy,zy
		center.y = cam.getCoord().y - (float)(Math.sin((latitude)*DEG_TO_RAD)                            * 1000000);
	}

	public void mouseMotion()
	{
		if(Mouse.isInsideWindow())
		{
			int x = Mouse.getX();
			int y = Mouse.getY();
			int height = display.getHeight();
			int width = display.getWidth();
			int offsetTurningCamera = 20;
			if(lat+60*(height/2-y)/height < 90 && lat+60*(height/2-y)/height > -90)
			{
				// the mouse latitude will be got by refering to the middle of the window's height
				mlat = 60*(height/2-y)/height;
				// this is for turning the view more than the mouse can let
				if(y <= offsetTurningCamera && lat+mlat+MOUSE_TURN_ANGLE < 90)
					lat += MOUSE_TURN_ANGLE;
				if(y >= height - offsetTurningCamera && lat+mlat-MOUSE_TURN_ANGLE > -90)
					lat -= MOUSE_TURN_ANGLE;
			}
			// the mouse longitude will be got by refering to the middle of the window's width
			mlon = 60*(width/2-x)/width;

			// this is for turning the view more than the mouse can let
			if(x <= offsetTurningCamera)
				lon += MOUSE_TURN_ANGLE;
			if(x >= width - offsetTurningCamera)
				lon -= MOUSE_TURN_ANGLE;
		}
	}

	private void pollInput() 
	{
        if (Mouse.isButtonDown(0)) 
        {
		    int x = Mouse.getX();
		    int y = Mouse.getY();
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) 
		{
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_S)) 
		{
			angleZ--;
			angleZ%=360;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			angleZ++;
			angleZ%=360;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			angleY--;
			angleY%=360;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			angleY++;
			angleY%=360;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Z))
		{
			angleX--;
			angleX%=360;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_C))
		{
			angleX++;
			angleX%=360;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_E))
		{
			/*if(distance <= 1)
			{
				distance +=0.01;
				cam.getCoord().x += 0.01;
				cam.getCoord().y += 0.01;
				cam.getCoord().z += 0.01;
			}
			else if(distance < 20)
			{*/
				distance +=10;
				cam.getCoord().x += 0.1; //1000000
				cam.getCoord().y += 0.1;
				cam.getCoord().z += 0.1;
			//}
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
		{
			/*if(distance > 1)
			{*/
				distance-=10;
				cam.getCoord().x -= 0.1;
				cam.getCoord().y -= 0.1;
				cam.getCoord().z -= 0.1;
			/*}
			else if(distance >= 0.1)
			{
				distance-=0.01;
				cam.getCoord().x -= 0.01;
				cam.getCoord().y -= 0.01;
				cam.getCoord().z -= 0.01;
			}*/
		}

		/*while (Keyboard.next()) 
		{
	    	if (Keyboard.getEventKeyState()) 
	    	{

	    	} 
	    	else 
	    	{
	        	if (Keyboard.getEventKey() == Keyboard.KEY_A) 
	        	{
		    		//System.out.println("A Key Released");
	        	}
	    		if (Keyboard.getEventKey() == Keyboard.KEY_S) 
	    		{
		    		//System.out.println("S Key Released");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D) 
				{
		    		//System.out.println("D Key Released");
				}
	    	}
		}*/
    }

    private StringBuilder readShader(String source)
    {
    	StringBuilder string = new StringBuilder();
    	try
		{
			BufferedReader reader = new BufferedReader(new FileReader(source));
			String line;
			while((line = reader.readLine()) != null)
			{
				string.append(line).append('\n');
			}
			reader.close();
			return string;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
    }

	private void init()
	{
		//texture = TextureHeightMap.createHeightMap(0);
		angleX = angleY = angleZ = 0;
		distance = 3f;
		glEnable (GL_DEPTH_TEST) ;
		glShadeModel(GL_SMOOTH); 
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective ((float)48.0, (float) 800/(float) 600, 0.1f, 100000000000.0f); // 10
		//glOrtho(0, 800, 0, 600, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		gluLookAt(distance,distance,distance,
					  0.0f,0.0f,0.0f,
					  0.0f,1.0f,0.0f);
		camV = new Vertex(distance,distance,distance);
		center = new Vertex(0,0,0);
		lat= 45.0f;               // look at sun from above (look down at sun) 
		lon= 45.0f;                // look between axes (at sun) 
		mlat= 0.0f;                // set mouse look at zero
		mlon= 0.0f;       
		Mouse.setCursorPosition(400,300);             
		// init OpenGL here
		FloatBuffer lightPos = BufferUtils.createFloatBuffer(light_position0.length);
		lightPos.put(light_position0);
		lightPos.flip();
		FloatBuffer white = BufferUtils.createFloatBuffer(white_light.length);
		white.put(white_light);
		white.flip();
		glLight(GL_LIGHT0, GL_POSITION, lightPos);
		glLight(GL_LIGHT0, GL_DIFFUSE, white);  // set the diffuse light
		glLight(GL_LIGHT0, GL_SPECULAR, white); // set the specular light
		glEnable(GL_LIGHT0);

		shaderProgram = glCreateProgram();
		vertexShader = glCreateShader(GL_VERTEX_SHADER);
		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		StringBuilder vertSeSrc = new StringBuilder();
		StringBuilder fragSeSrc = new StringBuilder();
		vertSeSrc = readShader("shader.vert");
		fragSeSrc = readShader("shader.frag");

		glShaderSource(vertexShader,vertSeSrc);
		glCompileShader(vertexShader);
		if(glGetShaderi(vertexShader,GL_COMPILE_STATUS) == GL_FALSE)
			System.err.println("Vertex shader couldn't load!");

		glShaderSource(fragmentShader,fragSeSrc);
		glCompileShader(fragmentShader);
		if(glGetShaderi(fragmentShader,GL_COMPILE_STATUS) == GL_FALSE)
			System.err.println("Fragment shader couldn't load!");

		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);
		cam = new Frustum(camV,3);
		/*root = new Node(new Vertex(0,0,0),4);
		
		tree = new Tree(root,5);*/
		//ico = new Icosahedron();
		theSolarSystem = new SolarSystem();
	}

	public void drawAxes()
	{
		float dist = 1000000000;
		glPushMatrix();
		glUseProgram(shaderProgram);
		glBegin(GL_LINES);
		glColor3f(1.0f, 0.0f, 0.0f);
		glVertex3f(0.0f, 0.0f, 0.0f);
		glVertex3f(dist, 0f, 0f);

		glColor3f(0.0f, 1.0f, 0.0f);
		glVertex3f(0.0f, 0.0f, 0.0f);
		glVertex3f(0f, dist, 0f);

		glColor3f(0.0f, 0.0f, 1.0f);
		glVertex3f(0.0f, 0.0f, 0.0f);
		glVertex3f(0f, 0f, dist);
		glEnd();
		glUseProgram(0);
		glPopMatrix();
	}

	public static void drawCube(Vertex location, float hWidth)
	{
		glPushMatrix();
		glBegin(GL_QUAD_STRIP);
		glColor3f(1.0f, 1.0f, 1.0f);

		glVertex3f(location.x + hWidth,location.y - hWidth,location.z + hWidth);
		glVertex3f(location.x - hWidth,location.y - hWidth,location.z + hWidth);
		glVertex3f(location.x + hWidth,location.y - hWidth,location.z - hWidth);
		glVertex3f(location.x - hWidth,location.y - hWidth,location.z - hWidth);

		glVertex3f(location.x + hWidth,location.y + hWidth,location.z - hWidth);
		glVertex3f(location.x - hWidth,location.y + hWidth,location.z - hWidth);

		glVertex3f(location.x + hWidth,location.y + hWidth,location.z + hWidth);
		glVertex3f(location.x - hWidth,location.y + hWidth,location.z + hWidth);

		glVertex3f(location.x + hWidth,location.y - hWidth,location.z + hWidth);
		glVertex3f(location.x - hWidth,location.y - hWidth,location.z + hWidth);
		glEnd();
		glPopMatrix();
	}

	float sx = 1.19f;
	float sy = 0.89f;
	float sz = 4;
	public void drawMap()
	{	
		glEnable(GL_TEXTURE_2D);
		glColor3f(1f, 1f, 1f);
		//glBindTexture(GL_TEXTURE_2D, texture);	
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
		Vertex v17 = v3.getMidV(v4);
		Vertex v18 = new Vertex(v17);
		Vertex v19 = v1.getMidV(v2);
		Vertex v20 = new Vertex(v19);
		/*v1.rotateAZR(ang);
		v2.rotateAZR(ang);
		v3.rotateAZR(ang);
		v4.rotateAZR(ang);
		v5.rotateAZR(ang);
		v6.rotateAZR(ang);
		v7.rotateAZR(ang);
		v8.rotateAZR(ang);
		v9.rotateAZR(ang);
		v10.rotateAZR(ang);
		v11.rotateAZR(ang);
		v12.rotateAZR(ang);*/

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
		v17.normalize();
		v17.u = 0.25f;
		v17.v = 1;
		v18.normalize();
		v18.u = 0.75f;
		v18.v = 1;
		v19.normalize();
		v19.u = 0.25f;
		v19.v = 0;
		v20.normalize();
		v20.u = 0.75f;
		v20.v = 0;

		/*System.out.println(v1.getPrint2());
		System.out.println(v2.getPrint2());
		System.out.println(v3.getPrint2());
		System.out.println(v4.getPrint2());
		System.out.println(v5.getPrint2());
		System.out.println(v6.getPrint2());
		System.out.println(v7.getPrint2());
		System.out.println(v8.getPrint2());
		System.out.println(v9.getPrint2());
		System.out.println(v10.getPrint2());
		System.out.println(v11.getPrint2());
		System.out.println(v12.getPrint2());

		System.exit(0);*/
		glPushMatrix();
		glLoadIdentity();
			glBegin(GL_POLYGON);
			glTexCoord2f(1f,1f); glVertex3f(-sx,-sy,-sz);
			glTexCoord2f(0,1f); glVertex3f(sx,-sy,-sz);
			glTexCoord2f(0,0); glVertex3f(sx,sy,-sz);
			glTexCoord2f(1f,0); glVertex3f(-sx,sy,-sz);
			glEnd();
			glDisable(GL_TEXTURE_2D);
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			glEnable(GL_POLYGON_OFFSET_LINE);
			glPolygonOffset( -2, -2 );
			glColor3f(0,0,0);
			glBegin(GL_TRIANGLES);
			drawT(v1,v12,v6);
			//drawT(v1,v6,v2);
			//drawT(v1,v2,v8);
			drawT(v11,v8,v13);//drawT(v1,v8,v11);
			drawT(v1,v15,v12);//drawT(v1,v11,v12);

			drawT(v4,v10,v9);
			drawT(v4,v9,v7);
			//drawT(v4,v7,v3);
			//drawT(v4,v3,v5);
			drawT(v4,v5,v10);

			drawT(v6,v5,v12);
			drawT(v5,v12,v3);
			drawT(v3,v12,v15);//drawT(v3,v12,v11);
			drawT(v11,v7,v8);
			drawT(v8,v7,v9);

			drawT(v8,v9,v2);
			drawT(v2,v9,v10);
			drawT(v10,v2,v6);
			drawT(v10,v6,v5);
			drawT(v11,v7,v16);//drawT(v11,v7,v3);

			drawT(v13,v11,v14);
			drawT(v16,v11,v14);

			drawT(v16,v7,v17);
			drawT(v7,v17,v4);
			drawT(v4,v18,v5);
			drawT(v5,v18,v3);

			drawT(v13,v8,v19);
			drawT(v8,v19,v2);
			drawT(v2,v20,v6);
			drawT(v20,v6,v1);

			glEnd();
			glDisable(GL_POLYGON_OFFSET_LINE);
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glPopMatrix();
	}

	public void drawT(Vertex v1, Vertex v2, Vertex v3)
	{
		glVertex3f(sx*(v1.u*2-1),sy*(v1.v*2-1),-sz);
		glVertex3f(sx*(v2.u*2-1),sy*(v2.v*2-1),-sz);
		glVertex3f(sx*(v3.u*2-1),sy*(v3.v*2-1),-sz);
	}

	private void render()
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		mouseMotion();
		calculateLookpoint();
		/*gluLookAt(0,30,0,
				  0,0,0,
				  0.0f,0.0f,1.0f);*/
		gluLookAt(cam.getCoord().x ,cam.getCoord().y ,cam.getCoord().z , //100000000f
				  0,0,0,
				  0.0f,1.0f,0.0f);
		drawAxes();
		float r = 1.0f;
		//int t = 0;
		int uv = 0;
		theSolarSystem.drawCelestials();
		int width = (int) (50 - 2*distance);
		int height = (int) (50 - 2*distance);

		/*glRotatef(angleX,1f,0f,0f);
		glRotatef(angleY,0f,1f,0f);
		glRotatef(angleZ,0f,0f,1f);
		ico.draw(texture);
		glPopMatrix();
		//drawMap();*/
		/*
		{
			float[] vertices = new float[(height - 2)*(width )*3 + 6];
			float[] vertTex = new float[(height - 2)*(width )*2 + 4];
			int[] idx = new int[(height-3)*((width-1)*6 + (width-1)*6)];*/
			/*for(int j=1;j<height-1;j++)
			{
				for(int i=0;i<width;i++)
				{ 
					float th = ((float)(j) / (height -1)) * (float) Math.PI;
					float p = ((float)(i) / (width - 1)) * 2 * (float) Math.PI;
					float x = (float)(r*Math.sin(p)*Math.sin(th));
					float y = (float)(r*Math.cos(p)*Math.sin(th));
					float z = (float)(r*Math.cos(th));
					vertices[t++] = x;
					vertices[t++] = y;
					vertices[t++] = z;
					float u = (float)(0.5 + Math.atan2(x,y)/(Math.PI *2));
					float v = (float)(0.5 - Math.asin(z)/Math.PI);
					vertTex[uv++] = u;
					vertTex[uv++] = v;
				}
			}
			vertices[t++]=0; vertices[t++]=0; vertices[t++]= r*1; 
			vertTex[uv++] = (float)(0.5 + Math.atan2(0,0)/(Math.PI *2));
			vertTex[uv++] = (float)(0.5 - Math.asin(r*1)/Math.PI);
	 		vertices[t++]=0; vertices[t++]=0; vertices[t++]= r*-1; 
	 		vertTex[uv++] = (float)(0.5 + Math.atan2(0,0)/(Math.PI *2));
			vertTex[uv++] = (float)(0.5 - Math.asin(-r*1)/Math.PI);
			t = 0;
			for(int j=0; j<height - 3; j++ )
			{
				for(int i=0; i<width - 1; i++ )
				{
					idx[t++] = ((j  )*width + i);
					idx[t++] = ((j+1)*width + i+1);
					idx[t++] = ((j  )*width + i+1);
					idx[t++] = ((j  )*width + i  );
					idx[t++] = ((j+1)*width + i  );
					idx[t++] = ((j+1)*width + i+1);
				}
			}
			for(int i=0; i<width - 1; i++ )
			{
				idx[t++] = ((height - 2)*width);
				idx[t++] = (i);
				idx[t++] = (i+1);
				idx[t++] = ((height - 2)*width+1);
				idx[t++] = ((height - 3)*width + i+1);
				idx[t++] = ((height - 3)*width + i);
			}
		}*/

	  	/*if(!subdivded)
		{
			vert = new ArrayList<Vertex>();
			ind = new ArrayList<TriangleInd>();
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
			int sub = 0;
			float dist = (float)(Math.sqrt(Math.pow(cam.getCoord().x,2) + Math.pow(cam.getCoord().y,2) + Math.pow(cam.getCoord().z,2)));
			isClose = dist < 2;
			subdivide(v1,v12,v6,sub);
			subdivide(v1,v6,v2,sub);
			subdivide(v1,v2,v8,sub);
			subdivide(v1,v8,v11,sub);
			subdivide(v1,v11,v12,sub);

			subdivide(v4,v10,v9,sub);
			subdivide(v4,v9,v7,sub);
			subdivide(v4,v7,v3,sub);
			subdivide(v4,v3,v5,sub);
			subdivide(v4,v5,v10,sub);

			subdivide(v6,v5,v12,sub);
			subdivide(v5,v12,v3,sub);
			subdivide(v3,v12,v11,sub);
			subdivide(v11,v7,v8,sub);
			subdivide(v8,v7,v9,sub);

			subdivide(v8,v9,v2,sub);
			subdivide(v2,v9,v10,sub);
			subdivide(v10,v2,v6,sub);
			subdivide(v10,v6,v5,sub);
			subdivide(v11,v7,v3,sub);
			vertices = new float[vert.size()*3];
			vertTex = new float[vert.size()*2];
			idx = new int[ind.size()*3];
			//normals = new float[ind.size()*3];
			for(int i=0;i<ind.size();i++)
			{
				TriangleInd tr = ind.get(i);
				idx[i*3] = tr.v1;
				idx[i*3+1] = tr.v2;
				idx[i*3+2] = tr.v3;
				float vx, vy, vz, ux, uy, uz;

				vx = vert.get(tr.v1).x - vert.get(tr.v2).x;
				vy = vert.get(tr.v1).y - vert.get(tr.v2).y;
				vz = vert.get(tr.v1).z - vert.get(tr.v2).z;

				ux = vert.get(tr.v2).x - vert.get(tr.v3).x;
				uy = vert.get(tr.v2).y - vert.get(tr.v3).y;
				uz = vert.get(tr.v2).z - vert.get(tr.v3).z;

				normals[i*3] = vy*uz - vz*uy;
				normals[i*3+1] = vz*ux - vx*uz;
				normals[i*3+2] = vx*uy - vy*ux;
			}
			for(int i=0;i<vert.size();i++)
			{
				Vertex v = vert.get(i);
				v.adjustHeight();
				vertices[i*3] = v.x;
				vertices[i*3+1] = v.y;
				vertices[i*3+2] = v.z;
				vertTex[i*2] = v.u;
				vertTex[i*2+1] = v.v;
			}
			subdivded = true;
		}*/
		/*if(!subdivded)
		{
			tree.LOD(root,cam);
			ArrayList<Triangle> triangles = tree.getTriangles();
			vertices = new float[tree.vert.size()*3];
			vertTex = new float[tree.vert.size()*2];
			idx = new int[triangles.size()*3];
			//normals = new float[ind.size()*3];
			for(int i=0;i<triangles.size();i++)
			{
				Triangle tr = triangles.get(i);
				idx[i*3] = tree.vert.indexOf(tr.v1);
				idx[i*3+1] = tree.vert.indexOf(tr.v2);
				idx[i*3+2] = tree.vert.indexOf(tr.v3);
			}
			for(int i=0;i<tree.vert.size();i++)
			{
				Vertex v = tree.vert.get(i);
				//v.adjustHeight();
				vertices[i*3] = v.x;
				vertices[i*3+1] = v.y;
				vertices[i*3+2] = v.z;
				vertTex[i*2] = v.u;
				vertTex[i*2+1] = v.v;
			}
			subdivded = true;
		}*/
		Display.update();
	}

	/* public void subdivide(Vertex v1, Vertex v2, Vertex v3, int sub)
	 {
		boolean done = false;
		Vertex mid = new Vertex((v1.x+v2.x+v3.x)/3,(v1.y+v2.y+v3.y)/3,(v1.z+v2.z+v3.z)/3);
		double dist = Math.sqrt(Math.pow(mid.x-cam.getCoord().x,2) + Math.pow(mid.y-cam.getCoord().y,2) + Math.pow(mid.z-cam.getCoord().z,2));
		//System.out.println(dist);
		if(dist < 1 && sub >= 6)
			done = true;
		else if((dist >= 1 && dist < 2) && sub >= 5)
			done = true;
		else if((dist >= 2 && dist < 3) && sub >= 4)
			done = true;
		else if((dist >= 3 && dist < 4) && sub >= 3)
			done = true;
		else if((dist >= 4 && dist < 5) && sub >= 2)
			done = true;
		else if(dist >= 5 && sub >= 1)
			done = true;

		if(done || sub >= 5)
		{
			float ax,ay,bx,by;
			if(v1.u < v2.u && v1.u < v3.u)
			{
				ax = v2.u - v1.u;
				ay = v2.v - v1.v;

				bx = v3.u - v1.u;
				by = v3.v - v1.v;
			}
			else if(v2.u < v1.u && v2.u < v3.u)
			{
				ax = v1.u - v2.u;
				ay = v1.v - v2.v;

				bx = v3.u - v2.u;
				by = v3.v - v2.v;
			}
			else
			{
				ax = v1.u - v3.u;
				ay = v1.v - v3.v;

				bx = v2.u - v3.u;
				by = v2.v - v3.v;
			}
			double p,s1,s2;
			s1 = Math.sqrt(Math.pow(ax,2) + Math.pow(ay,2));
			s2 = Math.sqrt(Math.pow(bx,2) + Math.pow(by,2));
			p = s2;
			if(s1 > s2)
				p = s1;

			if(Math.abs(p) < 0.51)
			{
				if(!(isClose && dist > 1))
					ind.add(new TriangleInd(vert.indexOf(v1),vert.indexOf(v2),vert.indexOf(v3)));
			}
			else
			{
				if(v1.z == 0)
				{
					if(v2.z == 0)
					{
						//System.out.println(v2.u + " " + v2.v + " " + v3.u + " " + v3.v);
						Vertex v = new Vertex(v1.x,v1.y,v1.z);
						v.normalize();
						Vertex u = new Vertex(v2.x,v2.y,v2.z);
						u.normalize();
						if(v1.y == 1 || v1.y == -1)
							v.u = 0.5f;
						else
							v.u = 0;
						v.v = v1.v;
						u.u = 0;
						u.v = v2.v;

						if(!vert.contains(v))
							vert.add(v);
						if(!vert.contains(u))
							vert.add(u);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v),vert.indexOf(u),vert.indexOf(v3)));
					}
					else if(v3.z == 0)
					{
						Vertex v = new Vertex(v1.x,v1.y,v1.z);
						Vertex u = new Vertex(v3.x,v3.y,v3.z);
						v.normalize();
						u.normalize();
						//if(v1.y == -1 || v1.y == 1)
						//	v.u = 0.5f;
						//else
							v.u = 0;
						v.v = v1.v;
						u.u = 0;
						u.v = v3.v;
						if(!vert.contains(v))
							vert.add(v);
						if(!vert.contains(u))
							vert.add(u);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v),vert.indexOf(v2),vert.indexOf(u)));
					}
					else
					{
						Vertex v = new Vertex(v1.x,v1.y,v1.z);
						v.normalize();
						v.u = 0;
						v.v = v1.v;
						if(!vert.contains(v))
							vert.add(v);
						if(v2.z < 0 && v3.z <0)
						{
							if(!(isClose && dist > 1))
								ind.add(new TriangleInd(vert.indexOf(v),vert.indexOf(v2),vert.indexOf(v3)));
						}
						else if(v2.z < 0)
						{
							Vertex u = new Vertex(v3.x,v3.y,0);
							u.normalize();
							u.u = 0;
							u.v = v3.v;
							if(!vert.contains(u))
								vert.add(u);
							if(!(isClose && dist > 1))
								ind.add(new TriangleInd(vert.indexOf(v),vert.indexOf(v2),vert.indexOf(u)));

							Vertex z = new Vertex(v2.x,v2.y,0);
							z.normalize();
							z.u = 1;
							z.v = v2.v;
							v.u = 1;

							float midHeight = (z.height + u.height)/2;
							z.setHeight(midHeight);
							u.setHeight(midHeight);
							if(!vert.contains(v))
								vert.add(v);
							if(!vert.contains(z))
								vert.add(z);
							if(!(isClose && dist > 1))
								ind.add(new TriangleInd(vert.indexOf(v),vert.indexOf(z),vert.indexOf(v3)));
						}
						else
						{
							Vertex u = new Vertex(v2.x,v2.y,0);
							u.normalize();
							u.u = 0;
							u.v = v2.v;
							if(!vert.contains(u))
								vert.add(u);
							if(!(isClose && dist > 1))
								ind.add(new TriangleInd(vert.indexOf(v),vert.indexOf(u),vert.indexOf(v3)));

							Vertex z = new Vertex(v3.x,v3.y,0);
							z.normalize();
							z.u = 1;
							z.v = v3.v;
							v.u = 1;
							float midHeight = (z.height + u.height)/2;
							z.setHeight(midHeight);
							u.setHeight(midHeight);
							if(!vert.contains(v))
								vert.add(v);
							if(!vert.contains(z))
								vert.add(z);
							if(!(isClose && dist > 1))
								ind.add(new TriangleInd(vert.indexOf(v),vert.indexOf(v2),vert.indexOf(z)));
						}
					}
				}
				else if(v2.z == 0)
				{
					Vertex v = new Vertex(v2.x,v2.y,v2.z);
					v.normalize();
					v.u = 0;
					v.v = v2.v;
					if(!vert.contains(v))
						vert.add(v);
					if(v1.z < 0 && v3.z <0)
					{
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v1),vert.indexOf(v),vert.indexOf(v3)));
					}
					else if(v1.z < 0)
					{
						Vertex u = new Vertex(v1.x,v1.y,0);
						u.normalize();
						u.u = 0;
						u.v = v1.v;
						if(!vert.contains(u))
							vert.add(u);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v1),vert.indexOf(v),vert.indexOf(u)));

						Vertex z = new Vertex(v3.x,v3.y,0);
						z.normalize();
						z.u = 1;
						z.v = v3.v;
						v.u = 1;
						float midHeight = (z.height + u.height)/2;
							z.setHeight(midHeight);
							u.setHeight(midHeight);
						if(!vert.contains(v))
							vert.add(v);
						if(!vert.contains(z))
							vert.add(z);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(z),vert.indexOf(v),vert.indexOf(v3)));
					}
					else
					{
						Vertex u = new Vertex(v3.x,v3.y,0);
						u.normalize();
						u.u = 0;
						u.v = v3.v;
						if(!vert.contains(u))
							vert.add(u);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(u),vert.indexOf(v),vert.indexOf(v3)));

						Vertex z = new Vertex(v1.x,v1.y,0);
						z.normalize();
						z.u = 1;
						z.v = v1.v;
						v.u = 1;
						float midHeight = (z.height + u.height)/2;
							z.setHeight(midHeight);
							u.setHeight(midHeight);
						if(!vert.contains(v))
							vert.add(v);
						if(!vert.contains(z))
							vert.add(z);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v1),vert.indexOf(v),vert.indexOf(z)));
					}
				}
				else if(v3.z == 0)
				{
					Vertex v = new Vertex(v3.x,v3.y,v3.z);
					v.normalize();
					v.u = 0;
					v.v = v3.v;
					if(!vert.contains(v))
						vert.add(v);
					if(v1.z < 0 && v2.z <0)
					{
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v1),vert.indexOf(v2),vert.indexOf(v)));
					}
					else if(v1.z < 0)
					{
						Vertex u = new Vertex(v1.x,v1.y,0);
						u.normalize();
						u.u = 0;
						u.v = v1.v;
						if(!vert.contains(u))
							vert.add(u);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v1),vert.indexOf(u),vert.indexOf(v)));

						Vertex z = new Vertex(v2.x,v2.y,0);
						z.normalize();
						z.u = 1;
						z.v = v2.v;
						v.u = 1;
						float midHeight = (z.height + u.height)/2;
							z.setHeight(midHeight);
							u.setHeight(midHeight);
						if(!vert.contains(v))
							vert.add(v);
						if(!vert.contains(z))
							vert.add(z);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(z),vert.indexOf(v2),vert.indexOf(v)));
					}
					else
					{
						Vertex u = new Vertex(v2.x,v2.y,0);
						u.normalize();
						u.u = 0;
						u.v = v2.v;
						if(!vert.contains(u))
							vert.add(u);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(u),vert.indexOf(v2),vert.indexOf(v)));

						Vertex z = new Vertex(v1.x,v1.y,0);
						z.normalize();
						z.u = 1;
						z.v = v1.v;
						v.u = 1;
						float midHeight = (z.height + u.height)/2;
							z.setHeight(midHeight);
							u.setHeight(midHeight);
						if(!vert.contains(v))
							vert.add(v);
						if(!vert.contains(z))
							vert.add(z);
						if(!(isClose && dist > 1))
							ind.add(new TriangleInd(vert.indexOf(v1),vert.indexOf(z),vert.indexOf(v)));
					}
				}					
			}
			return;
		}
		Vertex v12 = v1.getMidV(v2);
		v12.normalize();
		Vertex v23 = v2.getMidV(v3);
		v23.normalize();
		Vertex v31 = v3.getMidV(v1);
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

		if(!(isClose && dist > 1))
		{
			subdivide(v1,v12,v31,sub+1);
			subdivide(v2,v12,v23,sub+1);
			subdivide(v3,v23,v31,sub+1);
			subdivide(v12,v23,v31,sub+1);
		}
	 }
	*/

	public static void main(String[] argv) 
	{
		/*for(int i=0;i<5000;i++)
		{
			double n = Noise.PerlinNoise(i,i);
			if(n >= 0.8 || n <= -0.8)
				System.out.println(n);
		}*/
		Game displayExample = new Game();
		displayExample.start();
	}
}
