package net.aspw.viaforgeplus.injection.forge.mixins.entity;

import net.aspw.viaforgeplus.api.ProtocolFixer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public boolean noClip;

    @ModifyConstant(method = "getCollisionBorderSize", constant = @Constant(floatValue = 0.1F))
    private float onLivingUpdate(float constant) {
        return ProtocolFixer.newerThan1_8() ? 0.0F : 0.1F;
    }
}
