package de.vmoon.craftAttack.utils;

public class SpawnTeleporterData {
    public final boolean enabled;
    public final String regionWorld;
    public final int x1, y1, z1, x2, y2, z2;
    public final int delaySeconds;
    public final String teleportWorld;
    public final double teleportX, teleportY, teleportZ;
    public final String sound;

    public SpawnTeleporterData(boolean enabled, String regionWorld, int x1, int y1, int z1, int x2, int y2, int z2,
                               int delaySeconds, String teleportWorld, double teleportX, double teleportY, double teleportZ, String sound) {
        this.enabled = enabled;
        this.regionWorld = regionWorld;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.x2 = x2; this.y2 = y2; this.z2 = z2;
        this.delaySeconds = delaySeconds;
        this.teleportWorld = teleportWorld;
        this.teleportX = teleportX;
        this.teleportY = teleportY;
        this.teleportZ = teleportZ;
        this.sound = sound;
    }
}