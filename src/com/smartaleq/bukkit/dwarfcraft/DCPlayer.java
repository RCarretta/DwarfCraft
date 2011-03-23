package com.smartaleq.bukkit.dwarfcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

class DCPlayer {
	private final DwarfCraft plugin;
	private HashMap<Integer, Skill> skills;
	private Player player;
	private Race race ;

	public void setPlayer(Player player) {
		this.player = player;
	}

	protected DCPlayer(final DwarfCraft plugin, Player whoami) {
		this.plugin = plugin;
		player = whoami;
		race = plugin.getConfigManager().getDefaultRace();
	}

	protected List<ItemStack> calculateTrainingCost(Skill skill) {
		int highSkills = countHighSkills();
		int dwarfLevel = getDwarfLevel();
		int quartileSize = Math.min(4, highSkills / 4);
		int quartileNumber = 1; // 1 = top, 2 = 2nd, etc.
		int[] levelList = new int[highSkills + 1];
		List<ItemStack> trainingStack = new ArrayList<ItemStack>();
		int i = 0;

		// Creates an ordered list of skill levels and finds where in that list
		// the skill is (what quartile)
		if (DwarfCraft.debugMessagesThreshold < 0)
			System.out.println("DC0: starting skill ordering for quartiles");
		for (Skill s : getSkills().values()) {
			if (s.getLevel() > 5) {
				levelList[i] = s.getLevel();
				i++;
			}
		}
		Arrays.sort(levelList);
		if (levelList[highSkills - quartileSize] <= skill.getLevel())
			quartileNumber = 1;
		else if (levelList[highSkills - 2 * quartileSize] <= skill.getLevel())
			quartileNumber = 2;
		else if (levelList[highSkills - 3 * quartileSize] <= skill.getLevel())
			quartileNumber = 3;
		if (skill.getLevel() < 5)
			quartileNumber = 1; // low skills train full speed

		// calculate quartile penalties for 2nd/3rd/4th quartile
		double multiplier = Math
				.max(1, Math.pow(1.072, (skill.getLevel() - 5)));
		if (quartileNumber == 2)
			multiplier *= (1 + 1 * dwarfLevel / (100 + 3 * dwarfLevel));
		if (quartileNumber == 3)
			multiplier *= (1 + 2 * dwarfLevel / (100 + 3 * dwarfLevel));
		if (quartileNumber == 4)
			multiplier *= (1 + 3 * dwarfLevel / (100 + 3 * dwarfLevel));

		// create output item stack of new items
		trainingStack.add(new ItemStack(skill.getTrainingItem1Mat(), (int) Math
				.min(Math.ceil((skill.getLevel() + 1)
						* skill.getTrainingItem1BaseCost() * multiplier - .01),
				skill.getTrainingItem1MaxAmount())));
		if (skill.getTrainingItem2Mat() != Material.AIR)
			trainingStack.add(new ItemStack(skill.getTrainingItem2Mat(),
					(int) Math.min(
							Math.ceil((skill.getLevel() + 1)
									* skill.getTrainingItem2BaseCost()
									* multiplier - .01), 
							skill.getTrainingItem2MaxAmount())));
		if (skill.getTrainingItem3Mat() != Material.AIR)
			trainingStack.add(new ItemStack(skill.getTrainingItem3Mat(),
					(int) Math.min(
							Math.ceil((skill.getLevel() + 1)
									* skill.getTrainingItem3BaseCost()
									* multiplier - .01),
							skill.getTrainingItem3MaxAmount())));
		return trainingStack;
	}

	/**
	 * Counts skills greater than level 5, used for training costs
	 */
	private int countHighSkills() {
		int highCount = 0;
		for (Skill s : getSkills().values()) {
				if (s.getLevel() > 5)
					highCount++;
		}
		return highCount;
	}

	/**
	 * Counts items in a dwarf's inventory
	 * 
	 * @param itemId
	 * @return total item count int
	 */
	protected int countItem(int itemId) {
		int itemCount = 0;
		ItemStack[] items = player.getInventory().getContents();
		for (ItemStack item : items) {
			if (item.getTypeId() == itemId) {
				itemCount += item.getAmount();
			}
		}
		return itemCount;
	}

	/**
	 * Counts items in a dwarf's inventory
	 * 
	 * @param material
	 * @return total item count int
	 */
	protected int countItem(Material material) {
		return countItem(material.getId());
	}

	/**
	 * Calculates the dwarf's total level for display/e-peening. Value is the
	 * total of all skill level above 5, or the highest skill level when none
	 * are above 5.
	 * 
	 * @return
	 */
	protected int getDwarfLevel() {
		int playerLevel = 5;
		int highestSkill = 0;
		for (Skill s : getSkills().values()) {
			if (s.getLevel() > highestSkill)
				highestSkill = s.getLevel();
			
			if (s.getLevel() > 5)
				playerLevel = playerLevel + s.getLevel() - 5;
		}
		if (playerLevel == 5) 
			playerLevel = highestSkill;
		return playerLevel;
	}

	/**
	 * Retrieves an effect from a player based on its effectId.
	 * 
	 * @param effectId
	 * @return
	 */
	protected Effect getEffect(int effectId) {
		Skill skill = getSkill(effectId / 10);
		for (Effect effect : skill.getEffects()) {
			if (effect.getId() == effectId)
				return effect;
		}
		return null;
	}

