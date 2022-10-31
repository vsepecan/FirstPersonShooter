package com.vsepecan.firstpersonshooter.weapon;

import com.vsepecan.firstpersonshooter.ConfigManager;
import com.vsepecan.firstpersonshooter.FirstPersonShooter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class WeaponsManager {

    private final FirstPersonShooter firstPersonShooter;
    private final ArrayList<Weapon> ownedWeapons;
    
    public WeaponsManager(FirstPersonShooter firstPersonShooter) {
        this.firstPersonShooter = firstPersonShooter;
        ownedWeapons = new ArrayList<>();
    }
    
    public void createAndAddWeapon(@NotNull ItemStack itemStack, Player player) {
        this.removeAllWeapons(player);

        WeaponType weaponType = WeaponType.valueOf(Objects.requireNonNull(itemStack.getItemMeta()).getLocalizedName());
        Weapon weapon = new Weapon(firstPersonShooter, weaponType, player.getUniqueId());
        ownedWeapons.add(weapon);

        player.getInventory().addItem(weapon.getWeaponType().getItemStack());
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1f);
    }

    public void removeAllWeapons(Player player) {
        if (getWeapon(player) != null) {
            player.getInventory().remove(getWeapon(player).getWeaponType().getItemStack());
            
            if (player.getInventory().getItemInOffHand().equals(getWeapon(player).getWeaponType().getItemStack()))
                player.getInventory().setItemInOffHand(null);
        }
        
        ownedWeapons.removeIf(ownedWeapon -> ownedWeapon.getOwner().equals(player.getUniqueId()));
    }

    public Weapon getWeapon(Player player) {
        for (Weapon ownedWeapon : ownedWeapons)
            if (ownedWeapon.getOwner().equals(player.getUniqueId()))
                return ownedWeapon;
        return null;
    }
    
    public void giveWeaponsToPlayers(ArrayList<UUID> inGamePlayers) {
        inGamePlayers.forEach(uuid -> {
            if (getWeapon(Bukkit.getPlayer(uuid)) == null)
                createAndAddWeapon(ConfigManager.getDefaultWeaponType().getItemStack(), Bukkit.getPlayer(uuid));
        });
    }
}
