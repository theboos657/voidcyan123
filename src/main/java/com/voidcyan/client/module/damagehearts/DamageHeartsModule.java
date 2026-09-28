package com.voidcyan.client.module.damagehearts;

import com.voidcyan.client.VoidCyanClient;
import java.awt.Color;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class DamageHeartsModule {
   private static final HeartManager heartManager = new HeartManager(200);
   public static boolean isEnabled = false;
   public static boolean showPlayers = true;
   public static boolean showHostile = true;
   public static boolean showPassive = true;
   public static boolean showAnimals = true;
   public static boolean showBosses = true;
   public static float heartScale = 1.0F;
   public static float fadeTime = 2.0F;
   public static float lifetime = 2.5F;
   public static float rotationSpeed = 2.0F;
   public static float movementSpeed = 0.02F;
   public static boolean distanceScaling = true;
   public static float renderDistance = 64.0F;
   public static boolean glowEnabled = true;
   public static boolean critGlowEnabled = true;
   public static boolean rainbowMode = false;
   public static int normalColor = -65536;
   public static int explosionColor = -5635841;
   public static int magicColor = -16711681;
   public static int poisonColor = -16711936;
   public static boolean showNumeric = false;
   public static int combineDelayMs = 150;
   public static boolean goldenHeartsMode = true;
   private static long lastDamageTime = 0L;
   private static float combinedDamage = 0.0F;
   private static LivingEntity lastDamagedEntity = null;

   public static void onEntityDamaged(LivingEntity entity, float damage, DamageSource source) {
      if (isEnabled && shouldRenderForEntity(entity)) {
         long now = System.currentTimeMillis();
         if (lastDamagedEntity == entity && now - lastDamageTime < combineDelayMs) {
            combinedDamage += damage;
         } else {
            if (combinedDamage > 0.0F && lastDamagedEntity != null) {
               spawnHearts(lastDamagedEntity, combinedDamage, source);
            }

            combinedDamage = damage;
            lastDamagedEntity = entity;
         }

         lastDamageTime = now;
      }
   }

   public static void flushCombinedDamage() {
      if (combinedDamage > 0.0F && lastDamagedEntity != null) {
         spawnHearts(lastDamagedEntity, combinedDamage, null);
         combinedDamage = 0.0F;
         lastDamagedEntity = null;
      }
   }

   private static void spawnHearts(LivingEntity entity, float damage, DamageSource source) {
      float hearts = damage / 2.0F;
      int color = getDamageColor(source);
      if (goldenHeartsMode && entity.getAbsorptionAmount() > 0.0F && color == normalColor) {
         color = -22016;
      }

      boolean isCrit = damage >= 3.0F;
      double bodyX = entity.getX() + (Math.random() - 0.5) * 0.4;
      double bodyY = entity.getY() + entity.getStandingEyeHeight() * 0.7 + (Math.random() - 0.5) * 0.2;
      double bodyZ = entity.getZ() + (Math.random() - 0.5) * 0.4;
      Vec3d spawnPos = new Vec3d(bodyX, bodyY, bodyZ);
      int wholeHearts = (int)hearts;
      float fractionalHeart = hearts - wholeHearts;

      for (int i = 0; i < wholeHearts; i++) {
         Vec3d offset = new Vec3d((Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.3);
         FloatingHeart heart = new FloatingHeart(spawnPos.add(offset), 1.0F, color, isCrit);
         heart.lifetime = lifetime * 1000.0F;
         heartManager.addHeart(heart);
      }

      if (fractionalHeart > 0.01F) {
         Vec3d offset = new Vec3d((Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.3);
         FloatingHeart heart = new FloatingHeart(spawnPos.add(offset), fractionalHeart, color, isCrit);
         heart.scale *= fractionalHeart;
         heart.lifetime = lifetime * 1000.0F;
         heartManager.addHeart(heart);
      }
   }

   private static int getDamageColor(DamageSource source) {
      if (rainbowMode) {
         return Color.HSBtoRGB((float)(System.currentTimeMillis() % 3000L) / 3000.0F, 1.0F, 1.0F);
      } else if (source == null) {
         return normalColor;
      } else {
         String type = source.getType().msgId();
         if (type != null) {
            if (type.contains("explosion") || type.contains("explosion")) {
               return explosionColor;
            }

            if (type.contains("magic") || type.contains("indirectMagic")) {
               return magicColor;
            }

            if (type.contains("poison") || type.contains("wither")) {
               return poisonColor;
            }
         }

         return normalColor;
      }
   }

   private static boolean shouldRenderForEntity(LivingEntity entity) {
      if (!entity.isAlive()) {
         return false;
      } else {
         return entity.timeUntilRegen > 0 && entity.hurtTime == 0 ? false : shouldShowFor(entity);
      }
   }

   private static boolean shouldShowFor(Entity entity) {
      if (VoidCyanClient.isBotOrFloatingText(entity)) {
         return false;
      } else if (entity instanceof PlayerEntity) {
         return showPlayers;
      } else if (entity instanceof EnderDragonEntity) {
         return showBosses;
      } else if (entity instanceof HostileEntity) {
         return showHostile;
      } else if (entity instanceof AnimalEntity) {
         return showAnimals;
      } else {
         return entity instanceof PassiveEntity ? showPassive : true;
      }
   }

   public static void update() {
      if (isEnabled) {
         long now = System.currentTimeMillis();
         if (combinedDamage > 0.0F && now - lastDamageTime > combineDelayMs) {
            flushCombinedDamage();
         }

         heartManager.update(1.0F);
      }
   }

   public static HeartManager getHeartManager() {
      return heartManager;
   }

   public static void clear() {
      heartManager.clear();
      combinedDamage = 0.0F;
      lastDamagedEntity = null;
   }
}
