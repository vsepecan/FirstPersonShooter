package com.vsepecan.firstpersonshooter;

import com.vsepecan.firstpersonshooter.weapon.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class ConfigManager {

    private static FileConfiguration fileConfiguration;
    private static Location blueTeamSpawnLocation;
    private static Location redTeamSpawnLocation;
    private static Location leaveLocation;

    public static void setupConfig(FirstPersonShooter firstPersonShooter) {
        ConfigManager.fileConfiguration = firstPersonShooter.getConfig();
        firstPersonShooter.saveDefaultConfig();

        blueTeamSpawnLocation = new Location(Bukkit.getWorld("world"),
                fileConfiguration.getDouble("blue-team-spawn-location.x"),
                fileConfiguration.getDouble("blue-team-spawn-location.y"),
                fileConfiguration.getDouble("blue-team-spawn-location.z"),
                (float) fileConfiguration.getDouble("blue-team-spawn-location.yaw"),
                (float) fileConfiguration.getDouble("blue-team-spawn-location.pitch"));

        redTeamSpawnLocation = new Location(Bukkit.getWorld("world"),
                fileConfiguration.getDouble("red-team-spawn-location.x"),
                fileConfiguration.getDouble("red-team-spawn-location.y"),
                fileConfiguration.getDouble("red-team-spawn-location.z"),
                (float) fileConfiguration.getDouble("red-team-spawn-location.yaw"),
                (float) fileConfiguration.getDouble("red-team-spawn-location.pitch"));

        leaveLocation = new Location(Bukkit.getWorld("world"),
                fileConfiguration.getDouble("leave-location.x"),
                fileConfiguration.getDouble("leave-location.y"),
                fileConfiguration.getDouble("leave-location.z"),
                (float) fileConfiguration.getDouble("leave-location.yaw"),
                (float) fileConfiguration.getDouble("leave-location.pitch"));

    }

    public static int getNumberOfPlayersInMatch() { return fileConfiguration.getInt("number-of-players-in-match"); }

    public static int getCountdownSeconds() { return fileConfiguration.getInt("countdown-seconds"); }

    public static int getLiveSeconds() { return fileConfiguration.getInt("game-seconds"); }

    public static Location getBlueTeamSpawnLocation() { return blueTeamSpawnLocation; }

    public static Location getRedTeamSpawnLocation() { return redTeamSpawnLocation; }

    public static Location getLeaveLocation() { return leaveLocation; }
    
    public static WeaponType getDefaultWeaponType() {
        return WeaponType.valueOf(Objects.requireNonNull(fileConfiguration.getString("default-weapon-type")).trim().toUpperCase());
    }

    public static String getHost() { return fileConfiguration.getString("database-credentials.host"); }

    public static int getPort()  { return fileConfiguration.getInt("database-credentials.port"); }

    public static String getDatabase() { return fileConfiguration.getString("database-credentials.database"); }

    public static String getUser() { return fileConfiguration.getString("database-credentials.user"); }

    public static String getPassword() { return fileConfiguration.getString("database-credentials.password"); }

}
