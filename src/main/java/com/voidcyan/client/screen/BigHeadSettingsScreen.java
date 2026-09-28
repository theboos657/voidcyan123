package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class BigHeadSettingsScreen extends BaseSettingsScreen {
   public BigHeadSettingsScreen(Screen parent) {
      super(parent, "Big Head Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Friends", () -> VoidCyanClient.bigHeadFriends, val -> VoidCyanClient.bigHeadFriends = val);
      this.addBoolean("Others", () -> VoidCyanClient.bigHeadOthers, val -> VoidCyanClient.bigHeadOthers = val);
      this.addBoolean("Self", () -> VoidCyanClient.bigHeadSelf, val -> VoidCyanClient.bigHeadSelf = val);
      this.addSlider("Size", 0.5F, 3.0F, () -> VoidCyanClient.bigHeadScale, val -> VoidCyanClient.bigHeadScale = val);
   }
}
