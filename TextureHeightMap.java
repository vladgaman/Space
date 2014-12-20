import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TextureHeightMap
{
	private static final int BYTES_PER_PIXEL = 3;//3 for RGB, 4 for RGBA
	public static float height [][];
	public static Vertex lerpColor(Vertex c1, Vertex c2,double amt)
	{
		return new Vertex((float)(c1.x + amt*(c2.x - c1.x)), (float)(c1.y + amt*(c2.y - c1.y))
						  ,(float)(c1.z + amt*(c2.z - c1.z)));
	}
	public static int createHeightMap(long seed)
	{
		double rx = new Random(seed).nextDouble()*1000;
		double ry = new Random((long)(rx + seed)/1000).nextDouble()*1000;
		double rz = new Random((long)(ry + seed)/1000).nextDouble()*1000;
		PrintWriter out;
		try
		{
			out = new PrintWriter(new FileWriter("out"));
		}
		catch(Exception e)
		{
			out = null;
		}

		Vertex deep = new Vertex(0,0,128);
		Vertex shallow = new Vertex(0,0,255);
		Vertex shore = new Vertex(0,128,255);
		Vertex sand = new Vertex(240,240,64);
		Vertex grass = new Vertex(32,160,0);
		Vertex dirt = new Vertex(224,224,0);
		Vertex rock = new Vertex(128,128,128);
		Vertex snow = new Vertex(255,255,255);

		height = new float[640][640];
		ByteBuffer buffer = BufferUtils.createByteBuffer(640 /*width*/ * 640 /*height*/ * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB
		for(int y = 0; y < 640 /*height*/; y++)
		{
			for(int x = 0; x < 640 /*width*/; x++)
			{
				double hori = (x + 0.5)/640*2*Math.PI;
				double vert = (y + 0.5)/640*Math.PI;
				double circle = Math.sin(vert + Math.PI);
				double xa = 2;
				double a = xa * Math.cos(hori) * circle;
				double b = xa * Math.sin(hori) * circle;
				double c = xa * Math.cos(vert);
				double nois = Noise.PerlinNoise(rx + a, ry + b, rz + c);
				height[x][y] = (float)nois;
				Vertex cp = new Vertex(0,0,0);
				if(nois >= -1 && nois <= -0.25)
					cp = lerpColor(deep,shallow, ((1+nois)*4)/3);
				else if(nois > -0.25 && nois <= 0)
					cp = lerpColor(shallow,shore,(0.25+nois)*4 );
				else if(nois > 0 && nois <= 0.0625)
					cp = lerpColor(shore,sand,nois*16 );
				else if(nois > 0.0625 && nois <= 0.1250)
					cp = lerpColor(sand,grass,(nois - 0.0625)*16 );
				else if(nois > 0.1250 && nois <= 0.3750)
					cp = lerpColor(grass,dirt,(nois - 0.1250)*4 );
				else if(nois > 0.3750 && nois <= 0.7500)
					cp = lerpColor(dirt,rock,((nois - 0.3750)*8)/3 );
				else if(nois > 0.7500 && nois <= 1.0)
					cp = lerpColor(rock,snow,(nois - 0.7500)*4 );

				buffer.put((byte) cp.x);	// Red component
				buffer.put((byte) cp.y);	// Green component
				buffer.put((byte) cp.z);	// Blue component
			}
		}
		buffer.flip();
		out.close();
		int textureID = glGenTextures(); //Generate texture ID
		glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

		//Setup wrap mode
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		//Setup texture scaling filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		//Send texel data to OpenGL
		glTexImage2D(GL_TEXTURE_2D, 0, 3, 640/*width*/, 640/*height*/, 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);

		//Return the texture ID so we can bind it later again
		return textureID;
	}
}