package com.vsepecan.firstpersonshooter.team;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public enum Team {

    RED("RED", Material.RED_BANNER, ChatColor.DARK_RED, 1),
    BLUE("BLUE", Material.BLUE_BANNER, ChatColor.DARK_BLUE, 7);

    private final String displayName;
    private final ChatColor color;
    private final ItemStack itemStack;
    private final int positionInGUI;

    Team(String displayString, Material material, ChatColor color, int positionInGUI) {
        this.displayName = displayString;
        this.color = color;
        this.itemStack = new ItemStack(material);
        setupItemMeta();
        this.positionInGUI = positionInGUI;
    }
    
    private void setupItemMeta() {
        ItemMeta itemMeta = itemStack.getItemMeta();
    
        Objects.requireNonNull(itemMeta).setDisplayName(this.color + "Team " + this.displayName);
        itemMeta.setLocalizedName(this.name());
    
        itemStack.setItemMeta(itemMeta);
    }
    
    public String getDisplayName() { return displayName; }
    
    public ChatColor getColor() { return color; }
    
    public int getPositionInGUI() { return positionInGUI; }
    
    public ItemStack getItemStack() { return itemStack; }
    
}
