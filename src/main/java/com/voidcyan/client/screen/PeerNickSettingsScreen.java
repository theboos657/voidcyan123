package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class PeerNickSettingsScreen extends BaseSettingsScreen {
   public PeerNickSettingsScreen(Screen parent) {
      super(parent, "Peer Nick Settings");
   }

   @Override
   protected void buildSettings() {
      this.addInputField("Nicknames (name=nick, name=nick)", () -> VoidCyanClient.peerNicknames, val -> VoidCyanClient.peerNicknames = val);
   }
}
