public class Noise
{
	private static double persistence = 0.6;
	private static double noOfOctaves = 8;

	public static double noise(int x)
	{
		int X = (x<<13) ^ x;
		int f = X * (X * X * 15731 + 789221) + 1376312589;
		int s = f & 0x7fffffff;
		return ( 1.0 - s / 1073741824.0);
	}

	public static double noise(int x, int y)
	{
		int n = x + y * 57;
		int X = (n<<13) ^ n;
		int f = X * (X * X * 15731 + 789221) + 1376312589;
		int s = f & 0x7fffffff;
		return ( 1.0 - s / 1073741824.0);
	}

	public static double noise(int x, int y, int z)
	{
		int n = x + y * 59 + z * 101;
		int X = (n<<13) ^ n;
		int f = X * (X * X * 15731 + 789221) + 1376312589;
		int s = f & 0x7fffffff;
		return ( 1.0 - s / 1073741824.0);
	}

	public static double SmoothedNoise(int x)
	{
		return noise(x)/2  +  noise(x-1)/4  +  noise(x+1)/4;
	}

	public static double SmoothedNoise(int x, int y)
	{
		double corners = (noise(x+1,y+1) + noise(x-1,y-1) + noise(x-1,y+1) + noise(x-1,y-1)) / 4;
		double sides = (noise(x+1,y) + noise(x-1,y) + noise(x,y+1) + noise(x,y-1)) / 4;
		double center = noise(x,y);
		return (corners + sides + center)/3;
	}

	public static double SmoothedNoise(int x, int y, int z)
	{
		double edges = (noise(x,y+1,z+1) + noise(x,y-1,z+1) + noise(x,y+1,z-1) + noise(x,y-1,z-1)
						+ noise(x+1,y,z+1) + noise(x+1,y,z-1) + noise(x-1,y,z+1) + noise(x-1,y,z-1)
						+ noise(x+1,y+1,z) + noise(x+1,y-1,z) + noise(x-1,y+1,z) + noise(x-1,y-1,z))/12;
		double corners = (noise(x+1,y+1,z+1) + noise(x-1,y-1,z+1) + noise(x-1,y+1,z+1) + noise(x-1,y-1,z+1)
						  + noise(x+1,y+1,z-1) + noise(x-1,y-1,z-1) + noise(x-1,y+1,z-1) + noise(x-1,y-1,z-1)) / 8;
		double sides = (noise(x+1,y,z) + noise(x-1,y,z) + noise(x,y+1,z) + noise(x,y-1,z) 
					   + noise(x,y,z+1) + noise(x,y,z-1)) / 6;
		double center = noise(x,y,z);
		return (corners + sides + center + edges)/4;
	}

	public static double CosineInterpolate(double a, double b, double x)
	{
		double ft = x * 3.1415927;
		double f = (1 - Math.cos(ft)) * 0.5;

		return  a*(1-f) + b*f;
	}

	public static double InterpolatedNoise(double x)
	{
		int integer_X = (int) x;
		double fractional_X = x - integer_X;

		double v1 = SmoothedNoise(integer_X);
		double v2 = SmoothedNoise(integer_X + 1);

		return CosineInterpolate(v1 , v2 , fractional_X);
	}

	public static double InterpolatedNoise(double x, double y)
	{
		int integer_X = (int) x;
		double fractional_X = x - integer_X;

		int integer_Y = (int) y;
		double fractional_Y = y - integer_Y;

		double v1 = SmoothedNoise(integer_X, integer_Y);
		double v2 = SmoothedNoise(integer_X + 1, integer_Y);
		double v3 = SmoothedNoise(integer_X, integer_Y + 1);
		double v4 = SmoothedNoise(integer_X + 1, integer_Y + 1);

		double i1 = CosineInterpolate(v1, v2, fractional_X);
		double i2 = CosineInterpolate(v3, v4, fractional_X);

		return CosineInterpolate(i1 , i2 , fractional_Y);
	}

	public static double InterpolatedNoise(double x, double y, double z)
	{
		int integer_X = (int) x;
		double fractional_X = x - integer_X;

		int integer_Y = (int) y;
		double fractional_Y = y - integer_Y;

		int integer_Z = (int) z;
		double fractional_Z = z - integer_Z;

		double v1 = SmoothedNoise(integer_X, integer_Y, integer_Z);
		double v2 = SmoothedNoise(integer_X + 1, integer_Y, integer_Z);
		double v3 = SmoothedNoise(integer_X, integer_Y + 1, integer_Z);
		double v4 = SmoothedNoise(integer_X + 1, integer_Y + 1, integer_Z);
		double v5 = SmoothedNoise(integer_X, integer_Y, integer_Z + 1);
		double v6 = SmoothedNoise(integer_X + 1, integer_Y, integer_Z + 1);
		double v7 = SmoothedNoise(integer_X, integer_Y + 1, integer_Z + 1);
		double v8 = SmoothedNoise(integer_X + 1, integer_Y + 1, integer_Z + 1);

		double i1 = CosineInterpolate(v1, v2, fractional_X);
		double i2 = CosineInterpolate(v3, v4, fractional_X);
		double i3 = CosineInterpolate(v5, v6, fractional_X);
		double i4 = CosineInterpolate(v7, v8, fractional_X);

		double j1 = CosineInterpolate(i1, i2, fractional_Y);
		double j2 = CosineInterpolate(i3, i4, fractional_Y);

		return CosineInterpolate(j1 , j2 , fractional_Z);
	}

	public static double PerlinNoise(double x)
	{
		double total = 0;
		double frequency;
		double amplitude;
		for(int i=0;i<noOfOctaves;i++)
		{
			frequency = Math.pow(2,i);
			amplitude = Math.pow(persistence,i);
			total = total + InterpolatedNoise(x * frequency) * amplitude;
		}
		return total;
	}

	public static double PerlinNoise(double x, double y)
	{
		double total = 0;
		double frequency;
		double amplitude;
		for(int i=0;i<noOfOctaves;i++)
		{
			frequency = Math.pow(2,i);
			amplitude = Math.pow(persistence,i);
			total = total + InterpolatedNoise(x * frequency, y * frequency) * amplitude;
		}
		return total;
	}

	public static double PerlinNoise(double x, double y, double z)
	{
		double total = 0;
		double frequency;
		double amplitude;
		for(int i=0;i<noOfOctaves;i++)
		{
			frequency = Math.pow(2,i);
			amplitude = Math.pow(persistence,i);
			total = total + InterpolatedNoise(x * frequency, y * frequency, z * frequency) * amplitude;
		}
		return total;
	}

	public static void setPers(double p)
	{
		persistence = p;
	}

	public static void setOctaves(double o)
	{
		noOfOctaves = o;
	}
}