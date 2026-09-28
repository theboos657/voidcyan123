package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Properties.class})
public class MixinClientWorld {
   @Inject(
      method = {"getTimeOfDay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
      if (VoidCyanClient.isTimeChangerEnabled) {
         cir.setReturnValue(VoidCyanClient.customTime);
      }
   }
}
