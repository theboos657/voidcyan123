package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.VoidCyanMenuScreen;
import com.voidcyan.client.util.WaypointManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.text.Text;

public final class DeathInfoManager {
   private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
   private static final int LINE_H = 12;
   private static final int PAD = 5;
   private static final int BTN_H = 18;
   private static final int BTN_W = 110;
   public static boolean expanded = false;
   public static String lastDeathCause = "";
   public static String waypointFeedback = "";
   private static long waypointFeedbackUntil = 0L;

   private DeathInfoManager() {
   }

   public static void recordDeathFromDeathScreen(Text deathMessage, ClientPlayerEntity player) {
      if (player != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (deathMessage != null) {
            lastDeathCause = deathMessage.getString();
         }

         if (client.world != null) {
            VoidCyanClient.lastDeathX = (int)player.getX();
            VoidCyanClient.lastDeathY = (int)player.getY();
            VoidCyanClient.lastDeathZ = (int)player.getZ();
            VoidCyanClient.lastDeathDimension = client.world.getRegistryKey().getValue().getPath();
         } else {
            VoidCyanClient.lastDeathX = (int)player.getX();
            VoidCyanClient.lastDeathY = (int)player.getY();
            VoidCyanClient.lastDeathZ = (int)player.getZ();
         }

         VoidCyanClient.lastDeathTime = System.currentTimeMillis();
         VoidCyanClient.hasDeathInfo = true;
         expanded = false;
         if (lastDeathCause.isBlank()) {
            lastDeathCause = inferFallbackCause(client);
         }

         VoidCyanClient.saveConfig();
      }
   }

   public static void recordDeathLocation(MinecraftClient client) {
      if (client != null && client.player != null && client.world != null) {
         VoidCyanClient.lastDeathX = (int)client.player.getX();
         VoidCyanClient.lastDeathY = (int)client.player.getY();
         VoidCyanClient.lastDeathZ = (int)client.player.getZ();
         VoidCyanClient.lastDeathDimension = client.world.getRegistryKey().getValue().getPath();
         VoidCyanClient.lastDeathTime = System.currentTimeMillis();
         VoidCyanClient.hasDeathInfo = true;
         expanded = false;
         if (lastDeathCause.isBlank()) {
            lastDeathCause = inferFallbackCause(client);
         }
      }
   }

   public static void recordDeathMessage(DeathMessageS2CPacket packet, MinecraftClient client) {
      if (client != null && client.player != null) {
         if (packet.playerId() == client.player.getId()) {
            lastDeathCause = packet.message().getString();
            recordDeathLocation(client);
         }
      }
   }

   private static String inferFallbackCause(MinecraftClient client) {
      String killType = VoidCyanClient.getKillType(client.player);

      return switch (killType) {
         case "crystal" -> "Killed by End Crystal";
         case "anchor" -> "Killed by Respawn Anchor";
         case "melee" -> "Killed in combat";
         default -> "Unknown cause";
      };
   }

   public static void load(Properties props) {
      lastDeathCause = props.getProperty("lastDeathCause", "");
   }

   public static void save(Properties props) {
      props.setProperty("lastDeathCause", lastDeathCause);
   }

   public static int getWidth(MinecraftClient client) {
      return computeWidth(client, false);
   }

   public static int getPreviewWidth(MinecraftClient client) {
      return computeWidth(client, !VoidCyanClient.hasDeathInfo);
   }

   private static int computeWidth(MinecraftClient client, boolean sample) {
      if (client != null && client.textRenderer != null) {
         int max = 0;

         for (String line : buildDisplayLines(client, sample)) {
            max = Math.max(max, client.textRenderer.getWidth(line));
         }

         if (expanded && !sample) {
            max = Math.max(max, 110);
         }

         return max + 10;
      } else {
         return 150;
      }
   }

   public static int getHeight() {
      return computeHeight(false);
   }

   public static int getPreviewHeight() {
      return computeHeight(!VoidCyanClient.hasDeathInfo);
   }

   private static int computeHeight(boolean sample) {
      int lineCount = expanded ? (sample ? 3 : 4) : 2;
      int h = 10 + lineCount * 12;
      if (expanded && !sample) {
         h += 22;
      }

      return h;
   }

   private static List<String> buildLines(MinecraftClient client, boolean forRender) {
      return buildDisplayLines(client, false);
   }

