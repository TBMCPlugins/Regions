package regions;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

import regions.Tree.Node;

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
				new Node( parseBytes(input, (a >>> 6 & 3)),
						  parseBytes(input, (a >>> 4 & 3)),
						  parseBytes(input, (a >>> 2 & 3)),
						  parseBytes(input, (a	     & 3)),
						  
						  parseBytes(input, (b >>> 6 & 3)),
						  parseBytes(input, (b >>> 4 & 3)),
						  parseBytes(input, (b >>> 2 & 3)),
						  parseBytes(input, (b	     & 3))
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
			
		output.write(	getByte( node.children[4],
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
	 * @param yMinExpansion
	 * @param xMaxExpansion
	 * @param zMaxExpansion
	 * @param yMaxExpansion
	 */
	protected void expand(double xMinExpansion, double zMinExpansion, double yMinExpansion, 
						  double xMaxExpansion, double zMaxExpansion, double yMaxExpansion
						  )
	{
		int xMinRounded = (int) Math.ceil(xMinExpansion),
			zMinRounded = (int) Math.ceil(zMinExpansion),
			yMinRounded = (int) Math.ceil(yMinExpansion),
			xMaxRounded = (int) Math.ceil(xMaxExpansion),
			zMaxRounded = (int) Math.ceil(zMaxExpansion),
			yMaxRounded = (int) Math.ceil(yMaxExpansion),
			
			size = nextPowerOfTwo(	xMinRounded + xMaxRounded + 1, 
									zMinRounded + zMaxRounded + 1, 
									yMinRounded + yMaxRounded + 1
									),
		
			xMargin = size - (xMinRounded + xMaxRounded + 1),
			zMargin = size - (zMinRounded + zMaxRounded + 1),
			yMargin = size - (yMinRounded + yMaxRounded + 1),
			
			xMarginHalf = xMargin / 2,
			zMarginHalf = zMargin / 2,
			yMarginHalf = yMargin / 2;
		
		
		
		xMinRounded += xMarginHalf;
		zMinRounded += zMarginHalf;
		yMinRounded += yMarginHalf;
		xMaxRounded += xMarginHalf;
		zMaxRounded += zMarginHalf;
		yMaxRounded += yMarginHalf;
		
		/* if margin is odd, add 1
		 * to the more close-fitting side
		 */
		if (xMargin % 2 == 1)
			if (xMinRounded - xMinExpansion > xMaxRounded - xMaxExpansion) xMinRounded++; else xMaxRounded++;
		
		if (zMargin % 2 == 1)
			if (zMinRounded - zMinExpansion > zMaxRounded - zMaxExpansion) zMinRounded++; else zMaxRounded++;
		
		if (yMargin % 2 == 1)
			if (yMinRounded - yMinExpansion > yMaxRounded - yMaxExpansion) yMinRounded++; else yMaxRounded++;
		
		
		
		int index;
		Node[] children;
		Node[] newRootChildren = children = Node.emptyNodeArray(8);
		
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
			if (yMinRounded >= size)
				yMinRounded -= size;
			else
			{
				yMaxRounded -= size;
				index += 4;
			}
			
			if (size > 1)
				children[index].children = children = Node.emptyNodeArray(8);
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
				yMinExpansion = 0,
				xMaxExpansion = 0,
				zMaxExpansion = 0,
				yMaxExpansion = 0;
		
		if 		(coords[0] < min[0]) xMinExpansion = (min[0] - coords[0]) / sideLength;
		else if (coords[0] > max[0]) xMaxExpansion = (coords[0] - max[0]) / sideLength;
		
		if 		(coords[1] < min[1]) xMinExpansion = (min[1] - coords[1]) / sideLength;
		else if	(coords[1] > max[1]) xMaxExpansion = (coords[1] - max[1]) / sideLength;
		
		if 		(coords[2] < min[2]) xMinExpansion = (min[2] - coords[2]) / sideLength;
		else if (coords[2] > max[2]) xMaxExpansion = (coords[2] - max[2]) / sideLength;
		
		if (xMinExpansion != 0 ||
			zMinExpansion != 0 ||
			yMinExpansion != 0 ||
			xMaxExpansion != 0 ||
			zMaxExpansion != 0 ||
			yMaxExpansion != 0
			)
			expand(xMinExpansion, 
				   zMinExpansion, 
				   yMinExpansion, 
				   xMaxExpansion, 
				   zMaxExpansion, 
				   yMaxExpansion);
	}
	
	
	@Override
	public void expandAsNeeded(int[]... bounds)
	{
		int sideLength = max[0] - min[0] + 1;
		
		double	xMinExpansion = 0,
				zMinExpansion = 0,
				yMinExpansion = 0,
				xMaxExpansion = 0,
				zMaxExpansion = 0,
				yMaxExpansion = 0;
		
		if (bounds[0][0] < min[0]) xMinExpansion = (min[0] - bounds[0][0]) / sideLength;
		if (bounds[0][1] > max[0]) xMaxExpansion = (bounds[0][1] - max[0]) / sideLength;
		
		if (bounds[1][0] < min[1]) xMinExpansion = (min[1] - bounds[1][0]) / sideLength;
		if (bounds[1][1] > max[1]) xMaxExpansion = (bounds[1][1] - max[1]) / sideLength;
		
		if (bounds[2][0] < min[2]) xMinExpansion = (min[2] - bounds[2][0]) / sideLength;
		if (bounds[2][1] > max[2]) xMaxExpansion = (bounds[2][1] - max[2]) / sideLength;
		
		if (xMinExpansion != 0 ||
			zMinExpansion != 0 ||
			yMinExpansion != 0 ||
			xMaxExpansion != 0 ||
			zMaxExpansion != 0 ||
			yMaxExpansion != 0
			)
			expand(xMinExpansion, 
				   zMinExpansion, 
				   yMinExpansion, 
				   xMaxExpansion, 
				   zMaxExpansion, 
				   yMaxExpansion);
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
			if (node.children.length == 0) node.children = Node.emptyNodeArray(8);
			
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
			if ((min[2] + half) > coords[2])
				max[2] -= half;
			else
			{
				min[2] += half;
				index  += 4;
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

	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		REMOVE VOLUME																		 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
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
