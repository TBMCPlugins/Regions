package regions;

public class Octree 
{
	//STATIC
	//================================================================== 
	
	public static class Node
	{
		public final boolean 	full;
		public final Node[] 	nodes;

		public Node(boolean full)
		{
			this.full 	= full;	
			this.nodes 	= new Node[0];
		}
		
		public Node(Node[] nodes)
		{
			this.full 	= false;
			this.nodes 	= nodes;
		}
		
	}
	
	
	
	private static class IntReference
	{
		int ref;
		
		IntReference(int i)
		{
			this.ref = i;
		}
	}
	
	
	
	private static Node parseBytes(IntReference index, byte[] bytes, int parentByte)
	{
		if (parentByte == 0b00000010) return new Node(true);
		if (parentByte == 0b00000001) return new Node(false);
		
		byte a = bytes[index.ref++], 	
			 b = bytes[index.ref++];
		
		return 
				new Node
				(
						new Node[]
						{
							parseBytes(index, bytes, (a >> 6 & 3)),
							parseBytes(index, bytes, (a >> 4 & 3)),
							parseBytes(index, bytes, (a >> 2 & 3)),
							parseBytes(index, bytes, (a 	 & 3)),
							
							parseBytes(index, bytes, (b >> 6 & 3)),
							parseBytes(index, bytes, (b >> 4 & 3)),
							parseBytes(index, bytes, (b >> 2 & 3)),
							parseBytes(index, bytes, (b 	 & 3))
						});
	}
	
	
	
	public static Node parseBytes(int startAtIndex, byte[] bytes)
	{
		return parseBytes	(	new IntReference(startAtIndex), 	bytes, 0	);
	}
	public static Node parseBytes(byte[] bytes)
	{
		return parseBytes	(	new IntReference(0), 				bytes, 0	);
	}
	
	//INSTANCE
	//==================================================================
	
	public final Owner 	owner;
	public final Node 	root;
	
	public Octree(Owner owner, byte[] bytes)
	{
		this.owner 	= owner;
		this.root 	= parseBytes(bytes);
	}
}
