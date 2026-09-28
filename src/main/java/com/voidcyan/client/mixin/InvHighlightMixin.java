package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HandledScreen.class})
public class InvHighlightMixin {
   @Inject(
      method = {"drawSlot"},
      at = {@At("HEAD")}
   )
   private void onDrawSlot(DrawContext context, Slot slot, int slotX, int slotY, CallbackInfo ci) {
      if (VoidCyanClient.isInvHighlightEnabled) {
         ItemStack stack = slot.getStack();
         if (!stack.isEmpty()) {
            Identifier id = Registries.ITEM.getId(stack.getItem());
            if (id != null) {
               String itemId = id.toString();
               String configuredItems = VoidCyanClient.invHighlightItems;
               if (configuredItems != null && !configuredItems.isEmpty()) {
                  boolean match = false;

                  for (String itemId2 : configuredItems.split(",")) {
                     if (itemId2.trim().equalsIgnoreCase(itemId)) {
                        match = true;
                        break;
                     }
                  }

                  if (match) {
                     int x = slot.x;
                     int y = slot.y;
                     int argb = 1073741824
                        | (VoidCyanClient.invHighlightRed & 0xFF) << 16
                        | (VoidCyanClient.invHighlightGreen & 0xFF) << 8
                        | VoidCyanClient.invHighlightBlue & 0xFF;
                     context.fill(x, y, x + 16, y + 16, argb);
                  }
               }
            }
         }
      }
   }
}
