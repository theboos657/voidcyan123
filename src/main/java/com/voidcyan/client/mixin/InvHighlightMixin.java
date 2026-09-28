package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import java.util.Arrays;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HandledScreen.class})
public class InvHighlightMixin {
   @Unique
   private static String voidcyan$parsedSource;
   @Unique
   private static String[] voidcyan$parsedItems = new String[0];

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
                  // Re-split only when the setting changes; this runs for every slot every frame.
                  if (configuredItems != voidcyan$parsedSource) {
                     voidcyan$parsedItems = Arrays.stream(configuredItems.split(",")).map(String::trim).toArray(String[]::new);
                     voidcyan$parsedSource = configuredItems;
                  }

                  boolean match = false;
                  for (String configured : voidcyan$parsedItems) {
                     if (configured.equalsIgnoreCase(itemId)) {
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
