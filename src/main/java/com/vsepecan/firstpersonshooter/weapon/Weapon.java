package com.vsepecan.firstpersonshooter.weapon;

import com.vsepecan.firstpersonshooter.FirstPersonShooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class Weapon {

    private final FirstPersonShooter firstPersonShooter;

    private final WeaponType weaponType;
    private final UUID owner;
    
    private boolean locked;
    private boolean loaded;
    private int magazineSize;

    public Weapon(FirstPersonShooter firstPersonShooter, WeaponType weaponType, @NotNull UUID owner) {
        this.firstPersonShooter = firstPersonShooter;
        this.weaponType = weaponType;
        this.owner = owner;
        locked = true;
        this.load();
    }

    public void shoot() {
        if (locked && loaded) {
            Objects.requireNonNull(Bukkit.getPlayer(this.owner)).launchProjectile(Trident.class);
            magazineSize--;
            Objects.requireNonNull(Bukkit.getPlayer(this.owner)).playSound(
                    Objects.requireNonNull(Bukkit.getPlayer(this.owner)).getLocation(),
                    weaponType.getFiringSound(), weaponType.getVolumeOfFiringSound(), 1f);
            
            this.locked = false;
            startCycleOfOperation();
            
            if (magazineSize == 0) {
                this.loaded = false;
                reloadWeapon();
            }
        }
    }

    private void startCycleOfOperation() {
        long durationOfTheCycle = (long) ((1/this.weaponType.getRateOfFire()) * 20);
        new CycleOfOperation(this).runTaskLater(firstPersonShooter, durationOfTheCycle);
    }
    
    private void reloadWeapon() {
        long durationOfLoadingProcess = weaponType.getDurationOfLoadingProcess() * 20;
        new WeaponLoadingProcess(this).runTaskLater(firstPersonShooter, durationOfLoadingProcess);
        sendMessageToOwner(ChatColor.RED + "reloading");
    }

    public UUID getOwner() { return owner; }

    public WeaponType getWeaponType() { return weaponType; }
    
    public void setLocked(boolean locked) { this.locked = locked; }
    
    public void load() {
        this.magazineSize = this.weaponType.getMagazineCapacity();
        
        Player owner = Bukkit.getPlayer(this.owner);
        Objects.requireNonNull(owner).playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 0.8f, 1f);
        
        this.loaded = true;
        sendMessageToOwner(ChatColor.GREEN + "loaded");
    }
    
    private void sendMessageToOwner(String message) {
        Objects.requireNonNull(Bukkit.getPlayer(this.owner)).sendMessage(message);
    }
    
}
