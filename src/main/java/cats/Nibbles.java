package cats;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import net.aspw.viaforgeplus.api.ProtocolFixer;
import net.aspw.viaforgeplus.injection.forge.mixins.accessors.C02PacketUseEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.Vec3;

public class Nibbles {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static boolean swing = false;

    public static boolean handle(Packet<?> packet) {
        if (!ProtocolFixer.newerThan1_8()) {
            return false;
        }

        if (packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity c02 = (C02PacketUseEntity) packet;
            switch (((C02PacketUseEntity) packet).getAction()) {
                case ATTACK: {
                    return false;
                }
                case INTERACT_AT: {
                    Vec3 hitVec = c02.getHitVec();
                    Entity entity = c02.getEntityFromWorld(mc.theWorld);

                    if (hitVec == null || entity == null) {
                        break;
                    }

                    if (
                        entity instanceof EntityItemFrame ||
                        entity instanceof EntityFireball
                    ) {
                        break;
                    }

                    float w = entity.width;
                    float h = entity.height;
                    ((C02PacketUseEntityAccessor) packet).setHitVec(new Vec3(
                        Math.max(-(w / 2.0D), Math.min(w / 2.0D, hitVec.xCoord)),
                        Math.max(0.0D, Math.min(h, hitVec.yCoord)),
                        Math.max(-(w / 2.0D), Math.min(w / 2.0D, hitVec.zCoord))
                    ));
                    break;
                }
            }
        }

        if (swing) {
            UserConnection c = Via.getManager().getConnectionManager().getConnections().iterator().next();
            PacketWrapper s = PacketWrapper.create(ServerboundPackets1_9.SWING, c);
            s.write(Types.VAR_INT, 0);
            s.sendToServer(Protocol1_9To1_8.class);
            swing = false;
        }

        if (packet instanceof C0APacketAnimation) {
            swing = true;
            return true;
        }

        // if (packet instanceof C08PacketPlayerBlockPlacement) {
        //     C08PacketPlayerBlockPlacement c08 = (C08PacketPlayerBlockPlacement) packet;
        // }

        return false;
    }
}
