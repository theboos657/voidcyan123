package com.voidcyan.client.util;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TextReplacer {
   public static Text replaceNames(Text text) {
      if (!VoidCyanClient.isNickHiderEnabled || text == null) {
         return text;
      }
      String targetName = VoidCyanClient.nickHiderTargetName;
      if (targetName == null || targetName.trim().isEmpty()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.getSession() != null && mc.getSession().getUsername() != null && !mc.getSession().getUsername().isEmpty()) {
            targetName = mc.getSession().getUsername();
         } else if (mc.player != null) {
            targetName = mc.player.getName().getString();
         }
      }
      String replacementName = VoidCyanClient.nickHiderReplacementName;
      if (replacementName == null || replacementName.isEmpty()) {
         replacementName = "Hidden";
      }
      if (targetName != null && !targetName.isEmpty()) {
         String originalString = text.getString();
         if (originalString.contains(targetName)) {
            String replacedString = originalString.replace(targetName, replacementName);
            return Text.literal(replacedString).setStyle(text.getStyle());
         }
      }
      return text;
   }
}
