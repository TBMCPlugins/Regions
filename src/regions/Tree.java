package regions;

public abstract class Tree 
{
	public static class Node
	{
		public boolean 	full;
		public Node		parent;
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
	
	
	
	static class IntReference
	{
		int ref;
		
		IntReference(int i)
		{
			this.ref = i;
		}
	}
	
	
	
	abstract Node parseBytes(IntReference index, byte[] bytes, int parentByte);
	
	
	
	public Node parseBytes(int startAtIndex, byte[] bytes)
	{
		return parseBytes	(	new IntReference(startAtIndex), 	bytes, 0	);
	}
	
	
	
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
