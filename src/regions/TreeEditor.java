package regions;

/**
 * 
 * 
 * @author Kevin Mathewson
 *
 */
public abstract class TreeEditor<T extends TreeEditor.Edit> 
{
	/**
	 * Abstract class describing an edit to a Tree object. In general, defines a selection 
	 * of blocks, specifying <tt>true</tt>, <tt>false</tt>, or <tt>no change</tt> for each.
	 * Dimensions are square or cubic, for quadtrees and octrees respectively.
	 */
	static abstract class Edit
	{
		/** 
		 * Minimum and maximum bounds [ [min] [max] ]. 
		 * Quadtree edits have bounds [x,z], Octree edits have bounds [x,z,y].
		 */
		private final int[][] bounds;
		private		  int[][] boundsTrue;
		private		  boolean containsTrue = false;
		
		Edit(int[][] bounds)
		{
			this.bounds = bounds;
			this.boundsTrue = this.bounds;
		}
	}
	
	/**
	 * Aggregator for all edits waiting to be added. Merges all incoming edits
	 * into a growing 'super-edit' that is periodically written to the tree and flushed.
	 */
	static class EditQueue<T extends Edit>
	{
		private final TreeEditor editor;
		private volatile Edit superEdit;
		
		EditQueue(TreeEditor editor, Edit superEdit)
		{
			this.editor 	= editor;
			this.superEdit 	= superEdit;
		}
		
		public synchronized T merge(T edit1, T edit2)
		{
			return null;
		}
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		CONSTRUCTOR
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	private final EditQueue changeQueue;
	
	public TreeEditor(EditQueue changeQueue)
	{
		this.changeQueue = changeQueue;
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		METHODS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	abstract void addEdit(Edit edit);
}
