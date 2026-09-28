package com.voidcyan.client.module;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.SkinTextures;

public class GhostPlayerEntity extends AbstractClientPlayerEntity {
   private SkinTextures capturedSkin;

   public GhostPlayerEntity(ClientWorld world, GameProfile profile) {
      super(world, profile);
   }

   public void setCapturedSkin(SkinTextures skin) {
      this.capturedSkin = skin;
   }

   public void setGhostPosition(double x, double y, double z) {
      this.setPos(x, y, z);
      this.lastX = x;
      this.lastY = y;
      this.lastZ = z;
      this.lastRenderX = x;
      this.lastRenderY = y;
      this.lastRenderZ = z;
   }

   public void setGhostRotation(float yaw, float pitch, float headYaw, float bodyYaw) {
      this.setYaw(yaw);
      this.setPitch(pitch);
      this.setHeadYaw(headYaw);
      this.setBodyYaw(bodyYaw);
      this.lastYaw = yaw;
      this.lastPitch = pitch;
      this.lastBodyYaw = bodyYaw;
      this.lastHeadYaw = headYaw;
   }

   public void copyInventory(PlayerInventory source) {
      PlayerInventory inv = this.getInventory();

      for (int i = 0; i < inv.size(); i++) {
         inv.setStack(i, source.getStack(i).copy());
      }
   }

   public SkinTextures getSkin() {
      return this.capturedSkin != null ? this.capturedSkin : super.getSkin();
   }

   public void tick() {
   }

   public void tickMovement() {
   }

   protected void tickPlayerMovement() {
   }

   public void tickRiding() {
   }
}
