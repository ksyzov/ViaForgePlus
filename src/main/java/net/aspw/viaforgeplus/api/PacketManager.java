package net.aspw.viaforgeplus.api;

import net.aspw.viaforgeplus.event.EventTarget;
import net.aspw.viaforgeplus.event.Listenable;
import net.aspw.viaforgeplus.event.MotionEvent;
import net.aspw.viaforgeplus.network.MinecraftInstance;

public class PacketManager extends MinecraftInstance implements Listenable {

    public static float eyeHeight;
    public static float lastEyeHeight;

    @EventTarget
    public void onMotion(MotionEvent event) {
        mc.leftClickCounter = 0;

        lastEyeHeight = eyeHeight;

        if (mc.thePlayer.isSneaking()) {
            eyeHeight = 1.54f;
        } else {
            eyeHeight = 1.62f;
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
