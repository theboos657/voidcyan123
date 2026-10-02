package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.ColorPickerModal;
import com.voidcyan.client.util.GuiScaleManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DropdownGuiScreen extends Screen {
   private static final Map<Integer, List<DropdownGuiScreen.Category>> tabWindows = new HashMap<>();
   private static int activeTab = 0;
   private static boolean initialized = false;
   private long lastFrameTime;
   private TextFieldWidget searchField;
   private static final String[] TABS = new String[]{
      "Modules", "Screenshots", "Backgrounds", "Settings", "Friends", "Config", "Statistics", "Notes", "Calculator"
   };
   private static final Identifier[] TAB_ICONS = java.util.stream.Stream.of(
      "modules", "screenshots", "backgrounds", "global", "friends", "config", "stats", "stats", "global", "modules"
   ).map(name -> Identifier.of("voidcyan", "textures/gui/icons/" + name + ".png")).toArray(Identifier[]::new);
   private float[] rowAnims = new float[0];
   private final float[] tabHoverProgress = new float[TABS.length];

   /** Module groups (by category name, without the GUI-settings group) for the Orbit profile; parent screens of their settings pages route back to the active main GUI. */
   public static java.util.Map<String, List<DropdownGuiScreen.Module>> orbitCategories() {
      DropdownGuiScreen router = new DropdownGuiScreen() {
         @Override
         protected void init() {
            VoidCyanClient.openMainGui(this.client, null);
         }
      };
      router.initTabs();
      java.util.Map<String, List<DropdownGuiScreen.Module>> out = new java.util.LinkedHashMap<>();
      for (DropdownGuiScreen.Category cat : tabWindows.getOrDefault(0, new ArrayList<>())) {
         if (!cat.name.equals("Settings")) out.put(cat.name, cat.modules);
      }

      return out;
   }

   private List<DropdownGuiScreen.Category> getActiveCategories() {
      return tabWindows.getOrDefault(activeTab, new ArrayList<>());
   }

   public DropdownGuiScreen() {
      super(Text.literal("VoidCyan"));
   }

   protected void init() {
      super.init();
      GuiScaleManager.update(this.client);
      this.width = GuiScaleManager.logicalWidth(this.client);
      this.height = GuiScaleManager.logicalHeight(this.client);
      this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 30, 200, 16, Text.literal("Search..."));
      this.searchField.setMaxLength(50);
      this.addDrawableChild(this.searchField);
      if (!initialized) {
         this.initTabs();
         initialized = true;
      }

      int total = 0;

      for (DropdownGuiScreen.Category cat : this.getActiveCategories()) {
         total += cat.modules.size();
      }

      this.rowAnims = new float[total];
      this.lastFrameTime = System.currentTimeMillis();
   }

   private void initTabs() {
      final MinecraftClient c = MinecraftClient.getInstance();
      int margin = 118;
      List<DropdownGuiScreen.Category> mods = new ArrayList<>();
      DropdownGuiScreen.Category hud = new DropdownGuiScreen.Category("HUD", 0, 0);
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "FPS Counter",
               () -> VoidCyanClient.isFpsCounterEnabled = !VoidCyanClient.isFpsCounterEnabled,
               () -> VoidCyanClient.isFpsCounterEnabled,
               () -> c.setScreen(new FpsCounterSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Coordinates",
               () -> VoidCyanClient.isCoordinatesEnabled = !VoidCyanClient.isCoordinatesEnabled,
               () -> VoidCyanClient.isCoordinatesEnabled,
               () -> c.setScreen(new CoordinatesSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "CPS Counter",
               () -> VoidCyanClient.isCpsEnabled = !VoidCyanClient.isCpsEnabled,
               () -> VoidCyanClient.isCpsEnabled,
               () -> c.setScreen(new CpsSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Keystrokes",
               () -> VoidCyanClient.isKeystrokesEnabled = !VoidCyanClient.isKeystrokesEnabled,
               () -> VoidCyanClient.isKeystrokesEnabled,
               () -> c.setScreen(new KeystrokesSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Armor Status",
               () -> VoidCyanClient.isArmorStatusEnabled = !VoidCyanClient.isArmorStatusEnabled,
               () -> VoidCyanClient.isArmorStatusEnabled,
               () -> c.setScreen(new ArmorStatusSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Potion Status",
               () -> VoidCyanClient.isPotionStatusEnabled = !VoidCyanClient.isPotionStatusEnabled,
               () -> VoidCyanClient.isPotionStatusEnabled,
               () -> c.setScreen(new PotionStatusSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Watermark",
               () -> VoidCyanClient.isWatermarkEnabled = !VoidCyanClient.isWatermarkEnabled,
               () -> VoidCyanClient.isWatermarkEnabled,
               () -> c.setScreen(new WatermarkSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Combo Counter",
               () -> VoidCyanClient.isComboCounterEnabled = !VoidCyanClient.isComboCounterEnabled,
               () -> VoidCyanClient.isComboCounterEnabled,
               () -> c.setScreen(new ComboCounterSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Ping Display",
               () -> VoidCyanClient.isPingDisplayEnabled = !VoidCyanClient.isPingDisplayEnabled,
               () -> VoidCyanClient.isPingDisplayEnabled,
               () -> c.setScreen(new PingDisplaySettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Keyboard Sounds",
               () -> VoidCyanClient.isKeyboardSoundsEnabled = !VoidCyanClient.isKeyboardSoundsEnabled,
               () -> VoidCyanClient.isKeyboardSoundsEnabled,
               () -> c.setScreen(new KeyboardSoundsSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Scoreboard",
               () -> VoidCyanClient.isScoreboardEnabled = !VoidCyanClient.isScoreboardEnabled,
               () -> VoidCyanClient.isScoreboardEnabled,
               () -> c.setScreen(new ScoreboardSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Shulker Preview",
               () -> VoidCyanClient.isShulkerPreviewEnabled = !VoidCyanClient.isShulkerPreviewEnabled,
               () -> VoidCyanClient.isShulkerPreviewEnabled,
               () -> c.setScreen(new ShulkerPreviewSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Chat",
               () -> VoidCyanClient.isChatModuleEnabled = !VoidCyanClient.isChatModuleEnabled,
               () -> VoidCyanClient.isChatModuleEnabled,
               () -> c.setScreen(new ChatSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Persistent Chat",
               () -> VoidCyanClient.isChatPersistEnabled = !VoidCyanClient.isChatPersistEnabled,
               () -> VoidCyanClient.isChatPersistEnabled,
               null
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Action Bar",
               () -> VoidCyanClient.isActionBarEnabled = !VoidCyanClient.isActionBarEnabled,
               () -> VoidCyanClient.isActionBarEnabled,
               () -> c.setScreen(new ActionBarSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Speed Display",
               () -> VoidCyanClient.isSpeedDisplayEnabled = !VoidCyanClient.isSpeedDisplayEnabled,
               () -> VoidCyanClient.isSpeedDisplayEnabled,
               () -> c.setScreen(new SpeedDisplaySettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "TPS Display",
               () -> VoidCyanClient.isTpsDisplayEnabled = !VoidCyanClient.isTpsDisplayEnabled,
               () -> VoidCyanClient.isTpsDisplayEnabled,
               () -> c.setScreen(new TpsDisplaySettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "ArrayList",
               () -> VoidCyanClient.isArrayListEnabled = !VoidCyanClient.isArrayListEnabled,
               () -> VoidCyanClient.isArrayListEnabled,
               () -> c.setScreen(new ArrayListSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Inventory",
               () -> VoidCyanClient.isInvHudEnabled = !VoidCyanClient.isInvHudEnabled,
               () -> VoidCyanClient.isInvHudEnabled,
               () -> c.setScreen(new InvHudSettingsScreen(this))
            )
         );
      hud.modules
         .add(
            new DropdownGuiScreen.Module(
               "Attack Indicator HUD",
               () -> VoidCyanClient.isAttackHudEnabled = !VoidCyanClient.isAttackHudEnabled,
               () -> VoidCyanClient.isAttackHudEnabled,
               () -> c.setScreen(new AttackHudSettingsScreen(this))
            )
         );
      mods.add(hud);
      DropdownGuiScreen.Category combat = new DropdownGuiScreen.Category("Combat", 0, 0);
      combat.modules
         .add(
            new DropdownGuiScreen.Module(
                  "Hit Color",
                  () -> VoidCyanClient.isHitColorEnabled = !VoidCyanClient.isHitColorEnabled,
                  () -> VoidCyanClient.isHitColorEnabled,
                  () -> c.setScreen(new HitColorSettingsScreen(this))
               )
               .addSetting(
                  new DropdownGuiScreen.BoolSetting("Apply to Self", () -> VoidCyanClient.hitColorApplyToSelf, val -> VoidCyanClient.hitColorApplyToSelf = val)
               )
               .addSetting(
                  new DropdownGuiScreen.SliderSetting("Transparency", 0, 255, () -> VoidCyanClient.hitColorAlpha, val -> VoidCyanClient.hitColorAlpha = val)
               )
               .addSetting(new DropdownGuiScreen.SliderSetting("Red", 0, 255, () -> VoidCyanClient.hitColorRed, val -> VoidCyanClient.hitColorRed = val))
               .addSetting(new DropdownGuiScreen.SliderSetting("Green", 0, 255, () -> VoidCyanClient.hitColorGreen, val -> VoidCyanClient.hitColorGreen = val))
               .addSetting(new DropdownGuiScreen.SliderSetting("Blue", 0, 255, () -> VoidCyanClient.hitColorBlue, val -> VoidCyanClient.hitColorBlue = val))
         );
      combat.modules
         .add(
            new DropdownGuiScreen.Module(
               "Attack Indicator (3D)",
               () -> VoidCyanClient.attackIndicatorEnabled = !VoidCyanClient.attackIndicatorEnabled,
               () -> VoidCyanClient.attackIndicatorEnabled,
               () -> c.setScreen(new TargetIndicatorSettingsScreen(this))
            )
         );
      combat.modules
         .add(
            new DropdownGuiScreen.Module(
               "Target HUD",
               () -> VoidCyanClient.isTargetHudEnabled = !VoidCyanClient.isTargetHudEnabled,
               () -> VoidCyanClient.isTargetHudEnabled,
               () -> c.setScreen(new TargetHudSettingsScreen(this))
            )
         );
      combat.modules
         .add(
            new DropdownGuiScreen.Module(
               "Effects",
               () -> com.voidcyan.client.CritEffectsManager.enabled = !com.voidcyan.client.CritEffectsManager.enabled,
               () -> com.voidcyan.client.CritEffectsManager.enabled,
               () -> c.setScreen(new EffectsSettingsScreen(this))
            )
         );
      mods.add(combat);
      DropdownGuiScreen.Category visual = new DropdownGuiScreen.Category("Visual", 0, 0);
      visual.modules
         .add(
            new DropdownGuiScreen.Module(
               "Name Tag Items",
               () -> VoidCyanClient.isNameTagItemsEnabled = !VoidCyanClient.isNameTagItemsEnabled,
               () -> VoidCyanClient.isNameTagItemsEnabled,
               () -> c.setScreen(new NameTagItemsSettingsScreen(this))
            )
         );
      visual.modules
         .add(
            new DropdownGuiScreen.Module(
               "Custom Crosshair",
               () -> VoidCyanClient.isCustomCrosshairEnabled = !VoidCyanClient.isCustomCrosshairEnabled,
               () -> VoidCyanClient.isCustomCrosshairEnabled,
               () -> c.setScreen(new CustomCrosshairSettingsScreen(this))
            )
         );
      visual.modules
         .add(
            new DropdownGuiScreen.Module(
               "Item Animations",
               () -> VoidCyanClient.isItemAnimationsEnabled = !VoidCyanClient.isItemAnimationsEnabled,
               () -> VoidCyanClient.isItemAnimationsEnabled,
               () -> c.setScreen(new ItemAnimationsSettingsScreen(this))
            )
         );
      visual.modules
         .add(
            new DropdownGuiScreen.Module(
               "Big Head",
               () -> VoidCyanClient.isBigHeadEnabled = !VoidCyanClient.isBigHeadEnabled,
               () -> VoidCyanClient.isBigHeadEnabled,
               () -> c.setScreen(new BigHeadSettingsScreen(this))
            )
         );
      visual.modules
         .add(
            new DropdownGuiScreen.Module(
               "Fullbright",
               () -> VoidCyanClient.isFullbrightEnabled = !VoidCyanClient.isFullbrightEnabled,
               () -> VoidCyanClient.isFullbrightEnabled,
               () -> c.setScreen(new FullbrightSettingsScreen(this))
            )
         );
      mods.add(visual);
      DropdownGuiScreen.Category player = new DropdownGuiScreen.Category("Player", 0, 0);
      player.modules
         .add(
            new DropdownGuiScreen.Module(
               "Freelook",
               () -> VoidCyanClient.isFreelookEnabled = !VoidCyanClient.isFreelookEnabled,
               () -> VoidCyanClient.isFreelookEnabled,
               () -> c.setScreen(new FreelookSettingsScreen(this))
            )
         );
      player.modules.add(new DropdownGuiScreen.Module("Zoom", () -> {}, () -> false, () -> c.setScreen(new ZoomSettingsScreen(this))));
      mods.add(player);
      DropdownGuiScreen.Category settings = new DropdownGuiScreen.Category("Settings", 0, 0);
      settings.modules.add(new DropdownGuiScreen.Module("Switch to Square GUI", () -> {
         VoidCyanClient.guiType = 0;
         VoidCyanClient.saveConfig();
         MinecraftClient.getInstance().setScreen(new ClickGuiScreen(null));
      }, () -> false, null));
      mods.add(settings);
      int curX = margin;
      int curY = 56;
      int rowMaxH = 0;

      for (DropdownGuiScreen.Category cat : mods) {
         if (curX + 140 > this.width - margin && curX > margin) {
            curX = margin;
            curY += rowMaxH + margin;
            rowMaxH = 0;
         }

         cat.x = curX;
         cat.y = curY;
         curX += 140 + margin;
         int panelH = 18 + (cat.collapsed ? 0 : cat.modules.size() * 18);
         if (panelH > rowMaxH) {
            rowMaxH = panelH;
         }
      }

      tabWindows.put(0, mods);
      List<DropdownGuiScreen.Category> screenshots = new ArrayList<>();
      screenshots.add(new DropdownGuiScreen.Category("Screenshots Manager", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.CustomWindowRenderer() {
         @Override
         public void render(DrawContext ctx, int x, int y, int w, int mx, int my) {
            ctx.drawTextWithShadow(c.textRenderer, Text.literal("Screenshot management coming soon..."), x + 5, y + 5, -1);
         }

         @Override
         public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w) {
            return false;
         }

         @Override
         public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         }

         @Override
         public void keyPressed(int k, int s, int m) {
         }

         @Override
         public void charTyped(char ch, int m) {
         }
      }));
      tabWindows.put(1, screenshots);
      List<DropdownGuiScreen.Category> bgs = new ArrayList<>();
      bgs.add(new DropdownGuiScreen.Category("Backgrounds", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.CustomWindowRenderer() {
         @Override
         public void render(DrawContext ctx, int x, int y, int w, int mx, int my) {
            ctx.drawTextWithShadow(c.textRenderer, Text.literal("Background selector coming soon..."), x + 5, y + 5, -1);
         }

         @Override
         public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w) {
            return false;
         }

         @Override
         public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         }

         @Override
         public void keyPressed(int k, int s, int m) {
         }

         @Override
         public void charTyped(char ch, int m) {
         }
      }));
      tabWindows.put(2, bgs);
      List<DropdownGuiScreen.Category> stgs = new ArrayList<>();
      stgs.add(new DropdownGuiScreen.Category("Global Settings", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.SettingsRenderer()));
      tabWindows.put(3, stgs);
      List<DropdownGuiScreen.Category> frds = new ArrayList<>();
      frds.add(new DropdownGuiScreen.Category("Friends List", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.CustomWindowRenderer() {
         @Override
         public void render(DrawContext ctx, int x, int y, int w, int mx, int my) {
            ctx.drawTextWithShadow(c.textRenderer, Text.literal("Friends list coming soon..."), x + 5, y + 5, -1);
         }

         @Override
         public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w) {
            return false;
         }

         @Override
         public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         }

         @Override
         public void keyPressed(int k, int s, int m) {
         }

         @Override
         public void charTyped(char ch, int m) {
         }
      }));
      tabWindows.put(4, frds);
      List<DropdownGuiScreen.Category> cfgs = new ArrayList<>();
      cfgs.add(new DropdownGuiScreen.Category("Configurations", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.CustomWindowRenderer() {
         @Override
         public void render(DrawContext ctx, int x, int y, int w, int mx, int my) {
            ctx.drawTextWithShadow(c.textRenderer, Text.literal("Config Manager coming soon..."), x + 5, y + 5, -1);
         }

         @Override
         public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w) {
            return false;
         }

         @Override
         public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         }

         @Override
         public void keyPressed(int k, int s, int m) {
         }

         @Override
         public void charTyped(char ch, int m) {
         }
      }));
      tabWindows.put(5, cfgs);
      List<DropdownGuiScreen.Category> stat = new ArrayList<>();
      stat.add(new DropdownGuiScreen.Category("Session Statistics", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.StatsRenderer()));
      tabWindows.put(6, stat);
      List<DropdownGuiScreen.Category> notes = new ArrayList<>();
      notes.add(new DropdownGuiScreen.Category("Notes", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.CustomWindowRenderer() {
         @Override
         public void render(DrawContext ctx, int x, int y, int w, int mx, int my) {
            ctx.drawTextWithShadow(c.textRenderer, Text.literal("Notes coming soon..."), x + 5, y + 5, -1);
         }

         @Override
         public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w) {
            return false;
         }

         @Override
         public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         }

         @Override
         public void keyPressed(int k, int s, int m) {
         }

         @Override
         public void charTyped(char ch, int m) {
         }
      }));
      tabWindows.put(7, notes);
      List<DropdownGuiScreen.Category> calc = new ArrayList<>();
      calc.add(new DropdownGuiScreen.Category("Calculator", margin, 56).withCustom(-1, -1, new DropdownGuiScreen.CustomWindowRenderer() {
         @Override
         public void render(DrawContext ctx, int x, int y, int w, int mx, int my) {
            ctx.drawTextWithShadow(c.textRenderer, Text.literal("Calculator coming soon..."), x + 5, y + 5, -1);
         }

         @Override
         public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w) {
            return false;
         }

         @Override
         public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
         }

         @Override
         public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         }

         @Override
         public void keyPressed(int k, int s, int m) {
         }

         @Override
         public void charTyped(char ch, int m) {
         }
      }));
      tabWindows.put(8, calc);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      GuiScaleManager.update(this.client);
      // Recompute the logical viewport every frame so slider/video-setting scale changes
      // resize the GUI live, without waiting for a window-resize init pass.
      this.width = GuiScaleManager.logicalWidth(this.client);
      this.height = GuiScaleManager.logicalHeight(this.client);
      float scaleRatio = GuiScaleManager.renderScale();
      context.getMatrices().pushMatrix();
      context.getMatrices().scale(scaleRatio, scaleRatio);
      long now = System.currentTimeMillis();
      float dt = Math.min((float)(now - this.lastFrameTime) / 1000.0F, 0.1F);
      this.lastFrameTime = now;
      int accent = VoidCyanClient.getPrimaryColor() | 0xFF000000;
      // Vanilla-style logical mouse coords (vanilla screen coords already equal logical space).
      double mx = GuiScaleManager.toLogical((double)mouseX);
      double my = GuiScaleManager.toLogical((double)mouseY);
      this.renderSidebar(context, (int)mx, (int)my);
      int animIdx = 0;

      for (DropdownGuiScreen.Category cat : this.getActiveCategories()) {
         animIdx = this.renderCategory(context, cat, (int)mx, (int)my, dt, accent, animIdx, this.searchField.getText().toLowerCase());
      }

      // Vanilla widgets (search field, blur) live in logical space too, so keep them
      // inside the scaled matrix and forward vanilla mouse coords unchanged.
      super.render(context, mouseX, mouseY, deltaTicks);
      context.getMatrices().popMatrix();
   }

   private int getSidebarTabStartY() {
      return 38;
   }

   private int getSidebarTabSpacing() {
      int startY = this.getSidebarTabStartY();
      int avail = this.height - startY - 8;
      int calculated = avail / TABS.length;
      return Math.max(20, Math.min(31, calculated));
   }

   private int getSidebarTabHeight() {
      return Math.max(18, this.getSidebarTabSpacing() - 4);
   }

   private void renderSidebar(DrawContext ctx, int mouseX, int mouseY) {
      int accent = VoidCyanClient.getPrimaryColor();
      int sidebarW = 125;
      ctx.fill(0, 0, sidebarW, this.height, -435548652);
      ctx.fill(sidebarW, 0, sidebarW + 1, this.height, -15065564);
      int tabStartY = this.getSidebarTabStartY();
      int titleY = Math.max(10, tabStartY - 22);
      ctx.drawTextWithShadow(this.textRenderer, Text.literal("VOIDCYAN"), 14, titleY, accent);
      int tabSpacing = this.getSidebarTabSpacing();
      int tabH = this.getSidebarTabHeight();
      int tabY = tabStartY;

      for (int i = 0; i < TABS.length; i++) {
         boolean isHovered = mouseX >= 10 && mouseX <= sidebarW - 10 && mouseY >= tabY && mouseY <= tabY + tabH;
         if (isHovered) {
            this.tabHoverProgress[i] = Math.min(1.0F, this.tabHoverProgress[i] + 0.1F);
         } else {
            this.tabHoverProgress[i] = Math.max(0.0F, this.tabHoverProgress[i] - 0.1F);
         }

         int tabX = 10;
         float textAlpha = 0.5F;
         if (i == activeTab) {
            textAlpha = 1.0F;
            int bgCol = 838860800 | accent & 16777215;
            drawRoundedRect(ctx, tabX, tabY, sidebarW - 20, tabH, 6, bgCol);
            int indCol = 0xFF000000 | accent & 16777215;
            int indH = Math.min(15, tabH - 6);
            drawRoundedRect(ctx, tabX - 10, tabY + (tabH - indH) / 2, 4, indH, 2, indCol);
         } else if (this.tabHoverProgress[i] > 0.0F) {
            textAlpha = 0.5F + this.tabHoverProgress[i] * 0.5F;
            int bgCol = (int)(20.0F * this.tabHoverProgress[i]) << 24 | 16777215;
            drawRoundedRect(ctx, tabX, tabY, sidebarW - 20, tabH, 6, bgCol);
         }

         int labelColor = (int)(textAlpha * 255.0F) << 24 | 16777215;
         Identifier iconTex = TAB_ICONS[i];
         int iconY = tabY + (tabH - 12) / 2;
         ctx.drawTexturedQuad(iconTex, tabX + 8, iconY, tabX + 20, iconY + 12, 0.0F, 1.0F, 0.0F, 1.0F);
         int textY = tabY + (tabH - 8) / 2;
         ctx.drawTextWithShadow(this.textRenderer, Text.literal(TABS[i]), tabX + 24, textY, labelColor);
         tabY += tabSpacing;
      }
   }

   private static void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
      if (radius > 0 && w > 0 && h > 0) {
         int maxR = Math.min(w / 2, h / 2);
         radius = Math.min(radius, maxR);
         ctx.fill(x + radius, y, x + w - radius, y + h, color);
         ctx.fill(x, y + radius, x + radius, y + h - radius, color);
         ctx.fill(x + w - radius, y + radius, x + w, y + h - radius, color);
         fillCircle(ctx, x + radius, y + radius, radius, color);
         fillCircle(ctx, x + w - radius, y + radius, radius, color);
         fillCircle(ctx, x + radius, y + h - radius, radius, color);
         fillCircle(ctx, x + w - radius, y + h - radius, radius, color);
      } else {
         ctx.fill(x, y, x + w, y + h, color);
      }
   }

   private static void fillCircle(DrawContext ctx, int cx, int cy, int r, int color) {
      int r2 = r * r;

      for (int dy = -r; dy <= r; dy++) {
         int maxX = (int)Math.sqrt(r2 - dy * dy);
         if (maxX > 0) {
            ctx.fill(cx - maxX, cy + dy, cx + maxX + 1, cy + dy + 1, color);
         }
      }
   }

   private boolean isSidebarTabClicked(double mx, double my) {
      int sidebarW = 125;
      int tabStartY = this.getSidebarTabStartY();
      int tabSpacing = this.getSidebarTabSpacing();
      int tabH = this.getSidebarTabHeight();
      if (mx >= 10.0 && mx <= sidebarW - 10 && my >= tabStartY) {
         int idx = (int)((my - tabStartY) / tabSpacing);
         int relY = (int)((my - tabStartY) % tabSpacing);
         return idx >= 0 && idx < TABS.length && relY <= tabH;
      } else {
         return false;
      }
   }

   private int renderCategory(DrawContext ctx, DropdownGuiScreen.Category cat, int mouseX, int mouseY, float dt, int accent, int startAnimIdx, String search) {
      int x = cat.x;
      int y = cat.y;
      int panelW = cat.getPanelWidth(this.width);
      List<DropdownGuiScreen.Module> visibleMods = new ArrayList<>();

      for (DropdownGuiScreen.Module mod : cat.modules) {
         if (com.voidcyan.client.ModuleDescriptions.matches(mod.name, search)) {
            visibleMods.add(mod);
         }
      }

      if (!search.isEmpty() && visibleMods.isEmpty()) {
         return startAnimIdx + cat.modules.size();
      } else {
         boolean headerHovered = mouseX >= x && mouseX <= x + panelW && mouseY >= y && mouseY <= y + 18;
         int headerBg = headerHovered ? -14013910 : -14803426;
         ctx.fill(x, y, x + panelW, y + 18, headerBg);
         ctx.fill(x, y, x + 2, y + 18, accent);
         ctx.fill(x, y + 18 - 1, x + panelW, y + 18, accent);
         String label = (cat.collapsed ? "▶ " : "▼ ") + cat.name;
         ctx.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 6, y + 5, -1);
         if (cat.collapsed) {
            return startAnimIdx + cat.modules.size();
         } else if (cat.customRenderer != null) {
            ctx.fill(x, y + 18, x + panelW, y + 18 + cat.getPanelHeight(this.height), -300674028);
            ctx.fill(x, y + 18, x + 2, y + 18 + cat.getPanelHeight(this.height), 1426063360 | accent & 16777215);
            cat.customRenderer.render(ctx, x + 2, y + 18, panelW - 2, mouseX, mouseY);
            ctx.fill(x, y + 18 + cat.getPanelHeight(this.height) - 1, x + panelW, y + 18 + cat.getPanelHeight(this.height), 1157627903);
            return startAnimIdx + cat.modules.size();
         } else {
            int panelH = 0;

            for (DropdownGuiScreen.Module modx : visibleMods) {
               panelH += 18;
               if (modx.expanded) {
                  panelH += modx.inlineSettings.size() * 16;
               }
            }

            ctx.fill(x, y + 18, x + 140, y + 18 + panelH, -300674028);
            ctx.fill(x, y + 18, x + 2, y + 18 + panelH, 1426063360 | accent & 16777215);
            int animIdx = startAnimIdx;
            int currentY = y + 18;

            for (int i = 0; i < visibleMods.size(); i++) {
               DropdownGuiScreen.Module modxx = visibleMods.get(i);
               boolean hovered = mouseX >= x && mouseX <= x + 140 && mouseY >= currentY && mouseY < currentY + 18;
               float target = hovered ? 1.0F : 0.0F;
               int ai = animIdx++;
               if (ai < this.rowAnims.length) {
                  this.rowAnims[ai] = this.rowAnims[ai] + (target - this.rowAnims[ai]) * Math.min(1.0F, dt * 12.0F);
               }

               float anim = ai < this.rowAnims.length ? this.rowAnims[ai] : 0.0F;
               if (anim > 0.01F) {
                  int ha = (int)(anim * 40.0F);
                  ctx.fill(x + 2, currentY, x + 140, currentY + 18, ha << 24 | 16777215);
               }

               boolean on = modxx.enabled.getAsBoolean();
               int indicatorColor = on ? accent : -12303275;
               ctx.fill(x + 4, currentY + 4, x + 8, currentY + 18 - 4, indicatorColor);
               int nameColor = on ? -1118482 : -8947832;
               ctx.drawTextWithShadow(this.textRenderer, Text.literal(modxx.name), x + 13, currentY + 5, nameColor);
               if (hovered && (modxx.openSettings != null || !modxx.inlineSettings.isEmpty())) {
                  String gear = modxx.expanded ? "v" : ">";
                  int gearX = x + 140 - this.textRenderer.getWidth(gear) - 4;
                  ctx.drawTextWithShadow(this.textRenderer, Text.literal(gear), gearX, currentY + 5, -10066313);
               }

               currentY += 18;
               if (modxx.expanded) {
                  for (DropdownGuiScreen.Setting s : modxx.inlineSettings) {
                     s.render(ctx, x + 2, currentY, 138, mouseX, mouseY);
                     currentY += 16;
                  }
               }

               if (i < visibleMods.size() - 1) {
                  ctx.fill(x + 4, currentY - 1, x + 140 - 4, currentY, 587202559);
               }
            }

            ctx.fill(x, y + 18 + panelH - 1, x + 140, y + 18 + panelH, 1157627903);
            return startAnimIdx + cat.modules.size();
         }
      }
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      GuiScaleManager.update(this.client);
      double mx = GuiScaleManager.toLogical((double)click.x());
      double my = GuiScaleManager.toLogical((double)click.y());
      int btn = click.button();
      if (btn == 0 && this.isSidebarTabClicked(mx, my)) {
         int tabStartY = this.getSidebarTabStartY();
         int tabSpacing = this.getSidebarTabSpacing();
         int idx = (int)((my - tabStartY) / tabSpacing);
         if (activeTab != idx) {
            activeTab = idx;
            int total = 0;

            for (DropdownGuiScreen.Category cat : this.getActiveCategories()) {
               total += cat.modules.size();
            }

            this.rowAnims = new float[total];
         }

         return true;
      } else if (mx <= 110.0) {
         return true;
      } else {
         String search = this.searchField != null ? this.searchField.getText().toLowerCase() : "";

         label138:
         for (DropdownGuiScreen.Category cat : this.getActiveCategories()) {
            int x = cat.x;
            int y = cat.y;
            int panelW = cat.getPanelWidth(this.width);
            if (mx >= x && mx <= x + panelW && my >= y && my <= y + 18) {
               if (btn == 0) {
                  cat.dragging = true;
                  cat.hasDragged = false;
                  cat.dragOffsetX = (int)(mx - x);
                  cat.dragOffsetY = (int)(my - y);
               }

               return true;
            }

            if (!cat.collapsed) {
               if (cat.customRenderer != null) {
                  if (mx >= x
                     && mx <= x + panelW
                     && my >= y + 18
                     && my <= y + 18 + cat.getPanelHeight(this.height)
                     && cat.customRenderer.mouseClicked(mx, my, btn, x + 2, y + 18, panelW - 2)) {
                     return true;
                  }
               } else {
                  List<DropdownGuiScreen.Module> visibleMods = new ArrayList<>();

                  for (DropdownGuiScreen.Module mod : cat.modules) {
                     if (com.voidcyan.client.ModuleDescriptions.matches(mod.name, search)) {
                        visibleMods.add(mod);
                     }
                  }

                  int currentY = y + 18;
                  Iterator var26 = visibleMods.iterator();

                  DropdownGuiScreen.Module modx;
                  int modY;
                  do {
                     if (!var26.hasNext()) {
                        continue label138;
                     }

                     modx = (DropdownGuiScreen.Module)var26.next();
                     modY = currentY;
                     currentY += 18;
                     if (modx.expanded) {
                        for (DropdownGuiScreen.Setting s : modx.inlineSettings) {
                           if (s.mouseClicked(mx, my, btn, x + 2, currentY, 138)) {
                              return true;
                           }

                           currentY += 16;
                        }
                     }
                  } while (!(mx >= x) || !(mx <= x + 140) || !(my >= modY) || !(my < modY + 18));

                  if (btn == 0) {
                     if (VoidCyanClient.isSafeModeBlocked(modx.name) && !modx.enabled.getAsBoolean()) {
                        VoidCyanClient.showSafeModeBlockedNotification(modx.name);
                     } else {
                        modx.toggle.run();
                     }

                     VoidCyanClient.saveConfig();
                  } else if (btn == 1) {
                     if (!modx.inlineSettings.isEmpty()) {
                        modx.expanded = !modx.expanded;
                     } else if (modx.openSettings != null) {
                        modx.openSettings.run();
                     }
                  }

                  return true;
               }
            }
         }

         return super.mouseClicked(GuiScaleManager.toLogical(click), false);
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      GuiScaleManager.update(this.client);
      double mx = GuiScaleManager.toLogical((double)click.x());
      double my = GuiScaleManager.toLogical((double)click.y());

      for (DropdownGuiScreen.Category cat : this.getActiveCategories()) {
         if (!cat.collapsed) {
            int panelW = cat.getPanelWidth(this.width);
            if (cat.customRenderer != null) {
               cat.customRenderer.mouseDragged(mx, my, click.button(), cat.x + 2, cat.y + 18, panelW - 2);
            } else {
               int currentY = cat.y + 18;

               for (DropdownGuiScreen.Module mod : cat.modules) {
                  currentY += 18;
                  if (mod.expanded) {
                     for (DropdownGuiScreen.Setting s : mod.inlineSettings) {
                        s.mouseDragged(mx, my, click.button(), cat.x + 2, currentY, panelW - 2);
                        currentY += 16;
                     }
                  }
               }
            }
         }
      }

      if (click.button() == 0) {
         for (DropdownGuiScreen.Category catx : this.getActiveCategories()) {
            if (catx.dragging) {
               int panelW = catx.getPanelWidth(this.width);
               catx.x = (int)(mx - catx.dragOffsetX);
               catx.y = (int)(my - catx.dragOffsetY);
               catx.x = Math.max(0, Math.min(catx.x, this.width - panelW));
               catx.y = Math.max(0, Math.min(catx.y, this.height - 18));
               catx.hasDragged = true;
               return true;
            }
         }
      }

      return super.mouseDragged(GuiScaleManager.toLogical(click), offsetX, offsetY);
   }

   public boolean mouseReleased(Click click) {
      GuiScaleManager.update(this.client);
      double mx = GuiScaleManager.toLogical((double)click.x());
      double my = GuiScaleManager.toLogical((double)click.y());

      for (DropdownGuiScreen.Category cat : this.getActiveCategories()) {
         if (!cat.collapsed) {
            int panelW = cat.getPanelWidth(this.width);
            if (cat.customRenderer != null) {
               cat.customRenderer.mouseReleased(mx, my, click.button(), cat.x + 2, cat.y + 18, panelW - 2);
            } else {
               int currentY = cat.y + 18;

               for (DropdownGuiScreen.Module mod : cat.modules) {
                  currentY += 18;
                  if (mod.expanded) {
                     for (DropdownGuiScreen.Setting s : mod.inlineSettings) {
                        s.mouseReleased(mx, my, click.button(), cat.x + 2, currentY, panelW - 2);
                        currentY += 16;
                     }
                  }
               }
            }
         }
      }

      if (click.button() == 0) {
         for (DropdownGuiScreen.Category catx : this.getActiveCategories()) {
            if (catx.dragging) {
               if (!catx.hasDragged) {
                  catx.collapsed = !catx.collapsed;
               }

               catx.dragging = false;
               catx.hasDragged = false;
            }
         }
      }

      return super.mouseReleased(GuiScaleManager.toLogical(click));
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      GuiScaleManager.update(this.client);
      double mx = GuiScaleManager.toLogical(mouseX);
      double my = GuiScaleManager.toLogical(mouseY);
      for (DropdownGuiScreen.Category cat : this.getActiveCategories()) {
         if (!cat.collapsed && cat.customRenderer != null) {
            int panelW = cat.getPanelWidth(this.width);
            if (mx >= cat.x && mx <= cat.x + panelW && my >= cat.y + 18 && my <= cat.y + 18 + cat.getPanelHeight(this.height)) {
               cat.customRenderer.mouseScrolled(mx, my, verticalAmount, cat.x + 2, cat.y + 18, panelW - 2);
               return true;
            }
         }
      }

      return super.mouseScrolled(mx, my, horizontalAmount, verticalAmount);
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      assert this.client != null;

      this.client.setScreen(null);
   }

   public static class BoolSetting extends DropdownGuiScreen.Setting {
      private final Supplier<Boolean> get;
      private final Consumer<Boolean> set;

      public BoolSetting(String name, Supplier<Boolean> get, Consumer<Boolean> set) {
         super(name);
         this.get = get;
         this.set = set;
      }

      @Override
      public void render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
         MinecraftClient client = MinecraftClient.getInstance();
         ctx.fill(x, y, x + width, y + 16, -1442840576);
         ctx.drawTextWithShadow(client.textRenderer, Text.literal(this.name), x + 4, y + 4, -2236963);
         boolean val = this.get.get();
         ctx.fill(x + width - 14, y + 4, x + width - 4, y + 14, val ? -16711936 : -65536);
      }

      @Override
      public boolean mouseClicked(double mx, double my, int btn, int x, int y, int width) {
         if (mx >= x && mx <= x + width && my >= y && my <= y + 16 && btn == 0) {
            this.set.accept(!this.get.get());
            VoidCyanClient.saveConfig();
            return true;
         } else {
            return false;
         }
      }
   }

   private static class Category {
      final String name;
      final List<DropdownGuiScreen.Module> modules = new ArrayList<>();
      DropdownGuiScreen.CustomWindowRenderer customRenderer;
      int customWidth;
      int customHeight;
      int x;
      int y;
      boolean collapsed = false;
      boolean dragging = false;
      boolean hasDragged = false;
      int dragOffsetX;
      int dragOffsetY;

      Category(String name, int x, int y) {
         this.name = name;
         this.x = x;
         this.y = y;
      }

      DropdownGuiScreen.Category withCustom(int w, int h, DropdownGuiScreen.CustomWindowRenderer r) {
         this.customWidth = w;
         this.customHeight = h;
         this.customRenderer = r;
         return this;
      }

      int getPanelWidth(int screenW) {
         if (this.customWidth == -1) {
            return Math.max(100, screenW - this.x - 8);
         } else {
            return this.customWidth > 0 ? this.customWidth : 140;
         }
      }

      int getPanelHeight(int screenH) {
         return this.customHeight == -1 ? Math.max(100, screenH - this.y - 18 - 8) : this.customHeight;
      }
   }

   public interface CustomWindowRenderer {
      void render(DrawContext var1, int var2, int var3, int var4, int var5, int var6);

      boolean mouseClicked(double var1, double var3, int var5, int var6, int var7, int var8);

      void mouseDragged(double var1, double var3, int var5, int var6, int var7, int var8);

      void mouseReleased(double var1, double var3, int var5, int var6, int var7, int var8);

      void mouseScrolled(double var1, double var3, double var5, int var7, int var8, int var9);

      void keyPressed(int var1, int var2, int var3);

      void charTyped(char var1, int var2);
   }

   public static class Module {
      public final String name;
      public final Runnable toggle;
      public final BooleanSupplier enabled;
      public final Runnable openSettings;
      public final List<DropdownGuiScreen.Setting> inlineSettings = new ArrayList<>();
      public boolean expanded = false;

      public Module(String name, Runnable toggle, BooleanSupplier enabled, Runnable openSettings) {
         this.name = name;
         this.toggle = toggle;
         this.enabled = enabled;
         this.openSettings = openSettings;
      }

      public DropdownGuiScreen.Module addSetting(DropdownGuiScreen.Setting s) {
         this.inlineSettings.add(s);
         return this;
      }
   }

   public abstract static class Setting {
      public final String name;

      public Setting(String name) {
         this.name = name;
      }

      public abstract void render(DrawContext var1, int var2, int var3, int var4, int var5, int var6);

      public abstract boolean mouseClicked(double var1, double var3, int var5, int var6, int var7, int var8);

      public void mouseDragged(double mx, double my, int btn, int x, int y, int width) {
      }

      public void mouseReleased(double mx, double my, int btn, int x, int y, int width) {
      }
   }

   private static class SettingsRenderer implements DropdownGuiScreen.CustomWindowRenderer {
      private int scrollY = 0;
      private int maxScroll = 0;
      private boolean draggingRed = false;
      private boolean draggingGreen = false;
      private boolean draggingBlue = false;
      private boolean draggingGuiAnim = false;
      private boolean draggingScale = false;
      private boolean draggingRgbSpeed = false;

      @Override
      public void render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
         MinecraftClient c = MinecraftClient.getInstance();
         ctx.enableScissor(x, y, x + width, MinecraftClient.getInstance().currentScreen.height);
         int offY = y - this.scrollY;
         ctx.drawTextWithShadow(c.textRenderer, Text.literal("Theme & Colors"), x + 12, offY + 12, -1);

         // 1. RGB Mode Toggle
         int btn1X = x + 12;
         int btn1Y = offY + 28;
         int btn1W = 95;
         int btn1H = 16;
         boolean rgbHovered = mouseX >= btn1X && mouseX <= btn1X + btn1W && mouseY >= btn1Y && mouseY <= btn1Y + btn1H;
         ctx.fill(btn1X, btn1Y, btn1X + btn1W, btn1Y + btn1H, VoidCyanClient.rgbChromaEnabled ? -13421773 : (rgbHovered ? -14540254 : -15658735));
         ctx.drawTextWithShadow(c.textRenderer, Text.literal("1. RGB: " + (VoidCyanClient.rgbChromaEnabled ? "ON" : "OFF")), btn1X + 6, btn1Y + 4, -1);

         // 2. Themes Selector
         int btn2X = x + 115;
         int btn2Y = offY + 28;
         int btn2W = 145;
         int btn2H = 16;
         boolean themeHovered = mouseX >= btn2X && mouseX <= btn2X + btn2W && mouseY >= btn2Y && mouseY <= btn2Y + btn2H;
         ctx.fill(btn2X, btn2Y, btn2X + btn2W, btn2Y + btn2H, themeHovered ? -13421773 : -14540254);
         ctx.drawTextWithShadow(c.textRenderer, Text.literal("2. < " + VoidCyanClient.colorTheme + " >"), btn2X + 6, btn2Y + 4, -1);

         // 3. RGB Speed Slider
         int speedY = offY + 48;
         ctx.drawTextWithShadow(c.textRenderer, Text.literal("3. RGB Speed: " + VoidCyanClient.rgbSpeed), x + 12, speedY + 2, -1);
         int speedTrackX = x + 95;
         int speedTrackY = speedY + 4;
         int speedTrackW = 165;
         ctx.fill(speedTrackX, speedTrackY, speedTrackX + speedTrackW, speedTrackY + 6, -12566464);
         float speedRatio = (VoidCyanClient.rgbSpeed - 1) / 9.0F;
         int speedFillW = (int)(speedTrackW * speedRatio);
         ctx.fill(speedTrackX, speedTrackY + 1, speedTrackX + speedFillW, speedTrackY + 5, VoidCyanClient.getPrimaryColor());
         ctx.fill(speedTrackX + speedFillW - 3, speedTrackY - 1, speedTrackX + speedFillW + 3, speedTrackY + 7, -1);

         int sliderY = offY + 70;
         this.renderSlider(ctx, mouseX, mouseY, x + 12, sliderY, 180, "Red", VoidCyanClient.primaryColorR, 16729156);
         this.renderSlider(ctx, mouseX, mouseY, x + 12, sliderY + 23, 180, "Green", VoidCyanClient.primaryColorG, 4521796);
         this.renderSlider(ctx, mouseX, mouseY, x + 12, sliderY + 46, 180, "Blue", VoidCyanClient.primaryColorB, 4474111);
         int previewSize = 50;
         int previewX = x + width - previewSize - 12;
         int previewY = sliderY;
         int previewColor = VoidCyanClient.getPrimaryColor();
         ctx.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, previewColor);
         int modY = sliderY + 105;

         // Quick preset swatches (same set as the square GUI theme card).
         int presetY = sliderY + 78;
         int presetH = 18;
         int presetAreaW = width - 24;
         int psW = (presetAreaW - 5 * 6) / 6;
         int currentRgb = VoidCyanClient.getPrimaryColor() & 0x00FFFFFF;
         for (int pi = 0; pi < 6; pi++) {
            int pc = ColorPickerModal.PRESETS[pi];
            int px = x + 12 + pi * (psW + 6);
            ctx.fill(px, presetY, px + psW, presetY + presetH, pc);
            if (!VoidCyanClient.rgbChromaEnabled && (pc & 0x00FFFFFF) == currentRgb) {
               ctx.fill(px, presetY, px + psW, presetY + 1, -1);
               ctx.fill(px, presetY + presetH - 1, px + psW, presetY + presetH, -1);
               ctx.fill(px, presetY, px + 1, presetY + presetH, -1);
               ctx.fill(px + psW - 1, presetY, px + psW, presetY + presetH, -1);
            }
         }

         ctx.drawTextWithShadow(c.textRenderer, Text.literal("GUI Options"), x + 12, modY, -1);
         int guiAnimY = modY + 20;
         String animLabel = "Animation: " + VoidCyanClient.guiAnimationDurationMs + "ms";
         ctx.drawTextWithShadow(c.textRenderer, Text.literal(animLabel), x + 12, guiAnimY, -1);
         int trackY = guiAnimY + 12;
         ctx.fill(x + 12, trackY, x + 12 + 250, trackY + 6, -12566464);
         float fillRatio = VoidCyanClient.guiAnimationDurationMs / 2000.0F;
         int fillWidth = (int)(250.0F * fillRatio);
         int primaryColor = VoidCyanClient.getPrimaryColor();
         ctx.fill(x + 12, trackY + 1, x + 12 + fillWidth, trackY + 5, primaryColor);
         ctx.fill(x + 12 + fillWidth - 4, trackY - 1, x + 12 + fillWidth + 4, trackY + 7, -1);
         int guiScaleY = guiAnimY + 30;
         String scaleLabel = String.format("Click GUI Scale: %.1fx (syncs with GUI Scale video setting)", VoidCyanClient.clickGuiScale);
         ctx.drawTextWithShadow(c.textRenderer, Text.literal(scaleLabel), x + 12, guiScaleY, -1);
         int scaleTrackY = guiScaleY + 12;
         ctx.fill(x + 12, scaleTrackY, x + 12 + 250, scaleTrackY + 6, -12566464);
         float scaleFillRatio = (VoidCyanClient.clickGuiScale - 0.5F) / 1.5F;
         int scaleFillWidth = (int)(250.0F * scaleFillRatio);
         ctx.fill(x + 12, scaleTrackY + 1, x + 12 + scaleFillWidth, scaleTrackY + 5, primaryColor);
         ctx.fill(x + 12 + scaleFillWidth - 4, scaleTrackY - 1, x + 12 + scaleFillWidth + 4, scaleTrackY + 7, -1);
         int typeY = guiScaleY + 30;
         boolean typeHovered = mouseX >= x + 12 && mouseX <= x + 12 + 250 && mouseY >= typeY && mouseY <= typeY + 16;
         ctx.fill(x + 12, typeY, x + 12 + 250, typeY + 16, typeHovered ? -13421773 : -14540254);
         String typeLabel = "GUI Type: " + (new String[]{"Square", "Cool", "Orbit"}[VoidCyanClient.guiType]);
         ctx.drawTextWithShadow(c.textRenderer, Text.literal(typeLabel), x + 16, typeY + 4, -1);
         int safeY = typeY + 26;
         boolean safeHovered = mouseX >= x + 12 && mouseX <= x + 12 + 250 && mouseY >= safeY && mouseY <= safeY + 16;
         ctx.fill(x + 12, safeY, x + 12 + 250, safeY + 16, safeHovered ? -13421773 : -14540254);
         String safeLabel = "Safe Mode: " + (VoidCyanClient.safeModeEnabled ? "ON" : "OFF")
            + (VoidCyanClient.safeModeEnabled ? " (locks Health/NameTag/TargetHUD)" : "");
         ctx.drawTextWithShadow(c.textRenderer, Text.literal(safeLabel), x + 16, safeY + 4, -1);
         if (this.draggingRed) {
            this.updateColor(mouseX, x + 12 + 50, 130, 0);
         }

         if (this.draggingGreen) {
            this.updateColor(mouseX, x + 12 + 50, 130, 1);
         }

         if (this.draggingBlue) {
            this.updateColor(mouseX, x + 12 + 50, 130, 2);
         }

         if (this.draggingRgbSpeed) {
            float r = Math.max(0.0F, Math.min(1.0F, (float)(mouseX - (x + 95)) / 165.0F));
            VoidCyanClient.rgbSpeed = 1 + Math.round(r * 9.0F);
         }

         if (this.draggingGuiAnim) {
            float val = Math.max(0.0F, Math.min(1.0F, (mouseX - (x + 12)) / 250.0F));
            VoidCyanClient.guiAnimationDurationMs = (int)(val * 2000.0F);
         }

         if (this.draggingScale) {
            float val = Math.max(0.0F, Math.min(1.0F, (mouseX - (x + 12)) / 250.0F));
            VoidCyanClient.clickGuiScale = 0.5F + val * 1.5F;
         }

         int btnY = typeY + 30 + 26;
         ctx.drawTextWithShadow(c.textRenderer, Text.literal("Display Options"), x + 12, btnY, -1);
         btnY += 20;
         this.renderButton(ctx, mouseX, mouseY, x + 12, btnY, 250, 20, "Overlay Settings ->");
         btnY += 25;
         this.renderButton(ctx, mouseX, mouseY, x + 12, btnY, 250, 20, "Particle Settings ->");
         btnY += 25;
         this.renderButton(ctx, mouseX, mouseY, x + 12, btnY, 250, 20, "Visual Settings ->");
         btnY += 25;
         this.renderButton(ctx, mouseX, mouseY, x + 12, btnY, 250, 20, "Warnings Settings ->");
         this.maxScroll = Math.max(0, btnY + 56 - offY - (MinecraftClient.getInstance().currentScreen.height - y));
         ctx.disableScissor();
      }

      private void renderSlider(DrawContext ctx, int mx, int my, int x, int y, int w, String label, int val, int color) {
         MinecraftClient c = MinecraftClient.getInstance();
         ctx.drawTextWithShadow(c.textRenderer, Text.literal(label), x, y + 3, -1);
         int trackY = y + 6;
         ctx.fill(x + 50, trackY, x + w, trackY + 8, -12566464);
         float ratio = val / 255.0F;
         int fillW = (int)((w - 50) * ratio);
         ctx.fill(x + 50, trackY + 1, x + 50 + fillW, trackY + 7, 0xFF000000 | color);
         ctx.fill(x + 50 + fillW - 4, trackY - 2, x + 50 + fillW + 6, trackY + 10, -1);
         ctx.drawTextWithShadow(c.textRenderer, Text.literal(String.valueOf(val)), x + w + 10, y + 3, -1);
      }

      private void renderButton(DrawContext ctx, int mx, int my, int x, int y, int w, int h, String label) {
         boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + h;
         ctx.fill(x, y, x + w, y + h, hovered ? -13421773 : -14540254);
         int borderCol = VoidCyanClient.getPrimaryColor();
         ctx.fill(x, y, x + w, y + 1, borderCol);
         ctx.fill(x, y + h - 1, x + w, y + h, borderCol);
         ctx.fill(x, y, x + 1, y + h, borderCol);
         ctx.fill(x + w - 1, y, x + w, y + h, borderCol);
         ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(label), x + 5, y + 6, -1);
      }

      private void updateColor(int mx, int startX, int w, int type) {
         float ratio = Math.max(0.0F, Math.min(1.0F, (float)(mx - startX) / w));
         int val = (int)(ratio * 255.0F);
         if (type == 0) {
            VoidCyanClient.primaryColorR = val;
         } else if (type == 1) {
            VoidCyanClient.primaryColorG = val;
         } else {
            VoidCyanClient.primaryColorB = val;
         }
      }

      @Override
      public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w) {
         int offY = y - this.scrollY;

         // 1. RGB Mode Toggle
         int btn1X = x + 12;
         int btn1Y = offY + 28;
         int btn1W = 95;
         int btn1H = 16;
         if (mx >= btn1X && mx <= btn1X + btn1W && my >= btn1Y && my <= btn1Y + btn1H) {
            VoidCyanClient.rgbChromaEnabled = !VoidCyanClient.rgbChromaEnabled;
            VoidCyanClient.saveConfig();
            return true;
         }

         // 2. Themes Selector
         int btn2X = x + 115;
         int btn2Y = offY + 28;
         int btn2W = 145;
         int btn2H = 16;
         if (mx >= btn2X && mx <= btn2X + btn2W && my >= btn2Y && my <= btn2Y + btn2H) {
            int currentIdx = 0;
            for (int ti = 0; ti < VoidCyanClient.THEMES.length; ti++) {
               if (VoidCyanClient.THEMES[ti].equalsIgnoreCase(VoidCyanClient.colorTheme)) {
                  currentIdx = ti;
                  break;
               }
            }
            if (mx < btn2X + 25) {
               currentIdx = (currentIdx - 1 + VoidCyanClient.THEMES.length) % VoidCyanClient.THEMES.length;
            } else {
               currentIdx = (currentIdx + 1) % VoidCyanClient.THEMES.length;
            }
            VoidCyanClient.applyTheme(VoidCyanClient.THEMES[currentIdx]);
            return true;
         }

         // 3. RGB Speed Slider
         int speedY = offY + 48;
         int speedTrackX = x + 95;
         int speedTrackW = 165;
         if (mx >= speedTrackX && mx <= speedTrackX + speedTrackW && my >= speedY && my <= speedY + 14) {
            this.draggingRgbSpeed = true;
            float r = Math.max(0.0F, Math.min(1.0F, (float)(mx - speedTrackX) / speedTrackW));
            VoidCyanClient.rgbSpeed = 1 + Math.round(r * 9.0F);
            VoidCyanClient.saveConfig();
            return true;
         }

         int sliderY = offY + 70;
         if (mx >= x + 12 + 50 && mx <= x + 12 + 180) {
            if (my >= sliderY + 6 && my <= sliderY + 14) {
               this.draggingRed = true;
            }

            if (my >= sliderY + 29 && my <= sliderY + 37) {
               this.draggingGreen = true;
            }

            if (my >= sliderY + 52 && my <= sliderY + 60) {
               this.draggingBlue = true;
            }
         }

         // Preset swatch clicks (must match render geometry above).
         int presetYc = sliderY + 78;
         int presetHc = 18;
         int presetAreaWc = w - 24;
         int psWc = (presetAreaWc - 5 * 6) / 6;
         if (my >= presetYc && my <= presetYc + presetHc && mx >= x + 12 && mx <= x + 12 + presetAreaWc) {
            int pi = (int)((mx - (x + 12)) / (psWc + 6));
            if (pi >= 0 && pi < 6) {
               int pc = ColorPickerModal.PRESETS[pi] & 0x00FFFFFF;
               VoidCyanClient.colorTheme = "Custom";
               VoidCyanClient.rgbChromaEnabled = false;
               VoidCyanClient.primaryColorR = pc >> 16 & 0xFF;
               VoidCyanClient.primaryColorG = pc >> 8 & 0xFF;
               VoidCyanClient.primaryColorB = pc & 0xFF;
               VoidCyanClient.saveConfig();
               return true;
            }
         }

         int guiAnimY = sliderY + 105 + 20;
         if (mx >= x + 12 && mx <= x + 12 + 250 && my >= guiAnimY + 12 && my <= guiAnimY + 18) {
            this.draggingGuiAnim = true;
         }

         int guiScaleY = guiAnimY + 30;
         if (mx >= x + 12 && mx <= x + 12 + 250 && my >= guiScaleY + 12 && my <= guiScaleY + 18) {
            this.draggingScale = true;
         }

         int typeY = guiScaleY + 30;
         if (mx >= x + 12 && mx <= x + 12 + 250 && my >= typeY && my <= typeY + 16) {
            VoidCyanClient.guiType = (VoidCyanClient.guiType + 1) % 3;
            VoidCyanClient.saveConfig();
            VoidCyanClient.openMainGui(MinecraftClient.getInstance(), null);

            return true;
         }

         int safeY = typeY + 26;
         if (mx >= x + 12 && mx <= x + 12 + 250 && my >= safeY && my <= safeY + 16) {
            VoidCyanClient.safeModeEnabled = !VoidCyanClient.safeModeEnabled;
            if (VoidCyanClient.safeModeEnabled) {
               VoidCyanClient.isHealthIndicatorsEnabled = false;
               VoidCyanClient.isNameTagItemsEnabled = false;
               VoidCyanClient.isTargetHudEnabled = false;
            }

            VoidCyanClient.saveConfig();
            return true;
         } else {
            int btnY = typeY + 30 + 20;
            if (mx >= x + 12 && mx <= x + 12 + 250) {
               MinecraftClient client = MinecraftClient.getInstance();
               Screen parent = client.currentScreen;
               if (my >= btnY && my <= btnY + 20) {
                  client.setScreen(new OverlaysSettingsScreen(parent));
                  return true;
               }

               if (my >= btnY + 25 && my <= btnY + 20 + 25) {
                  client.setScreen(new ParticlesSettingsScreen(parent));
                  return true;
               }

               if (my >= btnY + 50 && my <= btnY + 20 + 50) {
                  client.setScreen(new VisualSettingsScreen(parent));
                  return true;
               }

               if (my >= btnY + 75 && my <= btnY + 20 + 75) {
                  client.setScreen(new WarningsSettingsScreen(parent));
                  return true;
               }
            }

            return this.draggingRed || this.draggingGreen || this.draggingBlue || this.draggingGuiAnim || this.draggingScale || this.draggingRgbSpeed;
         }
      }

      @Override
      public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
         if (this.draggingRed || this.draggingGreen || this.draggingBlue || this.draggingGuiAnim || this.draggingScale || this.draggingRgbSpeed) {
            VoidCyanClient.saveConfig();
         }

         this.draggingRed = false;
         this.draggingGreen = false;
         this.draggingBlue = false;
         this.draggingGuiAnim = false;
         this.draggingScale = false;
         this.draggingRgbSpeed = false;
      }

      @Override
      public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         this.scrollY -= (int)(a * 20.0);
         if (this.scrollY < 0) {
            this.scrollY = 0;
         }

         if (this.scrollY > this.maxScroll) {
            this.scrollY = this.maxScroll;
         }
      }

      @Override
      public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
      }

      @Override
      public void keyPressed(int k, int s, int m) {
      }

      @Override
      public void charTyped(char ch, int m) {
      }
   }

   public static class SliderSetting extends DropdownGuiScreen.Setting {
      private final int min;
      private final int max;
      private final Supplier<Integer> get;
      private final Consumer<Integer> set;
      private boolean dragging = false;

      public SliderSetting(String name, int min, int max, Supplier<Integer> get, Consumer<Integer> set) {
         super(name);
         this.min = min;
         this.max = max;
         this.get = get;
         this.set = set;
      }

      @Override
      public void render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
         MinecraftClient client = MinecraftClient.getInstance();
         ctx.fill(x, y, x + width, y + 16, -1442840576);
         ctx.drawTextWithShadow(client.textRenderer, Text.literal(this.name + ": " + this.get.get()), x + 4, y + 2, -2236963);
         float percent = (float)(this.get.get() - this.min) / (this.max - this.min);
         ctx.fill(x + 4, y + 12, x + width - 4, y + 14, -12303292);
         ctx.fill(x + 4, y + 12, x + 4 + (int)((width - 8) * percent), y + 14, -16711681);
      }

      @Override
      public boolean mouseClicked(double mx, double my, int btn, int x, int y, int width) {
         if (mx >= x && mx <= x + width && my >= y && my <= y + 16 && btn == 0) {
            this.dragging = true;
            this.updateVal(mx, x, width);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void mouseDragged(double mx, double my, int btn, int x, int y, int width) {
         if (this.dragging) {
            this.updateVal(mx, x, width);
         }
      }

      @Override
      public void mouseReleased(double mx, double my, int btn, int x, int y, int width) {
         if (btn == 0) {
            this.dragging = false;
         }
      }

      private void updateVal(double mx, int x, int width) {
         float percent = (float)(mx - (x + 4)) / (width - 8);
         percent = Math.max(0.0F, Math.min(1.0F, percent));
         this.set.accept(this.min + (int)(percent * (this.max - this.min)));
         VoidCyanClient.saveConfig();
      }
   }

   private static class StatsRenderer implements DropdownGuiScreen.CustomWindowRenderer {
      private final Set<Integer> expandedStreaks = new HashSet<>();
      private int scrollY = 0;
      private int maxScroll = 0;

      private String formatTime(long ticks) {
         long sSecs = ticks / 20L;
         long d = sSecs / 86400L;
         long h = sSecs % 86400L / 3600L;
         long m = sSecs % 3600L / 60L;
         long s = sSecs % 60L;
         StringBuilder sb = new StringBuilder();
         if (d > 0L) {
            sb.append(d).append("d ");
         }

         if (h > 0L || d > 0L) {
            sb.append(h).append("h ");
         }

         if (m > 0L || h > 0L || d > 0L) {
            sb.append(m).append("m ");
         }

         sb.append(s).append("s");
         return sb.toString();
      }

      private String kd(int kills, int deaths) {
         return deaths > 0 ? String.format("%.2f", (double)kills / deaths) : kills + ".00";
      }

      private String accuracy(int hits, int clicks) {
         return clicks > 0 ? String.format("%.1f%%", (double)hits / clicks * 100.0) : "0.0%";
      }

      @Override
      public void render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
         MinecraftClient c = MinecraftClient.getInstance();
         int contentHeight = 500;
         ctx.enableScissor(x, y, x + width, MinecraftClient.getInstance().currentScreen.height);
         int offY = y - this.scrollY;
         int colAllTime = x + width - 150;
         int colSession = x + width - 60;
         ctx.drawTextWithShadow(c.textRenderer, Text.literal("All-Time"), colAllTime, offY + 10, VoidCyanClient.getPrimaryColor());
         ctx.drawTextWithShadow(c.textRenderer, Text.literal("Session"), colSession, offY + 10, -5592406);
         ctx.fill(x + 5, offY + 22, x + width - 5, offY + 23, 1090519039);
         String[][] rows = new String[][]{
            {"Kills", String.valueOf(VoidCyanClient.allTimeKills), String.valueOf(VoidCyanClient.sessionKills)},
            {"Deaths", String.valueOf(VoidCyanClient.allTimeDeaths), String.valueOf(VoidCyanClient.sessionDeaths)},
            {"K/D", this.kd(VoidCyanClient.allTimeKills, VoidCyanClient.allTimeDeaths), this.kd(VoidCyanClient.sessionKills, VoidCyanClient.sessionDeaths)},
            {"Totem Pops", String.valueOf(VoidCyanClient.allTimePops), String.valueOf(VoidCyanClient.sessionPops)},
            {"Anchors", String.valueOf(VoidCyanClient.allTimeAnchorKills), String.valueOf(VoidCyanClient.sessionAnchorKills)},
            {"Crystal Kills", String.valueOf(VoidCyanClient.allTimeCrystalKills), String.valueOf(VoidCyanClient.sessionCrystalKills)},
            {"Crystal Deaths", String.valueOf(VoidCyanClient.allTimeCrystalDeaths), String.valueOf(VoidCyanClient.sessionCrystalDeaths)},
            {"Clicks", String.valueOf(VoidCyanClient.allTimeClicks), String.valueOf(VoidCyanClient.sessionClicks)},
            {"Hits", String.valueOf(VoidCyanClient.allTimeHits), String.valueOf(VoidCyanClient.sessionHits)},
            {
                  "Accuracy",
                  this.accuracy(VoidCyanClient.allTimeHits, VoidCyanClient.allTimeClicks),
                  this.accuracy(VoidCyanClient.sessionHits, VoidCyanClient.sessionClicks)
            },
            {"Damage Dealt", String.format("%.1f", VoidCyanClient.allTimeDamage), String.format("%.1f", VoidCyanClient.sessionDamage)},
            {"Blocks Broken", String.valueOf(VoidCyanClient.allTimeBlocksBroken), String.valueOf(VoidCyanClient.sessionBlocksBroken)},
            {"Blocks Placed", String.valueOf(VoidCyanClient.allTimeBlocksPlaced), String.valueOf(VoidCyanClient.sessionBlocksPlaced)},
            {"Crystals Placed", String.valueOf(VoidCyanClient.allTimeCrystalsPlaced), String.valueOf(VoidCyanClient.sessionCrystalsPlaced)},
            {"Crystals Broken", String.valueOf(VoidCyanClient.allTimeCrystalsBroken), String.valueOf(VoidCyanClient.sessionCrystalsBroken)},
            {"Anchors Placed", String.valueOf(VoidCyanClient.allTimeAnchorsPlaced), String.valueOf(VoidCyanClient.sessionAnchorsPlaced)},
            {"Anchors Blown", String.valueOf(VoidCyanClient.allTimeAnchorsBlown), String.valueOf(VoidCyanClient.sessionAnchorsBlown)},
            {"Glowstone Used", String.valueOf(VoidCyanClient.allTimeAnchorsCharged), String.valueOf(VoidCyanClient.sessionAnchorsCharged)},
            {"Time Played", this.formatTime(VoidCyanClient.allTimePlayTimeTicks), this.formatTime(VoidCyanClient.sessionPlayTimeTicks)}
         };
         int rowY = offY + 28;

         for (String[] row : rows) {
            if (rowY + 12 >= y && rowY <= MinecraftClient.getInstance().currentScreen.height) {
               ctx.drawTextWithShadow(c.textRenderer, Text.literal(row[0]), x + 10, rowY, -1);
               ctx.drawTextWithShadow(c.textRenderer, Text.literal(row[1]), colAllTime, rowY, VoidCyanClient.getPrimaryColor());
               ctx.drawTextWithShadow(c.textRenderer, Text.literal(row[2]), colSession, rowY, -5592406);
            }

            rowY += 16;
         }

         int resetDrawY = rowY + 10;
         if (resetDrawY + 20 >= y && resetDrawY <= MinecraftClient.getInstance().currentScreen.height) {
            ctx.fill(x + 10, resetDrawY, x + 110, resetDrawY + 20, -2130750123);
            ctx.drawTextWithShadow(c.textRenderer, Text.literal("Reset Session"), x + 25, resetDrawY + 6, -1);
         }

         rowY += 40;
         contentHeight = rowY - offY + 20;
         this.maxScroll = Math.max(0, contentHeight - (MinecraftClient.getInstance().currentScreen.height - y));
         ctx.disableScissor();
      }

      @Override
      public boolean mouseClicked(double mx, double my, int btn, int x, int y, int width) {
         int offY = y - this.scrollY;
         int rowsCount = 19;
         int resetDrawY = offY + 28 + rowsCount * 16 + 10;
         if (mx >= x + 10 && mx <= x + 110 && my >= resetDrawY && my <= resetDrawY + 20) {
            VoidCyanClient.sessionKills = 0;
            VoidCyanClient.sessionDeaths = 0;
            VoidCyanClient.sessionCrystalKills = 0;
            VoidCyanClient.sessionCrystalDeaths = 0;
            VoidCyanClient.sessionPops = 0;
            VoidCyanClient.sessionAnchorKills = 0;
            VoidCyanClient.sessionClicks = 0;
            VoidCyanClient.sessionHits = 0;
            VoidCyanClient.sessionDamage = 0.0;
            VoidCyanClient.sessionBlocksBroken = 0;
            VoidCyanClient.sessionBlocksPlaced = 0;
            VoidCyanClient.sessionCrystalsPlaced = 0;
            VoidCyanClient.sessionCrystalsBroken = 0;
            VoidCyanClient.sessionAnchorsPlaced = 0;
            VoidCyanClient.sessionAnchorsBlown = 0;
            VoidCyanClient.sessionAnchorsCharged = 0;
            VoidCyanClient.sessionPlayTimeTicks = 0L;
            VoidCyanClient.saveConfig();
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void mouseScrolled(double mx, double my, double a, int x, int y, int w) {
         this.scrollY -= (int)(a * 20.0);
         if (this.scrollY < 0) {
            this.scrollY = 0;
         }

         if (this.scrollY > this.maxScroll) {
            this.scrollY = this.maxScroll;
         }
      }

      @Override
      public void mouseDragged(double mx, double my, int btn, int x, int y, int w) {
      }

      @Override
      public void mouseReleased(double mx, double my, int btn, int x, int y, int w) {
      }

      @Override
      public void keyPressed(int k, int s, int m) {
      }

      @Override
      public void charTyped(char ch, int m) {
      }
   }
}
