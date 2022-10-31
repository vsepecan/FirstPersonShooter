package com.vsepecan.firstpersonshooter;

import com.vsepecan.firstpersonshooter.command.FPSCommand;
import com.vsepecan.firstpersonshooter.command.LeaveFPSCommand;
import com.vsepecan.firstpersonshooter.database.DatabaseConnection;
import com.vsepecan.firstpersonshooter.database.PlayerStatistics;
import com.vsepecan.firstpersonshooter.weapon.WeaponsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class FirstPersonShooter extends JavaPlugin implements Listener {
	
	private Arena arena;
	private WeaponsManager weaponsManager;
	private DatabaseConnection databaseConnection;
	
	@Override
	public void onEnable() {
		ConfigManager.setupConfig(this);
		
		this.arena = new Arena(this);
		this.weaponsManager = new WeaponsManager(this);
		this.databaseConnection = new DatabaseConnection();
		
		Objects.requireNonNull(this.getCommand("fps")).setExecutor(new FPSCommand(this));
		Objects.requireNonNull(this.getCommand("leavefps")).setExecutor(new LeaveFPSCommand(this));
		
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
		Bukkit.getPluginManager().registerEvents(new OtherEventsListener(this), this);
	}
	
	@Override
	public void onDisable() { databaseConnection.disconnect(); }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) { e.getPlayer().performCommand("leavefps"); }
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		
		if (arena.containsPlayer(player) && !(e.getPlayer().isOp() || e.getPlayer().hasPermission("MBH-FPS.bypasscommand"))
				&& !(e.getMessage().contains("/fps") || e.getMessage().equals("/leavefps"))) {
			e.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You are still in FPS. Run /leavefps to leave it.");
		}
	}
	
	public void writePlayerStatisticsIntoDB(HashMap<UUID, PlayerStatistics> playerStatisticsHashMap) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		
		playerStatisticsHashMap.forEach((uuid, playerStatistics) -> {
			try (PreparedStatement ps = databaseConnection.getConnection().prepareStatement("INSERT INTO daily_player_statistics " +
					"VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
					"number_of_wins = number_of_wins + ?, " +
					"number_of_kills = number_of_kills + ?, " +
					"number_of_deaths = number_of_deaths + ?")) {
				
				ps.setString(1,  uuid.toString());
				ps.setDate(2, java.sql.Date.valueOf(format.format(date)));
				ps.setInt(3, (playerStatistics.isInWinningTeam()) ? 1 : 0);
				ps.setInt(4, playerStatistics.getKillCount());
				ps.setInt(5, playerStatistics.getDeathCount());
				
				ps.setInt(6, (playerStatistics.isInWinningTeam()) ? 1 : 0);
				ps.setInt(7, playerStatistics.getKillCount());
				ps.setInt(8, playerStatistics.getDeathCount());
				
				ps.executeUpdate();
			} catch (SQLException e) { throw new RuntimeException(e); }
		});
	}
	
	public Arena getArena() { return arena; }
	
	public WeaponsManager getWeaponsManager() { return weaponsManager; }
	
}
