public class TextureColor
{
	private Vertex[] color = new Vertex[8];
	private float[] lerps = new float[6];

	TextureColor(int seed)
	{
		if(seed < 0)
		{
			lerps[0] = -0.25f;
			lerps[1] = 0;
			lerps[2] = 0.0625f;
			lerps[3] = 0.125f;
			lerps[4] = 0.375f;
			lerps[5] = 0.75f;
			color[0] = new Vertex(0,0,128);
			color[1] = new Vertex(0,0,255);
			color[2] = new Vertex(0,128,255);
			color[3] = new Vertex(240,240,64);
			color[4] = new Vertex(32,160,0);
			color[5] = new Vertex(224,224,0);
			color[6] = new Vertex(128,128,128);
			color[7] = new Vertex(255,255,255);
		}
		else
		{
			lerps[0] = (float)Noise.noise(seed);
			lerps[1] = getLerp(getAmount(Noise.noise(seed+1),-1,1),lerps[0],1);
			lerps[2] = getLerp(getAmount(Noise.noise(seed+2),-1,1),lerps[1],1);
			lerps[3] = getLerp(getAmount(Noise.noise(seed+3),-1,1),lerps[2],1);
			lerps[4] = getLerp(getAmount(Noise.noise(seed+4),-1,1),lerps[3],1);
			lerps[5] = getLerp(getAmount(Noise.noise(seed+5),-1,1),lerps[4],1);
			color[0] = new Vertex(getLerp(Noise.noise(seed+6),0,255),getLerp(Noise.noise(seed+7),0,255),getLerp(Noise.noise(seed+8),0,255));
			color[1] = new Vertex(getLerp(Noise.noise(seed+9),0,255),getLerp(Noise.noise(seed+10),0,255),getLerp(Noise.noise(seed+11),0,255));
			color[2] = new Vertex(getLerp(Noise.noise(seed,12),0,255),getLerp(Noise.noise(seed,13),0,255),getLerp(Noise.noise(seed,11),0,255));
			color[3] = new Vertex(getLerp(Noise.noise(seed,15),0,255),getLerp(Noise.noise(seed,16),0,255),getLerp(Noise.noise(seed,17),0,255));
			color[4] = new Vertex(getLerp(Noise.noise(seed,18),0,255),getLerp(Noise.noise(seed,19),0,255),getLerp(Noise.noise(seed,20),0,255));
			color[5] = new Vertex(getLerp(Noise.noise(seed,21),0,255),getLerp(Noise.noise(seed,22),0,255),getLerp(Noise.noise(seed,23),0,255));
			color[6] = new Vertex(getLerp(Noise.noise(seed,24),0,255),getLerp(Noise.noise(seed,25),0,255),getLerp(Noise.noise(seed,26),0,255));
			color[7] = new Vertex(getLerp(Noise.noise(seed,27),0,255),getLerp(Noise.noise(seed,28),0,255),getLerp(Noise.noise(seed,29),0,255));
		}
	}

	Vertex getColor(float nois)
	{
		if(nois >= -1 && nois <= lerps[0])
			return lerpColor(color[0],color[1], getAmount(nois,-1,lerps[0]));
		else if(nois > lerps[0] && nois <= lerps[1])
			return lerpColor(color[1],color[2], getAmount(nois,lerps[0],lerps[1]) );
		else if(nois > lerps[1] && nois <= lerps[2])
			return lerpColor(color[2],color[3], getAmount(nois,lerps[1],lerps[2]) );
		else if(nois > lerps[2] && nois <= lerps[3])
			return lerpColor(color[3],color[4], getAmount(nois,lerps[2],lerps[3]) );
		else if(nois > lerps[3] && nois <= lerps[4])
			return lerpColor(color[4],color[5], getAmount(nois,lerps[3],lerps[4]) );
		else if(nois > lerps[4] && nois <= lerps[5])
			return lerpColor(color[5],color[6], getAmount(nois,lerps[4],lerps[5]) );
		return lerpColor(color[6],color[7], getAmount(nois,lerps[5],1) );
	}

	Vertex lerpColor(Vertex c1, Vertex c2,double amt)
	{
		return new Vertex((float)(c1.x + amt*(c2.x - c1.x)), (float)(c1.y + amt*(c2.y - c1.y))
						  ,(float)(c1.z + amt*(c2.z - c1.z)));
	}

	float getAmount(double lerp, double min, double max)
	{
		return (float)((lerp - min)/(max - min));
	}

	float getLerp(double amount, double min, double max)
	{
		return (float)(min + amount*(max - min));
	}
}