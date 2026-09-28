package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ParticlesSettingsScreen extends BaseSettingsScreen {
   public ParticlesSettingsScreen(Screen parent) {
      super(parent, "Particles");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Critical Hit Particles", () -> VoidCyanClient.isCriticalParticlesEnabled, val -> VoidCyanClient.isCriticalParticlesEnabled = val);
      this.addBoolean("Damage Particles", () -> VoidCyanClient.isDamageParticlesEnabled, val -> VoidCyanClient.isDamageParticlesEnabled = val);
      this.addBoolean("Explosion Particles", () -> VoidCyanClient.isExplosionParticlesEnabled, val -> VoidCyanClient.isExplosionParticlesEnabled = val);
      this.addBoolean("Potion Particles", () -> VoidCyanClient.isPotionParticlesEnabled, val -> VoidCyanClient.isPotionParticlesEnabled = val);
      this.addBoolean("All Particles", () -> VoidCyanClient.isParticlesEnabled, val -> VoidCyanClient.isParticlesEnabled = val);
   }
}
