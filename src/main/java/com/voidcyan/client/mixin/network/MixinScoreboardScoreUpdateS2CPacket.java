package com.voidcyan.client.mixin.network;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ScoreboardScoreUpdateS2CPacket.class})
public class MixinScoreboardScoreUpdateS2CPacket {
   @Inject(
      method = {"scoreHolderName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onScoreHolderName(CallbackInfoReturnable<String> cir) {
      String original = (String)cir.getReturnValue();
      if (original != null) {
         cir.setReturnValue(NameProtect.INSTANCE.replace(original));
      }
   }
}
