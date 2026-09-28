package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.AlarmSoundManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;

public class LowHealthAlarmManager {
   private static long lastBeepTime = 0L;

   public static void render(DrawContext context) {
      if (VoidCyanClient.isLowHealthAlarmEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (VoidCyanClient.lowHealthTotemThreshold > 0) {
               boolean hasTotemInHand = client.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)
                  || client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
               if (hasTotemInHand) {
                  return;
               }
            }

            float currentHealth = client.player.getHealth();
            if (currentHealth <= VoidCyanClient.lowHealthAlarmThreshold) {
               if (VoidCyanClient.lowHealthAlarmSound) {
                  long now = System.currentTimeMillis();
                  if (now - lastBeepTime > 1000L) {
                     AlarmSoundManager.playAlarm(VoidCyanClient.lowHealthAlarmSoundFile);
                     lastBeepTime = now;
                  }
               }

               if (VoidCyanClient.lowHealthAlarmText) {
                  String text = "LOW HEALTH!";
                  int color;
                  if (VoidCyanClient.lowHealthUseThemeColor) {
                     color = 0xFF000000 | VoidCyanClient.getPrimaryColor();
                  } else {
                     color = -65536;
                  }

                  context.drawTextWithShadow(client.textRenderer, text, 0, 0, color);
               }
            }
         }
      }
   }

   public static int getWidth(MinecraftClient client) {
      return client != null && client.textRenderer != null ? client.textRenderer.getWidth("LOW HEALTH!") + 2 : 100;
   }

   public static int getHeight() {
      return 12;
   }
}
