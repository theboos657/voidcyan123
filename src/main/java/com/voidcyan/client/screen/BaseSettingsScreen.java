package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public abstract class BaseSettingsScreen extends Screen {
   protected final Screen parent;
   private final String titleStr;
   private long openTime;
   private final List<BaseSettingsScreen.SettingEntry<?>> entries = new ArrayList<>();
   private int draggingIndex = -1;
   private BaseSettingsScreen.SettingEntry<?> listeningKeybind = null;
   private float scrollOffset = 0.0F;
   private float scrollTarget = 0.0F;

   protected BaseSettingsScreen(Screen parent, String title) {
      super(Text.literal(title));
      this.parent = parent;
      this.titleStr = title;
   }

   protected abstract void buildSettings();

   protected boolean includeAutoToggleKeybind() {
      return true;
   }

   protected void addBoolean(String label, Supplier<Boolean> get, Consumer<Boolean> set) {
      this.entries.add(new BaseSettingsScreen.BoolEntry(label, get, set));
   }

   protected void addSlider(String label, float min, float max, Supplier<Float> get, Consumer<Float> set) {
      this.entries.add(new BaseSettingsScreen.FloatEntry(label, min, max, get, set));
   }

   protected void addIntSlider(String label, int min, int max, Supplier<Integer> get, Consumer<Integer> set) {
      this.entries.add(new BaseSettingsScreen.IntEntry(label, min, max, get, set));
   }

   protected void addIntSlider(String label, int min, int max, Supplier<Integer> get, Consumer<Integer> set, Supplier<Boolean> enabledCondition) {
      this.entries.add(new BaseSettingsScreen.IntEntry(label, min, max, get, set, enabledCondition));
   }

   protected void addEnum(String label, String[] opts, Supplier<Integer> get, Consumer<Integer> set) {
      this.entries.add(new BaseSettingsScreen.EnumEntry(label, opts, get, set));
   }

   protected void addDropdown(String label, Supplier<String[]> optsSupplier, Supplier<String> get, Consumer<String> set) {
      this.entries.add(new BaseSettingsScreen.StringDropdownEntry(label, optsSupplier, get, set));
   }

   protected void addKeybind(String label, Supplier<Integer> get, Consumer<Integer> set) {
      this.entries.add(new BaseSettingsScreen.KeybindEntry(label, get, set));
   }

   protected void addButton(String label, String btnText, Runnable action) {
      this.entries.add(new BaseSettingsScreen.ActionEntry(label, btnText, action));
   }

   protected void addInputField(String label, Supplier<String> get, Consumer<String> set) {
      this.entries.add(new BaseSettingsScreen.InputEntry(label, get, set));
   }

   protected void addMultiSelect(String label, String[] opts, Supplier<boolean[]> get, Consumer<boolean[]> set) {
      this.entries.add(new BaseSettingsScreen.MultiSelectEntry(label, opts, get, set));
   }

   protected void init() {
      this.openTime = System.currentTimeMillis();
      this.scrollOffset = 0.0F;
      this.scrollTarget = 0.0F;
      this.entries.clear();
      this.buildSettings();
      if (this.includeAutoToggleKeybind() && this.titleStr != null && this.titleStr.endsWith(" Settings")) {
         String moduleName = this.titleStr.substring(0, this.titleStr.length() - 9);
         this.addKeybind("Toggle Keybind", () -> VoidCyanClient.getModuleKey(moduleName), val -> VoidCyanClient.setModuleKey(moduleName, val));
      }
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1728053248);
   }

   private void drawModernRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
      context.fill(x, y, x + width, y + height, color);
   }

   private void drawShadow(DrawContext context, int x, int y, int width, int height, int spread, float alpha) {
      for (int i = 1; i <= spread; i++) {
         int currentAlpha = (int)(alpha * 255.0F * (1.0 - (float)i / spread));
         if (currentAlpha > 0) {
            int color = currentAlpha << 24 | 0;
            this.drawModernRoundedRect(context, x - i, y - i, width + i * 2, height + i * 2, 4 + i, color);
         }
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      this.scrollOffset = this.scrollOffset + (this.scrollTarget - this.scrollOffset) * 0.25F;
      float anim = Math.min(1.0F, (float)(System.currentTimeMillis() - this.openTime) / 280.0F);
      anim = 1.0F - (float)Math.pow(1.0F - anim, 3.0);
      int pw = Math.min(350, this.width - 20);
      int bodyH = this.entries.isEmpty() ? 40 : this.entries.stream().mapToInt(e -> e.entryHeight()).sum();
      int ph = Math.min(this.height - 40, 34 + bodyH + 18);
      int x = (this.width - pw) / 2;
      int y = (int)((this.height - ph) / 2 + (1.0F - anim) * 18.0F);
      int cy = VoidCyanClient.getPrimaryColor();
      context.fill(x, y, x + pw, y + ph, -435089135);
      context.fill(x, y, x + pw, y + 3, cy);
      context.fill(x, y + 3, x + 1, y + ph, 1728053247);
      context.fill(x + pw - 1, y + 3, x + pw, y + ph, 1728053247);
      context.fill(x, y + ph - 1, x + pw, y + ph, 1728053247);
      context.drawTextWithShadow(this.textRenderer, Text.literal(this.titleStr), x + 12, y + 10, cy);
      context.fill(x + 10, y + 34 - 2, x + pw - 10, y + 34 - 1, 872415231);
      int bodyTop = y + 34;
      int bodyBottom = y + ph - 18;
      context.enableScissor(x, bodyTop, x + pw, bodyBottom);
      if (this.entries.isEmpty()) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("No settings."), x + 14, bodyTop + 12, -5592406);
      } else {
         int ry = bodyTop - (int)this.scrollOffset;

         for (int i = 0; i < this.entries.size(); i++) {
            BaseSettingsScreen.SettingEntry<?> entry = this.entries.get(i);
            int h = entry.entryHeight();
            if (ry + h >= bodyTop && ry <= bodyBottom) {
               boolean hovered = mouseX >= x && mouseX <= x + pw && mouseY >= ry && mouseY <= ry + h;
               entry.render(context, x + 10, ry, pw - 20, mouseX, mouseY, cy, hovered);
               context.fill(x + 10, ry + h - 1, x + pw - 10, ry + h, 587202559);
            }

            ry += h;
         }
      }

      context.disableScissor();
      context.drawTextWithShadow(this.textRenderer, Text.literal("ESC  to return"), x + 12, y + ph - 13, -1716868438);
      super.render(context, mouseX, mouseY, deltaTicks);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int total = this.entries.stream().mapToInt(e -> e.entryHeight()).sum();
      int pw = Math.min(350, this.width - 20);
      int ph = Math.min(this.height - 40, 34 + total + 18);
      int visible = ph - 34 - 18;
      float max = Math.max(0, total - visible);
      this.scrollTarget = Math.clamp(this.scrollTarget - (float)(verticalAmount * 22.0), 0.0F, max);
      return true;
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      if (click.button() != 0) {
         return super.mouseClicked(click, doubled);
      } else {
         int pw = Math.min(350, this.width - 20);
         int total = this.entries.stream().mapToInt(e -> e.entryHeight()).sum();
         int ph = Math.min(this.height - 40, 34 + total + 18);
         int x = (this.width - pw) / 2;
         int y = (this.height - ph) / 2;
         int bodyTop = y + 34;

         // Safe Mode guard: settings screens of locked modules embed their own
         // enable toggle (e.g. "Indicators Enabled"). Snap it back off if a click
         // just enabled it while Safe Mode is active.
         String moduleName = this.titleStr != null && this.titleStr.endsWith(" Settings")
            ? this.titleStr.substring(0, this.titleStr.length() - 9)
            : null;
         boolean safeModeGuardActive = moduleName != null && VoidCyanClient.isSafeModeBlocked(moduleName);
         boolean wasEnabled = safeModeGuardActive ? this.getModuleEnabledFlag(moduleName) : false;

         if (click.y() >= bodyTop && click.y() <= y + ph - 18) {
            int ry = bodyTop - (int)this.scrollOffset;

            for (int i = 0; i < this.entries.size(); i++) {
               BaseSettingsScreen.SettingEntry<?> entry = this.entries.get(i);
               int h = entry.entryHeight();
               if (entry.click(click.x(), click.y(), x + 10, ry, pw - 20)) {
                  if (entry.isSlider()) {
                     this.draggingIndex = i;
                  }

                  if (safeModeGuardActive && !wasEnabled && this.getModuleEnabledFlag(moduleName)) {
                     this.setModuleEnabledFlag(moduleName, false);
                     VoidCyanClient.showSafeModeBlockedNotification(moduleName);
                     return true;
                  }

                  VoidCyanClient.saveConfig();
                  return true;
               }

               ry += h;
            }
         }

         return super.mouseClicked(click, doubled);
      }
   }

   private boolean getModuleEnabledFlag(String moduleName) {
      return switch (moduleName) {
         case "Health Indicators" -> VoidCyanClient.isHealthIndicatorsEnabled;
         case "Name Tag Items" -> VoidCyanClient.isNameTagItemsEnabled;
         case "Target HUD" -> VoidCyanClient.isTargetHudEnabled;
         default -> false;
      };
   }

   private void setModuleEnabledFlag(String moduleName, boolean value) {
      switch (moduleName) {
         case "Health Indicators" -> VoidCyanClient.isHealthIndicatorsEnabled = value;
         case "Name Tag Items" -> VoidCyanClient.isNameTagItemsEnabled = value;
         case "Target HUD" -> VoidCyanClient.isTargetHudEnabled = value;
         default -> {
         }
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      if (this.draggingIndex < 0) {
         return super.mouseDragged(click, offsetX, offsetY);
      } else {
         int pw = Math.min(350, this.width - 20);
         int total = this.entries.stream().mapToInt(e -> e.entryHeight()).sum();
         int ph = Math.min(this.height - 40, 34 + total + 18);
         int x = (this.width - pw) / 2;
         int y = (this.height - ph) / 2;
         int ry = y + 34 - (int)this.scrollOffset;

         for (int i = 0; i < this.draggingIndex; i++) {
            ry += this.entries.get(i).entryHeight();
         }

         this.entries.get(this.draggingIndex).drag(click.x(), x + 10, pw - 20);
         return true;
      }
   }

   public boolean mouseReleased(Click click) {
      if (this.draggingIndex >= 0) {
         this.draggingIndex = -1;
         VoidCyanClient.saveConfig();
         return true;
      } else {
         return super.mouseReleased(click);
      }
   }

   public boolean keyPressed(KeyInput input) {
      for (int i = 0; i < this.entries.size(); i++) {
         if (this.entries.get(i) instanceof BaseSettingsScreen.InputEntry) {
            BaseSettingsScreen.InputEntry entry = (BaseSettingsScreen.InputEntry)this.entries.get(i);
            if (entry.hasFocus) {
               int key = input.key();
               if (key == 259) {
                  String val = entry.get.get();
                  if (!val.isEmpty()) {
                     entry.set.accept(val.substring(0, val.length() - 1));
                     VoidCyanClient.saveConfig();
                  }

                  return true;
               }

               if (key == 256) {
                  entry.hasFocus = false;
                  return true;
               }

               if (key == 32) {
                  String val = entry.get.get();
                  entry.set.accept(val + " ");
                  VoidCyanClient.saveConfig();
                  return true;
               }

               if (key >= 65 && key <= 90) {
                  char chr = (char)(97 + (key - 65));
                  long window = this.client.getWindow().getHandle();
                  boolean shift = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
                  if (shift) {
                     chr = Character.toUpperCase(chr);
                  }

                  String val = entry.get.get();
                  entry.set.accept(val + chr);
                  VoidCyanClient.saveConfig();
                  return true;
               }

               if (key >= 48 && key <= 57) {
                  long window = this.client.getWindow().getHandle();
                  boolean shift = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
                  char chr;
                  if (shift) {
                     char[] symbols = new char[]{')', '!', '@', '#', '$', '%', '^', '&', '*', '('};
                     chr = symbols[key - 48];
                  } else {
                     chr = (char)(48 + (key - 48));
                  }

                  String val = entry.get.get();
                  entry.set.accept(val + chr);
                  VoidCyanClient.saveConfig();
                  return true;
               }

               if (key == 46) {
                  String val = entry.get.get();
                  entry.set.accept(val + ".");
                  VoidCyanClient.saveConfig();
                  return true;
               }

               if (key == 44) {
                  String val = entry.get.get();
                  entry.set.accept(val + ",");
                  VoidCyanClient.saveConfig();
                  return true;
               }

               if (key == 45) {
                  long window = this.client.getWindow().getHandle();
                  boolean shift = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
                  String val = entry.get.get();
                  entry.set.accept(val + (shift ? "_" : "-"));
                  VoidCyanClient.saveConfig();
                  return true;
               }

               if (key == 59) {
                  long window = this.client.getWindow().getHandle();
                  boolean shift = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
                  String val = entry.get.get();
                  entry.set.accept(val + (shift ? ":" : ";"));
                  VoidCyanClient.saveConfig();
                  return true;
               }

               if (key == 47) {
                  long window = this.client.getWindow().getHandle();
                  boolean shift = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
                  String val = entry.get.get();
                  entry.set.accept(val + (shift ? "?" : "/"));
                  VoidCyanClient.saveConfig();
                  return true;
               }
            }
         }
      }

      if (this.listeningKeybind != null && this.listeningKeybind instanceof BaseSettingsScreen.KeybindEntry) {
         int keyx = input.key();
         if (keyx == 256) {
            ((BaseSettingsScreen.KeybindEntry)this.listeningKeybind).set.accept(-1);
         } else {
            ((BaseSettingsScreen.KeybindEntry)this.listeningKeybind).set.accept(keyx);
         }

         this.listeningKeybind = null;
         VoidCyanClient.saveConfig();
         return true;
      } else if (input.key() == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public void close() {
      VoidCyanClient.saveConfig();
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }

   private static class ActionEntry extends BaseSettingsScreen.SettingEntry<Void> {
      private final String btnText;
      private final Runnable action;

      ActionEntry(String label, String btnText, Runnable action) {
         super(label, null, null);
         this.btnText = btnText;
         this.action = action;
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         if (hovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 13 + 1, -2236963);
         int bw = Math.min(100, tr.getWidth(this.btnText) + 16);
         int bx = x + w - bw;
         int by = y + 9;
         boolean btnHovered = mx >= bx && mx <= bx + bw && my >= by && my <= by + 18;
         ctx.fill(bx, by, bx + bw, by + 18, btnHovered ? -13421773 : -14540254);
         ctx.fill(bx, by, bx + bw, by + 1, cy);
         ctx.fill(bx, by + 17, bx + bw, by + 18, cy);
         ctx.fill(bx, by, bx + 1, by + 18, cy);
         ctx.fill(bx + bw - 1, by, bx + bw, by + 18, cy);
         ctx.drawTextWithShadow(tr, Text.literal(this.btnText), bx + (bw - tr.getWidth(this.btnText)) / 2, by + 5, cy);
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         int bw = Math.min(100, tr.getWidth(this.btnText) + 16);
         int bx = x + w - bw;
         int by = y + 9;
         if (mx >= bx && mx <= bx + bw && my >= by && my <= by + 18) {
            if (this.action != null) {
               this.action.run();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private static class BoolEntry extends BaseSettingsScreen.SettingEntry<Boolean> {
      BoolEntry(String l, Supplier<Boolean> g, Consumer<Boolean> s) {
         super(l, g, s);
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         boolean val = this.get.get();
         if (hovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(this.label), x, y + 13 + 1, -2236963);
         int bx = x + w - 30;
         int by = y + 11;
         ctx.fill(bx, by, bx + 30, by + 14, val ? -14540254 : -15658735);
         int kx = val ? bx + 30 - 14 : bx;
         ctx.fill(kx, by, kx + 14, by + 14, val ? cy : -11184811);
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         int bx = x + w - 30;
         int by = y + 11;
         if (mx >= bx && mx <= bx + 30 && my >= by && my <= by + 14) {
            this.set.accept(!this.get.get());
            return true;
         } else {
            return false;
         }
      }
   }

   private static class EnumEntry extends BaseSettingsScreen.SettingEntry<Integer> {
      final String[] opts;
      boolean expanded = false;

      EnumEntry(String l, String[] opts, Supplier<Integer> g, Consumer<Integer> s) {
         super(l, g, s);
         this.opts = opts;
      }

      @Override
      int entryHeight() {
         return 36 + (this.expanded ? this.opts.length * 20 + 4 : 0);
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         int val = this.get.get();
         String cur = val >= 0 && val < this.opts.length ? this.opts[val] : "?";
         boolean mainHovered = mx >= x - 10 && mx <= x + w + 10 && my >= y && my <= y + 36;
         if (mainHovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 13 + 1, -2236963);
         int bw = Math.min(100, tr.getWidth(cur) + 24);
         int bx = x + w - bw;
         int by = y + 10;
         ctx.fill(bx, by, bx + bw, by + 16, this.expanded ? -13421773 : -14540254);
         ctx.fill(bx, by, bx + bw, by + 1, cy);
         ctx.fill(bx, by + 15, bx + bw, by + 16, cy);
         ctx.drawTextWithShadow(tr, Text.literal(cur), bx + 8, by + 4, cy);
         ctx.drawTextWithShadow(tr, Text.literal(this.expanded ? "▲" : "▼"), bx + bw - 12, by + 4, -5592406);
         if (this.expanded) {
            int optsY = y + 36;
            ctx.fill(x + 10, optsY, x + w - 10, optsY + this.opts.length * 20 + 4, 855638016);
            ctx.fill(x + 10, optsY, x + 11, optsY + this.opts.length * 20 + 4, cy);

            for (int i = 0; i < this.opts.length; i++) {
               int optY = optsY + 2 + i * 20;
               boolean optHovered = mx >= x + 10 && mx <= x + w - 10 && my >= optY && my < optY + 20;
               if (optHovered) {
                  ctx.fill(x + 12, optY, x + w - 10, optY + 20, 587202559);
               }

               int textColor = i == val ? cy : (optHovered ? -1 : -5592406);
               ctx.drawTextWithShadow(tr, Text.literal(this.opts[i]), x + 20, optY + 6, textColor);
            }
         }
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         if (my >= y && my <= y + 36) {
            this.expanded = !this.expanded;
            return true;
         } else {
            if (this.expanded && my > y + 36 && my <= y + 36 + this.opts.length * 20 + 4) {
               int clickedIndex = (int)(my - (y + 36 + 2)) / 20;
               if (clickedIndex >= 0 && clickedIndex < this.opts.length) {
                  this.set.accept(clickedIndex);
                  this.expanded = false;
                  return true;
               }
            }

            return false;
         }
      }
   }

   private static class StringDropdownEntry extends BaseSettingsScreen.SettingEntry<String> {
      final Supplier<String[]> optsSupplier;
      boolean expanded = false;

      StringDropdownEntry(String l, Supplier<String[]> optsSupplier, Supplier<String> g, Consumer<String> s) {
         super(l, g, s);
         this.optsSupplier = optsSupplier;
      }

      @Override
      int entryHeight() {
         String[] opts = this.optsSupplier.get();
         int count = opts != null ? opts.length : 0;
         return 36 + (this.expanded ? count * 20 + 4 : 0);
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         String cur = this.get.get();
         if (cur == null || cur.isEmpty()) cur = "Default Beep";
         String[] opts = this.optsSupplier.get();
         if (opts == null) opts = new String[0];
         boolean mainHovered = mx >= x - 10 && mx <= x + w + 10 && my >= y && my <= y + 36;
         if (mainHovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 13 + 1, -2236963);
         int bw = Math.max(90, Math.min(180, tr.getWidth(cur) + 24));
         int bx = x + w - bw;
         int by = y + 10;
         ctx.fill(bx, by, bx + bw, by + 16, this.expanded ? -13421773 : -14540254);
         ctx.fill(bx, by, bx + bw, by + 1, cy);
         ctx.fill(bx, by + 15, bx + bw, by + 16, cy);
         String truncated = cur;
         if (tr.getWidth(truncated) > bw - 20) {
            while (tr.getWidth(truncated + "...") > bw - 20 && truncated.length() > 1) {
               truncated = truncated.substring(0, truncated.length() - 1);
            }
            truncated = truncated + "...";
         }
         ctx.drawTextWithShadow(tr, Text.literal(truncated), bx + 6, by + 4, cy);
         ctx.drawTextWithShadow(tr, Text.literal(this.expanded ? "▲" : "▼"), bx + bw - 12, by + 4, -5592406);
         if (this.expanded) {
            int optsY = y + 36;
            ctx.fill(x + 10, optsY, x + w - 10, optsY + opts.length * 20 + 4, 855638016);
            ctx.fill(x + 10, optsY, x + 11, optsY + opts.length * 20 + 4, cy);

            for (int i = 0; i < opts.length; i++) {
               int optY = optsY + 2 + i * 20;
               boolean optHovered = mx >= x + 10 && mx <= x + w - 10 && my >= optY && my < optY + 20;
               if (optHovered) {
                  ctx.fill(x + 12, optY, x + w - 10, optY + 20, 587202559);
               }

               int textColor = opts[i].equals(cur) ? cy : (optHovered ? -1 : -5592406);
               ctx.drawTextWithShadow(tr, Text.literal(opts[i]), x + 20, optY + 6, textColor);
            }
         }
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         if (my >= y && my <= y + 36) {
            this.expanded = !this.expanded;
            return true;
         } else {
            String[] opts = this.optsSupplier.get();
            if (opts != null && this.expanded && my > y + 36 && my <= y + 36 + opts.length * 20 + 4) {
               int clickedIndex = (int)(my - (y + 36 + 2)) / 20;
               if (clickedIndex >= 0 && clickedIndex < opts.length) {
                  this.set.accept(opts[clickedIndex]);
                  this.expanded = false;
                  return true;
               }
            }

            return false;
         }
      }
   }

   private static class FloatEntry extends BaseSettingsScreen.SettingEntry<Float> {
      final float min;
      final float max;

      FloatEntry(String l, float min, float max, Supplier<Float> g, Consumer<Float> s) {
         super(l, g, s);
         this.min = min;
         this.max = max;
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         float val = this.get.get();
         if (hovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 6, -2236963);
         ctx.drawTextWithShadow(tr, Text.literal(String.format("%.2f", val)), x + w - 35, y + 6, cy);
         int maxSliderWidth = 120;
         int tx = x + w - maxSliderWidth - 10;
         int ty = y + 36 - 6 - 4;
         int tw = Math.min(w - 100, maxSliderWidth);
         ctx.fill(tx, ty, tx + tw, ty + 6, -13421773);
         float p = (val - this.min) / (this.max - this.min);
         ctx.fill(tx, ty, tx + (int)(p * tw), ty + 6, cy);
         int hx = tx + (int)(p * tw);
         ctx.fill(hx - 4, ty - 4 + 3, hx + 4, ty + 4 + 3, cy);
      }

      @Override
      boolean isSlider() {
         return true;
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         int maxSliderWidth = 120;
         int tx = x + w - maxSliderWidth - 10;
         int ty = y + 36 - 6 - 4;
         if (my >= ty - 6 && my <= ty + 6 + 6 && mx >= tx && mx <= tx + Math.min(w - 100, maxSliderWidth)) {
            this.applyDrag(mx, x, w);
            return true;
         } else {
            return false;
         }
      }

      @Override
      void drag(double mx, int x, int w) {
         this.applyDrag(mx, x, w);
      }

      private void applyDrag(double mx, int x, int w) {
         int maxSliderWidth = 120;
         int tx = x + w - maxSliderWidth - 10;
         float p = (float)Math.clamp((mx - tx) / Math.min(w - 100, maxSliderWidth), 0.0, 1.0);
         this.set.accept(this.min + p * (this.max - this.min));
      }
   }

   private static class InputEntry extends BaseSettingsScreen.SettingEntry<String> {
      private boolean hasFocus = false;
      private long lastCursorBlink = 0L;
      private boolean cursorVisible = true;

      InputEntry(String l, Supplier<String> g, Consumer<String> s) {
         super(l, g, s);
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         String val = this.get.get();
         if (hovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 13 + 1, -2236963);
         int bw = w - 20;
         int by = y + 10;
         ctx.fill(x, by, x + bw, by + 16, this.hasFocus ? -14540254 : -15658735);
         ctx.fill(x, by, x + bw, by + 1, this.hasFocus ? cy : -13421773);
         ctx.fill(x, by + 15, x + bw, by + 16, this.hasFocus ? cy : -13421773);
         ctx.fill(x, by, x + 1, by + 16, this.hasFocus ? cy : -13421773);
         ctx.fill(x + bw - 1, by, x + bw, by + 16, this.hasFocus ? cy : -13421773);
         int textX = x + 4;
         int textY = by + 4;
         int maxWidth = bw - 8;
         String visibleText = val;
         if (tr.getWidth(val) > maxWidth) {
            while (tr.getWidth(visibleText) > maxWidth - 20 && visibleText.length() > 0) {
               visibleText = visibleText.substring(1);
            }

            visibleText = "..." + visibleText;
         }

         ctx.drawTextWithShadow(tr, Text.literal(visibleText), textX, textY, -1);
         if (this.hasFocus) {
            long now = System.currentTimeMillis();
            if (now - this.lastCursorBlink > 500L) {
               this.cursorVisible = !this.cursorVisible;
               this.lastCursorBlink = now;
            }

            if (this.cursorVisible) {
               int cursorX = textX + tr.getWidth(visibleText);
               ctx.fill(cursorX, textY, cursorX + 1, textY + 9, cy);
            }
         }
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         int by = y + 10;
         if (mx >= x && mx <= x + w - 20 && my >= by && my <= by + 16) {
            this.hasFocus = true;
            return true;
         } else {
            this.hasFocus = false;
            return false;
         }
      }

      @Override
      boolean isSlider() {
         return false;
      }
   }

   private static class IntEntry extends BaseSettingsScreen.SettingEntry<Integer> {
      final int min;
      final int max;
      final Supplier<Boolean> enabledCondition;

      IntEntry(String l, int min, int max, Supplier<Integer> g, Consumer<Integer> s) {
         this(l, min, max, g, s, null);
      }

      IntEntry(String l, int min, int max, Supplier<Integer> g, Consumer<Integer> s, Supplier<Boolean> enabledCondition) {
         super(l, g, s);
         this.min = min;
         this.max = max;
         this.enabledCondition = enabledCondition;
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         boolean enabled = this.enabledCondition == null || this.enabledCondition.get();
         int val = this.get.get();
         int alpha = enabled ? 255 : 100;
         if (hovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, alpha << 24 | 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 6, alpha << 24 | -2236963);
         ctx.drawTextWithShadow(tr, Text.literal(String.valueOf(val)), x + w - 25, y + 6, enabled ? cy : -2147483648 | cy & 16777215);
         int maxSliderWidth = 120;
         int tx = x + w - maxSliderWidth - 10;
         int ty = y + 36 - 6 - 4;
         int tw = Math.min(w - 100, maxSliderWidth);
         ctx.fill(tx, ty, tx + tw, ty + 6, alpha << 24 | -13421773);
         float p = (float)(val - this.min) / (this.max - this.min);
         ctx.fill(tx, ty, tx + (int)(p * tw), ty + 6, enabled ? cy : -2147483648 | cy & 16777215);
         int hx = tx + (int)(p * tw);
         ctx.fill(hx - 4, ty - 4 + 3, hx + 4, ty + 4 + 3, enabled ? cy : -2147483648 | cy & 16777215);
      }

      @Override
      boolean isSlider() {
         return true;
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         if (this.enabledCondition != null && !this.enabledCondition.get()) {
            return false;
         } else {
            int maxSliderWidth = 120;
            int tx = x + w - maxSliderWidth - 10;
            int ty = y + 36 - 6 - 4;
            if (my >= ty - 6 && my <= ty + 6 + 6 && mx >= tx && mx <= tx + Math.min(w - 100, maxSliderWidth)) {
               this.applyDrag(mx, x, w);
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      void drag(double mx, int x, int w) {
         this.applyDrag(mx, x, w);
      }

      private void applyDrag(double mx, int x, int w) {
         int maxSliderWidth = 120;
         int tx = x + w - maxSliderWidth - 10;
         float p = (float)Math.clamp((mx - tx) / Math.min(w - 100, maxSliderWidth), 0.0, 1.0);
         this.set.accept(this.min + Math.round(p * (this.max - this.min)));
      }
   }

   private class KeybindEntry extends BaseSettingsScreen.SettingEntry<Integer> {
      KeybindEntry(String l, Supplier<Integer> g, Consumer<Integer> s) {
         super(l, g, s);
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         int val = this.get.get();
         String keyName = this.getKeyName(val);
         if (BaseSettingsScreen.this.listeningKeybind == this) {
            keyName = "> PRESS A KEY <";
         }

         if (hovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 13 + 1, -2236963);
         int bw = Math.min(100, tr.getWidth(keyName) + 16);
         int bx = x + w - bw;
         int by = y + 9;
         boolean btnHovered = mx >= bx && mx <= bx + bw && my >= by && my <= by + 18;
         int bgCol = BaseSettingsScreen.this.listeningKeybind == this ? cy : (btnHovered ? -13421773 : -14540254);
         ctx.fill(bx, by, bx + bw, by + 18, bgCol);
         ctx.fill(bx, by, bx + bw, by + 1, cy);
         ctx.fill(bx, by + 17, bx + bw, by + 18, cy);
         ctx.fill(bx, by, bx + 1, by + 18, cy);
         ctx.fill(bx + bw - 1, by, bx + bw, by + 18, cy);
         int textCol = BaseSettingsScreen.this.listeningKeybind == this ? -1 : cy;
         ctx.drawTextWithShadow(tr, Text.literal(keyName), bx + (bw - tr.getWidth(keyName)) / 2, by + 5, textCol);
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         int val = this.get.get();
         String keyName = BaseSettingsScreen.this.listeningKeybind == this ? "> PRESS A KEY <" : this.getKeyName(val);
         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         int bw = Math.min(100, tr.getWidth(keyName) + 16);
         int bx = x + w - bw;
         int by = y + 9;
         if (mx >= bx && mx <= bx + bw && my >= by && my <= by + 18) {
            if (BaseSettingsScreen.this.listeningKeybind == this) {
               BaseSettingsScreen.this.listeningKeybind = null;
            } else {
               BaseSettingsScreen.this.listeningKeybind = this;
            }

            return true;
         } else {
            return false;
         }
      }

      private String getKeyName(int keyCode) {
         if (keyCode == -1) {
            return "NONE";
         } else {
            String name = GLFW.glfwGetKeyName(keyCode, 0);
            if (name != null) {
               return name.toUpperCase();
            } else {
               switch (keyCode) {
                  case 32:
                     return "SPACE";
                  case 256:
                     return "ESCAPE";
                  case 257:
                     return "ENTER";
                  case 258:
                     return "TAB";
                  case 259:
                     return "BACKSPACE";
                  case 262:
                     return "RIGHT";
                  case 263:
                     return "LEFT";
                  case 264:
                     return "DOWN";
                  case 265:
                     return "UP";
                  case 340:
                     return "LSHIFT";
                  case 341:
                     return "LCTRL";
                  case 342:
                     return "LALT";
                  case 344:
                     return "RSHIFT";
                  case 345:
                     return "RCTRL";
                  case 346:
                     return "RALT";
                  default:
                     return "KEY_" + keyCode;
               }
            }
         }
      }
   }

   private static class MultiSelectEntry extends BaseSettingsScreen.SettingEntry<boolean[]> {
      final String[] opts;
      boolean expanded = false;

      MultiSelectEntry(String l, String[] opts, Supplier<boolean[]> g, Consumer<boolean[]> s) {
         super(l, g, s);
         this.opts = opts;
      }

      @Override
      int entryHeight() {
         return 36 + (this.expanded ? this.opts.length * 20 + 4 : 0);
      }

      @Override
      void render(DrawContext ctx, int x, int y, int w, int mx, int my, int cy, boolean hovered) {
         boolean[] val = this.get.get();
         int count = 0;

         for (boolean b : val) {
            if (b) {
               count++;
            }
         }

         String cur = count + " Selected";
         boolean mainHovered = mx >= x - 10 && mx <= x + w + 10 && my >= y && my <= y + 36;
         if (mainHovered) {
            ctx.fill(x - 10, y, x + w + 10, y + 36, 301989887);
         }

         TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         ctx.drawTextWithShadow(tr, Text.literal(this.label), x, y + 13 + 1, -2236963);
         int bw = Math.min(100, tr.getWidth(cur) + 24);
         int bx = x + w - bw;
         int by = y + 10;
         ctx.fill(bx, by, bx + bw, by + 16, this.expanded ? -13421773 : -14540254);
         ctx.fill(bx, by, bx + bw, by + 1, cy);
         ctx.fill(bx, by + 15, bx + bw, by + 16, cy);
         ctx.drawTextWithShadow(tr, Text.literal(cur), bx + 8, by + 4, cy);
         ctx.drawTextWithShadow(tr, Text.literal(this.expanded ? "▲" : "▼"), bx + bw - 12, by + 4, -5592406);
         if (this.expanded) {
            int optsY = y + 36;
            ctx.fill(x + 10, optsY, x + w - 10, optsY + this.opts.length * 20 + 4, 855638016);
            ctx.fill(x + 10, optsY, x + 11, optsY + this.opts.length * 20 + 4, cy);

            for (int i = 0; i < this.opts.length; i++) {
               int optY = optsY + 2 + i * 20;
               boolean optHovered = mx >= x + 10 && mx <= x + w - 10 && my >= optY && my < optY + 20;
               if (optHovered) {
                  ctx.fill(x + 12, optY, x + w - 10, optY + 20, 587202559);
               }

               boolean selected = val[i];
               int textColor = selected ? cy : (optHovered ? -1 : -5592406);
               ctx.drawTextWithShadow(tr, Text.literal((selected ? "[✓] " : "[ ] ") + this.opts[i]), x + 20, optY + 6, textColor);
            }
         }
      }

      @Override
      boolean click(double mx, double my, int x, int y, int w) {
         if (my >= y && my <= y + 36) {
            this.expanded = !this.expanded;
            return true;
         } else {
            if (this.expanded && my > y + 36 && my <= y + 36 + this.opts.length * 20 + 4) {
               int clickedIndex = (int)(my - (y + 36 + 2)) / 20;
               if (clickedIndex >= 0 && clickedIndex < this.opts.length) {
                  boolean[] val = (boolean[])this.get.get().clone();
                  val[clickedIndex] = !val[clickedIndex];
                  this.set.accept(val);
                  return true;
               }
            }

            return false;
         }
      }
   }

   private abstract static class SettingEntry<T> {
      final String label;
      Supplier<T> get;
      Consumer<T> set;

      SettingEntry(String label, Supplier<T> get, Consumer<T> set) {
         this.label = label;
         this.get = get;
         this.set = set;
      }

      abstract void render(DrawContext var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8);

      abstract boolean click(double var1, double var3, int var5, int var6, int var7);

      int entryHeight() {
         return 36;
      }

      void drag(double mx, int x, int w) {
      }

      boolean isSlider() {
         return false;
      }
   }
}
