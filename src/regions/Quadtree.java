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
			if (child.children != null)
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
		//TODO finish method
		return false;
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
	 * Method for sharing logic among the variants of expandAsNeeded()
	 * 
	 * @param xMinExpansion
	 * @param zMinExpansion
	 * @param xMaxExpansion
	 * @param zMaxExpansion
	 */
	protected void expand(double xMinExpansion, double zMinExpansion, double xMaxExpansion, double zMaxExpansion)
	{
		int xMinCeil = (int) Math.ceil(xMinExpansion),
			zMinCeil = (int) Math.ceil(zMinExpansion),
			xMaxCeil = (int) Math.ceil(xMaxExpansion),
			zMaxCeil = (int) Math.ceil(zMaxExpansion),
			
			size = nextPowerOfTwo(xMinCeil + xMaxCeil + 1, 
								  zMinCeil + zMaxCeil + 1
								  ),
		
			xMargin = size - (xMinCeil + xMaxCeil + 1),
			zMargin = size - (zMinCeil + zMaxCeil + 1),
			xMarginHalf = xMargin / 2,
			zMarginHalf = zMargin / 2;
		
		
		xMinCeil += xMarginHalf;
		zMinCeil += zMarginHalf;
		xMaxCeil += xMarginHalf;
		zMaxCeil += zMarginHalf;
		
		if (xMargin % 2 == 1) if (xMinCeil - xMinExpansion > xMaxCeil - xMaxExpansion) xMinCeil++; else xMaxCeil++;
		
		if (zMargin % 2 == 1) if (zMinCeil - zMinExpansion > zMaxCeil - zMaxExpansion) zMinCeil++; else zMaxCeil++;
		
		int sideLength = max[0] - min[0] + 1;
		min[0] -= (sideLength * xMinCeil);
		min[1] -= (sideLength * zMinCeil);
		max[0] += (sideLength * xMaxCeil);
		max[1] += (sideLength * zMaxCeil);
		
		
		int index;
		Node[] children;
		Node[] newRootChildren = children = Node.emptyNodeArray(4);
				
		while(true)
		{
			size >>>= 1;
			index = 0;
			
			if (xMinCeil >= size)
				xMinCeil -= size;
			else
			{
				xMaxCeil -= size;
				index += 1;
			}
			if (zMinCeil >= size)
				zMinCeil -= size;
			else
			{
				zMaxCeil -= size;
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
		
		if 		(coords[1] < min[1]) zMinExpansion = (min[1] - coords[1]) / sideLength;
		else if	(coords[1] > max[1]) zMaxExpansion = (coords[1] - max[1]) / sideLength;
		
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
		
		if (bounds[1][0] < min[1]) zMinExpansion = (min[1] - bounds[1][0]) / sideLength;
		if (bounds[1][1] > max[1]) zMaxExpansion = (bounds[1][1] - max[1]) / sideLength;
		
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
		add() SINGLE BLOCK
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * @param node
	 * @param half
	 * @param node_minX
	 * @param node_minZ
	 * @param node_maxX
	 * @param node_maxZ
	 * @param blockX
	 * @param blockZ
	 */
	protected void add(Node node, 
					   int half,
					   int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
					   int blockX, int blockZ
					   )
	{
		if (node.full) return;
		if (node.children == null) node.children = Node.emptyNodeArray(4);
		
		int index = 0;
		
		if (node_minX  + half > blockX)
			node_maxX -= half;
		else
		{
			node_minX += half;
			index += 1;
		}
		if (node_minZ  + half > blockZ)
			node_maxZ -= half;
		else
		{
			node_minZ += half;
			index += 2;
		}
		
		if (half > 1) add(node.children[index],
						  half >>>= 1,
						  node_minX, node_minZ, node_maxX, node_maxZ,
						  blockX, blockZ
						  );
		else
		{
			node.children[index].full = true;
			for (Node child : node.children)
				if (!child.full)
					return;
			
			node.full = true;
			node.children = null;
		}
	}
	
	
	@Override
	public void add(int... coords) 
	{
		expandAsNeeded(coords);
		
		add(root,
			(max[0] - min[0] + 1) / 2,
			this.min[0], this.min[1], this.max[0], this.max[1],
			coords[0], coords[1]
			);
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		add() BOUNDED SELECTION
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * @param node
	 * @param half
	 * @param node_minX
	 * @param node_minZ
	 * @param node_maxX
	 * @param node_maxZ
	 * @param sel_minX
	 * @param sel_minZ
	 * @param sel_maxX
	 * @param sel_maxZ
	 */
	protected void add(Node node, 
					   int half,
					   int node_minX, int node_minZ, int node_maxX, int node_maxZ,
					   int sel_minX,  int sel_minZ,  int sel_maxX,  int sel_maxZ
					   )
	{
		if (node.full) return;
		if (node_minX >= sel_minX)
		{
			if (node_maxX <= sel_maxX)
			{
				if (node_minZ >= sel_minZ)
				{
					if (node_maxZ <= sel_maxZ)
					{
						node.full = true;
						node.children = null;
					}
					return;
				}
				if (node_maxZ < sel_minZ)
					return;
			}
			if (node_minZ > sel_maxZ || node_maxZ < sel_minZ)
				return;
		}
		if (node_maxX < sel_minX || node_minZ > sel_maxZ || node_maxZ < sel_minZ)
			return;
		
		if (node.children == null) 
			node.children = Node.emptyNodeArray(4);
		
		int half_minX = min[0] + half, 
			half_maxX = half_minX - 1,
			half_minZ = min[1] + half, 
			half_maxZ = half_minZ - 1;
		
		half >>>= 1;
		
		
		/* child index:
		
			X         →         X
		  Z	╔═════════╦═════════╗
			║         ║         ║
			║    0    ║    1    ║
			║         ║         ║
		  ↓	╠═════════╬═════════╣
			║         ║         ║
			║    2    ║    3    ║
			║         ║         ║
		  Z	╚═════════╩═════════╝*/
		
		add(node.children[0],
			half,
			node_minX, node_minZ, half_maxX, half_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[1],
			half,
			half_minX, node_minZ, node_maxX, half_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[2],
			half,
			node_minX, half_minZ, half_maxX, node_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[3],
			half,
			half_minX, half_minZ, node_maxX, node_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		if (node.children[0].full && node.children[1].full && node.children[2].full && node.children[3].full)
		{
			node.full = true;
			node.children = null;
		}
	}
	
	
	@Override
	public void add(int[]... bounds) 
	{
		expandAsNeeded(bounds);
		
		add(root,
			(max[0] - min[0] + 1),
			min[0], min[1], max[0], max[1], 
			bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1]
			);
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		add() COMPLEX BOUNDED SELECTION
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * @param node
	 * @param half
	 * @param node_minX
	 * @param node_minZ
	 * @param node_maxX
	 * @param node_maxZ
	 * @param sel_minX
	 * @param sel_minZ
	 * @param sel_maxX
	 * @param sel_maxZ
	 * @param blocks
	 */
	protected void add(Node node, 
					   int half,
					   int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
					   int sel_minX,  int sel_minZ,  int sel_maxX,  int sel_maxZ,
					   BitSet blocks
					   )
	{
		if (node.full) return;
		if (node_minX >= sel_minX)
		{
			if (node_maxX <= sel_maxX)
			{
				if (node_minZ >= sel_minZ)
				{
					if (node_maxZ <= sel_maxZ)
					{
						node.full = true;
						node.children = null;
					}
					return;
				}
				if (node_maxZ < sel_minZ)
					return;
			}
			if (node_minZ > sel_maxZ || node_maxZ < sel_minZ)
				return;
		}
		if (node_maxX < sel_minX || node_minZ > sel_maxZ || node_maxZ < sel_minZ)
			return;
		
		if (node.children == null) 
			node.children = Node.emptyNodeArray(4);
		
		int half_minX = min[0] + half, 
			half_maxX = half_minX - 1,
			half_minZ = min[1] + half, 
			half_maxZ = half_minZ - 1;
		
		half >>>= 1;
		
		
		/* child index:
			
			X         →         X
		  Z	╔═════════╦═════════╗
			║         ║         ║
			║    0    ║    1    ║
			║         ║         ║
		  ↓	╠═════════╬═════════╣
			║         ║         ║
			║    2    ║    3    ║
			║         ║         ║
		  Z	╚═════════╩═════════╝*/
		
		add(node.children[0], 
			node_minX, node_minZ, half_maxX, half_maxZ, half, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[1], 
			half_minX, node_minZ, node_maxX, half_maxZ, half, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[2], 
			node_minX, half_minZ, half_maxX, node_maxZ, half, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[3], 
			half_minX, half_minZ, node_maxX, node_maxZ, half, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		if (node.children[0].full && node.children[1].full && node.children[2].full && node.children[3].full)
		{
			node.full = true;
			node.children = null;
		}
	}
	
	
	@Override
	public void add(BitSet blocks, int[]... bounds) 
	{
		expandAsNeeded(bounds);
		
		add(root,
			(max[0] - min[0] + 1),
			min[0], min[1], max[0], max[1],
			bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1],
			blocks
			);
	}

	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		REMOVE VOLUME																		 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		remove() SINGLE BLOCK
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	

	/**
	 * 
	 * @param node
	 * @param half
	 * @param node_minX
	 * @param node_minZ
	 * @param node_maxX
	 * @param node_maxZ
	 * @param blockX
	 * @param blockZ
	 */
	protected void remove(Node node, 
						  int half,
						  int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
						  int blockX, int blockZ
						  )
	{
		if (node.children == null) return;
		if (node.full)
		{
			node.full = false; 
			node.children = Node.emptyNodeArray(4);
		}
		
		int index = 0;
		
		if (node_minX  + half > blockX)
			node_maxX -= half;
		else
		{
			node_minX += half;
			index += 1;
		}
		if (node_minZ  + half > blockZ)
			node_maxZ -= half;
		else
		{
			node_minZ += half;
			index += 2;
		}
		
		if (half > 1) remove(node.children[index],
							 half >>>= 1,
							 node_minX, node_minZ, node_maxX, node_maxZ,
							 blockX, blockZ
							 );
		else
		{
			node.children[index].full = false;
			for (Node child : node.children)
				if (child.full)
					return;
			
			node.full = false;
			node.children = null;
		}
	}
	
	
	@Override
	public void remove(int... coords) 
	{
		remove(root,
			   (max[0] - min[0] + 1) / 2,
			   this.min[0], this.min[1], this.max[0], this.max[1],
			   coords[0], coords[1]
			   );
		
		trimAsNeeded();
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		remove() BOUNDED SELECTION
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	@Override
	public void remove(int[]... bounds) 
	{
		// TODO Auto-generated method stub
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		remove() COMPLEX BOUNDED SELECTION
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	@Override
	public void remove(BitSet blocks, int[]... bounds) 
	{
		// TODO Auto-generated method stub
	}
}
