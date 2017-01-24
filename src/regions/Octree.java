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
	
	@Override
	public Node parseBytes(DataInputStream input, int parentByte) throws IOException
	{
		if (parentByte == 0b00000010) return new Node(true);
		if (parentByte == 0b00000001) return new Node(false);
		
		int a = input.read(),	//returns -1 if there are no more bytes
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
	
	@Override
	public void writeBytes(Node node, OutputStream output) throws IOException
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
		
		for (Node child : node.children) 
			if (child.children.length > 0)
				writeBytes(child, output);
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		CONSTRUCTORS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	public Octree(File file) throws IOException
	{
		super(file);
	}

	
	/*-------------------------------------
		Methods used by constructors.
	-------------------------------------*/
	
	@Override
	protected final TreeEditor<Octree> newEditor() 
	{
		return new TreeEditor<Octree>(this, min[0], min[1], min[2]);
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
