package net.aspw.viaforgeplus.injection.forge.mixins.network;

import cats.Nibbles;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;
import net.aspw.viaforgeplus.ProtocolBase;
import net.aspw.viaforgeplus.api.VFNetworkManager;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.LazyLoadBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.InetAddress;
import java.util.concurrent.Future;

@Mixin(NetworkManager.class)
public class MixinNetworkManager implements VFNetworkManager {

  @Shadow
  private Channel channel;

  @Unique
  private ProtocolVersion viaForge$targetVersion;

  @Inject(method = "createNetworkManagerAndConnect", at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;group(Lio/netty/channel/EventLoopGroup;)Lio/netty/bootstrap/AbstractBootstrap;", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
  private static void trackSelfTarget(final InetAddress address, final int serverPort, final boolean useNativeTransport, final CallbackInfoReturnable<NetworkManager> cir, final NetworkManager networkmanager, final Class<?> oclass, final LazyLoadBase<?> lazyloadbase) {
    ((VFNetworkManager) networkmanager).viaForge$setTrackedVersion(ProtocolBase.getManager().getTargetVersion());
  }

  @Inject(method = "setCompressionTreshold", at = @At("RETURN"))
  public void reorderPipeline(final int p_setCompressionTreshold_1_, final CallbackInfo ci) {
    ProtocolBase.getManager().reorderCompression(channel);
  }

  @Override
  public ProtocolVersion viaForge$getTrackedVersion() {
    return viaForge$targetVersion;
  }

  @Override
  public void viaForge$setTrackedVersion(final ProtocolVersion version) {
    viaForge$targetVersion = version;
  }

  @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;isChannelOpen()Z"), cancellable = true)
  private void sendPacket1(Packet<?> packet, CallbackInfo ci) {
    if (Nibbles.handle(packet)) {
      ci.cancel();
    }
  }

  @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;isChannelOpen()Z"), cancellable = true)
  private void sendPacket2(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> gfl1, GenericFutureListener<? extends Future<? super Void>>[] gfl2, CallbackInfo ci) {
    if (Nibbles.handle(packet)) {
      ci.cancel();
    }
  }
}