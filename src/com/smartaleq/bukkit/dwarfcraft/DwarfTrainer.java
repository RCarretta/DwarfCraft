package com.smartaleq.bukkit.dwarfcraft;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.World;

import redecouverte.npcspawner.*;

final class DwarfTrainer {
	// protected static BasicHumanNpcList HumanNpcList;
	private BasicHumanNpc basicHumanNpc;
	private Integer skillId;
	private Integer maxSkill;
	private boolean greeter;
	private String messageId;
	private World world;
	private ItemStack itemStack;	
	private double x, y, z;
	private float yaw, pitch;
	private String basicNpcUniqueId, basicNpcName;
	private final DwarfCraft plugin;
	
	// only used by DB
	// schema (world,uniqueId,name,skill,maxSkill,material,isGreeter,messageId,x,y,z,yaw,pitch)
	protected DwarfTrainer (final DwarfCraft plugin, World newWorld, String newUniqueId, String newName, Integer newSkillId, Integer newMaxSkill, Material newMaterial, boolean newIsGreeter, String newGreeterMessage, double newX, double newY, double newZ, Float newYaw, Float newPitch ) {
		this.plugin = plugin;
		skillId = newSkillId;
		maxSkill = newMaxSkill;
		greeter = newIsGreeter;
		messageId = newGreeterMessage;
		x = newX;
		y = newY;
		z = newZ;
		yaw = newYaw;
		pitch = newPitch;
		basicNpcUniqueId = newUniqueId;
		basicNpcName = newName;
		basicHumanNpc = NpcSpawner.SpawnBasicHumanNpc(
				newUniqueId, 
				newName, 
				newWorld,
				newX, 
				newY, 
				newZ, 
				newYaw, 
				newPitch);
		
		Material material = newMaterial;
		assert (material != null);
		if ( material != Material.AIR ) {
			itemStack = new ItemStack(material);
			itemStack.setAmount(1);
			itemStack.setDurability((short)0);
			basicHumanNpc.getBukkitEntity().setItemInHand(itemStack);
		}
	}

	// constructor only for *trainers*
	protected DwarfTrainer (final DwarfCraft plugin, Player player, String uniqueId, String name, Integer skillId, Integer maxSkill, String greeterMessage, boolean isGreeter) {
		this.plugin = plugin;
		this.skillId = skillId; 
		this.maxSkill = maxSkill;
		this.messageId = greeterMessage;
		greeter = isGreeter;
		world = player.getWorld();
		x = player.getLocation().getX();
		y = player.getLocation().getY();
		z = player.getLocation().getZ();
		yaw = player.getLocation().getYaw();
		pitch = player.getLocation().getPitch();
		basicNpcUniqueId = uniqueId;
		basicNpcName = name;

		basicHumanNpc = NpcSpawner.SpawnBasicHumanNpc(
				uniqueId, 
				name, 
				player.getWorld(), 
				player.getLocation().getX(), 
				player.getLocation().getY(), 
				player.getLocation().getZ(), 
				player.getLocation().getYaw(), 
				player.getLocation().getPitch());
		
		Material material;
		if ( greeter )
			material = Material.AIR;
		else
			material = plugin.getDataManager().find(player).getSkill(skillId).getTrainerHeldMaterial();
		assert (material != null);
		if ( material != Material.AIR ) {
			itemStack = new ItemStack(material);
			itemStack.setAmount(1);
			itemStack.setDurability((short)0);
			basicHumanNpc.getBukkitEntity().setItemInHand(itemStack);
		}
	}
	
	@Override
	public boolean equals(Object that) {
		if ( this == that ) {
			return true;
		}
		else if ( that instanceof HumanEntity ) {
			if ( basicHumanNpc.getBukkitEntity().getEntityId() == ((HumanEntity)that).getEntityId()) {
				return true;
			}
		}
		return false;
	}
	
