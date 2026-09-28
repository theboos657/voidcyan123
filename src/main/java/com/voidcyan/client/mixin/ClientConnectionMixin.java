package com.voidcyan.client.mixin;

import com.voidcyan.client.module.DropPreventionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public class ClientConnectionMixin {
   @Inject(
      method = {"send"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void voidcyan$onSend(Packet<?> packet, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         if (packet instanceof PlayerActionC2SPacket actionPacket) {
            if (actionPacket.getAction() == Action.DROP_ITEM || actionPacket.getAction() == Action.DROP_ALL_ITEMS) {
               ItemStack stack = mc.player.getMainHandStack();
               if (DropPreventionManager.shouldPreventDrop(stack)) {
                  ci.cancel();
               }
            }
         } else if (packet instanceof ClickSlotC2SPacket clickPacket && clickPacket.actionType() == SlotActionType.THROW) {
            ItemStack stack;
            if (clickPacket.slot() == -999) {
               stack = mc.player.currentScreenHandler.getCursorStack();
            } else {
               stack = mc.player.currentScreenHandler.getSlot(clickPacket.slot()).getStack();
            }

            if (DropPreventionManager.shouldPreventDrop(stack)) {
               ci.cancel();
               mc.player.currentScreenHandler.updateToClient();
            }
         }
      }
   }
}
