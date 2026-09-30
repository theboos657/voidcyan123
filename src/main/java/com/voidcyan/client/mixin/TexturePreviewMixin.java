package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Texture Studio 3D preview: swaps the entity's base skin for the live canvas texture.
 * Keyed on the render-state instance because GUI entities are drawn deferred (after
 * Screen.render returns), so a global on/off flag would already be cleared by then.
 */
@Mixin(LivingEntityRenderer.class)
public class TexturePreviewMixin {
   @Redirect(
      method = "getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)Lnet/minecraft/util/Identifier;"
      )
   )
   @SuppressWarnings({"rawtypes", "unchecked"})
   private Identifier voidcyan$previewTexture(LivingEntityRenderer renderer, LivingEntityRenderState state) {
      Identifier override = VoidCyanClient.previewBaseTextures.get(state);
      return override != null ? override : renderer.getTexture(state);
   }
}
