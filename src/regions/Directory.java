package regions;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;

import net.minecraft.server.v1_11_R1.WorldNBTStorage;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagDouble;
import net.minecraft.server.v1_11_R1.NBTTagList;


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
	
	/**
	 * 
	 * @param coords
	 * @return
	 */
	public boolean testFor(int... coords)
	{
		for (Tree tree : trees)
			if (tree.testFor(coords)) 
				return true;
		
		for (Directory child : children)
			if (child.testFor(coords)) 
				return true;
		
		return false;
	}
	
	/*
	╔══════════════════════════════════════════════════════════════════════════════════════════════╗
	║ ╔══════════════════════════════════════════════════════════════════════════════════════════╗ ║
	║ ║																							 ║ ║
	║ ║		MINECRAFT CONVENIENCE METHODS														 ║ ║
	║ ║																							 ║ ║
	║ ╚══════════════════════════════════════════════════════════════════════════════════════════╝ ║
	╚══════════════════════════════════════════════════════════════════════════════════════════════╝ */
	
	public static final WorldNBTStorage storage = 
			
			(WorldNBTStorage) ((CraftServer) Bukkit.getServer())
							  .getServer()
							  .worlds.get(0)
							  .getDataManager();
	
	
	/**
	 * {@link #testFor() TestFor()} the block position described in the player's .dat file under "Pos."
	 * Returns false for new players, and for players without stored position values.
	 * 
	 * @param uuid			the player's UUID string
	 * @return				whether or not any trees contain the player's position
	 */
	public boolean testForOfflinePlayer(UUID uuid)
	{
		NBTTagCompound nbt = storage.getPlayerData(uuid.toString());
		if (nbt == null) 
			return false;
		
		NBTTagList pos = nbt.getList("Pos", 6);
		if (pos.size() != 3) 
			return false;
		
		return testFor((int) ((NBTTagDouble) pos.h(0)).asDouble(), 
					   (int) ((NBTTagDouble) pos.h(2)).asDouble(), 
					   (int) ((NBTTagDouble) pos.h(1)).asDouble()
					   );
	}
	
	
	/**
	 * {@link #testFor() TestFor()} the coordinates of the given Location
	 * 
	 * @param location		the location to evaluate
	 * @return				whether or not any trees contain this location
	 */
	public boolean testForLocation(Location location)
	{
		return testFor(location.getBlockX(), location.getBlockZ(), location.getBlockY());
	}
}
