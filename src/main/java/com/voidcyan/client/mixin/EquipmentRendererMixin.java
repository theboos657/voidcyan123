package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.EntityRenderStateAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {
   @Redirect(
      method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/command/RenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
      )
   )
   private <S> void voidcyan$redirectSubmitModel(
      RenderCommandQueue queue,
      Model<? super S> model,
      S state,
      MatrixStack matrices,
      RenderLayer renderLayer,
      int light,
      int overlay,
      int color,
      Sprite sprite,
      int outlineColor,
      ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand
   ) {
      int finalOverlay = overlay;
      if (state instanceof EntityRenderStateAccessor accessor) {
         int id = accessor.voidcyan$getEntityId();
         if (id != -1) {
            boolean isArmorStand = state instanceof ArmorStandEntityRenderState;
            boolean hurt = (state instanceof LivingEntityRenderState ls) && ls.hurt;
            MinecraftClient client = MinecraftClient.getInstance();
            boolean isSelf = client.player != null && id == client.player.getId();

            if (VoidCyanClient.shouldApplyTotemPopColor(id) && VoidCyanClient.totemPopColorApplyToArmor && (!isArmorStand || VoidCyanClient.totemPopColorApplyToArmorStands)) {
               VoidCyanClient.writeTotemPopColorToTexture();
               finalOverlay = OverlayTexture.packUv(OverlayTexture.getU(0.0F), 3);
            } else if (VoidCyanClient.shouldApplyDamageColor(id) && VoidCyanClient.damageColorApplyToArmor && (!isArmorStand || VoidCyanClient.damageColorApplyToArmorStands)) {
               VoidCyanClient.writeDamageColorToTexture();
               finalOverlay = OverlayTexture.packUv(OverlayTexture.getU(0.0F), 3);
            } else if (VoidCyanClient.shouldApplyHitColor(id) && VoidCyanClient.hitColorApplyToArmor && (!isArmorStand || VoidCyanClient.hitColorApplyToArmorStands)) {
               VoidCyanClient.writeHitColorToTexture();
               finalOverlay = OverlayTexture.packUv(OverlayTexture.getU(0.0F), 3);
            } else if (hurt && VoidCyanClient.hitColorApplyToArmor && (!isArmorStand || VoidCyanClient.hitColorApplyToArmorStands)) {
               if (VoidCyanClient.isHitColorEnabled && (!isSelf || VoidCyanClient.hitColorApplyToSelf)) {
                  VoidCyanClient.writeHitColorToTexture();
               } else {
                  VoidCyanClient.writeVanillaRedToTexture();
               }
               finalOverlay = OverlayTexture.packUv(OverlayTexture.getU(0.0F), 3);
            }
         }
      }
      RenderLayer finalLayer = renderLayer;
      if (VoidCyanClient.customPreviewTexture != null) {
         finalLayer = model.getLayer(VoidCyanClient.customPreviewTexture);
      }
      queue.submitModel(model, state, matrices, finalLayer, light, finalOverlay, color, sprite, outlineColor, crumblingOverlayCommand);
   }
}
