import java.util.ArrayList;

public class Moon extends CelestialBody
{
	protected ArrayList<Moon> moons;
	protected Vector cumulativeSpeed;

	Moon(Vector coord, float rad, float m,long seed)
	{
		super(coord,rad,m,seed);
		moons = new ArrayList<Moon>();
		cumulativeSpeed = new Vector(0,0,0);
	}

	void addMoon(Moon aMoon)
	{
		moons.add(aMoon);
		aMoon.orbits(this);
	}

	Vector getCumulativeSpeed()
	{
		return cumulativeSpeed;
	}

	@Override
	void updateLocation()
	{
		super.updateLocation();
		if(getClass() != orbitingTo.getClass())
		{
			Planet planet = (Planet)(orbitingTo);
			cumulativeSpeed = new Vector(planet.getSpeed());
		}
		else
		{
			Moon moon = (Moon)(orbitingTo);
			cumulativeSpeed.add(moon.getCumulativeSpeed());
		}
		cumulativeSpeed.add(speedV);
		for(Moon moon : moons)
		{
			moon.getCoordinates().add(cumulativeSpeed);
		}
	}

}