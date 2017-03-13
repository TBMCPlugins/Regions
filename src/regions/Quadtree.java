package regions;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

import regions.Tree.Node;

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
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		contains() SINGLE BLOCK
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	@Override
	public boolean contains(int... coords)
	{
		if (coords[0] < min[0] || coords[0] >= max[0] ||
			coords[1] < min[1] || coords[1] >= max[1]
			)
			return false;
		
		int half = max[0] - min[0] >>> 1,
			minX = min[0],
			minZ = min[1];
		
		Node node = root;
		int index;
		while (true)
		{
			if (node.full) return true;
			else if (node.children == null) 
				return false;
			
			index = 0;
			if (minX + half <= coords[0])
			{
				minX += half;
				index += 1;
			}
			if (minZ + half <= coords[1])
			{
				minZ += half;
				index += 2;
			}
			half >>>= 1;
			node = node.children[index];
		}
	}
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		contains() BOUNDED SELECTION
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
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
	 * @param xMinPercent
	 * @param zMinPercent
	 * @param xMaxPercent
	 * @param zMaxPercent
	 */
	protected void expand(double xMinPercent, double zMinPercent, double xMaxPercent, double zMaxPercent)
	{
		int xMin = (int) Math.ceil(xMinPercent),
			zMin = (int) Math.ceil(zMinPercent),
			xMax = (int) Math.ceil(xMaxPercent),
			zMax = (int) Math.ceil(zMaxPercent),
			
			size = nextPowerOfTwo(xMin + xMax + 1, 
								  zMin + zMax + 1
								  ),
		
			xMargin = size - (xMin + xMax + 1),
			zMargin = size - (zMin + zMax + 1),
			xMarginHalf = xMargin / 2,
			zMarginHalf = zMargin / 2;
		
		
		xMin += xMarginHalf;
		zMin += zMarginHalf;
		xMax += xMarginHalf;
		zMax += zMarginHalf;
		if (xMargin % 2 == 1) if (xMin - xMinPercent > xMax - xMaxPercent) xMin++; else xMax++;
		if (zMargin % 2 == 1) if (zMin - zMinPercent > zMax - zMaxPercent) zMin++; else zMax++;
		
		{
			int sideLength = max[0] - min[0];
			min[0] -= (sideLength * xMin);
			min[1] -= (sideLength * zMin);
			max[0] += (sideLength * xMax);
			max[1] += (sideLength * zMax);
		}
		
		int index;
		Node[] children;
		Node[] newRootChildren = children = Node.emptyNodeArray(4);
				
		while(true)
		{
			size >>>= 1;
			index = 0;
			
			if (xMin >= size)
				xMin -= size;
			else
			{
				xMax -= size;
				index += 1;
			}
			if (zMin >= size)
				zMin -= size;
			else
			{
				zMax -= size;
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
		int sideLength = max[0] - min[0];
		
		double	xMinPercent = 0,
				zMinPercent = 0,
				xMaxPercent = 0,
				zMaxPercent = 0;
		
		if 		(coords[0] <  min[0]) xMinPercent = (min[0] - coords[0]) / sideLength;
		else if (coords[0] >= max[0]) xMaxPercent = (coords[0] - max[0]) / sideLength;
		
		if 		(coords[1] <  min[1]) zMinPercent = (min[1] - coords[1]) / sideLength;
		else if	(coords[1] >= max[1]) zMaxPercent = (coords[1] - max[1]) / sideLength;
		
		if (xMinPercent != 0 ||
			zMinPercent != 0 ||
			xMaxPercent != 0 ||
			zMaxPercent != 0
			)
			expand(xMinPercent, 
				   zMinPercent, 
				   xMaxPercent, 
				   zMaxPercent);
	}
	
	
	@Override
	public void expandAsNeeded(int[]... bounds)
	{
		int sideLength = max[0] - min[0];
		
		double	xMinPercent = 0,
				zMinPercent = 0,
				xMaxPercent = 0,
				zMaxPercent = 0;
		
		if (bounds[0][0] <  min[0]) xMinPercent = (min[0] - bounds[0][0]) / sideLength;
		if (bounds[0][1] >= max[0]) xMaxPercent = (bounds[0][1] - max[0]) / sideLength;
		
		if (bounds[1][0] <  min[1]) zMinPercent = (min[1] - bounds[1][0]) / sideLength;
		if (bounds[1][1] >= max[1]) zMaxPercent = (bounds[1][1] - max[1]) / sideLength;
		
		if (xMinPercent != 0 ||
			zMinPercent != 0 ||
			xMaxPercent != 0 ||
			zMaxPercent != 0
			)
			expand(xMinPercent, 
				   zMinPercent, 
				   xMaxPercent, 
				   zMaxPercent);
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
	protected void add(Node node, int half,
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
		
		if (half > 1) add(node.children[index], half >>>= 1,
						  node_minX, node_minZ, node_maxX, node_maxZ,
						  blockX, blockZ
						  );
		else
		{
			node.children[index].full = true;
			if (node.children[0].full && 
				node.children[1].full && 
				node.children[2].full && 
				node.children[3].full
				)
			{
				node.full = true;
				node.children = null;
			}
		}
	}
	
	
	@Override
	public void add(int... coords) 
	{
		expandAsNeeded(coords);
		
		add(root, max[0] - min[0] >>> 1,
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
	protected void add(Node node, int half,
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
				if (node_maxZ <= sel_minZ)
					return;
			}
			if (node_minZ >= sel_maxZ || node_maxZ <= sel_minZ)
				return;
		}
		if (node_maxX <= sel_minX || node_minZ >= sel_maxZ || node_maxZ <= sel_minZ)
			return;
		
		if (node.children == null) 
			node.children = Node.emptyNodeArray(4);
		
		int midpointX = min[0] + half, 
			midpointZ = min[1] + half;
		
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
		
		add(node.children[0], half,
			node_minX, node_minZ, midpointX, midpointZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[1], half,
			midpointX, node_minZ, node_maxX, midpointZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[2], half,
			node_minX, midpointZ, midpointX, node_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[3], half,
			midpointX, midpointZ, node_maxX, node_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		if (node.children[0].full && node.children[1].full && 
			node.children[2].full && node.children[3].full
			)
		{
			node.full = true;
			node.children = null;
		}
	}
	
	
	@Override
	public void add(int[]... bounds) 
	{
		expandAsNeeded(bounds);
		
		add(root, max[0] - min[0] >>> 1,
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
	 * @param node_minX
	 * @param node_minZ
	 * @param node_maxX
	 * @param node_maxZ
	 * @param sel_minX
	 * @param sel_minZ
	 * @param sel_maxX
	 * @param sel_maxZ
	 * @param blocks
	 * @return
	 */
	protected static boolean attemptAdd(Node node,
										int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
										int sel_minX,  int sel_minZ,  int sel_maxX,  int sel_maxZ,
										BitSet blocks
										)
	{
		int partial = _2D.compareRegion(node_minX, node_minZ, node_maxX, node_maxZ, 
										sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
									  	blocks
										);
		
		if (partial == 0) return false;
		if (partial == 1) return true;
		
		node.full = true;
		node.children = null;
		return true;
	}
	
	
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
	protected void add(Node node, int half,
					   int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
					   int sel_minX,  int sel_minZ,  int sel_maxX,  int sel_maxZ,
					   BitSet blocks
					   )
	{
		if (node.full ||
			node_minX >= sel_maxX || node_maxX <= sel_minX || 
			node_minZ >= sel_maxZ || node_maxZ <= sel_minZ ||
			attemptAdd(node,
					   node_minX, node_minZ, node_maxX, node_maxZ, 
					   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
					   blocks
					   ))
			return;
		
		if (node.children == null) node.children = Node.emptyNodeArray(4);
		
		int midpointX = min[0] + half,
			midpointZ = min[1] + half;
		
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
		
		add(node.children[0], half,
			node_minX, node_minZ, midpointX, midpointZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[1], half,
			midpointX, node_minZ, node_maxX, midpointZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[2], half,
			node_minX, midpointZ, midpointX, node_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		add(node.children[3], half,
			midpointX, midpointZ, node_maxX, node_maxZ, 
			sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			);
		
		if (node.children[0].full && node.children[1].full && 
			node.children[2].full && node.children[3].full
			)
		{
			node.full = true;
			node.children = null;
		}
	}
	
	
	@Override
	public void add(BitSet blocks, int[]... bounds) 
	{
		expandAsNeeded(bounds);
		
		add(root, max[0] - min[0] >>> 1,
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
	
	
	@Override
	public void trimAsNeeded()
	{
		int half;
		
		while (!root.full && root.children != null)
		{
			half = max[0] - min[0] >>> 1;
			if (root.children[0].children != null)
			{
				if (root.children[1].children == null &&
					root.children[2].children == null &&
					root.children[3].children == null
					)
				{
					root.children = root.children[0].children;
					max[0] -= half;
					max[1] -= half;
					continue;
				}
			}
			
			else if (root.children[1].children != null)
			{
				if (root.children[2].children == null &&
					root.children[3].children == null
					)
				{
					root.children = root.children[1].children;
					min[0] += half;
					max[1] -= half;
					continue;
				}
			}
			
			else if (root.children[2].children != null)
			{
				if (root.children[3].children == null)
				{
					root.children = root.children[2].children;
					max[0] -= half;
					min[1] += half;
					continue;
				}
			}
			
			else if (root.children[3].children != null)
			{
				root.children = root.children[3].children;
				min[0] += half;
				min[1] += half;
			}
		}
	}
	
	
	
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
	protected void remove(Node node, int half,
						  int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
						  int blockX, int blockZ
						  )
	{
		if (node.full)
		{
			node.full = false; 
			node.children = Node.fullNodeArray(4);
		}
		else if (node.children == null) return;
		
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
		
		if (half > 1) remove(node.children[index], half >>>= 1,
							 node_minX, node_minZ, node_maxX, node_maxZ,
							 blockX, blockZ
							 );
		else
		{
			node.children[index].full = false;
			if (node.children[0].children == null && 
				node.children[1].children == null && 
				node.children[2].children == null && 
				node.children[3].children == null
				)
			{
				node.full = false;
				node.children = null;
			}
		}
	}
	
	
	@Override
	public void remove(int... coords) 
	{
		remove(root, max[0] - min[0] >>> 1,
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
	protected void remove(Node node, int half,
						  int node_minX, int node_minZ, int node_maxX, int node_maxZ,
						  int sel_minX,  int sel_minZ,  int sel_maxX,  int sel_maxZ
						  )
	{
		if (!node.full && node.children == null) 
			return;
		
		if (node_minX >= sel_minX)
		{
			if (node_maxX <= sel_maxX)
			{
				if (node_minZ >= sel_minZ)
				{
					if (node_maxZ <= sel_maxZ)
					{
						node.full = false;
						node.children = null;
					}
					return;
				}
				if (node_maxZ <= sel_minZ)
					return;
			}
			if (node_minZ >= sel_maxZ || node_maxZ <= sel_minZ)
				return;
		}
		if (node_maxX <= sel_minX || node_minZ >= sel_maxZ || node_maxZ <= sel_minZ)
			return;
		
		if (node.full) 
		{
			node.full = false;
			node.children = Node.fullNodeArray(4);
		}
		
		int midpointX = min[0] + half, 
			midpointZ = min[1] + half;
		
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
		
		remove(node.children[0], half,
			   node_minX, node_minZ, midpointX, midpointZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			   );
		
		remove(node.children[1], half,
			   midpointX, node_minZ, node_maxX, midpointZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			   );
		
		remove(node.children[2], half,
			   node_minX, midpointZ, midpointX, node_maxZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			   );
		
		remove(node.children[3], half,
			   midpointX, midpointZ, node_maxX, node_maxZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ
			   );
		
		if (node.children[0].children == null && node.children[1].children == null && 
			node.children[2].children == null && node.children[3].children == null
			)
		{
			node.full = false;
			node.children = null;
		}
	}
	
	
	@Override
	public void remove(int[]... bounds) 
	{
		remove(root, max[0] - min[0] >>> 1,
			   this.min[0], this.min[1], this.max[0], this.max[1],
			   bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1]
			   );
		
		trimAsNeeded();
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		remove() COMPLEX BOUNDED SELECTION
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * @param node
	 * @param node_minX
	 * @param node_minZ
	 * @param node_maxX
	 * @param node_maxZ
	 * @param sel_minX
	 * @param sel_minZ
	 * @param sel_maxX
	 * @param sel_maxZ
	 * @param blocks
	 * @return
	 */
	protected static boolean attemptRemove(Node node,
										   int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
										   int sel_minX,  int sel_minZ,  int sel_maxX,  int sel_maxZ,
										   BitSet blocks
										   )
	{
		int partial = _2D.compareRegion(node_minX, node_minZ, node_maxX, node_maxZ, 
										sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
									  	blocks
										);
		
		if (partial == 0) return false;
		if (partial == 1) return true;
		
		node.full = false;
		node.children = null;
		return true;
	}
	
	
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
	protected void remove(Node node, int half,
						  int node_minX, int node_minZ, int node_maxX, int node_maxZ, 
						  int sel_minX,  int sel_minZ,  int sel_maxX,  int sel_maxZ,
						  BitSet blocks
						  )
	{
		if ((!node.full && node.children == null) ||
			node_minX >= sel_maxX || node_maxX <= sel_minX || 
			node_minZ >= sel_maxZ || node_maxZ <= sel_minZ ||
			attemptRemove(node,
						  node_minX, node_minZ, node_maxX, node_maxZ, 
						  sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
						  blocks
						  ))
			return;
		
		if (node.children == null) node.children = Node.fullNodeArray(4);
		
		int midpointX = min[0] + half,
			midpointZ = min[1] + half;
		
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
		
		remove(node.children[0], half,
			   node_minX, node_minZ, midpointX, midpointZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
			   blocks
			   );
		
		remove(node.children[1], half,
			   midpointX, node_minZ, node_maxX, midpointZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
			   blocks
			   );
		
		remove(node.children[2], half,
			   node_minX, midpointZ, midpointX, node_maxZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
			   blocks
			   );
		
		remove(node.children[3], half,
			   midpointX, midpointZ, node_maxX, node_maxZ, 
			   sel_minX,  sel_minZ,  sel_maxX,  sel_maxZ,
			   blocks
			   );
		
		if (node.children[0].children == null && node.children[1].children == null && 
			node.children[2].children == null && node.children[3].children == null
			)
		{
			node.full = false;
			node.children = null;
		}
	}
	
	
	@Override
	public void remove(BitSet blocks, int[]... bounds) 
	{
		remove(root, max[0] - min[0] >>> 1,
			   min[0], min[1], max[0], max[1],
			   bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1],
			   blocks
			   );
		
		trimAsNeeded();
	}
}
