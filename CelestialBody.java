import java.lang.Math;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;

public class CelestialBody
{
	protected static float G = (float)(6.67 * Math.pow(10,-11));
	protected Vector coordinates;
	protected Vector speedV;
	protected Vector accelerationV;
	protected float radius;
	protected float mass;
	protected Vector upV; // shows where north is and how the planet rotates
	protected boolean isOrbiting = false;
	protected CelestialBody orbitingTo = null;
	protected Icosahedron body;
	public static int time = 10;
	protected int textureCSeed = 31; 
	CelestialBody(Vector coord, float rad, float m, long seed) 
	{
		coordinates = coord;
		radius = rad;
		mass = m;
		if(seed >= 0)
			body = new Icosahedron(seed,rad,(int)(textureCSeed*seed));
	}// constructor

	CelestialBody(Vector coord, float rad, float m, long seed, boolean isHabitable) 
	{
		coordinates = coord;
		radius = rad;
		mass = m;
		if(seed >= 0)
		{
			if(isHabitable)
				body = new Icosahedron(seed,rad,-1);
			else
				body = new Icosahedron(seed,rad,(int)(textureCSeed*seed));
		}
	}// constructor

	void orbits(CelestialBody body)
	{
		isOrbiting = true;
		orbitingTo = body;
	}// orbits

	void setInitials()
	{
		if(orbitingTo == null)
			return;
		Vector relativeCoord = new Vector(coordinates);
		relativeCoord.sub(orbitingTo.getCoordinates());
		float distance = relativeCoord.getMagnitude();
		float accelerationS = (float)((G*orbitingTo.getMass())/Math.pow(distance,2));
		/*float eccentricity = 0.3f;
		float majorAxis = distance / (1 - eccentricity);
		float focalDist = majorAxis - distance;
		float minorAxis = (float)(Math.sqrt(Math.pow(majorAxis,2) - Math.pow(focalDist,2)));*/
		float speedS = (float)(Math.sqrt(accelerationS * distance));

		relativeCoord.normalize();
		accelerationV = new Vector(relativeCoord);
		accelerationV.mult(-accelerationS);
		if(relativeCoord.y > 0)
			speedV = relativeCoord.cross(new Vector(-relativeCoord.x,0,-relativeCoord.z));
		else if(relativeCoord.y < 0)
			speedV = relativeCoord.cross(new Vector(relativeCoord.x,0,relativeCoord.z));
		else
			speedV = relativeCoord.cross(new Vector(relativeCoord.x,1,relativeCoord.z));
		speedV.normalize();
		speedV.mult(speedS);
		// coordinates.add(speedV);
	}// setInitials

	void updateLocation() 
	{
		if(orbitingTo == null)
			return;

		Vector relativeCoord = new Vector(coordinates);
		relativeCoord.sub(orbitingTo.getCoordinates());
		float distance = relativeCoord.getMagnitude();
		float accelerationS = (float)((G*orbitingTo.getMass())/Math.pow(distance,2));
		relativeCoord.normalize();
		accelerationV = new Vector(relativeCoord);
		accelerationV.mult(-accelerationS);
		speedV.add(accelerationV);
		coordinates.add(speedV);
	}// updateLocation

	float getMass()
	{
		return mass;
	}// getMass

	float getRadius()
	{
		return radius;
	}// getRadius

	Vector getCoordinates()
	{
		return coordinates;
	}// getMass

	Vector getAcceleration()
	{
		if(speedV == null)
			return null;	
		Vector v = new Vector(accelerationV);
		return v;
	}// getAcceleration

	Vector getSpeed()
	{
		if(speedV == null)
			return null;
		Vector v = new Vector(speedV);
		return v;
	}// getSpeed

	void setSpeed(Vector speed)
	{
		speedV = speed;
	}

	void draw(Frustum cam)
	{
		float distance = radius;
		Vector dist = new Vector(cam.getCoord().x,cam.getCoord().y,cam.getCoord().z); 
		dist.sub(coordinates);
		glPushMatrix();
		glTranslatef(coordinates.x,coordinates.y,coordinates.z);
		if(dist.getMagnitude() < distance)
		{
			body.subdivide(cam,0.6f*radius,3,coordinates);
		}
		else
			body.resetSubdivion();
		body.draw();
		glTranslatef(-coordinates.x,-coordinates.y,-coordinates.z);
		glPopMatrix();
	}

	boolean isOrbiting()
	{
		return isOrbiting;
	}
}