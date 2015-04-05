import java.util.ArrayList;
import java.lang.Math;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

public class SolarSystem
{
	private static float G = (float)(6.67 * Math.pow(10,-11));
	public ArrayList<Planet> planets;
	public ArrayList<Sun> suns;
	public ArrayList<Moon> moons;
	private static float solarM = (float)(1.9891 * Math.pow(10,8)); // it is 10 to power of 30 for a solar mass kg
	private static float planetaryM = (float)(5.9736 * Math.pow(10,2)); // it is 10 to power of 24 for a planetary mass kg
	private static float solarDens = (float)(1.408 * Math.pow(10,-10)); // it is 10 to power of 12 for kg/km3
	private static float planetaryDens = (float)(5.515 * Math.pow(10,-10)); // it is 10 to power of 12 for kg/km3 ;
	private static float au = 149597871; // astronomical unit km 
	private static float solarRad;
	private int seedNo;
	private Frustum camera;
	float scaleRadius;
	/*private ArrayList<> comets;
	private ArrayList<> asteroids;*/

	public static FloatBuffer white;
	public SolarSystem(Frustum cam, int initialSeed)
	{
		camera = cam;
		suns = new ArrayList<Sun>();
		planets = new ArrayList<Planet>();
		moons = new ArrayList<Moon>();
		seedNo = (int)(Noise.noise(initialSeed)*1000000);
		int noOfPlanets = (int)(((Noise.noise(seedNo) + 1)/2)*20); // at most 20 planets
		int noOfSuns = (int)(Math.round(Noise.noise(seedNo) + 1)); // one or two
		solarRad = getRadius(solarM,solarDens);
		// start adding sun (suns)
		float mass = getProceduralMass(noOfPlanets+noOfSuns,0.08*solarM,30*solarM);
		//scaleRadius = getRadius(mass,solarDens)/au;
		float radius = getRadius(mass,solarDens);
		scaleRadius = radius;
		long seed;
		Sun firstSun = new Sun(new Vector(0,0,0), 1, mass, -1);
		suns.add(firstSun);

		float luminosity1 = getLuminosity(radius, mass, firstSun);// our sun has aprox 6000 K
		float luminosity2, habitInRad = 1, habitOutRad = 1;
		/*System.out.println(luminosity1);
		System.exit(0);*/
		
		/*float white_light[] = { 1.0f, 1.0f, 1.0f, 0.0f }; // default color of light
		white = BufferUtils.createFloatBuffer(white_light.length);
		white.put(white_light);
		white.flip();

		float light_position0[] = { firstSun.getCoordinates().x, firstSun.getCoordinates().y, firstSun.getCoordinates().z,1.0f };
		FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(light_position0.length);
		lightPos0.put(light_position0);
		lightPos0.flip();
		glLight(GL_LIGHT0, GL_POSITION, lightPos0);
		glLight(GL_LIGHT0, GL_SPECULAR, white); // set the specular light
		glLight(GL_LIGHT0, GL_DIFFUSE, white);  // set the diffuse light
		glEnable(GL_LIGHT0);*/

		float dist;
		Sun secondSun = new Sun(new Vector(0,0,0),1,1,-1); // stub
		if(noOfSuns == 2)
		{
			mass = getProceduralMass(noOfPlanets+noOfSuns+1,0.08*solarM,30*solarM);
			radius = getRadius(mass,solarDens);
			float distance = 1;
			if( firstSun.getMass() > mass )
				distance = getRocheLimit(radius/scaleRadius,firstSun.getMass(),mass);
			else
				distance = getRocheLimit(firstSun.getRadius(),mass,firstSun.getMass());
			distance = getLerp(getAPercentage(seedNo) ,distance,distance*10);
			secondSun = new Sun(new Vector(0,0,distance), radius/scaleRadius,mass,-1);

			firstSun.orbitsSun(secondSun);
			firstSun.setInitials();
			secondSun.setInitials();
			suns.add(secondSun);

			luminosity2 = getLuminosity(radius,mass,secondSun);// our sun has aprox 6000 K
			float luminSum = luminosity1 + luminosity2;
			habitInRad *= (float)(Math.sqrt(luminSum/1.1));
			habitOutRad *= (float)(Math.sqrt(luminSum/0.53));
			/*float light_position1[] = {secondSun.getCoordinates().x, secondSun.getCoordinates().y, secondSun.getCoordinates().z,1.0f};
			FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(light_position1.length);
			lightPos1.put(light_position1);
			lightPos1.flip();
			glLight(GL_LIGHT1, GL_POSITION, lightPos1);
			glLight(GL_LIGHT1, GL_SPECULAR, white); // set the specular light
			glLight(GL_LIGHT1, GL_DIFFUSE, white);  // set the diffuse light
			glEnable(GL_LIGHT1);*/
		}
		else
		{
			habitInRad *= (float)(Math.sqrt(luminosity1/1.1));
			habitOutRad *= (float)(Math.sqrt(luminosity1/0.53));
			firstSun.setInitials();
		}
		float safeDistance = 1;

		System.out.println(habitInRad + " " + habitOutRad);

		// start adding planets
		float percent = getAPercentage(seedNo);
		mass = (percent <= 0.7) ? getProceduralMass(0,0.02*planetaryM,20*planetaryM) : getProceduralMass(0,20*planetaryM,4000*planetaryM);
		radius = getRadius(mass,planetaryDens)/scaleRadius;

		if(noOfSuns == 1)
			safeDistance = getRocheLimit(radius,firstSun.getMass(),mass) + firstSun.getRadius();
		else 
		{
			safeDistance = firstSun.getBarycenter().getRadius();
			if(firstSun.getMass() > secondSun.getMass())
				safeDistance += getRocheLimit(radius,secondSun.getMass(),mass) + secondSun.getRadius();
			else
				safeDistance += getRocheLimit(radius,firstSun.getMass(),mass) + firstSun.getRadius();
		}

		seed = (long)((Noise.noise(seedNo*noOfPlanets) + 1)*1000000/2);
		Vector position = getProceduralVector(seedNo,safeDistance);
		dist = position.getMagnitude();
		boolean isHabit = (dist >= habitInRad && dist <= habitOutRad);
		Planet planet = new Planet(position,radius,mass ,seed,isHabit);
		firstSun.addPlanet(planet);
		planet.setInitials();
		planets.add(planet);
		System.out.println("Planet 1 done!");

		for(int i=1; i<noOfPlanets;i++)
		{
			float distance = 0;
			float accelerationS = 1;
			while(i != 0 && accelerationS > Math.pow(10,-7))
			{
				accelerationS = (float)((G*planets.get(i-1).getMass())/Math.pow(distance,2));
				distance += 10;
			}
			percent = getAPercentage(seedNo + i);
			mass = (percent <= 0.7) ? getProceduralMass(i*seedNo,0.02*planetaryM,20*planetaryM) : getProceduralMass(i*seedNo,20*planetaryM,4000*planetaryM);
			radius = getRadius(mass,planetaryDens)/scaleRadius;
			distance += radius + planets.get(i-1).getRadius();
			distance += planets.get(i-1).getCoordinates().getMagnitude();
			seed = (long)((Noise.noise(seedNo*noOfPlanets + i) + 1)*1000000/2);
			position = getProceduralVector((int)(((Noise.noise(i) + 1)/2) * 100), distance);
			dist = position.getMagnitude();
			isHabit = (dist >= habitInRad && dist <= habitOutRad);
			planet = new Planet(position ,radius, mass,seed,isHabit);
			System.out.println(dist);
			firstSun.addPlanet(planet);
			planet.setInitials();
			planets.add(planet);
			System.out.println("Planet " + (i+1) + " done!");
		}
		// start adding moons
		addMoons(seedNo * planets.size());
	}

