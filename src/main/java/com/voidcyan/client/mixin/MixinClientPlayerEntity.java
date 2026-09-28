package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.DropPreventionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerEntity.class})
public class MixinClientPlayerEntity {
   @Inject(
      method = {"dropSelectedItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
      ItemStack stack = player.getMainHandStack();
      if (DropPreventionManager.shouldPreventDrop(stack)) {
         cir.setReturnValue(false);
         cir.cancel();
      }
   }

   @Inject(
      method = {"tickMovement"},
      at = {@At("HEAD")}
   )
   private void onTickMovement(CallbackInfo ci) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.options != null) {
         if (!VoidCyanClient.isToggleSprintEnabled) {
            VoidCyanClient.sprintToggled = false;
         } else {
            while (client.options.sprintKey.wasPressed()) {
               VoidCyanClient.sprintToggled = !VoidCyanClient.sprintToggled;
            }

            if (VoidCyanClient.sprintToggled) {
               client.options.sprintKey.setPressed(true);
            }
         }

         if (!VoidCyanClient.isToggleSneakEnabled) {
            VoidCyanClient.sneakToggled = false;
         } else {
            while (client.options.sneakKey.wasPressed()) {
               VoidCyanClient.sneakToggled = !VoidCyanClient.sneakToggled;
            }

            if (VoidCyanClient.sneakToggled) {
               client.options.sneakKey.setPressed(true);
            }
         }
      }
   }
}
