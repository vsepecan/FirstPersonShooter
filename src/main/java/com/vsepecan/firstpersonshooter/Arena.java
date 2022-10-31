package com.vsepecan.firstpersonshooter;

import com.vsepecan.firstpersonshooter.database.PlayerStatistics;
import com.vsepecan.firstpersonshooter.gameState.CountdownState;
import com.vsepecan.firstpersonshooter.gameState.GameState;
import com.vsepecan.firstpersonshooter.gameState.LiveState;
import com.vsepecan.firstpersonshooter.gameState.RecruitingState;
import com.vsepecan.firstpersonshooter.team.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Arena {

    private final FirstPersonShooter firstPersonShooter;

    private GameState gameState;

    private final ArrayList<UUID> waitingPlayers;
    private final ArrayList<UUID> inGamePlayers;
    private final HashMap<UUID, Team> inGamePlayersTeamsHashMap;
    private final HashMap<UUID, Team> waitingPlayersTeamsHashMap;

    private CountdownState countdownState;  // Starts and cancels itself
    private LiveState liveState;    // Starts itself, doesn't cancel itself


    public Arena(FirstPersonShooter firstPersonShooter) {
        this.firstPersonShooter = firstPersonShooter;

        this.inGamePlayers = new ArrayList<>();
        this.waitingPlayers = new ArrayList<>();
        this.inGamePlayersTeamsHashMap = new HashMap<>();
        this.waitingPlayersTeamsHashMap = new HashMap<>();

        gameState = GameState.RECRUITING;

        // Runs itself in the constructor, never cancels
        // In reality, it should be canceled when there are no players waiting
        // The only purpose of that is to slightly improve performance
        new RecruitingState(firstPersonShooter, this);
    }

    // PLAYER INSERTION, REMOVAL, TRANSFER

    // addPlayer() and removePlayer()
    // Both are mainly concerned with proper insertion and removal of players from the two main sets of players
    // And the two HashMaps keeping track of players and teams they're on
    // After the operation they notify the Arena of the subsequent state change
    // addPlayer() returns boolean indicating success of the insertion process
    public boolean addPlayer(Player player) {
        if (gameState.equals(GameState.RECRUITING)) {
            waitingPlayers.add(player.getUniqueId());
            giveTeamChoosingItem(player);
            
            startCountdownIfPossible();
        } else if (gameState.equals(GameState.COUNTDOWN) || gameState.equals(GameState.LIVE)) {
            if (waitingPlayers.size() < ConfigManager.getNumberOfPlayersInMatch()) {
                waitingPlayers.add(player.getUniqueId());
                giveTeamChoosingItem(player);
            } else return false;
        } else { System.out.println("Error 63524"); return false; }
        
        return true;
    }

    // Role same as addPlayer()'s, check its description
    public void removePlayer(Player player) {
        if (containsPlayerWaiting(player)) {
            waitingPlayers.remove(player.getUniqueId());
            removeTeam(player);
        } else if (containsPlayerInGame(player)) {
            inGamePlayers.remove(player.getUniqueId());
            Team leavingPlayersTeam = getTeam(player);
            removeTeam(player);
            
            if (gameState.equals(GameState.COUNTDOWN))
                restartOrCancelCountdown();
            else if (gameState.equals(GameState.LIVE))
                continueOrCancelGame(player, leavingPlayersTeam);
            
            firstPersonShooter.getWeaponsManager().removeAllWeapons(player);
        }
        
        removeTeamChoosingItem(player);
    }

    private void startCountdownIfPossible() {
        if (waitingPlayers.size() == ConfigManager.getNumberOfPlayersInMatch()) {
            inGamePlayers.addAll(waitingPlayers);
            inGamePlayersTeamsHashMap.putAll(waitingPlayersTeamsHashMap);
            waitingPlayers.clear();
            waitingPlayersTeamsHashMap.clear();

            updateState(GameState.COUNTDOWN);
        }
    }

    // Takes a player from waitingPlayers if there are any and restarts countdown
    // Otherwise transfers players properly and notifies arena of updating state to RECRUITING
    private void restartOrCancelCountdown() {
        if (waitingPlayers.size() > 0) {
            UUID newUUID = waitingPlayers.get(0);

            inGamePlayers.add(newUUID);
            if (waitingPlayersTeamsHashMap.get(newUUID) != null)
                inGamePlayersTeamsHashMap.put(newUUID, waitingPlayersTeamsHashMap.get(newUUID));

            waitingPlayers.remove(0);
            waitingPlayersTeamsHashMap.remove(newUUID);

            countdownState.restart();
        } else {
            broadcastMessageToInGamePlayers(ChatColor.RED + "A player just left.");

            waitingPlayers.addAll(inGamePlayers);
            waitingPlayersTeamsHashMap.putAll(inGamePlayersTeamsHashMap);
            inGamePlayers.clear();
            inGamePlayersTeamsHashMap.clear();

            updateState(GameState.RECRUITING);
        }
    }

    // Checks if both teams have at least one member
    // If not transfers players properly and notifies arena of updating state to RECRUITING
    private void continueOrCancelGame(Player leavingPlayer, Team leavingPlayersTeam) {
        if (inGamePlayersTeamsHashMap.containsValue(Team.RED) && inGamePlayersTeamsHashMap.containsValue(Team.BLUE)) {
            broadcastMessageToInGamePlayers(leavingPlayersTeam.getColor() + leavingPlayer.getName() +
                    " (" + leavingPlayersTeam.getDisplayName() + ") just left.");
        } else {
            broadcastMessageToInGamePlayers(ChatColor.RED + "Too many players left the game.");
            updateState(GameState.RECRUITING);
        }
    }

    // STATE CHANGE

    // Receiver of notices of state change
    // Handles consequences of each change of state
    // Generally by calling Arena functions
    // and then by proper scheduling and canceling of game states (which extend BukkitRunnable class)
    public void updateState(@NotNull GameState gameState) {
        if (gameState.equals(GameState.RECRUITING)) {
            if (this.gameState.equals(GameState.COUNTDOWN)) {
                countdownState.cancel();

                this.gameState = gameState;
            } else if (this.gameState.equals(GameState.LIVE)) {
                liveState.cancel();
                HandlerList.unregisterAll(liveState);
    
                inGamePlayers.forEach(uuid -> firstPersonShooter.getWeaponsManager().removeAllWeapons(
                        Objects.requireNonNull(Bukkit.getPlayer(uuid))));
                
                teleportPlayersOutOfArena();
                inGamePlayers.clear();
                inGamePlayersTeamsHashMap.clear();

                this.gameState = gameState;
                startCountdownIfPossible();
            } else
                System.out.println("Error 82724");
        } else if (gameState.equals(GameState.COUNTDOWN)) {
            this.gameState = gameState;
            countdownState = new CountdownState(firstPersonShooter);
        } else if (gameState.equals(GameState.LIVE)) {
            inGamePlayers.forEach(uuid -> {
                Objects.requireNonNull(Bukkit.getPlayer(uuid)).closeInventory();
                removeTeamChoosingItem(Objects.requireNonNull(Bukkit.getPlayer(uuid)));
            });
            assignInGamePlayersToTeams();
            firstPersonShooter.getWeaponsManager().giveWeaponsToPlayers(inGamePlayers);
            inGamePlayersTeamsHashMap.values().forEach(team -> broadcastTeamColoredMessageToTeam(team, "Run /fps weapons to choose a different weapon."));
            teleportPlayersToArena();

            this.gameState = gameState;
            liveState = new LiveState(firstPersonShooter, inGamePlayersTeamsHashMap);
        } else
            System.out.println("Error 51246");
    }

    // TEAMS

    public void setTeam(Player player, Team team) {
        removeTeam(player);

        if (waitingPlayers.contains(player.getUniqueId()))
            waitingPlayersTeamsHashMap.put(player.getUniqueId(), team);
        else if (inGamePlayers.contains(player.getUniqueId()))
            inGamePlayersTeamsHashMap.put(player.getUniqueId(), team);
    }

    private void removeTeam(Player player) {
        waitingPlayersTeamsHashMap.remove(player.getUniqueId());
        inGamePlayersTeamsHashMap.remove(player.getUniqueId());
    }

    public Team getTeam(Player player) {
        if (waitingPlayers.contains(player.getUniqueId()))
            return waitingPlayersTeamsHashMap.get(player.getUniqueId());
        else if (inGamePlayers.contains(player.getUniqueId()))
            return inGamePlayersTeamsHashMap.get(player.getUniqueId());
        else return null;
    }

    private void assignInGamePlayersToTeams() {
        inGamePlayers.forEach(uuid -> {
            if (!inGamePlayersTeamsHashMap.containsKey(uuid)) {
                Team smallestTeam = Team.values()[0];

                for (Team team : Team.values())
                    if (getPlayerCountOfInGameTeam(team) < getPlayerCountOfInGameTeam(smallestTeam))
                        smallestTeam = team;

                setTeam(Bukkit.getPlayer(uuid), smallestTeam);
                Objects.requireNonNull(Bukkit.getPlayer(uuid)).sendMessage(smallestTeam.getColor() +
                        "You are in Team " + smallestTeam.getDisplayName());
            }
        });
    }

    public boolean atLeastTwoCorrespondingTeamsNotFull(Player player) {
        int notFullTeams = 0;

        for (Team team : Team.values()) {
            if (waitingPlayers.contains(player.getUniqueId())) {
                if (!isWaitingTeamFull(team)) {
                    notFullTeams++;

                    if (notFullTeams == 2)
                        return true;
                }
            } else if (inGamePlayers.contains(player.getUniqueId())) {
                if (!isInGameTeamFull(team)) {
                    notFullTeams++;

                    if (notFullTeams == 2)
                        return true;
                }
            }
        }

        return false;
    }

    private int getPlayerCountOfInGameTeam(Team team) { return Collections.frequency(inGamePlayersTeamsHashMap.values(), team); }

    private int getPlayerCountOfWaitingTeam(Team team) { return Collections.frequency(waitingPlayersTeamsHashMap.values(), team); }

    public boolean isInGameTeamFull(Team team) {
        return getPlayerCountOfInGameTeam(team) >= (ConfigManager.getNumberOfPlayersInMatch() / Team.values().length);
    }

    public boolean isWaitingTeamFull(Team team) {
        return getPlayerCountOfWaitingTeam(team) >= (ConfigManager.getNumberOfPlayersInMatch() / Team.values().length);
    }
    
    private void giveTeamChoosingItem(Player player) {
        ItemStack itemStack = new ItemStack(Material.SKULL_BANNER_PATTERN);
        
        ItemMeta itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta).setDisplayName(ChatColor.BLACK + "Choose your team!");
        itemMeta.setLore(Collections.singletonList(""));
        itemMeta.setLocalizedName("Team Choosing Item");
        
        itemStack.setItemMeta(itemMeta);
        
        player.getInventory().addItem(itemStack);
    }
    
    private void removeTeamChoosingItem(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        
        for (ItemStack itemStack : playerInventory) {
            if (itemStack == null || itemStack.getItemMeta() == null || !itemStack.getItemMeta().hasLocalizedName())
                continue;
            
            if (itemStack.getItemMeta().getLocalizedName().equals("Team Choosing Item")) {
                playerInventory.remove(itemStack);
                break;
            }
        }
        
        ItemStack offHandItem = playerInventory.getItemInOffHand();
        
        if (offHandItem.getItemMeta() == null || !offHandItem.getItemMeta().hasLocalizedName())
            return;
        
        if (offHandItem.getItemMeta().getLocalizedName().equals("Team Choosing Item"))
            playerInventory.setItemInOffHand(null);
    }

    // GENERAL TOOLS

    public boolean containsPlayer(Player player) {
        return waitingPlayers.contains(player.getUniqueId()) || inGamePlayers.contains(player.getUniqueId());
    }

    public boolean containsPlayerInGame(Player player) { return inGamePlayers.contains(player.getUniqueId()); }

    public boolean containsPlayerWaiting(Player player) { return waitingPlayers.contains(player.getUniqueId()); }

    private void teleportPlayersToArena() { inGamePlayers.forEach(uuid -> teleportToTeamSpawnLocation(Bukkit.getPlayer(uuid))); }

    private void teleportPlayersOutOfArena() {
        inGamePlayers.forEach(uuid -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).teleport(ConfigManager.getLeaveLocation()));
    }

    public void teleportToTeamSpawnLocation(Player player) {
        if (getTeam(player).equals(Team.BLUE)) {
            player.teleport(ConfigManager.getBlueTeamSpawnLocation());
        } else if (getTeam(player).equals(Team.RED)) {
            player.teleport(ConfigManager.getRedTeamSpawnLocation());
        } else
            System.out.println("Error 29483");
    }

    public void broadcastTitleToInGamePlayers(String title, String subtitle) {
        if (inGamePlayers.size() > 0)
            inGamePlayers.forEach(uuid -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).
                    sendTitle(title, subtitle, 5, 20, 10));
    }

    public void broadcastTeamColoredTitleToInGamePlayers(String title, String subtitle) {
        if (inGamePlayers.size() > 0) {
            AtomicReference<ChatColor> chatColor = new AtomicReference<>();

            inGamePlayers.forEach(uuid -> {
                chatColor.set((inGamePlayersTeamsHashMap.containsKey(uuid)) ? inGamePlayersTeamsHashMap.get(uuid).getColor() : ChatColor.GREEN);

                Objects.requireNonNull(Bukkit.getPlayer(uuid)).
                        sendTitle(chatColor + title, chatColor + subtitle, 5, 20, 10);
            });
        }
    }

    public void broadcastTeamColoredActionBarToInGamePlayers(String message) {
        if (inGamePlayers.size() > 0) {
            AtomicReference<ChatColor> chatColor = new AtomicReference<>();

            inGamePlayers.forEach(uuid -> {
                chatColor.set((inGamePlayersTeamsHashMap.containsKey(uuid)) ? inGamePlayersTeamsHashMap.get(uuid).getColor() : ChatColor.GREEN);

                Objects.requireNonNull(Bukkit.getPlayer(uuid)).spigot()
                        .sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(chatColor + message));
            });
        }
    }

    public void broadcastTeamColoredActionBarToPlayersWaiting(String message) {
        if (waitingPlayers.size() > 0) {
            AtomicReference<ChatColor> chatColor = new AtomicReference<>();

            waitingPlayers.forEach(uuid -> {
                chatColor.set((inGamePlayersTeamsHashMap.containsKey(uuid)) ? inGamePlayersTeamsHashMap.get(uuid).getColor() : ChatColor.GREEN);

                Objects.requireNonNull(Bukkit.getPlayer(uuid)).spigot()
                        .sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(chatColor + message));
            });
        }
    }

    public void broadcastMessageToInGamePlayers(String message) {
        if (inGamePlayers.size() > 0)
            inGamePlayers.forEach(uuid -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).sendMessage(message));
    }

    public void broadcastTeamColoredMessageToTeam(Team targetTeam, String message) {
        inGamePlayersTeamsHashMap.forEach((uuid, team) -> {
            if (team == targetTeam)
                Objects.requireNonNull(Bukkit.getPlayer(uuid)).sendMessage(team.getColor() + message);
        });
    }

    // DATABASE

    public void writeIntoDB(HashMap<UUID, PlayerStatistics> playerStatisticsHashMap) {
        firstPersonShooter.writePlayerStatisticsIntoDB(playerStatisticsHashMap);
    }


    public GameState getCurrentGameState() { return gameState; }

    public int getWaitingPlayersCount() { return waitingPlayers.size(); }

    public LiveState getLiveState() { return liveState; }

}