	private void addMoons(int aSeed)
	{
		Planet aPlanet = planets.get(0);
		float probToHaveMoon = (aPlanet.getMass() * 5)/planetaryM;
		float compareProb = (float)((Noise.noise((int)(aPlanet.getMass()  + probToHaveMoon)) + 1)*5);
		float mass, radius, fDistance, sDistance, distance; Vector p; Moon moon; long seed;
		int j = 0;
		if(compareProb >= probToHaveMoon)
		{
			mass = getProceduralMass(aSeed*2,0.02*planetaryM,aPlanet.getMass()/3);
			radius = getRadius(mass,planetaryDens)/scaleRadius;
			if(suns.get(0).isOrbiting())
				fDistance = (aPlanet.getCoordinates().getMagnitude() - suns.get(0).getBarycenter().getRadius() - aPlanet.getRadius())/2;
			else
				fDistance = (aPlanet.getCoordinates().getMagnitude() - aPlanet.getRadius() - suns.get(0).getRadius())/2;
			if(planets.size() > 1)
				sDistance = (planets.get(1).getCoordinates().getMagnitude() - aPlanet.getCoordinates().getMagnitude() - planets.get(1).getRadius())/2;
			else
				sDistance = fDistance;
			distance = (fDistance < sDistance) ? fDistance : sDistance;
			distance = (float)(((Noise.noise(aSeed*2) + 1)/2 * (distance/2)) + aPlanet.getRadius() + radius);
			p = new Vector(aPlanet.getCoordinates());
			p.add(getProceduralVector(aSeed*2,distance));
			seed = (long)((Noise.noise(seedNo + planets.size()) + 1)*1000000/2);
			moon = new Moon( p,radius, mass,seed);
			aPlanet.addMoon(moon);
			moon.setInitials();
			moons.add(moon);
			j++;
			System.out.println("Moon " + j + " done to planet 0!");
		}
		
		if(planets.size() == 1)
			return;
		for(int i=1;i<planets.size()-1;i++)
		{
			Planet currentPlanet = planets.get(i);
			probToHaveMoon = (currentPlanet.getMass() * 5)/planetaryM;
			compareProb = (float)((Noise.noise((int)(currentPlanet.getMass()  + probToHaveMoon)) + 1)*5);
			if(compareProb < probToHaveMoon)
				continue;
			Planet prevPlanet = planets.get(i-1);
			Planet nextPlanet = planets.get(i+1);
			mass = getProceduralMass((aSeed+i)*2,0.02*planetaryM,currentPlanet.getMass()/3);
			radius = getRadius(mass,planetaryDens)/scaleRadius;

			fDistance = ( - prevPlanet.getCoordinates().getMagnitude() + currentPlanet.getCoordinates().getMagnitude() - prevPlanet.getRadius())/2;
			sDistance = ( - currentPlanet.getCoordinates().getMagnitude() + nextPlanet.getCoordinates().getMagnitude() - nextPlanet.getRadius())/2;

			distance = (fDistance < sDistance) ? fDistance : sDistance;
			distance = (float)(((Noise.noise((aSeed+i)*2) + 1)/2 * (distance/2)) + currentPlanet.getRadius() + radius);
			p = new Vector(currentPlanet.getCoordinates());
			p.add(getProceduralVector((aSeed+i)*2,distance));
			j++;
			seed = (long)((Noise.noise(seedNo*j + planets.size()) + 1)*1000000/2);
			moon = new Moon(p,radius, mass,seed);
			currentPlanet.addMoon(moon);
			moon.setInitials();
			moons.add(moon);
			System.out.println("Moon " + j + " done to planet " + i + "!");
		}

		aPlanet = planets.get(planets.size()-1);
		probToHaveMoon = (aPlanet.getMass() * 5)/planetaryM;
		compareProb = (float)((Noise.noise((int)(aPlanet.getMass() + probToHaveMoon)) + 1)*5);
		if(compareProb >= probToHaveMoon)
		{
			mass = getProceduralMass((aSeed+planets.size()-1)*2,0.02*planetaryM,aPlanet.getMass()/3);
			radius = getRadius(mass,planetaryDens)/scaleRadius;
			distance = (-planets.get(planets.size()-2).getCoordinates().getMagnitude() + aPlanet.getCoordinates().getMagnitude() - planets.get(planets.size()-2).getRadius())/2;
			distance = (float)(((Noise.noise((aSeed+planets.size()-1)*2) + 1)/2 * (distance/2)) + aPlanet.getRadius() + radius);
			p = new Vector(aPlanet.getCoordinates());
			p.add(getProceduralVector((aSeed+planets.size()-1)*2,distance));
			j++;
			seed = (long)((Noise.noise(seedNo*j + planets.size())+1)*1000000/2);
			moon = new Moon( p,radius, mass,seed);
			aPlanet.addMoon(moon);
			moon.setInitials();
			moons.add(moon);
			System.out.println("Moon " + j + " done to planet " + planets.size() + "!");
		}
	}

