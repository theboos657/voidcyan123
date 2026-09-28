package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.EnemyCrucialsHudRenderer;
import com.voidcyan.client.module.MmoArmorHudRenderer;
import com.voidcyan.client.module.TargetHudRenderer;
import com.voidcyan.client.module.TextHudRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class VoidCyanMenuScreen extends Screen {
   private VoidCyanMenuScreen.HudElement draggingElement = null;
   private VoidCyanMenuScreen.HudElement resizingElement = null;
   private int dragOffsetX = 0;
   private int dragOffsetY = 0;
   private int resizeAnchorX = 0;
   private int resizeAnchorY = 0;
   private float initialScale = 1.0F;
   private List<VoidCyanMenuScreen.HudElement> elements = new ArrayList<>();

   public VoidCyanMenuScreen() {
      super(Text.literal("Void Cyan Menu"));
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Inv HUD",
               () -> VoidCyanClient.isInvHudEnabled,
               () -> VoidCyanClient.invHudX,
               () -> VoidCyanClient.invHudY,
               () -> VoidCyanClient.invHudScale,
               x -> VoidCyanClient.invHudX = x,
               y -> VoidCyanClient.invHudY = y,
               s -> VoidCyanClient.invHudScale = s,
               () -> 176 + VoidCyanClient.invHudBorderThickness * 2,
               () -> VoidCyanClient.invHudShowHotbar ? 84 + VoidCyanClient.invHudBorderThickness * 2 : 58 + VoidCyanClient.invHudBorderThickness * 2,
               () -> this.client.setScreen(new InvHudSettingsScreen(this))
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "FPS Counter",
               () -> VoidCyanClient.isFpsCounterEnabled,
               () -> VoidCyanClient.fpsCounterX,
               () -> VoidCyanClient.fpsCounterY,
               () -> VoidCyanClient.fpsCounterScale,
               x -> VoidCyanClient.fpsCounterX = x,
               y -> VoidCyanClient.fpsCounterY = y,
               s -> VoidCyanClient.fpsCounterScale = s,
               () -> 80,
               () -> 20,
               null
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Coordinates",
               () -> VoidCyanClient.isCoordinatesEnabled,
               () -> VoidCyanClient.coordinatesX,
               () -> VoidCyanClient.coordinatesY,
               () -> VoidCyanClient.coordinatesScale,
               x -> VoidCyanClient.coordinatesX = x,
               y -> VoidCyanClient.coordinatesY = y,
               s -> VoidCyanClient.coordinatesScale = s,
               VoidCyanClient::getCoordinatesHudWidth,
               () -> 20,
               null
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Custom Text",
               () -> VoidCyanClient.isTextHudEnabled,
               () -> VoidCyanClient.textHudX,
               () -> VoidCyanClient.textHudY,
               () -> VoidCyanClient.textHudScale,
               x -> VoidCyanClient.textHudX = x,
               y -> VoidCyanClient.textHudY = y,
               s -> VoidCyanClient.textHudScale = s,
               TextHudRenderer::getWidth,
               TextHudRenderer::getHeight,
               null
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "CPS",
               () -> VoidCyanClient.isCpsEnabled,
               () -> VoidCyanClient.cpsX,
               () -> VoidCyanClient.cpsY,
               () -> VoidCyanClient.cpsScale,
               x -> VoidCyanClient.cpsX = x,
               y -> VoidCyanClient.cpsY = y,
               s -> VoidCyanClient.cpsScale = s,
               () -> 60,
               () -> 20,
               null
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Keystrokes",
               () -> VoidCyanClient.isKeystrokesEnabled,
               () -> VoidCyanClient.keystrokesX,
               () -> VoidCyanClient.keystrokesY,
               () -> VoidCyanClient.keystrokesScale,
               x -> VoidCyanClient.keystrokesX = x,
               y -> VoidCyanClient.keystrokesY = y,
               s -> VoidCyanClient.keystrokesScale = s,
               () -> 70,
               () -> VoidCyanClient.keystrokesShowMouse ? 70 : 45,
               () -> this.client.setScreen(new KeystrokesSettingsScreen(this))
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Compass",
               () -> VoidCyanClient.isCompassEnabled,
               () -> VoidCyanClient.compassX,
               () -> VoidCyanClient.compassY,
               () -> VoidCyanClient.compassScale,
               x -> VoidCyanClient.compassX = x,
               y -> VoidCyanClient.compassY = y,
               s -> VoidCyanClient.compassScale = s,
               () -> 200,
               () -> 30,
               null
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Armor Status",
               () -> VoidCyanClient.isArmorStatusEnabled,
               () -> VoidCyanClient.armorStatusX,
               () -> VoidCyanClient.armorStatusY,
               () -> VoidCyanClient.armorStatusScale,
               x -> VoidCyanClient.armorStatusX = x,
               y -> VoidCyanClient.armorStatusY = y,
               s -> VoidCyanClient.armorStatusScale = s,
               () -> VoidCyanClient.armorStatusOrientation == 1 ? 95 : 100,
               () -> VoidCyanClient.armorStatusOrientation == 1 ? 29 : 95,
               () -> this.client.setScreen(new ArmorStatusSettingsScreen(this))
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Target HUD",
               () -> VoidCyanClient.isTargetHudEnabled,
               () -> VoidCyanClient.targetHudX,
               () -> VoidCyanClient.targetHudY,
               () -> VoidCyanClient.targetHudScale,
               x -> VoidCyanClient.targetHudX = x,
               y -> VoidCyanClient.targetHudY = y,
               s -> VoidCyanClient.targetHudScale = s,
               () -> 160,
               TargetHudRenderer::getHudHeight,
               () -> this.client.setScreen(new TargetHudSettingsScreen(this))
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Enemy Crucials",
               () -> VoidCyanClient.isTargetHudEnabled && VoidCyanClient.targetHudShowEnemyCrucials,
               () -> VoidCyanClient.enemyCrucialsHudX,
               () -> VoidCyanClient.enemyCrucialsHudY,
               () -> VoidCyanClient.enemyCrucialsHudScale,
               x -> VoidCyanClient.enemyCrucialsHudX = x,
               y -> VoidCyanClient.enemyCrucialsHudY = y,
               s -> VoidCyanClient.enemyCrucialsHudScale = s,
               () -> 126,
               EnemyCrucialsHudRenderer::getHudHeight,
               null
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "MMO Armor Display",
               () -> VoidCyanClient.isTargetHudEnabled && VoidCyanClient.targetHudMmoArmorDisplay,
               () -> VoidCyanClient.targetHudMmoArmorX,
               () -> VoidCyanClient.targetHudMmoArmorY,
               () -> VoidCyanClient.targetHudMmoArmorScale,
               x -> VoidCyanClient.targetHudMmoArmorX = x,
               y -> VoidCyanClient.targetHudMmoArmorY = y,
               s -> VoidCyanClient.targetHudMmoArmorScale = s,
               () -> 62,
               () -> 126,
               null
            )
         );
      this.elements
         .add(
            new VoidCyanMenuScreen.HudElement(
               "Stats HUD",
               () -> VoidCyanClient.isStatsHudEnabled,
               () -> VoidCyanClient.statsHudX,
               () -> VoidCyanClient.statsHudY,
               () -> VoidCyanClient.statsHudScale,
               x -> VoidCyanClient.statsHudX = x,
               y -> VoidCyanClient.statsHudY = y,
               s -> VoidCyanClient.statsHudScale = s,
               () -> VoidCyanClient.getStatsHudWidth(),
               () -> VoidCyanClient.getStatsHudHeight(),
               () -> this.client.setScreen(new StatsHudSettingsScreen(this))
            )
         );
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, 1140850688);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      super.render(context, mouseX, mouseY, deltaTicks);
      int pc = VoidCyanClient.getPrimaryColor();
      context.drawTextWithShadow(this.textRenderer, Text.literal("Active Modules:"), 10, 10, pc);
      int listY = 25;
      if (VoidCyanClient.isInvHudEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- Inv HUD"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isFpsCounterEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- FPS Counter"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isCoordinatesEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- Coordinates"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isCpsEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- CPS"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isKeystrokesEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- Keystrokes"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isNickHiderEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- NickHider"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isCompassEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- Compass"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isArmorStatusEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- Armor Status"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isTargetHudEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- Target HUD"), 10, listY, pc);
         listY += 15;
      }

      if (VoidCyanClient.isStatsHudEnabled) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("- Stats HUD"), 10, listY, pc);
         listY += 15;
      }

      int buttonWidth = 150;
      int buttonHeight = 20;
      int btnX = (this.width - buttonWidth) / 2;
      int btnY = (this.height - buttonHeight) / 2;
      int hoverColor = mouseX >= btnX && mouseX <= btnX + buttonWidth && mouseY >= btnY && mouseY <= btnY + buttonHeight
         ? pc & 16777215 | -1879048192
         : Integer.MIN_VALUE;
      context.fill(btnX, btnY, btnX + buttonWidth, btnY + buttonHeight, hoverColor);
      context.fill(btnX - 2, btnY - 2, btnX + buttonWidth + 2, btnY - 1, pc);
      context.fill(btnX - 2, btnY + buttonHeight + 1, btnX + buttonWidth + 2, btnY + buttonHeight + 2, pc);
      context.fill(btnX - 2, btnY - 1, btnX - 1, btnY + buttonHeight + 1, pc);
      context.fill(btnX + buttonWidth + 1, btnY - 1, btnX + buttonWidth + 2, btnY + buttonHeight + 1, pc);
      Text btnText = Text.literal("Open Click GUI");
      int textWidth = this.textRenderer.getWidth(btnText);
      context.drawTextWithShadow(this.textRenderer, btnText, btnX + (buttonWidth - textWidth) / 2, btnY + (buttonHeight - 8) / 2, -1);
      if (VoidCyanClient.isInvHudEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.invHudX, VoidCyanClient.invHudY, VoidCyanClient.invHudScale);
         VoidCyanClient.renderInvHud(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isFpsCounterEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.fpsCounterX, VoidCyanClient.fpsCounterY, VoidCyanClient.fpsCounterScale);
         VoidCyanClient.renderFpsCounter(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isCoordinatesEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.coordinatesX, VoidCyanClient.coordinatesY, VoidCyanClient.coordinatesScale);
         VoidCyanClient.renderCoordinates(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isTextHudEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.textHudX, VoidCyanClient.textHudY, VoidCyanClient.textHudScale);
         TextHudRenderer.render(context, 0, 0);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isCpsEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.cpsX, VoidCyanClient.cpsY, VoidCyanClient.cpsScale);
         VoidCyanClient.renderCps(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isKeystrokesEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.keystrokesX, VoidCyanClient.keystrokesY, VoidCyanClient.keystrokesScale);
         VoidCyanClient.renderKeystrokes(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isCompassEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.compassX, VoidCyanClient.compassY, VoidCyanClient.compassScale);
         VoidCyanClient.renderCompass(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isArmorStatusEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.armorStatusX, VoidCyanClient.armorStatusY, VoidCyanClient.armorStatusScale);
         VoidCyanClient.renderArmorStatus(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isTargetHudEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.targetHudX, VoidCyanClient.targetHudY, VoidCyanClient.targetHudScale);
         TargetHudRenderer.render(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isTargetHudEnabled && VoidCyanClient.targetHudShowEnemyCrucials) {
         VoidCyanClient.applyScale(context, VoidCyanClient.enemyCrucialsHudX, VoidCyanClient.enemyCrucialsHudY, VoidCyanClient.enemyCrucialsHudScale);
         EnemyCrucialsHudRenderer.render(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isTargetHudEnabled && VoidCyanClient.targetHudMmoArmorDisplay) {
         VoidCyanClient.applyScale(context, VoidCyanClient.targetHudMmoArmorX, VoidCyanClient.targetHudMmoArmorY, VoidCyanClient.targetHudMmoArmorScale);
         MmoArmorHudRenderer.render(context);
         VoidCyanClient.resetScale(context);
      }

      if (VoidCyanClient.isStatsHudEnabled) {
         VoidCyanClient.applyScale(context, VoidCyanClient.statsHudX, VoidCyanClient.statsHudY, VoidCyanClient.statsHudScale);
         VoidCyanClient.renderStatsHud(context);
         VoidCyanClient.resetScale(context);
      }

      for (VoidCyanMenuScreen.HudElement el : this.elements) {
         if (el.isEnabled.get()) {
            int x = el.getX.get();
            int y = el.getY.get();
            float scale = el.getScale.get();
            int width = el.getWidth.get();
            int height = el.getHeight.get();
            int scaledWidth = (int)(width * scale);
            int scaledHeight = (int)(height * scale);
            context.fill(x, y, x + scaledWidth, y + 1, pc);
            context.fill(x, y + scaledHeight - 1, x + scaledWidth, y + scaledHeight, pc);
            context.fill(x, y, x + 1, y + scaledHeight, pc);
            context.fill(x + scaledWidth - 1, y, x + scaledWidth, y + scaledHeight, pc);
            context.fill(x, y, x + scaledWidth, y + scaledHeight, pc & 16777215 | 1073741824);
            int dotSize = 4;
            context.fill(x - dotSize, y - dotSize, x + dotSize, y + dotSize, -1);
            context.fill(x + scaledWidth - dotSize, y - dotSize, x + scaledWidth + dotSize, y + dotSize, -1);
            context.fill(x - dotSize, y + scaledHeight - dotSize, x + dotSize, y + scaledHeight + dotSize, -1);
            context.fill(x + scaledWidth - dotSize, y + scaledHeight - dotSize, x + scaledWidth + dotSize, y + scaledHeight + dotSize, -1);
            context.drawTextWithShadow(
               this.textRenderer, Text.literal(el.name), x + scaledWidth / 2 - this.textRenderer.getWidth(el.name) / 2, y + scaledHeight / 2 - 4, -1
            );
         }
      }
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      int buttonWidth = 150;
      int buttonHeight = 20;
      int btnX = (this.width - buttonWidth) / 2;
      int btnY = (this.height - buttonHeight) / 2;
      if (button == 0 && mouseX >= btnX && mouseX <= btnX + buttonWidth && mouseY >= btnY && mouseY <= btnY + buttonHeight) {
         return true;
      } else {
         for (int i = this.elements.size() - 1; i >= 0; i--) {
            VoidCyanMenuScreen.HudElement el = this.elements.get(i);
            if (el.isEnabled.get()) {
               int x = el.getX.get();
               int y = el.getY.get();
               float scale = el.getScale.get();
               int scaledWidth = (int)(el.getWidth.get().intValue() * scale);
               int scaledHeight = (int)(el.getHeight.get().intValue() * scale);
               int dotSize = 4;
               if (button == 0) {
                  if (this.isHoveringDot(mouseX, mouseY, x, y, scaledWidth, scaledHeight, dotSize)) {
                     this.resizingElement = el;
                     this.resizeAnchorX = (int)mouseX;
                     this.resizeAnchorY = (int)mouseY;
                     this.initialScale = scale;
                     return true;
                  }

                  if (mouseX >= x && mouseX <= x + scaledWidth && mouseY >= y && mouseY <= y + scaledHeight) {
                     this.draggingElement = el;
                     this.dragOffsetX = (int)(mouseX - x);
                     this.dragOffsetY = (int)(mouseY - y);
                     return true;
                  }
               } else if (button == 1 && mouseX >= x && mouseX <= x + scaledWidth && mouseY >= y && mouseY <= y + scaledHeight && el.onRightClick != null) {
                  el.onRightClick.run();
                  return true;
               }
            }
         }

         return super.mouseClicked(click, doubled);
      }
   }

   private boolean isHoveringDot(double mx, double my, int x, int y, int w, int h, int dotSize) {
      if (mx >= x - dotSize && mx <= x + dotSize && my >= y - dotSize && my <= y + dotSize) {
         return true;
      } else if (mx >= x + w - dotSize && mx <= x + w + dotSize && my >= y - dotSize && my <= y + dotSize) {
         return true;
      } else {
         return mx >= x - dotSize && mx <= x + dotSize && my >= y + h - dotSize && my <= y + h + dotSize
            ? true
            : mx >= x + w - dotSize && mx <= x + w + dotSize && my >= y + h - dotSize && my <= y + h + dotSize;
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      double mouseX = click.x();
      double mouseY = click.y();
      if (this.draggingElement != null) {
         this.draggingElement.setX.accept((int)(mouseX - this.dragOffsetX));
         this.draggingElement.setY.accept((int)(mouseY - this.dragOffsetY));
         return true;
      } else if (this.resizingElement != null) {
         int diffX = (int)(mouseX - this.resizeAnchorX);
         float scaleDelta = diffX * 0.01F;
         float newScale = Math.max(0.1F, Math.min(5.0F, this.initialScale + scaleDelta));
         this.resizingElement.setScale.accept(newScale);
         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   public boolean mouseReleased(Click click) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      if (this.draggingElement == null && this.resizingElement == null) {
         VoidCyanClient.saveConfig();
         return super.mouseReleased(click);
      } else {
         VoidCyanClient.saveConfig();
         this.draggingElement = null;
         this.resizingElement = null;
         return true;
      }
   }

   public void close() {
      VoidCyanClient.saveConfig();
      super.close();
   }

   private static class HudElement {
      String name;
      Supplier<Boolean> isEnabled;
      Supplier<Integer> getX;
      Supplier<Integer> getY;
      Supplier<Float> getScale;
      Consumer<Integer> setX;
      Consumer<Integer> setY;
      Consumer<Float> setScale;
      Supplier<Integer> getWidth;
      Supplier<Integer> getHeight;
      Runnable onRightClick;

      public HudElement(
         String name,
         Supplier<Boolean> isEnabled,
         Supplier<Integer> getX,
         Supplier<Integer> getY,
         Supplier<Float> getScale,
         Consumer<Integer> setX,
         Consumer<Integer> setY,
         Consumer<Float> setScale,
         Supplier<Integer> getWidth,
         Supplier<Integer> getHeight,
         Runnable onRightClick
      ) {
         this.name = name;
         this.isEnabled = isEnabled;
         this.getX = getX;
         this.getY = getY;
         this.getScale = getScale;
         this.setX = setX;
         this.setY = setY;
         this.setScale = setScale;
         this.getWidth = getWidth;
         this.getHeight = getHeight;
         this.onRightClick = onRightClick;
      }
   }
}
