package com.smartaleq.bukkit.dwarfcraft;

import java.util.Iterator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.World;
import com.smartaleq.bukkit.dwarfcraft.ui.DCCommand;
import com.smartaleq.bukkit.dwarfcraft.ui.Out;

/**
 * 
 * DwarfCraft is a RPG-like plugin for minecraft (via Bukkit) that allows players to improve their characters.
 * Players(Dwarfs!) may pay materials to a trainer to improve a skill level, which will provide benefits
 * such as increased weapon damage, decreased tool durability drop, increased drops from blocks or mobs, etc.
 * 
 * Data used for this plugin comes from two places: 
 * On each load, a list of skills and effects is pulled from flatfiles.
 * Dwarf's skill levels and world training zones are kept in database (currently supports only sqlite)  
 * 
 * @author smartaleq
 * @author RCarretta
 * 
 */
public class DwarfCraft extends JavaPlugin {


private final DCBlockListener	blockListener 	= new DCBlockListener(this);
private final DCPlayerListener	playerListener	= new DCPlayerListener(this);
private final DCEntityListener	entityListener	= new DCEntityListener(this);
private final DCVehicleListener	vehicleListener	= new DCVehicleListener(this);
private final DCWorldListener 	worldListener 	= new DCWorldListener(this);
private final DCCraftListener	craftListener	= new DCCraftListener(this);
private ConfigManager cm;
private DataManager dm; 
private Out out;
public static int debugMessagesThreshold = 0;
public static boolean disableEffects = false;

	/**	
	 * Called upon enabling the plugin
	 */
	public void onEnable() {
	    PluginManager pm = getServer().getPluginManager();
	    
	    pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Low, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Low, this);
		
		pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.High, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.High, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Low, this);
				
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Low, this);
		
		pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_EXIT, vehicleListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.Lowest, this);
		
		pm.registerEvent(Event.Type.CHUNK_UNLOADED, worldListener, Priority.Low, this);
		pm.registerEvent(Event.Type.WORLD_LOADED, worldListener, Priority.Low, this);
		
		pm.registerEvent(Event.Type.BLOCK_INTERACT, craftListener, Priority.Normal, this);
				
		cm = new ConfigManager(this, "./plugins/DwarfCraft/", "DwarfCraft.config");
		dm = new DataManager(this, cm);
		out = new Out(this);
		
		cm.readConfigFile();
		if(!cm.readSkillsFile() || !cm.readEffectsFile() || !cm.readMessagesFile() || !cm.readGreeterMessagesfile()){
			System.out.println("[SEVERE] Failed to Enable DwarfCraft Skills and Effects)");
			pm.disablePlugin(this); //TODO failed to init skills
		
		}
		dm.dbInitialize();
		
		for ( Iterator<World> i = getServer().getWorlds().iterator(); i.hasNext(); ) {
			World w = i.next();
			dm.populateTrainers(w);
		}	
				
	    PluginDescriptionFile pdfFile = this.getDescription();
	    System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}
		

	/**
	 * Called upon disabling the plugin.
	 */
	public void onDisable() {
		/*
		 * close dbs
		 */
	}
	
	// TODO: make these protected
	public Out getOut() { return out; }
	public DataManager getDataManager() { return dm; }
	public ConfigManager getConfigManager() { return cm; }
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		DCCommand cmd = new DCCommand(this, command.getName());
		return cmd.execute(sender, commandLabel, args);
	}
	
	public Player getPlayer(String playerName){
		Player[] players = this.getServer().getOnlinePlayers();
        for (Player player : players) {
            if (player.getName().equalsIgnoreCase(playerName)) return player;
        }
        return null;
	}	

}
