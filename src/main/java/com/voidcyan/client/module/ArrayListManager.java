package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ArrayListManager {
   private static final List<ArrayListManager.ModuleEntry> MODULES = new ArrayList<>();

   private static List<ArrayListManager.ModuleEntry> getActiveModules(MinecraftClient client) {
      List<ArrayListManager.ModuleEntry> active = new ArrayList<>();
      long now = System.currentTimeMillis();

      for (ArrayListManager.ModuleEntry m : MODULES) {
         boolean current = m.isEnabled.get();
         if (current && !m.wasEnabled) {
            m.timeEnabled = now;
         }

         m.wasEnabled = current;
         if (current) {
            active.add(m);
         }
      }

      if (client != null && client.textRenderer != null) {
         int mode = VoidCyanClient.arrayListSortMode;
         if (mode == 0) {
            active.sort(Comparator.comparing(mx -> mx.name));
         } else if (mode == 1) {
            active.sort(Comparator.<ArrayListManager.ModuleEntry>comparingInt(mx -> client.textRenderer.getWidth(mx.name)).reversed());
         } else if (mode == 2) {
            active.sort(Comparator.<ArrayListManager.ModuleEntry>comparingLong(mx -> mx.timeEnabled).reversed());
         }
      }

      return active;
   }

   public static int getWidth(MinecraftClient client) {
      if (client != null && client.textRenderer != null) {
         List<ArrayListManager.ModuleEntry> active = getActiveModules(client);
         if (active.isEmpty()) {
            return 100;
         } else {
            int maxW = 0;

            for (ArrayListManager.ModuleEntry m : active) {
               maxW = Math.max(maxW, client.textRenderer.getWidth(m.name));
            }

            return maxW + 6;
         }
      } else {
         return 100;
      }
   }

   public static int getHeight(MinecraftClient client) {
      if (client != null && client.textRenderer != null) {
         List<ArrayListManager.ModuleEntry> active = getActiveModules(client);
         return active.isEmpty() ? 12 : active.size() * 10;
      } else {
         return 12;
      }
   }

   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.textRenderer != null) {
         List<ArrayListManager.ModuleEntry> active = getActiveModules(client);
         if (!active.isEmpty()) {
            int yOffset = 0;
            long time = System.currentTimeMillis();
            int boxWidth = getWidth(client);

            for (int i = 0; i < active.size(); i++) {
               String name = active.get(i).name;
               if (VoidCyanClient.arrayListBackground) {
                  context.fill(0, yOffset, boxWidth, yOffset + 10, 1610612736);
               }

               int color = WatermarkManager.getThemeColor(time, i * -200);
               int textX = boxWidth - client.textRenderer.getWidth(name) - 2;
               context.drawTextWithShadow(client.textRenderer, name, textX, yOffset + 1, color);
               yOffset += 10;
            }
         }
      }
   }

   static {
      MODULES.add(new ArrayListManager.ModuleEntry("Inventory", () -> VoidCyanClient.isInvHudEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("FPS Counter", () -> VoidCyanClient.isFpsCounterEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Coordinates", () -> VoidCyanClient.isCoordinatesEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("CPS", () -> VoidCyanClient.isCpsEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Keystrokes", () -> VoidCyanClient.isKeystrokesEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Compass", () -> VoidCyanClient.isCompassEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Target HUD", () -> VoidCyanClient.isTargetHudEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Stats HUD", () -> VoidCyanClient.isStatsHudEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Freelook", () -> VoidCyanClient.isFreelookEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Zoom", () -> VoidCyanClient.isZoomEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Nick Hider", () -> VoidCyanClient.isNickHiderEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Armor Status", () -> VoidCyanClient.isArmorStatusEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Custom Hitbox", () -> VoidCyanClient.isCustomHitboxEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Damage Hearts", () -> VoidCyanClient.isDamageHeartsEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Health Indicators", () -> VoidCyanClient.isHealthIndicatorsEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Combo Counter", () -> VoidCyanClient.isComboCounterEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Ping Display", () -> VoidCyanClient.isPingDisplayEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Shulker Preview", () -> VoidCyanClient.isShulkerPreviewEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Chat", () -> VoidCyanClient.isChatModuleEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Persistent Chat", () -> VoidCyanClient.isChatPersistEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Action Bar", () -> VoidCyanClient.isActionBarEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Server Info", () -> VoidCyanClient.isServerInfoEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Pack Display", () -> VoidCyanClient.isPackDisplayEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Block Info HUD", () -> VoidCyanClient.isBlockIndicatorEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Custom Crosshair", () -> VoidCyanClient.isCustomCrosshairEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Custom F3", () -> VoidCyanClient.isCustomF3Enabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Death Info", () -> VoidCyanClient.isDeathInfoEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Waypoints", () -> VoidCyanClient.isWaypointsEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Entity Counter", () -> VoidCyanClient.isEntityCounterEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("TPS Display", () -> VoidCyanClient.isTpsDisplayEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Stopwatch", () -> VoidCyanClient.isStopwatchEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("FPS Graph", () -> VoidCyanClient.isFpsGraphEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("TNT Timer", () -> VoidCyanClient.isTntTimerEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Time Changer", () -> VoidCyanClient.isTimeChangerEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Potion Status", () -> VoidCyanClient.isPotionStatusEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Fullbright", () -> VoidCyanClient.isFullbrightEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Toggle Sprint", () -> VoidCyanClient.isToggleSprintEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Toggle Sneak", () -> VoidCyanClient.isToggleSneakEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Peer Nick", () -> VoidCyanClient.isPeerNickEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Drop Prevention", () -> VoidCyanClient.isDropPreventionEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Watermark", () -> VoidCyanClient.isWatermarkEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("ArrayList", () -> VoidCyanClient.isArrayListEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Big Head", () -> VoidCyanClient.isBigHeadEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("China Hat", () -> VoidCyanClient.isChinaHatEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Inv Highlight", () -> VoidCyanClient.isInvHighlightEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Mouse Strokes", () -> VoidCyanClient.isMouseStrokesEnabled));
      MODULES.add(new ArrayListManager.ModuleEntry("Transparent Shield", () -> VoidCyanClient.isTransparentShieldEnabled));
   }

   public static class ModuleEntry {
      public String name;
      public Supplier<Boolean> isEnabled;
      public boolean wasEnabled;
      public long timeEnabled;

      public ModuleEntry(String name, Supplier<Boolean> isEnabled) {
         this.name = name;
         this.isEnabled = isEnabled;
         this.wasEnabled = isEnabled.get();
         this.timeEnabled = this.wasEnabled ? System.currentTimeMillis() : 0L;
      }
   }
}
