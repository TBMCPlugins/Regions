package regions;

/**
 * 
 * 
 * @author Kevin Mathewson
 *
 */
public interface TreeEditor 
{
	public abstract class ChangeQueue<T extends Edit>{}
	
	public abstract class Edit{}
	
	public void edit(Edit edit);
}
