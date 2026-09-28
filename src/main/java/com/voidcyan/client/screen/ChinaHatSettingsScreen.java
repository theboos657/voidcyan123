package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class ChinaHatSettingsScreen extends Screen {
   private final Screen parent;
   private int draggingSlider = -1;

   public ChinaHatSettingsScreen(Screen parent) {
      super(Text.literal("China Hat Settings"));
      this.parent = parent;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
      super.render(context, mouseX, mouseY, deltaTicks);
      int w = 320;
      int h = 240;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      context.fill(x, y, x + w, y + h, Integer.MIN_VALUE);
      int accent = VoidCyanClient.getPrimaryColor();
      context.fill(x, y, x + w, y + 1, accent);
      context.fill(x, y + h - 1, x + w, y + h, accent);
      context.fill(x, y, x + 1, y + h, accent);
      context.fill(x + w - 1, y, x + w, y + h, accent);
      context.drawTextWithShadow(this.textRenderer, Text.literal("China Hat Settings"), x + 10, y + 10, -1);
      int previewColor = 0xFF000000
         | (VoidCyanClient.chinaHatRed & 0xFF) << 16
         | (VoidCyanClient.chinaHatGreen & 0xFF) << 8
         | VoidCyanClient.chinaHatBlue & 0xFF;
      context.fill(x + 10, y + 32, x + 26, y + 48, previewColor);
      context.fill(x + 10, y + 32, x + 26, y + 33, accent);
      context.fill(x + 10, y + 47, x + 26, y + 48, accent);
      context.fill(x + 10, y + 33, x + 11, y + 47, accent);
      context.fill(x + 25, y + 33, x + 26, y + 47, accent);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Hat Color"), x + 30, y + 35, -3355444);
      context.drawTextWithShadow(this.textRenderer, Text.literal("R"), x + 10, y + 60, -43691);
      context.drawTextWithShadow(this.textRenderer, Text.literal("G"), x + 10, y + 85, -11141291);
      context.drawTextWithShadow(this.textRenderer, Text.literal("B"), x + 10, y + 110, -11184641);
      this.drawCustomSlider(context, mouseX, mouseY, x + 30, y + 55, 260, 20, "Red", VoidCyanClient.chinaHatRed, 0.0F, 255.0F, 0);
      this.drawCustomSlider(context, mouseX, mouseY, x + 30, y + 80, 260, 20, "Green", VoidCyanClient.chinaHatGreen, 0.0F, 255.0F, 1);
      this.drawCustomSlider(context, mouseX, mouseY, x + 30, y + 105, 260, 20, "Blue", VoidCyanClient.chinaHatBlue, 0.0F, 255.0F, 2);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Size"), x + 10, y + 140, -3355444);
      this.drawCustomSlider(context, mouseX, mouseY, x + 30, y + 135, 260, 20, "Scale", VoidCyanClient.chinaHatSize, 0.5F, 3.0F, 3);
      String fillLabel = VoidCyanClient.chinaHatFilled ? "§aFilled: ON" : "§7Filled: OFF";
      this.drawCustomBtn(context, mouseX, mouseY, x + 10, y + 165, 130, 20, fillLabel, VoidCyanClient.chinaHatFilled);
      this.drawCustomBtn(context, mouseX, mouseY, x + w - 80, y + h - 30, 70, 20, "Done", false);
   }

   private void drawCustomBtn(DrawContext context, int mouseX, int mouseY, int btnX, int btnY, int btnW, int btnH, String label, boolean enabled) {
      boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
      int bgColor = !hovered && !enabled ? -13421773 : -11184811;
      context.fill(btnX, btnY, btnX + btnW, btnY + btnH, bgColor);
      context.drawTextWithShadow(
         this.textRenderer, Text.literal(label), btnX + btnW / 2 - this.textRenderer.getWidth(label) / 2, btnY + 6, enabled ? -11141291 : -1
      );
      if (hovered) {
         context.fill(btnX, btnY, btnX + btnW, btnY + 1, -1);
         context.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, -1);
         context.fill(btnX, btnY, btnX + 1, btnY + btnH, -1);
         context.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, -1);
      }
   }

   private void drawCustomSlider(
      DrawContext context, int mouseX, int mouseY, int sx, int sy, int sw, int sh, String label, float value, float min, float max, int id
   ) {
      float progress = (value - min) / (max - min);
      boolean hovered = mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + sh;
      context.fill(sx, sy, sx + sw, sy + sh, -14540254);
      int fillW = (int)(sw * progress);
      context.fill(sx, sy, sx + fillW, sy + sh, -12303292);
      String display;
      if (id == 3) {
         display = label + ": " + String.format("%.2f", value);
      } else {
         display = label + ": " + (int)value;
      }

      context.drawTextWithShadow(this.textRenderer, Text.literal(display), sx + 4, sy + 6, -5592406);
      boolean active = hovered || this.draggingSlider == id;
      if (active) {
         context.fill(sx, sy, sx + sw, sy + 1, -1);
         context.fill(sx, sy + sh - 1, sx + sw, sy + sh, -1);
      }
   }

   protected void init() {
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int w = 320;
      int h = 240;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      if (mouseX >= x + 30 && mouseX <= x + 290) {
         if (mouseY >= y + 55 && mouseY <= y + 75) {
            this.draggingSlider = 0;
            this.updateSlider(mouseX, x + 30, 260);
            return true;
         }

         if (mouseY >= y + 80 && mouseY <= y + 100) {
            this.draggingSlider = 1;
            this.updateSlider(mouseX, x + 30, 260);
            return true;
         }

         if (mouseY >= y + 105 && mouseY <= y + 125) {
            this.draggingSlider = 2;
            this.updateSlider(mouseX, x + 30, 260);
            return true;
         }

         if (mouseY >= y + 135 && mouseY <= y + 155) {
            this.draggingSlider = 3;
            this.updateSlider(mouseX, x + 30, 260);
            return true;
         }
      }

      if (mouseX >= x + 10 && mouseX <= x + 140 && mouseY >= y + 165 && mouseY <= y + 185) {
         VoidCyanClient.chinaHatFilled = !VoidCyanClient.chinaHatFilled;
         return true;
      } else if (mouseX >= x + w - 80 && mouseX <= x + w - 10 && mouseY >= y + h - 30 && mouseY <= y + h - 10) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.mouseClicked(click, doubled);
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      if (this.draggingSlider != -1) {
         int w = 320;
         int x = (this.width - w) / 2;
         this.updateSlider(click.x(), x + 30, 260);
         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   public boolean mouseReleased(Click click) {
      if (this.draggingSlider != -1) {
         this.draggingSlider = -1;
         VoidCyanClient.saveConfig();
         return true;
      } else {
         return super.mouseReleased(click);
      }
   }

   private void updateSlider(double mouseX, int sliderX, int sliderW) {
      float progress = Math.clamp((float)((mouseX - sliderX) / sliderW), 0.0F, 1.0F);
      if (this.draggingSlider == 0) {
         VoidCyanClient.chinaHatRed = (int)(progress * 255.0F);
      }

      if (this.draggingSlider == 1) {
         VoidCyanClient.chinaHatGreen = (int)(progress * 255.0F);
      }

      if (this.draggingSlider == 2) {
         VoidCyanClient.chinaHatBlue = (int)(progress * 255.0F);
      }

      if (this.draggingSlider == 3) {
         VoidCyanClient.chinaHatSize = 0.5F + progress * 2.5F;
      }
   }

   public void removed() {
      VoidCyanClient.saveConfig();
   }

   public boolean keyPressed(KeyInput input) {
      if (input.key() == 256) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(input);
      }
   }
}
