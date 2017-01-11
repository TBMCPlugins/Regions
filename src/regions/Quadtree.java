package regions;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class Quadtree extends Tree
{
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		FROM BYTES
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	public Node parseBytes(DataInputStream input, int parentByte) throws IOException
	{
		if (parentByte == 0b00000010) return new Node(true);
		if (parentByte == 0b00000001) return new Node(false);
		
		int a = input.read();
		
		return  a == -1 ?
				new Node( false ) :
				new Node( new Node[] {	parseBytes(input, (a >>> 6 & 3)),
										parseBytes(input, (a >>> 4 & 3)),
										parseBytes(input, (a >>> 2 & 3)),
										parseBytes(input, (a 	   & 3))
										});
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		TO BYTES
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	public void writeBytes(Node node, OutputStream output)
	{
		try 
		{
			output.write(	getByte(	node.children[0],
										node.children[1],
										node.children[2],
										node.children[3]	
										));
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		for (Node child : node.children) 
			if (child.children.length > 0)
				writeBytes(child, output);
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		CONSTRUCTORS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	public Quadtree(int[][] bounds, File file)
	{
		super(bounds, file);
	}
	public QuadtreeEditor newEditor()
	{
		return new QuadtreeEditor(this);
	}
}
