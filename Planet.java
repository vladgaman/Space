import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;

public class Planet extends CelestialBody
{
	protected ArrayList<Moon> moons;

	Planet(Vector coord, float rad, float m, long seed, boolean isHabitable)
	{
		super(coord,rad,m,seed,isHabitable);
		moons = new ArrayList<Moon>();
	}

	void addMoon(Moon aMoon)
	{
		moons.add(aMoon);
		aMoon.orbits(this);
	}

	Moon getMoon(int ind)
	{
		return moons.get(ind);
	}

	@Override
	void updateLocation()
	{
		super.updateLocation();
		for(Moon moon : moons)
		{
			moon.getCoordinates().add(speedV);
		}
	}

	void draw(Frustum cam, float sR)
	{
		float distance = radius;
		Vector dist = new Vector(cam.getCoord().x,cam.getCoord().y,cam.getCoord().z); 
		dist.sub(coordinates);
		Vector coord = new Vector(coordinates);
		coord.div(sR);
		glPushMatrix();
		glTranslatef(coord.x,coord.y,coord.z);
		if(dist.getMagnitude() < distance)
		{
			body.subdivide(cam,0.6f*radius,3,coord);
		}
		else
			body.resetSubdivion();
		body.draw();
		glPopMatrix();
	}
}