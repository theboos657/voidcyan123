package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({Camera.class})
public abstract class CameraMixin {
   @ModifyVariable(
      method = {"setRotation"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private float modifyYaw(float yaw) {
      return VoidCyanClient.isFreelookEnabled && VoidCyanClient.freelookActive ? VoidCyanClient.freelookYaw : yaw;
   }

   @ModifyVariable(
      method = {"setRotation"},
      at = @At("HEAD"),
      ordinal = 1,
      argsOnly = true
   )
   private float modifyPitch(float pitch) {
      return VoidCyanClient.isFreelookEnabled && VoidCyanClient.freelookActive ? VoidCyanClient.freelookPitch : pitch;
   }
}
