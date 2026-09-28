package com.voidcyan.client.mixin;

import com.mojang.authlib.GameProfile;
import com.voidcyan.client.util.NameProtect;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListEntry.class})
public class MixinPlayerListEntry {
   @Shadow
   @Final
   private GameProfile profile;
   private GameProfile customProfile = null;

   @Inject(
      method = {"getProfile"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void injectGetProfile(CallbackInfoReturnable<GameProfile> cir) {
      if (NameProtect.INSTANCE.isEnabled() && this.profile != null) {
         String originalName = this.profile.name();
         String replacedName = NameProtect.INSTANCE.replace(originalName);
         if (!replacedName.equals(originalName)) {
            if (this.customProfile == null || !this.customProfile.name().equals(replacedName)) {
               this.customProfile = new GameProfile(this.profile.id(), replacedName, this.profile.properties());
            }

            cir.setReturnValue(this.customProfile);
         }
      }
   }

   @Inject(
      method = {"getDisplayName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void injectGetDisplayName(CallbackInfoReturnable<Text> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         Text original = (Text)cir.getReturnValue();
         if (original != null) {
            Text processed = NameProtect.processText(original);
            if (processed != null && processed != original) {
               cir.setReturnValue(processed);
            }
         }
      }
   }
}
