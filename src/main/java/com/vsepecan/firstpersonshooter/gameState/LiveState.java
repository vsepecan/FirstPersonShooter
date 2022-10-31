/*
* Functioning of the live state of the game
* Mainly concerned with Running (hence the extending of BukkitRunnable class)
* And with Listening (hence the implementation of the Listener interface)
* All other functionalities should be called for*/
package com.vsepecan.firstpersonshooter.gameState;

import com.vsepecan.firstpersonshooter.Arena;
import com.vsepecan.firstpersonshooter.ConfigManager;
import com.vsepecan.firstpersonshooter.FirstPersonShooter;
import com.vsepecan.firstpersonshooter.database.PlayerStatistics;
import com.vsepecan.firstpersonshooter.team.Team;
import com.vsepecan.firstpersonshooter.weapon.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class LiveState extends BukkitRunnable implements Listener {

    private final FirstPersonShooter firstPersonShooter;
    private final Arena arena;

    private int liveSeconds;

    private final HashMap<UUID, PlayerStatistics> playerStatisticsHashMap;
    private final HashMap<UUID, Team> initialTeamsHashMap;

    public LiveState(FirstPersonShooter firstPersonShooter, HashMap<UUID, Team> inGamePlayersTeamsHashMap) {
        this.firstPersonShooter = firstPersonShooter;
        this.arena = firstPersonShooter.getArena();
        liveSeconds = ConfigManager.getLiveSeconds();

        Bukkit.getPluginManager().registerEvents(this, firstPersonShooter);
        runTaskTimer(firstPersonShooter, 0, 20);

        arena.broadcastTeamColoredTitleToInGamePlayers("Start!", "");

        playerStatisticsHashMap = new HashMap<>();
        this.initialTeamsHashMap = new HashMap<>();
        this.initialTeamsHashMap.putAll(inGamePlayersTeamsHashMap);
    }

    public String getTimeDisplay() { return ((liveSeconds > 59) ? ((liveSeconds / 60) + ":") : "") + (liveSeconds % 60); }

    @Override
    public void run() {
        if (liveSeconds == 0) {
            Team winnerTeam = calculateWhichTeamWon();

            if (winnerTeam != null) {
                arena.broadcastTitleToInGamePlayers(winnerTeam.getColor() + winnerTeam.getDisplayName() + " team won!", "");
                arena.broadcastTeamColoredMessageToTeam(winnerTeam, "Good job!");
            } else
                arena.broadcastTitleToInGamePlayers(ChatColor.DARK_AQUA + "It's a tie.","");

            arena.writeIntoDB(playerStatisticsHashMap);
            playerStatisticsHashMap.clear();

            arena.broadcastTeamColoredActionBarToInGamePlayers("");
            HandlerList.unregisterAll(this);

            arena.updateState(GameState.RECRUITING);
            return;
        } else if (liveSeconds <= 5) {
            arena.broadcastTeamColoredTitleToInGamePlayers("", getTimeDisplay());
        }

        arena.broadcastTeamColoredActionBarToInGamePlayers(ChatColor.BOLD + "Time left: " + getTimeDisplay());

        liveSeconds--;
    }

    @EventHandler
    public void onPlayerShoot(PlayerInteractEvent e) {
        if (arena.containsPlayerInGame(e.getPlayer()) && Objects.equals(e.getHand(), EquipmentSlot.HAND)
                && e.getItem() != null) {
                Weapon playerWeapon = firstPersonShooter.getWeaponsManager().getWeapon(e.getPlayer());
                
                if (playerWeapon == null || !e.getItem().equals(playerWeapon.getWeaponType().getItemStack()))
                    return;
                
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    e.setCancelled(true);
                    playerWeapon.shoot();
                } else if (e.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Trident bullet && bullet.getShooter() instanceof Player gunman &&
                arena.containsPlayerInGame(gunman)) {
            Weapon weapon = firstPersonShooter.getWeaponsManager().getWeapon(gunman);
        
            if (weapon == null || !gunman.getInventory().getItemInMainHand().equals(weapon.getWeaponType().getItemStack()))
                return;
            
            if (e.getHitEntity() instanceof Player wounded && arena.containsPlayerInGame(wounded)) {
                e.setCancelled(true);
                bullet.remove();

                if (!arena.getTeam(gunman).equals(arena.getTeam(wounded)))   // If not friendly fire
                    dealDamage(gunman, weapon, wounded);
                
            } else if (e.getHitBlock() != null) bullet.remove();
        }
    }
    
    private void dealDamage(@NotNull Player gunman, @NotNull Weapon weapon, @NotNull Player wounded) {
        if ((wounded.getHealth() - weapon.getWeaponType().getWoundingPercentage() / 5) <= 0) {
            arena.broadcastMessageToInGamePlayers(arena.getTeam(gunman).getColor() + gunman.getDisplayName()
                    + arena.getTeam(gunman).getColor() + " has killed "
                    + arena.getTeam(wounded).getColor() + wounded.getDisplayName());
            updatePlayerStatistics(gunman, wounded);
        
            wounded.setHealth(Objects.requireNonNull(wounded.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue());
            arena.teleportToTeamSpawnLocation(wounded);
            wounded.sendMessage(arena.getTeam(wounded).getColor() + "Watch out!");
        } else
            wounded.damage(weapon.getWeaponType().getWoundingPercentage() / 5);
    }
    
    private void updatePlayerStatistics(Player gunman, Player wounded) {
        if (playerStatisticsHashMap.containsKey(gunman.getUniqueId()))
            playerStatisticsHashMap.get(gunman.getUniqueId()).addKill();
        else
            playerStatisticsHashMap.put(gunman.getUniqueId(), new PlayerStatistics(false, 1, 0));

        if (playerStatisticsHashMap.containsKey(wounded.getUniqueId()))
            playerStatisticsHashMap.get(wounded.getUniqueId()).addDeath();
        else
            playerStatisticsHashMap.put(wounded.getUniqueId(), new PlayerStatistics(false, 0, 1));
    }

    private Team calculateWhichTeamWon() {
        int redTeamKillCount = 0;
        int blueTeamKillCount = 0;

        UUID uuid;
        PlayerStatistics playerStatistics;

        for (Map.Entry<UUID, PlayerStatistics> statisticsEntry : playerStatisticsHashMap.entrySet()) {
            uuid = statisticsEntry.getKey();
            playerStatistics = statisticsEntry.getValue();

            if (playerStatistics.getKillCount() > 0)
                switch (initialTeamsHashMap.get(uuid)) {
                    case RED -> redTeamKillCount += playerStatistics.getKillCount();
                    case BLUE -> blueTeamKillCount += playerStatistics.getKillCount();
                }
        }

        Team winnerTeam = null;

        if (redTeamKillCount > blueTeamKillCount)
            winnerTeam = Team.RED;
        else if (redTeamKillCount < blueTeamKillCount)
            winnerTeam =  Team.BLUE;

        // Update player statistics to reflect who is in the winning team
        Team finalWinnerTeam = winnerTeam;
        playerStatisticsHashMap.forEach((uuid2, playerStatistics2) -> playerStatistics2.setInWinningTeam(initialTeamsHashMap.get(uuid2) == finalWinnerTeam));

        return winnerTeam;
    }

}
