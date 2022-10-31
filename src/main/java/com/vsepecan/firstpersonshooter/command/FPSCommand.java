package com.vsepecan.firstpersonshooter.command;

import com.vsepecan.firstpersonshooter.Arena;
import com.vsepecan.firstpersonshooter.FirstPersonShooter;
import com.vsepecan.firstpersonshooter.gameState.GameState;
import com.vsepecan.firstpersonshooter.team.TeamsGUI;
import com.vsepecan.firstpersonshooter.weapon.WeaponsGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FPSCommand implements CommandExecutor {

    FirstPersonShooter firstPersonShooter;

    public FPSCommand(FirstPersonShooter firstPersonShooter) { this.firstPersonShooter = firstPersonShooter; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            Arena arena = firstPersonShooter.getArena();

            if (args.length == 0) {
                if (!arena.containsPlayer(player)) {
                    boolean insertionSuccess = arena.addPlayer(player);
                    if (insertionSuccess) {
                        player.sendMessage(ChatColor.GREEN + "You joined the game!");
                        player.sendMessage(ChatColor.GREEN + "Run /leavefps to leave.");
                    } else
                        player.sendMessage(ChatColor.RED + "Both the game and waiting queue are full. Please wait a minute or two.");
                } else
                    player.sendMessage(ChatColor.RED + "You are already in the FPS game.");

                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("teams")) {
                if (arena.containsPlayer(player)) {
                    if (arena.containsPlayerInGame(player) && arena.getCurrentGameState().equals(GameState.LIVE)) {
                        player.sendMessage(arena.getTeam(player).getColor() + "You can't change teams now.");
                    } else if (arena.getTeam(player) == null && !arena.atLeastTwoCorrespondingTeamsNotFull(player)) {
                        player.sendMessage(ChatColor.RED + "The teams are full, there's no choice to make.");
                        player.sendMessage(ChatColor.GREEN + "We'll assign you a team when the game starts.");
                    } else {
                        new TeamsGUI(player);
                    }
                } else player.sendMessage(ChatColor.RED + "You are not in the FPS game. Run /fps to get in.");

                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("weapons")) {
                if (!arena.containsPlayer(player))
                    player.sendMessage(ChatColor.RED + "You are not in the FPS game. Run /fps to get in.");
                else if (arena.containsPlayerWaiting(player) || !firstPersonShooter.getArena().getCurrentGameState().equals(GameState.LIVE)) {
                    player.sendMessage(ChatColor.RED + "You can't choose weapons yet. Wait for the game to start.");
                } else
                    new WeaponsGUI(player);

                return true;
            } else return false;
        }

        return false;
    }

}
