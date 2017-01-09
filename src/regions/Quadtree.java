package regions;

public class Quadtree extends Tree
{
	//LOADING AND SAVING
	//================================================================== 
	
	Node parseBytes(IntReference index, byte[] bytes, int parentByte)
	{
		if (parentByte == 0b00000010) return new Node(true);
		if (parentByte == 0b00000001) return new Node(false);
		
		byte a = bytes[index.ref++];
		
		return 
				new Node
				(
						new Node[]
						{
							parseBytes(index, bytes, (a >>> 6 & 3)),
							parseBytes(index, bytes, (a >>> 4 & 3)),
							parseBytes(index, bytes, (a >>> 2 & 3)),
							parseBytes(index, bytes, (a 	  & 3))
						});
	}
	
	//CONSTRUCTOR
	//==================================================================
	
	public Quadtree(Owner owner, byte[] bytes)
	{
		super(owner, bytes);
	}
}
