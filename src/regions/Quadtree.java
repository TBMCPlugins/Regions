package regions;

import java.io.IOException;
import java.io.OutputStream;

public class Quadtree extends Tree
{
	/*----------------------------------------------------
	------------------------------------------------------
		FROM BYTES
	------------------------------------------------------
	----------------------------------------------------*/
	
	Node parseBytes(IntReference index, byte[] bytes, int parentByte)
	{
		if (parentByte == 0b00000010) return new Node(true);
		if (parentByte == 0b00000001) return new Node(false);
		
		byte a = bytes[index.ref++];
		
		return 
				new Node
				(
						new Node[]
						{
							parseBytes(index, bytes, (a >>> 6 & 3)),
							parseBytes(index, bytes, (a >>> 4 & 3)),
							parseBytes(index, bytes, (a >>> 2 & 3)),
							parseBytes(index, bytes, (a 	  & 3))
						});
	}
	
	/*----------------------------------------------------
	------------------------------------------------------
		TO BYTES
	------------------------------------------------------
	----------------------------------------------------*/
	
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
				writeBytes(child, output);
	}
	
	/*----------------------------------------------------
	------------------------------------------------------
		CONSTRUCTORS
	------------------------------------------------------
	----------------------------------------------------*/
	
	public Quadtree(Owner owner, byte[] bytes)
	{
		super(owner, bytes);
	}
}
