package regions;

import org.bukkit.event.Event;

public class RegionEvent<T extends Event> 
{
	public final T 		bukkitEvent;
	public final Owner 	region;
	
	public RegionEvent(T bukkitEvent, Owner region)
	{
		this.bukkitEvent 	= bukkitEvent;
		this.region 		= region;
	}
}
