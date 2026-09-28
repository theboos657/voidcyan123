package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.input.Scroller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Scroller.class})
public class MixinScroller {
   @Inject(
      method = {"scrollCycling"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void voidcyan$preventWrapping(double amount, int currentValue, int max, CallbackInfoReturnable<Integer> cir) {
      if (VoidCyanClient.disableHotbarLooping) {
         int direction = (int)Math.signum(amount);
         int newValue = currentValue - direction;
         if (newValue < 0 || newValue >= max) {
            cir.setReturnValue(currentValue);
         }
      }
   }
}