   private static List<String> buildDisplayLines(MinecraftClient client, boolean sample) {
      List<String> lines = new ArrayList<>();
      if (sample) {
         lines.add("☠ Slain by Zombie");
         lines.add(formatClock(System.currentTimeMillis()) + " · just now");
         if (expanded) {
            lines.add("XYZ: 100, 64, -200 (Overworld)");
         }

         return lines;
      } else {
         String cause = lastDeathCause.isBlank() ? "Unknown cause" : lastDeathCause;
         lines.add("☠ " + cause);
         lines.add(formatClock(VoidCyanClient.lastDeathTime) + " · " + formatElapsed(VoidCyanClient.lastDeathTime));
         if (expanded) {
            String dim = formatDimension(VoidCyanClient.lastDeathDimension);
            lines.add(String.format("XYZ: %d, %d, %d (%s)", VoidCyanClient.lastDeathX, VoidCyanClient.lastDeathY, VoidCyanClient.lastDeathZ, dim));
         }

         return lines;
      }
   }

   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.currentScreen == null || client.currentScreen instanceof VoidCyanMenuScreen) {
         if (VoidCyanClient.hasDeathInfo) {
            drawPanel(context, client, 0, 0, false, false);
         }
      }
   }

   public static void renderPreview(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      boolean sample = !VoidCyanClient.hasDeathInfo;
      drawPanel(context, client, 0, 0, sample, true);
   }

   public static void renderOnDeathScreen(DrawContext context, int screenWidth, int screenHeight) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (VoidCyanClient.hasDeathInfo) {
         int x = getAnchorX(client, screenWidth);
         int y = getAnchorY(client, screenHeight);
         context.getMatrices().pushMatrix();
         context.getMatrices().translate(x, y);
         context.getMatrices().scale(VoidCyanClient.deathInfoScale, VoidCyanClient.deathInfoScale);
         drawPanel(context, client, 0, 0, false, false);
         context.getMatrices().popMatrix();
      }
   }

   private static int getAnchorX(MinecraftClient client, int screenWidth) {
      return client.currentScreen instanceof DeathScreen ? 10 : VoidCyanClient.deathInfoX;
   }

   private static int getAnchorY(MinecraftClient client, int screenHeight) {
      return client.currentScreen instanceof DeathScreen ? screenHeight / 4 + 8 : VoidCyanClient.deathInfoY;
   }

   private static void drawPanel(DrawContext context, MinecraftClient client, int x, int y, boolean sample, boolean settingsPreview) {
      List<String> lines = buildDisplayLines(client, sample);
      int boxW = sample ? getPreviewWidth(client) : getWidth(client);
      int boxH = sample ? getPreviewHeight() : getHeight();
      context.fill(x, y, x + boxW, y + boxH, Integer.MIN_VALUE);
      drawBorder(context, x, y, boxW, boxH, -16711681);
      int textY = y + 5;

      for (int i = 0; i < lines.size(); i++) {
         int color = i == 0 ? -43691 : (i == 1 ? -5592406 : -1);
         context.drawTextWithShadow(client.textRenderer, Text.literal(lines.get(i)), x + 5, textY, color);
         textY += 12;
      }

      if (expanded && !sample) {
         int btnX = x + 5;
         int btnY = y + boxH - 5 - 18;
         boolean hover = isMouseOverButton(client, btnX, btnY, false);
         context.fill(btnX, btnY, btnX + 110, btnY + 18, hover ? -1442775041 : -2013265920);
         drawBorder(context, btnX, btnY, 110, 18, -16711681);
         String label = "Add Waypoint";
         context.drawTextWithShadow(client.textRenderer, Text.literal(label), btnX + 55 - client.textRenderer.getWidth(label) / 2, btnY + 5, -1);
      }

      if (System.currentTimeMillis() < waypointFeedbackUntil && !waypointFeedback.isBlank()) {
         int fy = y + boxH + 4;
         context.drawTextWithShadow(client.textRenderer, Text.literal(waypointFeedback), x + 5, fy, -11141291);
      }

      if (!expanded && !settingsPreview) {
         String hint = "Click for coords";
         context.drawTextWithShadow(client.textRenderer, Text.literal(hint), x + boxW - 5 - client.textRenderer.getWidth(hint), y + boxH - 5 - 2, -10066330);
      }
   }

   private static boolean isMouseOverButton(MinecraftClient client, int btnX, int btnY, boolean sample) {
      if (client.mouse.isCursorLocked()) {
         return false;
      } else {
         double mx = client.mouse.getScaledX(client.getWindow());
         double my = client.mouse.getScaledY(client.getWindow());
         int sx = getAnchorX(client, client.getWindow().getScaledWidth());
         int sy = getAnchorY(client, client.getWindow().getScaledHeight());
         float scale = VoidCyanClient.deathInfoScale;
         double lx = (mx - sx) / scale;
         double ly = (my - sy) / scale;
         return lx >= btnX && lx < btnX + 110 && ly >= btnY && ly < btnY + 18;
      }
   }

   private static boolean isMouseOverButton(MinecraftClient client, int btnX, int btnY, int anchorX, int anchorY) {
      double mx = client.mouse.getScaledX(client.getWindow());
      double my = client.mouse.getScaledY(client.getWindow());
      float scale = VoidCyanClient.deathInfoScale;
      double lx = (mx - anchorX) / scale;
      double ly = (my - anchorY) / scale;
      return lx >= btnX && lx < btnX + 110 && ly >= btnY && ly < btnY + 18;
   }

   public static boolean handlePreviewClick(double mouseX, double mouseY, int anchorX, int anchorY, int button) {
      if (button != 0) {
         return false;
      } else {
         MinecraftClient client = MinecraftClient.getInstance();
         int w = getPreviewWidth(client);
         int h = getPreviewHeight();
         if (!(mouseX < anchorX) && !(mouseY < anchorY) && !(mouseX >= anchorX + w) && !(mouseY >= anchorY + h)) {
            double lx = mouseX - anchorX;
            double ly = mouseY - anchorY;
            if (expanded && VoidCyanClient.hasDeathInfo) {
               int boxH = getPreviewHeight();
               int btnX = 5;
               int btnY = boxH - 5 - 18;
               if (lx >= btnX && lx < btnX + 110 && ly >= btnY && ly < btnY + 18) {
                  addDeathWaypoint();
                  return true;
               }
            }

            expanded = !expanded;
            return true;
         } else {
            return false;
         }
      }
   }

   private static boolean isMouseOverButton(MinecraftClient client, int btnX, int btnY) {
      return isMouseOverButton(client, btnX, btnY, false);
   }

   public static boolean handleClick(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else if (VoidCyanClient.isDeathInfoEnabled && VoidCyanClient.hasDeathInfo) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.mouse.isCursorLocked()) {
            return false;
         } else {
            int sx = getAnchorX(client, client.getWindow().getScaledWidth());
            int sy = getAnchorY(client, client.getWindow().getScaledHeight());
            float scale = VoidCyanClient.deathInfoScale;
            int w = (int)(getWidth(client) * scale);
            int h = (int)(getHeight() * scale);
            if (!(mouseX < sx) && !(mouseY < sy) && !(mouseX >= sx + w) && !(mouseY >= sy + h)) {
               double lx = (mouseX - sx) / scale;
               double ly = (mouseY - sy) / scale;
               if (expanded) {
                  int boxH = getHeight();
                  int btnX = 5;
                  int btnY = boxH - 5 - 18;
                  if (lx >= btnX && lx < btnX + 110 && ly >= btnY && ly < btnY + 18) {
                     addDeathWaypoint();
                     return true;
                  }
               }

               expanded = !expanded;
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static void addDeathWaypoint() {
      if (VoidCyanClient.hasDeathInfo) {
         String dim = VoidCyanClient.lastDeathDimension;
         if (dim == null || dim.isBlank()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
               dim = client.world.getRegistryKey().getValue().getPath();
            } else {
               dim = "overworld";
            }
         }

         String name = "Death " + formatClock(VoidCyanClient.lastDeathTime);
         WaypointManager.addWaypoint(
            name,
            VoidCyanClient.lastDeathX,
            VoidCyanClient.lastDeathY,
            VoidCyanClient.lastDeathZ,
            dim,
            -43691,
            true,
            10,
            100,
            true,
            true,
            "minecraft:skeleton_skull"
         );
         waypointFeedback = "Waypoint added!";
         waypointFeedbackUntil = System.currentTimeMillis() + 2500L;
      }
   }

   private static String formatClock(long timestamp) {
      return timestamp <= 0L ? "--:--" : Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(CLOCK_FORMAT);
   }

   private static String formatElapsed(long timestamp) {
      if (timestamp <= 0L) {
         return "just now";
      } else {
         long elapsed = (System.currentTimeMillis() - timestamp) / 1000L;
         if (elapsed < 60L) {
            return elapsed + "s ago";
         } else if (elapsed < 3600L) {
            return elapsed / 60L + "m ago";
         } else {
            return elapsed < 86400L ? elapsed / 3600L + "h ago" : elapsed / 86400L + "d ago";
         }
      }
   }

   private static String formatDimension(String dimension) {
      return dimension == null ? "Unknown" : dimension.replace("the_nether", "Nether").replace("the_end", "End").replace("overworld", "Overworld");
   }

   private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y, x + 1, y + h, color);
      context.fill(x + w - 1, y, x + w, y + h, color);
   }
}
