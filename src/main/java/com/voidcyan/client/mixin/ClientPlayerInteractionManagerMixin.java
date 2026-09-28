package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.DropPreventionManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"dropCreativeStack"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDropCreativeStack(ItemStack stack, CallbackInfo ci) {
      if (DropPreventionManager.shouldPreventDrop(stack)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"attackEntity"},
      at = {@At("HEAD")}
   )
   private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
      VoidCyanClient.sessionHits++;
      VoidCyanClient.allTimeHits++;
      if (target instanceof EndCrystalEntity) {
         VoidCyanClient.sessionCrystalsBroken++;
         VoidCyanClient.allTimeCrystalsBroken++;
         // Effects module: fire "crystal" trigger effects at the crystal
         com.voidcyan.client.CritEffectsManager.onCrystalBreak(
            player.getEntityWorld(), target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ());
      }

      if (System.currentTimeMillis() - VoidCyanClient.lastSlotSwapTime <= 100L) {
         VoidCyanClient.sessionAttributeSwaps++;
         VoidCyanClient.allTimeAttributeSwaps++;
      }

      if (VoidCyanClient.isComboCounterEnabled) {
         boolean isValidHit = true;
         if (VoidCyanClient.comboVersion == 0) {
            isValidHit = player.getAttackCooldownProgress(0.5F) >= 0.9F;
         }

         if (isValidHit) {
            long now = System.currentTimeMillis();
            if (now - VoidCyanClient.lastComboHitTime > VoidCyanClient.comboTimeout) {
               VoidCyanClient.currentCombo = 1;
            } else {
               VoidCyanClient.currentCombo++;
            }

            VoidCyanClient.lastComboHitTime = now;
         } else {
            VoidCyanClient.currentCombo = 0;
         }
      }

      VoidCyanClient.lastAttackedEntity = target;
      VoidCyanClient.lastAttackTime = System.currentTimeMillis();
      VoidCyanClient.flashHitColor(target);
      VoidCyanClient.lastReachDistance = player.distanceTo(target);
      VoidCyanClient.lastReachTime = System.currentTimeMillis();
   }

   @Inject(
      method = {"breakBlock"},
      at = {@At("RETURN")}
   )
   private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if ((Boolean)cir.getReturnValue()) {
         VoidCyanClient.sessionBlocksBroken++;
         VoidCyanClient.allTimeBlocksBroken++;
      }
   }

   @Inject(
      method = {"interactBlock"},
      at = {@At("RETURN")}
   )
   private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
      if (((ActionResult)cir.getReturnValue()).isAccepted()) {
         VoidCyanClient.sessionBlocksPlaced++;
         VoidCyanClient.allTimeBlocksPlaced++;
         ItemStack stack = player.getStackInHand(hand);
         if (stack.getItem() == Items.END_CRYSTAL) {
            VoidCyanClient.sessionCrystalsPlaced++;
            VoidCyanClient.allTimeCrystalsPlaced++;
         } else if (stack.getItem() == Items.RESPAWN_ANCHOR) {
            VoidCyanClient.sessionAnchorsPlaced++;
            VoidCyanClient.allTimeAnchorsPlaced++;
         }

         BlockState state = MinecraftClient.getInstance().world.getBlockState(hitResult.getBlockPos());
         if (state.getBlock() == Blocks.RESPAWN_ANCHOR) {
            if (stack.getItem() == Items.GLOWSTONE) {
               VoidCyanClient.sessionAnchorsCharged++;
               VoidCyanClient.allTimeAnchorsCharged++;
            } else if ((Integer)state.get(RespawnAnchorBlock.CHARGES) > 0 && MinecraftClient.getInstance().world.getRegistryKey() != World.NETHER) {
               VoidCyanClient.sessionAnchorsBlown++;
               VoidCyanClient.allTimeAnchorsBlown++;
            }
         }

         VoidCyanClient.lastInteractTime = System.currentTimeMillis();
         VoidCyanClient.lastInteractPos = hitResult.getBlockPos();
      }
   }
}
