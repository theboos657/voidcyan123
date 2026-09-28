package com.voidcyan.client.mixin;

import com.voidcyan.client.util.ObjModelParser;
import com.voidcyan.client.util.PlayerModelManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Renders the user's imported .obj player model on their own body — in-world AND in
 * every GUI entity preview (the Texture Studio preview routes through this same
 * renderer with a real OrderedRenderCommandQueue).
 *
 * Two hooks on LivingEntityRenderer:
 *  - isVisible: returns false for the local player while a custom model is active,
 *    which (combined with state.invisible=true set in EntityRendererStateMixin) makes
 *    vanilla skip the base skin model entirely while feature renderers (armor, held
 *    items) still run — verified against vanilla bytecode.
 *  - render RETURN: submits the OBJ triangles via queue.submitCustom. The vanilla
 *    model frame is popped by then, so the mesh re-applies the body-yaw rotation to
 *    follow the player.
 */
@Mixin(LivingEntityRenderer.class)
public class PlayerModelRenderMixin {


   @Inject(
      method = "isVisible(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)Z",
      at = @At("HEAD"),
      cancellable = true
   )
   private void voidcyan$hideVanillaSkinForCustomModel(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null) return;
      if (state instanceof PlayerEntityRenderState playerState
            && PlayerModelManager.isFeatureEnabled()
            && PlayerModelManager.hasModel()
            && playerState.id == client.player.getId()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      at = @At("RETURN")
   )
   private void voidcyan$submitCustomPlayerModel(
      LivingEntityRenderState state,
      MatrixStack matrices,
      OrderedRenderCommandQueue queue,
      CameraRenderState cameraRenderState,
      CallbackInfo ci
   ) {
      if (!PlayerModelManager.isFeatureEnabled()) return;
      if (!(state instanceof PlayerEntityRenderState playerState)) return;
      ObjModelParser.Result model = PlayerModelManager.getModel();
      if (model == null) return;

      MinecraftClient client = MinecraftClient.getInstance();
      RenderLayer layer = PlayerModelManager.hasTexture()
         ? PlayerModelManager.modelLayer()
         : PlayerModelManager.fallbackLayer();
      if (layer == null) return;

      final LivingEntityRenderState fState = state;
      matrices.push();
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - fState.bodyYaw));
      queue.submitCustom(matrices, layer, (entry, vertexConsumer) -> {
         try {
            int light = fState.light;
            int[] indices = {0, 1, 2, 2};
            for (ObjModelParser.Tri tri : model.tris) {
               float[] v = tri.xyz;
               float[] t = tri.uv;
               float[] n = tri.normal;
               for (int k : indices) {
                  vertexConsumer.vertex(entry, v[k * 3], v[k * 3 + 1], v[k * 3 + 2])
                                .color(255, 255, 255, 255)
                                .texture(t[k * 2], t[k * 2 + 1])
                                .overlay(OverlayTexture.DEFAULT_UV)
                                .light(light)
                                .normal(entry, n[0], n[1], n[2]);
               }
            }
         } catch (RuntimeException e) {
            // Never let a bad/huge custom model take the client down; drop it instead.
            PlayerModelManager.clearModel();
         }
      });
      matrices.pop();
   }
}
