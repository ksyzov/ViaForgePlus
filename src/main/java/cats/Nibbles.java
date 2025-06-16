package cats;

import com.viaversion.viabackwards.protocol.v1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viabackwards.protocol.v1_17_1to1_17.Protocol1_17_1To1_17;
import com.viaversion.viabackwards.protocol.v1_17_1to1_17.storage.InventoryStateIds;
import com.viaversion.viabackwards.protocol.v1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viabackwards.protocol.v1_17to1_16_4.storage.PlayerLastCursorItem;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.BossBarStorage;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.PlayerPositionTracker;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Nibbles {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Queue<PacketWrapper> confirmations = new ConcurrentLinkedQueue<>();

    private static boolean swing = false;

    public static void init() {
        ProtocolManager protocolManager = Via.getManager().getProtocolManager();
        Protocol1_9To1_8 protocol1_9To1_8 = protocolManager.getProtocol(Protocol1_9To1_8.class);
        Protocol1_17To1_16_4 protocol1_17To1_16_4 = protocolManager.getProtocol(Protocol1_17To1_16_4.class);
        Protocol1_17_1To1_17 protocol1_17_1To1_17 = protocolManager.getProtocol(Protocol1_17_1To1_17.class);
        if (protocol1_9To1_8 == null || protocol1_17To1_16_4 == null || protocol1_17_1To1_17 == null) {
            return;
        }

        protocol1_9To1_8.registerClientbound(ClientboundPackets1_9.PLAYER_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE);
                map(Types.DOUBLE);
                map(Types.DOUBLE);
                map(Types.FLOAT);
                map(Types.FLOAT);
                map(Types.BYTE);
                handler(wrapper -> {
                    int id = wrapper.read(Types.VAR_INT);
                    PacketWrapper c = PacketWrapper.create(ServerboundPackets1_9.ACCEPT_TELEPORTATION, wrapper.user());
                    c.write(Types.VAR_INT, id);
                    confirmations.offer(c);

                    PlayerPositionTracker tracker = wrapper.user().get(PlayerPositionTracker.class);
                    if (tracker != null) {
                        tracker.setConfirmId(id);

                        byte flags = wrapper.get(Types.BYTE, 0);
                        double x = wrapper.get(Types.DOUBLE, 0);
                        double y = wrapper.get(Types.DOUBLE, 1);
                        double z = wrapper.get(Types.DOUBLE, 2);
                        float yaw = wrapper.get(Types.FLOAT, 0);
                        float pitch = wrapper.get(Types.FLOAT, 1);

                        wrapper.set(Types.BYTE, 0, (byte) 0);

                        if (flags != 0) {
                            if ((flags & 0x01) != 0) {
                                x += tracker.getPosX();
                                wrapper.set(Types.DOUBLE, 0, x);
                            }
                            if ((flags & 0x02) != 0) {
                                y += tracker.getPosY();
                                wrapper.set(Types.DOUBLE, 1, y);
                            }
                            if ((flags & 0x04) != 0) {
                                z += tracker.getPosZ();
                                wrapper.set(Types.DOUBLE, 2, z);
                            }
                            if ((flags & 0x08) != 0) {
                                yaw += tracker.getYaw();
                                wrapper.set(Types.FLOAT, 0, yaw);
                            }
                            if ((flags & 0x10) != 0) {
                                pitch += tracker.getPitch();
                                wrapper.set(Types.FLOAT, 1, pitch);
                            }
                        }

                        tracker.setPos(x, y, z);
                        tracker.setYaw(yaw);
                        tracker.setPitch(pitch);
                    }
                });
            }
        });

        protocol1_9To1_8.registerServerbound(ServerboundPackets1_8.MOVE_PLAYER_POS_ROT, wrapper -> {
            PacketWrapper c = confirmations.poll();
            if (c != null) {
                c.sendToServer(Protocol1_9To1_8.class);
            }

            double x = wrapper.passthrough(Types.DOUBLE);
            double y = wrapper.passthrough(Types.DOUBLE);
            double z = wrapper.passthrough(Types.DOUBLE);
            float yaw = wrapper.passthrough(Types.FLOAT);
            float pitch = wrapper.passthrough(Types.FLOAT);
            boolean onGround = wrapper.passthrough(Types.BOOLEAN);
            PlayerPositionTracker tracker = wrapper.user().get(PlayerPositionTracker.class);
            if (tracker != null) {
                tracker.sendAnimations();
                if (tracker.getConfirmId() != -1) {
                    if (
                        tracker.getPosX() == x &&
                        tracker.getPosY() == y &&
                        tracker.getPosZ() == z &&
                        tracker.getYaw() == yaw &&
                        tracker.getPitch() == pitch
                    ) {
                        tracker.setConfirmId(-1);
                    }
                } else {
                    tracker.setPos(x, y, z);
                    tracker.setYaw(yaw);
                    tracker.setPitch(pitch);
                    tracker.setOnGround(onGround);
                    BossBarStorage storage = wrapper.user().get(BossBarStorage.class);
                    if (storage != null) {
                        storage.updateLocation();
                    }
                }
            }
        });

        protocol1_17To1_16_4.registerServerbound(ServerboundPackets1_16_2.CONTAINER_CLICK, ServerboundPackets1_17.CONTAINER_CLICK, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE);
                handler(wrapper -> {
                    short slot = wrapper.passthrough(Types.SHORT);
                    byte button = wrapper.passthrough(Types.BYTE);
                    wrapper.read(Types.SHORT);
                    int mode = wrapper.passthrough(Types.VAR_INT);
                    Item clicked = protocol1_17To1_16_4.getItemRewriter().handleItemToServer(
                        wrapper.user(), wrapper.read(Types.ITEM1_13_2)
                    );

                    wrapper.write(Types.VAR_INT, 0);

                    PlayerLastCursorItem state = wrapper.user().get(PlayerLastCursorItem.class);
                    if (state == null) {
                        wrapper.write(Types.ITEM1_13_2, clicked);
                        return;
                    }

                    if (mode == 0 && button == 0 && clicked != null) {
                        state.setLastCursorItem(clicked);
                    } else if (mode == 0 && button == 1 && clicked != null) {
                        if (state.isSet()) {
                            state.setLastCursorItem(clicked);
                        } else {
                            state.setLastCursorItem(clicked, (clicked.amount() + 1) / 2);
                        }
                    } else if (!(mode == 5 && (slot == -999 && (button == 0 || button == 4) || (button == 1 || button == 5)))) {
                        state.setLastCursorItem(null);
                    }

                    Item carried = state.getLastCursorItem();
                    if (carried == null) {
                        wrapper.write(Types.ITEM1_13_2, clicked);
                    } else {
                        wrapper.write(Types.ITEM1_13_2, carried);
                    }
                });
            }
        }, true);

        protocol1_17_1To1_17.registerServerbound(ServerboundPackets1_17.CONTAINER_CLICK, ServerboundPackets1_17.CONTAINER_CLICK, wrapper -> {
            short containerId = wrapper.passthrough(Types.UNSIGNED_BYTE);
            int stateId = Integer.MAX_VALUE;
            InventoryStateIds state = wrapper.user().get(InventoryStateIds.class);
            if (state != null) {
                stateId = state.removeStateId(containerId);
                state.setStateId(containerId, stateId);
            }
            wrapper.write(Types.VAR_INT, stateId == Integer.MAX_VALUE ? 0 : stateId);
        }, true);
    }

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
            UserConnection uc = Via.getManager().getConnectionManager().getConnections().iterator().next();
            PacketWrapper s = PacketWrapper.create(ServerboundPackets1_9.SWING, uc);
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
                UserConnection uc = Via.getManager().getConnectionManager().getConnections().iterator().next();
                PacketWrapper s = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM_ON, uc);
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
