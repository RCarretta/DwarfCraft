package com.smartaleq.bukkit.dwarfcraft;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

class DCPlayerListener extends PlayerListener {

    private final DwarfCraft plugin;

    protected DCPlayerListener(final DwarfCraft plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player interacts with the environment
     * @param event
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        /**
         * Called when a player uses an item Eating food will cause extra health or
         * less health to be gained
         */
        // General information
        Player player = event.getPlayer();
        Dwarf dwarf = plugin.getDataManager().find(player);
        List<Skill> skills = dwarf.getSkills();
        boolean hadEffect = false;
        // Effect Specific information
        ItemStack item = player.getItemInHand();
        int itemId = -1;
        if (item != null) {
            itemId = item.getTypeId();
        }
        //TODO BUG. cant break blocks with food.
        for (Skill s : skills) {
            if (s == null) {
                continue;
            }
            for (Effect e : s.getEffects()) {
                if (e == null) {
                    continue;
                }
                if (e.getEffectType() == EffectType.EAT
                        && e.getInitiatorId() == itemId) {
                    if (player.getHealth() >= 20) {
                        event.setCancelled(true);
                        return;
                    }
                    if (DwarfCraft.debugMessagesThreshold < 8) {
                        System.out.println("DC8: ate food:"
                                + item.getType().toString() + " for "
                                + e.getEffectAmount(dwarf));
                    }
                    player.setHealth((int) (player.getHealth() + e.getEffectAmount(dwarf)));
                    player.getInventory().removeItem(item);
                    hadEffect = true;

                }
            }
        }

        if (hadEffect) {
            event.setCancelled(true);
        }
        /**
         * Called when a player right clicks a block, used for hoe-ing grass.
         */
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack tool = player.getItemInHand();
            int toolId = -1;
            short durability = 0;
            if (tool != null) {
                toolId = tool.getTypeId();
                durability = tool.getDurability();
            }
            Block block = event.getClickedBlock();
            Location loc = block.getLocation();
            Material material = event.getClickedBlock().getType();
            for (Skill s : skills) {
                if (s == null) {
                    continue;
                }
                for (Effect e : s.getEffects()) {
                    if (e == null) {
                        continue;
                    }
                    if (e.getEffectType() == EffectType.PLOWDURABILITY) {
                        for (int id : e.getTools()) {
                            if (id == toolId
                                    && (material == Material.DIRT || material == Material.GRASS)) {
                                double effectAmount = e.getEffectAmount(dwarf);
                                if (DwarfCraft.debugMessagesThreshold < 3) {
                                    System.out.println("DC2: affected durability of a hoe - old:"
                                            + durability);
                                }
                                tool.setDurability((short) (durability + Util.randomAmount(effectAmount)));
                                if (DwarfCraft.debugMessagesThreshold < 3) {
                                    System.out.println("DC3: affected durability of a hoe - new:"
                                            + tool.getDurability());
                                }
                                Util.toolChecker(player);
                                block.setTypeId(60);
                            }
                        }
                    }
                    if (e.getEffectType() == EffectType.PLOW) {
                        for (int id : e.getTools()) {
                            if (id == toolId && material == Material.GRASS) {
                                Util.dropBlockEffect(loc, e,
                                        e.getEffectAmount(dwarf), true, (byte) 0);
                                if (DwarfCraft.debugMessagesThreshold < 3) {
                                    System.out.println("DC3: hoed some ground:"
                                            + e.getEffectAmount(dwarf));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * When a player joins the server this initialized their data from the
     * database or creates new info for them.
     *
     * also broadcasts a welcome "player" message
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Dwarf dwarf = plugin.getDataManager().find(player);

        if (dwarf == null) {
            dwarf = plugin.getDataManager().createDwarf(player);
        }
        if (!plugin.getDataManager().getDwarfData(dwarf)) {
            plugin.getDataManager().createDwarfData(dwarf);
        }
        plugin.getOut().welcome(plugin.getServer(), dwarf);
    }
//	public void onPlayerQuit(PlayerQuitEvent event) {
//		Dwarf dwarf = plugin.getDataManager().find(event.getPlayer());
//		plugin.getDataManager().Remove(dwarf);
//	}
}
