package com.vsepecan.firstpersonshooter.weapon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;

public enum WeaponType {

    MINIGUN("Minigun", ChatColor.GRAY, 100, 7f, 30, 6, 13.34f, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f),
    SNIPER("Sniper", ChatColor.RED, 200, 0.9f, 1, 4, 50.51f, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f),
    RIFFLE("Riffle", ChatColor.DARK_AQUA, 300, 5f, 15, 3, 23f, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f);

    private final String displayName;
    private final ChatColor color;
    private final int customModelData;
    private final ItemStack itemStack;

    private final float rateOfFire;   // Measured in rounds per second
    private final int magazineCapacity;   // Maximum number of rounds (bullets) in a magazine
    private final long durationOfLoadingProcess;  // Measured in seconds
    private final float woundingPercentage;   // Measured in the percentage of players maximum health (Attribute.GENERIC_MAX_HEALTH)
    private final Sound firingSound;
    private final float volumeOfFiringSound;
    
    WeaponType(String displayName, ChatColor color, int customModelData, float rateOfFire, int magazineCapacity, long durationOfLoadingProcess, float woundingAmount,
               Sound firingSound, float volumeOfFiringSound) {
        this.displayName = displayName;
        Material material = Material.IRON_HOE;
        this.color = color;
        this.customModelData = customModelData;

        this.rateOfFire = rateOfFire;
        this.magazineCapacity = magazineCapacity;
        this.durationOfLoadingProcess = durationOfLoadingProcess;
        this.woundingPercentage = woundingAmount;

        itemStack = new ItemStack(material);
        setupItemMeta();
    
        this.firingSound = firingSound;
        this.volumeOfFiringSound = volumeOfFiringSound;
    }

    private void setupItemMeta() {
        ItemMeta itemMeta = itemStack.getItemMeta();

        Objects.requireNonNull(itemMeta).setDisplayName(this.color + this.displayName);
        itemMeta.setCustomModelData(this.customModelData);
        itemMeta.setLocalizedName(this.name());
        itemMeta.setLore(Arrays.asList("", color.toString() + (woundingPercentage / 5) + " Attack Damage", color.toString() + rateOfFire + " Attack Speed"));
        itemMeta.setUnbreakable(true);

        itemStack.setItemMeta(itemMeta);
    }

    public ItemStack getItemStack() { return itemStack; }

    public String getDisplayName() { return displayName; }

    public float getRateOfFire() { return rateOfFire; }

    public float getWoundingPercentage() { return woundingPercentage; }
    
    public Sound getFiringSound() { return firingSound; }
    
    public float getVolumeOfFiringSound() { return volumeOfFiringSound; }
    
    public int getMagazineCapacity() { return magazineCapacity; }
    
    public long getDurationOfLoadingProcess() { return durationOfLoadingProcess; }
    
}
