package com.vsepecan.firstpersonshooter.weapon;

import org.bukkit.scheduler.BukkitRunnable;

public class CycleOfOperation extends BukkitRunnable {
    
    private final Weapon weapon;

    public CycleOfOperation(Weapon weapon) { this.weapon = weapon; }

    @Override
    public void run() { weapon.setLocked(true); cancel(); }

}