	private float getRocheLimit(float secRadius, float primMass, float secMass)
	{
		return (float)(2.42 * secRadius * Math.pow(primMass/secMass,1.0/3));
	}

	private float getLuminosity(float radius, float mass, Sun aSun)
	{
		return (float)(Math.pow(radius/solarRad,2) * Math.pow(getATemparature(mass,aSun)/getATemparature(solarM,null),4) );
	}

	public Vector getProceduralVector(int seed,float magnitude)
	{
		float angleY = (float)Math.cos(Math.toRadians( (float)(90 + Noise.noise(seed,200)*10) ));
		Vector v = new Vector((float)(Noise.noise(seed,100)),angleY,(float)(Noise.noise(seed,300)));
		v.normalize();
		v.mult(magnitude);
		return v;
	}

	public float getProceduralMass(int seed, double factorMin, double factorMax)
	{
		return (float)(getAPercentage(seed)*(factorMax-factorMin) + factorMin);
	}

	public float getRadius(float mass,float density)
	{
		float vol = mass/density;
		return (float)Math.pow(((vol*3/4)/Math.PI),1.0/3);
	}

	public float getSDMass(float radius,float density)
	{
		return (float)(density * 4 * Math.PI * Math.pow(radius,3))/3;
	}

	public float getAPercentage(int seed)
	{
		return (float)((Noise.noise(seed) + 1)/2);
	}

