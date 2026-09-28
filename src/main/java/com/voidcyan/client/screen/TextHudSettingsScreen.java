package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class TextHudSettingsScreen extends Screen {
   private final Screen parent;
   private long openTime;
   private TextFieldWidget textWidget;
   private int colorPickerRText;
   private int colorPickerGText;
   private int colorPickerBText;
   private int colorPickerRBg;
   private int colorPickerGBg;
   private int colorPickerBBg;
   private int alphaPickerBg;
   private String draggingSlider = null;
   private ColorPickerModal colorModal = null;

   public TextHudSettingsScreen(Screen parent) {
      super(Text.literal("Text HUD Settings"));
      this.parent = parent;
      int textC = VoidCyanClient.textHudTextColor;
      this.colorPickerRText = textC >> 16 & 0xFF;
      this.colorPickerGText = textC >> 8 & 0xFF;
      this.colorPickerBText = textC & 0xFF;
      int bgC = VoidCyanClient.textHudBgColor;
      this.colorPickerRBg = bgC >> 16 & 0xFF;
      this.colorPickerGBg = bgC >> 8 & 0xFF;
      this.colorPickerBBg = bgC & 0xFF;
      this.alphaPickerBg = VoidCyanClient.textHudBgAlpha;
   }

   protected void init() {
      super.init();
      this.openTime = System.currentTimeMillis();
      int panelW = 320;
      int px = (this.width - panelW) / 2;
      int py = (this.height - 240) / 2;
      this.textWidget = new TextFieldWidget(this.textRenderer, px + 10, py + 40, panelW - 20, 20, Text.literal("Custom Text"));
      this.textWidget.setMaxLength(256);
      this.textWidget.setText(VoidCyanClient.textHudString);
      this.textWidget.setChangedListener(text -> {
         VoidCyanClient.textHudString = text;
         VoidCyanClient.saveConfig();
      });
      this.addDrawableChild(this.textWidget);
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      int prim = VoidCyanClient.getPrimaryColor();
      int primAlpha30 = prim & 16777215 | 805306368;
      int panelW = 320;
      int panelH = 240;
      int px = (this.width - panelW) / 2;
      int py = (this.height - panelH) / 2;
      context.fill(px, py, px + panelW, py + panelH, -535752424);
      context.fill(px, py, px + panelW, py + 1, prim);
      context.fill(px, py + panelH - 1, px + panelW, py + panelH, prim);
      context.fill(px, py + 1, px + 1, py + panelH - 1, prim);
      context.fill(px + panelW - 1, py + 1, px + panelW, py + panelH - 1, prim);
      context.fill(px + 1, py + 1, px + panelW - 1, py + 26, 1342177280);
      context.drawTextWithShadow(this.textRenderer, Text.literal("▦ Text HUD Settings"), px + 10, py + 8, prim);
      context.fill(px + 1, py + 26, px + panelW - 1, py + 27, primAlpha30);
      super.render(context, mouseX, mouseY, deltaTicks);
      int cy = py + 70;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Text Color"), px + 10, cy, -1);
      this.renderColorSwatch(context, mouseX, mouseY, px + 80, cy - 2, 0xFF000000 | this.colorPickerRText << 16 | this.colorPickerGText << 8 | this.colorPickerBText, "text");
      cy += 25;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Bg Color"), px + 10, cy, -1);
      this.renderColorSwatch(context, mouseX, mouseY, px + 80, cy - 2, 0xFF000000 | this.colorPickerRBg << 16 | this.colorPickerGBg << 8 | this.colorPickerBBg, "bg");
      cy += 25;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Bg Alpha"), px + 10, cy, -1);
      this.renderSlider(context, mouseX, mouseY, px + 80, cy, 200, "Alpha", this.alphaPickerBg, 11184810);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Press ESC to return and save"), px + 10, py + panelH - 14, prim);

      if (this.colorModal != null) {
         this.colorModal.render(context, mouseX, mouseY, this.width, this.height);
         if (!this.colorModal.isOpen()) this.colorModal = null;
      }
   }

   private void renderColorSwatch(DrawContext context, int mouseX, int mouseY, int x, int y, int argb, String id) {
      boolean hov = mouseX >= x && mouseX <= x + 60 && mouseY >= y && mouseY <= y + 16;
      context.fill(x, y, x + 60, y + 16, argb);
      context.fill(x - 1, y - 1, x + 60 + 1, y, hov ? 0xFFFFFFFF : 0x60FFFFFF);
      context.fill(x - 1, y + 16, x + 61, y + 17, hov ? 0xFFFFFFFF : 0x60FFFFFF);
      context.fill(x - 1, y, x, y + 16, hov ? 0xFFFFFFFF : 0x60FFFFFF);
      context.fill(x + 60, y, x + 61, y + 16, hov ? 0xFFFFFFFF : 0x60FFFFFF);
      if (hov) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("Edit"), x + 18, y + 4, 0xFFFFFFFF);
      }
   }

   private void renderSlider(DrawContext context, int mouseX, int mouseY, int x, int y, int width, String label, int value, int color) {
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y + 2, -1);
      int trackX = x + 25;
      int trackW = width - 25;
      context.fill(trackX, y + 4, trackX + trackW, y + 10, -12566464);
      float fillRatio = value / 255.0F;
      int fillWidth = (int)(trackW * fillRatio);
      context.fill(trackX, y + 5, trackX + fillWidth, y + 9, 0xFF000000 | color);
      int handleX = trackX + fillWidth - 2;
      context.fill(handleX, y + 2, handleX + 4, y + 12, -1);
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      if (this.colorModal != null) {
         float scaleFactor = this.client != null ? this.client.getWindow().getScaleFactor() : 1.0F;
         float scaleRatio = 3.0F / scaleFactor;
         Click scaled = new Click(click.x() / scaleRatio, click.y() / scaleRatio, new MouseInput(click.button(), click.buttonInfo().modifiers()));
         boolean consumed = this.colorModal.mouseClicked(scaled);
         if (!this.colorModal.isOpen()) this.colorModal = null;
         if (consumed) return true;
      }
      if (click.button() == 0) {
         double mouseX = click.x();
         double mouseY = click.y();
         int panelW = 320;
         int px = (this.width - panelW) / 2;
         int py = (this.height - 240) / 2;
         int cy = py + 70;
         if (mouseX >= px + 80 && mouseX <= px + 140 && mouseY >= cy - 2 && mouseY <= cy + 14) {
            this.openPicker("text", 0xFF000000 | this.colorPickerRText << 16 | this.colorPickerGText << 8 | this.colorPickerBText);
            return true;
         }
         cy += 25;
         if (mouseX >= px + 80 && mouseX <= px + 140 && mouseY >= cy - 2 && mouseY <= cy + 14) {
            this.openPicker("bg", 0xFF000000 | this.colorPickerRBg << 16 | this.colorPickerGBg << 8 | this.colorPickerBBg);
            return true;
         }
         cy += 25;
         if (this.checkSliderClick(mouseX, mouseY, px + 80, cy, 60, "textR")) {
            return true;
         }

         if (this.checkSliderClick(mouseX, mouseY, px + 150, cy, 60, "textG")) {
            return true;
         }

         if (this.checkSliderClick(mouseX, mouseY, px + 220, cy, 60, "textB")) {
            return true;
         }

         cy += 25;
         if (this.checkSliderClick(mouseX, mouseY, px + 80, cy, 60, "bgR")) {
            return true;
         }

         if (this.checkSliderClick(mouseX, mouseY, px + 150, cy, 60, "bgG")) {
            return true;
         }

         if (this.checkSliderClick(mouseX, mouseY, px + 220, cy, 60, "bgB")) {
            return true;
         }

         cy += 25;
         if (this.checkSliderClick(mouseX, mouseY, px + 80, cy, 200, "bgAlpha")) {
            return true;
         }
      }

      return super.mouseClicked(click, doubled);
   }

   private boolean checkSliderClick(double mx, double my, int x, int y, int w, String sliderId) {
      int trackX = x + 25;
      int trackW = w - 25;
      if (mx >= trackX - 2 && mx <= trackX + trackW + 2 && my >= y && my <= y + 14) {
         this.draggingSlider = sliderId;
         this.updateSliderValue(mx, trackX, trackW);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      if (this.colorModal != null) {
         float scaleFactor = this.client != null ? this.client.getWindow().getScaleFactor() : 1.0F;
         float scaleRatio = 3.0F / scaleFactor;
         Click scaled = new Click(click.x() / scaleRatio, click.y() / scaleRatio, new MouseInput(click.button(), click.buttonInfo().modifiers()));
         if (this.colorModal.mouseDragged(scaled)) return true;
      }
      if (this.draggingSlider != null && click.button() == 0) {
         double mouseX = click.x();
         int panelW = 320;
         int px = (this.width - panelW) / 2;
         if (this.draggingSlider.equals("textR")) {
            this.updateSliderValue(mouseX, px + 80 + 25, 35);
         } else if (this.draggingSlider.equals("textG")) {
            this.updateSliderValue(mouseX, px + 150 + 25, 35);
         } else if (this.draggingSlider.equals("textB")) {
            this.updateSliderValue(mouseX, px + 220 + 25, 35);
         } else if (this.draggingSlider.equals("bgR")) {
            this.updateSliderValue(mouseX, px + 80 + 25, 35);
         } else if (this.draggingSlider.equals("bgG")) {
            this.updateSliderValue(mouseX, px + 150 + 25, 35);
         } else if (this.draggingSlider.equals("bgB")) {
            this.updateSliderValue(mouseX, px + 220 + 25, 35);
         } else if (this.draggingSlider.equals("bgAlpha")) {
            this.updateSliderValue(mouseX, px + 80 + 25, 175);
         }

         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   public boolean mouseReleased(Click click) {
      this.draggingSlider = null;
      if (this.colorModal != null) {
         this.colorModal.mouseReleased();
         if (!this.colorModal.isOpen()) this.colorModal = null;
      }
      return super.mouseReleased(click);
   }

   private void openPicker(String which, int initialArgb) {
      float scaleFactor = this.client != null ? this.client.getWindow().getScaleFactor() : 1.0F;
      float scaleRatio = 3.0F / scaleFactor;
      int cx = (int)(this.width / 2.0 / scaleRatio * scaleFactor) / 2 * 2;
      int cy = (int)(this.height / 2.0 / scaleRatio * scaleFactor) / 2 * 2;
      this.colorModal = new ColorPickerModal(this.width / 2, this.height / 2, initialArgb, which.equals("text") ? "Text Color" : "Background Color", "Text HUD", argb -> {
         if (which.equals("text")) {
            this.colorPickerRText = argb >> 16 & 0xFF;
            this.colorPickerGText = argb >> 8 & 0xFF;
            this.colorPickerBText = argb & 0xFF;
         } else {
            this.colorPickerRBg = argb >> 16 & 0xFF;
            this.colorPickerGBg = argb >> 8 & 0xFF;
            this.colorPickerBBg = argb & 0xFF;
         }
         this.saveSettings();
      });
   }

   private void updateSliderValue(double mx, int trackX, int trackW) {
      int val = (int)((mx - trackX) / trackW * 255.0);
      val = Math.max(0, Math.min(255, val));
      if (this.draggingSlider.equals("textR")) {
         this.colorPickerRText = val;
      } else if (this.draggingSlider.equals("textG")) {
         this.colorPickerGText = val;
      } else if (this.draggingSlider.equals("textB")) {
         this.colorPickerBText = val;
      } else if (this.draggingSlider.equals("bgR")) {
         this.colorPickerRBg = val;
      } else if (this.draggingSlider.equals("bgG")) {
         this.colorPickerGBg = val;
      } else if (this.draggingSlider.equals("bgB")) {
         this.colorPickerBBg = val;
      } else if (this.draggingSlider.equals("bgAlpha")) {
         this.alphaPickerBg = val;
      }

      this.saveSettings();
   }

   private void saveSettings() {
      VoidCyanClient.textHudString = this.textWidget.getText();
      VoidCyanClient.textHudTextColor = this.colorPickerRText << 16 | this.colorPickerGText << 8 | this.colorPickerBText;
      VoidCyanClient.textHudBgColor = this.colorPickerRBg << 16 | this.colorPickerGBg << 8 | this.colorPickerBBg;
      VoidCyanClient.textHudBgAlpha = this.alphaPickerBg;
      VoidCyanClient.saveConfig();
   }

   public boolean keyPressed(KeyInput input) {
      if (this.colorModal != null && this.colorModal.keyPressed(input)) {
         if (input.key() == 256) {
            this.colorModal = null;
         }
         return true;
      }
      if (input.key() == 256) {
         this.saveSettings();
         this.client.setScreen(this.parent);
         return true;
      } else {
         boolean result = super.keyPressed(input);
         if (result) {
            this.saveSettings();
         }

         return result;
      }
   }

   public boolean charTyped(CharInput input) {
      if (this.colorModal != null && this.colorModal.charTyped(input)) {
         return true;
      }
      boolean result = super.charTyped(input);
      if (result) {
         this.saveSettings();
      }

      return result;
   }

   public void close() {
      this.saveSettings();
      this.client.setScreen(this.parent);
   }
}
