package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.EntityRenderStateAccessor;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public class EntityRendererStateMixin<T extends Entity, S extends EntityRenderState> {
   @Inject(
      method = {"updateRenderState"},
      at = {@At("RETURN")}
   )
   private void voidcyan$onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
      if (state instanceof EntityRenderStateAccessor accessor) {
         accessor.voidcyan$setEntityId(entity.getId());

         // Capture fall data for the item-physics falling spin: dropped items flip
         // proportionally to how far they have fallen (Vanilla wipes fallDistance on
         // landing, so grounded items get zero extra rotation).
         float tumble = 0.0F;
         if (VoidCyanClient.isItemPhysicsEnabled
               && VoidCyanClient.itemPhysicsFallFlips
               && entity instanceof ItemEntity item
               && !item.isOnGround()) {
            tumble = (float)(item.fallDistance * Math.PI * 2.0 * VoidCyanClient.itemPhysicsFlipsPerBlock);
         }
         accessor.voidcyan$setFallTumble(tumble);
      }
   }
}
