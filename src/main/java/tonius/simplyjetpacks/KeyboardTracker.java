package tonius.simplyjetpacks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;

public class KeyboardTracker {

    private static Map<EntityPlayer, Boolean> jumpKeyState = new HashMap();
    private static Map<EntityPlayer, Boolean> forwardKeyState = new HashMap();

    public static boolean isJumpKeyDown(EntityPlayer player) {
        return jumpKeyState.containsKey(player) && jumpKeyState.get(player).booleanValue();
    }

    public static boolean isForwardKeyDown(EntityPlayer player) {
        return forwardKeyState.containsKey(player) && forwardKeyState.get(player).booleanValue();
    }

    public static void processKeyUpdate(EntityPlayer player, boolean keyJump, boolean keyForward) {
        jumpKeyState.put(player, keyJump);
        forwardKeyState.put(player, keyForward);
    }

}
