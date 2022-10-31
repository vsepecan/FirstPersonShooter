package com.vsepecan.firstpersonshooter.gameState;

import com.vsepecan.firstpersonshooter.Arena;
import com.vsepecan.firstpersonshooter.ConfigManager;
import com.vsepecan.firstpersonshooter.FirstPersonShooter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class CountdownState extends BukkitRunnable {

    private final Arena arena;
    private int countdownSeconds;

    public CountdownState(FirstPersonShooter firstPersonShooter) {
        this.arena = firstPersonShooter.getArena();
        countdownSeconds = ConfigManager.getCountdownSeconds();
        runTaskTimer(firstPersonShooter, 0, 20);
    }

    @Override
    public void run() {
        if (countdownSeconds == 0) {
            cancel();
            arena.updateState(GameState.LIVE);
            return;
        }

        arena.broadcastTeamColoredTitleToInGamePlayers("Game starts in: " + countdownSeconds, "");

        countdownSeconds--;
    }

    public void restart() {
        arena.broadcastMessageToInGamePlayers(ChatColor.RED + "A player just left.");
        arena.broadcastMessageToInGamePlayers(ChatColor.GREEN + "A new one joined.");

        countdownSeconds = ConfigManager.getCountdownSeconds();
    }

}
