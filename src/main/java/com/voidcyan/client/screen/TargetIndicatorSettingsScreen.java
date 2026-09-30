package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TargetIndicatorSettingsScreen extends Screen {
   private long openTime;
   private final Screen parent;
   private int currentTab = 0;
   private int activeSliderId = -1;
   private static final String[] STYLE_OPTIONS = new String[]{"LEGACY", "SOUL", "SPIRAL", "TOPKA", "PLUS"};
   private static final String[] LEGACY_TEXTURE_OPTIONS = new String[]{"LEGACY", "MARKER", "BO", "SIMPLE", "SCIFI", "JEKA", "AMONGUS", "SKULL", "VEGAS"};
   private static final String[] SOUL_TEXTURE_OPTIONS = new String[]{"FIREFLY", "ALT"};
   private static final String[] SOUL_STYLE_OPTIONS = new String[]{"SMOKE", "PLASMA"};

   /** Friendly display names for the stored style / texture ids (config values stay unchanged). */
   private static String label(String id) {
      return switch (id.toUpperCase()) {
         case "LEGACY" -> "Classic";
         case "SOUL" -> "Orbit";
         case "SPIRAL" -> "Spiral";
         case "TOPKA" -> "Scanner";
         case "PLUS" -> "Plus";
         case "MARKER" -> "Marker";
         case "BO" -> "Reticle";
         case "SIMPLE" -> "Simple";
         case "SCIFI" -> "Tech";
         case "JEKA" -> "Badge";
         case "AMONGUS" -> "Bean";
         case "SKULL" -> "Skull";
         case "VEGAS" -> "Neon";
         case "FIREFLY" -> "Glow";
         case "ALT" -> "Soft Glow";
         case "SMOKE" -> "Smoke";
         case "PLASMA" -> "Plasma";
         default -> id;
      };
   }

   public TargetIndicatorSettingsScreen(Screen parent) {
      super(Text.literal("Target Marker Settings"));
      this.parent = parent;
   }

   protected void init() {
      this.openTime = System.currentTimeMillis();
      super.init();
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
   }

   private void drawTabButton(DrawContext context, int x, int y, int width, String label, boolean active) {
      int height = 18;
      int bgColor = active ? VoidCyanClient.getPrimaryColor() & 16777215 | -1879048192 : 1073741824;
      int borderColor = active ? VoidCyanClient.getPrimaryColor() : -11184811;
      context.fill(x, y, x + width, y + height, bgColor);
      context.fill(x, y, x + width, y + 1, borderColor);
      context.fill(x, y + height - 1, x + width, y + height, borderColor);
      context.fill(x, y + 1, x + 1, y + height - 1, borderColor);
      context.fill(x + width - 1, y + 1, x + width, y + height - 1, borderColor);
      int textWidth = this.textRenderer.getWidth(label);
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x + (width - textWidth) / 2, y + 5, -1);
   }

   private void drawToggle(DrawContext context, int x, int y, String label, boolean value) {
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y + 4, -1);
      int switchX = x + 160;
      int switchWidth = 30;
      int switchHeight = 12;
      int bgColor = value ? VoidCyanClient.getPrimaryColor() : -8355712;
      context.fill(switchX, y, switchX + switchWidth, y + switchHeight, bgColor);
      int knobX = value ? switchX + switchWidth - 10 : switchX + 2;
      context.fill(knobX, y + 2, knobX + 8, y + switchHeight - 2, -16777216);
   }

   private void drawSlider(DrawContext context, int x, int y, String label, float val, float min, float max, String suffix) {
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y, -1);
      String valText = String.format("%.2f", val) + suffix;
      context.drawTextWithShadow(this.textRenderer, Text.literal(valText), x + 160, y, VoidCyanClient.getPrimaryColor());
      int sliderY = y + 12;
      int sliderWidth = 180;
      int sliderHeight = 8;
      context.fill(x, sliderY, x + sliderWidth, sliderY + sliderHeight, -13421773);
      context.fill(x, sliderY, x + sliderWidth, sliderY + 1, -11184811);
      context.fill(x, sliderY + sliderHeight - 1, x + sliderWidth, sliderY + sliderHeight, -11184811);
      float pct = (val - min) / (max - min);
      pct = Math.max(0.0F, Math.min(1.0F, pct));
      int handleX = x + (int)(pct * sliderWidth);
      context.fill(handleX - 2, sliderY - 2, handleX + 2, sliderY + sliderHeight + 2, VoidCyanClient.getPrimaryColor());
   }

   private void drawSelector(DrawContext context, int x, int y, String label, String value) {
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y + 4, -1);
      int boxX = x + 160;
      int boxWidth = 100;
      int boxHeight = 16;
      context.fill(boxX, y, boxX + boxWidth, y + boxHeight, 1073741824);
      context.fill(boxX, y, boxX + boxWidth, y + 1, VoidCyanClient.getPrimaryColor());
      context.fill(boxX, y + boxHeight - 1, boxX + boxWidth, y + boxHeight, VoidCyanClient.getPrimaryColor());
      context.fill(boxX, y + 1, boxX + 1, y + boxHeight - 1, VoidCyanClient.getPrimaryColor());
      context.fill(boxX + boxWidth - 1, y + 1, boxX + boxWidth, y + boxHeight - 1, VoidCyanClient.getPrimaryColor());
      value = label(value);
      int textWidth = this.textRenderer.getWidth(value);
      context.drawTextWithShadow(this.textRenderer, Text.literal(value), boxX + (boxWidth - textWidth) / 2, y + 4, VoidCyanClient.getPrimaryColor());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      float progress = (float)(System.currentTimeMillis() - this.openTime) / 250.0F;
      if (progress > 1.0F) {
         progress = 1.0F;
      }

      float scale = 1.0F - (float)Math.pow(1.0F - progress, 3.0);
      context.getMatrices().pushMatrix();
      context.getMatrices().translate(this.width / 2.0F, this.height / 2.0F);
      context.getMatrices().scale(scale, scale);
      context.getMatrices().translate(-this.width / 2.0F, -this.height / 2.0F);
      super.render(context, mouseX, mouseY, deltaTicks);
      int panelWidth = Math.max(Math.min(420, this.width - 10), (int)(this.width * 0.9F));
      int panelHeight = Math.max(Math.min(310, this.height - 10), (int)(this.height * 0.9F));
      int x = (this.width - panelWidth) / 2;
      int y = (this.height - panelHeight) / 2;
      context.fill(x, y, x + panelWidth, y + panelHeight, Integer.MIN_VALUE);
      context.fill(x, y, x + panelWidth, y + 1, VoidCyanClient.getPrimaryColor());
      context.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, VoidCyanClient.getPrimaryColor());
      context.fill(x, y + 1, x + 1, y + panelHeight - 1, VoidCyanClient.getPrimaryColor());
      context.fill(x + panelWidth - 1, y + 1, x + panelWidth, y + panelHeight - 1, VoidCyanClient.getPrimaryColor());
      context.drawTextWithShadow(this.textRenderer, Text.literal("Target Marker Settings"), x + 15, y + 12, VoidCyanClient.getPrimaryColor());
      int tabX = x + 15;
      int tabY = y + 30;
      int tabWidth = 72;
      int tabGap = 8;
      this.drawTabButton(context, tabX, tabY, tabWidth, "General", this.currentTab == 0);
      this.drawTabButton(context, tabX + tabWidth + tabGap, tabY, tabWidth, "Crosshair", this.currentTab == 1);
      this.drawTabButton(context, tabX + 2 * (tabWidth + tabGap), tabY, tabWidth, "Classic", this.currentTab == 2);
      this.drawTabButton(context, tabX + 3 * (tabWidth + tabGap), tabY, tabWidth, "Orbit", this.currentTab == 3);
      this.drawTabButton(context, tabX + 4 * (tabWidth + tabGap), tabY, tabWidth, "Scanner", this.currentTab == 4);
      int contentY = y + 65;
      if (this.currentTab == 0) {
         this.drawToggle(context, x + 20, contentY, "Marker Enabled", VoidCyanClient.attackIndicatorEnabled);
         contentY += 25;
         this.drawSelector(context, x + 20, contentY, "Style", VoidCyanClient.attackIndicatorStyle);
         contentY += 25;
         this.drawToggle(context, x + 20, contentY, "Only Players", VoidCyanClient.attackIndicatorOnlyPlayers);
         contentY += 25;
         this.drawToggle(context, x + 20, contentY, "All Entities (Carts/Crystals)", VoidCyanClient.attackIndicatorAllowNonLiving);
         contentY += 25;
         this.drawToggle(context, x + 20, contentY, "Always Active", VoidCyanClient.attackIndicatorAlwaysActive);
         contentY += 30;
         this.drawSlider(context, x + 20, contentY, "Live Time", VoidCyanClient.attackIndicatorLiveTime, 0.1F, 10.0F, "s");
      } else if (this.currentTab == 1) {
         this.drawToggle(context, x + 20, contentY, "Spinning Enabled", VoidCyanClient.targetIndicatorSpinning);
         contentY += 25;
         this.drawToggle(context, x + 20, contentY, "Hit Reach Only", VoidCyanClient.targetIndicatorHitReachOnly);
         contentY += 30;
         this.drawSlider(context, x + 20, contentY, "Max Reach", VoidCyanClient.targetIndicatorMaxReach, 3.0F, 64.0F, " blocks");
         contentY += 35;
         this.drawSlider(context, x + 20, contentY, "Spin Speed", VoidCyanClient.targetIndicatorSpinSpeed, 0.0F, 5.0F, "x");
      } else if (this.currentTab == 2) {
         this.drawSelector(context, x + 20, contentY, "Classic Texture", VoidCyanClient.attackIndicatorLegacyTexture);
         contentY += 35;
         this.drawSlider(context, x + 20, contentY, "Classic Scale", VoidCyanClient.attackIndicatorLegacyScale, 10.0F, 200.0F, "");
         contentY += 35;
         this.drawSlider(context, x + 20, contentY, "Classic Opacity", VoidCyanClient.attackIndicatorLegacyAlpha, 0.0F, 100.0F, "%");
         contentY += 35;
         this.drawSlider(context, x + 20, contentY, "Roll Speed", VoidCyanClient.attackIndicatorLegacyRollSpeed, 0.0F, 500.0F, "");
      } else if (this.currentTab == 3) {
         this.drawSelector(context, x + 20, contentY, "Orbit Texture", VoidCyanClient.attackIndicatorSoulTexture);
         contentY += 25;
         this.drawSelector(context, x + 20, contentY, "Orbit Blend", VoidCyanClient.attackIndicatorSoulStyle);
         contentY += 35;
         int leftColX = x + 20;
         int rightColX = x + 215;
         this.drawSlider(context, leftColX, contentY, "Trail Length", VoidCyanClient.attackIndicatorSoulLength, 5.0F, 100.0F, "");
         this.drawSlider(context, rightColX, contentY, "Speed Factor", VoidCyanClient.attackIndicatorSoulFactor, 0.1F, 10.0F, "");
         contentY += 35;
         this.drawSlider(context, leftColX, contentY, "Shaking", VoidCyanClient.attackIndicatorSoulShaking, 0.1F, 20.0F, "");
         this.drawSlider(context, rightColX, contentY, "Amplitude", VoidCyanClient.attackIndicatorSoulAmplitude, 0.1F, 10.0F, "");
         contentY += 35;
         this.drawSlider(context, leftColX, contentY, "Orbit Radius", VoidCyanClient.attackIndicatorSoulRadius, 10.0F, 200.0F, "");
         this.drawSlider(context, rightColX, contentY, "Start Size", VoidCyanClient.attackIndicatorSoulStartSize, 10.0F, 200.0F, "");
         contentY += 35;
         this.drawSlider(context, leftColX, contentY, "End Size", VoidCyanClient.attackIndicatorSoulEndSize, 10.0F, 200.0F, "");
         this.drawSlider(context, rightColX, contentY, "Overall Scale", VoidCyanClient.attackIndicatorSoulScale, 10.0F, 200.0F, "");
         contentY += 35;
         this.drawSlider(context, leftColX, contentY, "Subdivisions", VoidCyanClient.attackIndicatorSoulSubdivision, 1.0F, 10.0F, "");
      } else if (this.currentTab == 4) {
         this.drawSlider(context, x + 20, contentY, "Scan Radius", VoidCyanClient.attackIndicatorTopkaRadius, 10.0F, 200.0F, "");
         contentY += 35;
         this.drawSlider(context, x + 20, contentY, "Scan Speed", VoidCyanClient.attackIndicatorTopkaSpeed, 0.1F, 10.0F, "");
      }

      int btnWidth = 100;
      int btnHeight = 20;
      int btnX = x + (panelWidth - btnWidth) / 2;
      int btnY = y + panelHeight - 30;
      int btnColor = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight ? VoidCyanClient.getPrimaryColor() : -11184811;
      context.fill(btnX, btnY, btnX + btnWidth, btnY + btnHeight, btnColor);
      int backTextWidth = this.textRenderer.getWidth("Back");
      context.drawTextWithShadow(this.textRenderer, Text.literal("Back"), btnX + (btnWidth - backTextWidth) / 2, btnY + 6, -1);
      context.getMatrices().popMatrix();
   }

   private int getIndex(String[] array, String value) {
      for (int i = 0; i < array.length; i++) {
         if (array[i].equalsIgnoreCase(value)) {
            return i;
         }
      }

      return 0;
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      if (click.button() != 0) {
         return super.mouseClicked(click, doubled);
      } else {
         int panelWidth = Math.max(Math.min(420, this.width - 10), (int)(this.width * 0.9F));
         int panelHeight = Math.max(Math.min(310, this.height - 10), (int)(this.height * 0.9F));
         int x = (this.width - panelWidth) / 2;
         int y = (this.height - panelHeight) / 2;
         int tabY = y + 30;
         int tabHeight = 18;
         if (mouseY >= tabY && mouseY <= tabY + tabHeight) {
            int tabX = x + 15;
            int tabW = 72;
            int gap = 8;
            if (mouseX >= tabX && mouseX <= tabX + tabW) {
               this.currentTab = 0;
               return true;
            }

            if (mouseX >= tabX + tabW + gap && mouseX <= tabX + tabW + gap + tabW) {
               this.currentTab = 1;
               return true;
            }

            if (mouseX >= tabX + (tabW + gap) * 2 && mouseX <= tabX + (tabW + gap) * 2 + tabW) {
               this.currentTab = 2;
               return true;
            }

            if (mouseX >= tabX + (tabW + gap) * 3 && mouseX <= tabX + (tabW + gap) * 3 + tabW) {
               this.currentTab = 3;
               return true;
            }

            if (mouseX >= tabX + (tabW + gap) * 4 && mouseX <= tabX + (tabW + gap) * 4 + tabW) {
               this.currentTab = 4;
               return true;
            }
         }

         int contentY = y + 65;
         int toggleX = x + 20 + 160;
         int selectorX = x + 20 + 160;
         int sliderX = x + 20;
         if (this.currentTab == 0) {
            if (mouseX >= toggleX && mouseX <= toggleX + 40 && mouseY >= contentY && mouseY <= contentY + 16) {
               VoidCyanClient.attackIndicatorEnabled = !VoidCyanClient.attackIndicatorEnabled;
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 25;
            if (mouseX >= selectorX && mouseX <= selectorX + 100 && mouseY >= contentY && mouseY <= contentY + 16) {
               int nextIdx = (this.getIndex(STYLE_OPTIONS, VoidCyanClient.attackIndicatorStyle) + 1) % STYLE_OPTIONS.length;
               VoidCyanClient.attackIndicatorStyle = STYLE_OPTIONS[nextIdx];
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 25;
            if (mouseX >= toggleX && mouseX <= toggleX + 40 && mouseY >= contentY && mouseY <= contentY + 16) {
               VoidCyanClient.attackIndicatorOnlyPlayers = !VoidCyanClient.attackIndicatorOnlyPlayers;
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 25;
            if (mouseX >= toggleX && mouseX <= toggleX + 40 && mouseY >= contentY && mouseY <= contentY + 16) {
               VoidCyanClient.attackIndicatorAllowNonLiving = !VoidCyanClient.attackIndicatorAllowNonLiving;
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 25;
            if (mouseX >= toggleX && mouseX <= toggleX + 40 && mouseY >= contentY && mouseY <= contentY + 16) {
               VoidCyanClient.attackIndicatorAlwaysActive = !VoidCyanClient.attackIndicatorAlwaysActive;
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 30;
            int sliderY = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderY - 4 && mouseY <= sliderY + 12) {
               this.activeSliderId = 0;
               this.updateSliderVal(mouseX, sliderX, 180, 0.1F, 10.0F);
               return true;
            }
         } else if (this.currentTab == 1) {
            if (mouseX >= toggleX && mouseX <= toggleX + 40 && mouseY >= contentY && mouseY <= contentY + 16) {
               VoidCyanClient.targetIndicatorSpinning = !VoidCyanClient.targetIndicatorSpinning;
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 25;
            if (mouseX >= toggleX && mouseX <= toggleX + 40 && mouseY >= contentY && mouseY <= contentY + 16) {
               VoidCyanClient.targetIndicatorHitReachOnly = !VoidCyanClient.targetIndicatorHitReachOnly;
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 30;
            int sliderY = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderY - 4 && mouseY <= sliderY + 12) {
               this.activeSliderId = 1;
               this.updateSliderVal(mouseX, sliderX, 180, 3.0F, 64.0F);
               return true;
            }

            contentY += 35;
            sliderY = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderY - 4 && mouseY <= sliderY + 12) {
               this.activeSliderId = 2;
               this.updateSliderVal(mouseX, sliderX, 180, 0.0F, 5.0F);
               return true;
            }
         } else if (this.currentTab == 2) {
            if (mouseX >= selectorX && mouseX <= selectorX + 100 && mouseY >= contentY && mouseY <= contentY + 16) {
               int nextIdx = (this.getIndex(LEGACY_TEXTURE_OPTIONS, VoidCyanClient.attackIndicatorLegacyTexture) + 1) % LEGACY_TEXTURE_OPTIONS.length;
               VoidCyanClient.attackIndicatorLegacyTexture = LEGACY_TEXTURE_OPTIONS[nextIdx];
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 35;
            int sliderYx = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderYx - 4 && mouseY <= sliderYx + 12) {
               this.activeSliderId = 4;
               this.updateSliderVal(mouseX, sliderX, 180, 10.0F, 200.0F);
               return true;
            }

            contentY += 35;
            sliderYx = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderYx - 4 && mouseY <= sliderYx + 12) {
               this.activeSliderId = 5;
               this.updateSliderVal(mouseX, sliderX, 180, 0.0F, 100.0F);
               return true;
            }

            contentY += 35;
            sliderYx = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderYx - 4 && mouseY <= sliderYx + 12) {
               this.activeSliderId = 6;
               this.updateSliderVal(mouseX, sliderX, 180, 0.0F, 500.0F);
               return true;
            }
         } else if (this.currentTab == 3) {
            if (mouseX >= selectorX && mouseX <= selectorX + 100 && mouseY >= contentY && mouseY <= contentY + 16) {
               int nextIdx = (this.getIndex(SOUL_TEXTURE_OPTIONS, VoidCyanClient.attackIndicatorSoulTexture) + 1) % SOUL_TEXTURE_OPTIONS.length;
               VoidCyanClient.attackIndicatorSoulTexture = SOUL_TEXTURE_OPTIONS[nextIdx];
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 25;
            if (mouseX >= selectorX && mouseX <= selectorX + 100 && mouseY >= contentY && mouseY <= contentY + 16) {
               int nextIdx = (this.getIndex(SOUL_STYLE_OPTIONS, VoidCyanClient.attackIndicatorSoulStyle) + 1) % SOUL_STYLE_OPTIONS.length;
               VoidCyanClient.attackIndicatorSoulStyle = SOUL_STYLE_OPTIONS[nextIdx];
               VoidCyanClient.saveConfig();
               return true;
            }

            contentY += 35;
            int leftSliderX = x + 20;
            int rightSliderX = x + 215;
            int sliderYxx = contentY + 12;
            if (mouseX >= leftSliderX && mouseX <= leftSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 7;
               this.updateSliderVal(mouseX, leftSliderX, 180, 5.0F, 100.0F);
               return true;
            }

            if (mouseX >= rightSliderX && mouseX <= rightSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 8;
               this.updateSliderVal(mouseX, rightSliderX, 180, 0.1F, 10.0F);
               return true;
            }

            contentY += 35;
            sliderYxx = contentY + 12;
            if (mouseX >= leftSliderX && mouseX <= leftSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 9;
               this.updateSliderVal(mouseX, leftSliderX, 180, 0.1F, 20.0F);
               return true;
            }

            if (mouseX >= rightSliderX && mouseX <= rightSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 10;
               this.updateSliderVal(mouseX, rightSliderX, 180, 0.1F, 10.0F);
               return true;
            }

            contentY += 35;
            sliderYxx = contentY + 12;
            if (mouseX >= leftSliderX && mouseX <= leftSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 11;
               this.updateSliderVal(mouseX, leftSliderX, 180, 10.0F, 200.0F);
               return true;
            }

            if (mouseX >= rightSliderX && mouseX <= rightSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 12;
               this.updateSliderVal(mouseX, rightSliderX, 180, 10.0F, 200.0F);
               return true;
            }

            contentY += 35;
            sliderYxx = contentY + 12;
            if (mouseX >= leftSliderX && mouseX <= leftSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 13;
               this.updateSliderVal(mouseX, leftSliderX, 180, 10.0F, 200.0F);
               return true;
            }

            if (mouseX >= rightSliderX && mouseX <= rightSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 14;
               this.updateSliderVal(mouseX, rightSliderX, 180, 10.0F, 200.0F);
               return true;
            }

            contentY += 35;
            sliderYxx = contentY + 12;
            if (mouseX >= leftSliderX && mouseX <= leftSliderX + 180 && mouseY >= sliderYxx - 4 && mouseY <= sliderYxx + 12) {
               this.activeSliderId = 15;
               this.updateSliderVal(mouseX, leftSliderX, 180, 1.0F, 10.0F);
               return true;
            }
         } else if (this.currentTab == 4) {
            int sliderYxxx = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderYxxx - 4 && mouseY <= sliderYxxx + 12) {
               this.activeSliderId = 16;
               this.updateSliderVal(mouseX, sliderX, 180, 10.0F, 200.0F);
               return true;
            }

            contentY += 35;
            sliderYxxx = contentY + 12;
            if (mouseX >= sliderX && mouseX <= sliderX + 180 && mouseY >= sliderYxxx - 4 && mouseY <= sliderYxxx + 12) {
               this.activeSliderId = 17;
               this.updateSliderVal(mouseX, sliderX, 180, 0.1F, 10.0F);
               return true;
            }
         }

         int btnWidth = 100;
         int btnHeight = 20;
         int btnX = x + (panelWidth - btnWidth) / 2;
         int btnY = y + panelHeight - 30;
         if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
            this.client.setScreen(this.parent);
            return true;
         } else {
            return super.mouseClicked(click, doubled);
         }
      }
   }

   private void updateSliderVal(double mouseX, int sliderX, int sliderWidth, float min, float max) {
      float pct = (float)(mouseX - sliderX) / sliderWidth;
      pct = Math.max(0.0F, Math.min(1.0F, pct));
      float value = min + pct * (max - min);
      switch (this.activeSliderId) {
         case 0:
            VoidCyanClient.attackIndicatorLiveTime = value;
            break;
         case 1:
            VoidCyanClient.targetIndicatorMaxReach = value;
            break;
         case 2:
            VoidCyanClient.targetIndicatorSpinSpeed = value;
         case 3:
         default:
            break;
         case 4:
            VoidCyanClient.attackIndicatorLegacyScale = (int)value;
            break;
         case 5:
            VoidCyanClient.attackIndicatorLegacyAlpha = (int)value;
            break;
         case 6:
            VoidCyanClient.attackIndicatorLegacyRollSpeed = (int)value;
            break;
         case 7:
            VoidCyanClient.attackIndicatorSoulLength = Math.round(value);
            break;
         case 8:
            VoidCyanClient.attackIndicatorSoulFactor = value;
            break;
         case 9:
            VoidCyanClient.attackIndicatorSoulShaking = value;
            break;
         case 10:
            VoidCyanClient.attackIndicatorSoulAmplitude = value;
            break;
         case 11:
            VoidCyanClient.attackIndicatorSoulRadius = value;
            break;
         case 12:
            VoidCyanClient.attackIndicatorSoulStartSize = value;
            break;
         case 13:
            VoidCyanClient.attackIndicatorSoulEndSize = value;
            break;
         case 14:
            VoidCyanClient.attackIndicatorSoulScale = value;
            break;
         case 15:
            VoidCyanClient.attackIndicatorSoulSubdivision = Math.round(value);
            break;
         case 16:
            VoidCyanClient.attackIndicatorTopkaRadius = value;
            break;
         case 17:
            VoidCyanClient.attackIndicatorTopkaSpeed = value;
      }

      VoidCyanClient.saveConfig();
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      if (this.activeSliderId != -1) {
         int panelWidth = Math.max(Math.min(420, this.width - 10), (int)(this.width * 0.9F));
         int x = (this.width - panelWidth) / 2;
         int realWidth = this.client.getWindow().getWidth();
         int realHeight = this.client.getWindow().getHeight();
         double guiScale = this.client.getWindow().getScaleFactor();
         float scale = (float)(Math.min(realWidth / 420.0, realHeight / 310.0) / guiScale);
         double mouseX = (click.x() - this.width / 2.0) / scale + this.width / 2.0;
         if (this.currentTab == 0) {
            if (this.activeSliderId == 0) {
               this.updateSliderVal(mouseX, x + 20, 180, 0.1F, 10.0F);
            }
         } else if (this.currentTab == 1) {
            if (this.activeSliderId == 1) {
               this.updateSliderVal(mouseX, x + 20, 180, 3.0F, 64.0F);
            } else if (this.activeSliderId == 2) {
               this.updateSliderVal(mouseX, x + 20, 180, 0.0F, 5.0F);
            }
         } else if (this.currentTab == 2) {
            if (this.activeSliderId == 4) {
               this.updateSliderVal(mouseX, x + 20, 180, 10.0F, 200.0F);
            } else if (this.activeSliderId == 5) {
               this.updateSliderVal(mouseX, x + 20, 180, 0.0F, 100.0F);
            } else if (this.activeSliderId == 6) {
               this.updateSliderVal(mouseX, x + 20, 180, 0.0F, 500.0F);
            }
         } else if (this.currentTab == 3) {
            int leftSliderX = x + 20;
            int rightSliderX = x + 215;
            if (this.activeSliderId == 7) {
               this.updateSliderVal(mouseX, leftSliderX, 180, 5.0F, 100.0F);
            } else if (this.activeSliderId == 8) {
               this.updateSliderVal(mouseX, rightSliderX, 180, 0.1F, 10.0F);
            } else if (this.activeSliderId == 9) {
               this.updateSliderVal(mouseX, leftSliderX, 180, 0.1F, 20.0F);
            } else if (this.activeSliderId == 10) {
               this.updateSliderVal(mouseX, rightSliderX, 180, 0.1F, 10.0F);
            } else if (this.activeSliderId == 11) {
               this.updateSliderVal(mouseX, leftSliderX, 180, 10.0F, 200.0F);
            } else if (this.activeSliderId == 12) {
               this.updateSliderVal(mouseX, rightSliderX, 180, 10.0F, 200.0F);
            } else if (this.activeSliderId == 13) {
               this.updateSliderVal(mouseX, leftSliderX, 180, 10.0F, 200.0F);
            } else if (this.activeSliderId == 14) {
               this.updateSliderVal(mouseX, rightSliderX, 180, 10.0F, 200.0F);
            } else if (this.activeSliderId == 15) {
               this.updateSliderVal(mouseX, leftSliderX, 180, 1.0F, 10.0F);
            }
         } else if (this.currentTab == 4) {
            if (this.activeSliderId == 16) {
               this.updateSliderVal(mouseX, x + 20, 180, 10.0F, 200.0F);
            } else if (this.activeSliderId == 17) {
               this.updateSliderVal(mouseX, x + 20, 180, 0.1F, 10.0F);
            }
         }

         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   public boolean mouseReleased(Click click) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      if (click.button() == 0) {
         this.activeSliderId = -1;
      }

      return super.mouseReleased(click);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }
}
