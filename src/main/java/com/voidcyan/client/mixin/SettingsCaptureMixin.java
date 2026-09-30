package com.voidcyan.client.mixin;

import com.voidcyan.client.screen.BaseSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class SettingsCaptureMixin {
   @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
   private void voidcyan$captureSettings(Screen screen, CallbackInfo ci) {
      if (BaseSettingsScreen.captureActive && screen instanceof BaseSettingsScreen settings) {
         BaseSettingsScreen.captured = settings;
         ci.cancel();
      }
   }
}