	public float getAmount(double lerp, double min, double max)
	{
		return (float)((lerp - min)/(max - min));
	}

	public float getLerp(double amount, double min, double max)
	{
		return (float)(min + amount*(max - min));
	}

	Vertex blue = new Vertex(0,0,255);
	Vertex paleBlue = new Vertex(100,131,255);
	Vertex deepBlueWhite = new Vertex(145,167,255);
	Vertex blueWhite = new Vertex(200,210,255);
	Vertex whiteC = new Vertex(255,255,255);
	Vertex yellowWhite = new Vertex(255,251,223);
	Vertex yellowOrange = new Vertex(255,223,165);
	Vertex orangeRed = new Vertex(255,165,60);

	Vertex lerpColor(Vertex c1, Vertex c2,double amt)
	{
		return new Vertex((float)(c1.x + amt*(c2.x - c1.x)), (float)(c1.y + amt*(c2.y - c1.y))
						  ,(float)(c1.z + amt*(c2.z - c1.z)));
	}

	public float getATemparature(float mass,Sun aSun)
	{
		float scaledM = mass/solarM;
		float amt;
		if(scaledM <= 0.45)
		{
			amt = getAmount(scaledM, 0.08, 0.45);
			if(aSun != null)
			{	
				aSun.setColor(lerpColor(yellowOrange,orangeRed,amt));
			}
			return getLerp( amt, 2400, 3700);
		}
		else if(scaledM > 0.45 && scaledM <= 0.8)
		{
			amt = getAmount(scaledM, 0.45, 0.8);
			if(aSun != null)
			{	
				aSun.setColor(lerpColor(yellowWhite,yellowOrange,amt));
			}
			return getLerp( amt, 3700, 5200);
		}
		else if(scaledM > 0.8 && scaledM <= 1.04)
		{
			amt = getAmount(scaledM, 0.8, 1.04);
			if(aSun != null)
			{	
				aSun.setColor(lerpColor(whiteC,yellowWhite,amt));
			}
			return getLerp( amt, 5200, 6000);
		}
		else if(scaledM > 1.04 && scaledM <= 1.4)
		{
			amt = getAmount(scaledM, 1.04, 1.4);
			if(aSun != null)
			{	
				aSun.setColor(lerpColor(blueWhite,whiteC,amt));
			}
			return getLerp( amt, 6000, 7500);
		}
		else if(scaledM > 1.4 && scaledM <= 2.1)
		{
			amt = getAmount(scaledM, 1.4, 2.1);
			if(aSun != null)
			{	
				aSun.setColor(lerpColor(deepBlueWhite,blueWhite,amt));
			}
			return getLerp( amt, 7500, 10000);
		}
		else if(scaledM > 2.1 && scaledM <= 16)
		{
			amt = getAmount(scaledM, 2.1, 16);
			if(aSun != null)
			{	
				aSun.setColor(lerpColor(deepBlueWhite,paleBlue,amt));
			}
			return getLerp( amt, 10000, 30000);
		}
		amt = getAmount(scaledM, 16, 30);
		if(aSun != null)
		{	
			aSun.setColor(lerpColor(paleBlue,blue,amt));
		}
		return getLerp(amt , 30000, 50000);
	}

