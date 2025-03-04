package net.aspw.viaforgeplus.injection.forge.mixins.entity;

import net.aspw.viaforgeplus.api.McUpdatesHandler;
import net.aspw.viaforgeplus.api.PacketManager;
import net.aspw.viaforgeplus.api.ProtocolFixer;
import net.aspw.viaforgeplus.network.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {
    @Unique
    private final ItemStack[] viaForgePlus$mainInventory = new ItemStack[36];

    @Unique
    private final ItemStack[] viaForgePlus$armorInventory = new ItemStack[4];

    @Shadow
    public abstract boolean isPlayerSleeping();

    /**
     * @author As_pw
     * @reason Eye Height Fix
     */
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    public void getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if (((Entity) (Object) this) instanceof EntityPlayerSP) {
            final Minecraft mc = MinecraftInstance.mc;
            if (ProtocolFixer.newerThanOrEqualsTo1_13() && McUpdatesHandler.doingEyeRot) {
                cir.setReturnValue(
                    McUpdatesHandler.lastEyeHeight +
                    (McUpdatesHandler.eyeHeight - McUpdatesHandler.lastEyeHeight) *
                    mc.timer.renderPartialTicks
                );
            }

            if (this.isPlayerSleeping()) {
                cir.setReturnValue(0.2F);
            }

            cir.setReturnValue(
                PacketManager.lastEyeHeight +
                (PacketManager.eyeHeight - PacketManager.lastEyeHeight) *
                mc.timer.renderPartialTicks
            );
        }
    }

    /**
     * @author As_pw
     * @reason 1.16+ Item Drop Fix
     */
    @Inject(method = "dropItem", at = @At("HEAD"))
    private void dropItem(ItemStack p_dropItem_1_, boolean p_dropItem_2_, boolean p_dropItem_3_, CallbackInfoReturnable<EntityItem> cir) {
        for (int i = 0; i < this.viaForgePlus$mainInventory.length; ++i) {
            if (ProtocolFixer.newerThanOrEqualsTo1_16())
                MinecraftInstance.mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
            if (this.viaForgePlus$mainInventory[i] != null) {
                this.viaForgePlus$mainInventory[i] = null;
            }
        }

        for (int j = 0; j < this.viaForgePlus$armorInventory.length; ++j) {
            if (ProtocolFixer.newerThanOrEqualsTo1_16())
                MinecraftInstance.mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
            if (this.viaForgePlus$armorInventory[j] != null) {
                this.viaForgePlus$armorInventory[j] = null;
            }
        }
    }
}
