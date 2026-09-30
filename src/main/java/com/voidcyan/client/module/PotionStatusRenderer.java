package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.EditHudScreen;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PotionStatusRenderer {

   public static void render(DrawContext context) {
      if (VoidCyanClient.isPotionStatusEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            boolean inEditor = client.currentScreen instanceof EditHudScreen;
            Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
            if (!effects.isEmpty() || inEditor) {
               List<StatusEffectInstance> sorted;
               if (effects.isEmpty() && inEditor) {
                  sorted = new ArrayList<>();
                  sorted.add(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1));
               } else {
                  sorted = new ArrayList<>(effects);
                  sorted.sort((a, b) -> {
                     int catA = category(a);
                     int catB = category(b);
                     return catA != catB ? Integer.compare(catA, catB) : Integer.compare(b.getDuration(), a.getDuration());
                  });
               }

               boolean horizontal = VoidCyanClient.potionStatusOrientation == 0;
               if (VoidCyanClient.potionStatusStyle == 1) {
                  renderVanilla(context, client, sorted, horizontal);
                  return;
               }

               int count = sorted.size();
               int totalW;
               int totalH;
               if (horizontal) {
                  totalW = count * 26 + 3;
                  totalH = 32 + (VoidCyanClient.potionStatusShowDuration ? 10 : 0);
               } else {
                  totalW = verticalWidth(sorted);
                  totalH = count * 26 + 3;
               }

               context.fill(0, 0, totalW, totalH, Integer.MIN_VALUE);
               int theme = VoidCyanClient.getPrimaryColor();
               context.fill(0, 0, totalW, 1, theme);
               context.fill(0, totalH - 1, totalW, totalH, theme);
               context.fill(0, 0, 1, totalH, theme);
               context.fill(totalW - 1, 0, totalW, totalH, theme);

               for (int i = 0; i < sorted.size(); i++) {
                  StatusEffectInstance inst = sorted.get(i);
                  RegistryEntry<StatusEffect> effect = inst.getEffectType();
                  int iconX;
                  int iconY;
                  if (horizontal) {
                     iconX = 3 + i * 26;
                     iconY = 3;
                  } else {
                     iconX = 3;
                     iconY = 3 + i * 26;
                  }

                  if (VoidCyanClient.potionStatusIconType == 1) {
                     Identifier texture = effect.getKey().map(key -> key.getValue().withPrefixedPath("mob_effect/")).orElse(MissingSprite.getMissingSpriteId());
                     context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, iconX + 2, iconY + 2, 16, 16);
                  } else {
                     String fxName = getEffectName(effect);
                     String iconLetter = fxName.isEmpty() ? "?" : fxName.substring(0, 1);
                     context.drawTextWithShadow(client.textRenderer, iconLetter, iconX + 10 - client.textRenderer.getWidth(iconLetter) / 2, iconY + 10 - 4, -1);
                  }

                  int borderCol = effectColor(inst);
                  context.fill(iconX, iconY, iconX + 20, iconY + 1, borderCol);
                  context.fill(iconX, iconY + 20 - 1, iconX + 20, iconY + 20, borderCol);
                  context.fill(iconX, iconY + 1, iconX + 1, iconY + 20 - 1, borderCol);
                  context.fill(iconX + 20 - 1, iconY + 1, iconX + 20, iconY + 20 - 1, borderCol);
                  String durationStr = formatDuration(inst.getDuration());
                  String ampStr = amplifierStr(inst);

                  if (horizontal) {
                     if (VoidCyanClient.potionStatusShowDuration) {
                        int textX = iconX + 10 - client.textRenderer.getWidth(durationStr) / 2;
                        int textY = iconY + 20 + 2;
                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(iconX + 10.0F, iconY + 20 + 4.0F);
                        context.getMatrices().scale(0.75F, 0.75F);
                        context.drawTextWithShadow(client.textRenderer, durationStr, -client.textRenderer.getWidth(durationStr) / 2, 0, -1);
                        context.getMatrices().popMatrix();
                     }
                  } else if (VoidCyanClient.potionStatusShowAmplifier || VoidCyanClient.potionStatusShowDuration) {
                     int textX = iconX + 20 + 4;
                     int textY = iconY + 2;
                     String effectName = getEffectName(effect);
                     if (VoidCyanClient.potionStatusShowAmplifier && !ampStr.isEmpty()) {
                        effectName = effectName + " " + ampStr;
                     }

                     context.drawTextWithShadow(client.textRenderer, effectName, textX, textY, -1);
                     if (VoidCyanClient.potionStatusShowDuration) {
                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(textX, textY + 9.0F);
                        context.getMatrices().scale(0.85F, 0.85F);
                        context.drawTextWithShadow(client.textRenderer, durationStr, 0, 0, isLowDuration(inst) ? -43691 : -5592321);
                        context.getMatrices().popMatrix();
                     }
                  }

                  if (horizontal && VoidCyanClient.potionStatusShowAmplifier && inst.getAmplifier() > 0) {
                     String amp = romanNumeral(inst.getAmplifier() + 1);
                     context.getMatrices().pushMatrix();
                     context.getMatrices().translate(iconX + 20 - 1.0F, iconY + 1.0F);
                     context.getMatrices().scale(0.6F, 0.6F);
                     context.drawTextWithShadow(client.textRenderer, amp, -client.textRenderer.getWidth(amp), 0, -256);
                     context.getMatrices().popMatrix();
                  }
               }
            }
         }
      }
   }

   /** Vanilla-style boxes (like the in-game effect HUD) with name / duration text. */
   private static void renderVanilla(DrawContext context, MinecraftClient client, List<StatusEffectInstance> sorted, boolean horizontal) {
      Identifier bg = Identifier.of("minecraft", "hud/effect_background");
      Identifier bgAmbient = Identifier.of("minecraft", "hud/effect_background_ambient");
      for (int i = 0; i < sorted.size(); i++) {
         StatusEffectInstance inst = sorted.get(i);
         RegistryEntry<StatusEffect> effect = inst.getEffectType();
         int bx = horizontal ? i * 26 : 0;
         int by = horizontal ? 0 : i * 26;
         context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, inst.isAmbient() ? bgAmbient : bg, bx, by, 24, 24);
         Identifier texture = effect.getKey().map(key -> key.getValue().withPrefixedPath("mob_effect/")).orElse(MissingSprite.getMissingSpriteId());
         context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, bx + 3, by + 3, 18, 18);
         String durationStr = formatDuration(inst.getDuration());
         int durCol = isLowDuration(inst) ? -43691 : -1;
         if (horizontal) {
            if (VoidCyanClient.potionStatusShowDuration) {
               context.getMatrices().pushMatrix();
               context.getMatrices().translate(bx + 12.0F, by + 26.0F);
               context.getMatrices().scale(0.75F, 0.75F);
               context.drawTextWithShadow(client.textRenderer, durationStr, -client.textRenderer.getWidth(durationStr) / 2, 0, durCol);
               context.getMatrices().popMatrix();
            }

            if (VoidCyanClient.potionStatusShowAmplifier && inst.getAmplifier() > 0) {
               String amp = romanNumeral(inst.getAmplifier() + 1);
               context.getMatrices().pushMatrix();
               context.getMatrices().translate(bx + 22.0F, by + 2.0F);
               context.getMatrices().scale(0.6F, 0.6F);
               context.drawTextWithShadow(client.textRenderer, amp, -client.textRenderer.getWidth(amp), 0, -256);
               context.getMatrices().popMatrix();
            }
         } else {
            String name = getEffectName(effect);
            String amp = amplifierStr(inst);
            if (VoidCyanClient.potionStatusShowAmplifier && !amp.isEmpty()) name += " " + amp;
            context.drawTextWithShadow(client.textRenderer, name, bx + 28, by + 4, -1);
            if (VoidCyanClient.potionStatusShowDuration) {
               context.getMatrices().pushMatrix();
               context.getMatrices().translate(bx + 28.0F, by + 14.0F);
               context.getMatrices().scale(0.85F, 0.85F);
               context.drawTextWithShadow(client.textRenderer, durationStr, 0, 0, durCol == -1 ? -5592321 : durCol);
               context.getMatrices().popMatrix();
            }
         }
      }
   }

   private static int category(StatusEffectInstance inst) {
      int ordinal = ((StatusEffect)inst.getEffectType().value()).getCategory().ordinal();
      if (ordinal == 0) {
         return 0;
      } else {
         return ordinal == 2 ? 2 : 1;
      }
   }

   private static int effectColor(StatusEffectInstance inst) {
      int ordinal = ((StatusEffect)inst.getEffectType().value()).getCategory().ordinal();
      if (ordinal == 0) {
         return -11141291;
      } else {
         return ordinal == 2 ? -43691 : -171;
      }
   }

   private static String formatDuration(int ticks) {
      if (ticks == Integer.MAX_VALUE) {
         return "∞";
      } else {
         int secs = ticks / 20;
         int mins = secs / 60;
         secs %= 60;
         return mins > 0 ? mins + ":" + String.format("%02d", secs) : secs + "s";
      }
   }

   private static boolean isLowDuration(StatusEffectInstance inst) {
      return inst.getDuration() < 600 && inst.getDuration() != Integer.MAX_VALUE;
   }

   private static String amplifierStr(StatusEffectInstance inst) {
      return inst.getAmplifier() == 0 ? "" : romanNumeral(inst.getAmplifier() + 1);
   }

   private static String romanNumeral(int n) {
      if (n == 1) {
         return "I";
      } else if (n == 2) {
         return "II";
      } else if (n == 3) {
         return "III";
      } else if (n == 4) {
         return "IV";
      } else if (n == 5) {
         return "V";
      } else if (n == 6) {
         return "VI";
      } else if (n == 7) {
         return "VII";
      } else if (n == 8) {
         return "VIII";
      } else if (n == 9) {
         return "IX";
      } else {
         return n == 10 ? "X" : String.valueOf(n);
      }
   }

   private static String getEffectName(RegistryEntry<StatusEffect> entry) {
      return Text.translatable(((StatusEffect)entry.value()).getTranslationKey()).getString();
   }

   private static int verticalWidth(Collection<StatusEffectInstance> effects) {
      if (!VoidCyanClient.potionStatusShowAmplifier && !VoidCyanClient.potionStatusShowDuration) return 32;
      var tr = MinecraftClient.getInstance().textRenderer;
      int max = 0;
      for (StatusEffectInstance inst : effects) {
         String name = getEffectName(inst.getEffectType());
         if (VoidCyanClient.potionStatusShowAmplifier && !amplifierStr(inst).isEmpty()) name += " " + amplifierStr(inst);
         max = Math.max(max, Math.max(tr.getWidth(name), (int)(tr.getWidth(formatDuration(inst.getDuration())) * 0.85F)));
      }

      return 3 + 20 + 4 + Math.max(max, 40) + 6;
   }

   public static int getWidth(int count) {
      if (VoidCyanClient.potionStatusOrientation == 0) return Math.max(1, count) * 26 + 3;
      var player = MinecraftClient.getInstance().player;
      return player == null || player.getStatusEffects().isEmpty() ? 32 + (VoidCyanClient.potionStatusShowAmplifier ? 40 : 0) : verticalWidth(player.getStatusEffects());
   }

   public static int getHeight(int count) {
      return VoidCyanClient.potionStatusOrientation == 0 ? 32 + (VoidCyanClient.potionStatusShowDuration ? 10 : 0) : Math.max(1, count) * 26 + 3;
   }
}
