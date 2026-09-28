package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class MouseStrokesSettingsScreen extends BaseSettingsScreen {
   public MouseStrokesSettingsScreen(Screen parent) {
      super(parent, "Mouse Strokes Settings");
   }

   @Override
   protected void buildSettings() {
      this.addSlider("Sensitivity", 0.2F, 8.0F, () -> VoidCyanClient.mouseStrokesSensitivity, val -> VoidCyanClient.mouseStrokesSensitivity = val);
      this.addBoolean("Show Cross", () -> VoidCyanClient.mouseStrokesShowCross, val -> VoidCyanClient.mouseStrokesShowCross = val);
      this.addIntSlider("Symbol Red", 0, 255, () -> VoidCyanClient.mouseStrokesSymbolRed, val -> VoidCyanClient.mouseStrokesSymbolRed = val);
      this.addIntSlider("Box Red", 0, 255, () -> VoidCyanClient.mouseStrokesBoxRed, val -> VoidCyanClient.mouseStrokesBoxRed = val);
   }
}
