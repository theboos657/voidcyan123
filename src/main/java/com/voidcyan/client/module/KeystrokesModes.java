package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class KeystrokesModes {
   public static void render(DrawContext context, int mode) {
      MinecraftClient client = MinecraftClient.getInstance();
      switch (mode) {
         case 0:
         case 1:
         case 2:
         case 3:
            renderFullMode(context, client, mode);
            break;
         case 4:
            renderWASD(context, client);
            break;
         case 5:
            renderWASDOnly(context, client);
            break;
         case 6:
            renderMouseOnly(context, client);
            break;
         case 7:
            renderCPSOnly(context, client);
            break;
         default:
            renderWASD(context, client);
      }
   }

   private static void renderWASD(DrawContext context, MinecraftClient client) {
      int keySize = 25;
      int keyGap = 5;
      int border = -1;
      int pressed = -1;
      int unpressed = -16777216;
      drawKey(context, client, keySize + keyGap, 0, keySize, "W", 87, border, pressed, unpressed);
      drawKey(context, client, 0, keySize + keyGap, keySize, "A", 65, border, pressed, unpressed);
      drawKey(context, client, keySize + keyGap, keySize + keyGap, keySize, "S", 83, border, pressed, unpressed);
      drawKey(context, client, (keySize + keyGap) * 2, keySize + keyGap, keySize, "D", 68, border, pressed, unpressed);
      int mouseY = (keySize + keyGap) * 2;
      int mouseW = keySize + 20;
      drawMouse(context, client, 0, mouseY, mouseW, keySize, "L", true, border, pressed, unpressed);
      drawMouse(context, client, mouseW + keyGap, mouseY, mouseW, keySize, "R", false, border, pressed, unpressed);
   }

   private static void renderWASDOnly(DrawContext context, MinecraftClient client) {
      int keySize = 30;
      int keyGap = 6;
      int border = -1;
      int pressed = -1;
      int unpressed = -16777216;
      drawKey(context, client, keySize + keyGap, 0, keySize, "W", 87, border, pressed, unpressed);
      drawKey(context, client, 0, keySize + keyGap, keySize, "A", 65, border, pressed, unpressed);
      drawKey(context, client, keySize + keyGap, keySize + keyGap, keySize, "S", 83, border, pressed, unpressed);
      drawKey(context, client, (keySize + keyGap) * 2, keySize + keyGap, keySize, "D", 68, border, pressed, unpressed);
   }

   private static void renderMouseOnly(DrawContext context, MinecraftClient client) {
      int keySize = 35;
      int keyGap = 10;
      int border = -1;
      int pressed = -1;
      int unpressed = -16777216;
      int mouseW = keySize + 30;
      drawMouse(context, client, 0, 0, mouseW, keySize, "LEFT", true, border, pressed, unpressed);
      drawMouse(context, client, mouseW + keyGap, 0, mouseW, keySize, "RIGHT", false, border, pressed, unpressed);
   }

   private static void renderCPSOnly(DrawContext context, MinecraftClient client) {
      long now = System.currentTimeMillis();
      VoidCyanClient.leftClickTimestamps.removeIf(t -> now - t > 1000L);
      VoidCyanClient.rightClickTimestamps.removeIf(t -> now - t > 1000L);
      int left = VoidCyanClient.leftClickTimestamps.size();
      int right = VoidCyanClient.rightClickTimestamps.size();
      String text = left + " | " + right + " CPS";
      context.drawTextWithShadow(client.textRenderer, text, 0, 0, -1);
   }

   private static void renderFullMode(DrawContext context, MinecraftClient client, int mode) {
      int keySize = 18;
      int keyGap = 2;
      int border = -1;
      int pressed = -1;
      int unpressed = -16777216;
      String[][] layout;
      int[][] codes;
      switch (mode) {
         case 0:
         case 1:
            layout = new String[][]{
               {"ESC", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "BKSP"},
               {"TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\"},
               {"CAPS", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "ENTER"},
               {"SHIFT", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "SHIFT"},
               {"CTRL", "ALT", "SPACE", "ALT", "CTRL"}
            };
            codes = new int[][]{
               {256, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 45, 61, 259},
               {258, 81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 91, 93, 92},
               {280, 65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 39, 257},
               {340, 90, 88, 67, 86, 66, 78, 77, 44, 46, 47, 344},
               {341, 342, 32, 346, 345}
            };
            break;
         case 2:
            layout = new String[][]{
               {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
               {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
               {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
               {"Z", "X", "C", "V", "B", "N", "M"},
               {"CTRL", "SPACE", "ALT"}
            };
            codes = new int[][]{
               {49, 50, 51, 52, 53, 54, 55, 56, 57, 48},
               {81, 87, 69, 82, 84, 89, 85, 73, 79, 80},
               {65, 83, 68, 70, 71, 72, 74, 75, 76},
               {90, 88, 67, 86, 66, 78, 77},
               {341, 32, 342}
            };
            keySize = 20;
            keyGap = 3;
            break;
         case 3:
            layout = new String[][]{{"Q", "W", "E", "R", "T", "Y"}, {"A", "S", "D", "F", "G", "H"}, {"Z", "X", "C", "V", "B"}, {"SPACE"}};
            codes = new int[][]{{81, 87, 69, 82, 84, 89}, {65, 83, 68, 70, 71, 72}, {90, 88, 67, 86, 66}, {32}};
            keySize = 22;
            keyGap = 4;
            break;
         default:
            renderWASD(context, client);
            return;
      }

      int currentY = 0;

      for (int row = 0; row < layout.length; row++) {
         int currentX = 0;

         for (int col = 0; col < layout[row].length; col++) {
            String label = layout[row][col];
            int key = codes[row][col];
            int width = keySize;
            if (label.equals("ESC") || label.equals("TAB") || label.equals("CAPS")) {
               width = (int)(keySize * 1.5);
            } else if (label.equals("BKSP") || label.equals("\\") || label.equals("ENTER")) {
               width = (int)(keySize * 1.5);
            } else if (label.equals("SHIFT")) {
               width = keySize * 2;
            } else if (label.equals("SPACE")) {
               width = keySize * 6;
            } else if (label.equals("CTRL") || label.equals("ALT")) {
               width = (int)(keySize * 1.5);
            }

            boolean isPressed = VoidCyanClient.pressedKeys.contains(key);
            int bg = isPressed ? pressed : unpressed;
            int txt = isPressed ? -16777216 : -1;
            context.fill(currentX, currentY, currentX + width, currentY + keySize, bg);
            context.fill(currentX, currentY, currentX + width, currentY + 1, border);
            context.fill(currentX, currentY + keySize - 1, currentX + width, currentY + keySize, border);
            context.fill(currentX, currentY + 1, currentX + 1, currentY + keySize - 1, border);
            context.fill(currentX + width - 1, currentY + 1, currentX + width, currentY + keySize - 1, border);
            String displayLabel = label.length() > 4 ? label.substring(0, 1) : label;
            int lw = client.textRenderer.getWidth(displayLabel);
            context.drawText(client.textRenderer, displayLabel, currentX + (width - lw) / 2, currentY + (keySize - 8) / 2, txt, false);
            currentX += width + keyGap;
         }

         currentY += keySize + keyGap;
      }

      if (mode <= 2 && VoidCyanClient.keystrokesShowMouse) {
         int mouseY = currentY + keyGap;
         int mouseW = keySize * 3;
         drawMouse(context, client, 50, mouseY, mouseW, keySize, "LEFT", true, border, pressed, unpressed);
         drawMouse(context, client, 50 + mouseW + keyGap * 2, mouseY, mouseW, keySize, "RIGHT", false, border, pressed, unpressed);
      }
   }

   private static void drawKey(
      DrawContext context, MinecraftClient client, int x, int y, int size, String label, int key, int border, int pressed, int unpressed
   ) {
      boolean isPressed = VoidCyanClient.pressedKeys.contains(key);
      int bg = isPressed ? pressed : unpressed;
      int txt = isPressed ? -16777216 : -1;
      context.fill(x, y, x + size, y + size, bg);
      context.fill(x, y, x + size, y + 1, border);
      context.fill(x, y + size - 1, x + size, y + size, border);
      context.fill(x, y + 1, x + 1, y + size - 1, border);
      context.fill(x + size - 1, y + 1, x + size, y + size - 1, border);
      int w = client.textRenderer.getWidth(label);
      context.drawText(client.textRenderer, label, x + (size - w) / 2, y + (size - 8) / 2, txt, false);
   }

   private static void drawMouse(
      DrawContext context, MinecraftClient client, int x, int y, int w, int h, String label, boolean left, int border, int pressed, int unpressed
   ) {
      boolean isPressed = left ? client.options.attackKey.isPressed() : client.options.useKey.isPressed();
      int bg = isPressed ? pressed : unpressed;
      int txt = isPressed ? -16777216 : -1;
      context.fill(x, y, x + w, y + h, bg);
      context.fill(x, y, x + w, y + 1, border);
      context.fill(x, y + h - 1, x + w, y + h, border);
      context.fill(x, y + 1, x + 1, y + h - 1, border);
      context.fill(x + w - 1, y + 1, x + w, y + h - 1, border);
      int lw = client.textRenderer.getWidth(label);
      context.drawText(client.textRenderer, label, x + (w - lw) / 2, y + (h - 8) / 2, txt, false);
   }
}
