package com.smartaleq.bukkit.dwarfcraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Util {
	
	static int randomAmount(double input){
		double rand = Math.random();
		if (rand>input%1) return (int) Math.floor(input);
		else return (int) Math.ceil(input);
	}
	
	// Stolen from nossr50
    private static int charLength(char x) {
    	if("i.:,;|!".indexOf(x) != -1)
    		return 2;
    	else if("l'".indexOf(x) != -1)
    		return 3;	
    	else if("tI[]".indexOf(x) != -1)	
    		return 4;
    	else if("fk{}<>\"*()".indexOf(x) != -1)
    		return 5;	
    	else if("abcdeghjmnopqrsuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ1234567890\\/#?$%-=_+&^".indexOf(x) != -1)
    		return 6;	
    	else if("@~".indexOf(x) != -1)	
    		return 7;	
    	else if(x==' ')	
    		return 4;	
    	else	
    		return -1;    
    }
    
    public static int msgLength(String str) {
    	int len = 0;
    	
    	for ( int i = 0; i < str.length(); i++ ) {
    		if (str.charAt(i) == '&'){
    			i++;
    			continue; // increment by 2 for colors, as in the case of "&3"
    		}
    		len += charLength(str.charAt(i));
    	}
    	return len;
    }
	
    /**
     * Drops blocks at a block based on a specific effect(and level)
     * @param block Block being destroyed
     * @param e Effect causing a block to drop
     * @param effectAmount Double number of blocks to drop
     * @param drop item naturally or not
     */
	public static void dropBlockEffect(Location loc, Effect e, double effectAmount, boolean dropNaturally) {
		ItemStack item = new ItemStack(e.outputId, Util.randomAmount(effectAmount));
		if (item.getAmount() == 0){
			if (DwarfCraft.debugMessagesThreshold < 6) System.out.println("Debug: dropped " + item.toString());
			return;
		}
		if(dropNaturally) loc.getWorld().dropItemNaturally(loc, item);
		else loc.getWorld().dropItem(loc, item);
		if (DwarfCraft.debugMessagesThreshold < 5) System.out.println("Debug: dropped " + item.toString());	
	}
	
	public static boolean toolChecker(Player player){
		Inventory inv = player.getInventory();
		boolean removedSomething = false;
		for (ItemStack item:inv.getContents()){
			int damage = item.getDurability();
			int maxDamage = item.getType().getMaxDurability();
			if (damage>maxDamage) {
				inv.removeItem(item);
				removedSomething = true;
			}
		} 
		return removedSomething;
	}

}
