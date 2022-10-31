package com.vsepecan.firstpersonshooter.database;

public class PlayerStatistics {
    private boolean inWinningTeam;
    private int killCount;
    private int deathCount;

    public PlayerStatistics(boolean inWinningTeam, int killCount, int deathCount) {
        this.inWinningTeam = inWinningTeam;
        this.killCount = killCount;
        this.deathCount = deathCount;
    }

    public void setInWinningTeam(boolean inWinningTeam) { this.inWinningTeam = inWinningTeam; }

    public void addKill() { this.killCount++; }

    public void addDeath() { deathCount++; }

    public boolean isInWinningTeam() { return inWinningTeam; }

    public int getKillCount() { return killCount; }

    public int getDeathCount() { return deathCount; }
}
