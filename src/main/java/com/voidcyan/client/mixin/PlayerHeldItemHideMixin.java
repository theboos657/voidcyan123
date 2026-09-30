package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.PlayerModelManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides held items on players who are drawn with the custom 3D model. */
@Mixin(ArmedEntityRenderState.class)
public class PlayerHeldItemHideMixin {
   @Inject(method = "updateRenderState", at = @At("RETURN"))
   private static void voidcyan$hideHeldItems(LivingEntity entity, ArmedEntityRenderState state, ItemModelManager itemModelManager, float tickDelta, CallbackInfo ci) {
      if (!(state instanceof PlayerEntityRenderState)) return;
      if (!VoidCyanClient.playerModelHideHeld || !PlayerModelManager.isFeatureEnabled() || !PlayerModelManager.hasModel()) return;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || (entity.getId() != client.player.getId() && !VoidCyanClient.playerModelOthers)) return;
      state.rightHandItemState.clear();
      state.leftHandItemState.clear();
   }
}
