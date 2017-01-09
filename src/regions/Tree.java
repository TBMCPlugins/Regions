package regions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
 * Next, you divide this cube evenly into 8 more cubes, determining which of these cubes
 * your shape fills, which it leaves empty, and which it intersects. Intersected cubes are 
 * divided again into 8 more cubes, and the process repeats until only completely full 
 * (<tt>true</tt>) and completely empty (<tt>false</tt>) cubes remain.<p>
 * 
 * With this tree, you can quickly determine whether any arbitrary point is inside or outside
 * your shape by navigating the tree to reach the smallest cube containing your point.
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
	
	
	
	/*----------------------------------------------------
	------------------------------------------------------
		FROM BYTES
	------------------------------------------------------
	----------------------------------------------------*/
	
	
	/**
	 * Defines an object containing a single int field. Used by the nested, recursive method 
	 * <tt>Node parseBytes()</tt>, so all the child-calls and parent-calls of the method can
	 * refer to the same integer, representing the current index within the parsed byte array.
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
	
	
	
	/*----------------------------------------------------
	------------------------------------------------------
		TO BYTES
	------------------------------------------------------
	----------------------------------------------------*/
	
	
	/**
	 * Get the 2-bit representation of this node. Used in <tt>{@link #getByte(Node node)}</tt> 
	 * to construct the parent byte from the bits of four child nodes, xx xx xx xx.<p>
	 * 
	 * @param node 			the node to evaluate
	 * @return				2 (full), 1 (empty), or 0 (has children)
	 */
	public static byte getBits(Node node)
	{
		return 	node.full ?	(byte) 2 : node.children.length == 0 ? 	(byte) 1 : (byte) 0;
	}
	
	
	/**
	 * Get byte representation for four child nodes. Returns the following:<p>
	 * 
	 * <tt>	{@link #getBits(Node node) getBits(a)} << 6 | 	</tt><p>
	 * <tt>	{@link #getBits(Node node) getBits(b)} << 4 | 	</tt><p>
	 * <tt>	{@link #getBits(Node node) getBits(c)} << 2 | 	</tt><p>
	 * <tt>	{@link #getBits(Node node) getBits(d)}			</tt>
	 * 
	 * @param a 			1st child node
	 * @param b 			2nd child node
	 * @param c 			3rd child node
	 * @param d 			4th child node
	 * @see 				{@link #getBits(Node node)}
	 */
	public static byte getByte(Node a, Node b, Node c, Node d)
	{
		return (byte) (	getBits(a) << 6 | 
						getBits(b) << 4 | 
						getBits(c) << 2 | 
						getBits(d)			
						);
	}
	
	
	/**
	 * An abstract method, implemented slightly differently by octrees and quadtrees because 
	 * octrees have 8 branches per node and quadtrees only 4.<p>
	 * 
	 * Parses the tree rooted at this node, appending in depth-first order the result of invoking
	 * <tt>{@link #getByte(Node, Node, Node, Node) getByte(children)}</tt> for each encountered 
	 * node in the tree, skipping childless nodes.<p>
	 * 
	 * NOTE: assumes an OutputStream that appends with each write.
	 * 
	 * @param node 			the node to be parsed
	 * @return 				a byte array representing the node and all its child nodes
	 */
	public abstract void writeBytes(Node node, OutputStream output);
	
	
	/**
	 * Parses the tree rooted at this node, appending in depth-first order the result of invoking
	 * <tt>{@link #getByte(Node, Node, Node, Node) getByte(children)}</tt> for each encountered 
	 * node in the tree, skipping childless nodes.<p>
	 * 
	 * Writes to a ByteArrayOutputStream.
	 * 
	 * @param node 			the root node of the tree to be parsed
	 * @return 				a byte array representing the root node and all its child nodes
	 */
	public byte[] getBytes(Node node)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writeBytes(node, output);
		return output.toByteArray();
	}
	
	
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt>, using a FileOutputStream of the
	 * given file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 */
	public void saveToFile(Node node, File destination)
	{
		try 
		{
			FileOutputStream output = new FileOutputStream (destination, true);
			writeBytes(node, output);
			output.close();
		} 
		catch (FileNotFoundException e) { e.printStackTrace(); } 
		catch (IOException e) 			{ e.printStackTrace(); }
	}
	
	
	
	/*----------------------------------------------------
	------------------------------------------------------
		CONSTRUCTOR
	------------------------------------------------------
	----------------------------------------------------*/
	
	
	public final Owner 	owner;
	public final Node 	root;
	
	public Tree(Owner owner, byte[] bytes)
	{
		this.owner 	= owner;
		this.root 	= parseBytes(bytes);
	}
}
