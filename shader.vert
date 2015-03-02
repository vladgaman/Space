void main()
{
	vec4 pos = gl_Vertex;
	pos.x = pos.x ;
	gl_FrontColor = gl_Color;
	gl_Position = gl_ModelViewProjectionMatrix * pos;
}