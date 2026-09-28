package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.NameProtect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class MixinInGameHud {
   @ModifyVariable(
      method = {"setTitle"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text injectNameProtectSetTitle(Text text) {
      if (NameProtect.INSTANCE.isEnabled() && text != null) {
         Text processed = NameProtect.processText(text);
         if (processed != null) {
            return processed;
         }
      }

      return text;
   }

   @ModifyVariable(
      method = {"setSubtitle"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text injectNameProtectSetSubtitle(Text text) {
      if (NameProtect.INSTANCE.isEnabled() && text != null) {
         Text processed = NameProtect.processText(text);
         if (processed != null) {
            return processed;
         }
      }

      return text;
   }

   @ModifyVariable(
      method = {"setOverlayMessage"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text injectNameProtectSetOverlayMessage(Text text) {
      if (NameProtect.INSTANCE.isEnabled() && text != null) {
         Text processed = NameProtect.processText(text);
         if (processed != null) {
            return processed;
         }
      }

      return text;
   }

   @Redirect(
      method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V"
      )
   )
   private void redirectDrawTextScoreboard(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
      if (NameProtect.INSTANCE.isEnabled() && text != null) {
         Text flatProcessed = NameProtect.processTextFlat(text);
         if (flatProcessed != null) {
            text = flatProcessed;
         } else {
            Text processed = NameProtect.processText(text);
            if (processed != null) {
               text = processed;
            }
         }
      }

      instance.drawText(textRenderer, text, x, y, color, shadow);
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (!client.getDebugHud().shouldShowDebugHud() && !client.options.hudHidden) {
         TextRenderer textRenderer = client.textRenderer;
         if (VoidCyanClient.isToggleSprintEnabled) {
            String sprintText = "[Sprinting (Toggled)]";
            int color = VoidCyanClient.sprintToggled ? -16711936 : -1;
            if (VoidCyanClient.sprintToggled || client.options.sprintKey.isPressed()) {
               context.drawTextWithShadow(textRenderer, Text.literal(sprintText), VoidCyanClient.toggleSprintX, VoidCyanClient.toggleSprintY, color);
            }
         }

         if (VoidCyanClient.isToggleSneakEnabled) {
            String sneakText = "[Sneaking (Toggled)]";
            int color = VoidCyanClient.sneakToggled ? -16711936 : -1;
            if (VoidCyanClient.sneakToggled || client.options.sneakKey.isPressed()) {
               context.drawTextWithShadow(textRenderer, Text.literal(sneakText), VoidCyanClient.toggleSneakX, VoidCyanClient.toggleSneakY, color);
            }
         }
      }
   }

   @Inject(
      method = {"renderStatusBars"},
      at = {@At("TAIL")}
   )
   private void renderSaturationOverlay(DrawContext context, CallbackInfo ci) {
      if (VoidCyanClient.isAppleSkinEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         PlayerEntity player = client.player;
         if (player != null) {
            Entity vehicle = player.getVehicle();
            if (!(vehicle instanceof LivingEntity) || !(((LivingEntity)vehicle).getMaxHealth() > 0.0F)) {
               float saturation = player.getHungerManager().getSaturationLevel();
               if (!(saturation <= 0.0F)) {
                  int right = context.getScaledWindowWidth() / 2 + 91;
                  int bottom = context.getScaledWindowHeight() - 39;
                  int fullShanks = (int)(saturation / 2.0F);
                  boolean halfShank = saturation % 2.0F >= 1.0F;
                  Identifier TEX = Identifier.of("voidcyan", "textures/gui/saturation_icons.png");
                  context.getMatrices().pushMatrix();

                  for (int i = 0; i < 10; i++) {
                     int x = right - i * 8 - 9;
                     if (i < fullShanks) {
                        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEX, x, bottom, 18.0F, 0.0F, 9, 9, 256, 256);
                     } else if (i == fullShanks && halfShank) {
                        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEX, x, bottom, 9.0F, 0.0F, 9, 9, 256, 256);
                     }
                  }

                  context.getMatrices().popMatrix();
               }
            }
         }
      }
   }

   @Inject(
      method = {"renderPortalOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderPortalOverlay(DrawContext context, float f, CallbackInfo ci) {
      if (!VoidCyanClient.isPortalEnabled) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
      if (!VoidCyanClient.isPumpkinEnabled && texture.getPath().contains("pumpkin")) {
         ci.cancel();
      }
   }
}