	protected String getUniqueId() { return basicHumanNpc.getUniqueId(); }
	protected String getName() { return basicHumanNpc.getName(); }
	protected BasicHumanNpc getBasicHumanNpc() { return basicHumanNpc; }
	protected Integer getSkillTrained() { return skillId; }
	protected World getWorld() { return world; }
	protected Integer getMaxSkill() { return maxSkill; }
	protected boolean isGreeter() { return greeter; }
	protected int getMaterial() { if (itemStack != null) return itemStack.getTypeId(); else return (int)(Material.AIR.getId()); }
	protected String getMessage() { return messageId; }
	
	protected Location getLocation() {
		return basicHumanNpc.getBukkitEntity().getLocation();
	}
	
	private void spawn() {
		basicHumanNpc = NpcSpawner.SpawnBasicHumanNpc(
				basicNpcUniqueId,
				basicNpcName, 
				world,
				x, 
				y, 
				z, 
				yaw, 
				pitch);
	}
	
	protected void lookAt(Entity target) {
		assert(target != null);
		Location l;
		l = target.getLocation().clone();
		if ( target instanceof Player ) {
			l.setY(l.getY()+((Player) target).getEyeHeight());
		}
		this.lookAt(l);
		return;
	}
	
	protected void lookAt(Location l) {
		assert(l != null);
		return;
	}
	
	protected void printLeftClick(Player player) {
		GreeterMessage msg = plugin.getDataManager().getGreeterMessage(messageId);
		if ( msg != null ) {
			plugin.getOut().sendMessage(player, msg.getLeftClickMessage());
		}
		else { 
			System.out.println("[DC] Error: Greeter " + basicHumanNpc.getUniqueId() + " has no left click message. Check your configuration file for message ID " + messageId);
		}
		return;
	}
	
	protected void printRightClick(Player player) {
		GreeterMessage msg = plugin.getDataManager().getGreeterMessage(messageId);
		if ( msg != null ) {
			plugin.getOut().sendMessage(player, msg.getRightClickMessage());
		}
		return;
	}
	
	protected void trainSkill(Dwarf dwarf){
		boolean soFarSoGood = true;
		Skill skill = dwarf.getSkill(this.skillId);
		assert(skill != null);
		Player player = dwarf.getPlayer();
		List <ItemStack> trainingCosts = dwarf.calculateTrainingCost(skill); 
		
		//Must be a dwarf, not an elf
		if (dwarf.isElf()) {
			plugin.getOut().sendMessage(player, "&cYou are an &fElf &cnot a &9Dwarf&6!", "&6[Train &b"+skill.getId()+"&6] ");
			soFarSoGood = false;
		}
		//Must have skill level between 0 and 29
		if ( skill.getLevel() >= 30) {
			plugin.getOut().sendMessage(player, "&cYour skill is max level (30)!", "&6[Train &b"+skill.getId()+"&6] ");
			soFarSoGood = false;
		}
		if (skill.getLevel()> maxSkill) {
			plugin.getOut().sendMessage(player, "&cI can't teach you any more, find a new trainer", "&6[Train &b"+skill.getId()+"&6] ");
			soFarSoGood = false;
		}
		
		//Must have enough materials to train
		for (ItemStack itemStack: trainingCosts) {
			if(itemStack == null) continue;
			if(itemStack.getAmount() == 0) continue;
			if(dwarf.countItem(itemStack.getTypeId()) < itemStack.getAmount()) {
				plugin.getOut().sendMessage(player, "&cYou do not have the &2"+itemStack.getAmount() + " " + itemStack.getType()+ " &crequired", "&6[Train &b"+skill.getId()+"&6] ");
				soFarSoGood = false;
			}
			else plugin.getOut().sendMessage(player, "&aYou have the &2"+itemStack.getAmount() + " " + itemStack.getType()+ " &arequired", "&6[Train &b"+skill.getId()+"&6] ");

		}
		
		//If passed all the 'musts' successfully
		if(soFarSoGood){
			skill.setLevel(skill.getLevel() + 1);
			for (ItemStack itemStack: trainingCosts)
				dwarf.removeInventoryItems(itemStack.getTypeId(), itemStack.getAmount());
			plugin.getOut().sendMessage(player,"&6Training Successful!","&6[&b"+skill.getId()+"&6] ");
			plugin.getDataManager().saveDwarfData(dwarf);
			return;
		}
		else{
			return; //something else goes here
		}
	}
}