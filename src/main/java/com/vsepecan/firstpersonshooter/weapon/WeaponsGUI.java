package com.vsepecan.firstpersonshooter.weapon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class WeaponsGUI {

    public WeaponsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.BLACK + "Choose a weapon");

        for (WeaponType weaponType : WeaponType.values())
            gui.addItem(weaponType.getItemStack());

        player.openInventory(gui);
    }

}
