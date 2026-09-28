package com.voidcyan.client.mixin;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({DrawContext.class})
public class MixinDrawContext {
   @ModifyVariable(
      method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private Text modifyDrawText(Text text) {
      if (text != null && NameProtect.INSTANCE.isEnabled()) {
         Text processed = NameProtect.processText(text);
         if (processed != null) {
            return processed;
         }
      }

      return text;
   }

   @ModifyVariable(
      method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private String modifyDrawTextString(String text) {
      if (text != null && NameProtect.INSTANCE.isEnabled()) {
         return NameProtect.INSTANCE.replace(text);
      }
      return text;
   }
}
