package regions;

import java.util.BitSet;

public class BitRegion implements UtilBitSet
{
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		CONSTRUCTORS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	private int[] min;				public int[] getMin()	  { return min;		}
	private int[] max;				public int[] getMax()	  { return max;		}
	private int[] minTrue;			public int[] getMinTrue() { return minTrue;	}
	private int[] maxTrue;			public int[] getMaxTrue() { return maxTrue;	}
	
	public		final boolean 	is3D;
	protected	final BitSet 	blocks;
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 */
	public BitRegion(int minX, int minZ)
	{
		this.min = new int[] { minX, minZ };
		this.is3D = false;
		this.blocks = new BitSet();
	}
	
	
	/**
	 * 
	 * 
	 * @param minX
	 * @param minZ
	 * @param minY
	 */
	public BitRegion(int minX, int minZ, int minY)
	{
		this.min = new int[] { minX, minZ, minY };
		this.is3D = true;
		this.blocks = new BitSet();
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		GET VALUE
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * 
	 * @return
	 */
	public int getVolume()
	{
		return blocks.cardinality();
	}
	
	
	/**
	 * 
	 * 
	 * @return
	 */
	public int getBoundsVolume()
	{
		if (max == null) return 0;
		
		return is3D ?
				(max[0] - min[0]) * (max[1] - min[1]) * (max[2] - min[2]) :
				(max[0] - min[0]) * (max[1] - min[1]);
	}
	
	
	/**
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getIndex(int x, int z)
	{
		return (x - min[0]) + (z - min[1]);
	}
	
	
	/**
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getIndex(int x, int z, int y)
	{
		return (x - min[0]) + (z - min[1]) + (y - min[2]);
	}
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public int getCoords(int index)
	{
		return is3D ?
				 :
				;
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
