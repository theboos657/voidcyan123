package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ChatSettingsScreen extends BaseSettingsScreen {
   public ChatSettingsScreen(Screen parent) {
      super(parent, "Chat Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enabled", () -> VoidCyanClient.isChatModuleEnabled, val -> VoidCyanClient.isChatModuleEnabled = val);
      this.addBoolean(
         "Unlimited History", () -> VoidCyanClient.isChatUnlimitedHistoryEnabled, val -> VoidCyanClient.isChatUnlimitedHistoryEnabled = val
      );
      this.addBoolean("Persistent Chat", () -> VoidCyanClient.isChatPersistEnabled, val -> VoidCyanClient.isChatPersistEnabled = val);
      this.addBoolean("Range Filter", () -> VoidCyanClient.isChatRangeFilterEnabled, val -> VoidCyanClient.isChatRangeFilterEnabled = val);
      this.addIntSlider(
         "Range Filter Distance", 8, 256, () -> VoidCyanClient.chatRangeFilterDistance, val -> VoidCyanClient.chatRangeFilterDistance = val
      );
      this.addBoolean("Chat Bubbles", () -> VoidCyanClient.isChatBubblesEnabled, val -> VoidCyanClient.isChatBubblesEnabled = val);
      this.addBoolean("Compact Repeats", () -> VoidCyanClient.isChatRepeatCompactEnabled, val -> VoidCyanClient.isChatRepeatCompactEnabled = val);
      this.addColorPicker("Repeat Count Color", () -> VoidCyanClient.chatRepeatCountColor, val -> VoidCyanClient.chatRepeatCountColor = val);
      this.addBoolean("Block Harmful Words", () -> VoidCyanClient.isHarmfulWordFilterEnabled, val -> VoidCyanClient.isHarmfulWordFilterEnabled = val);
      this.addInputField("Blocked Words (comma separated)", () -> VoidCyanClient.harmfulWordsList, val -> VoidCyanClient.harmfulWordsList = val);
   }
}
