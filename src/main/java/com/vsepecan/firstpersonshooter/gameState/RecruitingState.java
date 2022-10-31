package com.vsepecan.firstpersonshooter.gameState;

import com.vsepecan.firstpersonshooter.Arena;
import com.vsepecan.firstpersonshooter.ConfigManager;
import com.vsepecan.firstpersonshooter.FirstPersonShooter;
import org.bukkit.scheduler.BukkitRunnable;

public class RecruitingState extends BukkitRunnable {

    private final Arena arena;
    String broadcastMessage;

    public RecruitingState(FirstPersonShooter firstPersonShooter, Arena arena) {
        this.arena = arena;
        runTaskTimer(firstPersonShooter, 0, 20);
    }

    @Override
    public void run() {
        if (arena.getCurrentGameState().equals(GameState.RECRUITING)) {
            broadcastMessage = "There " + ((arena.getWaitingPlayersCount() > 1) ? "are " : "is ") +
                    arena.getWaitingPlayersCount() + " player" +
                    ((arena.getWaitingPlayersCount() > 1) ? "s" : "") + " in queue. Waiting for " +
                    ConfigManager.getNumberOfPlayersInMatch() + ".";

            arena.broadcastTeamColoredActionBarToPlayersWaiting(broadcastMessage);
        } else if (arena.getCurrentGameState().equals(GameState.COUNTDOWN)) {
            arena.broadcastTeamColoredActionBarToPlayersWaiting("You're in a queue. Please wait for the current game to finish.");
        } else if (arena.getCurrentGameState().equals(GameState.LIVE)) {
            arena.broadcastTeamColoredActionBarToPlayersWaiting("You're in a queue. Current game finishes in " +
                    arena.getLiveState().getTimeDisplay());
        }
    }

}
