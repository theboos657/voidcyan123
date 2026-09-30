package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.NameReplacer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
   @Inject(
      method = "renderLabelIfPresent",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderLabelIfPresent(
      S state, MatrixStack matrices, OrderedRenderCommandQueue commandQueue, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (state != null) {
         Text originalName = state.displayName;
         if (state instanceof PlayerEntityRenderState playerState && originalName == null) {
            originalName = playerState.playerName;
         }

         if (originalName != null) {
            if (VoidCyanClient.isChatModuleEnabled && VoidCyanClient.isHarmfulWordFilterEnabled) {
               String filtered = VoidCyanClient.filterHarmfulPlain(originalName.getString());
               if (!filtered.equals(originalName.getString())) {
                  originalName = Text.literal(filtered);
                  if (state instanceof PlayerEntityRenderState fps) fps.playerName = originalName;
                  state.displayName = originalName;
               }
            }

            if (VoidCyanClient.isPeerNickEnabled
               && state instanceof PlayerEntityRenderState peerState
               && MinecraftClient.getInstance().player != null
               && peerState.id != MinecraftClient.getInstance().player.getId()) {
               String nick = VoidCyanClient.peerNickFor(originalName.getString());
               if (nick != null) {
                  originalName = Text.literal(nick);
                  peerState.playerName = originalName;
                  state.displayName = originalName;
               }
            }

            if (state instanceof PlayerEntityRenderState playerState) {
               MinecraftClient client = MinecraftClient.getInstance();
               if (client.world != null) {
                  PlayerEntity player = VoidCyanClient.findTargetPlayer(playerState.id, originalName.getString());
                  if (player != null) {
                     String originalString = originalName.getString();
                     String replacedString = NameReplacer.getDisplayName(originalString);
                     if (playerState.playerName != null && !originalString.equals(replacedString)) {
                        playerState.playerName = Text.literal(replacedString);
                     }

                     if (state.displayName != null && !originalString.equals(replacedString)) {
                        state.displayName = Text.literal(replacedString);
                     }
                  }
               }
            }

            if (VoidCyanClient.isFriendGreenNameTagsEnabled && originalName != null) {
               String unformatted = originalName.getString();
               if (VoidCyanClient.mentionsFriend(unformatted)) {
                  if (state instanceof PlayerEntityRenderState playerStatex && playerStatex.playerName != null) {
                     playerStatex.playerName = Text.literal(unformatted).formatted(Formatting.GREEN);
                  }

                  if (state.displayName != null) {
                     state.displayName = Text.literal(unformatted).formatted(Formatting.GREEN);
                  }
               }
            }
         }
      }
   }
}
