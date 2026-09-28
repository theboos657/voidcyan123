package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class NickHiderSettingsScreen extends Screen {
   private final Screen parent;
   private String replacementName = "";
   private String colorHex = "";
   private boolean useCustomColor = false;
   private int focusedField = 0;
   private int cursorTimer;

   public NickHiderSettingsScreen(Screen parent) {
      super(Text.literal("NickHider Settings"));
      this.parent = parent;
   }

   protected void init() {
      this.replacementName = VoidCyanClient.nickHiderReplacementName;
      this.useCustomColor = VoidCyanClient.nickHiderUseCustomColor;
      this.colorHex = String.format("#%06X", VoidCyanClient.nickHiderColor & 16777215);
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      int w = 400;
      int h = 250;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      context.fill(x, y, x + w, y + h, Integer.MIN_VALUE);
      context.fill(x, y, x + w, y + 1, VoidCyanClient.getPrimaryColor());
      context.fill(x, y + h - 1, x + w, y + h, VoidCyanClient.getPrimaryColor());
      context.fill(x, y, x + 1, y + h, VoidCyanClient.getPrimaryColor());
      context.fill(x + w - 1, y, x + w, y + h, VoidCyanClient.getPrimaryColor());
      context.drawTextWithShadow(this.textRenderer, Text.literal("NickHider Settings"), x + 10, y + 10, VoidCyanClient.getPrimaryColor());
      context.drawTextWithShadow(this.textRenderer, Text.literal("Replacement Name:"), x + 10, y + 40, -1);
      int fieldX = x + 10;
      int fieldY = y + 55;
      int fieldW = w - 20;
      int fieldH = 22;
      boolean focused = this.focusedField == 1;
      int bg = focused ? -13421773 : -14540254;
      context.fill(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, bg);
      context.fill(fieldX, fieldY, fieldX + fieldW, fieldY + 1, focused ? -1 : -11184811);
      context.fill(fieldX, fieldY + fieldH - 1, fieldX + fieldW, fieldY + fieldH, focused ? -1 : -11184811);
      context.fill(fieldX, fieldY, fieldX + 1, fieldY + fieldH, focused ? -1 : -11184811);
      context.fill(fieldX + fieldW - 1, fieldY, fieldX + fieldW, fieldY + fieldH, focused ? -1 : -11184811);
      String display = this.replacementName.isEmpty() ? "Enter replacement name..." : this.replacementName;
      int textColor = this.replacementName.isEmpty() ? -11184811 : -1;
      context.drawTextWithShadow(this.textRenderer, Text.literal(display), fieldX + 4, fieldY + 6, textColor);
      this.cursorTimer++;
      if (focused && this.cursorTimer / 20 % 2 == 0) {
         int cursorX = fieldX + 4 + this.textRenderer.getWidth(this.replacementName);
         context.fill(cursorX, fieldY + 4, cursorX + 1, fieldY + fieldH - 4, -1);
      }

      int toggleY = y + 90;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Use Custom Color"), x + 35, toggleY + 6, -1);
      int toggleBg = this.useCustomColor ? VoidCyanClient.getPrimaryColor() : -12303292;
      context.fill(x + 10, toggleY, x + 30, toggleY + 20, toggleBg);
      if (this.useCustomColor) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("Color Hex Code:"), x + 10, y + 125, -1);
         int colorFieldY = y + 140;
         boolean cFocused = this.focusedField == 2;
         int cBg = cFocused ? -13421773 : -14540254;
         context.fill(fieldX, colorFieldY, fieldX + fieldW, colorFieldY + fieldH, cBg);
         context.fill(fieldX, colorFieldY, fieldX + fieldW, colorFieldY + 1, cFocused ? -1 : -11184811);
         context.fill(fieldX, colorFieldY + fieldH - 1, fieldX + fieldW, colorFieldY + fieldH, cFocused ? -1 : -11184811);
         context.fill(fieldX, colorFieldY, fieldX + 1, colorFieldY + fieldH, cFocused ? -1 : -11184811);
         context.fill(fieldX + fieldW - 1, colorFieldY, fieldX + fieldW, colorFieldY + fieldH, cFocused ? -1 : -11184811);
         String cDisplay = this.colorHex.isEmpty() ? "#FFFFFF" : this.colorHex;
         int cTextColor = this.colorHex.isEmpty() ? -11184811 : -1;
         int previewColor = -1;

         try {
            if (this.colorHex.startsWith("#")) {
               previewColor = 0xFF000000 | Integer.parseInt(this.colorHex.substring(1), 16);
            } else {
               previewColor = 0xFF000000 | Integer.parseInt(this.colorHex, 16);
            }
         } catch (Exception var26) {
         }

         context.fill(fieldX + fieldW - 25, colorFieldY + 2, fieldX + fieldW - 2, colorFieldY + fieldH - 2, previewColor);
         context.drawTextWithShadow(this.textRenderer, Text.literal(cDisplay), fieldX + 4, colorFieldY + 6, cTextColor);
         if (cFocused && this.cursorTimer / 20 % 2 == 0) {
            int cursorX = fieldX + 4 + this.textRenderer.getWidth(this.colorHex);
            context.fill(cursorX, colorFieldY + 4, cursorX + 1, colorFieldY + fieldH - 4, -1);
         }
      }

      context.drawTextWithShadow(this.textRenderer, Text.literal("Press ESC to return"), x + 10, y + h - 20, VoidCyanClient.getPrimaryColor());
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      int w = 400;
      int h = 250;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      int fieldX = x + 10;
      int fieldW = w - 20;
      int fieldH = 22;
      int fieldY = y + 55;
      if (mx >= fieldX && mx <= fieldX + fieldW && my >= fieldY && my <= fieldY + fieldH) {
         this.focusedField = 1;
         this.cursorTimer = 0;
         return true;
      } else {
         int toggleY = y + 90;
         if (mx >= x + 10 && mx <= x + 30 && my >= toggleY && my <= toggleY + 20) {
            this.useCustomColor = !this.useCustomColor;
            this.focusedField = 0;
            return true;
         } else {
            if (this.useCustomColor) {
               int colorFieldY = y + 140;
               if (mx >= fieldX && mx <= fieldX + fieldW && my >= colorFieldY && my <= colorFieldY + fieldH) {
                  this.focusedField = 2;
                  this.cursorTimer = 0;
                  return true;
               }
            }

            this.focusedField = 0;
            return super.mouseClicked(click, doubled);
         }
      }
   }

   public boolean keyPressed(KeyInput input) {
      if (input.key() == 256) {
         this.close();
         return true;
      } else if (this.focusedField == 1) {
         int key = input.key();
         if (key == 259 && !this.replacementName.isEmpty()) {
            this.replacementName = this.replacementName.substring(0, this.replacementName.length() - 1);
            this.cursorTimer = 0;
            return true;
         } else if (key != 257 && key != 335) {
            return true;
         } else {
            this.focusedField = 0;
            return true;
         }
      } else if (this.focusedField == 2) {
         int key = input.key();
         if (key == 259 && !this.colorHex.isEmpty()) {
            this.colorHex = this.colorHex.substring(0, this.colorHex.length() - 1);
            this.cursorTimer = 0;
            return true;
         } else if (key != 257 && key != 335) {
            return true;
         } else {
            this.focusedField = 0;
            return true;
         }
      } else {
         return super.keyPressed(input);
      }
   }

   public boolean charTyped(CharInput input) {
      if (this.focusedField == 0) {
         return false;
      } else {
         char c = (char)input.codepoint();
         if (this.focusedField == 1) {
            if (c >= ' ' && c < 127 && this.replacementName.length() < 50) {
               this.replacementName = this.replacementName + c;
               this.cursorTimer = 0;
               return true;
            }
         } else if (this.focusedField == 2 && (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F' || c == '#') && this.colorHex.length() < 7) {
            this.colorHex = this.colorHex + c;
            this.cursorTimer = 0;
            return true;
         }

         return false;
      }
   }

   public void close() {
      VoidCyanClient.nickHiderReplacementName = this.replacementName;
      VoidCyanClient.nickHiderUseCustomColor = this.useCustomColor;

      try {
         if (this.colorHex.startsWith("#")) {
            VoidCyanClient.nickHiderColor = Integer.parseInt(this.colorHex.substring(1), 16);
         } else if (!this.colorHex.isEmpty()) {
            VoidCyanClient.nickHiderColor = Integer.parseInt(this.colorHex, 16);
         }
      } catch (Exception var2) {
      }

      VoidCyanClient.saveConfig();
      VoidCyanClient.updateNameProtectMappings(this.client);
      this.client.setScreen(this.parent);
   }
}
