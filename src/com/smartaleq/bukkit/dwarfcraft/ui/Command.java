package com.smartaleq.bukkit.dwarfcraft.ui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.smartaleq.bukkit.dwarfcraft.DataManager;
import com.smartaleq.bukkit.dwarfcraft.Dwarf;
import com.smartaleq.bukkit.dwarfcraft.DwarfCraft;
import com.smartaleq.bukkit.dwarfcraft.Effect;
import com.smartaleq.bukkit.dwarfcraft.School;
import com.smartaleq.bukkit.dwarfcraft.Skill;
import com.smartaleq.bukkit.dwarfcraft.ui.Out;

public class Command {
	
	Player player;
	String[] playerInput;
	private final DwarfCraft plugin;
	
	public Command(DwarfCraft instance, Player player, String[] playerInput){
		this.plugin = instance;
		this.player = player;
		this.playerInput = playerInput;
	}
	
	public Player getPlayer(String playerName){
		Player[] players = plugin.getServer().getOnlinePlayers();
        for (Player player : players) {
            if (player.getName().equalsIgnoreCase(playerName)) return player;
        }
        return null;
	}	
	
	/**
	 * isInt(String)
	 * returns true if string is an int, false if not
	 */
	public boolean isInt (String str) {
		try {
			Integer.parseInt(str);
			return true;
		}
		catch(NumberFormatException nfe) {
			return false;
		}
	}
	
	public boolean execute(){
		if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started execute");

		if (playerInput[0].equalsIgnoreCase("debug")) return debug();

		if (playerInput[0].equalsIgnoreCase("help")) return help();
		if (playerInput[0].equalsIgnoreCase("?")) return help();
		if (playerInput[0].equalsIgnoreCase("info")) return info();
		if (playerInput[0].equalsIgnoreCase("rules")) return rules();
		if (playerInput[0].equalsIgnoreCase("commands")) return commands(1);
		if (playerInput[0].equalsIgnoreCase("commands2")) return commands(2);
		if (playerInput[0].equalsIgnoreCase("tutorial")) return tutorial(1);
		if (playerInput[0].equalsIgnoreCase("tutorial2")) return tutorial(2);
		if (playerInput[0].equalsIgnoreCase("tutorial3")) return tutorial(3);
		if (playerInput[0].equalsIgnoreCase("tutorial4")) return tutorial(4);
		if (playerInput[0].equalsIgnoreCase("tutorial5")) return tutorial(5);
		if (playerInput[0].equalsIgnoreCase("skillsheet")) 			return skillSheet();
		if (playerInput[0].equalsIgnoreCase("skillinfo")) 			return skillInfo();
		if (playerInput[0].equalsIgnoreCase("effectinfo")) 			return effectInfo();
		
		if (playerInput[0].equalsIgnoreCase("train")) 				return train();
		if (playerInput[0].equalsIgnoreCase("setskill")) 			return (player.isOp() ? setSkill(): notAnOpError());
		
		if (playerInput[0].equalsIgnoreCase("MAKEMEADWARF")) 		return makeMeADwarf(false);
		if (playerInput[0].equalsIgnoreCase("REALLYMAKEMEADWARF"))	return makeMeADwarf(true);
		if (playerInput[0].equalsIgnoreCase("MAKEMEANELF")) 		return makeMeAnElf(false);
		if (playerInput[0].equalsIgnoreCase("REALLYMAKEMEANELF")) 	return makeMeAnElf(true);
		
		if (playerInput[0].equalsIgnoreCase("schools")) 			return schoolList();
		if (playerInput[0].equalsIgnoreCase("SCHOOLINFO")) 			return schoolInfo();
		if (playerInput[0].equalsIgnoreCase("HERE")) 				return here();
		if (playerInput[0].equalsIgnoreCase("CREATESCHOOL")) 		return (player.isOp() ? createSchool(): notAnOpError());
		if (playerInput[0].equalsIgnoreCase("removeSCHOOL")) 		return (player.isOp() ? removeSchool(): notAnOpError());			
		if (playerInput[0].equalsIgnoreCase("listschools")) 		return (player.isOp() ? listAllSchools(): notAnOpError());		
		return false;
	}
	
