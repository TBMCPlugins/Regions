package regions;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

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
			if (child.children != null)
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
		if (coords[0] < min[0] || coords[0] > max[0] ||
			coords[1] < min[1] || coords[1] > max[1] ||
			coords[2] < min[2] || coords[2] > max[2]
			)
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
		int xMinCeil = (int) Math.ceil(xMinExpansion),
			zMinCeil = (int) Math.ceil(zMinExpansion),
			yMinCeil = (int) Math.ceil(yMinExpansion),
			xMaxCeil = (int) Math.ceil(xMaxExpansion),
			zMaxCeil = (int) Math.ceil(zMaxExpansion),
			yMaxCeil = (int) Math.ceil(yMaxExpansion),
			
			size = nextPowerOfTwo(xMinCeil + xMaxCeil + 1, 
								  zMinCeil + zMaxCeil + 1, 
								  yMinCeil + yMaxCeil + 1
								  ),
		
			xMargin = size - (xMinCeil + xMaxCeil + 1),
			zMargin = size - (zMinCeil + zMaxCeil + 1),
			yMargin = size - (yMinCeil + yMaxCeil + 1),
			xMarginHalf = xMargin / 2,
			zMarginHalf = zMargin / 2,
			yMarginHalf = yMargin / 2;
		
		
		xMinCeil += xMarginHalf;
		zMinCeil += zMarginHalf;
		yMinCeil += yMarginHalf;
		xMaxCeil += xMarginHalf;
		zMaxCeil += zMarginHalf;
		yMaxCeil += yMarginHalf;
		
		if (xMargin % 2 == 1) if (xMinCeil - xMinExpansion > xMaxCeil - xMaxExpansion) xMinCeil++; else xMaxCeil++;
		
		if (zMargin % 2 == 1) if (zMinCeil - zMinExpansion > zMaxCeil - zMaxExpansion) zMinCeil++; else zMaxCeil++;
		
		if (yMargin % 2 == 1) if (yMinCeil - yMinExpansion > yMaxCeil - yMaxExpansion) yMinCeil++; else yMaxCeil++;
		
		int sideLength = max[0] - min[0] + 1;
		min[0] -= (sideLength * xMinCeil);
		min[1] -= (sideLength * zMinCeil);
		min[2] -= (sideLength * yMinCeil);
		max[0] += (sideLength * xMaxCeil);
		max[1] += (sideLength * zMaxCeil);
		max[2] += (sideLength * yMaxCeil);
		
		
		int index;
		Node[] children;
		Node[] newRootChildren = children = Node.emptyNodeArray(8);
		
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
			if (yMinCeil >= size)
				yMinCeil -= size;
			else
			{
				yMaxCeil -= size;
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
		
		if (bounds[1][0] < min[1]) zMinExpansion = (min[1] - bounds[1][0]) / sideLength;
		if (bounds[1][1] > max[1]) zMaxExpansion = (bounds[1][1] - max[1]) / sideLength;
		
		if (bounds[2][0] < min[2]) yMinExpansion = (min[2] - bounds[2][0]) / sideLength;
		if (bounds[2][1] > max[2]) yMaxExpansion = (bounds[2][1] - max[2]) / sideLength;
		
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
					   int node_minX, int node_minZ, int node_minY, int node_maxX, int node_maxZ, int node_maxY, 
					   int blockX, int blockZ, int blockY
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
		if (node_minY  + half > blockY)
			node_maxY -= half;
		else
		{
			node_minY += half;
			index += 4;
		}
		
		if (half > 1) add(node.children[index],
						  half >>>= 1,
						  node_minX, node_minZ, node_minY, node_maxX, node_maxZ, node_maxY,
						  blockX, blockZ, blockY
						  );
		else
		{
			node.children[index].full = true;
			if (node.children[0].full && node.children[1].full && 
				node.children[2].full && node.children[3].full &&
				node.children[4].full && node.children[5].full && 
				node.children[6].full && node.children[7].full
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
		
		add(root,
			(max[0] - min[0] + 1) / 2,
			min[0], min[1], min[2], max[0], max[1], max[2],
			coords[0], coords[1], coords[2]
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
	 * @param node_minY
	 * @param node_maxX
	 * @param node_maxZ
	 * @param node_maxY
	 * @param sel_minX
	 * @param sel_minZ
	 * @param sel_minY
	 * @param sel_maxX
	 * @param sel_maxZ
	 * @param sel_maxY
	 */
	protected void add(Node node, 
					   int half,
					   int node_minX, int node_minZ, int node_minY, int node_maxX, int node_maxZ, int node_maxY,
					   int sel_minX,  int sel_minZ,  int sel_minY,  int sel_maxX,  int sel_maxZ,  int sel_maxY
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
						if (node_minY >= sel_minY)
						{
							if (node_maxY <= sel_maxY)
							{
								node.full = true;
								node.children = null;
							}
							return;
						}
						if (node_maxY < sel_minY)
							return;
					}
					if (node_minY > sel_maxY || node_maxY < sel_minY)
						return;
				}
				if (node_maxZ < sel_minZ || node_minY > sel_maxY || node_maxY < sel_minY)
					return;
			}
			if (node_minZ > sel_maxZ || node_maxZ < sel_minZ || node_minY > sel_maxY || node_maxY < sel_minY)
				return;
		}
		if (node_maxX < sel_minX || node_minZ > sel_maxZ || node_maxZ < sel_minZ || node_minY > sel_maxY || node_maxY < sel_minY)
			return;
		
		if (node.children == null) 
			node.children = Node.emptyNodeArray(8);
		
		int half_minX = min[0] + half,
			half_maxX = half_minX - 1,
			half_minZ = min[1] + half,
			half_maxZ = half_minZ - 1,
			half_minY = min[2] + half,
			half_maxY = half_minY - 1;
		
		half >>>= 1;
		
		
		/* child index:
									  Y → Y
			X         →         X				X         →         X
		  Z	╔═════════╦═════════╗			  Z	╔═════════╦═════════╗
			║         ║         ║				║         ║         ║
			║    0    ║    1    ║				║    4    ║    5    ║
			║         ║         ║				║         ║         ║
		  ↓	╠═════════╬═════════╣			  ↓	╠═════════╬═════════╣
			║         ║         ║				║         ║         ║
			║    2    ║    3    ║				║    6    ║    7    ║
			║         ║         ║				║         ║         ║
		  Z	╚═════════╩═════════╝			  Z	╚═════════╩═════════╝*/
		
		add(node.children[0],
			half,
			node_minX, node_minZ, node_minY, half_maxX, half_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[1],
			half,
			half_minX, node_minZ, node_minY, node_maxX, half_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[2],
			half,
			node_minX, half_minZ, node_minY, half_maxX, node_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[3],
			half,
			half_minX, half_minZ, node_minY, node_maxX, node_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[4],
			half,
			node_minX, node_minZ, half_minY, half_maxX, half_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[5],
			half,
			half_minX, node_minZ, half_minY, node_maxX, half_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[6],
			half,
			node_minX, half_minZ, half_minY, half_maxX, node_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[7],
			half,
			half_minX, half_minZ, half_minY, node_maxX, node_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		if (node.children[0].full && node.children[1].full && 
			node.children[2].full && node.children[3].full &&
			node.children[4].full && node.children[5].full && 
			node.children[6].full && node.children[7].full
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
		
		add(root, 
			(max[0] - min[0] + 1),
			min[0], min[1], min[2], max[0], max[1], max[2], 
			bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]
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
	 * @param node_minY
	 * @param node_maxX
	 * @param node_maxZ
	 * @param node_maxY
	 * @param sel_minX
	 * @param sel_minZ
	 * @param sel_minY
	 * @param sel_maxX
	 * @param sel_maxZ
	 * @param sel_maxY
	 * @param blocks
	 */
	protected void add(Node node, 
					   int half,
					   int node_minX, int node_minZ, int node_minY, int node_maxX, int node_maxZ, int node_maxY,
					   int sel_minX,  int sel_minZ,  int sel_minY,  int sel_maxX,  int sel_maxZ,  int sel_maxY,
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
						if (node_minY >= sel_minY)
						{
							if (node_maxY <= sel_maxY)
							{
								node.full = true;
								node.children = null;
							}
							return;
						}
						if (node_maxY < sel_minY)
							return;
					}
					if (node_minY > sel_maxY || node_maxY < sel_minY)
						return;
				}
				if (node_maxZ < sel_minZ || node_minY > sel_maxY || node_maxY < sel_minY)
					return;
			}
			if (node_minZ > sel_maxZ || node_maxZ < sel_minZ || node_minY > sel_maxY || node_maxY < sel_minY)
				return;
		}
		if (node_maxX < sel_minX || node_minZ > sel_maxZ || node_maxZ < sel_minZ || node_minY > sel_maxY || node_maxY < sel_minY)
			return;
		
		if (node.children == null) 
			node.children = Node.emptyNodeArray(8);
		
		int half_minX = min[0] + half,
			half_maxX = half_minX - 1,
			half_minZ = min[1] + half,
			half_maxZ = half_minZ - 1,
			half_minY = min[2] + half,
			half_maxY = half_minY - 1;
		
		half >>>= 1;
		
		
		/* child index:
									  Y → Y
			X         →         X				X         →         X
		  Z	╔═════════╦═════════╗			  Z	╔═════════╦═════════╗
			║         ║         ║				║         ║         ║
			║    0    ║    1    ║				║    4    ║    5    ║
			║         ║         ║				║         ║         ║
		  ↓	╠═════════╬═════════╣			  ↓	╠═════════╬═════════╣
			║         ║         ║				║         ║         ║
			║    2    ║    3    ║				║    6    ║    7    ║
			║         ║         ║				║         ║         ║
		  Z	╚═════════╩═════════╝			  Z	╚═════════╩═════════╝*/
		
		add(node.children[0],
			half,
			node_minX, node_minZ, node_minY, half_maxX, half_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[1],
			half,
			half_minX, node_minZ, node_minY, node_maxX, half_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[2],
			half,
			node_minX, half_minZ, node_minY, half_maxX, node_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[3],
			half,
			half_minX, half_minZ, node_minY, node_maxX, node_maxZ, half_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[4],
			half,
			node_minX, node_minZ, half_minY, half_maxX, half_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[5],
			half,
			half_minX, node_minZ, half_minY, node_maxX, half_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[6],
			half,
			node_minX, half_minZ, half_minY, half_maxX, node_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		add(node.children[7],
			half,
			half_minX, half_minZ, half_minY, node_maxX, node_maxZ, node_maxY,
			sel_minX,  sel_minZ,  sel_minY,  sel_maxX,  sel_maxZ,  sel_maxY
			);
		
		if (node.children[0].full && node.children[1].full && 
			node.children[2].full && node.children[3].full &&
			node.children[4].full && node.children[5].full && 
			node.children[6].full && node.children[7].full
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
		
		add(root, 
			(max[0] - min[0] + 1),
			min[0], min[1], min[2], max[0], max[1], max[2], 
			bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1],
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
			half = (max[0] - min[0] + 1) / 2;
			if (root.children[0].children != null)
			{
				if (root.children[1].children == null &&
					root.children[2].children == null &&
					root.children[3].children == null &&
					root.children[4].children == null &&
					root.children[5].children == null &&
					root.children[6].children == null &&
					root.children[7].children == null
					)
				{
					root.children = root.children[0].children;
					max[0] -= half;
					max[1] -= half;
					max[2] -= half;
					continue;
				}
			}
			
			else if (root.children[1].children != null)
			{
				if (root.children[2].children == null &&
					root.children[3].children == null &&
					root.children[4].children == null &&
					root.children[5].children == null &&
					root.children[6].children == null &&
					root.children[7].children == null
					)
				{
					root.children = root.children[1].children;
					min[0] += half;
					max[1] -= half;
					max[2] -= half;
					continue;
				}
			}
			
			else if (root.children[2].children != null)
			{
				if (root.children[3].children == null &&
					root.children[4].children == null &&
					root.children[5].children == null &&
					root.children[6].children == null &&
					root.children[7].children == null)
				{
					root.children = root.children[2].children;
					max[0] -= half;
					min[1] += half;
					max[2] -= half;
					continue;
				}
			}
			
			else if (root.children[3].children != null)
			{
				if (root.children[4].children == null &&
					root.children[5].children == null &&
					root.children[6].children == null &&
					root.children[7].children == null)
				{
					root.children = root.children[3].children;
					max[0] -= half;
					min[1] += half;
					max[2] -= half;
					continue;
				}
			}
			
			if (root.children[4].children != null)
			{
				if (root.children[5].children == null &&
					root.children[6].children == null &&
					root.children[7].children == null
					)
				{
					root.children = root.children[4].children;
					max[0] -= half;
					max[1] -= half;
					min[2] += half;
					continue;
				}
			}
			
			else if (root.children[5].children != null)
			{
				if (root.children[6].children == null &&
					root.children[7].children == null
					)
				{
					root.children = root.children[5].children;
					min[0] += half;
					max[1] -= half;
					min[2] += half;
					continue;
				}
			}
			
			else if (root.children[6].children != null)
			{
				if (root.children[7].children == null)
				{
					root.children = root.children[6].children;
					max[0] -= half;
					min[1] += half;
					min[2] += half;
					continue;
				}
			}
			
			else if (root.children[7].children != null)
			{
				root.children = root.children[7].children;
				max[0] -= half;
				min[1] += half;
				min[2] += half;
				continue;
			}
		}
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		remove() SINGLE BLOCK
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	

	@Override
	public void remove(int... coords) 
	{
		// TODO Auto-generated method stub
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
