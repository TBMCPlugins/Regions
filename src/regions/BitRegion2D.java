package regions;

import java.util.BitSet;

public class BitRegion2D
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
	
	public BitRegion2D()
	{
		sides	= new int[] { 0, 0 };
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
		return (sides[0]) * (sides[1]);
	}
	
	
	/**
	 * Get coordinates for the block at the given index in the BitSet.
	 * 
	 * @param index
	 * @return
	 */
	protected int[] getCoords(int index)
	{
		if (index++ == 0) return new int[] {0, 0};
		
		int[] xz = new int[2];
			
		xz[1] = index / sides[0];	if ((index = index % sides[0]) == 0) { xz[1] -= 1; 	index = sides[0]; }
		xz[0] = index - 1;
		
		return xz;
	}
	
	
	/**
	 * Get the index within the internal BitSet for the given coordinates.
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getIndex(int x, int z)
	{
		return (z * sides[0]) + x;
	}
	
	
	/**
	 * Returns whether or not region contains the block at the given coordinates.
	 * 
	 * @param x
	 * @param z
	 * @return
	 */
	public boolean contains(int x, int z)
	{
		if (0 > x || x >= sides[0] || 
			0 > z || z >= sides[1]) 
			return false;
		
		return blocks.get(getIndex(x,z));
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
			blocks.set(i, blocks.get(i - shift));
		
		blocks.clear(fromIndex, toAboveThisIndex);
	}
	
	
	/**
	 * 
	 * @param xShift
	 * @param zShift
	 * @param yShift
	 */
	public void expand(int xMin, int xMax, int zMin, int zMax)
	{
		int[] newSides = new int[] {xMax - xMin + 1, 
									zMax - zMin + 1};
		
		/* add empty space before each x row
		 * add empty space at bottom
		 */
	}
	
	
	/**
	 * 
	 * @param x
	 * @param z
	 */
	public void adjustBoundsIfNecessary(int x, int z)
	{
		if (-1 < x && x < sides[0] && 
			-1 < z && z < sides[1]) 
			return;
		
		int bX = sides[0] - 1;
		int bZ = sides[1] - 1;
		
		expand(	x < 0  ? x : 0, 
				x > bX ? x : bX,
				
				z < 0  ? z : 0,
				z > bZ ? z : bZ);
	}
	
	
	/**
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void adjustBoundsIfNecessary(int minX, int maxX, int minZ, int maxZ)
	{
		if (-1 < minX && maxX < sides[0] &&
			-1 < minZ && maxZ < sides[1]) 
			return;
		
		int bX = sides[0] - 1;
		int bZ = sides[1] - 1;
		
		expand(	minX < 0  ? minX : 0, 
				maxX > bX ? maxX : bX,
				
				minZ < 0  ? minZ : 0,
				maxZ > bZ ? maxZ : bZ);
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
	 */
	public void fill(int x, int z)
	{
		adjustBoundsIfNecessary(x, z);
		
		blocks.set(getIndex(x, z));
	}
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void fill(int minX, int maxX, int minZ, int maxZ)
	{
		adjustBoundsIfNecessary(minX, maxX, minZ, maxZ);
		
		int z;
		int x;
		
		int index;
		
		for (z = minZ; z <= maxZ; z++)
		{
			index = getIndex(minX, z);
			for (x = minX; x <= maxX; x++) 
			{	
				blocks.set(index++);
			}
		}
	}
	
	
	/**
	 * 
	 * @param blocksToFill
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void fill(BitSet blocksToFill, int minX, int maxX, int minZ, int maxZ)
	{
		adjustBoundsIfNecessary(minX, maxX, minZ, maxZ);
		
		int z;
		int x;
		
		int index1;
		int index2 = 0;
		
		for (z = minZ; z <= maxZ; z++)
		{
			index1 = getIndex(minX, z);
			for (x = minX; x <= maxX; x++) 
			{	
				if (blocksToFill.get(index2++)) blocks.set(index1++);
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
	 */
	public void clear(int x, int z)
	{
		blocks.clear(getIndex(x, z));
	}
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void clear(int minX, int maxX, int minZ, int maxZ)
	{
		int z;
		int x;
		
		int index;
		
		for (z = minZ; z <= maxZ; z++)
		{
			index = getIndex(minX, z);
			for (x = minX; x <= maxX; x++) 
			{	
				blocks.clear(index++);
			}
		}
	}
	
	
	/**
	 * 
	 * @param blocksToClear
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void clear(BitSet blocksToClear, int minX, int maxX, int minZ, int maxZ)
	{
		int z;
		int x;
		
		int index1;
		int index2 = 0;
		
		for (z = minZ; z <= maxZ; z++)
		{
			index1 = getIndex(minX, z);
			for (x = minX; x <= maxX; x++) 
			{	
				if (blocksToClear.get(index2++)) blocks.clear(index1++);
			}
		}
	}
}
