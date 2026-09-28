package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.VoidCyanMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class MouseStrokesRenderer {
   private static final int BOX_SIZE = 60;
   private static final int HALF = 30;
   private static final long RESET_TIMEOUT_MS = 5000L;
   private static float cursorX = 30.0F;
   private static float cursorY = 30.0F;
   private static float lastYaw = Float.NaN;
   private static float lastPitch = Float.NaN;
   private static long lastMouseMoveTime = System.currentTimeMillis();

   private MouseStrokesRenderer() {
   }

   public static void render(DrawContext context) {
      if (VoidCyanClient.isMouseStrokesEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null || client.currentScreen instanceof VoidCyanMenuScreen) {
            if (client.getWindow() != null) {
               if (client.player != null) {
                  long window = client.getWindow().getHandle();
                  double[] mx = new double[1];
                  double[] my = new double[1];
                  GLFW.glfwGetCursorPos(window, mx, my);
                  double rawMouseX = mx[0];
                  double rawMouseY = my[0];
                  int screenWidth = client.getWindow().getWidth();
                  int screenHeight = client.getWindow().getHeight();
                  int scaledWidth = client.getWindow().getScaledWidth();
                  int scaledHeight = client.getWindow().getScaledHeight();
                  double scaleX = (double)screenWidth / scaledWidth;
                  double scaleY = (double)screenHeight / scaledHeight;
                  int mouseX = (int)(rawMouseX / scaleX);
                  int mouseY = (int)(rawMouseY / scaleY);

                  if (client.currentScreen != null) {
                     // In a menu the dot follows the real cursor; that counts as activity.
                     lastMouseMoveTime = System.currentTimeMillis();
                     cursorX = (float)((double)mouseX / scaledWidth * 60.0);
                     cursorY = (float)((double)mouseY / scaledHeight * 60.0);
                  } else if (client.player != null) {
                     long now = System.currentTimeMillis();
                     float yaw = client.player.getYaw();
                     float pitch = client.player.getPitch();
                     if (Float.isNaN(lastYaw)) {
                        lastYaw = yaw;
                        lastPitch = pitch;
                     }

                     float dYaw = yaw - lastYaw;
                     if (dYaw > 180.0F) {
                        dYaw -= 360.0F;
                     }

                     if (dYaw < -180.0F) {
                        dYaw += 360.0F;
                     }

                     float dPitch = pitch - lastPitch;
                     lastYaw = yaw;
                     lastPitch = pitch;

                     // Any real movement counts as activity and keeps tracking alive.
                     if (Math.abs(dYaw) > 0.001F || Math.abs(dPitch) > 0.001F) {
                        lastMouseMoveTime = now;
                     }

                     if (now - lastMouseMoveTime > RESET_TIMEOUT_MS) {
                        // Idle: ease the dot back to center once, but stay live —
                        // the moment the mouse moves again tracking resumes.
                        cursorX += (30.0F - cursorX) * 0.15F;
                        cursorY += (30.0F - cursorY) * 0.15F;
                        if (Math.abs(cursorX - 30.0F) < 0.3F) cursorX = 30.0F;
                        if (Math.abs(cursorY - 30.0F) < 0.3F) cursorY = 30.0F;
                     } else {
                        cursorX += dYaw * VoidCyanClient.mouseStrokesSensitivity;
                        cursorY += dPitch * VoidCyanClient.mouseStrokesSensitivity;
                     }
                  } else {
                     cursorX = 30.0F;
                     cursorY = 30.0F;
                     lastYaw = Float.NaN;
                     lastPitch = Float.NaN;
                  }

                  int relX = Math.clamp((long)((int)cursorX), 3, 57);
                  int relY = Math.clamp((long)((int)cursorY), 3, 57);
                  renderBox(context, 0, 0, relX, relY);
               }
            }
         }
      }
   }

   public static void renderAt(DrawContext context, int boxX, int boxY, int mouseX, int mouseY, int scaledWidth, int scaledHeight) {
      int relX = (int)((double)mouseX / scaledWidth * 60.0);
      int relY = (int)((double)mouseY / scaledHeight * 60.0);
      relX = Math.clamp((long)relX, 3, 57);
      relY = Math.clamp((long)relY, 3, 57);
      renderBox(context, boxX, boxY, relX, relY);
   }

   private static void renderBox(DrawContext context, int bx, int by, int relX, int relY) {
      int symbolColor = 0xFF000000
         | (VoidCyanClient.mouseStrokesSymbolRed & 0xFF) << 16
         | (VoidCyanClient.mouseStrokesSymbolGreen & 0xFF) << 8
         | VoidCyanClient.mouseStrokesSymbolBlue & 0xFF;
      context.fill(bx, by, bx + 60, by + 60, 1610612736);
      context.fill(bx + 30 - 1, by + 2, bx + 30 + 1, by + 60 - 2, 822083583);
      context.fill(bx + 2, by + 30 - 1, bx + 60 - 2, by + 30 + 1, 822083583);
      int symX = bx + relX;
      int symY = by + relY;
      if (VoidCyanClient.mouseStrokesShowCross) {
         int crossSize = 5;
         context.fill(symX - crossSize, symY - 1, symX + crossSize + 1, symY + 2, symbolColor);
         context.fill(symX - 1, symY - crossSize, symX + 2, symY + crossSize + 1, symbolColor);
      } else {
         int radius = 4;
         int prevX = symX + radius;
         int prevY = symY;

         for (int i = 1; i <= 16; i++) {
            double angle = (Math.PI * 2) * i / 16.0;
            int curX = symX + (int)(Math.cos(angle) * radius);
            int curY = symY + (int)(Math.sin(angle) * radius);
            drawThinLine(context, prevX, prevY, curX, curY, symbolColor);
            prevX = curX;
            prevY = curY;
         }
      }
   }

   private static void drawThinLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
      int dx = Math.abs(x2 - x1);
      int dy = Math.abs(y2 - y1);
      int steps = Math.max(dx, dy);
      if (steps == 0) {
         context.fill(x1, y1, x1 + 1, y1 + 1, color);
      } else {
         float stepX = (float)(x2 - x1) / steps;
         float stepY = (float)(y2 - y1) / steps;
         float cx = x1;
         float cy = y1;

         for (int i = 0; i <= steps; i++) {
            context.fill((int)cx, (int)cy, (int)cx + 1, (int)cy + 1, color);
            cx += stepX;
            cy += stepY;
         }
      }
   }
}
