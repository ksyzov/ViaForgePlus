package net.aspw.viaforgeplus.injection.forge.mixins.entity;

import net.aspw.viaforgeplus.api.ProtocolFixer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {
    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.005D))
    private double onLivingUpdate(double constant) {
        return ProtocolFixer.newerThan1_8() ? 0.003D : 0.005D;
    }
}
