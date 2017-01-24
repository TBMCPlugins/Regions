package regions;

import java.util.BitSet;

public class BitRegion
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
	
	public final boolean is3D;
	
	protected final BitSet blocks;
	
	
	protected BitRegion(int xSideLength, int zSideLength)
	{
		sides	= new int[] { xSideLength, zSideLength };
		is3D 	= false;
		blocks	= new BitSet();
	}
	
	
	protected BitRegion(int xSideLength, int zSideLength, int ySideLength)
	{
		sides	= new int[] { xSideLength, zSideLength, ySideLength };
		is3D 	= true;
		blocks	= new BitSet();
	}
	
	
	public static BitRegion new2DBitRegion()
	{
		return new BitRegion(0, 0);
	}
	
	
	public static BitRegion new3DBitRegion()
	{
		return new BitRegion(0, 0, 0);
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
	 * Get number of blocks contained in the region
	 */
	public int getVolume()
	{
		return blocks.cardinality();
	}
	
	
	/**
	 * Get number of blocks contained in the region's bounding box
	 */
	public int getBoundsVolume()
	{
		return is3D ?
				(sides[0]) * (sides[1]) * (sides[2]) :
				(sides[0]) * (sides[1]);
	}
	
	
	/**
	 * Get coordinates for the block at the given index in the BitSet
	 * 
	 * @param index
	 * @return
	 */
	protected int[] getCoords(int index)
	{
		if (++index > getBoundsVolume()) return null;
		
		return is3D ? 
				getCoords3D(index) : 
				getCoords2D(index);
	}
	
	/**
	 * 
	 * 
	 * @param index
	 * @return
	 */
	protected int[] getCoords3D(int index)
	{
		if (index == 0) return new int[] {0, 0, 0};
		
		int[] xzy = new int[3];
		
		int crossSec = sides[0] * sides[1];
		
		xzy[2] = index / crossSec 	- ((index = index % crossSec) == 0 ? 1 : 0);		index = (index == 0 ? crossSec : index);
		xzy[1] = index / sides[0]	- ((index = index % sides[0]) == 0 ? 1 : 0);		index = (index == 0 ? sides[0] : index);
		xzy[0] = index - 1;
		
		return xzy;
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	protected int[] getCoords2D(int index)
	{
		if (index == 0) return new int[] {0, 0};
		
		int[] xz = new int[2];
			
		xz[1] = index / sides[0]	- ((index = index % sides[0]) == 0 ? 1 : 0);		index = (index == 0 ? sides[0] : index);
		xz[0] = index - 1;
		
		return xz;
	}
	
	
	
	/*-------------------------------------
		OVERLOADS : getIndex()
	-------------------------------------*/
	/**
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getIndex(int x, int z)//TODO return -1 if outside bounds
	{
		return (z * sides[0]) + x;
	}
	
	/**
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getIndex(int x, int z, int y)//TODO return -1 if outside bounds
	{
		return (y * sides[0] * sides[1]) + (z * sides[0]) + x;
	}
	
	
	/*-------------------------------------
		OVERLOADS : blockAt()
	-------------------------------------*/
	/**
	 * 
	 * @param x
	 * @param z
	 * @return
	 */
	public boolean blockAt(int x, int z)//TODO return false if outside bounds
	{
		return blocks.get(getIndex(x,z));
	}
	
	/**
	 * 
	 * @param x
	 * @param z
	 * @param y
	 * @return
	 */
	public boolean blockAt(int x, int z, int y)//TODO return false if outside bounds
	{
		return blocks.get(getIndex(x, z, y));
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public boolean blockAt(int index)
	{
		return blocks.get(index);
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		OVERLOADS : shift()
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * @param shift
	 * @param fromIndex
	 */
	public void bitshift(int shift, int fromIndex)
	{
		if (shift < 1) return;
		
		int toAboveThisIndex = fromIndex + shift - 1;
		
		for (int i = blocks.size() + shift; i > toAboveThisIndex; i++)
		{
			blocks.set(i - shift, blocks.get(i));
		}
		blocks.clear(fromIndex, toAboveThisIndex);
	}
	
	
	
	public void shift(int xShift, int zShift)
	{
		
	}
	
	
	
	public void shift(int xShift, int zShift, int yShift)
	{
		
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		OVERLOADS : fill()
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
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
		blocks.set(getIndex(x, z));
	}
	
	
	/**
	 * 
	 * @param x
	 * @param z
	 * @param y
	 */
	public void fill(int x, int z, int y)
	{
		blocks.set(getIndex(x, z, y));
	}
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void fill(int minX, int minZ, int maxX, int maxZ)//TODO adjust bounds if necessary
	{
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
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void fill(int minX, int minZ, int minY, int maxX, int maxZ, int maxY)//TODO adjust bounds if necessary
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
					blocks.set(index++);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 * @param blocksToFill
	 */
	public void fill(int minX, int minZ, int maxX, int maxZ, BitSet blocksToFill)//TODO adjust bounds if necessary
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
				if (blocksToFill.get(index2++)) blocks.set(index1++);
			}
		}
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
	 * @param blocksToFill
	 */
	public void fill(int minX, int minZ, int minY, int maxX, int maxZ, int maxY, BitSet blocksToFill)//TODO adjust bounds if necessary
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
					if (blocksToFill.get(index2++)) blocks.set(index1++);
				}
			}
		}
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		OVERLOADS : clear()
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
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
	 * @param maxX
	 * @param maxZ
	 */
	public void clear(int minX, int minZ, int maxX, int maxZ)
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
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 */
	public void clear(int minX, int minZ, int minY, int maxX, int maxZ, int maxY)
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
	 * 
	 * @param minX
	 * @param minZ
	 * @param maxX
	 * @param maxZ
	 * @param blocksToClear
	 */
	public void clear(int minX, int minZ, int maxX, int maxZ, BitSet blocksToClear)
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
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param minY
	 * @param maxX
	 * @param maxZ
	 * @param maxY
	 * @param blocksToClear
	 */
	public void clear(int minX, int minZ, int minY, int maxX, int maxZ, int maxY, BitSet blocksToClear)
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