	private boolean rules() {
		return Out.rules(player);
	}

	private boolean listAllSchools() {
		if(!Out.listSchools(player)){Out.sendMessage(player, Messages.Fixed.ERRORNOZONES.message); return true;}
		return true;
	}

	private boolean removeSchool() {
		if (playerInput[1]== null) {
			Out.sendMessage(player, "&cYou need to enter the exact school name.");
		}
		if(!DataManager.removeSchoolZone(playerInput[1])){
			Out.sendMessage(player, "&cYou need to enter the exact school name.");
		}
		else return true;
		return false;
	}

	/**
	 * Changes the level of debug reporting in console
	 */
	private boolean debug() {
		if (playerInput[1] != null ) {
			DwarfCraft.debugMessagesThreshold=Integer.parseInt(playerInput[1]);
			if (DwarfCraft.debugMessagesThreshold < 9) System.out.println("*** DEBUG LEVEL CHANGED TO "+playerInput[1]+" ***");
			return true;
		}
		return false;
	}

	/**
	 * Sends detailed help text from command help listing or general help text with no argument
	 */
	public boolean help() {
		if (DwarfCraft.debugMessagesThreshold < 2) System.out.println("Debug Message: started help");
		if (playerInput[1] == null)	return Out.generalHelp(player);
		if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started help2");
		for (CommandInfo c: CommandInfo.values()){
			if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started help3");
			if (playerInput[1].equalsIgnoreCase(c.toString())){		
				if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started help4");
				//this is a command
				return Out.commandHelp(player, c);
			}
		}
		// this is not a command
		Out.error(player, Messages.Fixed.ERRORBADINPUT);
		return false;
	}
	
	private boolean info() {
		return Out.info(player);
	}

	private boolean commands(int i) {
		return Out.commandList(player, i);
	}
	private boolean tutorial(int i) {
		return Out.tutorial(player, i);
	}

	/**
	 * Player command to print current skillsheet
	 * Syntax: /dc skillsheet [target]
	 * Does own sanitization and error checking.
	 * Target is optional, will print caller's skillsheet on null
	 */
	private boolean skillSheet() {
		if (DwarfCraft.debugMessagesThreshold < 2) System.out.println("Debug Message: starting skillsheet");
		Dwarf target;
		if (playerInput[1] != null) {
			Player playerTarget = getPlayer(playerInput[1]);
			if (playerTarget != null && playerTarget.isOnline())
				target = Dwarf.find(playerTarget);
			else
				target = Dwarf.find(playerInput[1]);
			
			if (target == null) {
				Out.sendMessage(player, "Could not find player &9" + playerInput[1]);
				return true;
			}
		}
		else { // playerinput was null
			target = Dwarf.find(player);
		}
		if (DwarfCraft.debugMessagesThreshold < 2) System.out.println("Debug Message: skillsheet target =" + playerInput[1]);
		return Out.printSkillSheet(target, player, playerInput[1]);
	}

	/**
	 * Player command to print skill information
	 * Syntax: /dc skillinfo <skill>
	 * <skill> is skill ID or skill name
	 * Does own error checking.
	 */
	private boolean skillInfo() {
		if ( playerInput[1] == null || playerInput[2] != null ) {
			Out.sendMessage(player, "Usage: /dc skillinfo &b<skill>");
			return true;
		}
//		wtf does this do?			
//		if (playerInput[2] != null) playerInput[1] = playerInput[1].concat(" " + playerInput[2]);
		Dwarf dwarf = Dwarf.find(player);
		if (dwarf == null) { // can this ever happen?
			System.out.println("Error: in skillInfo(): Player " + player.getDisplayName() + " has no associated dwarf.");
			return false;
		}
		
		Skill skill = dwarf.getSkill(playerInput[1]);
		if (skill == null) {
			Out.sendMessage(player, "Skill &b" + playerInput[1] + "&6 not found.");
			return true;
		}
		return Out.printSkillInfo(player, skill);
	}

