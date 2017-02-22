package regions;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

/**
 * A superclass for octrees and quadtrees. Concept credit to Don Meagher, who first named and 
 * described Octrees in his 1980 paper, "Octree Encoding: A New Technique for the Representation, 
 * Manipulation and Display of Arbitrary 3-D Objects by Computer."<p>
 * 
 * Octree application can be visualized like this:<p>
 * 
 * For any arbitrary 3-D object, find the bounding cube of the shape (or the smallest cube it will
 * fit inside of). Divide this bounding cube into 8 sub-cubes, or octants.<p>
 * 
 * With the space thus divided, evaluate which octants the object fills, which it avoids, and which
 * it intersects. Divide intersected octants again. Continue dividing until no partial octants remain,
 * or until the desired resolution is reached.<p>
 * 
 * Useful for testing whether an arbitrary 3-D object contains any given point.
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
		public Node(Node... nodes)
		{
			this.full 		= false;
			this.children 	= nodes;
		}
		
		
		/**
		 * Returns an array containing the given number of empty, childless nodes
		 * 
		 * @param length	desired size of array
		 */
		public static Node[] emptyNodeArray(int length)
		{
			Node[] array = new Node[length];
			
			for (int i = 0; i < length; i++)
			{
				array[i] = new Node(false);
			}
			return array;
		}
	}
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		BYTE CONVERSION																		 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		FROM BYTES
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * Reads each byte as four 2-bit values, where 00 = partial, 01 = empty (<tt>false</tt>)
	 * and 10 = full (<tt>true</tt>). Octree nodes use 2 bytes each, Quadtree nodes use 1 byte each. 
	 * Assumes a depth-first arrangement.<p>
	 * 
	 * Does not close the input stream.
	 * 
	 * @param input 		DataInputStream of source bytes
	 * @param parentBits 	the 2-bit value (00, 01, or 10)
	 * @return 				a new Node
	 * @throws 				IOException 
	 */
	public abstract Node parseBytes(DataInputStream input, int parentBits) throws IOException;
	
	
	/**
	 * Reads each byte as four 2-bit values, where 00 = partial, 01 = empty (<tt>false</tt>)
	 * and 10 = full (<tt>true</tt>). Octree nodes use 2 bytes each, Quadtree nodes use 1 byte each. 
	 * Assumes a depth-first arrangement.<p>
	 * 
	 * Does not close the input stream.
	 * 
	 * @param input 		DataInputStream of source bytes
	 * @return 				a new Node
	 * @throws 				IOException 	
	 */
	public Node parseBytes(DataInputStream input) throws IOException
	{
		return parseBytes(input, 0);
	}
	
	
	/**
	 * Reads each byte as four 2-bit values, where 00 = partial, 01 = empty (<tt>false</tt>)
	 * and 10 = full (<tt>true</tt>). Octree nodes use 2 bytes each, Quadtree nodes use 1 byte each. 
	 * Assumes a depth-first arrangement.<p>
	 * 
	 * @param file			binary file to read bytes from
	 * @return 				a new Node
	 * @throws 				IOException 	
	 */
	public Node parseBytes(File file) throws IOException
	{
		if (file.length() == 0) return new Node(false);
	
		DataInputStream input = new DataInputStream( new BufferedInputStream( new FileInputStream(file) ));
		Node node = parseBytes(input);
		input.close();
		return node;
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		TO BYTES
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
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
	 * Appends to the OutputStream the result of invoking <tt>{@link #getByte(Node, Node, Node, Node) 
	 * getByte(children)}</tt> for each node, skipping childless nodes. Assumes an OutputStream that 
	 * appends with each write. Traverses depth-first.
	 * 
	 * @param node 			the node to be parsed
	 * @param output		the OutputStream to write to
	 * @throws				IOException
	 */
	public abstract void writeBytes(Node node, OutputStream output) throws IOException;
	
	
	
	/*-------------------------------------
		OVERLOADS : saveToFile()
	-------------------------------------*/
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the root node, 
	 * using a FileOutputStream of the source file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 * @throws 				IOException
	 */
	public void saveToFile() throws IOException
	{
		FileOutputStream output = new FileOutputStream (new File(file.getAbsolutePath()), true);
		writeBytes(root, output);
		output.close();
	}
	
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the given node, 
	 * using a FileOutputStream of the source file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 * @throws 				IOException
	 */
	public void saveToFile(Node node) throws IOException
	{
		FileOutputStream output = new FileOutputStream (new File(file.getAbsolutePath()), true);
		writeBytes(node, output);
		output.close();
	}
	
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the root node, 
	 * using a FileOutputStream of the given file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 * @throws 				IOException
	 */
	public void saveToFile(File destination) throws IOException
	{
		FileOutputStream output = new FileOutputStream (new File(destination.getAbsolutePath()), true);
		writeBytes(root, output);
		output.close();
	}
	
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the given node, 
	 * using a FileOutputStream of the given file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 * @throws 				IOException
	 */
	public void saveToFile(Node node, File destination) throws IOException
	{
		FileOutputStream output = new FileOutputStream (new File(destination.getAbsolutePath()), true);
		writeBytes(node, output);
		output.close();
	}
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		CONSTRUCTORS																		 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
	protected int[]	min;			public int[] getMin()	  { return min;		}
	protected int[]	max;			public int[] getMax()	  { return max;		}
	protected int[]	minTrue;		public int[] getMinTrue() { return minTrue;	}
	protected int[]	maxTrue;		public int[] getMaxTrue() { return maxTrue;	}
	
	public final File file;
	public final Node root;
	
	/**
	 * Create a Tree from the given binary file. Invokes {@link #parseBytes(File)}
	 * 
	 * @param file 			The source file, and save destination, for this Tree.
	 * @throws 				IOException
	 */
	public Tree(File file) throws IOException
	{
		setBoundsFromFilename(file);
		
		this.file	= file;
		this.root	= parseBytes(file);
	}
	
	/**
	 * 
	 * 
	 * @param file			The source file, and save destination, for this Tree
	 * @param blocks		BitSet representing all <tt>true</tt> points in the given volume
	 * @param bounds		Min and max coordinates of the bounding box
	 */
	public Tree(File file, BitSet blocks, int[][] bounds)
	{
		this.file 	= file;
		this.root 	= null;
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		Methods used by constructors
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * Set bounds of this tree from values specified
	 * in the filename of the given source file.
	 * 
	 * @param file 			the source file to examine
	 */
	private void setBoundsFromFilename(File file)
	{
		//TODO finish setBoundsFromFilename() method
	}
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		CALCULATIONS																		 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		STATIC CALCULATIONS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	/*-------------------------------------
		OVERLOADS : nextPowerOfTwo()
	-------------------------------------*/
	/**
	 * 
	 * @param a
	 * @return
	 */
	public static int nextPowerOfTwo(int a)
	{
		return java.lang.Integer.highestOneBit(a) << 1;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int nextPowerOfTwo(int a, int b)
	{
		return java.lang.Integer.highestOneBit(Math.max(a, b)) << 1;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static int nextPowerOfTwo(int a, int b, int c)
	{
		return java.lang.Integer.highestOneBit(Math.max(Math.max(a, b), c)) << 1;
	}
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		EVALUATE
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * @param coords
	 * @return
	 */
	public abstract boolean contains(int... coords);
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		ADD VOLUME
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * @param coordinates
	 */
	protected abstract void expandAsNeeded(int...coords);
	
	
	/**
	 * 
	 * @param bounds
	 */
	protected abstract void expandAsNeeded(int[]...bounds);
	
	
	/**
	 * 
	 * @param coordinates
	 * @return
	 */
	public abstract void add(int...coords);
	
	
	/**
	 * 
	 * @param bounds
	 * @return
	 */
	public abstract void add(int[]...bounds);
	
	
	/**
	 * 
	 * @param blocks
	 * @param bounds
	 * @return
	 */
	public abstract void add(BitSet blocks, int[]...bounds);
	
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		REMOVE VOLUME
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * 
	 */
	public abstract void trimAsNeeded();
	
	
	/**
	 * 
	 * @param coordinates
	 * @return
	 */
	public abstract void remove(int...coords);
	
	
	/**
	 * 
	 * @param bounds
	 * @return
	 */
	public abstract void remove(int[]...bounds);
	
	
	/**
	 * 
	 * @param blocks
	 * @param bounds
	 * @return
	 */
	public abstract void remove(BitSet blocks, int[]...bounds);
}
