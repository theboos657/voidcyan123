package com.voidcyan.client.mixin;

import com.mojang.authlib.GameProfile;
import com.voidcyan.client.util.NameProtect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerEntity.class})
public class MixinEntity {
   private GameProfile customProfile = null;

   @Inject(
      method = {"getName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void injectNameProtectPlayerName(CallbackInfoReturnable<Text> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         PlayerEntity player = (PlayerEntity)(Object)this;
         if (player.getEntityWorld() != null && player.getEntityWorld().isClient()) {
            Text original = (Text)cir.getReturnValue();
            if (original != null) {
               Text processed = NameProtect.processText(original);
               if (processed != null && !processed.getString().equals(original.getString())) {
                  cir.setReturnValue(processed);
               }
            }
         }
      }
   }

   @Inject(
      method = {"getDisplayName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void injectNameProtectDisplayName(CallbackInfoReturnable<Text> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         PlayerEntity player = (PlayerEntity)(Object)this;
         if (player.getEntityWorld() != null && player.getEntityWorld().isClient()) {
            Text original = (Text)cir.getReturnValue();
            if (original != null) {
               Text processed = NameProtect.processText(original);
               if (processed != null && !processed.getString().equals(original.getString())) {
                  cir.setReturnValue(processed);
               }
            }
         }
      }
   }

   @Inject(
      method = {"getGameProfile"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void injectNameProtectGameProfile(CallbackInfoReturnable<GameProfile> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         PlayerEntity player = (PlayerEntity)(Object)this;
         if (player.getEntityWorld() == null || !player.getEntityWorld().isClient()) {
            return;
         }

         GameProfile profile = (GameProfile)cir.getReturnValue();
         if (profile != null) {
            String originalName = profile.name();
            String replacedName = NameProtect.INSTANCE.replace(originalName);
            if (!replacedName.equals(originalName)) {
               if (this.customProfile == null || !this.customProfile.name().equals(replacedName)) {
                  this.customProfile = new GameProfile(profile.id(), replacedName, profile.properties());
               }

               cir.setReturnValue(this.customProfile);
            }
         }
      }
   }
}
