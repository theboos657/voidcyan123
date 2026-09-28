package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PotWarningManager {
   private static long lastPotBeepTime = 0L;
   private static long lastEffectBeepTime = 0L;

   public static void render(DrawContext context) {
      if (VoidCyanClient.isPotWarningEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            long now = System.currentTimeMillis();
            if (VoidCyanClient.potWarnLowPots) {
               int potCount = countHealthPots(client);
               if (potCount <= VoidCyanClient.potWarnPotThreshold) {
                  maybePlaySound(now, true);
                  if (VoidCyanClient.potWarnShowText) {
                     String line = potCount == 0 ? "NO HEALTH POTS!" : "LOW POTS: " + potCount;
                     context.drawTextWithShadow(client.textRenderer, line, 0, 0, warningColor());
                  }
               }
            }

            if (VoidCyanClient.potWarnLowEffects) {
               Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
               boolean anyLow = false;
               int lineY = VoidCyanClient.potWarnLowPots ? 12 : 0;
               Iterator var7 = effects.iterator();

               while (true) {
                  StatusEffectInstance effect;
                  while (true) {
                     if (!var7.hasNext()) {
                        if (anyLow) {
                           maybePlaySound(now, false);
                        }

                        return;
                     }

                     effect = (StatusEffectInstance)var7.next();
                     boolean beneficial = ((StatusEffect)effect.getEffectType().value()).getCategory().ordinal() == 0;
                     if (beneficial && effect.getDuration() != Integer.MAX_VALUE) {
                        String effectId = effect.getEffectType().getIdAsString();
                        if (!effectId.contains("night_vision") || effect.getAmplifier() != 255) {
                           if (VoidCyanClient.potWarnIgnoredEffects.isEmpty()) {
                              break;
                           }

                           boolean ignored = false;

                           for (String ignoredId : VoidCyanClient.potWarnIgnoredEffects.split("[,\\s;]+")) {
                              if (effectId.contains(ignoredId.trim()) || effectId.equals(ignoredId.trim())) {
                                 ignored = true;
                                 break;
                              }
                           }

                           if (!ignored) {
                              break;
                           }
                        }
                     }
                  }

                  if (effect.getDuration() <= VoidCyanClient.potWarnEffectThreshold) {
                     anyLow = true;
                     if (VoidCyanClient.potWarnShowText) {
                        String name = Text.translatable(((StatusEffect)effect.getEffectType().value()).getTranslationKey()).getString();
                        String timeStr = formatTicks(effect.getDuration());
                        context.drawTextWithShadow(client.textRenderer, name + " " + timeStr, 0, lineY, warningColor());
                        lineY += 10;
                     }
                  }
               }
            }
         }
      }
   }

   private static int countHealthPots(MinecraftClient client) {
      int count = 0;

      for (int i = 0; i <= 8; i++) {
         if (isInstantHealthPotion(client.player.getInventory().getStack(i))) {
            count++;
         }
      }

      if (isInstantHealthPotion(client.player.getOffHandStack())) {
         count++;
      }

      return count;
   }

   private static boolean isInstantHealthPotion(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (!stack.isOf(Items.SPLASH_POTION) && !stack.isOf(Items.LINGERING_POTION) && !stack.isOf(Items.POTION)) {
         return false;
      } else {
         PotionContentsComponent contents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
         if (contents == null) {
            return false;
         } else {
            for (StatusEffectInstance e : contents.getEffects()) {
               if (e.getEffectType().equals(StatusEffects.INSTANT_HEALTH)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private static void maybePlaySound(long now, boolean isPot) {
      if (VoidCyanClient.potWarnSound) {
         if (isPot) {
            long[] var10000 = new long[]{lastPotBeepTime};
         } else {
            long[] var5 = new long[]{lastEffectBeepTime};
         }

         if (now - (isPot ? lastPotBeepTime : lastEffectBeepTime) > 2000L) {
            com.voidcyan.client.util.AlarmSoundManager.playAlarm(VoidCyanClient.potWarnSoundFile);

            if (isPot) {
               lastPotBeepTime = now;
            } else {
               lastEffectBeepTime = now;
            }
         }
      }
   }

   private static int warningColor() {
      return VoidCyanClient.potWarnUseThemeColor ? 0xFF000000 | VoidCyanClient.getPrimaryColor() : -43776;
   }

   private static String formatTicks(int ticks) {
      int secs = ticks / 20;
      int mins = secs / 60;
      secs %= 60;
      return mins > 0 ? mins + ":" + String.format("%02d", secs) : secs + "s";
   }

   public static int getWidth(MinecraftClient client) {
      return client != null && client.textRenderer != null ? client.textRenderer.getWidth("LOW POTS: 0") + 2 : 100;
   }

   public static int getHeight() {
      int lines = 0;
      if (VoidCyanClient.potWarnLowPots) {
         lines++;
      }

      if (VoidCyanClient.potWarnLowEffects) {
         lines += 2;
      }

      return Math.max(1, lines) * 10 + 2;
   }
}
