package de.vmoon.craftAttack.utils;

import de.vmoon.craftAttack.CraftAttack;

public class PvPUtils {
    public static void togglePvP(boolean enable) {
        if (enable) {
            CraftAttack.getInstance().getPvpCmd().enablepvp();
        } else {
            CraftAttack.getInstance().getPvpCmd().disablepvp();
        }
    }
}