import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.util.glu.Sphere;

public class Sun extends CelestialBody
{
	private ArrayList<Planet> planets;
	private float period;
	private Sun barycenter;
	private Vertex color = new Vertex(1,1,0);
	public int first = 0;
	Sun(Vector coord, float rad, float mass,long seed)
	{
		super(coord,rad,mass,seed);
		planets = new ArrayList<Planet>();
	}// constructor

	void orbitsSun(Sun aSun)
	{
		if(!isOrbiting)
		{
			if(first == 0 && aSun.first == 0)
				first = 1;
			else if(aSun.first == 1)
				first = 2;
			isOrbiting = true;
			orbitingTo = aSun;
			aSun.orbitsSun(this);
		}
	}// orbits

	@Override
	void setInitials()
	{
		if(orbitingTo == null)
			return;
		Vector relativeCoord = new Vector(coordinates); 
		// get coordinates of this sun
		relativeCoord.sub(orbitingTo.getCoordinates()); 
		// subtract coordinates of the other sun to get a relative distance to it
		float distance = relativeCoord.getMagnitude();

		// the barycenter between the two suns is:
		Vector barycenterV = new Vector(coordinates);
		// first multiply the coordinates of this with it's mass
		barycenterV.mult(mass);
		// then get the other sun coordinates and multiply with it's mass
		Vector temp = new Vector(orbitingTo.getCoordinates());
		temp.mult(orbitingTo.getMass());
		// add them togheter
		barycenterV.add(temp);
		// and divide them by the sum of their masses
		barycenterV.div(mass + orbitingTo.getMass());
		if(first == 1)
		{
			coordinates.sub(barycenterV);
			orbitingTo.getCoordinates().sub(barycenterV);
			barycenterV = new Vector(0,0,0);
			barycenter = new Sun(barycenterV,distance,mass+orbitingTo.getMass(),-1);
		}
		relativeCoord = new Vector(coordinates);
		//float accelerationS = (float)((G*(orbitingTo.getMass()))/Math.pow(relativeCoord.getMagnitude(),2));
		period = (float)(Math.sqrt( (Math.pow(distance,2)*relativeCoord.getMagnitude()*4*Math.pow(Math.PI,2)) / (G*orbitingTo.getMass()) ));
		float accelerationS = (float)(relativeCoord.getMagnitude()*4*Math.pow(Math.PI,2)/Math.pow(period,2));
		//float speedS = (float)(Math.sqrt(accelerationS * relativeCoord.getMagnitude()));
		float speedS = (float)(2*Math.PI*relativeCoord.getMagnitude()/period);

		relativeCoord.normalize();
		accelerationV = new Vector(relativeCoord);
		accelerationV.mult(-accelerationS);

		if(relativeCoord.y != 0)
			speedV = relativeCoord.cross(new Vector(relativeCoord.x,0,relativeCoord.z));
		else
			speedV = relativeCoord.cross(new Vector(relativeCoord.x,1,relativeCoord.z));
		speedV.normalize();
		speedV.mult(speedS);
		//coordinates.add(speedV);
	}

	@Override
	void updateLocation()
	{
		if(orbitingTo == null)
			return;
		Vector relativeCoord = new Vector(coordinates);
		float distance = relativeCoord.getMagnitude();
		//float accelerationS = (float)((G*(orbitingTo.getMass()))/Math.pow(distance,2));
		float accelerationS = (float)(relativeCoord.getMagnitude()*4*Math.pow(Math.PI,2)/Math.pow(period,2));
		relativeCoord.normalize();
		accelerationV = new Vector(relativeCoord);
		accelerationV.mult(-accelerationS);
		speedV.add(accelerationV);
		coordinates.add(speedV);
	}// updateLocation

	void addPlanet(Planet aPlanet)
	{
		planets.add(aPlanet);
		if(barycenter == null)
			aPlanet.orbits(this);
		else
		{
			aPlanet.orbits(barycenter);
		}
	}// addPlanet

	Sun getBarycenter()
	{
		return barycenter;
	}

	void setColor(Vertex col)
	{
		color = new Vertex(col.x/255,col.y/255,col.z/255);
	}

	@Override
	void draw(Frustum cam)
	{
		glPushMatrix();
		glTranslatef(coordinates.x,coordinates.y,coordinates.z);
		glColor3f(color.x,color.y,color.z);
		Sphere s = new Sphere();
		s.draw(radius,10,10);

		glPopMatrix();
	}
}