package regions;

import java.util.List;

public class Owner
{
	public final String 		name;
	public final Owner			parent;
	public final List<Owner> 	children;
	public final List<Tree> 	trees;
	
	public Owner(String name, Owner parent, List<Owner> children, List<Tree> trees)
	{
		this.name 		= name;
		this.parent		= parent;
		this.children 	= children;
		this.trees 		= trees;
	}
}