	public void drawCelestials(Frustum cam)
	{
		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		for(Sun aSun : suns)
		{
			/*glPushMatrix();
			glTranslatef(aSun.getCoordinates().x,aSun.getCoordinates().y,aSun.getCoordinates().z);
			Sphere s = new Sphere();
			s.draw(aSun.getRadius(),10,10);

			glPopMatrix();*/
			aSun.draw(cam);
		}

		for(Planet p : planets)
		{
			/*glPushMatrix();
			glTranslatef(p.getCoordinates().x,p.getCoordinates().y,p.getCoordinates().z);
			Sphere s = new Sphere();
			glColor3f(1.0f, 1.0f, 0.0f);
			s.draw(p.getRadius(),10,10);

			// show acceleration and speed
			glBegin(GL_LINES);
			glColor3f(1.0f, 0.0f, 0.0f);
			glVertex3f(0,0,0);
			glVertex3f(p.getAcceleration().x*1000,p.getAcceleration().y*1000,p.getAcceleration().z*1000);

			glColor3f(0.0f, 1.0f, 0.0f);
			glVertex3f(0,0,0);
			glVertex3f(p.getSpeed().x*10,p.getSpeed().y*10,p.getSpeed().z*10);
			glEnd();

			glPopMatrix();*/
			p.draw(cam);
		}

		for(Moon m : moons)
		{
			/*glPushMatrix();
			glTranslatef(m.getCoordinates().x,m.getCoordinates().y,m.getCoordinates().z);
			glColor3f(0.0f, 1.0f, 1.0f);
			Sphere s = new Sphere();
			s.draw(m.getRadius(),10,10);
			glBegin(GL_LINES);
			glColor3f(1.0f, 0.0f, 0.0f);
			glVertex3f(0,0,0);
			glVertex3f(m.getAcceleration().x*1000,m.getAcceleration().y*1000,m.getAcceleration().z*1000);

			glColor3f(0.0f, 1.0f, 0.0f);
			glVertex3f(0,0,0);
			glVertex3f(m.getSpeed().x*10,m.getSpeed().y*10,m.getSpeed().z*10);
			glEnd();
			glPopMatrix();*/
			m.draw(cam);
		}
		//glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}

	public void updateCelestials()
	{
		for(Sun aSun : suns)
		{
			aSun.updateLocation();
			/*float light_position[] = { aSun.getCoordinates().x, aSun.getCoordinates().y, aSun.getCoordinates().z,1.0f };
			FloatBuffer lightPos = BufferUtils.createFloatBuffer(light_position.length);
			lightPos.put(light_position);
			lightPos.flip();
			if(aSun.first == 1)
			{
				glDisable(GL_LIGHT0);
				glLight(GL_LIGHT0, GL_POSITION, lightPos);
				glLight(GL_LIGHT0, GL_SPECULAR, white); // set the specular light
				glLight(GL_LIGHT0, GL_DIFFUSE, white);  // set the diffuse light
				glEnable(GL_LIGHT0);
			}
			else if(aSun.first == 2)
			{
				glDisable(GL_LIGHT1);
				glLight(GL_LIGHT1, GL_POSITION, lightPos);
				glLight(GL_LIGHT1, GL_SPECULAR, white); // set the specular light
				glLight(GL_LIGHT1, GL_DIFFUSE, white);  // set the diffuse light
				glEnable(GL_LIGHT1);
			}*/
		}

		for(Planet p : planets)
		{
			p.updateLocation();
		}

		for(Moon m : moons)
		{
			m.updateLocation();
		}
	}
}