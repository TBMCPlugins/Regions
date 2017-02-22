package regions;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

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
				new Node( parseBytes(input, (a >>> 6 & 3)),
						  parseBytes(input, (a >>> 4 & 3)),
						  parseBytes(input, (a >>> 2 & 3)),
						  parseBytes(input, (a 	     & 3))
						  );
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		TO BYTES
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	@Override
	public void writeBytes(Node node, OutputStream output) throws IOException
	{
		output.write(	getByte( node.children[0],
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
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		EVALUATE																			 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
	@Override
	public boolean contains(int... coords)
	{
		
	}
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		ADD VOLUME																			 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		expand()
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	/**
	 * 
	 * @param xMinExpansion
	 * @param zMinExpansion
	 * @param xMaxExpansion
	 * @param zMaxExpansion
	 */
	protected void expand(double xMinExpansion, double zMinExpansion, double xMaxExpansion, double zMaxExpansion)
	{
		int xMinRounded = (int) Math.ceil(xMinExpansion),
			zMinRounded = (int) Math.ceil(zMinExpansion),
			xMaxRounded = (int) Math.ceil(xMaxExpansion),
			zMaxRounded = (int) Math.ceil(zMaxExpansion),
			
			size = nextPowerOfTwo(	xMinRounded + xMaxRounded + 1, 
									zMinRounded + zMaxRounded + 1
									),
		
			xMargin = size - (xMinRounded + xMaxRounded + 1),
			zMargin = size - (zMinRounded + zMaxRounded + 1),
			
			xMarginHalf = xMargin / 2,
			zMarginHalf = zMargin / 2;
		
		
		
		xMinRounded += xMarginHalf;
		zMinRounded += zMarginHalf;
		xMaxRounded += xMarginHalf;
		zMaxRounded += zMarginHalf;
		
		/* if margin is odd, add 1
		 * to the more close-fitting side
		 */
		if (xMargin % 2 == 1)
			if (xMinRounded - xMinExpansion > xMaxRounded - xMaxExpansion) xMinRounded++; else xMaxRounded++;
		
		if (zMargin % 2 == 1)
			if (zMinRounded - zMinExpansion > zMaxRounded - zMaxExpansion) zMinRounded++; else zMaxRounded++;
		
		
		
		int index;
		Node[] children;
		Node[] newRootChildren = children = Node.emptyNodeArray(4);
				
		while(true)
		{
			size >>>= 1;
			index = 0;
			
			if (xMinRounded >= size)
				xMinRounded -= size;
			else
			{
				xMaxRounded -= size;
				index += 1;
			}
			if (zMinRounded >= size)
				zMinRounded -= size;
			else
			{
				zMaxRounded -= size;
				index += 2;
			}
			
			if (size > 1)
				children[index].children = children = Node.emptyNodeArray(4);
			else
			{
				children[index].children = root.children;
				break;
			}
		}
		root.children = newRootChildren;
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		expandAsNeeded()
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	@Override
	protected void expandAsNeeded(int... coords) 
	{
		int sideLength = max[0] - min[0] + 1;
		
		double	xMinExpansion = 0,
				zMinExpansion = 0,
				xMaxExpansion = 0,
				zMaxExpansion = 0;
		
		if 		(coords[0] < min[0]) xMinExpansion = (min[0] - coords[0]) / sideLength;
		else if (coords[0] > max[0]) xMaxExpansion = (coords[0] - max[0]) / sideLength;
		
		if 		(coords[1] < min[1]) xMinExpansion = (min[1] - coords[1]) / sideLength;
		else if	(coords[1] > max[1]) xMaxExpansion = (coords[1] - max[1]) / sideLength;
		
		if (xMinExpansion != 0 ||
			zMinExpansion != 0 ||
			xMaxExpansion != 0 ||
			zMaxExpansion != 0
			)
			expand(xMinExpansion, 
				   zMinExpansion, 
				   xMaxExpansion, 
				   zMaxExpansion);
	}
	
	
	@Override
	public void expandAsNeeded(int[]... bounds)
	{
		int sideLength = max[0] - min[0] + 1;
		
		double	xMinExpansion = 0,
				zMinExpansion = 0,
				xMaxExpansion = 0,
				zMaxExpansion = 0;
		
		if (bounds[0][0] < min[0]) xMinExpansion = (min[0] - bounds[0][0]) / sideLength;
		if (bounds[0][1] > max[0]) xMaxExpansion = (bounds[0][1] - max[0]) / sideLength;
		
		if (bounds[1][0] < min[1]) xMinExpansion = (min[1] - bounds[1][0]) / sideLength;
		if (bounds[1][1] > max[1]) xMaxExpansion = (bounds[1][1] - max[1]) / sideLength;
		
		if (xMinExpansion != 0 ||
			zMinExpansion != 0 ||
			xMaxExpansion != 0 ||
			zMaxExpansion != 0
			)
			expand(xMinExpansion, 
				   zMinExpansion, 
				   xMaxExpansion, 
				   zMaxExpansion);
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		add()
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	@Override
	public void add(int... coords) 
	{
		expandAsNeeded(coords);
		
		Node node = root;
		
		int[] min = this.min;
		int[] max = this.max;
		
		int size  = max[0] - min[0] + 1;
		int half  = size / 2;
		int index = 0;
		
		outerloop:
		while (true)
		{
			if (node.full) return;
			if (node.children.length == 0) node.children = Node.emptyNodeArray(4);
			
			if ((min[0] + half) > coords[0])
				max[0] -= half;
			else
			{
				min[0] += half;
				index  += 1;
			}
			if ((min[1] + half) > coords[1])
				max[1] -= half;
			else
			{
				min[1] += half;
				index  += 2;
			}
			
			if ((size >>>= 1) > 1)
			{
				node = node.children[index];
				half = size / 2;
			}
			else
			{
				node.children[index].full = true;
				
				for (Node child : node.children)
				{
					if (!child.full)
					{
						break outerloop;
					}
				}
				node.full = true;
				node.children = new Node[0];
			}
		}
	}
	
	
	@Override
	public void add(int[]... bounds) 
	{
		expandAsNeeded(bounds);
	}
	
	@Override
	public void add(BitSet blocks, int[]... bounds) 
	{
		expandAsNeeded(bounds);
	}

	
	
	/*-------------------------------------
		REMOVE VOLUME
	-------------------------------------*/
	
	
	@Override
	public void trimAsNeeded() 
	{
		// TODO Auto-generated method stub
	}
	

	@Override
	public void remove(int... coords) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void remove(int[]... bounds) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void remove(BitSet blocks, int[]... bounds) 
	{
		// TODO Auto-generated method stub
	}
}
