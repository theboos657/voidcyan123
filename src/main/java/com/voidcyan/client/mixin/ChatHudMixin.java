package com.voidcyan.client.mixin;

import com.voidcyan.client.util.NameReplacer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ChatHud.class})
public class ChatHudMixin {
   @ModifyVariable(
      method = {"addMessage"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text modifyChatMessage(Text message) {
      return NameReplacer.replaceInText(message);
   }
}
