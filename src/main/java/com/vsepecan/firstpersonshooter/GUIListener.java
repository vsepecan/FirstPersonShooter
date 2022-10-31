package com.vsepecan.firstpersonshooter;

import com.vsepecan.firstpersonshooter.team.Team;
import com.vsepecan.firstpersonshooter.weapon.WeaponsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class GUIListener implements Listener {

    Arena arena;
    WeaponsManager weaponsManager;

    public GUIListener(FirstPersonShooter firstPersonShooter) {
        this.arena = firstPersonShooter.getArena();
        this.weaponsManager = firstPersonShooter.getWeaponsManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() != null && e.getView().getTitle().equals(ChatColor.BLACK + "Choose your team!")) {
            Player player = (Player) e.getWhoClicked();

            if (Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getLocalizedName().equals("Assign whichever")) {
                player.sendMessage(ChatColor.DARK_AQUA + "No problem. We'll put you in a team when the game starts.");
                player.closeInventory();
                e.setCancelled(true);
                return;
            }

            Team team = Team.valueOf(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getLocalizedName());

            if (arena.getTeam(player) == team) {
                player.sendMessage(team.getColor() + "You're already with us!");
            } else if ((arena.containsPlayerInGame(player) && arena.isInGameTeamFull(team)) ||
                    (arena.containsPlayerWaiting(player) && arena.isWaitingTeamFull(team))) {
                player.sendMessage(ChatColor.RED + "We're full. Try a different team.");
            } else {
                arena.setTeam(player, team);
                player.sendMessage(team.getColor() + "Welcome onboard to the " + team.getDisplayName() + " team");
            }

            player.closeInventory();
            e.setCancelled(true);
        } else if (e.getCurrentItem() != null && e.getView().getTitle().equals(ChatColor.BLACK + "Choose a weapon")) {
            Player player = (Player) e.getWhoClicked();

            if (weaponsManager.getWeapon(player) != null &&
                    e.getCurrentItem().equals(Objects.requireNonNull(weaponsManager.getWeapon(player)).getWeaponType().getItemStack())) {
                player.sendMessage(ChatColor.RED + "You're already equipped with a " +
                        Objects.requireNonNull(weaponsManager.getWeapon(player)).getWeaponType().getDisplayName());
            } else
                weaponsManager.createAndAddWeapon(e.getCurrentItem(), player);

            player.closeInventory();
            e.setCancelled(true);
        }
    }

}
