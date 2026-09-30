package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class InvHudSettingsScreen extends BaseSettingsScreen {
   public InvHudSettingsScreen(Screen parent) {
      super(parent, "Inventory Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Show Borders", () -> VoidCyanClient.invHudShowBorders, val -> VoidCyanClient.invHudShowBorders = val);
      this.addIntSlider("Border Thickness", 0, 5, () -> VoidCyanClient.invHudBorderThickness, val -> VoidCyanClient.invHudBorderThickness = val);
      this.addBoolean("Border Glow", () -> VoidCyanClient.invHudGlow, val -> VoidCyanClient.invHudGlow = val);
      this.addBoolean("Show Hotbar", () -> VoidCyanClient.invHudShowHotbar, val -> VoidCyanClient.invHudShowHotbar = val);
      this.addBoolean("Show Item Count", () -> VoidCyanClient.invHudShowItemCount, val -> VoidCyanClient.invHudShowItemCount = val);
      this.addBoolean("Transparent Background", () -> VoidCyanClient.invHudTransparent, val -> VoidCyanClient.invHudTransparent = val);
   }
}