	/**
	 * Player command to print effect information
	 * Syntax: /dc effectinfo <effect>
	 * <effect> is effect ID
	 * Does own error checking.
	 */
	private boolean effectInfo() {
		if (playerInput[1] == null || playerInput[2] != null) { 
			Out.sendMessage(player, "Usage: /dc effectinfo &5<effect>");
			return true;
		}
		Dwarf dwarf = Dwarf.find(player);
		if (dwarf == null) { // can this ever happen?
			System.out.println("Error: in effectInfo(): Player " + player.getDisplayName() + " has no associated dwarf.");
			return false;			
		}
		
		if (!isInt(playerInput[1])) {
			Out.sendMessage(player, "Effect must be a numeric effect ID");
			return true;			
		}
		Effect effect = dwarf.getEffect(Integer.parseInt(playerInput[1]));
		if(effect == null) {
			Out.sendMessage(player, "Could not find effect ID &5" + playerInput[1]);
			return true;
		}
		else
			return Out.effectInfo(player, effect);
	}
	
	/**
	 * Player command for training skills
	 * Syntax: /dc train <skill>
	 * If successful increases skill, removes items
	 * Does own error checking
	 */
	private boolean train() {
		boolean soFarSoGood = true;
		if ( playerInput[1] == null || playerInput[2] != null ) {
			Out.sendMessage(player, "Usage: /dc train &b<skill>");
			return true;
		}
		
		Dwarf dwarf = (Dwarf.find(player));
		Skill skill = dwarf.getSkill(playerInput[1]);
		if (skill == null) { 
			Out.sendMessage(player, "&cCould not find skill &b" + playerInput[1]);
			return true;
		}
		
		ItemStack[] trainingCosts = dwarf.calculateTrainingCost(skill); 
		
		//Must be a dwarf, not an elf
		if (dwarf.isElf) {
			Out.sendMessage(dwarf, "&cYou are an &fElf &cnot a &9Dwarf&6!", "&6[Train &b"+skill.id+"&6] ");
			soFarSoGood = false;
		}
		else Out.sendMessage(dwarf, "&aYou are a &9Dwarf &aand can train skills.", "&6[Train &b"+skill.id+"&6] ");
		
		//Must have skill level between 0 and 29
		if ( skill.level >= 30) {
			Out.sendMessage(dwarf, "&cYour skill is max level (30)!", "&6[Train &b"+skill.id+"&6] ");
			soFarSoGood = false;
		}
			
		//Must be in training Zone, may change this to near a trainer if/when NPCs are implemented
		if (!dwarf.isInSchoolZone(skill.school)) {
			Out.sendMessage(dwarf, "&cYou are not in a &1"+skill.school+" &ctraining zone", "&6[Train &b"+skill.id+"&6] ");
			soFarSoGood = false;
		}
		else Out.sendMessage(dwarf, "&aYou are in a &1"+skill.school+" &atraining zone", "&6[Train &b"+skill.id+"&6] ");
		
		//Must have enough materials to train
		for (ItemStack itemStack: trainingCosts) {
			if(itemStack == null) continue;
			if(itemStack.getAmount() == 0) continue;
			if(dwarf.countItem(itemStack.getTypeId()) < itemStack.getAmount()) {
				Out.sendMessage(dwarf, "&cYou do not have the &2"+itemStack.getAmount() + " " + itemStack.getType()+ " &crequired", "&6[Train &b"+skill.id+"&6] ");
				soFarSoGood = false;
			}
			else Out.sendMessage(dwarf, "&aYou have the &2"+itemStack.getAmount() + " " + itemStack.getType()+ " &arequired", "&6[Train &b"+skill.id+"&6] ");

		}
		
		//If passed all the 'musts' successfully
		if(soFarSoGood){
			skill.level++;
			for (ItemStack itemStack: trainingCosts)
				dwarf.removeInventoryItems(itemStack.getTypeId(), itemStack.getAmount());
			Out.sendMessage(dwarf,"&6Training Successful!","&6[&b"+skill.id+"&6] ");
			DataManager.saveDwarfData(dwarf);
			return true;
		}
		else{
			return true; //something else goes here
		}
	}
	
