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
	
	@Override
	public Node parseBytes(DataInputStream input, int parentByte) throws IOException
	{
		if (parentByte == 0b00000010) return new Node(true);
		if (parentByte == 0b00000001) return new Node(false);
		
		int a = input.read();	//returns -1 if there are no more bytes
		
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
	
	@Override
	public void writeBytes(Node node, OutputStream output) throws IOException
	{
		output.write(	getByte(	node.children[0],
									node.children[1],
									node.children[2],
									node.children[3]	
									));
		
		for (Node child : node.children) 
			if (child.children.length > 0)
				writeBytes(child, output);
	}
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		CONSTRUCTORS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	public Quadtree(File file) throws IOException
	{
		super(file);
	}
	
	
	/*-------------------------------------
		Methods used by constructors.
	-------------------------------------*/
	
	@Override
	protected final TreeEditor<Quadtree> newEditor() 
	{
		return new TreeEditor<Quadtree>(this, min[0], min[1]);
	}
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		CALCULATIONS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	@Override
	public Node[] getNodes(Node parentNode, int[][] regionBounds) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
