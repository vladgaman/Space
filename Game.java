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
	public static DisplayMode display;
	public static int shaderProgram;
	public static int vertexShader;
	public static int fragmentShader;
	public static Tree tree;
	public static Node root;
	static SolarSystem theSolarSystem;
	int timesFast = 1;
	Icosahedron ico;
	int planetInd = 0;
	int moonInd = 0;
	int sunInd = 0;
	float zoom = 0.1f;
	enum Cam {TOP, LEFT, FRONT, PERSPECTIVE, PLANET, MOON, SUN};
	Cam c = Cam.PERSPECTIVE;
	Cam prevC = c;
	static int seed;
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
		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			System.exit(0);

		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_T))
			c = Cam.TOP;
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_L))
			c = Cam.LEFT;
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_F))
			c = Cam.FRONT;
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_P))
			c = Cam.PERSPECTIVE;
		if(Keyboard.isKeyDown(Keyboard.KEY_LMENU) && Keyboard.isKeyDown(Keyboard.KEY_P))
			c = Cam.PLANET;
		if(Keyboard.isKeyDown(Keyboard.KEY_LMENU) && Keyboard.isKeyDown(Keyboard.KEY_M))
			c = Cam.MOON;
		if(Keyboard.isKeyDown(Keyboard.KEY_LMENU) && Keyboard.isKeyDown(Keyboard.KEY_S))
			c = Cam.SUN;

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) 
		{
			
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S))
		{

		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
		{
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D))
		{
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_E))
		{
			distance += zoom;
			cam.getCoord().x += zoom; //1000000
			cam.getCoord().y += zoom;
			cam.getCoord().z += zoom;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
		{
			distance -= zoom;
			cam.getCoord().x -= zoom;
			cam.getCoord().y -= zoom;
			cam.getCoord().z -= zoom;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) 
		{
			timesFast++;
			System.out.println(timesFast);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
		{
			if(timesFast != 0)
				timesFast--;
			System.out.println(timesFast);
		}

		while (Keyboard.next()) 
		{
			if(Keyboard.getEventKeyState())
			{
				if (Keyboard.getEventKey() == Keyboard.KEY_LEFT)
				{
					if(c == Cam.PLANET)
					{
						planetInd--;
						if(planetInd<0)
							planetInd = theSolarSystem.planets.size()-1;
						System.out.println("Planet: " + planetInd);
					}
					else if(c == Cam.MOON)
					{
						moonInd--;
						if(moonInd<0)
							moonInd = theSolarSystem.moons.size()-1;
						System.out.println("Moon: " + moonInd);
					}
					else if(c == Cam.SUN)
					{
						sunInd--;
						if(sunInd<0)
							sunInd = theSolarSystem.suns.size()-1;
						System.out.println("Sun: " + sunInd);
					}
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) 
				{
					if(c == Cam.PLANET)
					{
						planetInd++;
						planetInd %= theSolarSystem.planets.size();
						System.out.println("Planet: " + planetInd);
					}
					else if(c == Cam.MOON)
					{
						moonInd++;
						moonInd %= theSolarSystem.moons.size();
						System.out.println("Moon: " + moonInd);
					}
					else if(c == Cam.SUN)
					{
						sunInd++;
						sunInd %= theSolarSystem.suns.size();
						System.out.println("Sun: " + sunInd);
					}
				}

				if(Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)
				{
					zoom *= 0.1;
					System.out.println("Zoom: " + zoom);
				}
				if(Keyboard.getEventKey() == Keyboard.KEY_RSHIFT)
				{
					zoom *= 10;
					System.out.println("Zoom: " + zoom);
				}
			}
		}
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
		distance = 40;
		glEnable (GL_DEPTH_TEST) ;
		glShadeModel(GL_SMOOTH); 
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective ((float)48.0, (float) 800/(float) 600, 0.001f, 100000000000.0f); // 10
		//glOrtho(0, 800, 0, 600, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		gluLookAt(distance,distance,distance,
					  0.0f,0.0f,0.0f,
					  0.0f,1.0f,0.0f);
		camV = new Vertex(distance,distance,distance);
		center = new Vertex(0,0,0);
		lat= 45.0f;               // look at sun from above (look down at sun) 
		lon= 80.0f;                // look between axes (at sun) 
		mlat= 0.0f;                // set mouse look at zero
		mlon= 0.0f;       
		Mouse.setCursorPosition(400,300);
		// init OpenGL here
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
		theSolarSystem = new SolarSystem(cam, seed);
	}

	public void drawAxes()
	{
		float dist = 1000000000;
		glPushMatrix();
		//glUseProgram(shaderProgram);
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
		//glUseProgram(0);
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

	public void look()
	{
		switch(c)
		{
			case TOP: gluLookAt(0,100,0, //100000000f
				  			0,0,0,
				  			0.0f,0.0f,1.0f); prevC = c; break;
			case LEFT: gluLookAt(0,0,100, //100000000f
				  			0,0,0,
				  			0.0f,1.0f,0.0f); prevC = c; break;
			case FRONT: gluLookAt(100,0,0, //100000000f
				  			0,0,0,
				  			0.0f,1.0f,0.0f); prevC = c; break;
			case PERSPECTIVE: 
							if(prevC != c)
							{
								distance = 20;
								cam.getCoord().x = cam.getCoord().y = cam.getCoord().z = distance;
							}
							gluLookAt(cam.getCoord().x ,cam.getCoord().y ,cam.getCoord().z , //100000000f
				  			center.x,center.y,center.z,
				  			0.0f,1.0f,0.0f); 
				  			prevC = c; break;
			case PLANET:
						if(theSolarSystem.planets.size() == 0)
							break;
						Planet p = theSolarSystem.planets.get(planetInd);
						cam.getCoord().x = p.getCoordinates().x;
						cam.getCoord().y = p.getCoordinates().y;
						if(prevC != c)
						{
							planetInd = 0;
							distance = p.getRadius() + 1.5f;
							center.x=0;center.y = 0;center.z = 0;
							Vertex ctr = new Vertex(p.getCoordinates().x,p.getCoordinates().y,p.getCoordinates().z);
							ctr.sub(cam.getCoord());
							ctr.normalize();
							lat = 90 - (float)Math.toDegrees(Math.acos(ctr.y));
							ctr.y = 0;
							ctr.normalize();
							lon = (float)Math.toDegrees(Math.acos(ctr.z));
						}
						cam.getCoord().z = p.getCoordinates().z - distance;
						gluLookAt(cam.getCoord().x , cam.getCoord().y ,cam.getCoord().z, //100000000f
				  		p.getCoordinates().x + center.x,p.getCoordinates().y + center.y,p.getCoordinates().z + center.z,
				  		0.0f,1.0f,0.0f); 
				  		prevC = c; break;
			case MOON: 
						if(theSolarSystem.moons.size() == 0)
							break;
						Moon m = theSolarSystem.moons.get(moonInd);
						cam.getCoord().x = m.getCoordinates().x;
						cam.getCoord().y = m.getCoordinates().y;
						if(prevC != c)
						{
							moonInd = 0;
							distance = m.getRadius() + 1.5f;
							center.x=0;center.y = 0;center.z = 0;
							Vertex ctr = new Vertex(m.getCoordinates().x,m.getCoordinates().y,m.getCoordinates().z);
							ctr.sub(cam.getCoord());
							ctr.normalize();
							lat = 90 - (float)Math.toDegrees(Math.acos(ctr.y));
							ctr.y = 0;
							ctr.normalize();
							lon = (float)Math.toDegrees(Math.acos(ctr.z));
						}
						cam.getCoord().z = m.getCoordinates().z - distance;
						gluLookAt(cam.getCoord().x , cam.getCoord().y ,cam.getCoord().z, //100000000f
				  		m.getCoordinates().x + center.x,m.getCoordinates().y + center.y,m.getCoordinates().z + center.z,
				  		0.0f,1.0f,0.0f); 
				  		prevC = c; break;
			case SUN: 
						Sun s = theSolarSystem.suns.get(sunInd);
						cam.getCoord().x = s.getCoordinates().x;
						cam.getCoord().y = s.getCoordinates().y;
						if(prevC != c)
						{
							sunInd = 0;
							distance = s.getRadius() + 1.5f;
							center.x=0;center.y = 0;center.z = 0;
							Vertex ctr = new Vertex(s.getCoordinates().x,s.getCoordinates().y,s.getCoordinates().z);
							ctr.sub(cam.getCoord());
							ctr.normalize();
							lat = 90 - (float)Math.toDegrees(Math.acos(ctr.y));
							ctr.y = 0;
							ctr.normalize();
							lon = (float)Math.toDegrees(Math.acos(ctr.z));
						}
						cam.getCoord().z = s.getCoordinates().z - distance;
						gluLookAt(cam.getCoord().x , cam.getCoord().y ,cam.getCoord().z, //100000000f
				  		s.getCoordinates().x + center.x,s.getCoordinates().y + center.y,s.getCoordinates().z + center.z,
				  		0.0f,1.0f,0.0f); 
				  		prevC = c; break;
		}
	}

	private void render()
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		mouseMotion();
		calculateLookpoint();
		look();

		theSolarSystem.drawCelestials(cam);
		for(int i=0;i<timesFast;i++)
			theSolarSystem.updateCelestials();
		drawAxes();
		Display.update();
	}

	public static void main(String[] argv) 
	{
		/*for(int i=0;i<5000;i++)
		{
			double n = Noise.PerlinNoise(i,i);
			if(n >= 0.8 || n <= -0.8)
				System.out.println(n);
		}*/
		seed = Integer.parseInt(argv[0]);
		Game displayExample = new Game();
		displayExample.start();
	}
}
