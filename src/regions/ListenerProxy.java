package regions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * TODO
 */
public abstract class ListenerProxy<T extends Event> implements Listener
{
	/**
	 * Convenience class grouping a proxied Listener with its event-handling Method for easy invocation.
	 */
	public static class Proxy
	{
		public final Listener	listener;
		public final Method 	method;
		
		public Proxy(Listener listener, Method method)
		{
			this.listener 	= listener;
			this.method 	= method;
		}
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		CONSTRUCTORS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	protected final Class<?>			type;	
	
	protected final Directory 			directory;
	
	protected final LinkedList<Proxy>	lowest	= new LinkedList<Proxy>(),
										low		= new LinkedList<Proxy>(),
										normal	= new LinkedList<Proxy>(),
										high	= new LinkedList<Proxy>(),
										highest	= new LinkedList<Proxy>(),
										monitor	= new LinkedList<Proxy>();
	
	public ListenerProxy(Class<?> type, Directory directory)
	{
		this.type		= type;
		this.directory	= directory; 
	}
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		REGISTER LISTENERS
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	
	/**
	 * Register a Listener in the same way that Spigot registers listeners, except that ListenerProxy
	 * will only {@link #broadcastEvent(Event) broadcast} events occurring inside the specified region 
	 * Directory. Events are broadcast by invoking the EventHandlers of each proxied Listener, in order 
	 * of lowest to highest EventPriority (MONITOR being the highest).
	 * 
	 * @param listener		the Listener to register
	 * @see					{@link #broadcastEvent(Event)}
	 */
	public void registerListener(Listener listener)
	{
		for (Method method : listener.getClass().getDeclaredMethods())
		{
			if (method.getParameterCount() == 1 &&
				method.getParameterTypes()[0].equals(type) &&
				method.isAnnotationPresent(EventHandler.class))
			{
				switch (method.getAnnotation(EventHandler.class).priority())
				{
					case LOWEST  : lowest  .add(new Proxy(listener, method)); break;
					case LOW     : low     .add(new Proxy(listener, method)); break;
					case NORMAL  : normal  .add(new Proxy(listener, method)); break;
					case HIGH    : high    .add(new Proxy(listener, method)); break;
					case HIGHEST : highest .add(new Proxy(listener, method)); break;
					case MONITOR : monitor .add(new Proxy(listener, method)); break;
				}
			}
		}
	}
	
	
	/*----------------------------------------------------------------------------
	------------------------------------------------------------------------------
		EVENT HANDLING
	------------------------------------------------------------------------------
	----------------------------------------------------------------------------*/
	
	/**
	 * Extracts a coordinate position from an event, evaluates {@link #Directory.testFor()} for that 
	 * position, and, if true, invokes {@link #broadcastEvent(Event)}. Implemented differently for
	 * various event types.
	 * 
	 * @param event		the event to evaluate
	 * @throws			InvocationTargetException 
	 * @throws			IllegalArgumentException 
	 * @throws			IllegalAccessException 
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public abstract void listen(T event) throws IllegalAccessException, 
	   											IllegalArgumentException,
	   											InvocationTargetException;
	
	
	/**
	 * Invoke event-handling methods of all proxied Listeners for this event, in order of lowest to
	 * highest EventPriority (with MONITOR being the highest).
	 * 
	 * @param event		the parameter to pass
	 * @throws			InvocationTargetException 
	 * @throws			IllegalArgumentException 
	 * @throws			IllegalAccessException 
	 */
	public void broadcastEvent(T event) throws IllegalAccessException, 
											   IllegalArgumentException, 
											   InvocationTargetException
	{
		for (Proxy alias : lowest ) alias.method.invoke(alias.listener, alias.method);
		for (Proxy alias : low	  ) alias.method.invoke(alias.listener, alias.method);
		for (Proxy alias : normal ) alias.method.invoke(alias.listener, alias.method);
		for (Proxy alias : high	  ) alias.method.invoke(alias.listener, alias.method);
		for (Proxy alias : highest) alias.method.invoke(alias.listener, alias.method);
		for (Proxy alias : monitor) alias.method.invoke(alias.listener, alias.method);
	}
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		EVENT SUBCLASS IMPLEMENTATIONS														 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	
	/**
	 * TODO
	 *
	 * @param <T>		extends AsyncPlayerPreLoginEvent
	 */
	public static class For_AsyncPlayerPreLoginEvent<T extends AsyncPlayerPreLoginEvent> extends ListenerProxy<T>
	{
		public For_AsyncPlayerPreLoginEvent(Class<?> type, Directory directory){ super(type, directory); }
		
		/**
		 * TODO
		 * 
		 * @throws InvocationTargetException 
		 * @throws IllegalArgumentException 
		 * @throws IllegalAccessException 
		 */
		@Override
		public void listen(T event) throws IllegalAccessException, 
										   IllegalArgumentException, 
										   InvocationTargetException 
		{
			if (directory.testForOfflinePlayer(event.getUniqueId()))
				broadcastEvent(event);
		}
		
	}
	
	
	/**
	 * TODO
	 *
	 * @param <T>		extends BlockEvent
	 */
	public static class For_BlockEvent<T extends BlockEvent> extends ListenerProxy<T>
	{
		public For_BlockEvent(Class<?> type, Directory directory){ super(type, directory); }

		/**
		 * TODO
		 * 
		 * @throws InvocationTargetException 
		 * @throws IllegalArgumentException 
		 * @throws IllegalAccessException 
		 */
		@Override
		public void listen(T event) throws IllegalAccessException, 
										   IllegalArgumentException, 
										   InvocationTargetException 
		{
			Block block = event.getBlock();
			
			if (directory.testFor(block.getX(), block.getZ(), block.getY()))
				broadcastEvent(event);
		}	
	}
	

	/**
	 * TODO
	 *
	 * @param <T>		extends EntityEvent
	 */
	public static class For_EntityEvent<T extends EntityEvent> extends ListenerProxy<T>
	{
		public For_EntityEvent(Class<?> type, Directory directory){ super(type, directory); }

		/**
		 * TODO
		 */
		@Override
		public void listen(T event) 
		{
			if (directory.testForLocation(event.getEntity().getLocation()))
				broadcastEvent(event);
		}	
	}
	
	
	/**
	 * TODO
	 *
	 * @param <T>
	 */
	public static class For_HangingEvent<T extends HangingEvent> extends ListenerProxy<T>
	{
		public For_HangingEvent(Class<?> type, Directory directory){ super(type, directory); }

		/**
		 * TODO
		 */
		@Override
		public void listen(T event) 
		{
			
		}	
	}
	
	
	/**
	 * TODO
	 *
	 * @param <T>
	 */
	public static class For_InventoryEvent<T extends InventoryEvent> extends ListenerProxy<T>
	{
		public For_InventoryEvent(Class<?> type, Directory directory){ super(type, directory); }

		/**
		 * TODO
		 */
		@Override
		public void listen(T event) 
		{
			
		}	
	}
	// InventoryMoveItemEvent, 
	// InventoryPickupItemEvent, 
	// PlayerEvent, 
	// PlayerLeashEntityEvent, 
	// PlayerPreLoginEvent, 
	// ServerEvent, 
	// TabCompleteEvent, 
	// VehicleEvent, 
	// WeatherEvent, 
	// WorldEvent
	
}
