package com.voidcyan.client.mixin;

import com.voidcyan.client.FeatureModules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Menu Blur module: turning it off removes the background blur on every menu. */
@Mixin(Screen.class)
public class ScreenBlurMixin {
   @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
   private void voidcyan$noBlur(DrawContext context, CallbackInfo ci) {
      if (!FeatureModules.on[FeatureModules.MENUBLUR]) ci.cancel();
   }
}
