package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeybindsHudRenderer {
   private static int hudWidth = 100;
   private static int hudHeight = 30;

   public static void render(DrawContext context) {
      if (VoidCyanClient.isKeybindsDisplayEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         TextRenderer tr = client.textRenderer;
         List<String> lines = new ArrayList<>();
         if (VoidCyanClient.freelookKey != -1) {
            lines.add("FreeLook: " + getKeyName(VoidCyanClient.freelookKey));
         }

         if (VoidCyanClient.zoomKey != -1) {
            lines.add("Zoom: " + getKeyName(VoidCyanClient.zoomKey));
         }

         if (VoidCyanClient.stopwatchKey != -1) {
            lines.add("Stopwatch: " + getKeyName(VoidCyanClient.stopwatchKey));
         }

         for (Entry<String, Integer> entry : VoidCyanClient.moduleKeybinds.entrySet()) {
            if (entry.getValue() != -1) {
               lines.add(entry.getKey() + ": " + getKeyName(entry.getValue()));
            }
         }

         if (lines.isEmpty()) {
            lines.add("No Keybinds Set");
         }

         int maxW = tr.getWidth("Keybinds");

         for (String line : lines) {
            maxW = Math.max(maxW, tr.getWidth(line));
         }

         hudWidth = maxW + 16;
         hudHeight = 16 + lines.size() * 10 + 4;
         context.getMatrices().pushMatrix();
         context.getMatrices().translate(VoidCyanClient.keybindsHudX, VoidCyanClient.keybindsHudY);
         context.getMatrices().scale(VoidCyanClient.keybindsHudScale, VoidCyanClient.keybindsHudScale);
         context.fill(0, 0, hudWidth, hudHeight, -1728053248);
         context.fill(0, 0, hudWidth, 2, VoidCyanClient.getPrimaryColor());
         context.drawTextWithShadow(tr, Text.literal("Keybinds"), 6, 6, VoidCyanClient.getPrimaryColor());
         int y = 18;

         for (String line : lines) {
            context.drawTextWithShadow(tr, Text.literal(line), 8, y, -2236963);
            y += 10;
         }

         context.getMatrices().popMatrix();
      }
   }

   private static String getKeyName(int keyCode) {
      if (keyCode == -1) {
         return "NONE";
      } else {
         String name = GLFW.glfwGetKeyName(keyCode, 0);
         if (name != null) {
            return name.toUpperCase();
         } else {
            switch (keyCode) {
               case 32:
                  return "SPACE";
               case 256:
                  return "ESCAPE";
               case 257:
                  return "ENTER";
               case 258:
                  return "TAB";
               case 259:
                  return "BACKSPACE";
               case 262:
                  return "RIGHT";
               case 263:
                  return "LEFT";
               case 264:
                  return "DOWN";
               case 265:
                  return "UP";
               case 340:
                  return "LSHIFT";
               case 341:
                  return "LCTRL";
               case 342:
                  return "LALT";
               case 344:
                  return "RSHIFT";
               case 345:
                  return "RCTRL";
               case 346:
                  return "RALT";
               default:
                  return "KEY_" + keyCode;
            }
         }
      }
   }

   public static int getHudWidth() {
      return (int)(hudWidth * VoidCyanClient.keybindsHudScale);
   }

   public static int getHudHeight() {
      return (int)(hudHeight * VoidCyanClient.keybindsHudScale);
   }
}
