package regions;

import java.util.List;

public abstract class Directory 
{
					String 			name;
	public final	Directory		parent;
	public final	List<Directory>	children;
	public final	List<Tree> 		trees;
	
	public Directory(String name, Directory parent, List<Directory> children, List<Tree> trees)
	{
		this.name		= name;
		this.parent		= parent;
		this.children	= children;
		this.trees		= trees;
	}
}
