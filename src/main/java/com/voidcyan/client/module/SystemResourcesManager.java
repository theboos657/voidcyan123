package com.voidcyan.client.module;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class SystemResourcesManager {
   private static final List<String> SAMPLE_LINES = List.of("RAM: 2048/4096 MB", "CPU: 42%");
   private static String cpuLine = "CPU: --";
   private static long cpuSampledAt;

   private SystemResourcesManager() {
   }

   public static List<String> buildLines(boolean sample) {
      if (sample) {
         return SAMPLE_LINES;
      }
      Runtime runtime = Runtime.getRuntime();
      long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
      long maxMb = runtime.maxMemory() / 1048576L;
      return List.of("RAM: " + usedMb + "/" + maxMb + " MB", cpuLine());
   }

   // The OS CPU query is comparatively expensive, so sample it once per second rather than every frame.
   private static String cpuLine() {
      long now = System.currentTimeMillis();
      if (now - cpuSampledAt >= 1000L) {
         cpuSampledAt = now;
         double cpu = getProcessCpuPercent();
         cpuLine = cpu >= 0.0 ? String.format("CPU: %.0f%%", cpu) : "CPU: --";
      }
      return cpuLine;
   }

   private static double getProcessCpuPercent() {
      try {
         if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean os) {
            double load = os.getProcessCpuLoad();
            if (load >= 0.0) {
               return load * 100.0;
            }
         }
      } catch (Throwable ignored) {
      }
      return -1.0;
   }

   public static int getWidth(MinecraftClient client, boolean sample) {
      return client != null && client.textRenderer != null ? widthOf(client, buildLines(sample)) : 130;
   }

   private static int widthOf(MinecraftClient client, List<String> lines) {
      int max = 0;
      for (String line : lines) {
         max = Math.max(max, client.textRenderer.getWidth(line));
      }
      return max + 10;
   }

   public static int getHeight() {
      return 34;
   }

   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.currentScreen == null) {
         drawPanel(context, client, 0, 0, false);
      }
   }

   public static void renderPreview(DrawContext context) {
      drawPanel(context, MinecraftClient.getInstance(), 0, 0, true);
   }

   private static void drawPanel(DrawContext context, MinecraftClient client, int x, int y, boolean sample) {
      if (client != null && client.textRenderer != null) {
         List<String> lines = buildLines(sample);
         int boxW = widthOf(client, lines);
         int boxH = getHeight();
         context.fill(x, y, x + boxW, y + boxH, Integer.MIN_VALUE);
         context.drawStrokedRectangle(x, y, boxW, boxH, -16711681);
         int textY = y + 5;
         for (int i = 0; i < lines.size(); i++) {
            context.drawTextWithShadow(client.textRenderer, Text.literal(lines.get(i)), x + 5, textY, i == 0 ? -1 : -5592406);
            textY += 12;
         }
      }
   }
}
