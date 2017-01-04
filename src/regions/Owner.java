package regions;

public class Owner
{
	public final String 	name;
	public final Owner		parent;
	public final Owner[] 	children;
	public final Octree[] 	trees;
	
	public Owner(String name, Owner parent, Owner[] children, Octree[] trees)
	{
		this.name 		= name;
		this.parent		= parent;
		this.children 	= children;
		this.trees 		= trees;
	}
}
