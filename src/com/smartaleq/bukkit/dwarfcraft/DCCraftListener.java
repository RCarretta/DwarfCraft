package com.smartaleq.bukkit.dwarfcraft;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

class DCCraftListener extends PlayerListener {

    private final DwarfCraft plugin;

    protected DCCraftListener(DwarfCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock().getType() == Material.WORKBENCH) {
            // schedule a *syncronous* task
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin,
                    new DCCraftSchedule(plugin, plugin.getDataManager().find(event.getPlayer())), 5);    //TODO not sure if/how this will work
        }
    }
}
