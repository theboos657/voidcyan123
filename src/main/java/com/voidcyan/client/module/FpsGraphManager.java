package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.Arrays;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FpsGraphManager {
   private static final long[] frameTimeStamps = new long[30000];
   private static final long[] frameDeltas = new long[30000];
   private static int currentIndex = 0;
   private static long lastTime = System.nanoTime();
   private static long totalDeltaAll = 0L;
   private static long totalFramesAll = 0L;
   private static int avgFps = 0;
   private static int minFps = 0;
   private static int maxFps = 0;
   private static int low1Fps = 0;
   private static int low01Fps = 0;
   public static int currentFps = 0;
   private static long lastCalcTime = 0L;
   private static long[] window = null;
   private static int windowSize = 0;

   public static void onFrame() {
      long now = System.nanoTime();
      long delta = now - lastTime;
      lastTime = now;
      if (delta <= 0L) {
         delta = 1L;
      }

      frameTimeStamps[currentIndex] = now;
      frameDeltas[currentIndex] = delta;
      currentIndex = (currentIndex + 1) % 30000;
      totalDeltaAll += delta;
      totalFramesAll++;
      currentFps = (int)(1000000000L / delta);
      if (now - lastCalcTime > 500000000L && VoidCyanClient.isFpsGraphEnabled) {
         calculateMetrics(now);
         lastCalcTime = now;
      }
   }

   private static void calculateMetrics(long now) {
      long cutoff = now - 10000000000L;
      int count = 0;

      for (int idx = (currentIndex - 1 + 30000) % 30000; count < 30000 && frameTimeStamps[idx] > cutoff; idx = (idx - 1 + 30000) % 30000) {
         count++;
      }

      if (count >= 2) {
         if (window == null || windowSize < count) {
            window = new long[count];
            windowSize = count;
         }

         int validCount = 0;
         int var10 = (currentIndex - count + 30000) % 30000;

         for (int i = 0; i < count; i++) {
            long t = frameDeltas[(var10 + i) % 30000];
            if (t > 0L) {
               window[validCount++] = t;
            }
         }

         if (validCount >= 2) {
            Arrays.sort(window, 0, validCount);
            if (totalFramesAll > 0L) {
               avgFps = (int)(1000000000L / (totalDeltaAll / totalFramesAll));
            }

            maxFps = (int)(1000000000L / window[0]);
            minFps = (int)(1000000000L / window[validCount - 1]);
            low1Fps = (int)(1000000000L / window[validCount - Math.max(1, (int)(validCount * 0.01))]);
            low01Fps = (int)(1000000000L / window[validCount - Math.max(1, (int)(validCount * 0.001))]);
         }
      }
   }

   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.textRenderer != null) {
         int x = 0;
         int y = 0;
         int width = 120;
         int graphHeight = 40;
         if (VoidCyanClient.fpsGraphBackground) {
            context.fill(x, y, x + width, y + graphHeight, Integer.MIN_VALUE);
         }

         int graphDataPoints = width;
         int startIndex = (currentIndex - width + 30000) % 30000;

         for (int i = 0; i < graphDataPoints; i++) {
            int idx = (startIndex + i) % 30000;
            long delta = frameDeltas[idx];
            if (delta != 0L) {
               float fps = 1.0E9F / (float)delta;
               float maxFpsGraph = Math.max(60.0F, (float)maxFps);
               int barHeight = (int)(fps / maxFpsGraph * graphHeight);
               barHeight = Math.min(graphHeight, Math.max(1, barHeight));
               int color = fps < 30.0F ? -43691 : (fps < 60.0F ? -171 : -11141291);
               context.fill(x + i, y + graphHeight - barHeight, x + i + 1, y + graphHeight, color);
            }
         }

         if (VoidCyanClient.fpsGraphBackground) {
            context.fill(x, y, x + width, y + 1, -12303292);
            context.fill(x, y + graphHeight - 1, x + width, y + graphHeight, -12303292);
            context.fill(x, y, x + 1, y + graphHeight, -12303292);
            context.fill(x + width - 1, y, x + width, y + graphHeight, -12303292);
         }

         String fpsText = currentFps + " FPS";
         context.getMatrices().pushMatrix();
         float scale = 1.5F;
         context.getMatrices().scale(scale, scale);
         float textX = (x + width + 8) / scale;
         float textY = (y + graphHeight / 2.0F - 4.0F) / scale;
         context.drawTextWithShadow(client.textRenderer, fpsText, (int)textX, (int)textY, -1);
         context.getMatrices().popMatrix();
         int textYPos = y + graphHeight + 4;
         if (VoidCyanClient.showFpsAvg) {
            context.drawTextWithShadow(client.textRenderer, "Avg: " + avgFps + " FPS", x + 2, textYPos, -1);
            textYPos += 10;
         }

         if (VoidCyanClient.showFpsMinMax) {
            context.drawTextWithShadow(client.textRenderer, "Min: " + minFps + "  Max: " + maxFps, x + 2, textYPos, -5592406);
            textYPos += 10;
         }

         if (VoidCyanClient.showFps1Percent) {
            context.drawTextWithShadow(client.textRenderer, "1% Low: " + low1Fps, x + 2, textYPos, -21846);
            textYPos += 10;
         }

         if (VoidCyanClient.showFps01Percent) {
            context.drawTextWithShadow(client.textRenderer, "0.1% Low: " + low01Fps, x + 2, textYPos, -43691);
         }
      }
   }
}
