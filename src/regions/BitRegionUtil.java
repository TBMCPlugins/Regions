package regions;

import java.util.BitSet;

/**
 * 
 * @author Kevin Mathewson
 *
 */
public class BitRegionUtil 
{
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		2D REGION																			 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	public static class _2D
	{
		/*----------------------------------------------------------------------------
		------------------------------------------------------------------------------
			index()
		------------------------------------------------------------------------------
		----------------------------------------------------------------------------*/
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param maxX2
		 * @param maxZ2
		 * @return
		 */
		public static int index(int x, int z, int maxX2, int maxZ2)
		{
			if (x < 0 || x >= maxX2 || z < 0 || z >= maxZ2) return -1;
			
			return (z * maxX2) + x;
		}
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param minX2
		 * @param minZ2
		 * @param maxX2
		 * @param maxZ2
		 * @return
		 */
		public static int index(int x, int z, int minX2, int minZ2, int maxX2, int maxZ2)
		{
			if (x < minX2 || x >= maxX2 || z < minZ2 || z >= maxZ2) return -1;
			
			x 	  -= minX2;
			z 	  -= minZ2;
			maxX2 -= minX2;
			
			return (z * maxX2) + x;
		}
		
		
		/*----------------------------------------------------------------------------
		------------------------------------------------------------------------------
			contains()
		------------------------------------------------------------------------------
		----------------------------------------------------------------------------*/
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param maxX2
		 * @param maxZ2
		 * @param blocks
		 * @return
		 */
		public static boolean contains(int x, int z, int maxX2, int maxZ2, BitSet blocks)
		{
			int index = index(x, z, maxX2, maxZ2);
			
			return index == -1 ? false : blocks.get(index);
		}
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param minX2
		 * @param minZ2
		 * @param maxX2
		 * @param maxZ2
		 * @param blocks
		 * @return
		 */
		public static boolean contains(int x, int z, int minX2, int minZ2, int maxX2, int maxZ2, BitSet blocks)
		{
			int index = index(x, z, minX2, minZ2, maxX2, maxZ2);
			
			return index == -1 ? false : blocks.get(index);
		}
		
		
		/*----------------------------------------------------------------------------
		------------------------------------------------------------------------------
			EVALUATE
		------------------------------------------------------------------------------
		----------------------------------------------------------------------------*/
		
		
		/**
		 * 
		 * @param value
		 * @param minX1
		 * @param minZ1
		 * @param maxX1
		 * @param maxZ1
		 * @param maxX2
		 * @param maxZ2
		 * @param blocks
		 * @return
		 */
		public static boolean testFor(boolean value, 
									  int minX1, int minZ1, 
									  int maxX1, int maxZ1, 
									  int maxX2, int maxZ2, 
									  BitSet blocks
									  )
		{
			int z, x, index;
			for (z = minZ1; z < maxZ1; z++)
			{
				index = index(x = minX1, z, maxX2, maxZ2);
				for (; x < maxX1; x++) 
				{
					if (blocks.get(index++) == value) 
						return true;
				}
			}
			return false;
		}
		
		
		/**
		 * 
		 * @param minX1
		 * @param minZ1
		 * @param minY1
		 * @param maxX1
		 * @param maxZ1
		 * @param maxY1
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @param blocks
		 * @return
		 */
		public static int compareRegion(int minX1, int minZ1, 
										int maxX1, int maxZ1, 
										int maxX2, int maxZ2, 
										BitSet blocks
										)
		{
			boolean firstBlock = blocks.get(index(minX1, minZ1, maxX2, maxZ2));
			
			return testFor(!firstBlock, minX1, minZ1, maxX1, maxZ1, maxX2, maxZ2, blocks)
					
					? 0 : firstBlock ? 2 : 1;
		}
		
		
		/**
		 * 
		 * @param minX1
		 * @param minZ1
		 * @param maxX1
		 * @param maxZ1
		 * @param minX2
		 * @param minZ2
		 * @param maxX2
		 * @param maxZ2
		 * @param blocks
		 * @return
		 */
		public static int compareRegion(int minX1, int minZ1, int maxX1, int maxZ1,
										int minX2, int minZ2, int maxX2, int maxZ2,
										BitSet blocks
										)
		{
			minX1 -= minX2; minZ1 -= minZ2;
			maxX1 -= minX2; maxZ1 -= minZ2;
			maxX2 -= minX2; maxZ2 -= minZ2;
			
			return compareRegion(minX1, minZ1, maxX1, maxZ1, maxX2, maxZ2, blocks);
		}
	}
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		3D REGION																			 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	public static class _3D
	{
		/*----------------------------------------------------------------------------
		------------------------------------------------------------------------------
			index()
		------------------------------------------------------------------------------
		----------------------------------------------------------------------------*/
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param y
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @return
		 */
		public static int index(int x, int z, int y, int maxX2, int maxZ2, int maxY2)
		{
			if (x < 0 || x >= maxX2 || z < 0 || z >= maxZ2 || y < 0 || y >= maxY2) return -1;
			
			return (y * maxZ2 * maxX2) + (z * maxX2) + x;
		}
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param y
		 * @param minX2
		 * @param minZ2
		 * @param minY2
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @return
		 */
		public static int index(int x, int z, int y, int minX2, int minZ2, int minY2, int maxX2, int maxZ2, int maxY2)
		{
			if (x < minX2 || x >= maxX2 || z < minZ2 || z >= maxZ2 || y < minY2 || y >= maxY2) return -1;
			
			x 	  -= minX2;
			z 	  -= minZ2;
			y 	  -= minY2;
			maxX2 -= minX2;
			maxZ2 -= minZ2;
			
			return (y * maxZ2 * maxX2) + (z * maxX2) + x;
		}
		
		
		/*----------------------------------------------------------------------------
		------------------------------------------------------------------------------
			contains()
		------------------------------------------------------------------------------
		----------------------------------------------------------------------------*/
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param y
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @param blocks
		 * @return
		 */
		public static boolean contains(int x, int z, int y, int maxX2, int maxZ2, int maxY2, BitSet blocks)
		{
			int index = index(x, z, y, maxX2, maxZ2, maxY2);
			
			return index == -1 ? false : blocks.get(index);
		}
		
		
		/**
		 * 
		 * @param x
		 * @param z
		 * @param y
		 * @param minX2
		 * @param minZ2
		 * @param minY2
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @param blocks
		 * @return
		 */
		public static boolean contains(int x, int z, int y, int minX2, int minZ2, int minY2, int maxX2, int maxZ2, int maxY2, BitSet blocks)
		{
			int index = index(x, z, y, minX2, minZ2, minY2, maxX2, maxZ2, maxY2);
			
			return index == -1 ? false : blocks.get(index);
		}
		
		
		/*----------------------------------------------------------------------------
		------------------------------------------------------------------------------
			EVALUATE
		------------------------------------------------------------------------------
		----------------------------------------------------------------------------*/
		
		
		/**
		 * 
		 * @param value
		 * @param minX1
		 * @param minZ1
		 * @param minY1
		 * @param maxX1
		 * @param maxZ1
		 * @param maxY1
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @param blocks
		 * @return
		 */
		public static boolean testFor(boolean value,
									  int minX1, int minZ1, int minY1, 
									  int maxX1, int maxZ1, int maxY1,
									  int maxX2, int maxZ2, int maxY2,
									  BitSet blocks
									  )
		{
			int y, z, x, index;
			for (y = minY1; y < maxY1; y++) for (z = minZ1; z < maxZ1; z++)
			{
				index = index(x = minX1, z, y, maxX2, maxZ2, maxY2);
				for (; x < maxX1; x++) 
				{
					if (blocks.get(index++) == value) 
						return true;
				}
			}
			return false;
		}
		
		
		/**
		 * 
		 * @param minX1
		 * @param minZ1
		 * @param minY1
		 * @param maxX1
		 * @param maxZ1
		 * @param maxY1
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @param blocks
		 * @return
		 */
		public static int compareRegion(int minX1, int minZ1, int minY1, 
										int maxX1, int maxZ1, int maxY1,
										int maxX2, int maxZ2, int maxY2,
										BitSet blocks
										)
		{
			boolean firstBlock = blocks.get(index(minX1, minZ1, minY1, maxX2, maxZ2, maxY2));
			
			return testFor(!firstBlock, minX1, minZ1, minY1, maxX1, maxZ1, maxY1, maxX2, maxZ2, maxY2, blocks)
					
					? 0 : firstBlock ? 2 : 1;
		}
		
		
		/**
		 * 
		 * @param minX1
		 * @param minZ1
		 * @param minY1
		 * @param maxX1
		 * @param maxZ1
		 * @param maxY1
		 * @param minX2
		 * @param minZ2
		 * @param minY2
		 * @param maxX2
		 * @param maxZ2
		 * @param maxY2
		 * @param blocks
		 * @return
		 */
		public static int compareRegion(int minX1, int minZ1, int minY1, int maxX1, int maxZ1, int maxY1,
										int minX2, int minZ2, int minY2, int maxX2, int maxZ2, int maxY2,
										BitSet blocks
										)
		{
			minX1 -= minX2; minZ1 -= minZ2; minY1 -= minY2;
			maxX1 -= minX2; maxZ1 -= minZ2; maxY1 -= minY2;
			maxX2 -= minX2; maxZ2 -= minZ2; maxY2 -= minY2;
			
			return compareRegion(minX1, minZ1, minY1, maxX1, maxZ1, maxY1, maxX2, maxZ2, maxY2, blocks);
		}
	}
}
