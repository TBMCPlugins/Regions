package regions;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class Octree extends Tree
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
		
		int a = input.read(), 	
			b = input.read();
		
		return a == -1 || b == -1 ?
				new Node( false ) :
				new Node( new Node[] {	parseBytes(input, (a >>> 6 & 3)),
										parseBytes(input, (a >>> 4 & 3)),
										parseBytes(input, (a >>> 2 & 3)),
										parseBytes(input, (a	   & 3)),
										
										parseBytes(input, (b >>> 6 & 3)),
										parseBytes(input, (b >>> 4 & 3)),
										parseBytes(input, (b >>> 2 & 3)),
										parseBytes(input, (b	   & 3))
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
			
			output.write(	getByte(	node.children[4],
										node.children[5],
										node.children[6],
										node.children[7]	
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
	
	public Octree(int[][] bounds, File file, byte[] bytes)
	{
		super(bounds, file);
	}
	OctreeEditor newEditor() 
	{
		return new OctreeEditor(this);
	}
}
