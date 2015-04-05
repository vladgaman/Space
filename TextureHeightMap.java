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
	public static int createHeightMap(long seed,TextureColor texCol)
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

				Vertex cp = texCol.getColor((float)nois);

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