	/**
	 * Admin Command to change another player's skill.
	 * Syntax: /dc setskill <player> <skill> <level>
	 * <player> is target, <skill> is skill ID or alpha
	 * <level> is desired level in range 0-30
	 * Performs input sanitization and cursory error checking,
	 */
	private boolean setSkill() {
//		if (!player.hasPermission()) return false;
		try{
			
			if (playerInput[1] == null || playerInput[2] == null || playerInput[3] == null || playerInput[4] != null) {
				Out.sendMessage(player, "&cSyntax: &e/dc setskill &9<player> &b<skill> &3<level>");
				return true;
			}
			
			Player target = getPlayer(playerInput[1]);
			if (target == null) {
				Out.sendMessage(player, "&cError: &ePlayer &9" + playerInput[1] + " &ecould not be found.");
				return true;
			}
			Dwarf dwarf = Dwarf.find(target);
			if (dwarf == null) {
				Out.sendMessage(player, "&cError: &ePlayer &9" + playerInput[1] + " &efound, but could not find associated Dwarf.");
				return true;
			}
			if (dwarf.isElf()) {
				Out.sendMessage(player, "&cError: &ePlayer &9" + playerInput[1] + " &eis an elf.");
				return true;
			}
			
			Skill skill = dwarf.getSkill(playerInput[2]);
			if (skill == null) {
				Out.sendMessage(player, "&cError: &eCould not find skill &b" + playerInput[2]);
				return true;
			}
			
			if (!isInt(playerInput[3])) {
				Out.sendMessage(player, "&cError: &eSkill value not a number");
				return true;
			}
			Integer level = Integer.parseInt(playerInput[3]);
			
			if ( level < 0 || level > 30) { // only support setting in-bounds skills. (0-30)
				Out.sendMessage(player, "&cError: &eskill level &3" + level + "&e out of bounds.");
				return true;
			}
			
			skill.level = level;
			Out.sendMessage(player, "&aAdmin: &eset skill &b" + skill.displayName + "&e for player &9" + target.getDisplayName() + "&e to &3" + level);
			DataManager.saveDwarfData(dwarf);
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}

	private boolean makeMeADwarf(boolean confirmed) {
		Dwarf dwarf = Dwarf.find(player);
		if (dwarf.isElf) return dwarf.makeElfIntoDwarf(); 
		else if (confirmed) {
			Out.becameADwarf(player);
			return dwarf.makeElfIntoDwarf();
		}
		else {
			Out.confirmBecomingDwarf(player);
			return true;
		}
	}
	
	private boolean makeMeAnElf(boolean confirmed) {
		Dwarf dwarf = Dwarf.find(player);		
		if (dwarf.isElf) {
			Out.alreadyAnElf(player);
			return true;
		}
		else if (confirmed) {
			Out.becameAnElf(player);
			return dwarf.makeElf();
		}
		else {
			Out.confirmBecomingAnElf(player);
			return true;
		}
	}
	
	private boolean schoolList() {
		return Out.schoolList(player);
	}
	
	private boolean schoolInfo() {
		List<Skill> skills = new ArrayList<Skill>();
		School school = School.getSchool(playerInput[1]);
		for (Skill s: (Dwarf.find(player)).skills){
			if (s==null) continue;
			if(s.school == school) {
				skills.add(s);
			}
		}
		Out.schoolInfo(player, school, skills);
		return false;
	}
	
	private boolean here() {
		return Out.here(player,(Dwarf.find(player)).listAllZones());
	}
	
	private boolean createSchool() {
		try {
			World world = player.getWorld();
			School school = School.getSchool(Integer.parseInt(playerInput[1]));
			double x1 = Double.parseDouble(playerInput[2]); 
			double x2 = Double.parseDouble(playerInput[3]); 
			double y1 = Double.parseDouble(playerInput[4]); 
			double y2 = Double.parseDouble(playerInput[5]);
			double z1 = Double.parseDouble(playerInput[6]); 
			double z2 = Double.parseDouble(playerInput[7]);
			String name = playerInput[8];
			return DataManager.addSchoolZone(new Vector(x1,y1,z1), new Vector(x2,y2,z2), world, school, name);
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		} 		
	}

	private boolean notAnOpError(){
		Out.sendMessage(player, "You are not authorized to use that command");
	return true;
	}
}
