package regions;

import java.util.BitSet;

public class BitRegion3D
{
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		CONSTRUCTORS																		 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	protected int[] sides;		public int[] getSides() { return sides;	}
	
	protected final BitSet blocks;
	
	protected BitRegion3D()
	{
		sides	= new int[] { 0, 0, 0 };
		blocks	= new BitSet();
	}
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		GET VALUE																			 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
	/**
	 * Get number of blocks contained in the region.
	 */
	public int getVolume()
	{
		return blocks.cardinality();
	}
	
	
	/**
	 * Get number of blocks contained in the region's bounding box.
	 */
	public int getBoundsVolume()
	{
		return (sides[0]) * (sides[1]) * (sides[2]);
	}
	
	
	/**
	 * Get coordinates for the block at the given index in the BitSet.
	 * 
	 * @param index
	 * @return
	 */
	protected int[] getCoords3D(int index)
	{
		if (index++ == 0) return new int[] {0, 0, 0};
		
		int[] xzy = new int[3];
		
		int crossSec = sides[0] * sides[1];
		
		xzy[2] = index / crossSec;	if ((index = index % crossSec) == 0) { xzy[2] -= 1;	index = crossSec; }
		xzy[1] = index / sides[0];	if ((index = index % sides[0]) == 0) { xzy[1] -= 1;	index = sides[0]; }
		xzy[0] = index - 1;
		
		return xzy;
	}
	
	
	/**
	 * Get the index within the internal BitSet for the given coordinates.
	 * 
	 * @param coordinate
	 * @return
	 */
	protected int getIndex(int x, int z, int y)
	{
		return (y * sides[0] * sides[1]) + (z * sides[0]) + x;
	}
	
	
	/**
	 * Returns whether or not region contains the block at the given coordinates.
	 * 
	 * @param x
	 * @param z
	 * @param y
	 * @return
	 */
	public boolean contains(int x, int z, int y)
	{
		if (0 > x || x >= sides[0] || 
			0 > z || z >= sides[1] || 
			0 > y || y >= sides[2]) 
			return false;
		
		return blocks.get(getIndex(x, z, y));
	}
	
	
	/**
	 * Returns the value for this index within the internal BitSet.
	 * 
	 * @param index
	 * @return
	 */
	public boolean bitvalue(int index)
	{
		return blocks.get(index);
	}
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		OPERATIONS																			 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
	/**
	 * 
	 * @param shift
	 * @param fromIndex
	 */
	public void bitshift(int shift, int fromIndex)
	{
		if (shift < 1) return;
		
		int toAboveThisIndex = fromIndex + shift - 1;
		
		for (int i = blocks.size() + shift; i > toAboveThisIndex; i--)
			blocks.set(i - shift, blocks.get(i));
		
		blocks.clear(fromIndex, toAboveThisIndex);
	}
	
	
	/**
	 * 
	 * @param xShift
	 * @param zShift
	 * @param yShift
	 */
	public void expand(int xMin, int xMax, int zMin, int zMax, int yMin, int yMax)
	{
		int[] newSides = new int[] {xMax - xMin + 1, 
									zMax - zMin + 1, 
									yMax - yMin + 1};
		
		/* add empty space before each x row
		 * add empty space at bottom
		 */
	}
	
	
	/**
	 * 
	 * @param x
	 * @param z
	 * @param y
	 */
	public void adjustBoundsIfNecessary(int x, int z, int y)
	{
		if (-1 < x && x < sides[0] && 
			-1 < z && z < sides[1] && 
			-1 < y && y < sides[2]) 
			return;
		
		int bX = sides[0] - 1;
		int bZ = sides[1] - 1;
		int bY = sides[2] - 1;
		
		expand(	x < 0  ? x : 0, 
				x > bX ? x : bX,
				
				z < 0  ? z : 0,
				z > bZ ? z : bZ,
				
				y < 0  ? y : 0,
				y > bY ? y : bY);
	}
	
	
	/**
	 * 
	 * @param minX
	 * @param minZ
	 * @param minY
	 * @param maxX
	 * @param maxZ
	 * @param maxY
	 */
	public void adjustBoundsIfNecessary(int minX, int minZ, int minY, int maxX, int maxZ, int maxY)
	{
		if (-1 < minX && maxX < sides[0] &&
			-1 < minZ && maxZ < sides[1] && 
			-1 < minY && maxY < sides[2]) 
			return;
		
		int bX = sides[0] - 1;
		int bZ = sides[1] - 1;
		int bY = sides[2] - 1;
		
		expand(	minX < 0  ? minX : 0, 
				maxX > bX ? maxX : bX,
				
				minZ < 0  ? minZ : 0,
				maxZ > bZ ? maxZ : bZ,
				
				minY < 0  ? minY : 0,
				maxY > bY ? maxY : bY);
	}
	
	
	
