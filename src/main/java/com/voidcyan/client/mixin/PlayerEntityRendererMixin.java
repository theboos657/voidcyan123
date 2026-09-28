package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.NameReplacer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntityRenderer.class})
public abstract class PlayerEntityRendererMixin {
   @Inject(
      method = {"renderLabelIfPresent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderLabelIfPresent(
      PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue commandQueue, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (state != null) {
         if (VoidCyanClient.hideInvisNametags && state.invisible) {
            ci.cancel();
            return;
         }

         if (VoidCyanClient.isNickHiderEnabled) {
            Text originalName = state.playerName != null ? state.playerName : state.displayName;
            if (originalName != null) {
               String originalString = originalName.getString();
               String replacedString = NameReplacer.getDisplayName(originalString);
               if (!originalString.equals(replacedString)) {
                  MutableText newName = Text.literal(replacedString);
                  if (VoidCyanClient.nickHiderUseCustomColor) {
                     newName.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(VoidCyanClient.nickHiderColor)));
                  }

                  if (state.playerName != null) {
                     state.playerName = newName;
                  }
                  if (state.displayName != null) {
                     state.displayName = newName;
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = {"updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"},
      at = {@At("TAIL")},
      require = 0
   )
   private void onUpdateRenderState(net.minecraft.entity.PlayerLikeEntity entity, PlayerEntityRenderState state, float f, CallbackInfo ci) {
      if (VoidCyanClient.hideInvisNametags && (state.invisible || entity.isInvisible())) {
         state.playerName = null;
         state.displayName = null;
         state.nameLabelPos = null;
         return;
      }

      if (VoidCyanClient.freelookActive && entity == MinecraftClient.getInstance().player) {
         if (state.displayName == null) {
            state.displayName = entity.getDisplayName();
         }

         if (state.nameLabelPos == null) {
            state.nameLabelPos = new Vec3d(0.0, entity.getHeight() + 0.3, 0.0);
         }
      }
   }
}
