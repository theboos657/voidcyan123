package com.voidcyan.client.mixin.network;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ScoreboardDisplayS2CPacket.class})
public class MixinScoreboardDisplayS2CPacket {
   @Inject(
      method = {"getName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetName(CallbackInfoReturnable<String> cir) {
      String original = (String)cir.getReturnValue();
      if (original != null) {
         cir.setReturnValue(NameProtect.INSTANCE.replace(original));
      }
   }
}
