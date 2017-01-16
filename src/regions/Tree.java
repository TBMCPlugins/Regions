package regions;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																															 ║ ║
	║ ║		BYTE CONVERSION																										 ║ ║
	║ ║																															 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
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
	 */
	public Node parseBytes(DataInputStream input)
	{
		try 					{ return parseBytes(input, 0); 	} 
		catch (IOException e) 	{ return new Node(false);		}
	}
	
	
	/**
	 * Reads each byte as four 2-bit values, where 00 = partial, 01 = empty (<tt>false</tt>)
	 * and 10 = full (<tt>true</tt>). Octree nodes use 2 bytes each, Quadtree nodes use 1 byte each. 
	 * Assumes a depth-first arrangement.<p>
	 * 
	 * @param file			binary file to read bytes from
	 * @return 				a new Node
	 */
	public Node parseBytes(File file)
	{
		if (file.length() == 0) 
		{
			return new Node(false);
		}
		try 
		{
			DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			
			Node node = parseBytes(input);
			
			input.close();
			return node;
		} 
		catch (IOException e) 
		{ 
			return new Node(false);
		}
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
	
	
	
	/*-------------------------------------
		OVERLOADS : getBytes()
	-------------------------------------*/
	/**
	 * Parses the tree from the root, appending in depth-first order the result of invoking
	 * <tt>{@link #getByte(Node, Node, Node, Node) getByte(children)}</tt> for each encountered 
	 * node in the tree, skipping childless nodes.<p>
	 * 
	 * @return 				byte array representing the root node and all its child nodes
	 */
	public byte[] getBytes()
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writeBytes(root, output);
		return output.toByteArray();
	}
	
	/**
	 * Parses the tree below this node, appending in depth-first order the result of invoking
	 * <tt>{@link #getByte(Node, Node, Node, Node) getByte(children)}</tt> for each encountered 
	 * node in the tree, skipping childless nodes.<p>
	 * 
	 * @param node 			the node to parse from
	 * @return 				byte array representing the given node and all its child nodes
	 */
	public byte[] getBytes(Node node)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writeBytes(node, output);
		return output.toByteArray();
	}
	
	/**
	 * Parses the tree from the root, appending in depth-first order the result of invoking
	 * <tt>{@link #getByte(Node, Node, Node, Node) getByte(children)}</tt> for each encountered 
	 * node in the tree, skipping childless nodes.<p>
	 * 
	 * Writes to the given OutputStream, does not close the stream.
	 * 
	 * @param output 		the ByteArrayOutputStream to write to
	 */
	public void getBytes(OutputStream output)
	{
		writeBytes(root, output);
	}
	
	/**
	 * Parses the tree below this node, appending in depth-first order the result of invoking
	 * <tt>{@link #getByte(Node, Node, Node, Node) getByte(children)}</tt> for each encountered 
	 * node in the tree, skipping childless nodes.<p>
	 * 
	 * Writes to the given OutputStream, does not close the stream.
	 * 
	 * @param node 			the node to parse from
	 * @param output 		the ByteArrayOutputStream to write to
	 */
	public void getBytes(Node node, OutputStream output)
	{
		writeBytes(node, output);
	}
	
	
	
	/*-------------------------------------
		OVERLOADS : saveToFile()
	-------------------------------------*/
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the root node, using a 
	 * FileOutputStream of the source file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 */
	public void saveToFile()//TODO erase existing file contents before writing
	{
		try 
		{
			FileOutputStream output = new FileOutputStream (file, true);
			writeBytes(root, output);
			output.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the given node, using a 
	 * FileOutputStream of the source file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 */
	public void saveToFile(Node node)//TODO erase existing file contents before writing
	{
		try 
		{
			FileOutputStream output = new FileOutputStream (file, true);
			writeBytes(node, output);
			output.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the root node, using a 
	 * FileOutputStream of the given file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 */
	public void saveToFile(File destination)//TODO erase existing file contents before writing
	{
		try 
		{
			FileOutputStream output = new FileOutputStream (destination, true);
			writeBytes(root, output);
			output.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * Performs <tt>{@link #writeBytes(Node, OutputStream)}</tt> from the given node, using a 
	 * FileOutputStream of the given file as the OutputStream argument.
	 * 
	 * @param node			the root node of the tree to be parsed
	 * @param destination	the file to save to
	 */
	public void saveToFile(Node node, File destination)//TODO erase existing file contents before writing
	{
		try 
		{
			FileOutputStream output = new FileOutputStream (destination, true);
			writeBytes(node, output);
			output.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																															 ║ ║
	║ ║		CONSTRUCTORS																										 ║ ║
	║ ║																															 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
	private 		int[]			min;					public int[] getMin()	  { return min;		}
	private			int[]			max;					public int[] getMax()	  { return max;		}
	private			int[]			minTrue;				public int[] getMinTrue() { return minTrue;	}
	private			int[]			maxTrue;				public int[] getMaxTrue() { return maxTrue;	}
	
	public	final	File 			file;
	public	final	Node 			root;
	public	final	TreeEditor<?> 	editor;
	
	/**
	 * Create a Tree from the given binary file. Invokes <tt>parseBytes()</tt>
	 * 
	 * @param file 			The source file, and save destination, for this Tree.
	 * @see					{@link #parseBytes(File)}
	 */
	public Tree(File file)
	{
		setBoundsFromFilename(file);
		
		this.file	= file;
		this.root	= parseBytes(file);
		this.editor	= newEditor();
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
	private static void setBoundsFromFilename(File file)
	{
		//TODO finish setBoundsFromFilename() method
	}
	
	
	/**
	 * Abstract method, returns a new object 
	 * extending the abstract class TreeEditor
	 * 
	 * @return 				a new TreeEditor
	 */
	abstract TreeEditor<? extends TreeEditor.Edit> newEditor();
	
	
	
	
	
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																															 ║ ║
	║ ║		CALCULATIONS																										 ║ ║
	║ ║																															 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
	/**
	 * 
	 * @param parentNode	
	 * @param regionBounds	
	 * @return
	 */
	public abstract Node[] getNodes(Node parentNode, int[][] regionBounds);
	
	
	/**
	 * 
	 * @param regionBounds
	 * @return
	 */
	public Node[] getNodes(int[][] regionBounds)
	{
		return getNodes(root, regionBounds);
	}
}
