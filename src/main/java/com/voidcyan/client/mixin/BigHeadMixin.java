package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.mixin.accessor.PlayerEntityModelAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntityModel.class})
public class BigHeadMixin {
   @Unique
   private PlayerEntityRenderState currentState;

   @Inject(
      method = {"setAngles"},
      at = {@At("HEAD")}
   )
   private void onSetAnglesHead(PlayerEntityRenderState state, CallbackInfo ci) {
      this.currentState = state;
   }

   @Inject(
      method = {"setAngles"},
      at = {@At("TAIL")}
   )
   private void onSetAnglesTail(CallbackInfo ci) {
      if (VoidCyanClient.isBigHeadEnabled) {
         if (this.currentState != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
               PlayerEntity player = (PlayerEntity)client.world.getEntityById(this.currentState.id);
               if (player instanceof PlayerEntity) {
                  if (this.shouldScale(player)) {
                     float scale = Math.clamp(VoidCyanClient.bigHeadScale, 1.0F, 10.0F);
                     PlayerEntityModelAccessor accessor = (PlayerEntityModelAccessor)(Object)this;
                     ModelPart head = accessor.voidcyan$getHead();
                     head.xScale = scale;
                     head.yScale = scale;
                     head.zScale = scale;
                  }
               }
            }
         }
      }
   }

   @Unique
   private boolean shouldScale(PlayerEntity player) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null) {
         return false;
      } else {
         boolean isSelf = player == client.player;
         boolean isFriend = false;
         if (!isSelf && !VoidCyanClient.friends.isEmpty()) {
            String name = player.getName().getString();
            for (String friend : VoidCyanClient.friends) {
               if (name.equalsIgnoreCase(friend)) {
                  isFriend = true;
                  break;
               }
            }
         }
         boolean isOther = !isSelf && !isFriend;
         if (isSelf && !VoidCyanClient.bigHeadSelf) {
            return false;
         } else {
            return isFriend && !VoidCyanClient.bigHeadFriends ? false : !isOther || VoidCyanClient.bigHeadOthers;
         }
      }
   }
}
