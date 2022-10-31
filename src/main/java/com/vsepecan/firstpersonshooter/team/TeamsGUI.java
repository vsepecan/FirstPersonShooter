package com.vsepecan.firstpersonshooter.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class TeamsGUI {

    public TeamsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.BLACK + "Choose your team!");

        for (Team team : Team.values())
            gui.setItem(team.getPositionInGUI(), team.getItemStack());

        ItemStack itemStack = new ItemStack(Material.BOWL);
        ItemMeta itemMeta = itemStack.getItemMeta();

        Objects.requireNonNull(itemMeta).setDisplayName(ChatColor.DARK_AQUA + "Assign whichever");
        itemMeta.setLocalizedName("Assign whichever");
        itemStack.setItemMeta(itemMeta);

        gui.setItem(4, itemStack);

        player.openInventory(gui);
    }

}
