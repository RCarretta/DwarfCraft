package com.smartaleq.bukkit.dwarfcraft;

import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.Chunk;

public class DCWorldListener extends WorldListener {
	private final DwarfCraft plugin;

	protected DCWorldListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onChunkUnloaded(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		event.setCancelled(plugin.getDataManager().checkTrainersInChunk(chunk));
	}
}