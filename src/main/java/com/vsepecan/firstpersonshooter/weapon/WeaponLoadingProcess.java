package com.vsepecan.firstpersonshooter.weapon;

import org.bukkit.scheduler.BukkitRunnable;

public class WeaponLoadingProcess extends BukkitRunnable {
	
	private final Weapon weapon;
	
	public WeaponLoadingProcess(Weapon weapon) { this.weapon = weapon; }
	
	@Override
	public void run() { this.weapon.load(); cancel(); }
	
}
