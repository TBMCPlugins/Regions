package regions;

/**
 * Aggregator for all edits waiting to be added. Merges all incoming edits
 * into a growing 'super-edit' that is periodically written to the tree and flushed.
 */
public class TreeEditor<T extends Tree>
{
	public TreeEditor(Octree tree, int minX, int minZ, int minY)
	{
		
	}
	
	public TreeEditor(Quadtree tree, int minX, int minZ)
	{
		
	}
}
