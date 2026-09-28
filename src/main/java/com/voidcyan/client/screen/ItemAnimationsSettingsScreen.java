package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ItemAnimationsSettingsScreen extends BaseSettingsScreen {
   private static final String[] MODES = new String[]{"Vanilla", "Swipe", "Down", "Smooth", "Power", "Feast", "Custom"};

   public ItemAnimationsSettingsScreen(Screen parent) {
      super(parent, "Item Animations");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enabled", () -> VoidCyanClient.isItemAnimationsEnabled, val -> VoidCyanClient.isItemAnimationsEnabled = val);
      this.addEnum("Animation Mode", MODES, () -> VoidCyanClient.itemAnimationMode, val -> VoidCyanClient.itemAnimationMode = val);
      this.addButton("Resting Pos:", "Use View Model", () -> this.client.setScreen(new ViewModelSettingsScreen(this.parent)));
      this.addSlider("Custom Swing Pitch", -180.0F, 180.0F, () -> VoidCyanClient.customSwingPitch, val -> VoidCyanClient.customSwingPitch = val);
      this.addSlider("Custom Swing Yaw", -180.0F, 180.0F, () -> VoidCyanClient.customSwingYaw, val -> VoidCyanClient.customSwingYaw = val);
      this.addSlider("Custom Swing Roll", -180.0F, 180.0F, () -> VoidCyanClient.customSwingRoll, val -> VoidCyanClient.customSwingRoll = val);
   }
}
