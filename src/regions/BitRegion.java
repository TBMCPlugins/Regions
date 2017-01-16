package regions;

import java.util.BitSet;

public class BitRegion implements UtilBitSet
{
	private int[] min;				public int[] getMin()	  { return min;		}
	private int[] max;				public int[] getMax()	  { return max;		}
	private int[] minTrue;			public int[] getMinTrue() { return minTrue;	}
	private int[] maxTrue;			public int[] getMaxTrue() { return maxTrue;	}
	
	public		final boolean 	is3D;
	protected	final BitSet 	blocks;
	
	
	/**
	 * 
	 * 
	 * @param min
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
	 * @param min
	 */
	public BitRegion(int minX, int minZ, int minY)
	{
		this.min = new int[] { minX, minZ, minY };
		this.is3D = true;
		this.blocks = new BitSet();
	}
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																															 ║ ║
	║ ║		CALCULATIONS																										 ║ ║
	║ ║																															 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		GETTERS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	/**
	 * 
	 * @return
	 */
	public int getBoxVolume()
	{
		if (max == null) return 0;
		
		return is3D ?
				(max[0] - min[0]) * (max[1] - min[1]) * (max[2] - min[2]) :
				(max[0] - min[0]) * (max[1] - min[1]);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getVolume()
	{
		return blocks.cardinality();
	}
	
	
	
	/*-------------------------------------
		OVERLOADS : getIndex()
	-------------------------------------*/
	/**
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getIndex(int x, int z)
	{
		return getIndex(x, z, min[0], min[1]);
	}
	
	/**
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getIndex(int x, int z, int y)
	{
		return getIndex(x, z, y, min[0], min[1], min[2]);
	}
	
	/**
	 * 
	 * @param coordinate
	 * @param blocks
	 * @param minX
	 * @param maxX
	 * @param minZ
	 * @param maxZ
	 * @return
	 */
	public static int getIndex(int x, int z, int minX, int minZ)
	{
		return (x - minX) + (z - minZ);
	}
	
	/**
	 * 
	 * @param coordinate
	 * @param blocks
	 * @param minX
	 * @param maxX
	 * @param minZ
	 * @param maxZ
	 * @param minY
	 * @param maxY
	 * @return
	 */
	public static int getIndex(int x, int z, int y, int minX, int minZ, int minY)
	{
		return (x - minX) + (z - minZ) + (y - minY);
	}
	
	
	
	/*-------------------------------------
		OVERLOADS : getCoords()
	-------------------------------------*/
	/**
	 * 
	 * @param coordinate
	 * @return
	 */
	public int getCoords(int index)
	{
		return is3D ?
				getIndex(index, min[0], min[1], min[2]) :
				getIndex(index, min[0], min[1]);
	}
	
	/**
	 * 
	 * @param coordinate
	 * @param blocks
	 * @param minX
	 * @param maxX
	 * @param minZ
	 * @param maxZ
	 * @return
	 */
	public static int getCoords(int index, int minX, int minZ)
	{
		return (x - minX) + (z - minZ);
	}
	
	/**
	 * 
	 * @param coordinate
	 * @param blocks
	 * @param minX
	 * @param maxX
	 * @param minZ
	 * @param maxZ
	 * @param minY
	 * @param maxY
	 * @return
	 */
	public static int getCoords(int index, int minX, int minZ, int minY)
	{
		return (x - minX) + (z - minZ) + (y - minY);
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		fill() and clear()
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	/*-------------------------------------
		OVERLOADS : fill()
	-------------------------------------*/
	/**
	 * 
	 */
	public void fill()
	{
		blocks.set(0, getBoxVolume() - 1);
	}
	
	/**
	 * 
	 * 
	 * @param min
	 * @param max
	 */
	public void fill(int[] min, int[] max)
	{
		
	}
	
	/**
	 * 
	 * 
	 * @param min
	 * @param max
	 * @param blocks
	 */
	public void fill(int minX, int minZ, int maxX, int maxZ, BitSet blocksToFill)
	{
		BitSet row;
	}
	
	/**
	 * 
	 * 
	 * @param min
	 * @param max
	 * @param blocks
	 */
	public void fill(int minX, int minZ, int minY, int maxX, int maxZ, int maxY, BitSet blocksToFill)
	{
		BitSet row;
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
	 * 
	 * @param min
	 * @param max
	 */
	public void clear(int[] min, int[] max)
	{
		
	}
	
	/**
	 * 
	 * 
	 * @param min
	 * @param max
	 * @param blocks
	 */
	public void clear(int minX, int minZ, int maxX, int maxZ, BitSet blocksToClear)
	{
		BitSet row;
	}
	
	/**
	 * 
	 * 
	 * @param min
	 * @param max
	 * @param blocks
	 */
	public void clear(int minX, int minZ, int minY, int maxX, int maxZ, int maxY, BitSet blocksToClear)
	{
		BitSet row;
		int index = 0;
		
		for (int y = minY; y <= maxY; y++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				
				for (int x = minX; x <= maxX; x++)
				{
					
				}
			}
		}
	}
}