	protected Player getPlayer() {
		return player;
	}

	/**
	 * Gets a dwarf's skill from an effect
	 * 
	 * @param effect
	 *            (does not have to be this dwarf's effect, only used for ID#)
	 * @return Skill or null if none found
	 */
	protected Skill getSkill(Effect effect) {
		for (Skill skill : skills.values()) {
			if (skill.getId() == effect.getId() / 10)
				return skill;
		}
		return null;
	}

	/**
	 * Gets a dwarf's skill by id
	 * 
	 * @param skillId
	 * @return Skill or null if none found
	 */
	protected Skill getSkill(int skillId) {
		Skill skill = skills.get(skillId);
		return skill;
	}

	/**
	 * Gets a dwarf's skill by name or id number(as String)
	 * 
	 * @param skillName
	 * @return Skill or null if none found
	 */
	protected Skill getSkill(String skillName) {
		try {
			return getSkill(Integer.parseInt(skillName));
		} catch (NumberFormatException n) {
			for (Skill skill : getSkills().values()) {
				if (skill.getDisplayName() == null)
					continue;
				if (skill.getDisplayName().equalsIgnoreCase(skillName))
					return skill;
				if (skill.toString().equalsIgnoreCase(skillName))
					return skill;
				if (skill.getDisplayName().toLowerCase()
						.regionMatches(0, skillName.toLowerCase(), 0, 5))
					return skill;
				if (skill.toString().toLowerCase()
						.regionMatches(0, skillName.toLowerCase(), 0, 5))
					return skill;
			}

		}
		return null;
	}

	protected HashMap<Integer, Skill> getSkills() {
		return skills;
	}
	
	protected boolean isElf() {
		if (skills.size()==0)
			return true;
		return false;
	}

	/**
	 * Calculates the Dwarf's total Level
	 * 
	 * @return total level
	 */
	protected int level() {
		int playerLevel = 5;
		int highestSkill = 0;
		for (Skill s : getSkills().values()) {
			if (s.getLevel() > highestSkill)
				highestSkill = s.getLevel();
			if (s.getLevel() > 5)
				playerLevel += s.getLevel() - 5;
			;
		}
		if (playerLevel == 5)
			playerLevel = highestSkill;
		return playerLevel;
	}

	/**
	 * makes the Dwarf into an elf
	 * 
	 * @return
	 */
//	private boolean makeElf() {
//		if (isElf)
//			return false;
//		isElf = true;
//		for (Skill skill : getSkills().values)
//			if (skill != null)
//				skill.setLevel(0);
//		plugin.getDataManager().saveDwarfData(this);
//		return isElf;
//	}
//
//	/**
//	 * Makes an elf into a dwarf
//	 */
//	protected boolean makeElfIntoDwarf() {
//		isElf = false;
//		for (Skill skill : getSkills().values())
//			if (skill != null)
//				skill.setLevel(0);
//		plugin.getDataManager().saveDwarfData(this);
//		return true;
//	}


	/**
	 * Removes a set number of items from an inventory No error checking, should
	 * only be used if the items have already been counted
	 * 
	 * @param itemId
	 * @param amount
	 */
	protected void removeInventoryItems(int itemId, int amount) {
		Inventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		ItemStack[] newcontents = contents.clone();
		int amountLeft = amount;
		for (int i = 0; i < 40; i++) {
			ItemStack item = contents[i];
			if (item.getTypeId() == itemId) {
				if (item.getAmount() > amountLeft) {
					newcontents[i].setAmount(item.getAmount() - amountLeft);
					break;
				} else if (item.getAmount() == amountLeft) {
					newcontents[i] = null;
					break;
				} else {
					amountLeft = amountLeft - item.getAmount();
					newcontents[i] = null;
				}
			}
		}
		player.getInventory().setContents(newcontents);
	}

	/**
	 * @param skills
	 *            the skills to set
	 */
	protected void setSkills(HashMap<Integer,Skill> skills) {
		this.skills = skills;
	}

	protected int skillLevel(int i) {
		for (Skill s : getSkills().values())
			if (s.getId() == i)
				return s.getLevel();
		return 0;
	}

	protected int countArmorPieces(ArmorType type){
		int count = 0;
		PlayerInventory inv = this.getPlayer().getInventory();
		if (type.ids.contains(inv.getHelmet().getTypeId())) count++;
		if (type.ids.contains(inv.getChestplate().getTypeId())) count++;
		if (type.ids.contains(inv.getLeggings().getTypeId())) count++;
		if (type.ids.contains(inv.getBoots().getTypeId())) count++;
		return count;
	}
	
	public void setRace(Race race) {
		this.race = race;
		this.skills = plugin.getConfigManager().getAllSkills(race);
	}

	public Race getRace() {
		return race;
	}

	protected enum ArmorType{
		IRON (Arrays.asList(306,307,308,309)),
		GOLD (Arrays.asList(314,315,316,317)),
		LEATHER (Arrays.asList(298,299,300,301)),
		DIAMOND (Arrays.asList(310,311,312,313));
		
		List<Integer> ids;
		ArmorType(List<Integer> ids){
			this.ids = ids;
		}
	}

	public void changeRace(Race newRace) {
		// TODO Auto-generated method stub
		
	}
	
}
