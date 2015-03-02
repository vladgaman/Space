import java.util.ArrayList;
import java.lang.Math;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.util.glu.Sphere;

public class SolarSystem
{
	private ArrayList<Planet> planets;
	private ArrayList<Sun> suns;
	/*private ArrayList<> comets;
	private ArrayList<> asteroids;*/
	public SolarSystem()
	{
		suns = new ArrayList<Sun>();
		planets = new ArrayList<Planet>();
		double seedNo = Noise.noise(31)*1000000;
		int noOfPlanets = (int)(Noise.noise((int)(seedNo))*20); // at most 20 planets
		int noOfSuns = (int)(Math.round(Noise.noise((int)(seedNo)) + 1)); // one or two

		float solarM = (float)(1.9891 * Math.pow(10,30)); // it is 10 to power of 30 for a solar mass
		float planetaryM = (float)(5.9736 * Math.pow(10,24));
		float au = 149597871;
		Sun firstSun = new Sun(new Vector(0,0,0), 1, 10000000);
		float safeDistance = 1 + 1;
		if(noOfSuns == 2)
		{
			float distance = (float)(Noise.noise((int)seedNo)*5);
			Sun secondSun = new Sun(new Vector(0,0,distance), 1, 10000000);

			firstSun.orbitsSun(secondSun);
			firstSun.setInitials();
			secondSun.setInitials();
			suns.add(secondSun);
			safeDistance = (firstSun.getBarycenter()).getRadius() + 1;
		}
		suns.add(firstSun);

		System.out.println(noOfPlanets);
		for(int i=0; i<noOfPlanets;i++)
		{
			float distance = (float)(safeDistance + 2*Noise.noise((int)(seedNo + i)));
			Planet planet = new Planet(new Vector(distance,0,0),1,1000);
			Planet moon = new Planet(new Vector(distance+1,0,0),1,1000);
			firstSun.addPlanet(planet);
			planet.setInitials();
			planets.add(planet);
			
			planet.addMoon(moon);
			moon.setInitials();
			planets.add(moon);
		}
	}

	public void drawCelestials()
	{
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		for(Sun aSun : suns)
		{
			glPushMatrix();
			glTranslatef(aSun.getCoordinates().x,aSun.getCoordinates().y,aSun.getCoordinates().z);
			Sphere s = new Sphere();
			s.draw(0.1f,10,10);

			glPopMatrix();
		}

		for(Sun aSun : suns)
		{
			aSun.updateLocation();
		}

		for(Planet p : planets)
		{
			glPushMatrix();
			glTranslatef(p.getCoordinates().x,p.getCoordinates().y,p.getCoordinates().z);
			Sphere s = new Sphere();
			s.draw(0.1f,10,10);

			glBegin(GL_LINES);
			glColor3f(1.0f, 0.0f, 0.0f);
			glVertex3f(0,0,0);
			glVertex3f(p.getAcceleration().x*1000,p.getAcceleration().y*1000,p.getAcceleration().z*1000);

			glColor3f(0.0f, 1.0f, 0.0f);
			glVertex3f(0,0,0);
			glVertex3f(p.getSpeed().x*10,p.getSpeed().y*10,p.getSpeed().z*10);
			glEnd();

			glPopMatrix();
			p.updateLocation();
		}
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}
}