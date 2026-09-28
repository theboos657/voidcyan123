package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class InvHighlightSettingsScreen extends Screen {
   private final Screen parent;
   private TextFieldWidget itemField;

   public InvHighlightSettingsScreen(Screen parent) {
      super(Text.literal("Inv Highlight Settings"));
      this.parent = parent;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
      super.render(context, mouseX, mouseY, deltaTicks);
      int w = 350;
      int h = 220;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      context.fill(x, y, x + w, y + h, Integer.MIN_VALUE);
      context.fill(x, y, x + w, y + 1, VoidCyanClient.getPrimaryColor());
      context.fill(x, y + h - 1, x + w, y + h, VoidCyanClient.getPrimaryColor());
      context.fill(x, y, x + 1, y + h, VoidCyanClient.getPrimaryColor());
      context.fill(x + w - 1, y, x + w, y + h, VoidCyanClient.getPrimaryColor());
      context.drawTextWithShadow(this.textRenderer, Text.literal("Inv Highlight Settings"), x + 10, y + 10, -1);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Item IDs (comma-separated)"), x + 10, y + 35, -5592406);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Red: " + VoidCyanClient.invHighlightRed), x + 10, y + 75, -43691);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Green: " + VoidCyanClient.invHighlightGreen), x + 10, y + 100, -11141291);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Blue: " + VoidCyanClient.invHighlightBlue), x + 10, y + 125, -11184641);
      int previewColor = 0xFF000000
         | (VoidCyanClient.invHighlightRed & 0xFF) << 16
         | (VoidCyanClient.invHighlightGreen & 0xFF) << 8
         | VoidCyanClient.invHighlightBlue & 0xFF;
      context.fill(x + 250, y + 70, x + 320, y + 130, previewColor);
   }

   protected void init() {
      int w = 350;
      int h = 220;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      this.itemField = new TextFieldWidget(this.textRenderer, x + 10, y + 48, 320, 20, Text.literal("Item IDs"));
      this.itemField.setMaxLength(500);
      this.itemField.setText(VoidCyanClient.invHighlightItems);
      this.addDrawableChild(this.itemField);
      this.addDrawableChild(new SliderWidget(x + 80, y + 70, 150, 20, Text.literal(""), VoidCyanClient.invHighlightRed / 255.0) {
         protected void updateMessage() {
            VoidCyanClient.invHighlightRed = (int)(this.value * 255.0);
            this.setMessage(Text.literal("Red: " + VoidCyanClient.invHighlightRed));
         }

         protected void applyValue() {
            VoidCyanClient.invHighlightRed = (int)(this.value * 255.0);
         }
      });
      this.addDrawableChild(new SliderWidget(x + 80, y + 95, 150, 20, Text.literal(""), VoidCyanClient.invHighlightGreen / 255.0) {
         protected void updateMessage() {
            VoidCyanClient.invHighlightGreen = (int)(this.value * 255.0);
            this.setMessage(Text.literal("Green: " + VoidCyanClient.invHighlightGreen));
         }

         protected void applyValue() {
            VoidCyanClient.invHighlightGreen = (int)(this.value * 255.0);
         }
      });
      this.addDrawableChild(new SliderWidget(x + 80, y + 120, 150, 20, Text.literal(""), VoidCyanClient.invHighlightBlue / 255.0) {
         protected void updateMessage() {
            VoidCyanClient.invHighlightBlue = (int)(this.value * 255.0);
            this.setMessage(Text.literal("Blue: " + VoidCyanClient.invHighlightBlue));
         }

         protected void applyValue() {
            VoidCyanClient.invHighlightBlue = (int)(this.value * 255.0);
         }
      });
   }

   public void removed() {
      VoidCyanClient.invHighlightItems = this.itemField.getText();
      VoidCyanClient.saveConfig();
   }

   public void close() {
      VoidCyanClient.invHighlightItems = this.itemField.getText();
      VoidCyanClient.saveConfig();
      this.client.setScreen(this.parent);
   }

   public boolean keyPressed(KeyInput input) {
      if (input.key() == 256) {
         this.close();
         return true;
      } else {
         return this.itemField.keyPressed(input) ? true : super.keyPressed(input);
      }
   }
}
