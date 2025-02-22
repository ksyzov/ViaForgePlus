package net.aspw.viaforgeplus.injection.forge.mixins.accessors;

import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C02PacketUseEntity.class)
public interface C02PacketUseEntityAccessor {
    @Accessor(value = "hitVec")
    void setHitVec(Vec3 hitVec);
}
