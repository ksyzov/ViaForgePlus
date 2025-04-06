package cats;

import com.viaversion.viabackwards.protocol.v1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
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
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
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

        if (packet instanceof C09PacketHeldItemChange) {
            return false;
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

        if (packet instanceof C08PacketPlayerBlockPlacement && ProtocolFixer.newerThanOrEqualsTo1_11()) {
            C08PacketPlayerBlockPlacement c08 = (C08PacketPlayerBlockPlacement) packet;
            if (c08.getPlacedBlockDirection() != 255) {
                UserConnection c = Via.getManager().getConnectionManager().getConnections().iterator().next();
                PacketWrapper s = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM_ON, c);
                s.write(Types.BLOCK_POSITION1_8, new BlockPosition(
                    c08.getPosition().getX(),
                    c08.getPosition().getY(),
                    c08.getPosition().getZ()
                ));
                s.write(Types.VAR_INT, c08.getPlacedBlockDirection());
                s.write(Types.VAR_INT, 0);
                s.write(Types.FLOAT, c08.facingX);
                s.write(Types.FLOAT, c08.facingY);
                s.write(Types.FLOAT, c08.facingZ);
                s.sendToServer(Protocol1_11To1_10.class);
                return true;
            }
        }

        return false;
    }
}
