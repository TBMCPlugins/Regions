package regions;

/**
 * This is a superclass for octrees, quadtrees, and any other spatial trees.<p>
 * 
 * concept credit to Don Meagher, who first named and described Octrees in his 1980 paper,
 * "Octree Encoding: A New Technique for the Representation, Manipulation and Display of 
 * Arbitrary 3-D Objects by Computer."<p>
 * 
 * The idea works like this:<p>
 * 
 * For some arbitrary shape, you first define the bounding box of the shape (or the 
 * smallest box your shape will fit inside of). This box outlines the minimum and maximum 
 * x, y, and z dimensions of the shape.<p>
 * 
 * Next, you divide this cube evenly into 8 sections (2 x 2 x 2) and determine which sections 
 * your shape fills, which ones it leaves empty, and which ones your shape partially 
 * intersects.<p>
 * 
 * Intersected cubes are divided again into 8 subsections, and the process repeats until you 
 * have divided and subdivided your bounding box into <i>only</i> full (<tt>true</tt>) and
 * empty (<tt>false</tt>) cubes<p>
 * 
 * Then, given any point in space, you can quickly determine whether the point is inside or 
 * outside your shape by navigating the tree to reach the smallest cube your point is in.
 * 
 * @author Kevin Mathewson
 *
 */
public abstract class Tree 
{
	/**
	 * Tree node containing a boolean and an array of sub-nodes
	 */
	public static class Node
	{
		public boolean 	full;
		public Node[] 	children;

		public Node(boolean full)
		{
			this.full 		= full;	
			this.children 	= new Node[0];
		}
		
		public Node(Node[] nodes)
		{
			this.full 		= false;
			this.children 	= nodes;
		}
		
	}
	
	
	/**
	 * Defines an object containing a single int field. Used by the <tt>parseBytes()</tt> 
	 * method to track its current <tt>index</tt> in the byte array. <tt>Node parseBytes()</tt> 
	 * is a nested, self-calling method, and <tt>index</tt> increments with each call.
	 */
	static final class IntReference
	{
		int ref;
		
		IntReference(int i)
		{
			this.ref = i;
		}
	}
	
	
	/**
	 * An abstract method, implemented slightly differently by octrees and quadtrees because 
	 * octrees have 8 branches per node and quadtrees only 4.<p>
	 * 
	 * Given a byte array, interprets each byte as four 2-bit values, where 00 = partially full,
	 * 01 = empty (<tt>false</tt>) and 10 = full (<tt>true</tt>). Octree nodes use 2 bytes 
	 * each, Quadtree nodes use 1 byte each. Assumes a depth-first arrangement.<p>
	 * 
	 * @param index			current position in the byte array
	 * @param bytes 		byte array, presumably read from a binary file
	 * @param parentByte 	the 2-bit value (00000010, 00000001 or 00000000)
	 * @return 				a new Node
	 */
	abstract Node parseBytes(IntReference index, byte[] bytes, int parentByte);
	
	
	/**
	 * A simplified public alias for the package-private abstract method 
	 * <tt>Node parseBytes(IntReference index, byte[] bytes, int parentByte)</tt><p>
	 * 
	 * Given a byte array, interprets each byte as four 2-bit values, where 00 = partially full,
	 * 01 = empty (<tt>false</tt>) and 10 = full (<tt>true</tt>). Octree nodes use 2 bytes 
	 * each, Quadtree nodes use 1 byte each. Assumes a depth-first arrangement.<p>
	 * 
	 * @param startAtIndex	where in the byte[] array to begin
	 * @param bytes			byte array, presumably read from a binary file
	 * @return 				a new Node
	 */
	public Node parseBytes(int startAtIndex, byte[] bytes)
	{
		return parseBytes	(	new IntReference(startAtIndex), 	bytes, 0	);
	}
	
	
	/**
	 * A simplified public alias for the package-private abstract method 
	 * <tt>Node parseBytes(IntReference index, byte[] bytes, int parentByte)</tt><p>
	 * 
	 * Given a byte array, interprets each byte as four 2-bit values, where 00 = partially full,
	 * 01 = empty (<tt>false</tt>) and 10 = full (<tt>true</tt>). Octree nodes use 2 bytes 
	 * each, Quadtree nodes use 1 byte each. Assumes a depth-first arrangement.<p>
	 * 
	 * @param bytes			byte array, presumably read from a binary file
	 * @return 				a new Node
	 */
	public Node parseBytes(byte[] bytes)
	{
		return parseBytes	(	new IntReference(0), 				bytes, 0	);
	}
	
	
	
	public final Owner 	owner;
	public final Node 	root;
	
	public Tree(Owner owner, byte[] bytes)
	{
		this.owner 	= owner;
		this.root 	= parseBytes(bytes);
	}
}
