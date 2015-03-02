import java.util.ArrayList;

public class Planet extends CelestialBody
{
	private ArrayList<Planet> moons;

	Planet(Vector coord, float rad, float m)
	{
		super(coord,rad,m);
		moons = new ArrayList<Planet>();
	}

	void addMoon(Planet aPlanet)
	{
		moons.add(aPlanet);
		aPlanet.orbits(this);
	}


}