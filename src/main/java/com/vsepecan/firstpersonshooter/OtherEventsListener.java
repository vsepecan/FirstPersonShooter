package com.vsepecan.firstpersonshooter;

import com.vsepecan.firstpersonshooter.team.TeamsGUI;
import com.vsepecan.firstpersonshooter.weapon.Weapon;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class OtherEventsListener implements Listener {
	
	private final FirstPersonShooter firstPersonShooter;
	
	public OtherEventsListener(FirstPersonShooter firstPersonShooter) { this.firstPersonShooter = firstPersonShooter; }
	
	@EventHandler
	public void onPlayerDropItem (PlayerDropItemEvent event) {
		// Weapon drop
		Weapon weapon = firstPersonShooter.getWeaponsManager().getWeapon(event.getPlayer());
		
		if (weapon != null && event.getItemDrop().getItemStack().equals(weapon.getWeaponType().getItemStack()))
			event.setCancelled(true);
		
		// Team choosing item drop
		if (event.getItemDrop().getItemStack().getItemMeta() == null || !event.getItemDrop().getItemStack().getItemMeta().hasLocalizedName())
			return;
		
		if (event.getItemDrop().getItemStack().getItemMeta().getLocalizedName().equals("Team Choosing Item"))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Team choosing item interaction
		if (event.getItem() == null || event.getItem().getItemMeta() == null || !event.getItem().getItemMeta().hasLocalizedName())
			return;
		
		if (event.getItem().getItemMeta().getLocalizedName().equals("Team Choosing Item")) {
			Player player = event.getPlayer();
			Arena arena = firstPersonShooter.getArena();
			
			if (arena.getTeam(player) == null && !arena.atLeastTwoCorrespondingTeamsNotFull(player)) {
				player.sendMessage(ChatColor.RED + "The teams are full, there's no choice to make.");
				player.sendMessage(ChatColor.GREEN + "We'll assign you a team when the game starts.");
			} else
				new TeamsGUI(player);
		}
	}
	
}
