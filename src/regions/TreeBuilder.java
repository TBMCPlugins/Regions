package regions;

import java.util.BitSet;

public class TreeBuilder<T extends Tree> extends BitRegion
{
	public TreeBuilder(int minX, int minZ)
	{
		super(minX, minZ);
	}
	
	public TreeBuilder(int minX, int minZ, int minY)
	{
		super(minX, minZ, minY);
	}
}
