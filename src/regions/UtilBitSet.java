package regions;

import java.util.BitSet;

public interface UtilBitSet 
{
	/*-------------------------------------
		OVERLOADS : shift()
	-------------------------------------*/
	/**
	 * 
	 * @param bitset
	 * @param shift
	 * @return
	 */
	default BitSet shift(BitSet bitset, int shift)
	{
		if (shift == 0) return bitset;
		if (shift >  0)
		{
			for (int i = bitset.length() - 1; i > -1; i--)
			{
				bitset.set(i + shift, bitset.get(i));
			}
			return bitset;
		}
		else
		{
			int length = bitset.length();
			for (int i = -shift; i < length; i++)
			{
				bitset.set(i + shift, bitset.get(i));
			}
			bitset.clear(length + shift, length);
			return bitset;
		}
	}
	
	/**
	 * 
	 * @param bitset
	 * @param shift
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	default BitSet shift(BitSet bitset, int shift, int fromIndex, int toIndex)
	{
		
	}
}
