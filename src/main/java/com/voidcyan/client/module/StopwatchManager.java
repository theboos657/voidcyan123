package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.VoidCyanMenuScreen;
import java.util.Properties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class StopwatchManager {
   private static final int PAD = 5;

   private StopwatchManager() {
   }

   public static long getElapsedMs() {
      return VoidCyanClient.stopwatchRunning
         ? VoidCyanClient.stopwatchElapsedMs + (System.currentTimeMillis() - VoidCyanClient.stopwatchStartedAt)
         : VoidCyanClient.stopwatchElapsedMs;
   }

   public static void toggleRunning() {
      if (VoidCyanClient.stopwatchRunning) {
         VoidCyanClient.stopwatchElapsedMs = getElapsedMs();
         VoidCyanClient.stopwatchRunning = false;
      } else {
         VoidCyanClient.stopwatchStartedAt = System.currentTimeMillis();
         VoidCyanClient.stopwatchRunning = true;
      }

      VoidCyanClient.saveConfig();
   }

   public static void reset() {
      VoidCyanClient.stopwatchRunning = false;
      VoidCyanClient.stopwatchElapsedMs = 0L;
      VoidCyanClient.stopwatchStartedAt = 0L;
      VoidCyanClient.saveConfig();
   }

   public static void load(Properties props) {
      try {
         VoidCyanClient.stopwatchElapsedMs = Long.parseLong(props.getProperty("stopwatchElapsedMs", "0"));
      } catch (Exception ignored) {}
      VoidCyanClient.stopwatchRunning = false;
   }

   public static void save(Properties props) {
      props.setProperty("stopwatchElapsedMs", String.valueOf(getElapsedMs()));
   }

   public static String formatElapsed(long elapsedMs, boolean sample) {
      if (sample) {
         return "01:23.45";
      } else {
         long totalCs = elapsedMs / 10L;
         long cs = totalCs % 100L;
         long totalSec = totalCs / 100L;
         long sec = totalSec % 60L;
         long min = totalSec / 60L;
         return String.format("%02d:%02d.%02d", min, sec, cs);
      }
   }

   public static int getWidth(MinecraftClient client, boolean sample) {
      if (client != null && client.textRenderer != null) {
         String text = formatElapsed(sample ? 83450L : getElapsedMs(), sample);
         return client.textRenderer.getWidth(text) + 10;
      } else {
         return 90;
      }
   }

   public static int getHeight() {
      return 20;
   }

   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.currentScreen == null || client.currentScreen instanceof VoidCyanMenuScreen) {
         drawPanel(context, client, 0, 0, false);
      }
   }

   public static void renderPreview(DrawContext context) {
      drawPanel(context, MinecraftClient.getInstance(), 0, 0, true);
   }

   private static void drawPanel(DrawContext context, MinecraftClient client, int x, int y, boolean sample) {
      if (client != null && client.textRenderer != null) {
         String text = formatElapsed(sample ? 83450L : getElapsedMs(), sample);
         int boxW = getWidth(client, sample);
         int boxH = getHeight();
         int color = VoidCyanClient.stopwatchRunning && !sample ? -11141291 : -1;
         if (VoidCyanClient.stopwatchBackground) {
            context.fill(x, y, x + boxW, y + boxH, Integer.MIN_VALUE);
            drawBorder(context, x, y, boxW, boxH, VoidCyanClient.getPrimaryColor());
         }

         context.drawTextWithShadow(client.textRenderer, Text.literal(text), x + 5, y + 5, color);
      }
   }

   private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y, x + 1, y + h, color);
      context.fill(x + w - 1, y, x + w, y + h, color);
   }
}
