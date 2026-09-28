package com.voidcyan.client.mixin;

import com.voidcyan.client.module.DropPreventionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ScreenHandler.class})
public class ScreenHandlerMixin {
   @Inject(
      method = {"onSlotClick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
      if (actionType == SlotActionType.THROW) {
         ScreenHandler handler = (ScreenHandler)(Object)this;
         ItemStack cursorStack = handler.getCursorStack();
         if (!cursorStack.isEmpty() && DropPreventionManager.shouldPreventDrop(cursorStack)) {
            ci.cancel();
            return;
         }

         if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            ItemStack slotStack = ((Slot)handler.slots.get(slotIndex)).getStack();
            if (!slotStack.isEmpty() && DropPreventionManager.shouldPreventDrop(slotStack)) {
               ci.cancel();
               return;
            }
         }
      }

      if (actionType == SlotActionType.PICKUP && slotIndex == -999) {
         ScreenHandler handlerx = (ScreenHandler)(Object)this;
         ItemStack cursorStackx = handlerx.getCursorStack();
         if (!cursorStackx.isEmpty() && DropPreventionManager.shouldPreventDrop(cursorStackx)) {
            ci.cancel();
            return;
         }
      }
   }
}
