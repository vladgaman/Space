public class Frustum
{
	private float hWidth;
	private Vertex coord;
	public Frustum(Vertex location, float width)
	{
		hWidth = width/2;
		coord = location;
	}
	public float getHWidth()
	{
		return hWidth;
	}
	public Vertex getCoord()
	{
		return coord;
	}
}