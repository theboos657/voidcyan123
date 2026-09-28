package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class TotemTraceSettingsScreen extends BaseSettingsScreen {
   public TotemTraceSettingsScreen(Screen parent) {
      super(parent, "Totem Trace Settings");
   }

   @Override
   protected void buildSettings() {
      this.addIntSlider("Color Alpha", 0, 255, () -> VoidCyanClient.totemTraceColorAlpha, val -> VoidCyanClient.totemTraceColorAlpha = val);
      this.addIntSlider("Color Blue", 0, 255, () -> VoidCyanClient.totemTraceColorBlue, val -> VoidCyanClient.totemTraceColorBlue = val);
      this.addIntSlider("Color Green", 0, 255, () -> VoidCyanClient.totemTraceColorGreen, val -> VoidCyanClient.totemTraceColorGreen = val);
      this.addIntSlider("Color Red", 0, 255, () -> VoidCyanClient.totemTraceColorRed, val -> VoidCyanClient.totemTraceColorRed = val);
      this.addBoolean("Detect Others", () -> VoidCyanClient.totemTraceDetectOthers, val -> VoidCyanClient.totemTraceDetectOthers = val);
      this.addBoolean("Detect Self", () -> VoidCyanClient.totemTraceDetectSelf, val -> VoidCyanClient.totemTraceDetectSelf = val);
      this.addIntSlider("Duration", 1, 60, () -> VoidCyanClient.totemTraceDuration, val -> VoidCyanClient.totemTraceDuration = val);
      this.addBoolean("Show Armor", () -> VoidCyanClient.totemTraceShowArmor, val -> VoidCyanClient.totemTraceShowArmor = val);
      this.addBoolean("Show Color", () -> VoidCyanClient.totemTraceShowColor, val -> VoidCyanClient.totemTraceShowColor = val);
   }
}