	/*-------------------------------------
		OVERLOADS : fill()
	-------------------------------------*/
	/**
	 * 
	 */
	public void fill()
	{
		blocks.set(0, getBoundsVolume() - 1);
	}
	
	
	/**
	 * 
	 * @param x
	 * @param z
	 * @param y
	 */
	public void fill(int x, int z, int y)
	{
		adjustBoundsIfNecessary(x, z, y);
		
		blocks.set(getIndex(x, z, y));
	}
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param minY
	 * @param maxX
	 * @param maxZ
	 * @param maxY
	 */
	public void fill(int minX, int maxX, int minZ, int maxZ, int minY, int maxY)
	{
		adjustBoundsIfNecessary(minX, maxX, minZ, maxZ, minY, maxY);
		
		int y;
		int z;
		int x;
		
		int index;
		
		for (y = minY; y <= maxY; y++)
		{
			for (z = minZ; z <= maxZ; z++)
			{
				index = getIndex(minX, z, y);
				for (x = minX; x <= maxX; x++) 
				{	
					blocks.set(index++);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param blocksToFill
	 * @param minX
	 * @param minZ
	 * @param minY
	 * @param maxX
	 * @param maxZ
	 * @param maxY
	 */
	public void fill(BitSet blocksToFill, int minX, int maxX, int minZ, int maxZ, int minY, int maxY)
	{
		adjustBoundsIfNecessary(minX, maxX, minZ, maxZ, minY, maxY);
		
		int y;
		int z;
		int x;
		
		int index1;
		int index2 = 0;
		
		for (y = minY; y <= maxY; y++)
		{
			for (z = minZ; z <= maxZ; z++)
			{
				index1 = getIndex(minX, z, y);
				for (x = minX; x <= maxX; x++) 
				{	
					if (blocksToFill.get(index2++)) blocks.set(index1++);
				}
			}
		}
	}
	
	
	
	/*-------------------------------------
		OVERLOADS : clear()
	-------------------------------------*/
	
	
	/**
	 * 
	 */
	public void clear()
	{
		blocks.clear();
	}
	
	
	/**
	 * 
	 * @param x
	 * @param z
	 * @param y
	 */
	public void clear(int x, int z, int y)
	{
		blocks.clear(getIndex(x, z, y));
	}
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param minY
	 * @param maxX
	 * @param maxZ
	 * @param maxY
	 */
	public void clear(int minX, int maxX, int minZ, int maxZ, int minY, int maxY)
	{
		int y;
		int z;
		int x;
		
		int index;
		
		for (y = minY; y <= maxY; y++)
		{
			for (z = minZ; z <= maxZ; z++)
			{
				index = getIndex(minX, z, y);
				for (x = minX; x <= maxX; x++) 
				{	
					blocks.clear(index++);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param blocksToClear
	 * @param minX
	 * @param minZ
	 * @param minY
	 * @param maxX
	 * @param maxZ
	 * @param maxY
	 */
	public void clear(BitSet blocksToClear, int minX, int maxX, int minZ, int maxZ, int minY, int maxY)
	{
		int y;
		int z;
		int x;
		
		int index1;
		int index2 = 0;
		
		for (y = minY; y <= maxY; y++)
		{
			for (z = minZ; z <= maxZ; z++)
			{
				index1 = getIndex(minX, z, y);
				for (x = minX; x <= maxX; x++) 
				{	
					if (blocksToClear.get(index2++)) blocks.clear(index1++);
				}
			}
		}
	}
}
