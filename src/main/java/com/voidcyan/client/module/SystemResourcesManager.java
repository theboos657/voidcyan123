package com.voidcyan.client.module;

import com.sun.management.OperatingSystemMXBean;
import com.voidcyan.client.screen.VoidCyanMenuScreen;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class SystemResourcesManager {
   private static final int PAD = 5;
   private static final int LINE_H = 12;

   private SystemResourcesManager() {
   }

   public static List<String> buildLines(boolean sample) {
      List<String> lines = new ArrayList<>();
      if (sample) {
         lines.add("RAM: 2048/4096 MB");
         lines.add("CPU: 42%");
         return lines;
      } else {
         Runtime runtime = Runtime.getRuntime();
         long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
         long maxMb = runtime.maxMemory() / 1048576L;
         lines.add("RAM: " + usedMb + "/" + maxMb + " MB");
         double cpu = getProcessCpuPercent();
         if (cpu >= 0.0) {
            lines.add(String.format("CPU: %.0f%%", cpu));
         } else {
            lines.add("CPU: --");
         }

         return lines;
      }
   }

   private static double getProcessCpuPercent() {
      try {
         if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean os) {
            double load = os.getProcessCpuLoad();
            if (load >= 0.0) {
               return load * 100.0;
            }
         }
      } catch (Throwable var4) {
      }

      return -1.0;
   }

   public static int getWidth(MinecraftClient client, boolean sample) {
      if (client != null && client.textRenderer != null) {
         int max = 0;

         for (String line : buildLines(sample)) {
            max = Math.max(max, client.textRenderer.getWidth(line));
         }

         return max + 10;
      } else {
         return 130;
      }
   }

   public static int getHeight() {
      return 34;
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
         List<String> lines = buildLines(sample);
         int boxW = getWidth(client, sample);
         int boxH = getHeight();
         context.fill(x, y, x + boxW, y + boxH, Integer.MIN_VALUE);
         drawBorder(context, x, y, boxW, boxH, -16711681);
         int textY = y + 5;

         for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? -1 : -5592406;
            context.drawTextWithShadow(client.textRenderer, Text.literal(lines.get(i)), x + 5, textY, color);
            textY += 12;
         }
      }
   }

   private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y, x + 1, y + h, color);
      context.fill(x + w - 1, y, x + w, y + h, color);
   }
}
