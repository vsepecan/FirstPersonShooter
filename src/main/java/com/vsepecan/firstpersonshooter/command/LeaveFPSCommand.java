package com.vsepecan.firstpersonshooter.command;

import com.vsepecan.firstpersonshooter.ConfigManager;
import com.vsepecan.firstpersonshooter.FirstPersonShooter;
import com.vsepecan.firstpersonshooter.gameState.GameState;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LeaveFPSCommand implements CommandExecutor {

    private final FirstPersonShooter firstPersonShooter;

    public LeaveFPSCommand(FirstPersonShooter firstPersonShooter) { this.firstPersonShooter = firstPersonShooter; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (firstPersonShooter.getArena().containsPlayer(player)) {
                if (firstPersonShooter.getArena().containsPlayerInGame(player) &&
                        firstPersonShooter.getArena().getCurrentGameState().equals(GameState.LIVE))
                    player.teleport(ConfigManager.getLeaveLocation());

                firstPersonShooter.getArena().removePlayer(player);

                player.sendMessage(ChatColor.GREEN + "You left the FPS game.");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
                player.sendTitle("", "", 0, 1, 1);
            } else
                player.sendMessage(ChatColor.RED + "You are not in the FPS game. Run /fps to get in.");

            return true;
        }

        return false;
    }

}
