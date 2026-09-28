package com.voidcyan.client.screen;

import com.voidcyan.client.OptimizeManager;
import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.CritEffectsManager;
import net.minecraft.util.Util;
import com.voidcyan.client.module.Notification;
import com.voidcyan.client.module.StreakManager;
import com.voidcyan.client.util.GuiScaleManager;
import com.voidcyan.client.util.NoteManager;
import com.voidcyan.client.util.PlayerModelManager;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {
   private final Screen parent;
   private final List<ClickGuiScreen.ModuleInfo> modules = new ArrayList<>();
   private final List<ClickGuiScreen.Friend> friends = new ArrayList<>();
   private final List<ClickGuiScreen.Screenshot> screenshots = new ArrayList<>();
   private final List<ClickGuiScreen.BackgroundFile> backgroundFiles = new ArrayList<>();
   private int currentTab = 0;
   private int targetTab = 0;
   private int scrollOffset = 0;
   private int maxScrollOffset = 0;
   private int targetScrollOffset = 0;
   private boolean isDraggingScrollbar = false;
   private int dragStartY = 0;
   private int dragStartScroll = 0;
   private String searchInput = "";
   private String moduleSearchInput = "";
   private String screenshotSearchInput = "";
   private boolean isModuleSearchFocused = false;
   private boolean isScreenshotSearchFocused = false;
   private boolean isFriendSearchFocused = false;
   private String selectedModuleCategory = "All";
   private static final String[] MODULE_CATEGORIES = new String[]{
      "All", "Combat", "Items", "World", "HUD", "Player"
   };
   private final String[] tabNames = new String[]{
      "Modules", "Screenshots", "Backgrounds", "Settings", "Friends", "Config", "Statistics", "Notes", "Calculator", "Textures", "Player Model"
   };
   private boolean isConfigNameFocused = false;
   private int selectedNoteIndex = -1;
   private boolean isNoteTitleFocused = false;
   private boolean isNoteContentFocused = false;
   private int noteContentScrollOffset = 0;
   private boolean noteJustAdded = false;
   private String configNameInput = "";
   private String namedConfigStatus = "";
   private String calculatorInput = "";
   private String calculatorResult = "";
   private boolean isCalculatorInputFocused = false;
   private Set<Integer> expandedStreaks = new HashSet<>();
   private int textCursor = 0;
   private float backgroundOpacity = VoidCyanClient.guiBgOpacity / 255.0F;
   private int selectedBackgroundIndex = -1;
   private long lastScreenshotClickTime = 0L;
   private int lastClickedScreenshotIndex = -1;
   private int colorPickerR = 0;
   private int colorPickerG = 255;
   private int colorPickerB = 255;
   private ColorPickerModal colorModal;
   private boolean showOverlays = true;
   private boolean showParticles = true;
   private String draggingSlider = null;
   private int sliderContentX = 0;
   private int sliderContentY = 0;
   private int sliderContentW = 0;
   private long openTime = 0L;
   private long lastAnimationUpdateTime = 0L;
   private float sidebarAnimation = 0.0F;
   private float contentAnimation = 0.0F;
   private float[] cardAnimations = new float[0];
   private float tabTransition = 0.0F;
   private int lastModuleCount = 0;
   private int primaryColor = -16711681;
   private int secondaryColor = -16741493;
   private int accentColor = -16720419;
   private boolean sortAZ = false;
   private boolean filterEnabled = false;
   private float azChipHover = 0.0F;
   private float enabledChipHover = 0.0F;
   private float discordHover = 0.0F;
   private float[] catChipHover = new float[0];
   private float profileHover = 0.0F;
   private static final String DISCORD_INVITE = "discord.gg/zUCFVASjxu";
   private int windowX = 0;
   private int windowY = 0;
   private int windowW = 0;
   private int windowH = 0;
   private Map<String, Identifier> screenshotTextures = new HashMap<>();
   private boolean saveOnRelease;
   private Map<String, Identifier> backgroundTextures = new HashMap<>();
   private Map<String, Integer> backgroundDimensions = new HashMap<>();
   private Map<String, NativeImageBackedTexture> backgroundTextureObjects = new HashMap<>();
   private Map<String, AnimatedTexture> previewAnimatedTextures = new HashMap<>();
   private Map<String, Integer> screenshotDimensions = new HashMap<>();
   private String loadedBackgroundPath = "";
   private float lastScaleFactor = 0.0F;
   private float[] tabHoverProgress = new float[this.tabNames.length];
   private volatile boolean clickGuiPmPickerRunning = false;
   private String clickGuiPmStatus = "";

   protected void init() {
      super.init();
      GuiScaleManager.update(this.client);
      this.width = GuiScaleManager.logicalWidth(this.client);
      this.height = GuiScaleManager.logicalHeight(this.client);
   }

   public ClickGuiScreen(Screen parent) {
      super(Text.literal("Void Cyan Click GUI"));
      this.parent = parent;
      this.openTime = System.currentTimeMillis();
      this.initModules();
      this.initFriends();
      this.initScreenshots();
      this.initBackgroundFiles();
      this.backgroundOpacity = VoidCyanClient.guiBgOpacity / 255.0F;
      this.updateThemeColors();
      this.cardAnimations = new float[this.modules.size()];
      this.catChipHover = new float[MODULE_CATEGORIES.length];

      for (int i = 0; i < this.cardAnimations.length; i++) {
         this.cardAnimations[i] = 0.0F;
      }
   }

   private void updateThemeColors() {
      this.primaryColor = 0xFF000000 | VoidCyanClient.primaryColorR << 16 | VoidCyanClient.primaryColorG << 8 | VoidCyanClient.primaryColorB;
      int r = VoidCyanClient.primaryColorR;
      int g = VoidCyanClient.primaryColorG;
      int b = VoidCyanClient.primaryColorB;
      this.secondaryColor = 0xFF000000 | r * 2 / 3 << 16 | g * 2 / 3 << 8 | b * 2 / 3;
      this.accentColor = 0xFF000000 | Math.min(255, r + 30) << 16 | Math.min(255, g + 30) << 8 | Math.min(255, b + 30);
      this.colorPickerR = VoidCyanClient.primaryColorR;
      this.colorPickerG = VoidCyanClient.primaryColorG;
      this.colorPickerB = VoidCyanClient.primaryColorB;
   }

   private void initModules() {
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Inv HUD",
                  () -> VoidCyanClient.isInvHudEnabled = !VoidCyanClient.isInvHudEnabled,
                  VoidCyanClient.isInvHudEnabled,
                  () -> this.client.setScreen(new InvHudSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.invHudX = 10;
                     VoidCyanClient.invHudY = 10;
                     VoidCyanClient.invHudScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows your inventory on the HUD")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "FPS Counter",
                  () -> VoidCyanClient.isFpsCounterEnabled = !VoidCyanClient.isFpsCounterEnabled,
                  VoidCyanClient.isFpsCounterEnabled,
                  () -> this.client.setScreen(new FpsCounterSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.fpsCounterX = 10;
                     VoidCyanClient.fpsCounterY = 35;
                     VoidCyanClient.fpsCounterScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Displays current frames per second")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Coordinates",
                  () -> VoidCyanClient.isCoordinatesEnabled = !VoidCyanClient.isCoordinatesEnabled,
                  VoidCyanClient.isCoordinatesEnabled,
                  () -> this.client.setScreen(new CoordinatesSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.coordinatesX = 10;
                     VoidCyanClient.coordinatesY = 60;
                     VoidCyanClient.coordinatesScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows your XYZ position and facing")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "CPS Counter",
                  () -> VoidCyanClient.isCpsEnabled = !VoidCyanClient.isCpsEnabled,
                  VoidCyanClient.isCpsEnabled,
                  () -> this.client.setScreen(new CpsSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.cpsX = 10;
                     VoidCyanClient.cpsY = 110;
                     VoidCyanClient.cpsScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Tracks left and right clicks per second")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Keystrokes",
                  () -> VoidCyanClient.isKeystrokesEnabled = !VoidCyanClient.isKeystrokesEnabled,
                  VoidCyanClient.isKeystrokesEnabled,
                  () -> this.client.setScreen(new KeystrokesSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.keystrokesX = 10;
                     VoidCyanClient.keystrokesY = 135;
                     VoidCyanClient.keystrokesScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Displays WASD and click key inputs")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Compass",
                  () -> VoidCyanClient.isCompassEnabled = !VoidCyanClient.isCompassEnabled,
                  VoidCyanClient.isCompassEnabled,
                  () -> this.client.setScreen(new CompassSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.compassX = 10;
                     VoidCyanClient.compassY = 165;
                     VoidCyanClient.compassScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows a directional compass on the HUD")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Target HUD",
                  () -> VoidCyanClient.isTargetHudEnabled = !VoidCyanClient.isTargetHudEnabled,
                  VoidCyanClient.isTargetHudEnabled,
                  () -> this.client.setScreen(new TargetHudSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.targetHudX = 150;
                     VoidCyanClient.targetHudY = 150;
                     VoidCyanClient.targetHudScale = 1.0F;
                     VoidCyanClient.targetHudMmoArmorX = 150;
                     VoidCyanClient.targetHudMmoArmorY = 215;
                     VoidCyanClient.targetHudMmoArmorScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Displays targeted player info and health")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Effects",
                  () -> CritEffectsManager.enabled = !CritEffectsManager.enabled,
                  CritEffectsManager.enabled,
                  () -> this.client.setScreen(new EffectsSettingsScreen(this))
               )
               .desc("Crit/fight hit effects — previews in settings")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Stats HUD",
                  () -> VoidCyanClient.isStatsHudEnabled = !VoidCyanClient.isStatsHudEnabled,
                  VoidCyanClient.isStatsHudEnabled,
                  () -> this.client.setScreen(new StatsHudSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.statsHudX = 10;
                     VoidCyanClient.statsHudY = 100;
                     VoidCyanClient.statsHudScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Tracks kills, deaths, pops and more")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Armor Status",
                  () -> VoidCyanClient.isArmorStatusEnabled = !VoidCyanClient.isArmorStatusEnabled,
                  VoidCyanClient.isArmorStatusEnabled,
                  () -> this.client.setScreen(new ArmorStatusSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.armorStatusX = 10;
                     VoidCyanClient.armorStatusY = 195;
                     VoidCyanClient.armorStatusScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows armor durability and icons")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Potion Status",
                  () -> VoidCyanClient.isPotionStatusEnabled = !VoidCyanClient.isPotionStatusEnabled,
                  VoidCyanClient.isPotionStatusEnabled,
                  () -> this.client.setScreen(new PotionStatusSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.potionStatusX = 10;
                     VoidCyanClient.potionStatusY = 230;
                     VoidCyanClient.potionStatusScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Displays active potion effects and timers")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Combo Counter",
                  () -> VoidCyanClient.isComboCounterEnabled = !VoidCyanClient.isComboCounterEnabled,
                  VoidCyanClient.isComboCounterEnabled,
                  () -> this.client.setScreen(new ComboCounterSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.comboCounterX = 10;
                     VoidCyanClient.comboCounterY = 250;
                     VoidCyanClient.comboCounterScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Counts consecutive hits on a target")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Ping Display",
                  () -> VoidCyanClient.isPingDisplayEnabled = !VoidCyanClient.isPingDisplayEnabled,
                  VoidCyanClient.isPingDisplayEnabled,
                  () -> this.client.setScreen(new PingDisplaySettingsScreen(this)),
                  () -> {
                     VoidCyanClient.pingDisplayX = 10;
                     VoidCyanClient.pingDisplayY = 280;
                     VoidCyanClient.pingDisplayScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows your current server ping")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Server Info",
                  () -> VoidCyanClient.isServerInfoEnabled = !VoidCyanClient.isServerInfoEnabled,
                  VoidCyanClient.isServerInfoEnabled,
                  () -> this.client.setScreen(new ServerInfoSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.serverInfoX = 10;
                     VoidCyanClient.serverInfoY = 300;
                     VoidCyanClient.serverInfoScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Displays server address and version")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Keybinds Display",
                  () -> VoidCyanClient.isKeybindsDisplayEnabled = !VoidCyanClient.isKeybindsDisplayEnabled,
                  VoidCyanClient.isKeybindsDisplayEnabled,
                  () -> this.client.setScreen(new KeybindsSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.keybindsHudX = 10;
                     VoidCyanClient.keybindsHudY = 10;
                     VoidCyanClient.keybindsHudScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Lists your active module keybinds")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "FreeLook",
                  () -> VoidCyanClient.isFreelookEnabled = !VoidCyanClient.isFreelookEnabled,
                  VoidCyanClient.isFreelookEnabled,
                  () -> this.client.setScreen(new FreelookSettingsScreen(this))
               )
               .desc("Rotate the camera without moving")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Zoom",
                  () -> VoidCyanClient.isZoomEnabled = !VoidCyanClient.isZoomEnabled,
                  VoidCyanClient.isZoomEnabled,
                  () -> this.client.setScreen(new ZoomSettingsScreen(this))
               )
               .desc("Hold a key to zoom the view in")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Fullbright",
                  () -> VoidCyanClient.isFullbrightEnabled = !VoidCyanClient.isFullbrightEnabled,
                  VoidCyanClient.isFullbrightEnabled,
                  () -> this.client.setScreen(new FullbrightSettingsScreen(this))
               )
               .desc("Removes all darkness from the world")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Big Items",
                  () -> VoidCyanClient.isBigItemsEnabled = !VoidCyanClient.isBigItemsEnabled,
                  VoidCyanClient.isBigItemsEnabled,
                  () -> this.client.setScreen(new BigItemsSettingsScreen(this))
               )
               .desc("Makes selected items on the ground bigger")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Item Physics",
                  () -> VoidCyanClient.isItemPhysicsEnabled = !VoidCyanClient.isItemPhysicsEnabled,
                  VoidCyanClient.isItemPhysicsEnabled,
                  () -> this.client.setScreen(new ItemPhysicsSettingsScreen(this))
               )
               .desc("Dropped items tumble and rotate")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Low Health Alarm",
                  () -> VoidCyanClient.isLowHealthAlarmEnabled = !VoidCyanClient.isLowHealthAlarmEnabled,
                  VoidCyanClient.isLowHealthAlarmEnabled,
                  () -> this.client.setScreen(new LowHealthAlarmSettingsScreen(this))
               )
               .desc("Plays a sound when health is critical")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Pot Warning",
                  () -> VoidCyanClient.isPotWarningEnabled = !VoidCyanClient.isPotWarningEnabled,
                  VoidCyanClient.isPotWarningEnabled,
                  () -> this.client.setScreen(new PotWarningSettingsScreen(this))
               )
               .desc("Warns when pots or effects are low")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Drop Prevention",
                  () -> VoidCyanClient.isDropPreventionEnabled = !VoidCyanClient.isDropPreventionEnabled,
                  VoidCyanClient.isDropPreventionEnabled,
                  () -> this.client.setScreen(new DropPreventionSettingsScreen(this))
               )
               .desc("Blocks accidental item drops")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Hit Color",
                  () -> VoidCyanClient.isHitColorEnabled = !VoidCyanClient.isHitColorEnabled,
                  VoidCyanClient.isHitColorEnabled,
                  () -> this.client.setScreen(new HitColorSettingsScreen(this))
               )
               .desc("Flashes a color on hit entities")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Totem Pop Color",
                  () -> VoidCyanClient.isTotemPopColorEnabled = !VoidCyanClient.isTotemPopColorEnabled,
                  VoidCyanClient.isTotemPopColorEnabled,
                  () -> this.client.setScreen(new TotemPopColorSettingsScreen(this))
               )
               .desc("Highlights players who pop a totem")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Damage Color",
                  () -> VoidCyanClient.isDamageColorEnabled = !VoidCyanClient.isDamageColorEnabled,
                  VoidCyanClient.isDamageColorEnabled,
                  () -> this.client.setScreen(new DamageColorSettingsScreen(this))
               )
               .desc("Tints entities when they take damage")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Custom Hitbox",
                  () -> VoidCyanClient.isCustomHitboxEnabled = !VoidCyanClient.isCustomHitboxEnabled,
                  VoidCyanClient.isCustomHitboxEnabled,
                  () -> this.client.setScreen(new CustomHitboxSettingsScreen(this))
               )
               .desc("Draws colored outlines around players")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Damage Hearts",
                  () -> VoidCyanClient.isDamageHeartsEnabled = !VoidCyanClient.isDamageHeartsEnabled,
                  VoidCyanClient.isDamageHeartsEnabled,
                  () -> this.client.setScreen(new DamageHeartsSettingsScreen(this))
               )
               .desc("Shows floating hearts for damage dealt")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Health Indicators",
                  () -> VoidCyanClient.isHealthIndicatorsEnabled = !VoidCyanClient.isHealthIndicatorsEnabled,
                  VoidCyanClient.isHealthIndicatorsEnabled,
                  () -> this.client.setScreen(new HealthIndicatorsSettingsScreen(this))
               )
               .desc("Renders health bars above entities")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Custom Crosshair",
                  () -> VoidCyanClient.isCustomCrosshairEnabled = !VoidCyanClient.isCustomCrosshairEnabled,
                  VoidCyanClient.isCustomCrosshairEnabled,
                  () -> this.client.setScreen(new CustomCrosshairSettingsScreen(this))
               )
               .desc("Replaces the default crosshair style")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Waypoints",
                  () -> VoidCyanClient.isWaypointsEnabled = !VoidCyanClient.isWaypointsEnabled,
                  VoidCyanClient.isWaypointsEnabled,
                  () -> this.client.setScreen(new WaypointsScreen(this))
               )
               .desc("Place and display world waypoints")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Attack Indicator",
                  () -> VoidCyanClient.attackIndicatorEnabled = !VoidCyanClient.attackIndicatorEnabled,
                  VoidCyanClient.attackIndicatorEnabled,
                  () -> this.client.setScreen(new TargetIndicatorSettingsScreen(this))
               )
               .desc("Visual effect on your attack target")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Custom F3",
                  () -> VoidCyanClient.isCustomF3Enabled = !VoidCyanClient.isCustomF3Enabled,
                  VoidCyanClient.isCustomF3Enabled,
                  () -> this.client.setScreen(new CustomF3SettingsScreen(this))
               )
               .desc("Customises which F3 lines are shown")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Totem Trace",
                  () -> VoidCyanClient.isTotemTraceEnabled = !VoidCyanClient.isTotemTraceEnabled,
                  VoidCyanClient.isTotemTraceEnabled,
                  () -> this.client.setScreen(new TotemTraceSettingsScreen(this))
               )
               .desc("Draws a line to nearby totems")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Name Tag Items",
                  () -> VoidCyanClient.isNameTagItemsEnabled = !VoidCyanClient.isNameTagItemsEnabled,
                  VoidCyanClient.isNameTagItemsEnabled,
                  () -> this.client.setScreen(new NameTagItemsSettingsScreen(this))
               )
               .desc("Shows items, offhand, armor, and totem pops above player nametags")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Nick Hider",
                  () -> VoidCyanClient.isNickHiderEnabled = !VoidCyanClient.isNickHiderEnabled,
                  VoidCyanClient.isNickHiderEnabled,
                  () -> this.client.setScreen(new NickHiderSettingsScreen(this))
               )
               .desc("Hides your username from the tab list")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Peer Nick",
                  () -> VoidCyanClient.isPeerNickEnabled = !VoidCyanClient.isPeerNickEnabled,
                  VoidCyanClient.isPeerNickEnabled,
                  () -> this.client.setScreen(new NickHiderSettingsScreen(this))
               )
               .desc("Assigns nicknames to other players")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Notifications",
                  () -> VoidCyanClient.isNotificationsEnabled = !VoidCyanClient.isNotificationsEnabled,
                  VoidCyanClient.isNotificationsEnabled,
                  () -> this.client.setScreen(new NotificationsSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.notificationsX = 10;
                     VoidCyanClient.notificationsY = 100;
                     VoidCyanClient.notificationsScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Toast pop-ups for module events")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Saturation",
                  () -> VoidCyanClient.isAppleSkinEnabled = !VoidCyanClient.isAppleSkinEnabled,
                  VoidCyanClient.isAppleSkinEnabled,
                  () -> this.client.setScreen(new OverlaysSettingsScreen(this))
               )
               .desc("Shows food saturation on the HUD")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Player Trail",
                  () -> VoidCyanClient.isPlayerTrailEnabled = !VoidCyanClient.isPlayerTrailEnabled,
                  VoidCyanClient.isPlayerTrailEnabled,
                  () -> this.client.setScreen(new PlayerTrailSettingsScreen(this))
               )
               .desc("Leaves a fading trail behind you")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Stopwatch",
                  () -> VoidCyanClient.isStopwatchEnabled = !VoidCyanClient.isStopwatchEnabled,
                  VoidCyanClient.isStopwatchEnabled,
                  () -> this.client.setScreen(new StopwatchSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.stopwatchX = 10;
                     VoidCyanClient.stopwatchY = 50;
                     VoidCyanClient.stopwatchScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("In-game stopwatch with HUD display")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "TPS Display",
                  () -> VoidCyanClient.isTpsDisplayEnabled = !VoidCyanClient.isTpsDisplayEnabled,
                  VoidCyanClient.isTpsDisplayEnabled,
                  () -> this.client.setScreen(new TpsDisplaySettingsScreen(this)),
                  () -> {
                     VoidCyanClient.tpsDisplayX = 10;
                     VoidCyanClient.tpsDisplayY = 400;
                     VoidCyanClient.tpsDisplayScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows server ticks per second")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Watermark",
                  () -> VoidCyanClient.isWatermarkEnabled = !VoidCyanClient.isWatermarkEnabled,
                  VoidCyanClient.isWatermarkEnabled,
                  () -> this.client.setScreen(new WatermarkSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.watermarkX = 10;
                     VoidCyanClient.watermarkY = 10;
                     VoidCyanClient.watermarkScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Displays the VoidCyan logo on screen")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "ArrayList",
                  () -> VoidCyanClient.isArrayListEnabled = !VoidCyanClient.isArrayListEnabled,
                  VoidCyanClient.isArrayListEnabled,
                  () -> this.client.setScreen(new ArrayListSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.arrayListX = 50;
                     VoidCyanClient.arrayListY = 10;
                     VoidCyanClient.arrayListScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Lists all currently enabled modules")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "FPS Graph",
                  () -> VoidCyanClient.isFpsGraphEnabled = !VoidCyanClient.isFpsGraphEnabled,
                  VoidCyanClient.isFpsGraphEnabled,
                  () -> this.client.setScreen(new FpsGraphSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.fpsGraphX = 10;
                     VoidCyanClient.fpsGraphY = 50;
                     VoidCyanClient.fpsGraphScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Real-time graph of frame rate history")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Speed Display",
                  () -> VoidCyanClient.isSpeedDisplayEnabled = !VoidCyanClient.isSpeedDisplayEnabled,
                  VoidCyanClient.isSpeedDisplayEnabled,
                  () -> this.client.setScreen(new SpeedDisplaySettingsScreen(this)),
                  () -> {
                     VoidCyanClient.speedDisplayX = 10;
                     VoidCyanClient.speedDisplayY = 320;
                     VoidCyanClient.speedDisplayScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows your current movement speed")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Biome Display",
                  () -> VoidCyanClient.isBiomeDisplayEnabled = !VoidCyanClient.isBiomeDisplayEnabled,
                  VoidCyanClient.isBiomeDisplayEnabled,
                  () -> this.client.setScreen(new BiomeDisplaySettingsScreen(this)),
                  () -> {
                     VoidCyanClient.biomeDisplayX = 10;
                     VoidCyanClient.biomeDisplayY = 340;
                     VoidCyanClient.biomeDisplayScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows the biome you are standing in")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Entity Counter",
                  () -> VoidCyanClient.isEntityCounterEnabled = !VoidCyanClient.isEntityCounterEnabled,
                  VoidCyanClient.isEntityCounterEnabled,
                  () -> this.client.setScreen(new EntityCounterSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.entityCounterX = 10;
                     VoidCyanClient.entityCounterY = 360;
                     VoidCyanClient.entityCounterScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Counts loaded entities around you")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "IRL Clock",
                  () -> VoidCyanClient.isIrlClockEnabled = !VoidCyanClient.isIrlClockEnabled,
                  VoidCyanClient.isIrlClockEnabled,
                  () -> this.client.setScreen(new IrlClockSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.irlClockX = 10;
                     VoidCyanClient.irlClockY = 360;
                     VoidCyanClient.irlClockScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows your real-world time in-game")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Totem Counter",
                  () -> VoidCyanClient.isTotemCounterEnabled = !VoidCyanClient.isTotemCounterEnabled,
                  VoidCyanClient.isTotemCounterEnabled,
                  () -> this.client.setScreen(new TotemCounterSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.totemCounterX = 10;
                     VoidCyanClient.totemCounterY = 380;
                     VoidCyanClient.totemCounterScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Tracks totems of undying in inventory")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Arrow Counter",
                  () -> VoidCyanClient.isArrowCounterEnabled = !VoidCyanClient.isArrowCounterEnabled,
                  VoidCyanClient.isArrowCounterEnabled,
                  () -> this.client.setScreen(new ArrowCounterSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.arrowCounterX = 10;
                     VoidCyanClient.arrowCounterY = 400;
                     VoidCyanClient.arrowCounterScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Counts arrows in your inventory")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Reach Display",
                  () -> VoidCyanClient.isReachDisplayEnabled = !VoidCyanClient.isReachDisplayEnabled,
                  VoidCyanClient.isReachDisplayEnabled,
                  () -> this.client.setScreen(new ReachDisplaySettingsScreen(this)),
                  () -> {
                     VoidCyanClient.reachDisplayX = 10;
                     VoidCyanClient.reachDisplayY = 420;
                     VoidCyanClient.reachDisplayScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows the distance of your last hit")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Pack Display",
                  () -> VoidCyanClient.isPackDisplayEnabled = !VoidCyanClient.isPackDisplayEnabled,
                  VoidCyanClient.isPackDisplayEnabled,
                  () -> this.client.setScreen(new PackDisplaySettingsScreen(this)),
                  () -> {
                     VoidCyanClient.packDisplayX = 10;
                     VoidCyanClient.packDisplayY = 310;
                     VoidCyanClient.packDisplayScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows active resource pack names")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Block Info HUD",
                  () -> VoidCyanClient.isBlockIndicatorEnabled = !VoidCyanClient.isBlockIndicatorEnabled,
                  VoidCyanClient.isBlockIndicatorEnabled,
                  () -> this.client.setScreen(new BlockIndicatorSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.blockIndicatorX = 10;
                     VoidCyanClient.blockIndicatorY = 340;
                     VoidCyanClient.blockIndicatorScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Info about the block you are looking at")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Death Info",
                  () -> VoidCyanClient.isDeathInfoEnabled = !VoidCyanClient.isDeathInfoEnabled,
                  VoidCyanClient.isDeathInfoEnabled,
                  () -> this.client.setScreen(new DeathInfoSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.deathInfoX = 10;
                     VoidCyanClient.deathInfoY = 370;
                     VoidCyanClient.deathInfoScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Records cause and location of death")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "System Resources",
                  () -> VoidCyanClient.isSystemResourcesEnabled = !VoidCyanClient.isSystemResourcesEnabled,
                  VoidCyanClient.isSystemResourcesEnabled,
                  () -> this.client.setScreen(new SystemResourcesSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.systemResourcesX = 10;
                     VoidCyanClient.systemResourcesY = 50;
                     VoidCyanClient.systemResourcesScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Shows CPU and RAM usage on the HUD")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Toggle Sprint",
                  () -> VoidCyanClient.isToggleSprintEnabled = !VoidCyanClient.isToggleSprintEnabled,
                  VoidCyanClient.isToggleSprintEnabled,
                  () -> this.client.setScreen(new ToggleSprintSettingsScreen(this))
               )
               .desc("Hold sprint key to sprint indefinitely")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Toggle Sneak",
                  () -> VoidCyanClient.isToggleSneakEnabled = !VoidCyanClient.isToggleSneakEnabled,
                  VoidCyanClient.isToggleSneakEnabled,
                  () -> this.client.setScreen(new ToggleSneakSettingsScreen(this))
               )
               .desc("Press sneak once to stay crouched")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Time Changer",
                  () -> VoidCyanClient.isTimeChangerEnabled = !VoidCyanClient.isTimeChangerEnabled,
                  VoidCyanClient.isTimeChangerEnabled,
                  () -> this.client.setScreen(new TimeChangerSettingsScreen(this))
               )
               .desc("Changes the visual sky time client-side")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "TNT Timer",
                  () -> VoidCyanClient.isTntTimerEnabled = !VoidCyanClient.isTntTimerEnabled,
                  VoidCyanClient.isTntTimerEnabled,
                  () -> this.client.setScreen(new TntTimerSettingsScreen(this))
               )
               .desc("Counts down time until TNT explodes")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Custom Text",
                  () -> VoidCyanClient.isTextHudEnabled = !VoidCyanClient.isTextHudEnabled,
                  VoidCyanClient.isTextHudEnabled,
                  () -> this.client.setScreen(new TextHudSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.textHudX = 10;
                     VoidCyanClient.textHudY = 10;
                     VoidCyanClient.textHudScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Displays a custom text string on the HUD")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Big Head",
                  () -> VoidCyanClient.isBigHeadEnabled = !VoidCyanClient.isBigHeadEnabled,
                  VoidCyanClient.isBigHeadEnabled,
                  () -> this.client.setScreen(new BigHeadSettingsScreen(this))
               )
               .desc("Enlarges player head models")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Item Animations",
                  () -> VoidCyanClient.isItemAnimationsEnabled = !VoidCyanClient.isItemAnimationsEnabled,
                  VoidCyanClient.isItemAnimationsEnabled,
                  () -> this.client.setScreen(new ItemAnimationsSettingsScreen(this))
               )
               .desc("Old-style item swing animations")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "View Model",
                  () -> VoidCyanClient.isViewModelEnabled = !VoidCyanClient.isViewModelEnabled,
                  VoidCyanClient.isViewModelEnabled,
                  () -> this.client.setScreen(new ViewModelSettingsScreen(this))
               )
               .desc("Adjusts hand and item view model")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Block Overlay",
                  () -> VoidCyanClient.isBlockOverlayEnabled = !VoidCyanClient.isBlockOverlayEnabled,
                  VoidCyanClient.isBlockOverlayEnabled,
                  () -> this.client.setScreen(new BlockOverlaySettingsScreen(this))
               )
               .desc("Custom outline, fill and glow for the targeted block")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "China Hat",
                  () -> VoidCyanClient.isChinaHatEnabled = !VoidCyanClient.isChinaHatEnabled,
                  VoidCyanClient.isChinaHatEnabled,
                  () -> this.client.setScreen(new ChinaHatSettingsScreen(this))
               )
               .desc("Renders a conical hat on players")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Inv Highlight",
                  () -> VoidCyanClient.isInvHighlightEnabled = !VoidCyanClient.isInvHighlightEnabled,
                  VoidCyanClient.isInvHighlightEnabled,
                  () -> this.client.setScreen(new InvHighlightSettingsScreen(this))
               )
               .desc("Highlights important items in inventory")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Mouse Strokes",
                  () -> VoidCyanClient.isMouseStrokesEnabled = !VoidCyanClient.isMouseStrokesEnabled,
                  VoidCyanClient.isMouseStrokesEnabled,
                  () -> this.client.setScreen(new MouseStrokesSettingsScreen(this)),
                  () -> {
                     VoidCyanClient.mouseStrokesX = 100;
                     VoidCyanClient.mouseStrokesY = 100;
                     VoidCyanClient.mouseStrokesScale = 1.0F;
                     VoidCyanClient.markConfigDirty();
                  }
               )
               .desc("Visualises left and right mouse clicks")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Totem Pop Notifier",
                  () -> VoidCyanClient.isTotemPopNotifierEnabled = !VoidCyanClient.isTotemPopNotifierEnabled,
                  VoidCyanClient.isTotemPopNotifierEnabled
               )
               .desc("Plays a sound when a totem is used")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Logout Spots",
                  () -> VoidCyanClient.isLogoutSpotsEnabled = !VoidCyanClient.isLogoutSpotsEnabled,
                  VoidCyanClient.isLogoutSpotsEnabled,
                  () -> this.client.setScreen(new LogoutSpotsSettingsScreen(this))
               )
               .desc("Marks where players disconnected")
         );
      this.modules
         .add(
            new ClickGuiScreen.ModuleInfo(
                  "Transparent Shield",
                  () -> VoidCyanClient.isTransparentShieldEnabled = !VoidCyanClient.isTransparentShieldEnabled,
                  VoidCyanClient.isTransparentShieldEnabled
               )
               .desc("Makes the held shield transparent")
         );
   }

   private void initFriends() {
      this.friends.clear();

      for (String f : VoidCyanClient.friends) {
         this.friends.add(new ClickGuiScreen.Friend(f));
      }
   }

   private void initScreenshots() {
      File screenshotsDir = new File("screenshots");
      if (screenshotsDir.exists() && screenshotsDir.isDirectory()) {
         File[] files = screenshotsDir.listFiles();
         if (files != null) {
            for (File f : files) {
               if (f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".mp4")) {
                  this.screenshots.add(new ClickGuiScreen.Screenshot(f.getName()));
               }
            }
         }
      }
   }

   private void initBackgroundFiles() {
      this.backgroundFiles.clear();
      File dir = new File(MinecraftClient.getInstance().runDirectory, "background");
      if (!dir.exists()) {
         dir.mkdirs();
      }

      if (dir.exists() && dir.isDirectory()) {
         File[] files = dir.listFiles();
         if (files != null) {
            for (File f : files) {
               if (f.isDirectory() || f.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg|mp4)$")) {
                  this.backgroundFiles.add(new ClickGuiScreen.BackgroundFile(f.getName(), f));
               }
            }
         }
      }
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (VoidCyanClient.clickGuiBgEnabled && VoidCyanClient.guiBackgroundImagePath != null && !VoidCyanClient.guiBackgroundImagePath.isEmpty()) {
         float scaleFactor = this.client != null ? this.client.getWindow().getScaleFactor() : 1.0F;
         if (!VoidCyanClient.guiBackgroundImagePath.equals(this.loadedBackgroundPath) || Math.abs(scaleFactor - this.lastScaleFactor) > 0.01F) {
            VoidCyanClient.updateMainBackground();
            this.loadedBackgroundPath = VoidCyanClient.guiBackgroundImagePath;
            this.lastScaleFactor = scaleFactor;
         }

         if (VoidCyanClient.mainBackground != null) {
            Identifier bgId = VoidCyanClient.mainBackground.updateAndGetId();
            if (bgId != null) {
               int opacityInt = VoidCyanClient.guiBgOpacity;
               int color = opacityInt << 24 | 16777215;
               context.drawTexture(RenderPipelines.GUI_TEXTURED, bgId, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height, color);
            }
         }
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      GuiScaleManager.update(this.client);
      // Recompute the logical viewport every frame so slider/video-setting scale changes
      // resize the GUI live, without waiting for a window-resize init pass.
      this.width = GuiScaleManager.logicalWidth(this.client);
      this.height = GuiScaleManager.logicalHeight(this.client);
      // Vanilla screen coords already have the game GUI Scale baked in; only the
      // dedicated Click GUI multiplier remains as the render transform.
      float scaleRatio = GuiScaleManager.multiplier();
      context.getMatrices().pushMatrix();
      context.getMatrices().scale(scaleRatio, scaleRatio);
      double mx = GuiScaleManager.toLogical((double)mouseX);
      double my = GuiScaleManager.toLogical((double)mouseY);
      this.updateAnimations(deltaTicks);
      float openProgress = Math.min(1.0F, (float)(System.currentTimeMillis() - this.openTime) / Math.max(1.0F, (float)VoidCyanClient.guiAnimationDurationMs));
      openProgress = this.easeOutCubic(openProgress);

      // Floating particles behind the window.
      if (this.showParticles) {
         this.renderFloatingParticles(context);
      }

      int primary = VoidCyanClient.getPrimaryColor();
      float grow = Math.min(1.0F, 0.55F + openProgress * 0.45F);
      int winW = (int)((this.width - GuiStyle.WINDOW_MARGIN * 2) * grow);
      int winH = (int)((this.height - GuiStyle.WINDOW_MARGIN * 2) * grow);
      int winX = (this.width - winW) / 2;
      int winY = (this.height - winH) / 2;
      this.windowX = winX;
      this.windowY = winY;
      this.windowW = winW;
      this.windowH = winH;
      GuiStyle.drawWindow(context, winX, winY, winW, winH, primary, openProgress);
      context.enableScissor(winX + 1, winY + 1, winX + winW - 1, winY + winH - 1);

      int mix = (int)mx;
      int miy = (int)my;
      this.renderAnimatedSidebar(context, mix, miy, openProgress, winX, winY, winW, winH);
      this.renderAnimatedContent(context, mix, miy, openProgress, winX, winY, winW, winH);
      context.disableScissor();      // Color picker modal draws above the window content, in logical space.
      if (this.colorModal != null) {
         if (!this.colorModal.isOpen()) {
            this.colorModal = null;
         } else {
            this.colorModal.render(
               context, (int)GuiScaleManager.toLogical((double)mouseX), (int)GuiScaleManager.toLogical((double)mouseY), this.width, this.height
            );
         }
      }

      context.getMatrices().popMatrix();
   }

   private void updateAnimations(float delta) {
      long now = System.currentTimeMillis();
      long elapsedMs = this.lastAnimationUpdateTime == 0L ? 16L : Math.max(1L, now - this.lastAnimationUpdateTime);
      this.lastAnimationUpdateTime = now;
      if (VoidCyanClient.rgbChromaEnabled || "Rainbow".equalsIgnoreCase(VoidCyanClient.colorTheme)) {
         this.primaryColor = VoidCyanClient.getPrimaryColor();
         this.secondaryColor = VoidCyanClient.getSecondaryColor();
      }
      float durationMs = Math.max(50.0F, (float)VoidCyanClient.guiAnimationDurationMs);
      float animationSpeed = 1.0F - (float)Math.exp(-4.6F * (float)elapsedMs / durationMs);
      this.sidebarAnimation = this.sidebarAnimation + (1.0F - this.sidebarAnimation) * animationSpeed;
      this.contentAnimation = this.contentAnimation + (1.0F - this.contentAnimation) * animationSpeed * 0.8F;
      if (this.currentTab != this.targetTab) {
         this.tabTransition += animationSpeed * 2.0F;
         if (this.tabTransition >= 1.0F) {
            this.currentTab = this.targetTab;
            this.tabTransition = 0.0F;
            this.scrollOffset = 0;
            this.targetScrollOffset = 0;
         }
      }

      if (this.scrollOffset != this.targetScrollOffset) {
         float diff = this.targetScrollOffset - this.scrollOffset;
         this.scrollOffset += (int)(diff * animationSpeed * 3.0F);
         if (Math.abs(diff) < 2.0F) {
            this.scrollOffset = this.targetScrollOffset;
         }
      }

      if (this.modules.size() != this.lastModuleCount) {
         this.cardAnimations = new float[this.modules.size()];
         this.lastModuleCount = this.modules.size();
      }

      for (int i = 0; i < this.cardAnimations.length && i < this.modules.size(); i++) {
         ClickGuiScreen.ModuleInfo module = this.modules.get(i);
         this.cardAnimations[i] = this.cardAnimations[i] + (1.0F - this.cardAnimations[i]) * animationSpeed;
         if (module.enabled) {
            module.animationProgress += animationSpeed * 4.0F;
            if (module.animationProgress > 1.0F) {
               module.animationProgress = 1.0F;
            }
         } else {
            module.animationProgress -= animationSpeed * 4.0F;
            if (module.animationProgress < 0.0F) {
               module.animationProgress = 0.0F;
            }
         }
      }

      for (ClickGuiScreen.Friend friend : this.friends) {
         friend.animationProgress = friend.animationProgress + (1.0F - friend.animationProgress) * animationSpeed;
      }

      for (ClickGuiScreen.Screenshot screenshot : this.screenshots) {
         screenshot.animationProgress = screenshot.animationProgress + (1.0F - screenshot.animationProgress) * animationSpeed;
      }
   }

   private float easeOutCubic(float t) {
      return 1.0F - (float)Math.pow(1.0F - t, 3.0);
   }

   private int getSidebarTabStartY() {
      return 52;
   }

   private int getSidebarTabSpacing(int sidebarHeight) {
      int avail = sidebarHeight - this.getSidebarTabStartY() - 86;
      int calculated = avail / this.tabNames.length;
      return Math.max(16, Math.min(26, calculated));
   }

   private int getSidebarTabHeight(int sidebarHeight) {
      return Math.max(13, this.getSidebarTabSpacing(sidebarHeight) - 3);
   }

   private void renderAnimatedSidebar(DrawContext context, int mouseX, int mouseY, float openProgress, int winX, int winY, int winW, int winH) {
      int primary = VoidCyanClient.getPrimaryColor();
      float anim = this.contentAnimation * openProgress;
      if (anim <= 0.01F) return;
      int a255 = (int)(anim * 255.0F);
      int sidebarW = GuiStyle.SIDEBAR_W;
      int sbX = winX;
      int sbY = winY;
      int sbH = winH;

      // Sidebar background: darker well + vertical divider tinted by theme.
      GuiStyle.roundedRect(context, sbX, sbY, sidebarW, sbH, GuiStyle.WINDOW_RADIUS, (int)(anim * 235.0F) << 24 | 0x0A0812);
      context.fill(sbX + sidebarW - 1, sbY + 8, sbX + sidebarW, sbY + sbH - 8, (int)(anim * 70.0F) << 24 | (primary & 0x00FFFFFF));

      // ---- Brand row: mod icon + name + version chip.
      int logoSize = 24;
      int brandY = sbY + 12;
      GuiStyle.drawLogo(context, sbX + 12, brandY, logoSize, primary, anim);
      int nameCol = (int)(a255) << 24 | 0x00FFFFFF;
      GuiStyle.text(context, this.textRenderer, "VoidCyan", sbX + 12 + logoSize + 8, brandY + 3, nameCol);
      String verChip = "1.0.0";
      int chipW = this.textRenderer.getWidth(verChip) + 10;
      int chipH = 13;
      int chipX = sbX + 12 + logoSize + 8 + this.textRenderer.getWidth("VoidCyan") + 6;
      int chipY = brandY + 6;
      GuiStyle.roundedBordered(
         context, chipX, chipY, chipW, chipH, 4,
         (int)(anim * 0.18F) << 24 | (primary & 0x00FFFFFF),
         (int)(anim * 0.5F) << 24 | (primary & 0x00FFFFFF)
      );
      GuiStyle.text(context, this.textRenderer, verChip, chipX + 5, chipY + 3, (int)(anim * 200.0F) << 24 | (primary & 0x00FFFFFF));

      // ---- Tab rows.
      int tabStartY = sbY + this.getSidebarTabStartY();
      int tabSpacing = this.getSidebarTabSpacing(sbH);
      int tabH = this.getSidebarTabHeight(sbH);
      int tabX = sbX + 10;
      int tabW = sidebarW - 20;
      int tabY = tabStartY;
      for (int i = 0; i < this.tabNames.length; i++) {
         boolean isHovered = GuiStyle.inRect(mouseX, mouseY, tabX, tabY, tabW, tabH);
         if (isHovered) {
            this.tabHoverProgress[i] = Math.min(1.0F, this.tabHoverProgress[i] + 0.1F);
         } else {
            this.tabHoverProgress[i] = Math.max(0.0F, this.tabHoverProgress[i] - 0.1F);
         }

         boolean isActive = i == this.targetTab || i == this.currentTab;
         if (!isActive) {
            if (this.tabHoverProgress[i] > 0.01F) {
               int bgCol = (int)(26.0F * this.tabHoverProgress[i] * anim) << 24 | 0x00FFFFFF;
               GuiStyle.roundedRect(context, tabX, tabY, tabW, tabH, 8, bgCol);
            }

            int txtA = (int)((0.5F + this.tabHoverProgress[i] * 0.45F) * anim * 255.0F);
            int iconY = tabY + (tabH - 12) / 2;
            GuiStyle.sidebarIcon(context, i, tabX + 9, iconY, 12, txtA << 24 | 0x00FFFFFF);
            GuiStyle.text(context, this.textRenderer, this.tabNames[i], tabX + 28, tabY + (tabH - 8) / 2, txtA << 24 | 0x00FFFFFF);
         } else {
            int bgCol = (int)(54.0F * anim) << 24 | (primary & 0x00FFFFFF);
            GuiStyle.roundedRect(context, tabX, tabY, tabW, tabH, 8, bgCol);
            GuiStyle.roundedOutline(context, tabX, tabY, tabW, tabH, 8, (int)(anim * 150.0F) << 24 | (primary & 0x00FFFFFF));
            int indH = Math.min(tabH - 8, 16);
            GuiStyle.roundedRect(context, tabX - 6, tabY + (tabH - indH) / 2, 3, indH, 2, (int)(a255) << 24 | (primary & 0x00FFFFFF));
            int iconY = tabY + (tabH - 12) / 2;
            GuiStyle.sidebarIcon(context, i, tabX + 9, iconY, 12, (int)(a255) << 24 | (primary & 0x00FFFFFF));
            GuiStyle.text(context, this.textRenderer, this.tabNames[i], tabX + 28, tabY + (tabH - 8) / 2, (int)(a255) << 24 | (primary & 0x00FFFFFF));
         }

         tabY += tabSpacing;
      }

      // ---- Profile card (3D player, name, version, discord).
      int cardH = 72;
      int cardY = sbY + sbH - cardH - 12;
      int cardX = sbX + 10;
      int cardW = sidebarW - 20;
      boolean profHover = GuiStyle.inRect(mouseX, mouseY, cardX, cardY, cardW, cardH);
      this.profileHover = profHover ? Math.min(1.0F, this.profileHover + 0.1F) : Math.max(0.0F, this.profileHover - 0.1F);
      int pCardBg = (int)(anim * (140 + this.profileHover * 40.0F)) << 24 | 0x171122;
      int pCardBorder = (int)(anim * (60 + this.profileHover * 80.0F)) << 24 | (primary & 0x00FFFFFF);
      GuiStyle.roundedBordered(context, cardX, cardY, cardW, cardH, 6, pCardBg, pCardBorder);

      // Use the same vanilla entity renderer as the inventory screen instead of a flat skin crop.
      if (this.client != null && this.client.player != null) {
         int entityX = cardX + 5;
         int entityY = cardY + 3;
         // Static 3D model: passing the region center as the mouse position cancels rotation,
         // so the skin stands upright facing the viewer (no cursor tracking, no name tag).
         InventoryScreen.drawEntity(
            context, entityX, entityY, entityX + 38, entityY + 68, 22, 0.0625F,
            (float)entityX + 19.0F, (float)entityY + 34.0F, this.client.player
         );
      }

      // Keep the profile card visual-only: static 3D model on the left, info stacked on the right.
      int textX = cardX + 48;
      String subLine = "MC " + this.mcVersion();
      GuiStyle.text(context, this.textRenderer, subLine, textX, cardY + 16, (int)(anim * 150.0F) << 24 | 0x00FFFFFF);

      // Discord invite on its own row, right of the model so it never overlaps the skin.
      int dcY = cardY + 42;
      boolean dcHover = profHover && mouseX >= textX - 2 && mouseY >= dcY - 2 && mouseY <= dcY + 12;
      this.discordHover = dcHover ? Math.min(1.0F, this.discordHover + 0.1F) : Math.max(0.0F, this.discordHover - 0.1F);
      int dcCol = (int)(anim * (200 + this.discordHover * 55.0F)) << 24 | (primary & 0x00FFFFFF);
      String discordText = this.trimWithEllipsis(DISCORD_INVITE, Math.max(1, cardX + cardW - textX - 4));
      GuiStyle.text(context, this.textRenderer, discordText, textX, dcY, dcCol);
      int underlineW = Math.min(this.textRenderer.getWidth(discordText), cardX + cardW - textX - 4);
      if (underlineW > 0) {
         context.fill(textX, dcY + 10, textX + underlineW, dcY + 11, (int)(anim * (110 + this.discordHover * 90.0F)) << 24 | (primary & 0x00FFFFFF));
      }
   }

   private String trimWithEllipsis(String value, int maxWidth) {
      if (value == null || value.isEmpty() || this.textRenderer.getWidth(value) <= maxWidth) return value == null ? "" : value;
      String ellipsis = "…";
      int available = Math.max(1, maxWidth - this.textRenderer.getWidth(ellipsis));
      String trimmed = this.textRenderer.trimToWidth(value, available, false);
      return trimmed.isEmpty() ? ellipsis : trimmed + ellipsis;
   }

   private static String mcVersion;

   private String mcVersion() {
      if (mcVersion == null) {
         try {
            mcVersion = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("minecraft")
               .map(c -> c.getMetadata().getVersion().getFriendlyString())
               .orElse("?");
         } catch (Exception e) {
            mcVersion = "?";
         }
      }
      return mcVersion;
   }

   private void renderAnimatedContent(DrawContext context, int mouseX, int mouseY, float openProgress, int winX, int winY, int winW, int winH) {
      int primary = VoidCyanClient.getPrimaryColor();
      float anim = this.contentAnimation * openProgress;
      int contentX = winX + GuiStyle.SIDEBAR_W;
      int contentY = winY;
      int contentW = winW - GuiStyle.SIDEBAR_W;
      if (contentW <= 0) return;

      // Content well background.
      GuiStyle.roundedRect(context, contentX, contentY, contentW, winH, 10, (int)(anim * 140.0F) << 24 | 0x100C18);
      int sbX = contentX + 10;
      int sbY = contentY + 10;
      int sbW = contentW - 20;
      int sbH = winH - 20;
      GuiStyle.drawContentWell(context, sbX, sbY, sbW, sbH, primary, anim);

      float contentAlpha = anim;
      int innerX = sbX + 14;
      int innerY = sbY + 10;
      int innerW = sbW - 28;
      int innerBottom = sbY + sbH - 10;
      switch (this.currentTab) {
         case 0:
            this.renderAnimatedModulesTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha, innerBottom);
            break;
         case 1:
            this.renderAnimatedScreenshotsTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 2:
            this.renderAnimatedBackgroundsTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 3:
            this.renderAnimatedGlobalSettingsTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 4:
            this.renderAnimatedFriendsTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 5:
            this.renderAnimatedConfigTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 6:
            this.renderAnimatedStatsTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 7:
            this.renderAnimatedNotesTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 8:
            this.renderAnimatedCalculatorTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
         case 10:
            this.renderAnimatedPlayerModelTab(context, mouseX, mouseY, innerX, innerY, innerW, contentAlpha);
            break;
      }
   }

   /** Visible modules after search + category + enabled filter + optional A-Z sort. */
   private List<ClickGuiScreen.ModuleInfo> getVisibleModules() {
      List<ClickGuiScreen.ModuleInfo> out = new ArrayList<>();
      String q = this.moduleSearchInput == null ? "" : this.moduleSearchInput.toLowerCase();

      for (ClickGuiScreen.ModuleInfo m : this.modules) {
         if (this.filterEnabled && !m.enabled) continue;
         if (!"All".equals(this.selectedModuleCategory) && !ClickGuiScreen.getCategoryForModule(m.name).equals(this.selectedModuleCategory)) continue;
         boolean searchMatch = com.voidcyan.client.ModuleDescriptions.matches(m.name, q)
            || m.description.toLowerCase().contains(q);
         if (!searchMatch) continue;
         out.add(m);
      }

      if (this.sortAZ) {
         out.sort((ma, mb) -> ma.name.compareToIgnoreCase(mb.name));
      }

      return out;
   }

   private void drawAnimatedCard(DrawContext context, int x, int y, int width, int height, int color, float intensity, float progress) {
      if (!(progress <= 0.0F)) {
         int scaledWidth = (int)(width * progress);
         int scaledHeight = (int)(height * progress);
         int offsetX = (width - scaledWidth) / 2;
         int offsetY = (height - scaledHeight) / 2;
         int ax = x + offsetX;
         int ay = y + offsetY;
         float fade = intensity * progress;
         int shadowA = (int)(fade * 50.0F);
         GuiStyle.roundedRect(context, ax + 2, ay + 3, scaledWidth, scaledHeight, 3, shadowA << 24);
         int bgCol = (int)(progress * 216.0F) << 24 | 0x171320;
         GuiStyle.roundedRect(context, ax, ay, scaledWidth, scaledHeight, 3, bgCol);
         int hiA = (int)(fade * 16.0F);
         if (hiA > 0) {
            GuiStyle.roundedRect(context, ax + 1, ay + 1, scaledWidth - 2, 4, 3, hiA << 24 | 0xFFFFFF);
         }

         int borderA = (int)(fade * (90 + intensity * 90.0F));
         int borderCol = Math.min(255, borderA) << 24 | (color & 0x00FFFFFF);
         GuiStyle.roundedOutline(context, ax, ay, scaledWidth, scaledHeight, 3, borderCol);
      }
   }

   private void drawGlowingBorder(DrawContext context, int x, int y, int width, int height, int color, float intensity) {
      this.drawRefinedBorder(context, x, y, width, height, color, intensity);
   }

   private void drawRefinedBorder(DrawContext context, int x, int y, int width, int height, int color, float intensity) {
      if (!(intensity <= 0.0F)) {
         // Clean 1px rounded outline + one soft outer ring; no hard fill lines.
         int borderA = (int)(intensity * 170.0F);
         GuiStyle.roundedOutline(context, x, y, width, height, 3, borderA << 24 | color & 16777215);
         int g1 = (int)(intensity * 45.0F);
         if (g1 > 3) {
            GuiStyle.roundedOutline(context, x - 1, y - 1, width + 2, height + 2, 4, g1 << 24 | color & 16777215);
         }
      }
   }

   public static String getCategoryForModule(String name) {
      switch (name) {
         case "Target HUD":
         case "Custom Hitbox":
         case "Totem Trace":
         case "Health Indicators":
         case "Damage Hearts":
         case "Low Health Alarm":
         case "Pot Warning":
         case "Drop Prevention":
         case "Hit Color":
         case "Totem Pop Color":
         case "Damage Color":
         case "Totem Counter":
         case "Arrow Counter":
         case "Reach Display":
         case "Totem Pop Notifier":
         case "Logout Spots":
         case "Effects":
            return "Combat";
         case "Inv HUD":
         case "Item Physics":
         case "Big Items":
         case "Inv Highlight":
         case "Armor Status":
         case "Potion Status":
         case "Item Animations":
         case "View Model":
         case "Transparent Shield":
            return "Items";
         case "Block Info HUD":
         case "Waypoints":
         case "Fullbright":
         case "Time Changer":
         case "TNT Timer":
         case "Biome Display":
         case "Water Fog":
         case "Block Overlay":
            return "World";
         case "FreeLook":
         case "Zoom":
         case "Saturation":
         case "Player Trail":
         case "Toggle Sprint":
         case "Toggle Sneak":
         case "Nick Hider":
         case "Peer Nick":
         case "Big Head":
         case "China Hat":
            return "Player";
         default:
            return "HUD";
      }
   }

   private void renderAnimatedModulesTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha, int contentBottom) {
      if (alpha <= 0.0F) return;
      int primary = VoidCyanClient.getPrimaryColor();
      int a255 = (int)(alpha * 255.0F);

      // ---- Header: tab icon + title, then A-Z / Enabled chips + search pill on the right.
      int headerY = y - 2;
      String pageTitle = this.tabNames[this.currentTab];
      GuiStyle.text(context, this.textRenderer, pageTitle, x, headerY, (int)(a255) << 24 | (primary & 0x00FFFFFF));

      int searchPillW = Math.min(240, width / 3);
      int searchPillH = 20;
      int searchPillX = x + width - searchPillW;
      int searchPillY = headerY - 3;
      int searchIconX = searchPillX + 8;
      int searchInputX = searchIconX + 14;
      int clearX = searchPillX + searchPillW - 14;
      int azW = GuiStyle.chipWidth(this.textRenderer, "A-Z");
      int enW = GuiStyle.chipWidth(this.textRenderer, "Enabled");
      int azX = searchPillX - azW - 8;
      int enX = azX - enW - 6;

      boolean azHov = GuiStyle.inRect(mouseX, mouseY, azX, searchPillY, azW, searchPillH);
      this.azChipHover = azHov ? Math.min(1.0F, this.azChipHover + 0.15F) : Math.max(0.0F, this.azChipHover - 0.15F);
      boolean enHov = GuiStyle.inRect(mouseX, mouseY, enX, searchPillY, enW, searchPillH);
      this.enabledChipHover = enHov ? Math.min(1.0F, this.enabledChipHover + 0.15F) : Math.max(0.0F, this.enabledChipHover - 0.15F);
      GuiStyle.chip(context, this.textRenderer, "A-Z", azX, searchPillY, this.sortAZ, this.azChipHover, primary, alpha);
      GuiStyle.chip(context, this.textRenderer, "Enabled", enX, searchPillY, this.filterEnabled, this.enabledChipHover, primary, alpha);

      int spBorder = this.isModuleSearchFocused ? (int)(a255) << 24 | (primary & 0x00FFFFFF) : (int)(alpha * (90 + (GuiStyle.inRect(mouseX, mouseY, searchPillX, searchPillY, searchPillW, searchPillH) ? 70 : 0))) << 24 | 0x00FFFFFF;
      int spBg = (int)(alpha * 170.0F) << 24 | 0x14101C;
      GuiStyle.roundedBordered(context, searchPillX, searchPillY, searchPillW, searchPillH, 5, spBg, spBorder);
      GuiStyle.glyph(context, searchIconX, searchPillY + 6, 2, GuiStyle.GLYPH_SEARCH, (int)(alpha * 150.0F) << 24 | 0x00FFFFFF);
      if (this.moduleSearchInput.isEmpty() && !this.isModuleSearchFocused) {
         GuiStyle.text(context, this.textRenderer, "Search", searchInputX, searchPillY + 6, (int)(alpha * 120.0F) << 24 | 0x00FFFFFF);
      } else {
         String displayTxt = this.moduleSearchInput;
         if (this.isModuleSearchFocused && System.currentTimeMillis() / 500L % 2L == 0L) {
            displayTxt = displayTxt + "_";
         }

         GuiStyle.text(context, this.textRenderer, displayTxt, searchInputX, searchPillY + 6, (int)(a255) << 24 | 0x00FFFFFF);
      }

      if (!this.moduleSearchInput.isEmpty()) {
         boolean clrHov = mouseX >= clearX && mouseX <= clearX + 10 && mouseY >= searchPillY + 5 && mouseY <= searchPillY + 15;
         GuiStyle.text(context, this.textRenderer, "✕", clearX, searchPillY + 6, (int)(alpha * (clrHov ? 255.0F : 150.0F)) << 24 | 0x00FFFFFF);
      }

      // ---- Category chips row + count.
      int chipsY = searchPillY + searchPillH + 8;
      int chipsRowY = chipsY;
      if (this.catChipHover.length != MODULE_CATEGORIES.length) {
         this.catChipHover = new float[MODULE_CATEGORIES.length];
      }

      int chipCursor = x;
      for (int ci = 0; ci < MODULE_CATEGORIES.length; ci++) {
         String cat = MODULE_CATEGORIES[ci];
         int cw = GuiStyle.chipWidth(this.textRenderer, cat);
         boolean hov = GuiStyle.inRect(mouseX, mouseY, chipCursor, chipsRowY, cw, 16);
         this.catChipHover[ci] = hov ? Math.min(1.0F, this.catChipHover[ci] + 0.15F) : Math.max(0.0F, this.catChipHover[ci] - 0.15F);
         GuiStyle.chip(context, this.textRenderer, cat, chipCursor, chipsRowY, cat.equals(this.selectedModuleCategory), this.catChipHover[ci], primary, alpha);
         chipCursor += cw + 6;
      }

      // ---- Module cards: single column, rounded, scroll-clipped.
      int listStartY = chipsRowY + 22;
      int cardGap = 8;
      int cardH = 44;
      List<ClickGuiScreen.ModuleInfo> visible = this.getVisibleModules();
      int cardW = width;

      // Position animation pass (slide into their ordered slots).
      int slotY = listStartY - this.scrollOffset;
      int idx = 0;

      for (ClickGuiScreen.ModuleInfo m : visible) {
         m.searchAnimationProgress = Math.min(1.0F, m.searchAnimationProgress + 0.15F);
         float targetY = slotY;
         if (m.currentX != (float)x || m.currentY == -1.0F) {
            if (m.currentY == -1.0F) {
               m.currentX = x;
               m.currentY = targetY;
            }
         }

         m.currentX = m.currentX + (x - m.currentX) * 0.3F;
         m.currentY = m.currentY + (targetY - m.currentY) * 0.3F;
         slotY += cardH + cardGap;
         idx++;
      }

      // Decay animations of modules not currently visible.
      for (ClickGuiScreen.ModuleInfo m : this.modules) {
         if (!visible.contains(m)) {
            m.searchAnimationProgress = Math.max(0.0F, m.searchAnimationProgress - 0.15F);
         }
      }

      if (visible.isEmpty()) {
         String msg = "No modules match.";
         GuiStyle.text(context, this.textRenderer, msg, x + width / 2 - this.textRenderer.getWidth(msg) / 2, listStartY + 30, (int)(alpha * 120.0F) << 24 | 0x00FFFFFF);
      }

      int totalListH = visible.size() * (cardH + cardGap);
      int visibleH = Math.max(60, contentBottom - listStartY);
      this.maxScrollOffset = Math.max(0, totalListH - visibleH);
      if (this.maxScrollOffset == 0) {
         this.scrollOffset = 0;
         this.targetScrollOffset = 0;
      } else {
         this.targetScrollOffset = Math.max(0, Math.min(this.maxScrollOffset, this.targetScrollOffset));
         this.scrollOffset = Math.max(0, Math.min(this.maxScrollOffset, this.scrollOffset));
      }

      for (ClickGuiScreen.ModuleInfo module : visible) {
         if (module.searchAnimationProgress <= 0.01F) continue;
         float modAlpha = alpha * module.searchAnimationProgress;
         int cX = (int)module.currentX;
         int cY = (int)module.currentY;
         if (cY + cardH <= listStartY || cY >= contentBottom) continue;
         boolean isHovered = mouseX >= cX && mouseX <= cX + cardW && mouseY >= Math.max(listStartY, cY) && mouseY <= Math.min(contentBottom, cY + cardH);
         module.hoverProgress = isHovered ? Math.min(1.0F, module.hoverProgress + 0.15F) : Math.max(0.0F, module.hoverProgress - 0.15F);
         int alphaInt = (int)(modAlpha * 255.0F);
         int lift = (int)(module.hoverProgress * 2.0F);
         int drawY = cY - lift;

         int borderCol;
         int bgCol;
         if (module.enabled) {
            borderCol = (int)(modAlpha * (190 + module.hoverProgress * 65.0F)) << 24 | (primary & 0x00FFFFFF);
            bgCol = (int)(alphaInt * 0.9F) << 24 | GuiStyle.blend(0x151020, primary, 0.10F);
         } else if (isHovered) {
            borderCol = (int)(modAlpha * 130.0F) << 24 | 0x00FFFFFF;
            bgCol = (int)(alphaInt * 0.9F) << 24 | 0x1D1826;
         } else {
            borderCol = (int)(modAlpha * 42.0F) << 24 | 0x00FFFFFF;
            bgCol = (int)(alphaInt * 0.9F) << 24 | 0x161220;
         }

         GuiStyle.roundedBordered2(context, cX, drawY, cardW, cardH, 12, bgCol, borderCol);

         // Name + description (ellipsized so nothing sticks out of the card).
         int nameX = cX + 16;
         int nameCol = module.enabled ? (int)(modAlpha * 255.0F) << 24 | (primary & 0x00FFFFFF) : (int)(modAlpha * 235.0F) << 24 | 0x00FFFFFF;
         int maxNameW = Math.max(1, cardW - 170);
         String nameTxt = module.name;
         while (this.textRenderer.getWidth(nameTxt) > maxNameW && nameTxt.length() > 1) {
            nameTxt = nameTxt.substring(0, nameTxt.length() - 1);
         }

         GuiStyle.text(context, this.textRenderer, nameTxt, nameX, drawY + 8, nameCol);
         String subtitle = module.description.isEmpty() ? (module.openSettings != null ? "Right click to configure" : "Utility module") : module.description;
         int maxDescW = Math.max(1, cardW - 170);
         int descCol = (int)(modAlpha * 140.0F) << 24 | 0x00FFFFFF;
         String desc = subtitle;

         while (this.textRenderer.getWidth(desc) > maxDescW && desc.length() > 2) {
            desc = desc.substring(0, desc.length() - 2);
         }

         if (desc.length() < subtitle.length() && !desc.isEmpty()) {
            desc = desc.substring(0, Math.max(1, desc.length() - 1)).trim() + "…";
         }

         GuiStyle.text(context, this.textRenderer, desc, nameX, drawY + 20, descCol);

         // Reset pill (only for movable HUD modules).
         if (module.resetHudPosition != null) {
            int rW = 42;
            int rH = 16;
            int rX = cX + cardW - rW - 154;
            int rY = drawY + (cardH - rH) / 2;
            boolean rHov = isHovered && mouseX >= rX && mouseX <= rX + rW && mouseY >= rY && mouseY <= rY + rH;
            GuiStyle.roundedBordered(
               context,
               rX,
               rY,
               rW,
               rH,
               5,
               (int)(modAlpha * (rHov ? 60.0F : 30.0F)) << 24 | 0x00FFFFFF,
               (int)(modAlpha * (rHov ? 110.0F : 50.0F)) << 24 | 0x00FFFFFF
            );
            GuiStyle.textCentered(context, this.textRenderer, "Reset", rX + rW / 2, rY + 4, (int)(modAlpha * (rHov ? 255.0F : 170.0F)) << 24 | 0x00FFFFFF);
         }

         // Toggle (knob position shows state; no ON/OFF text).
         GuiStyle.toggleSwitch(context, cX + cardW - 58, drawY + (cardH - 16) / 2, 40, 16, module.enabled, module.enabled ? 1.0F : 0.0F, primary, modAlpha);
      }

      // Scrollbar on the far right of the list.
      if (this.maxScrollOffset > 0) {
         GuiStyle.scrollbar(
            context,
            x + width - 2,
            listStartY,
            contentBottom - listStartY,
            this.scrollOffset,
            this.maxScrollOffset,
            primary,
            alpha
         );
      }
   }

   private static void drawModernRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
      if (width <= 0 || height <= 0) return;
      if (radius <= 0) {
         context.fill(x, y, x + width, y + height, color);
         return;
      }
      int maxRadius = Math.min(width / 2, height / 2);
      radius = Math.min(radius, maxRadius);

      // Top curved cap
      for (int i = 0; i < radius; i++) {
         int distY = radius - 1 - i;
         int w = radius - (int)Math.sqrt(radius * radius - distY * distY);
         context.fill(x + w, y + i, x + width - w, y + i + 1, color);
      }

      // Center solid body
      if (height > radius * 2) {
         context.fill(x, y + radius, x + width, y + height - radius, color);
      }

      // Bottom curved cap
      for (int i = 0; i < radius; i++) {
         int distY = radius - 1 - i;
         int w = radius - (int)Math.sqrt(radius * radius - distY * distY);
         context.fill(x + w, y + height - 1 - i, x + width - w, y + height - i, color);
      }
   }

   private void drawShadow(DrawContext context, int x, int y, int width, int height, int radius, float alpha) {
      int a1 = (int)(alpha * 40.0F);
      if (a1 > 0) {
         drawModernRoundedRect(context, x, y + 2, width, height, radius, a1 << 24);
      }
   }

   private void drawGlowText(DrawContext context, String text, int x, int y, int color, float alpha) {
      int a = (int)(alpha * 255.0F);
      int c = a << 24 | color & 16777215;
      context.drawTextWithShadow(this.textRenderer, Text.literal(text), x, y, c);
   }

   private void renderAnimatedScreenshotsTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      this.drawGlowText(context, "Screenshots", x, y, this.primaryColor, alpha);
      if (this.screenshots.isEmpty()) {
         if (alpha > 0.5F) {
            float textAlpha = (alpha - 0.5F) * 2.0F;
            int textColor = (int)(textAlpha * 128.0F) << 24 | 8421504;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No screenshots found in screenshots folder"), x + width / 2, y + 60, textColor);
         }
      } else {
         int maxAvailableHeight = this.height - y - 20;
         if (maxAvailableHeight < 150) {
            maxAvailableHeight = 150;
         }

         // Narrower list column (37.5%) so the preview gets the space; everything fits
         // inside the tab: preview panel + name bar + copy + delete end at the bottom edge.
         int listX = x + 20;
         int listY = y + 30;
         int listWidth = (width - 60) * 3 / 8;
         int listHeight = maxAvailableHeight - 30;
         int previewX = listX + listWidth + 16;
         int previewWidth = width - listWidth - 16 - 40;
         int searchBarHeight = 28;
         int buttonHeight = 28;
         int controlsTotalHeight = searchBarHeight + 8 + buttonHeight + 8 + buttonHeight;
         int previewHeight = Math.max(80, listHeight - controlsTotalHeight);
         int searchBarY = listY + previewHeight + 8;
         int copyButtonY = searchBarY + searchBarHeight + 8;
         int copyButtonHeight = buttonHeight;
         int deleteButtonY = copyButtonY + copyButtonHeight + 8;
         int deleteButtonHeight = buttonHeight;
         this.renderScreenshotVerticalList(context, mouseX, mouseY, listX, listY, listWidth, listHeight, alpha);
         this.renderScreenshotPreviewPanel(context, mouseX, mouseY, previewX, listY, previewWidth, previewHeight, this.getSelectedScreenshot(), alpha);
         this.renderScreenshotSearchBar(context, mouseX, mouseY, previewX, searchBarY, previewWidth, searchBarHeight, this.getSelectedScreenshot(), alpha);
         int copyColor = -11751600;
         if (mouseX >= previewX && mouseX <= previewX + previewWidth && mouseY >= copyButtonY && mouseY <= copyButtonY + copyButtonHeight) {
            copyColor = -10044566;
         }

         this.renderBackgroundButton(
            context,
            mouseX,
            mouseY,
            previewX,
            copyButtonY,
            previewWidth,
            copyButtonHeight,
            "Copy to Background",
            copyColor,
            alpha > 0.3F ? Math.min(1.0F, (alpha - 0.3F) / 0.7F) : 0.0F
         );
         this.renderScreenshotDeleteButton(
            context, mouseX, mouseY, previewX, deleteButtonY, previewWidth, deleteButtonHeight, this.getSelectedScreenshot(), alpha
         );
      }
   }

   private void renderAnimatedBackgroundsTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      this.drawGlowText(context, "Background Manager", x, y, this.primaryColor, alpha);
      int buttonY = y + 20;
      int buttonSpacing = 10;
      int buttonWidth = Math.min(120, (width - buttonSpacing) / 2);
      int buttonHeight = 25;
      if (alpha > 0.3F) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         int clickGuiColor = VoidCyanClient.clickGuiBgEnabled ? -11751600 : -10456992;
         int editGuiColor = VoidCyanClient.editGuiBgEnabled ? -14575885 : -13615008;
         this.renderBackgroundButton(
            context,
            mouseX,
            mouseY,
            x,
            buttonY,
            buttonWidth,
            buttonHeight,
            "Click GUI " + (VoidCyanClient.clickGuiBgEnabled ? "ON" : "OFF"),
            clickGuiColor,
            textAlpha
         );
         this.renderBackgroundButton(
            context,
            mouseX,
            mouseY,
            x + buttonWidth + buttonSpacing,
            buttonY,
            buttonWidth,
            buttonHeight,
            "Edit GUI " + (VoidCyanClient.editGuiBgEnabled ? "ON" : "OFF"),
            editGuiColor,
            textAlpha
         );
         this.renderBackgroundButton(
            context, mouseX, mouseY, x, buttonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, "Open Folder", -6543440, textAlpha
         );
         this.renderBackgroundButton(
            context,
            mouseX,
            mouseY,
            x + buttonWidth + buttonSpacing,
            buttonY + buttonHeight + buttonSpacing,
            buttonWidth,
            buttonHeight,
            "Refresh",
            -26624,
            textAlpha
         );
      }

      if (this.backgroundFiles.isEmpty()) {
         if (alpha > 0.5F) {
            float textAlpha = (alpha - 0.5F) * 2.0F;
            int textColor = (int)(textAlpha * 128.0F) << 24 | 8421504;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No background files found"), x + width / 2, y + 80, textColor);
         }
      } else {
         int maxAvailableHeight = this.height - y - 20;
         if (maxAvailableHeight < 150) {
            maxAvailableHeight = 150;
         }

         int listX = x + 20;
         int listY = y + 90;
         int listWidth = (width - 60) / 2;
         int listHeight = maxAvailableHeight - 90;
         int previewX = listX + listWidth;
         int previewWidth = width - listWidth - 60;
         int controlsTotalHeight = 90;
         int previewHeight = listHeight - controlsTotalHeight - 10;
         if (previewHeight < 50) {
            previewHeight = 50;
         }

         int controlsY = listY + previewHeight + 10;
         this.renderBackgroundFileList(context, mouseX, mouseY, listX, listY, listWidth, listHeight, alpha);
         this.renderBackgroundPreview(context, mouseX, mouseY, previewX, listY, previewWidth, previewHeight, alpha);
         this.renderBackgroundControls(context, mouseX, mouseY, previewX, controlsY, previewWidth, alpha);
      }
   }

   private void renderScreenshotVerticalList(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, float alpha) {
      this.drawAnimatedCard(context, x, y, width, height, this.secondaryColor, 0.4F, alpha);
      if (alpha > 0.3F) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         this.drawGlowText(context, "Select Screenshot", x + 10, y + 10, this.primaryColor, textAlpha);
         int itemHeight = 35;
         int itemY = y + 40;
         int itemSpacing = 5;

         for (int i = 0; i < this.screenshots.size() && i < 10; i++) {
            ClickGuiScreen.Screenshot screenshot = this.screenshots.get(i);
            boolean isSelected = i == 0;
            if (itemY + itemHeight <= y + height - 10) {
               this.renderScreenshotListItem(context, mouseX, mouseY, x + 10, itemY, width - 20, itemHeight, screenshot, textAlpha, isSelected);
            }

            itemY += itemHeight + itemSpacing;
         }
      }
   }

   private void renderScreenshotListItem(
      DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, ClickGuiScreen.Screenshot screenshot, float alpha, boolean isSelected
   ) {
      boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      int baseColor = isSelected ? this.primaryColor : (isHovered ? this.accentColor : this.secondaryColor);
      float intensity = isSelected ? 0.8F : (isHovered ? 0.6F : 0.3F);
      this.drawAnimatedCard(context, x, y, width, height, baseColor, intensity, alpha);
      if (alpha > 0.5F) {
         float textAlpha = (alpha - 0.5F) * 2.0F;
         int textColor = (int)(textAlpha * 255.0F) << 24 | 16777215;
         int thumbSize = height - 6;
         int thumbX = x + 3;
         int thumbY = y + 3;
         Identifier texture = this.loadScreenshotTexture(screenshot);
         if (texture != null) {
            try {
               context.drawTexture(
                  RenderPipelines.GUI_TEXTURED,
                  texture,
                  thumbX,
                  thumbY,
                  0.0F,
                  0.0F,
                  thumbSize,
                  thumbSize,
                  thumbSize,
                  thumbSize,
                  (int)(textAlpha * 255.0F) << 24 | 16777215
               );
            } catch (Exception var24) {
               context.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize, (int)(textAlpha * 120.0F) << 24 | 4210752);
               String icon = "IMG";
               int iconX = thumbX + (thumbSize - this.textRenderer.getWidth(icon)) / 2;
               int iconY = thumbY + (thumbSize - 9) / 2;
               context.drawTextWithShadow(this.textRenderer, Text.literal(icon), iconX, iconY, textColor);
            }
         } else {
            context.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize, (int)(textAlpha * 120.0F) << 24 | 4210752);
            String icon = "IMG";
            int iconX = thumbX + (thumbSize - this.textRenderer.getWidth(icon)) / 2;
            int iconY = thumbY + (thumbSize - 9) / 2;
            context.drawTextWithShadow(this.textRenderer, Text.literal(icon), iconX, iconY, textColor);
         }

         this.drawGlowingBorder(context, thumbX, thumbY, thumbSize, thumbSize, baseColor, textAlpha * 0.3F);
         String displayName = screenshot.name;
         int maxNameWidth = width - thumbSize - 15;
         if (this.textRenderer.getWidth(displayName) > maxNameWidth) {
            while (this.textRenderer.getWidth(displayName + "...") > maxNameWidth && displayName.length() > 1) {
               displayName = displayName.substring(0, displayName.length() - 1);
            }

            displayName = displayName + "...";
         }

         context.drawTextWithShadow(this.textRenderer, Text.literal(displayName), thumbX + thumbSize + 8, y + (height - 9) / 2, textColor);
      }
   }

   private void renderScreenshotPreviewPanel(
      DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, ClickGuiScreen.Screenshot screenshot, float alpha
   ) {
      this.drawAnimatedCard(context, x, y, width, height, this.primaryColor, 0.6F, alpha);
      if (alpha > 0.3F && screenshot != null) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         this.drawGlowText(context, "Preview", x + 10, y + 10, this.primaryColor, textAlpha);
         int previewAreaX = x + 10;
         int previewAreaY = y + 35;
         int previewAreaWidth = width - 20;
         int previewAreaHeight = height - 100;
         Identifier textureId = this.loadScreenshotTexture(screenshot);
         if (textureId != null) {
            try {
               context.fill(previewAreaX, previewAreaY, previewAreaX + previewAreaWidth, previewAreaY + previewAreaHeight, (int)(textAlpha * 255.0F) << 24 | 0);
               int[] dimensions = this.getScreenshotDimensions(screenshot);
               int imageWidth = dimensions[0];
               int imageHeight = dimensions[1];
               if (imageWidth > 0 && imageHeight > 0) {
                  int imageMargin = 10;
                  int maxImageWidth = previewAreaWidth - imageMargin * 2;
                  int maxImageHeight = previewAreaHeight - imageMargin * 2;
                  float aspectRatio = (float)imageWidth / imageHeight;
                  int displayWidth = maxImageWidth;
                  int displayHeight = (int)(maxImageWidth / aspectRatio);
                  if (displayHeight > maxImageHeight) {
                     displayHeight = maxImageHeight;
                     displayWidth = (int)(maxImageHeight * aspectRatio);
                  }

                  int drawX = previewAreaX + (previewAreaWidth - displayWidth) / 2;
                  int drawY = previewAreaY + (previewAreaHeight - displayHeight) / 2;
                  context.drawTexturedQuad(textureId, drawX, drawY, drawX + displayWidth, drawY + displayHeight, 0.0F, 1.0F, 0.0F, 1.0F);
                  this.drawGlowingBorder(context, drawX - 1, drawY - 1, displayWidth + 2, displayHeight + 2, this.primaryColor, textAlpha * 0.2F);
               }
            } catch (Exception var32) {
               this.renderFallbackPreview(context, previewAreaX, previewAreaY, previewAreaWidth, previewAreaHeight, screenshot, textAlpha);
            }
         } else {
            this.renderFallbackPreview(context, previewAreaX, previewAreaY, previewAreaWidth, previewAreaHeight, screenshot, textAlpha);
         }

         this.drawGlowingBorder(context, previewAreaX, previewAreaY, previewAreaWidth, previewAreaHeight, this.secondaryColor, textAlpha * 0.5F);
         String fileInfo = screenshot.file.getName();
         long fileSize = screenshot.fileSize();
         String sizeInfo = String.format(" (%.1f KB)", fileSize / 1024.0);
         String fullInfo = fileInfo + sizeInfo;
         int infoColor = (int)(textAlpha * 180.0F) << 24 | 13421772;
         int infoX = previewAreaX + 10;
         int maxWidth = previewAreaWidth - 20;
         if (this.textRenderer.getWidth(fullInfo) > maxWidth) {
            if (this.textRenderer.getWidth(fileInfo) > maxWidth) {
               List<String> wrappedLines = new ArrayList<>();
               String[] words = fileInfo.split("(?<=[-_.])|(?=[-_.])");
               StringBuilder currentLine = new StringBuilder();

               for (String word : words) {
                  String testLine = currentLine + word;
                  if (this.textRenderer.getWidth(testLine) <= maxWidth) {
                     currentLine.append(word);
                  } else if (currentLine.length() > 0) {
                     wrappedLines.add(currentLine.toString());
                     currentLine = new StringBuilder(word);
                  } else {
                     wrappedLines.add(word);
                  }
               }

               if (currentLine.length() > 0) {
                  wrappedLines.add(currentLine.toString());
               }

               int lineY = y + height - 25 - (wrappedLines.size() - 1) * 10;

               for (String line : wrappedLines) {
                  context.drawTextWithShadow(this.textRenderer, Text.literal(line), infoX, lineY, infoColor);
                  lineY += 10;
               }

               context.drawTextWithShadow(this.textRenderer, Text.literal(sizeInfo), infoX, lineY, infoColor);
            } else {
               context.drawTextWithShadow(this.textRenderer, Text.literal(fileInfo), infoX, y + height - 25, infoColor);
               context.drawTextWithShadow(this.textRenderer, Text.literal(sizeInfo), infoX, y + height - 15, infoColor);
            }
         } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal(fullInfo), infoX, y + height - 25, infoColor);
         }
      } else if (alpha > 0.3F) {
         float textAlphax = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         int textColor = (int)(textAlphax * 128.0F) << 24 | 8421504;
         String noSelection = "Select a screenshot to preview";
         int noSelectionX = x + (width - this.textRenderer.getWidth(noSelection)) / 2;
         int noSelectionY = y + (height - 9) / 2;
         context.drawTextWithShadow(this.textRenderer, Text.literal(noSelection), noSelectionX, noSelectionY, textColor);
      }
   }

   private void renderFallbackPreview(DrawContext context, int x, int y, int width, int height, ClickGuiScreen.Screenshot screenshot, float alpha) {
      int checkerSize = 16;

      for (int checkY = 0; checkY < height; checkY += checkerSize) {
         for (int checkX = 0; checkX < width; checkX += checkerSize) {
            boolean isEvenSquare = (checkX / checkerSize + checkY / checkerSize) % 2 == 0;
            int checkerColor = isEvenSquare ? (int)(alpha * 60.0F) << 24 | 2763306 : (int)(alpha * 80.0F) << 24 | 3815994;
            int squareWidth = Math.min(checkerSize, width - checkX);
            int squareHeight = Math.min(checkerSize, height - checkY);
            context.fill(x + checkX, y + checkY, x + checkX + squareWidth, y + checkY + squareHeight, checkerColor);
         }
      }

      int frameThickness = 3;
      int innerX = x + 25;
      int innerY = y + 25;
      int innerWidth = width - 50;
      int innerHeight = height - 50;
      if (innerWidth > 0 && innerHeight > 0) {
         context.fill(x + 20, y + 20, x + width - 20, y + 20 + frameThickness, (int)(alpha * 255.0F) << 24 | this.primaryColor);
         context.fill(x + 20, y + height - 20 - frameThickness, x + width - 20, y + height - 20, (int)(alpha * 255.0F) << 24 | this.primaryColor);
         context.fill(x + 20, y + 20, x + 20 + frameThickness, y + height - 20, (int)(alpha * 255.0F) << 24 | this.primaryColor);
         context.fill(x + width - 20 - frameThickness, y + 20, x + width - 20, y + height - 20, (int)(alpha * 255.0F) << 24 | this.primaryColor);
         int topColor = (int)(alpha * 180.0F) << 24 | 4886754;
         int bottomColor = (int)(alpha * 180.0F) << 24 | 2906784;

         for (int i = 0; i < innerHeight; i++) {
            float gradientRatio = (float)i / innerHeight;
            int currentColor = this.lerpColor(topColor, bottomColor, gradientRatio);
            context.fill(innerX, innerY + i, innerX + innerWidth, innerY + i + 1, currentColor);
         }

         int textColor = (int)(alpha * 255.0F) << 24 | 16777215;
         String imageText = " Could not load image";
         int imageTextX = innerX + (innerWidth - this.textRenderer.getWidth(imageText)) / 2;
         int imageTextY = innerY + (innerHeight - 9) / 2 - 10;
         context.drawTextWithShadow(this.textRenderer, Text.literal(imageText), imageTextX, imageTextY, textColor);
         String filename = screenshot.name;
         int filenameX = innerX + (innerWidth - this.textRenderer.getWidth(filename)) / 2;
         context.drawTextWithShadow(this.textRenderer, Text.literal(filename), filenameX, imageTextY + 20, textColor);
      }
   }

   private int lerpColor(int color1, int color2, float ratio) {
      int a1 = color1 >>> 24 & 0xFF;
      int r1 = color1 >>> 16 & 0xFF;
      int g1 = color1 >>> 8 & 0xFF;
      int b1 = color1 & 0xFF;
      int a2 = color2 >>> 24 & 0xFF;
      int r2 = color2 >>> 16 & 0xFF;
      int g2 = color2 >>> 8 & 0xFF;
      int b2 = color2 & 0xFF;
      int a = (int)(a1 + (a2 - a1) * ratio);
      int r = (int)(r1 + (r2 - r1) * ratio);
      int g = (int)(g1 + (g2 - g1) * ratio);
      int b = (int)(b1 + (b2 - b1) * ratio);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private ClickGuiScreen.Screenshot getSelectedScreenshot() {
      return this.screenshots.isEmpty() ? null : this.screenshots.get(0);
   }

   private Identifier loadScreenshotTexture(ClickGuiScreen.Screenshot screenshot) {
      if (this.screenshotTextures.containsKey(screenshot.name)) {
         return this.screenshotTextures.get(screenshot.name);
      } else {
         try {
            File imageFile = screenshot.file;
            if (imageFile.exists() && (imageFile.getName().endsWith(".png") || imageFile.getName().endsWith(".jpg"))) {
               FileInputStream fis = new FileInputStream(imageFile);
               NativeImage nativeImage = NativeImage.read(fis);
               fis.close();
               int width = nativeImage.getWidth();
               int height = nativeImage.getHeight();
               this.screenshotDimensions.put(screenshot.name, width << 16 | height);
               NativeImageBackedTexture texture = new NativeImageBackedTexture(null, nativeImage);
               Identifier textureId = Identifier.of("voidcyan", "screenshot_" + screenshot.name.hashCode());
               this.client.getTextureManager().registerTexture(textureId, texture);
               this.screenshotTextures.put(screenshot.name, textureId);
               return textureId;
            }
         } catch (Exception var9) {
            System.err.println("Failed to load screenshot: " + screenshot.name + " - " + var9.getMessage());
         }

         // Remember the failure so the file isn't re-read every frame.
         this.screenshotTextures.put(screenshot.name, null);
         return null;
      }
   }

   private int[] getScreenshotDimensions(ClickGuiScreen.Screenshot screenshot) {
      if (this.screenshotDimensions.containsKey(screenshot.name)) {
         int packed = this.screenshotDimensions.get(screenshot.name);
         return new int[]{packed >> 16, packed & 65535};
      } else {
         return new int[]{0, 0};
      }
   }

   private void renderScreenshotSearchBar(
      DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, ClickGuiScreen.Screenshot screenshot, float alpha
   ) {
      if (!(alpha <= 0.3F) && screenshot != null) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
         boolean isFocused = this.isScreenshotSearchFocused;
         float intensity = isFocused ? 1.0F : (isHovered ? 0.8F : 0.4F);
         int borderColor = isFocused ? this.accentColor : this.primaryColor;
         this.drawAnimatedCard(context, x, y, width, height, borderColor, intensity, textAlpha);
         if (textAlpha > 0.5F) {
            float searchTextAlpha = (textAlpha - 0.5F) * 2.0F;
            int textColor = (int)(searchTextAlpha * 255.0F) << 24 | (this.screenshotSearchInput.isEmpty() && !isFocused ? 8421504 : 16777215);
            String displayText;
            if (isFocused && this.screenshotSearchInput.isEmpty()) {
               displayText = "Enter new name...";
            } else if (this.screenshotSearchInput.isEmpty()) {
               displayText = screenshot.editableName;
            } else {
               displayText = this.screenshotSearchInput;
            }

            context.drawTextWithShadow(this.textRenderer, Text.literal(displayText), x + 12, y + (height - 9) / 2, textColor);
            if (isFocused) {
            }

            if (isFocused || !this.screenshotSearchInput.isEmpty()) {
               int extColor = (int)(searchTextAlpha * 180.0F) << 24 | 8421504;
               String currentText = this.screenshotSearchInput.isEmpty() ? "" : this.screenshotSearchInput;
               context.drawTextWithShadow(
                  this.textRenderer, Text.literal(".png"), x + 12 + this.textRenderer.getWidth(currentText) + 5, y + (height - 9) / 2, extColor
               );
            }
         }
      }
   }

   private void renderScreenshotDeleteButton(
      DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, ClickGuiScreen.Screenshot screenshot, float alpha
   ) {
      if (!(alpha <= 0.3F) && screenshot != null) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
         float intensity = isHovered ? 1.0F : 0.7F;
         float scale = isHovered ? 1.02F : 1.0F;
         int deleteColor = -48060;
         int scaledWidth = (int)(width * scale);
         int scaledHeight = (int)(height * scale);
         int offsetX = (width - scaledWidth) / 2;
         int offsetY = (height - scaledHeight) / 2;
         this.drawAnimatedCard(context, x + offsetX, y + offsetY, scaledWidth, scaledHeight, deleteColor, intensity, textAlpha);
         if (textAlpha > 0.5F) {
            float buttonTextAlpha = (textAlpha - 0.5F) * 2.0F;
            int textColor = (int)(buttonTextAlpha * 255.0F) << 24 | 16777215;
            String deleteText = "Delete";
            int textX = x + (width - this.textRenderer.getWidth(deleteText)) / 2;
            int textY = y + (height - 9) / 2;
            context.drawTextWithShadow(this.textRenderer, Text.literal(deleteText), textX, textY, textColor);
         }

         if (isHovered) {
            this.drawGlowingBorder(context, x + offsetX - 2, y + offsetY - 2, scaledWidth + 4, scaledHeight + 4, deleteColor, textAlpha * 0.8F);
         }
      }
   }

   private void renderBackgroundFileList(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, float alpha) {
      this.drawAnimatedCard(context, x, y, width, height, this.secondaryColor, 0.4F, alpha);
      if (alpha > 0.3F) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         this.drawGlowText(context, "Background Files", x + 10, y + 10, this.primaryColor, textAlpha);
         int itemHeight = 25;
         int itemY = y + 40;
         int itemSpacing = 2;

         for (int i = 0; i < this.backgroundFiles.size() && i < 15; i++) {
            ClickGuiScreen.BackgroundFile bgFile = this.backgroundFiles.get(i);
            boolean isSelected = i == this.selectedBackgroundIndex;
            if (itemY + itemHeight <= y + height - 10) {
               this.renderBackgroundFileItem(context, mouseX, mouseY, x + 10, itemY, width - 20, itemHeight, bgFile, textAlpha, isSelected, i);
            }

            itemY += itemHeight + itemSpacing;
         }
      }
   }

   private void renderBackgroundFileItem(
      DrawContext context,
      int mouseX,
      int mouseY,
      int x,
      int y,
      int width,
      int height,
      ClickGuiScreen.BackgroundFile bgFile,
      float alpha,
      boolean isSelected,
      int index
   ) {
      boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      int baseColor = isSelected ? this.primaryColor : (isHovered ? this.accentColor : this.secondaryColor);
      float intensity = isSelected ? 0.8F : (isHovered ? 0.6F : 0.2F);
      this.drawAnimatedCard(context, x, y, width, height, baseColor, intensity, alpha);
      if (alpha > 0.5F) {
         float textAlpha = (alpha - 0.5F) * 2.0F;
         int textColor = (int)(textAlpha * 255.0F) << 24 | 16777215;
         context.drawTextWithShadow(this.textRenderer, Text.literal("name: " + bgFile.displayName), x + 8, y + 4, textColor);
         String sizeText = bgFile.size < 1048576L ? String.format("%.1f KB", bgFile.size / 1024.0) : String.format("%.1f MB", bgFile.size / 1048576.0);
         context.drawTextWithShadow(this.textRenderer, Text.literal("size: " + sizeText), x + 8, y + 14, -1426063361);
         if (isSelected) {
            context.fill(x + width - 8, y + height / 2 - 2, x + width - 3, y + height / 2 + 2, textColor);
         }
      }
   }

   private Identifier getOrLoadBackgroundTexture(ClickGuiScreen.BackgroundFile bg) {
      if (this.backgroundTextures.containsKey(bg.name)) {
         return this.backgroundTextures.get(bg.name);
      } else if (bg.name.toLowerCase().endsWith(".mp4")) {
         return null;
      } else {
         try {
            File imageFile = bg.file;
            if (!imageFile.exists()) {
               this.backgroundTextures.put(bg.name, null);
               return null;
            } else {
               NativeImage nativeImage;
               try {
                  FileInputStream fis = new FileInputStream(imageFile);
                  nativeImage = NativeImage.read(fis);
                  fis.close();
               } catch (Exception var8) {
                  BufferedImage bimg = ImageIO.read(imageFile);
                  if (bimg == null) {
                     this.backgroundTextures.put(bg.name, null);
                     return null;
                  }

                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  ImageIO.write(bimg, "png", baos);
                  byte[] bytes = baos.toByteArray();
                  nativeImage = NativeImage.read(new ByteArrayInputStream(bytes));
               }

               int width = nativeImage.getWidth();
               int height = nativeImage.getHeight();
               this.backgroundDimensions.put(bg.name, width << 16 | height);
               NativeImageBackedTexture texture = new NativeImageBackedTexture(null, nativeImage);
               Identifier textureId = Identifier.of("voidcyan", "bg_preview_" + Math.abs(bg.name.hashCode()));
               this.client.getTextureManager().registerTexture(textureId, texture);
               this.backgroundTextures.put(bg.name, textureId);
               this.backgroundTextureObjects.put(bg.name, texture);
               return textureId;
            }
         } catch (Exception var9) {
            System.err.println("[VoidCyan] Error loading background preview: " + var9.getMessage());
            this.backgroundTextures.put(bg.name, null);
            return null;
         }
      }
   }

   private void renderBackgroundPreview(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, float alpha) {
      this.drawAnimatedCard(context, x, y, width, height, this.primaryColor, 0.6F, alpha);
      if (alpha > 0.3F) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         this.drawGlowText(context, "Background Preview", x + 10, y + 10, this.primaryColor, textAlpha);
         int previewAreaX = x + 10;
         int previewAreaY = y + 35;
         int previewAreaWidth = width - 20;
         int previewAreaHeight = height - 50;
         context.fill(previewAreaX, previewAreaY, previewAreaX + previewAreaWidth, previewAreaY + previewAreaHeight, (int)(textAlpha * 200.0F) << 24 | 1118481);
         if (this.selectedBackgroundIndex >= 0 && this.selectedBackgroundIndex < this.backgroundFiles.size()) {
            ClickGuiScreen.BackgroundFile selected = this.backgroundFiles.get(this.selectedBackgroundIndex);
            if (selected.name.toLowerCase().endsWith(".mp4")) {
               AnimatedTexture animTex = this.previewAnimatedTextures.get(selected.name);
               if (animTex == null) {
                  animTex = new AnimatedTexture(selected.file.getAbsolutePath(), "voidcyan", "preview_" + selected.name.replaceAll("[^a-zA-Z0-9_]", "_"));
                  this.previewAnimatedTextures.put(selected.name, animTex);
               }

               Identifier previewTexId = animTex.updateAndGetId();
               if (previewTexId != null) {
                  context.drawTexturedQuad(
                     previewTexId, previewAreaX, previewAreaY, previewAreaX + previewAreaWidth, previewAreaY + previewAreaHeight, 0.0F, 1.0F, 0.0F, 1.0F
                  );
               } else if (textAlpha > 0.5F) {
                  String msg = "Loading video...";
                  int msgX = previewAreaX + (previewAreaWidth - this.textRenderer.getWidth(msg)) / 2;
                  int msgY = previewAreaY + previewAreaHeight / 2 - 5;
                  context.drawTextWithShadow(this.textRenderer, Text.literal(msg), msgX, msgY, (int)(textAlpha * 180.0F) << 24 | 8421504);
               }
            } else {
               Identifier textureId = this.getOrLoadBackgroundTexture(selected);
               if (textureId != null) {
                  int packed = this.backgroundDimensions.getOrDefault(selected.name, previewAreaWidth << 16 | previewAreaHeight);
                  int imageWidth = packed >> 16 & 65535;
                  int imageHeight = packed & 65535;
                  if (imageWidth > 0 && imageHeight > 0) {
                     float aspectRatio = (float)imageWidth / imageHeight;
                     int displayWidth = previewAreaWidth;
                     int displayHeight = (int)(previewAreaWidth / aspectRatio);
                     if (displayHeight > previewAreaHeight) {
                        displayHeight = previewAreaHeight;
                        displayWidth = (int)(previewAreaHeight * aspectRatio);
                     }

                     int drawX = previewAreaX + (previewAreaWidth - displayWidth) / 2;
                     int drawY = previewAreaY + (previewAreaHeight - displayHeight) / 2;
                     context.drawTexturedQuad(textureId, drawX, drawY, drawX + displayWidth, drawY + displayHeight, 0.0F, 1.0F, 0.0F, 1.0F);
                     this.drawGlowingBorder(context, drawX - 1, drawY - 1, displayWidth + 2, displayHeight + 2, this.primaryColor, textAlpha * 0.4F);
                  }
               } else if (textAlpha > 0.5F) {
                  String msg = "Loading preview...";
                  int msgX = previewAreaX + (previewAreaWidth - this.textRenderer.getWidth(msg)) / 2;
                  int msgY = previewAreaY + previewAreaHeight / 2 - 5;
                  context.drawTextWithShadow(this.textRenderer, Text.literal(msg), msgX, msgY, (int)(textAlpha * 180.0F) << 24 | 8421504);
               }
            }
         } else if (textAlpha > 0.5F) {
            String msg = "Select a background from the list";
            int msgX = previewAreaX + (previewAreaWidth - this.textRenderer.getWidth(msg)) / 2;
            int msgY = previewAreaY + previewAreaHeight / 2 - 5;
            context.drawTextWithShadow(this.textRenderer, Text.literal(msg), msgX, msgY, (int)(textAlpha * 180.0F) << 24 | 8421504);
         }

         this.drawGlowingBorder(context, previewAreaX, previewAreaY, previewAreaWidth, previewAreaHeight, this.secondaryColor, textAlpha * 0.5F);
      }
   }

   private void renderBackgroundControls(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      if (!(alpha <= 0.3F)) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         this.renderOpacitySlider(context, mouseX, mouseY, x, y, width, textAlpha);
         int setBgY = y + 40;
         this.renderBackgroundButton(context, mouseX, mouseY, x, setBgY, width, 35, "Set as Background", this.primaryColor, textAlpha);
      }
   }

   private void renderOpacitySlider(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      if (!(alpha <= 0.0F)) {
         boolean isHovered = mouseX >= x + 80 && mouseX <= x + width && mouseY >= y && mouseY <= y + 25;
         int textColor = (int)(alpha * 255.0F) << 24 | 16777215;
         context.drawTextWithShadow(this.textRenderer, Text.literal("Transparency"), x, y + 8, textColor);
         int trackX = x + 80;
         int trackY = y + 10;
         int trackWidth = width - 80 - 50;
         int trackHeight = 8;
         context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, (int)(alpha * 100.0F) << 24 | 4210752);
         int fillWidth = (int)(trackWidth * this.backgroundOpacity);
         context.fill(trackX, trackY + 1, trackX + fillWidth, trackY + trackHeight - 1, (int)(alpha * 255.0F) << 24 | this.primaryColor & 16777215);
         int handleX = trackX + fillWidth - 4;
         int handleSize = 12;
         float handleScale = isHovered ? 1.2F : 1.0F;
         int scaledSize = (int)(handleSize * handleScale);
         context.fill(handleX, trackY - 2, handleX + scaledSize, trackY + trackHeight + 2, (int)(alpha * 255.0F) << 24 | 16777215);
         context.drawTextWithShadow(
            this.textRenderer, Text.literal(String.format("%.0f%%", this.backgroundOpacity * 100.0F)), trackX + trackWidth + 10, y + 8, textColor
         );
      }
   }

   private void renderBackgroundButton(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, String text, int color, float alpha) {
      boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      float intensity = isHovered ? 1.0F : 0.7F;
      float scale = isHovered ? 1.02F : 1.0F;
      int scaledWidth = (int)(width * scale);
      int scaledHeight = (int)(height * scale);
      int offsetX = (width - scaledWidth) / 2;
      int offsetY = (height - scaledHeight) / 2;
      this.drawAnimatedCard(context, x + offsetX, y + offsetY, scaledWidth, scaledHeight, color, intensity, alpha);
      if (alpha > 0.5F) {
         float textAlpha = (alpha - 0.5F) * 2.0F;
         int textColor = (int)(textAlpha * 255.0F) << 24 | 16777215;
         int textX = x + (width - this.textRenderer.getWidth(text)) / 2;
         int textY = y + (height - 9) / 2;
         context.drawTextWithShadow(this.textRenderer, Text.literal(text), textX, textY, textColor);
      }

      if (isHovered) {
         this.drawGlowingBorder(context, x + offsetX - 1, y + offsetY - 1, scaledWidth + 2, scaledHeight + 2, color, alpha * 0.8F);
      }
   }

   private void renderAnimatedGlobalSettingsTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      this.drawGlowText(context, "Global Settings", x, y, this.primaryColor, alpha);
      int cardY = y + 40 - this.scrollOffset;
      this.renderAnimatedThemePickerCard(context, mouseX, mouseY, x, cardY, width - 20, alpha);
      cardY += 165;
      this.renderAnimatedModuleSettingsCard(context, mouseX, mouseY, x, cardY, width - 20, alpha);
      cardY += 125;
      this.renderAnimatedOverlayParticlesCard(context, mouseX, mouseY, x, cardY, width - 20, alpha);
      cardY += 130;
      this.renderAnimatedOptimizeCard(context, mouseX, mouseY, x, cardY, width - 20, alpha);
      cardY += 176;
      this.renderAnimatedCritEffectsCard(context, mouseX, mouseY, x, cardY, width - 20, alpha);
      cardY += 66;
      this.renderAnimatedSafeModeCard(context, mouseX, mouseY, x, cardY, width - 20, alpha);
   }

   private void renderAnimatedSafeModeCard(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      int height = 92;
      this.drawAnimatedCard(context, x, y, width, height, this.primaryColor, 0.6F, alpha);
      if (alpha < 0.3F) return;
      float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
      int textColor = (int)(textAlpha * 235.0F) << 24 | 0x00FFFFFF;
      int subColor = (int)(textAlpha * 120.0F) << 24 | 0x00FFFFFF;
      this.drawGlowText(context, "Safe Mode", x + 12, y + 12, this.primaryColor, textAlpha);
      GuiStyle.text(context, this.textRenderer, "Locks Health Indicators, Name Tag Items and Target HUD off", x + 12, y + 26, subColor);

      int toggleX = x + width - 58;
      GuiStyle.toggleSwitch(context, toggleX, y + 12, 40, 16, VoidCyanClient.safeModeEnabled, VoidCyanClient.safeModeEnabled ? 1.0F : 0.0F, this.primaryColor, textAlpha);

      String status = VoidCyanClient.safeModeEnabled ? "On — those modules cannot be toggled on" : "Off — all modules available";
      GuiStyle.text(context, this.textRenderer, status, x + 12, y + 38, subColor);
      int btnY = y + 58;
      int btnW = 120;
      int btnX = x + 12;
      boolean hov = GuiStyle.inRect(mouseX, mouseY, btnX, btnY, btnW, 18);
      GuiStyle.roundedRect(context, btnX, btnY, btnW, 18, 3, (int)(textAlpha * (hov ? 140 : 90)) << 24 | 0x2A2434);
      GuiStyle.roundedOutline(context, btnX, btnY, btnW, 18, 3, (int)(textAlpha * (hov ? 170 : 90)) << 24 | (this.primaryColor & 0xFFFFFF));
      String label = "Turn Safe Mode " + (VoidCyanClient.safeModeEnabled ? "Off" : "On");
      GuiStyle.text(context, this.textRenderer, label, btnX + (btnW - this.textRenderer.getWidth(label)) / 2, btnY + 5, textColor);
   }

   private boolean handleSafeModeClick(double mouseX, double mouseY, int x, int width, int cardY) {
      int cardW = width - 20;
      int safeY = cardY + 420 + 176 + 66;
      int toggleX = x + cardW - 58;
      if (GuiStyle.inRect(mouseX, mouseY, toggleX, safeY + 12, 40, 16)
         || GuiStyle.inRect(mouseX, mouseY, x + 12, safeY + 58, 120, 18)) {
         VoidCyanClient.safeModeEnabled = !VoidCyanClient.safeModeEnabled;
         if (VoidCyanClient.safeModeEnabled) {
            VoidCyanClient.isHealthIndicatorsEnabled = false;
            VoidCyanClient.isNameTagItemsEnabled = false;
            VoidCyanClient.isTargetHudEnabled = false;
         }

         // Keep cached card toggle states in sync with the force-disabled flags.
         for (ClickGuiScreen.ModuleInfo m : this.modules) {
            if (m.name.equals("Health Indicators")) m.enabled = VoidCyanClient.isHealthIndicatorsEnabled;
            else if (m.name.equals("Name Tag Items")) m.enabled = VoidCyanClient.isNameTagItemsEnabled;
            else if (m.name.equals("Target HUD")) m.enabled = VoidCyanClient.isTargetHudEnabled;
         }

         VoidCyanClient.saveConfig();
         return true;
      }

      return false;
   }

   private void renderAnimatedCritEffectsCard(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      int height = 66;
      this.drawAnimatedCard(context, x, y, width, height, this.primaryColor, 0.6F, alpha);
      if (alpha < 0.3F) return;
      float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
      int textColor = (int)(textAlpha * 235.0F) << 24 | 0x00FFFFFF;
      int subColor = (int)(textAlpha * 120.0F) << 24 | 0x00FFFFFF;
      this.drawGlowText(context, "Crit Effects", x + 12, y + 12, this.primaryColor, textAlpha);
      GuiStyle.text(context, this.textRenderer, "Particles or your own .obj models (Blender)", x + 12, y + 26, subColor);

      int toggleX = x + width - 58;
      GuiStyle.toggleSwitch(context, toggleX, y + 12, 40, 16, CritEffectsManager.enabled, CritEffectsManager.enabled ? 1.0F : 0.0F, this.primaryColor, textAlpha);

      int count = CritEffectsManager.getEffectCount();
      String status = count > 0 ? count + " effect(s) loaded" : "No effects — folder opens on first click";
      GuiStyle.text(context, this.textRenderer, status, x + 12, y + 38, subColor);

      int btnY = y + 38;
      int btnW = 110;
      int btnX = x + width - 12 - btnW;
      boolean hov = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + 16;
      GuiStyle.roundedRect(context, btnX, btnY, btnW, 16, 3, (int)(textAlpha * (hov ? 140 : 90)) << 24 | 0x2A2434);
      GuiStyle.roundedOutline(context, btnX, btnY, btnW, 16, 3, (int)(textAlpha * (hov ? 170 : 90)) << 24 | (this.primaryColor & 0xFFFFFF));
      String label = "Open Effects Folder";
      GuiStyle.text(context, this.textRenderer, label, btnX + (btnW - this.textRenderer.getWidth(label)) / 2, btnY + 4, textColor);
   }

   private void renderAnimatedOptimizeCard(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      int height = 154;
      this.drawAnimatedCard(context, x, y, width, height, this.primaryColor, 0.6F, alpha);
      if (alpha < 0.3F) return;
      float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
      this.drawGlowText(context, "Optimize", x + 12, y + 12, this.primaryColor, textAlpha);
      int textColor = (int)(textAlpha * 235.0F) << 24 | 0x00FFFFFF;
      int toggleX = x + width - 58;
      String[] titles = new String[]{"Optimize Items", "Optimize Chests", "Optimize Signs", "Optimize Players"};
      String[] descs = new String[]{"Hide dropped items behind walls", "Hide chests hidden by solid blocks", "No sign text past distance", "Hide players behind walls"};
      boolean[] vals = new boolean[]{OptimizeManager.optimizeItems, OptimizeManager.optimizeChests, OptimizeManager.optimizeSigns, OptimizeManager.optimizePlayers};

      for (int i = 0; i < titles.length; i++) {
         int rowY = y + 35 + i * 22;
         GuiStyle.text(context, this.textRenderer, titles[i], x + 12, rowY + 1, textColor);
         int subCol = (int)(textAlpha * 120.0F) << 24 | 0x00FFFFFF;
         int maxW = width - 58 - 12 - 12 - this.textRenderer.getWidth(titles[i]) - 8;
         String sub = descs[i];
         while (this.textRenderer.getWidth(sub) > maxW && sub.length() > 1) {
            sub = sub.substring(0, sub.length() - 1);
         }

         GuiStyle.text(context, this.textRenderer, sub, x + 12 + this.textRenderer.getWidth(titles[i]) + 8, rowY + 1, subCol);
         GuiStyle.toggleSwitch(context, toggleX, rowY, 40, 16, vals[i], vals[i] ? 1.0F : 0.0F, this.primaryColor, textAlpha);
      }

      int sliderY = y + 130;
      String label = "Sign Text Distance: " + OptimizeManager.signTextDistance + " blocks";
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 12, sliderY - 5, textColor);
      float ratio = (OptimizeManager.signTextDistance - 1) / 127.0F;
      boolean sliderHover = GuiStyle.inRect(mouseX, mouseY, x + 12, sliderY + 2, 250, 14);
      GuiStyle.slider(context, x + 12, sliderY + 6, 250, ratio, this.primaryColor, textAlpha, sliderHover);
   }

   private boolean listeningForEditGuiKey = false;

   private void renderAnimatedModuleSettingsCard(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      int height = 140;
      this.drawAnimatedCard(context, x, y, width, height, this.primaryColor, 0.6F, alpha);
      if (!(alpha < 0.3F)) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         this.drawGlowText(context, "Module Options", x + 12, y + 12, this.primaryColor, textAlpha);
         int sliderY = y + 35;
         int sliderWidth = 250;
         int textColor = (int)(alpha * 255.0F) << 24 | 16777215;
         String label = "GUI Animation Duration: " + VoidCyanClient.guiAnimationDurationMs + "ms";
         context.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 12, sliderY - 5, textColor);
         int trackY = sliderY + 8;
         int trackHeight = 6;
         context.fill(x + 12, trackY, x + 12 + sliderWidth, trackY + trackHeight, (int)(alpha * 100.0F) << 24 | 4210752);
         float fillRatio = VoidCyanClient.guiAnimationDurationMs / 2000.0F;
         int fillWidth = (int)(sliderWidth * fillRatio * alpha);
         int fillColor = (int)(alpha * 255.0F) << 24 | this.primaryColor & 16777215;
         context.fill(x + 12, trackY + 1, x + 12 + fillWidth, trackY + trackHeight - 1, fillColor);
         int handleX = x + 12 + fillWidth - 4;
         int handleSize = 8;
         context.fill(handleX, trackY - 1, handleX + handleSize, trackY + trackHeight + 1, (int)(alpha * 255.0F) << 24 | 16777215);
         int scaleSliderY = y + 60;
         String scaleLabel = String.format("Click GUI Scale: %.1fx (syncs with GUI Scale video setting)", VoidCyanClient.clickGuiScale);
         context.drawTextWithShadow(this.textRenderer, Text.literal(scaleLabel), x + 12, scaleSliderY - 5, textColor);
         int scaleTrackY = scaleSliderY + 8;
         context.fill(x + 12, scaleTrackY, x + 12 + sliderWidth, scaleTrackY + trackHeight, (int)(alpha * 100.0F) << 24 | 4210752);
         float scaleFillRatio = (VoidCyanClient.clickGuiScale - 0.5F) / 1.5F;
         int scaleFillWidth = (int)(sliderWidth * scaleFillRatio * alpha);
         context.fill(x + 12, scaleTrackY + 1, x + 12 + scaleFillWidth, scaleTrackY + trackHeight - 1, fillColor);
         int scaleHandleX = x + 12 + scaleFillWidth - 4;
         context.fill(scaleHandleX, scaleTrackY - 1, scaleHandleX + handleSize, scaleTrackY + trackHeight + 1, (int)(alpha * 255.0F) << 24 | 16777215);
         int btnY = y + 85;
         boolean typeHovered = mouseX >= x + 12 && mouseX <= x + 12 + sliderWidth && mouseY >= btnY && mouseY <= btnY + 16;
         int btnColor = typeHovered ? (int)(alpha * 255.0F) << 24 | 3355443 : (int)(alpha * 255.0F) << 24 | 2236962;
         context.fill(x + 12, btnY, x + 12 + sliderWidth, btnY + 16, btnColor);
         String typeLabel = "GUI Type: " + (VoidCyanClient.guiType == 0 ? "Square" : "Dropdown");
         context.drawTextWithShadow(this.textRenderer, Text.literal(typeLabel), x + 16, btnY + 4, textColor);
         // Edit GUI Keybind row
         int keyBtnY = y + 107;
         String keyName = this.listeningForEditGuiKey ? "> PRESS A KEY <" : this.getKeyDisplayName(VoidCyanClient.editGuiKey);
         String keyRowLabel = "Edit GUI Key:";
         context.drawTextWithShadow(this.textRenderer, Text.literal(keyRowLabel), x + 16, keyBtnY + 4, textColor);
         int kbw = Math.max(70, this.textRenderer.getWidth(keyName) + 16);
         int kbx = x + 12 + sliderWidth - kbw;
         boolean keyHovered = mouseX >= kbx && mouseX <= kbx + kbw && mouseY >= keyBtnY && mouseY <= keyBtnY + 16;
         int keyBtnBg = this.listeningForEditGuiKey ? fillColor : (keyHovered ? (int)(alpha * 255.0F) << 24 | 3355443 : (int)(alpha * 255.0F) << 24 | 2236962);
         context.fill(kbx, keyBtnY, kbx + kbw, keyBtnY + 16, keyBtnBg);
         context.fill(kbx, keyBtnY, kbx + kbw, keyBtnY + 1, (int)(alpha * 255.0F) << 24 | this.primaryColor & 0xFFFFFF);
         context.fill(kbx, keyBtnY + 15, kbx + kbw, keyBtnY + 16, (int)(alpha * 255.0F) << 24 | this.primaryColor & 0xFFFFFF);
         context.fill(kbx, keyBtnY, kbx + 1, keyBtnY + 16, (int)(alpha * 255.0F) << 24 | this.primaryColor & 0xFFFFFF);
         context.fill(kbx + kbw - 1, keyBtnY, kbx + kbw, keyBtnY + 16, (int)(alpha * 255.0F) << 24 | this.primaryColor & 0xFFFFFF);
         context.drawTextWithShadow(this.textRenderer, Text.literal(keyName), kbx + (kbw - this.textRenderer.getWidth(keyName)) / 2, keyBtnY + 4, textColor);
      }
   }

   private String getKeyDisplayName(int keyCode) {
      if (keyCode <= 0) return "NONE";
      String name = GLFW.glfwGetKeyName(keyCode, 0);
      if (name != null) return name.toUpperCase();
      switch (keyCode) {
         case 32: return "SPACE";
         case 256: return "ESC";
         case 257: return "ENTER";
         case 258: return "TAB";
         case 259: return "BACKSPACE";
         case 262: return "RIGHT";
         case 263: return "LEFT";
         case 264: return "DOWN";
         case 265: return "UP";
         case 340: return "LSHIFT";
         case 341: return "LCTRL";
         case 342: return "LALT";
         case 344: return "RSHIFT";
         case 345: return "RCTRL";
         case 346: return "RALT";
         default: return "KEY_" + keyCode;
      }
   }

   private void renderAnimatedThemePickerCard(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      int height = 150;
      this.drawAnimatedCard(context, x, y, width, height, this.primaryColor, 0.6F, alpha);
      if (!(alpha < 0.3F)) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         int textColor = (int)(textAlpha * 255.0F) << 24 | 16777215;
         this.drawGlowText(context, "Theme & Colors", x + 12, y + 8, this.primaryColor, textAlpha);

         // 1. RGB Mode Toggle
         int btn1X = x + 12;
         int btn1Y = y + 26;
         int btn1W = 95;
         int btn1H = 16;
         boolean rgbHovered = mouseX >= btn1X && mouseX <= btn1X + btn1W && mouseY >= btn1Y && mouseY <= btn1Y + btn1H;
         int rgbBg = VoidCyanClient.rgbChromaEnabled ? (int)(textAlpha * 180.0F) << 24 | (this.primaryColor & 16777215)
                                                     : (rgbHovered ? (int)(textAlpha * 120.0F) << 24 | 3355443 : (int)(textAlpha * 80.0F) << 24 | 2236962);
         context.fill(btn1X, btn1Y, btn1X + btn1W, btn1Y + btn1H, rgbBg);
         this.drawGlowingBorder(context, btn1X, btn1Y, btn1W, btn1H, this.primaryColor, textAlpha * (VoidCyanClient.rgbChromaEnabled ? 0.8F : 0.4F));
         String rgbText = "1. RGB: " + (VoidCyanClient.rgbChromaEnabled ? "ON" : "OFF");
         context.drawTextWithShadow(this.textRenderer, Text.literal(rgbText), btn1X + 6, btn1Y + 4, textColor);

         // 2. Theme Selector
         int btn2X = x + 115;
         int btn2Y = y + 26;
         int btn2W = 145;
         int btn2H = 16;
         boolean themeHovered = mouseX >= btn2X && mouseX <= btn2X + btn2W && mouseY >= btn2Y && mouseY <= btn2Y + btn2H;
         int themeBg = themeHovered ? (int)(textAlpha * 120.0F) << 24 | 3355443 : (int)(textAlpha * 80.0F) << 24 | 2236962;
         context.fill(btn2X, btn2Y, btn2X + btn2W, btn2Y + btn2H, themeBg);
         this.drawGlowingBorder(context, btn2X, btn2Y, btn2W, btn2H, this.primaryColor, textAlpha * 0.4F);
         String themeText = "2. < " + VoidCyanClient.colorTheme + " >";
         context.drawTextWithShadow(this.textRenderer, Text.literal(themeText), btn2X + 6, btn2Y + 4, textColor);

         // 3. RGB Speed Slider
         int speedY = y + 46;
         String speedLabel = "3. RGB Speed: " + VoidCyanClient.rgbSpeed;
         context.drawTextWithShadow(this.textRenderer, Text.literal(speedLabel), x + 12, speedY + 2, textColor);
         int speedTrackX = x + 95;
         int speedTrackY = speedY + 4;
         int speedTrackW = 165;
         context.fill(speedTrackX, speedTrackY, speedTrackX + speedTrackW, speedTrackY + 6, (int)(textAlpha * 100.0F) << 24 | 4210752);
         float speedRatio = (VoidCyanClient.rgbSpeed - 1) / 9.0F;
         int speedFillW = (int)(speedTrackW * speedRatio);
         context.fill(speedTrackX, speedTrackY + 1, speedTrackX + speedFillW, speedTrackY + 5, (int)(textAlpha * 255.0F) << 24 | (this.primaryColor & 16777215));
         int handleX = speedTrackX + speedFillW - 3;
         context.fill(handleX, speedTrackY - 1, handleX + 6, speedTrackY + 7, (int)(textAlpha * 255.0F) << 24 | 16777215);

         // Picker row: big live swatch (opens the modern modal) + quick preset swatches.
         int pickerX = x + 12;
         int pickerY = y + 68;
         int pickerW = width - 24;
         int pickerH = 60;
         int picked = 0xFF000000 | this.colorPickerR << 16 | this.colorPickerG << 8 | this.colorPickerB;
         int leftHalfW = pickerW / 2 - 4;
         context.fill(pickerX, pickerY, pickerX + leftHalfW, pickerY + pickerH, picked);
         this.drawGlowingBorder(context, pickerX, pickerY, leftHalfW, pickerH, this.primaryColor, textAlpha * 0.5F);
         String pickLbl = "Click to edit";
         GuiStyle.textCentered(context, this.textRenderer, pickLbl, pickerX + leftHalfW / 2, pickerY + pickerH / 2 - 4, (int)(textAlpha * 220.0F) << 24 | 0x00FFFFFF);
         String hint2 = "HEX " + String.format("#%02X%02X%02X", this.colorPickerR, this.colorPickerG, this.colorPickerB);
         GuiStyle.textCentered(context, this.textRenderer, hint2, pickerX + leftHalfW / 2, pickerY + pickerH / 2 + 8, (int)(textAlpha * 150.0F) << 24 | 0x00FFFFFF);
         int half2X = pickerX + pickerW / 2 + 4;
         int half2W = pickerW - pickerW / 2 - 4;
         int presetStart = 0xFF000000 | this.colorPickerR << 16 | this.colorPickerG << 8 | this.colorPickerB;
         int ps = (half2W - 5 * 6) / 6;
         for (int pi = 0; pi < 6; pi++) {
            int pc = ColorPickerModal.PRESETS[pi];
            int px2 = half2X + pi * (ps + 6);
            context.fill(px2, pickerY, px2 + ps, pickerY + pickerH, pc);
            if (pc == presetStart) {
               this.drawGlowingBorder(context, px2, pickerY, ps, pickerH, this.primaryColor, textAlpha * 0.8F);
            }
         }
      }
   }

   private void renderAnimatedOverlayParticlesCard(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      int height = 115;
      this.drawAnimatedCard(context, x, y, width, height, this.secondaryColor, 0.6F, alpha);
      if (!(alpha < 0.3F)) {
         float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
         this.drawGlowText(context, "Display Options", x + 12, y + 12, this.primaryColor, textAlpha);
         this.renderAnimatedSettingsButton(context, mouseX, mouseY, x + 12, y + 40, width - 24, "Overlay Settings ->", textAlpha);
         this.renderAnimatedSettingsButton(context, mouseX, mouseY, x + 12, y + 65, width - 24, "Particle Settings ->", textAlpha);
         this.renderAnimatedSettingsButton(context, mouseX, mouseY, x + 12, y + 90, width - 24, "Visual Settings ->", textAlpha);
      }
   }

   private void renderAnimatedSettingsButton(DrawContext context, int mouseX, int mouseY, int x, int y, int width, String label, float alpha) {
      if (!(alpha <= 0.0F)) {
         boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20;
         if (isHovered) {
            Math.min(1.0F, alpha * 2.0F);
         } else {
            float var10000 = 0.0F;
         }

         int bgColor = isHovered ? (int)(alpha * 100.0F) << 24 | this.primaryColor & 16777215 : (int)(alpha * 50.0F) << 24 | 16777215;
         context.fill(x, y, x + width, y + 20, bgColor);
         int textColor = (int)(alpha * 255.0F) << 24 | 16777215;
         context.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 8, y + 6, textColor);
      }
   }

   private void renderAnimatedFriendsTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      this.drawGlowText(context, "Friends", x, y, this.primaryColor, alpha);
      int settingsY = y + 20;
      int textColor = (int)(alpha * 255.0F) << 24 | 16777215;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Show Green Name Tags"), x, settingsY, textColor);
      drawModuleSwitchStatic(context, x + 140, settingsY, VoidCyanClient.isFriendGreenNameTagsEnabled);
      int searchY = y + 45;
      this.renderAnimatedSearchBar(context, mouseX, mouseY, x, searchY, width - 70, alpha);
      this.renderAnimatedAddButton(context, mouseX, mouseY, x + width - 60, searchY, 50, 30, alpha);
      List<ClickGuiScreen.Friend> filteredFriends = new ArrayList<>();

      for (ClickGuiScreen.Friend friend : this.friends) {
         if (this.searchInput.isEmpty() || friend.name.toLowerCase().contains(this.searchInput.toLowerCase())) {
            filteredFriends.add(friend);
         }
      }

      int friendsTopY = searchY + 40;
      int friendY = friendsTopY + 10 - this.scrollOffset;
      context.enableScissor(x, friendsTopY, x + width, this.height - 10);

      for (int i = 0; i < filteredFriends.size(); i++) {
         ClickGuiScreen.Friend friendx = filteredFriends.get(i);
         float cardProgress = friendx.animationProgress * alpha;
         if (cardProgress > 0.0F && friendY + 35 > friendsTopY && friendY < this.height - 10) {
            this.renderAnimatedFriendCard(context, mouseX, mouseY, x, friendY, width - 20, 35, friendx, cardProgress, i);
         }

         friendY += 45;
      }

      context.disableScissor();
   }

   private void renderAnimatedConfigTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      this.drawGlowText(context, "Config", x, y, this.primaryColor, alpha);
      int cardY = y + 40;
      int cardWidth = Math.min(320, width - 20);
      List<String> namedConfigs = VoidCyanClient.getNamedConfigNames();
      int shownConfigs = Math.min(6, namedConfigs.size());
      int cardHeight = 108 + shownConfigs * 29;
      this.drawAnimatedCard(context, x, cardY, cardWidth, cardHeight, this.primaryColor, 0.6F, alpha);
      if (!(alpha < 0.3F)) {
         int textColor = (int)(alpha * 255.0F) << 24 | 16777215;
         int subduedTextColor = (int)(alpha * 255.0F) << 24 | 11184810;
         context.drawTextWithShadow(this.textRenderer, Text.literal("Named Configs"), x + 12, cardY + 12, textColor);
         context.drawTextWithShadow(this.textRenderer, Text.literal("Type a name, then save or load any preset."), x + 12, cardY + 25, subduedTextColor);
         int buttonX = x + 12;
         int fieldY = cardY + 42;
         int fieldWidth = 178;
         boolean fieldHovered = mouseX >= buttonX && mouseX <= buttonX + fieldWidth && mouseY >= fieldY && mouseY <= fieldY + 25;
         this.drawAnimatedCard(
            context,
            buttonX,
            fieldY,
            fieldWidth,
            25,
            this.isConfigNameFocused ? this.accentColor : this.primaryColor,
            this.isConfigNameFocused ? 1.0F : (fieldHovered ? 0.85F : 0.55F),
            alpha
         );
         String fieldText = this.configNameInput.isEmpty() && !this.isConfigNameFocused ? "Config name..." : this.configNameInput;
         int fieldColor = this.configNameInput.isEmpty() && !this.isConfigNameFocused ? subduedTextColor : textColor;
         context.drawTextWithShadow(this.textRenderer, Text.literal(fieldText), buttonX + 8, fieldY + 8, fieldColor);
         if (this.isConfigNameFocused && System.currentTimeMillis() % 1000L < 500L) {
            int cursorX = buttonX + 8 + this.textRenderer.getWidth(this.configNameInput.substring(0, Math.min(this.textCursor, this.configNameInput.length())));
            context.fill(cursorX, fieldY + 6, cursorX + 1, fieldY + 19, textColor);
         }

         int saveX = buttonX + fieldWidth + 8;
         int buttonWidth = cardWidth - (saveX - x) - 12;
         int buttonHeight = 25;
         boolean hovered = mouseX >= saveX && mouseX <= saveX + buttonWidth && mouseY >= fieldY && mouseY <= fieldY + buttonHeight;
         this.drawAnimatedCard(
            context, saveX, fieldY, buttonWidth, buttonHeight, hovered ? this.primaryColor : this.secondaryColor, hovered ? 1.0F : 0.7F, alpha
         );
         context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Save"), saveX + buttonWidth / 2, fieldY + 8, -1);
         context.drawTextWithShadow(this.textRenderer, Text.literal("Saved configs"), x + 12, cardY + 77, textColor);
         if (namedConfigs.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("No saved configs yet."), x + 12, cardY + 91, subduedTextColor);
         }

         for (int i = 0; i < shownConfigs; i++) {
            String name = namedConfigs.get(i);
            int rowY = cardY + 91 + i * 29;
            context.drawTextWithShadow(this.textRenderer, Text.literal(name), x + 14, rowY + 8, textColor);
            int loadX = x + cardWidth - 148;
            boolean loadHovered = mouseX >= loadX && mouseX <= loadX + 60 && mouseY >= rowY && mouseY <= rowY + 23;
            this.drawAnimatedCard(context, loadX, rowY, 60, 23, loadHovered ? this.primaryColor : this.secondaryColor, loadHovered ? 1.0F : 0.65F, alpha);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Load"), loadX + 30, rowY + 7, -1);
            int delX = loadX + 66;
            boolean delHovered = mouseX >= delX && mouseX <= delX + 60 && mouseY >= rowY && mouseY <= rowY + 23;
            this.drawAnimatedCard(context, delX, rowY, 60, 23, delHovered ? -6741470 : -12316399, delHovered ? 1.0F : 0.65F, alpha);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Delete"), delX + 30, rowY + 7, -21846);
         }

         if (!this.namedConfigStatus.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(this.namedConfigStatus), x + 12, cardY + cardHeight - 12, subduedTextColor);
         }
      }
   }

   private void renderAnimatedSearchBar(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 30;
      float intensity = this.isFriendSearchFocused ? 1.0F : (isHovered ? 0.8F : 0.4F);
      int borderColor = this.isFriendSearchFocused ? this.accentColor : this.primaryColor;
      this.drawAnimatedCard(context, x, y, width, 30, borderColor, intensity, alpha);
      if (alpha > 0.5F) {
         float textAlpha = (alpha - 0.5F) * 2.0F;
         int textColor = (int)(textAlpha * 255.0F) << 24 | (this.searchInput.isEmpty() && !this.isFriendSearchFocused ? 8421504 : 16777215);
         String displayText;
         if (this.isFriendSearchFocused && this.searchInput.isEmpty()) {
            displayText = "";
         } else if (this.searchInput.isEmpty()) {
            displayText = "Search friends...";
         } else {
            displayText = this.searchInput;
         }

         context.drawTextWithShadow(this.textRenderer, Text.literal(displayText), x + 12, y + 11, textColor);
         if (this.isFriendSearchFocused) {
            long time = System.currentTimeMillis();
            if (time % 1000L < 500L) {
               int cursorX = x + 12 + this.textRenderer.getWidth(this.searchInput.substring(0, Math.min(this.textCursor, this.searchInput.length())));
               context.fill(cursorX, y + 8, cursorX + 1, y + 22, textColor);
            }
         }
      }
   }

   private void renderAnimatedAddButton(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, float alpha) {
      boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      float hoverScale = isHovered ? 1.1F : 1.0F;
      int scaledWidth = (int)(width * hoverScale * alpha);
      int scaledHeight = (int)(height * hoverScale * alpha);
      int offsetX = (width - scaledWidth) / 2;
      int offsetY = (height - scaledHeight) / 2;
      this.drawAnimatedCard(context, x + offsetX, y + offsetY, scaledWidth, scaledHeight, this.primaryColor, isHovered ? 1.0F : 0.7F, alpha);
      if (alpha > 0.5F) {
         float textAlpha = (alpha - 0.5F) * 2.0F;
         int textColor = (int)(textAlpha * 255.0F) << 24 | 0;
         context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("+"), x + width / 2, y + height / 2 - 4, textColor);
      }
   }

   private void renderAnimatedFriendCard(
      DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, ClickGuiScreen.Friend friend, float progress, int index
   ) {
      boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      float delayedProgress = Math.max(0.0F, Math.min(1.0F, progress));
      if (!(delayedProgress <= 0.01F)) {
         int cardA = (int)(delayedProgress * 220.0F);
         context.fill(x, y, x + width, y + height, cardA << 24 | 658964);
         int hlA = (int)(delayedProgress * 18.0F);
         context.fill(x + 1, y + 1, x + width - 1, y + 2, hlA << 24 | 16777215);
         this.drawRefinedBorder(context, x, y, width, height, this.primaryColor, delayedProgress * (isHovered ? 0.7F : 0.35F));
         int accentA2 = (int)(delayedProgress * 200.0F);
         context.fill(x + 2, y + 3, x + 5, y + height - 3, accentA2 << 24 | this.primaryColor & 16777215);
         if (delayedProgress > 0.25F) {
            float textAlpha = Math.min(1.0F, (delayedProgress - 0.25F) / 0.75F);
            int textA = (int)(textAlpha * 255.0F);
            String initial = friend.name.isEmpty() ? "?" : String.valueOf(friend.name.charAt(0)).toUpperCase();
            int avatarX = x + 10;
            int avatarY = y + (height - 14) / 2;
            int circleA = (int)(textAlpha * 180.0F);
            context.fill(avatarX, avatarY - 1, avatarX + 16, avatarY + 15, circleA << 24 | this.primaryColor & 16777215);
            context.fill(avatarX + 1, avatarY, avatarX + 15, avatarY + 14, (int)(textAlpha * 255.0F) << 24 | 662056);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(initial), avatarX + 8, avatarY + 3, textA << 24 | this.primaryColor & 16777215);
            context.drawTextWithShadow(this.textRenderer, Text.literal(friend.name), x + 32, y + height / 2 - 4, textA << 24 | 15265525);
            context.drawTextWithShadow(this.textRenderer, Text.literal("friend"), x + 32, y + height / 2 + 6, (int)(textAlpha * 120.0F) << 24 | 5275808);
            this.renderAnimatedRemoveButton(context, mouseX, mouseY, x + width - 30, y + (height - 22) / 2, 22, 22, textAlpha);
         }
      }
   }

   private void renderAnimatedRemoveButton(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, float alpha) {
      boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      float hoverScale = isHovered ? 1.2F : 1.0F;
      int scaledWidth = (int)(width * hoverScale);
      int scaledHeight = (int)(height * hoverScale);
      int offsetX = (width - scaledWidth) / 2;
      int offsetY = (height - scaledHeight) / 2;
      int bgColor = isHovered ? -48060 : -7864320;
      int displayColor = (int)(alpha * 255.0F) << 24 | bgColor & 16777215;
      context.fill(x + offsetX, y + offsetY, x + offsetX + scaledWidth, y + offsetY + scaledHeight, displayColor);
      if (alpha > 0.5F) {
         int textColor = (int)(alpha * 255.0F) << 24 | 16777215;
         context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("×"), x + width / 2, y + height / 2 - 4, textColor);
      }
   }

   private void renderFloatingParticles(DrawContext context) {
      if (this.showParticles) {
         long time = System.currentTimeMillis();

         for (int i = 0; i < 20; i++) {
            float offsetTime = (float)((time + i * 1000) % 15000L);
            float progress = offsetTime / 15000.0F;
            int x = (int)(Math.sin(progress * Math.PI * 2.0 + i) * 100.0 + this.width / 2);
            int y = (int)(Math.cos(progress * Math.PI * 1.5 + i * 0.7) * 50.0 + this.height / 2);
            float alpha = (float)(Math.sin(progress * Math.PI * 4.0) * 0.3 + 0.4);
            int particleAlpha = (int)(alpha * 100.0F);
            if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
               int size = (int)(3.0 + Math.sin(progress * Math.PI * 6.0 + i) * 1.0);
               context.fill(x, y, x + size, y + size, particleAlpha << 24 | this.primaryColor & 16777215);
            }
         }
      }
   }

   private int getMaxScrollOffset() {
      int visibleHeight = this.height - 120;
      if (this.currentTab == 3) {
         int totalHeight = 800;
         return Math.max(0, totalHeight - visibleHeight);
      } else if (this.currentTab == 6) {
         int rowCount = 19;
         int rowHeight = 22;
         int headerHeight = 50;
         int resetButtonHeight = 40;
         int streaksSectionHeight = 60;
         int totalStreaksHeight = 0;
         List<StreakManager.Streak> streaks = StreakManager.streaks;
         if (!streaks.isEmpty()) {
            int streakCardGap = 12;

            for (StreakManager.Streak streak : streaks) {
               boolean isExpanded = this.expandedStreaks.contains(streak.streakNumber);
               int cardHeight = isExpanded ? 280 : 55;
               totalStreaksHeight += cardHeight + streakCardGap;
            }
         } else {
            totalStreaksHeight = 50;
         }

         int totalHeight = headerHeight + rowCount * rowHeight + resetButtonHeight + streaksSectionHeight + totalStreaksHeight;
         return Math.max(0, totalHeight - (this.height - 60));
      } else      if (this.currentTab != 0) {
         if (this.currentTab == 4) {
            int listStartY = 115;
            int totalFriendsHeight = this.friends.size() * 45;
            int visibleFriendsHeight = this.height - listStartY - 20;
            return Math.max(0, totalFriendsHeight - visibleFriendsHeight);
         } else if (this.currentTab == 8) {
            int inputFieldH = 40;
            int resultH = 45;
            int buttonRows = 5;
            int buttonSize = 60;
            int buttonGap = 8;
            int totalButtonsH = buttonRows * (buttonSize + buttonGap);
            int margins = 80;
            int totalHeight = margins + inputFieldH + resultH + totalButtonsH;
            return Math.max(0, totalHeight - (this.height - 60));
         } else {
            int totalHeight = (int)Math.ceil(this.modules.size() / 2.0) * 125;
            return Math.max(0, totalHeight - visibleHeight);
         }
      } else {
         // Modules tab scroll is computed live by renderAnimatedModulesTab into maxScrollOffset.
         return this.maxScrollOffset;
      }
   }

   private float lerp(float start, float end, float progress) {
      return start + (end - start) * progress;
   }

   public boolean charTyped(CharInput input) {
      if (this.colorModal != null && this.colorModal.charTyped(input)) {
         if (!this.colorModal.isOpen()) this.colorModal = null;
         return true;
      }
      int codePoint = input.codepoint();
      if (this.hasTextFocus() && !Character.isISOControl(codePoint)) {
         String typedText = new String(Character.toChars(codePoint));
         if (this.isModuleSearchFocused) {
            this.moduleSearchInput = this.insertString(this.moduleSearchInput, typedText, 64);
            this.scrollOffset = 0;
            this.targetScrollOffset = 0;
         } else if (this.isScreenshotSearchFocused) {
            this.screenshotSearchInput = this.insertString(this.screenshotSearchInput, typedText, 96);
         } else if (this.isFriendSearchFocused) {
            this.searchInput = this.insertString(this.searchInput, typedText, 32);
         } else if (this.isConfigNameFocused) {
            this.configNameInput = this.insertString(this.configNameInput, typedText, 32);
         } else if (this.isCalculatorInputFocused) {
            if (typedText.matches("[0-9+\\-*/.()xX]")) {
               if (typedText.equalsIgnoreCase("x")) {
                  typedText = "*";
               }

               this.calculatorInput = this.insertString(this.calculatorInput, typedText, 128);
            }
         } else if (this.isNoteTitleFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
            NoteManager.notes.get(this.selectedNoteIndex).title = this.insertString(NoteManager.notes.get(this.selectedNoteIndex).title, typedText, 64);
         } else if (this.isNoteContentFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
            String cur = NoteManager.notes.get(this.selectedNoteIndex).content;
            NoteManager.notes.get(this.selectedNoteIndex).content = this.insertString(cur, typedText, 2000);
         }

         return true;
      } else {
         return super.charTyped(input);
      }
   }
   public boolean keyPressed(KeyInput input) {

      if (this.colorModal != null && this.colorModal.keyPressed(input)) {
         if (!this.colorModal.isOpen()) this.colorModal = null;
         return true;
      }
      // Capture Edit GUI keybind
      if (this.listeningForEditGuiKey) {
         int k = input.key();
         if (k == 256) { // ESC cancels, set to NONE
            VoidCyanClient.editGuiKey = -1;
         } else {
            VoidCyanClient.editGuiKey = k;
         }
         VoidCyanClient.saveConfig();
         this.listeningForEditGuiKey = false;
         return true;
      }
      if (!this.hasTextFocus()) {
         return super.keyPressed(input);
      } else {
         int keyCode = input.key();
         if (keyCode == 256) {
            this.clearTextFocus();
            return true;
         } else if (keyCode == 257 && this.isCalculatorInputFocused) {
            try {
               this.calculatorResult = String.valueOf(this.evaluateExpression(this.calculatorInput));
            } catch (Exception var8) {
               this.calculatorResult = "Error";
            }

            return true;
         } else {
            long window = this.client.getWindow().getHandle();
            boolean ctrl = GLFW.glfwGetKey(window, 341) == 1 || GLFW.glfwGetKey(window, 345) == 1;
            if (ctrl) {
               if (keyCode == 65) {
                  this.textCursor = this.getFocusedText().length();
                  return true;
               }

               if (keyCode == 67) {
                  this.client.keyboard.setClipboard(this.getFocusedText());
                  return true;
               }

               if (keyCode == 86) {
                  String clipboard = this.client.keyboard.getClipboard();
                  if (clipboard != null) {
                     if (this.isModuleSearchFocused) {
                        this.moduleSearchInput = this.insertString(this.moduleSearchInput, clipboard, 64);
                     } else if (this.isScreenshotSearchFocused) {
                        this.screenshotSearchInput = this.insertString(this.screenshotSearchInput, clipboard, 96);
                     } else if (this.isFriendSearchFocused) {
                        this.searchInput = this.insertString(this.searchInput, clipboard, 32);
                     } else if (this.isConfigNameFocused) {
                        this.configNameInput = this.insertString(this.configNameInput, clipboard, 32);
                     } else if (this.isCalculatorInputFocused) {
                        this.calculatorInput = this.insertString(this.calculatorInput, clipboard, 128);
                     } else if (this.isNoteTitleFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
                        NoteManager.notes.get(this.selectedNoteIndex).title = this.insertString(
                           NoteManager.notes.get(this.selectedNoteIndex).title, clipboard, 64
                        );
                     } else if (this.isNoteContentFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
                        NoteManager.notes.get(this.selectedNoteIndex).content = this.insertString(
                           NoteManager.notes.get(this.selectedNoteIndex).content, clipboard, 2000
                        );
                     }
                  }

                  return true;
               }

               if (keyCode == 88) {
                  this.client.keyboard.setClipboard(this.getFocusedText());
                  this.setFocusedText("");
                  this.textCursor = 0;
                  return true;
               }
            }

            if (keyCode == 263) {
               this.textCursor = Math.max(0, this.textCursor - 1);
               return true;
            } else if (keyCode == 262) {
               this.textCursor = Math.min(this.getFocusedText().length(), this.textCursor + 1);
               return true;
            } else if (keyCode == 259) {
               if (this.isModuleSearchFocused) {
                  this.moduleSearchInput = this.removeLastCharacter(this.moduleSearchInput);
                  this.scrollOffset = 0;
                  this.targetScrollOffset = 0;
               } else if (this.isScreenshotSearchFocused) {
                  this.screenshotSearchInput = this.removeLastCharacter(this.screenshotSearchInput);
               } else if (this.isFriendSearchFocused) {
                  this.searchInput = this.removeLastCharacter(this.searchInput);
               } else if (this.isConfigNameFocused) {
                  this.configNameInput = this.removeLastCharacter(this.configNameInput);
               } else if (this.isCalculatorInputFocused) {
                  this.calculatorInput = this.removeLastCharacter(this.calculatorInput);
               } else if (this.isNoteTitleFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
                  NoteManager.notes.get(this.selectedNoteIndex).title = this.removeLastCharacter(NoteManager.notes.get(this.selectedNoteIndex).title);
               } else if (this.isNoteContentFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
                  NoteManager.notes.get(this.selectedNoteIndex).content = this.removeLastCharacter(NoteManager.notes.get(this.selectedNoteIndex).content);
               }

               return true;
            } else if (keyCode == 261) {
               String txt = this.getFocusedText();
               if (this.textCursor < txt.length()) {
                  String newStr = txt.substring(0, this.textCursor) + txt.substring(this.textCursor + 1);
                  this.setFocusedText(newStr);
               }

               return true;
            } else if (keyCode != 257 && keyCode != 335) {
               return true;
            } else {
               if (this.isScreenshotSearchFocused) {
                  this.commitScreenshotRename();
               } else if (this.isFriendSearchFocused) {
                  this.addFriendFromInput();
               } else if (this.isConfigNameFocused) {
                  this.saveNamedConfigFromInput();
               } else if (this.isNoteContentFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
                  String cur = NoteManager.notes.get(this.selectedNoteIndex).content;
                  NoteManager.notes.get(this.selectedNoteIndex).content = this.insertString(cur, "\n", 2000);
               } else {
                  this.clearTextFocus();
               }

               return true;
            }
         }
      }
   }

   private boolean hasTextFocus() {
      return this.isModuleSearchFocused
         || this.isScreenshotSearchFocused
         || this.isFriendSearchFocused
         || this.isConfigNameFocused
         || this.isNoteTitleFocused
         || this.isNoteContentFocused
         || this.isCalculatorInputFocused;
   }

   private void clearTextFocus() {
      this.isModuleSearchFocused = false;
      this.isScreenshotSearchFocused = false;
      this.isFriendSearchFocused = false;
      this.isConfigNameFocused = false;
      this.isNoteTitleFocused = false;
      this.isNoteContentFocused = false;
      this.isCalculatorInputFocused = false;
      this.textCursor = 0;
   }

   private String insertString(String original, String insertion, int maxLen) {
      if (original == null) {
         original = "";
      }

      if (insertion == null) {
         return original;
      } else {
         String newStr = original.substring(0, this.textCursor) + insertion + original.substring(this.textCursor);
         if (newStr.length() > maxLen) {
            newStr = newStr.substring(0, maxLen);
         }

         this.textCursor = Math.min(newStr.length(), this.textCursor + insertion.length());
         return newStr;
      }
   }

   private String removeLastCharacter(String value) {
      if (value != null && !value.isEmpty() && this.textCursor > 0) {
         String newStr = value.substring(0, this.textCursor - 1) + value.substring(this.textCursor);
         this.textCursor--;
         return newStr;
      } else {
         return value;
      }
   }

   private String getFocusedText() {
      if (this.isModuleSearchFocused) {
         return this.moduleSearchInput;
      } else if (this.isScreenshotSearchFocused) {
         return this.screenshotSearchInput;
      } else if (this.isFriendSearchFocused) {
         return this.searchInput;
      } else if (this.isConfigNameFocused) {
         return this.configNameInput;
      } else if (this.isCalculatorInputFocused) {
         return this.calculatorInput;
      } else if (this.isNoteTitleFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
         return NoteManager.notes.get(this.selectedNoteIndex).title;
      } else {
         return this.isNoteContentFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()
            ? NoteManager.notes.get(this.selectedNoteIndex).content
            : "";
      }
   }

   private void setFocusedText(String text) {
      if (text == null) {
         text = "";
      }

      if (this.isModuleSearchFocused) {
         this.moduleSearchInput = text;
      } else if (this.isScreenshotSearchFocused) {
         this.screenshotSearchInput = text;
      } else if (this.isFriendSearchFocused) {
         this.searchInput = text;
      } else if (this.isConfigNameFocused) {
         this.configNameInput = text;
      } else if (this.isCalculatorInputFocused) {
         this.calculatorInput = text;
      } else if (this.isNoteTitleFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
         NoteManager.notes.get(this.selectedNoteIndex).title = text;
      } else if (this.isNoteContentFocused && this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
         NoteManager.notes.get(this.selectedNoteIndex).content = text;
      }
   }

   private void addFriendFromInput() {
      String name = this.searchInput.trim();
      if (!name.isEmpty() && !VoidCyanClient.friends.contains(name)) {
         this.friends.add(new ClickGuiScreen.Friend(name));
         VoidCyanClient.friends.add(name);
         VoidCyanClient.saveConfig();
      }

      this.searchInput = "";
      this.isFriendSearchFocused = false;
   }

   private void commitScreenshotRename() {
      ClickGuiScreen.Screenshot screenshot = this.getSelectedScreenshot();
      String requestedName = this.screenshotSearchInput.trim().replaceAll("[\\\\/:*?\"<>|]", "");
      if (screenshot != null && !requestedName.isEmpty()) {
         int extensionIndex = screenshot.originalName.lastIndexOf(46);
         String extension = extensionIndex >= 0 ? screenshot.originalName.substring(extensionIndex) : ".png";
         File renamedFile = new File(screenshot.file.getParentFile(), requestedName + extension);

         try {
            if (!screenshot.file.equals(renamedFile) && !renamedFile.exists()) {
               Files.move(screenshot.file.toPath(), renamedFile.toPath());
               screenshot.name = renamedFile.getName();
               screenshot.originalName = screenshot.name;
               screenshot.editableName = requestedName;
               screenshot.file = renamedFile;
            }

            this.screenshotSearchInput = "";
         } catch (IOException var7) {
         }

         this.isScreenshotSearchFocused = false;
      } else {
         this.isScreenshotSearchFocused = false;
      }
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      GuiScaleManager.update(this.client);
      if (this.colorModal != null) {
         boolean consumed = this.colorModal.mouseClicked(GuiScaleManager.toLogical(click));
         if (!this.colorModal.isOpen()) this.colorModal = null;
         if (consumed) return true;
      }
      double mouseX = GuiScaleManager.toLogical((double)click.x());
      double mouseY = GuiScaleManager.toLogical((double)click.y());
      int button = click.button();
      if (button == 0 || button == 1) {
         int winX = this.windowX;
         int winY = this.windowY;
         int winW = this.windowW;
         int winH = this.windowH;

         // ---- Sidebar interactions (left click only).
         if (button == 0 && mouseX >= winX && mouseX <= winX + GuiStyle.SIDEBAR_W && mouseY >= winY && mouseY <= winY + winH) {
            // Profile card discord link (right half of the card, below the MC line).
            int cardBottom = winY + winH - 12;
            int cardY = cardBottom - 72;
            int cardX = winX + 10;
            int dcTextX = cardX + 48;
            if (GuiStyle.inRect(mouseX, mouseY, dcTextX, cardY + 38, GuiStyle.SIDEBAR_W - 20 - 38, 26)) {
               this.openDiscord();
               return true;
            }

            if (GuiStyle.inRect(mouseX, mouseY, cardX, cardY, GuiStyle.SIDEBAR_W - 20, 72)) {
               return true; // absorb clicks on the rest of the player card
            }

            // Tab rows.
            int tabStartY = winY + this.getSidebarTabStartY();
            int tabSpacing = this.getSidebarTabSpacing(winH);
            int tabH = this.getSidebarTabHeight(winH);
            int tabX = winX + 10;
            if (mouseY >= tabStartY && mouseX >= tabX && mouseX <= tabX + GuiStyle.SIDEBAR_W - 20) {
               int tabIndex = (int)((mouseY - tabStartY) / tabSpacing);
               int relY = (int)((mouseY - tabStartY) % tabSpacing);
               if (tabIndex >= 0 && tabIndex < this.tabNames.length && relY <= tabH) {
                  this.clearTextFocus();
                  if (tabIndex == 9) {
                     this.client.setScreen(new TextureMakerScreen(this));
                     return true;
                  }

                  if (tabIndex != 7) {
                     this.selectedNoteIndex = -1;
                     this.noteContentScrollOffset = 0;
                  }

                  this.targetTab = tabIndex;
                  return true;
               }
            }

            return true; // clicks anywhere else in the sidebar are absorbed
         }

         // ---- Content interactions.
         int contentX = winX + GuiStyle.SIDEBAR_W;
         int contentY = winY;
         int contentWidth = winW - GuiStyle.SIDEBAR_W;
         if (mouseX < contentX || mouseX > winX + winW || mouseY < winY || mouseY > winY + winH) {
            return super.mouseClicked(GuiScaleManager.toLogical(click), doubled);
         }

         // Scrollbar drag (modules tab).
         if (button == 0 && this.currentTab == 0 && this.maxScrollOffset > 0) {
            int innerX = contentX + 10 + 14;
            int innerW = contentWidth - 20 - 28;
            int headerY = winY + 10 + 10 - 2;
            int searchPillW = Math.min(240, innerW / 3);
            int searchPillY = headerY - 3;
            int chipsY = searchPillY + 20 + 8;
            int countY = chipsY + 13;
            int chipsRowY = countY + 13;
            int listStartY = chipsRowY + 22;
            int sbX = innerX + innerW - 2;
            int trackH = winY + winH - 10 - 10 - listStartY;
            if (GuiStyle.inRect(mouseX, mouseY, sbX - 3, listStartY, 8, trackH)) {
               int maxS = this.maxScrollOffset;
               int thumbH = Math.max(24, trackH * trackH / (trackH + maxS));
               int thumbY0 = listStartY + (int)((float)this.scrollOffset / maxS * (trackH - thumbH));
               this.isDraggingScrollbar = true;
               this.dragStartY = (int)mouseY;
               this.dragStartScroll = this.scrollOffset;
               if (mouseY < thumbY0 || mouseY > thumbY0 + thumbH) {
                  int step = Math.max(30, trackH - 30);
                  this.targetScrollOffset = Math.max(0, Math.min(maxS,
                     mouseY < thumbY0 ? this.targetScrollOffset - step : this.targetScrollOffset + step));
                  this.scrollOffset = this.targetScrollOffset;
               }

               return true;
            }
         }

         int innerXc = contentX + 10 + 14;
         int innerYc = contentY + 10 + 10;
         int innerWc = contentWidth - 20 - 28;
         this.sliderContentX = innerXc;
         this.sliderContentY = innerYc;
         this.sliderContentW = innerWc;

         switch (this.currentTab) {
            case 0:
               if (button == 1) {
                  if (this.handleModulesClickRight(button, mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               } else if (this.handleModulesClick(mouseX, mouseY, innerXc, innerYc, innerWc)) {
                  return true;
               }

               break;
            case 1:
               if (button == 0 && this.handleScreenshotsClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 2:
               if (button == 0 && this.handleBackgroundsClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 3:
               if (button == 0 && this.handleGlobalClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 4:
               if (button == 0 && this.handleFriendsClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 5:
               if (button == 0 && this.handleConfigClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 6:
               if (button == 0 && this.handleStatsClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 7:
               if (button == 0 && this.handleNotesClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 8:
               if (button == 0 && this.handleCalculatorClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
            case 10:
               if (button == 0 && this.handlePlayerModelClick(mouseX, mouseY, innerXc, innerYc, innerWc)) return true;
               break;
         }
      }

      return super.mouseClicked(GuiScaleManager.toLogical(click), doubled);
   }

   /** Right-click on a module card opens its settings screen. */
   private boolean handleModulesClickRight(int button, double mouseX, double mouseY, int x, int y, int width) {
      // Right-click intentionally does nothing on module cards; settings open via
      // left-click anywhere except the Reset pill and the toggle switch.
      return false;
   }

   private void openDiscord() {
      try {
         Util.getOperatingSystem().open("https://" + DISCORD_INVITE);
      } catch (Exception ignored) {
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      GuiScaleManager.update(this.client);
      double mouseXx = GuiScaleManager.toLogical(mouseX);
      double mouseYx = GuiScaleManager.toLogical(mouseY);
      if (this.currentTab == 7 && this.selectedNoteIndex >= 0) {
         this.noteContentScrollOffset = Math.max(0, this.noteContentScrollOffset - (int)(verticalAmount * 11.0));
         return true;
      } else if (this.client != null && !this.client.isWindowFocused()) {
         return false;
      } else {
      if (this.currentTab == 0) {
         // Only eat scroll events over the search pill; list scrolling handles the rest.
         int contentX = this.windowX + GuiStyle.SIDEBAR_W;
         int innerX = contentX + 24;
         int innerW = this.windowW - GuiStyle.SIDEBAR_W - 20 - 28;
         int headerY = this.windowY + 20 - 2;
         int searchPillW = Math.min(240, innerW / 3);
         int searchPillX = innerX + innerW - searchPillW;
         int searchPillY = headerY - 3;
         if (mouseXx >= searchPillX && mouseXx <= searchPillX + searchPillW && mouseYx >= searchPillY && mouseYx <= searchPillY + 20) {
            return false;
         }
      }

         int maxScroll = this.getMaxScrollOffset();
         if (maxScroll <= 0) {
            return super.mouseScrolled(mouseXx, mouseYx, horizontalAmount, verticalAmount);
         } else if (verticalAmount == 0.0) {
            return true;
         } else {
            int scrollAmount = (int)Math.round(-Math.signum(verticalAmount) * 30.0);
            this.targetScrollOffset = Math.max(0, Math.min(maxScroll, this.targetScrollOffset + scrollAmount));
            return true;
         }
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      GuiScaleManager.update(this.client);
      if (this.colorModal != null && this.colorModal.mouseDragged(GuiScaleManager.toLogical(click))) {
         return true;
      }
      double my = GuiScaleManager.toLogical((double)click.y());
      if (this.isDraggingScrollbar && this.currentTab == 0) {
         int contentX = this.windowX + GuiStyle.SIDEBAR_W;
         int innerX = contentX + 24;
         int innerW = this.windowW - GuiStyle.SIDEBAR_W - 20 - 28;
         int headerY = this.windowY + 20 - 2;
         int searchPillY = headerY - 3;
         int chipsY = searchPillY + 20 + 8;
         int countY = chipsY + 13;
         int chipsRowY = countY + 13;
         int listStartY = chipsRowY + 22;
         int trackH = this.windowY + this.windowH - 20 - listStartY;
         int maxS = this.maxScrollOffset;
         if (maxS > 0 && trackH > 24) {
            int thumbH = Math.max(24, trackH * trackH / (trackH + maxS));
            int trackRange = trackH - thumbH;
            int delta = (int)my - this.dragStartY;
            int newScroll = this.dragStartScroll + delta * maxS / trackRange;
            newScroll = Math.max(0, Math.min(maxS, newScroll));
            this.targetScrollOffset = newScroll;
            this.scrollOffset = newScroll;
            return true;
         }
      }

      if (this.draggingSlider == null) {
         return super.mouseDragged(GuiScaleManager.toLogical(click), offsetX, offsetY);
      } else {
         double mx = GuiScaleManager.toLogical((double)click.x());
         int x = this.sliderContentX;
         int y = this.sliderContentY;
         int width = this.sliderContentW;
         String sbY = this.draggingSlider;
         switch (sbY) {
            case "opacity":
               int listWidth = (width - 60) / 2;
               int previewX = x + 20 + listWidth + 20;
               int controlsWidth = width - listWidth - 60;
               int trackX = previewX + 80;
               int trackWidth = controlsWidth - 80 - 50;
               this.backgroundOpacity = (float)(mx - trackX) / trackWidth;
               this.backgroundOpacity = Math.max(0.0F, Math.min(1.0F, this.backgroundOpacity));
               VoidCyanClient.guiBgOpacity = (int)(this.backgroundOpacity * 255.0F);
               this.saveOnRelease = true;
               break;
            case "rgbspeed": {
               int speedTrackX = x + 95;
               int speedTrackW = 165;
               float r = Math.max(0.0F, Math.min(1.0F, (float)(mx - speedTrackX) / speedTrackW));
               VoidCyanClient.rgbSpeed = 1 + Math.round(r * 9.0F);
               this.saveOnRelease = true;
               break;
            }
            case "red": {
               int cardY = y + 40 - this.scrollOffset;
               int sliderBaseX = x + 12 + 46;
               int sliderW = 180;
               this.colorPickerR = (int)((mx - sliderBaseX) / sliderW * 255.0);
               this.colorPickerR = Math.max(0, Math.min(255, this.colorPickerR));
               VoidCyanClient.primaryColorR = this.colorPickerR;
               this.updateThemeColors();
               break;
            }
            case "green": {
               int sliderBaseX = x + 12 + 46;
               int sliderW = 180;
               this.colorPickerG = (int)((mx - sliderBaseX) / sliderW * 255.0);
               this.colorPickerG = Math.max(0, Math.min(255, this.colorPickerG));
               VoidCyanClient.primaryColorG = this.colorPickerG;
               this.updateThemeColors();
               break;
            }
            case "blue": {
               int sliderBaseX = x + 12 + 46;
               int sliderW = 180;
               this.colorPickerB = (int)((mx - sliderBaseX) / sliderW * 255.0);
               this.colorPickerB = Math.max(0, Math.min(255, this.colorPickerB));
               VoidCyanClient.primaryColorB = this.colorPickerB;
               this.updateThemeColors();
               break;
            }
            case "anim": {
               int sliderBaseX = x + 12;
               int sliderW = 250;
               double norm = (mx - sliderBaseX) / sliderW;
               norm = Math.max(0.0, Math.min(1.0, norm));
               VoidCyanClient.guiAnimationDurationMs = (int)(norm * 2000.0);
               this.saveOnRelease = true;
               break;
            }
            case "scale": {
               int sliderBaseX = x + 12;
               int sliderW = 250;
               double norm = (mx - sliderBaseX) / sliderW;
               norm = Math.max(0.0, Math.min(1.0, norm));
               float newScale = 0.5F + (float)norm * 1.5F;
               newScale = Math.round(newScale * 10.0F) / 10.0F;
               VoidCyanClient.clickGuiScale = newScale;
               this.saveOnRelease = true;
               break;
            }
            case "signdist": {
               int sliderBaseX = x + 12;
               int sliderW = 250;
               double norm = (mx - sliderBaseX) / sliderW;
               norm = Math.max(0.0, Math.min(1.0, norm));
               OptimizeManager.signTextDistance = 1 + (int)Math.round(norm * 127.0);
               this.saveOnRelease = true;
            }
         }

         return true;
      }
   }

   public boolean mouseReleased(Click click) {
      if (this.colorModal != null) {
         this.colorModal.mouseReleased();
         if (!this.colorModal.isOpen()) this.colorModal = null;
      }
      this.draggingSlider = null;
      this.isDraggingScrollbar = false;
      // Slider drags used to write the config on every mouse move; persist once when the drag ends instead.
      if (this.saveOnRelease) {
         this.saveOnRelease = false;
         VoidCyanClient.saveConfig();
      }
      return super.mouseReleased(GuiScaleManager.toLogical(click));
   }

   private boolean handleModulesClick(double mouseX, double mouseY, int x, int y, int width) {
      // Header geometry must mirror renderAnimatedModulesTab exactly.
      int headerY = y - 2;
      int searchPillW = Math.min(240, width / 3);
      int searchPillH = 20;
      int searchPillX = x + width - searchPillW;
      int searchPillY = headerY - 3;
      int clearX = searchPillX + searchPillW - 14;
      int azW = GuiStyle.chipWidth(this.textRenderer, "A-Z");
      int enW = GuiStyle.chipWidth(this.textRenderer, "Enabled");
      int azX = searchPillX - azW - 8;
      int enX = azX - enW - 6;

      // A-Z chip.
      if (GuiStyle.inRect(mouseX, mouseY, azX, searchPillY, azW, searchPillH)) {
         this.sortAZ = !this.sortAZ;
         this.scrollOffset = 0;
         this.targetScrollOffset = 0;
         return true;
      }

      // Enabled chip.
      if (GuiStyle.inRect(mouseX, mouseY, enX, searchPillY, enW, searchPillH)) {
         this.filterEnabled = !this.filterEnabled;
         this.scrollOffset = 0;
         this.targetScrollOffset = 0;
         return true;
      }

      // Search pill.
      if (GuiStyle.inRect(mouseX, mouseY, searchPillX, searchPillY, searchPillW, searchPillH)) {
         if (!this.moduleSearchInput.isEmpty()
               && mouseX >= clearX && mouseX <= clearX + 10 && mouseY >= searchPillY + 5 && mouseY <= searchPillY + 15) {
            this.moduleSearchInput = "";
            this.scrollOffset = 0;
            this.targetScrollOffset = 0;
            return true;
         }

         this.isModuleSearchFocused = true;
         this.textCursor = this.moduleSearchInput.length();
         return true;
      }

      this.isModuleSearchFocused = false;

      // Category chips row.
      int chipsY = searchPillY + searchPillH + 8;
      int chipsRowY = chipsY;
      int chipCursor = x;

      for (int ci = 0; ci < MODULE_CATEGORIES.length; ci++) {
         String cat = MODULE_CATEGORIES[ci];
         int cw = GuiStyle.chipWidth(this.textRenderer, cat);
         if (GuiStyle.inRect(mouseX, mouseY, chipCursor, chipsRowY, cw, 16)) {
            this.selectedModuleCategory = this.selectedModuleCategory.equals(cat) ? "All" : cat;
            this.scrollOffset = 0;
            this.targetScrollOffset = 0;
            return true;
         }

         chipCursor += cw + 6;
      }

      // Module cards.
      int listStartY = chipsRowY + 22;
      int cardGap = 8;
      int cardH = 44;
      int cardW = width;
      if (mouseY < listStartY) return false;

      for (ClickGuiScreen.ModuleInfo module : this.getVisibleModules()) {
         int cX = (int)module.currentX;
         int cY = (int)module.currentY;
         if (mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH) {
            // Reset pill.
            if (module.resetHudPosition != null) {
               int rW = 42;
               int rH = 16;
               int rX = cX + cardW - rW - 154;
               int rY = cY + (cardH - rH) / 2;
               if (GuiStyle.inRect(mouseX, mouseY, rX, rY, rW, rH)) {
                  module.resetHudPosition.run();
                  return true;
               }
            }

            // Toggle switch region: only clicking the switch itself toggles.
            int tglX = cX + cardW - 58;
            int tglY = cY + (cardH - 16) / 2;
            if (GuiStyle.inRect(mouseX, mouseY, tglX, tglY, 40, 16)) {
               if (VoidCyanClient.isSafeModeBlocked(module.name) && !module.enabled) {
                  VoidCyanClient.showSafeModeBlockedNotification(module.name);
                  return true;
               }

               module.toggle.run();
               module.enabled = !module.enabled;
               VoidCyanClient.markConfigDirty();
               return true;
            }

            // Anywhere else on the card opens its settings (falls back to toggle
            // for modules without a settings screen).
            if (module.openSettings != null) {
               module.openSettings.run();
            } else {
               if (VoidCyanClient.isSafeModeBlocked(module.name) && !module.enabled) {
                  VoidCyanClient.showSafeModeBlockedNotification(module.name);
               } else {
                  module.toggle.run();
                  module.enabled = !module.enabled;
                  VoidCyanClient.markConfigDirty();
               }
            }

            return true;
         }
      }

      return false;
   }

   private boolean handleScreenshotsClick(double mouseX, double mouseY, int x, int y, int width) {
      if (this.screenshots.isEmpty()) {
         return false;
      } else {
         ClickGuiScreen.Screenshot selectedScreenshot = this.getSelectedScreenshot();
         if (selectedScreenshot == null) {
            return false;
         } else {
            int maxAvailableHeight = this.height - y - 20;
            if (maxAvailableHeight < 150) {
               maxAvailableHeight = 150;
            }

            // Must match renderAnimatedScreenshotsTab exactly.
            int listX = x + 20;
            int listY = y + 30;
            int listWidth = (width - 60) * 3 / 8;
            int listHeight = maxAvailableHeight - 30;
            int previewX = listX + listWidth + 16;
            int previewY = listY;
            int previewWidth = width - listWidth - 16 - 40;
            int searchBarHeight = 28;
            int buttonHeight = 28;
            int controlsTotalHeight = searchBarHeight + 8 + buttonHeight + 8 + buttonHeight;
            int previewHeight = Math.max(80, listHeight - controlsTotalHeight);
            int searchBarY = previewY + previewHeight + 8;
            int copyButtonY = searchBarY + searchBarHeight + 8;
            int deleteButtonY = copyButtonY + buttonHeight + 8;
            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
               int itemHeight = 35;
               int itemY = listY + 40;
               int itemSpacing = 5;

               for (int i = 0; i < this.screenshots.size() && i < 10; i++) {
                  if (itemY + itemHeight <= listY + listHeight - 10 && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                     long currentTime = System.currentTimeMillis();
                     ClickGuiScreen.Screenshot clicked = this.screenshots.get(i);
                     if (i == this.lastClickedScreenshotIndex && currentTime - this.lastScreenshotClickTime < 500L) {
                        this.screenshotSearchInput = clicked.editableName;
                        this.isScreenshotSearchFocused = true;
                     } else if (i != this.lastClickedScreenshotIndex) {
                        this.screenshotSearchInput = clicked.editableName;
                     }

                     this.lastScreenshotClickTime = currentTime;
                     this.lastClickedScreenshotIndex = i;
                     this.screenshots.remove(i);
                     this.screenshots.add(0, clicked);
                     return true;
                  }

                  itemY += itemHeight + itemSpacing;
               }
            }

            if (mouseX >= previewX && mouseX <= previewX + previewWidth && mouseY >= searchBarY && mouseY <= searchBarY + searchBarHeight) {
               if (!this.isScreenshotSearchFocused && this.screenshotSearchInput.isEmpty()) {
                  this.screenshotSearchInput = selectedScreenshot.editableName;
               }

               this.isScreenshotSearchFocused = true;
               return true;
            } else {
               boolean clickingButtons = mouseX >= previewX
                  && mouseX <= previewX + previewWidth
                  && mouseY >= searchBarY
                  && mouseY <= deleteButtonY + buttonHeight;
               if (!clickingButtons) {
                  this.isScreenshotSearchFocused = false;
               }

               if (mouseX >= previewX && mouseX <= previewX + previewWidth && mouseY >= copyButtonY && mouseY <= copyButtonY + buttonHeight) {
                  try {
                     File bgFolder = new File(MinecraftClient.getInstance().runDirectory, "background");
                     if (!bgFolder.exists()) {
                        bgFolder.mkdirs();
                     }

                     Files.copy(
                        selectedScreenshot.file.toPath(), new File(bgFolder, selectedScreenshot.file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING
                     );
                     VoidCyanClient.activeNotifications.add(new Notification("Copied " + selectedScreenshot.name + " to backgrounds!", 3000L));
                     this.screenshotSearchInput = "COPIED!";
                     this.isScreenshotSearchFocused = false;
                     this.initBackgroundFiles();
                  } catch (Exception var37) {
                     var37.printStackTrace();
                     this.screenshotSearchInput = "FAILED TO COPY!";
                  }

                  return true;
               } else if (mouseX >= previewX && mouseX <= previewX + previewWidth && mouseY >= deleteButtonY && mouseY <= deleteButtonY + buttonHeight) {
                  if (selectedScreenshot.file.delete()) {
                     this.screenshots.remove(selectedScreenshot);
                     this.screenshotSearchInput = "";
                     this.isScreenshotSearchFocused = false;
                     VoidCyanClient.activeNotifications.add(new Notification("Deleted screenshot", 3000L));
                  }

                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   private boolean handleBackgroundsClick(double mouseX, double mouseY, int x, int y, int width) {
      int buttonY = y + 20;
      int buttonSpacing = 10;
      int buttonWidth = Math.min(120, (width - buttonSpacing) / 2);
      int buttonHeight = 25;
      if (mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
         if (mouseX >= x && mouseX <= x + buttonWidth) {
            VoidCyanClient.clickGuiBgEnabled = !VoidCyanClient.clickGuiBgEnabled;
            VoidCyanClient.saveConfig();
            return true;
         }

         if (mouseX >= x + buttonWidth + buttonSpacing && mouseX <= x + buttonWidth * 2 + buttonSpacing) {
            VoidCyanClient.editGuiBgEnabled = !VoidCyanClient.editGuiBgEnabled;
            VoidCyanClient.saveConfig();
            return true;
         }
      }

      int row2Y = buttonY + buttonHeight + buttonSpacing;
      if (mouseY >= row2Y && mouseY <= row2Y + buttonHeight) {
         if (mouseX >= x && mouseX <= x + buttonWidth) {
            try {
               Desktop.getDesktop().open(new File(MinecraftClient.getInstance().runDirectory, "background"));
            } catch (Exception var30) {
            }

            return true;
         }

         if (mouseX >= x + buttonWidth + buttonSpacing && mouseX <= x + buttonWidth * 2 + buttonSpacing) {
            this.initBackgroundFiles();

            for (NativeImageBackedTexture tex : this.backgroundTextureObjects.values()) {
               try {
                  tex.close();
               } catch (Exception var32) {
               }
            }

            for (AnimatedTexture tex : this.previewAnimatedTextures.values()) {
               try {
                  tex.close();
               } catch (Exception var31) {
               }
            }

            this.previewAnimatedTextures.clear();
            this.backgroundTextures.clear();
            this.backgroundDimensions.clear();
            this.backgroundTextureObjects.clear();
            this.selectedBackgroundIndex = -1;
            return true;
         }
      }

      if (this.backgroundFiles.isEmpty()) {
         return false;
      } else {
         int maxAvailableHeight = this.height - y - 20;
         if (maxAvailableHeight < 150) {
            maxAvailableHeight = 150;
         }

         int listX = x + 20;
         int listY = y + 90;
         int listWidth = (width - 60) / 2;
         int listHeight = maxAvailableHeight - 90;
         int previewX = listX + listWidth + 20;
         int previewWidth = width - listWidth - 60;
         int controlsTotalHeight = 90;
         int previewHeight = listHeight - controlsTotalHeight - 10;
         if (previewHeight < 50) {
            previewHeight = 50;
         }

         int controlsY = listY + previewHeight + 10;
         if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
            int itemHeight = 25;
            int itemY = listY + 40;
            int itemSpacing = 2;

            for (int i = 0; i < this.backgroundFiles.size() && i < 15; i++) {
               if (itemY + itemHeight <= listY + listHeight - 10 && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                  this.selectedBackgroundIndex = i;
                  return true;
               }

               itemY += itemHeight + itemSpacing;
            }
         }

         if (mouseX >= previewX && mouseX <= previewX + previewWidth) {
            int trackX = previewX + 80;
            int trackWidth = previewWidth - 80 - 50;
            if (mouseY >= controlsY && mouseY <= controlsY + 25 && mouseX >= trackX && mouseX <= trackX + trackWidth) {
               this.draggingSlider = "opacity";
               this.backgroundOpacity = (float)(mouseX - trackX) / trackWidth;
               this.backgroundOpacity = Math.max(0.0F, Math.min(1.0F, this.backgroundOpacity));
               VoidCyanClient.guiBgOpacity = (int)(this.backgroundOpacity * 255.0F);
               VoidCyanClient.saveConfig();
               return true;
            }

            int setBgY = controlsY + 40;
            if (mouseX >= previewX && mouseX <= previewX + previewWidth && mouseY >= setBgY && mouseY <= setBgY + 35) {
               if (this.selectedBackgroundIndex >= 0 && this.selectedBackgroundIndex < this.backgroundFiles.size()) {
                  ClickGuiScreen.BackgroundFile selected = this.backgroundFiles.get(this.selectedBackgroundIndex);
                  VoidCyanClient.guiBackgroundImagePath = selected.file.getAbsolutePath();
                  VoidCyanClient.saveConfig();
                  VoidCyanClient.updateMainBackground();
               }

               return true;
            }
         }

         return false;
      }
   }

   private boolean handleGlobalClick(double mouseX, double mouseY, int x, int y, int width) {
      int cardY = y + 40 - this.scrollOffset;

      // 1. RGB Mode toggle
      int btn1X = x + 12;
      int btn1Y = cardY + 26;
      int btn1W = 95;
      int btn1H = 16;
      if (mouseX >= btn1X && mouseX <= btn1X + btn1W && mouseY >= btn1Y && mouseY <= btn1Y + btn1H) {
         VoidCyanClient.rgbChromaEnabled = !VoidCyanClient.rgbChromaEnabled;
         VoidCyanClient.saveConfig();
         this.updateThemeColors();
         return true;
      }

      // 2. Theme Selector
      int btn2X = x + 115;
      int btn2Y = cardY + 26;
      int btn2W = 145;
      int btn2H = 16;
      if (mouseX >= btn2X && mouseX <= btn2X + btn2W && mouseY >= btn2Y && mouseY <= btn2Y + btn2H) {
         int currentIdx = 0;
         for (int ti = 0; ti < VoidCyanClient.THEMES.length; ti++) {
            if (VoidCyanClient.THEMES[ti].equalsIgnoreCase(VoidCyanClient.colorTheme)) {
               currentIdx = ti;
               break;
            }
         }
         if (mouseX < btn2X + 25) {
            currentIdx = (currentIdx - 1 + VoidCyanClient.THEMES.length) % VoidCyanClient.THEMES.length;
         } else {
            currentIdx = (currentIdx + 1) % VoidCyanClient.THEMES.length;
         }
         VoidCyanClient.applyTheme(VoidCyanClient.THEMES[currentIdx]);
         this.colorPickerR = VoidCyanClient.primaryColorR;
         this.colorPickerG = VoidCyanClient.primaryColorG;
         this.colorPickerB = VoidCyanClient.primaryColorB;
         this.updateThemeColors();
         return true;
      }

      // 3. RGB Speed Slider
      int speedY = cardY + 46;
      int speedTrackX = x + 95;
      int speedTrackW = 165;
      if (mouseX >= speedTrackX && mouseX <= speedTrackX + speedTrackW && mouseY >= speedY && mouseY <= speedY + 14) {
         this.draggingSlider = "rgbspeed";
         float r = Math.max(0.0F, Math.min(1.0F, (float)(mouseX - speedTrackX) / speedTrackW));
         VoidCyanClient.rgbSpeed = 1 + Math.round(r * 9.0F);
         VoidCyanClient.saveConfig();
         return true;
      }

      int sliderY = cardY + 68;
      // Color picker area: big swatch opens the modal; quick preset chips apply instantly.
      int pickerY = cardY + 68;
      int pickerW = width - 24;
      int pickerH = 60;
      int leftHalfW = pickerW / 2 - 4;
      if (mouseX >= x + 12 && mouseX <= x + 12 + leftHalfW && mouseY >= pickerY && mouseY <= pickerY + pickerH) {
         this.openThemePicker();
         return true;
      }

      int half2Xc = x + 12 + pickerW / 2 + 4;
      int half2Wc = pickerW - pickerW / 2 - 4;
      int psW = (half2Wc - 5 * 6) / 6;
      boolean onPreset = false;
      if (mouseX >= half2Xc && mouseX <= half2Xc + half2Wc && mouseY >= pickerY && mouseY <= pickerY + pickerH) {
         int pi = (int)((mouseX - half2Xc) / (psW + 6));
         if (pi >= 0 && pi < 6) {
            int pc = ColorPickerModal.PRESETS[pi] & 0x00FFFFFF;
            this.colorPickerR = pc >> 16 & 0xFF;
            this.colorPickerG = pc >> 8 & 0xFF;
            this.colorPickerB = pc & 0xFF;
            VoidCyanClient.colorTheme = "Custom";
            VoidCyanClient.primaryColorR = this.colorPickerR;
            VoidCyanClient.primaryColorG = this.colorPickerG;
            VoidCyanClient.primaryColorB = this.colorPickerB;
            this.updateThemeColors();
            VoidCyanClient.saveConfig();
            onPreset = true;
            return true;
         }
      }

      {
         int moduleCardY = cardY + 165;
               if (mouseX >= x + 12 && mouseX <= x + 262 && mouseY >= moduleCardY + 85 && mouseY <= moduleCardY + 101) {
                  VoidCyanClient.guiType = VoidCyanClient.guiType == 0 ? 1 : 0;
                  VoidCyanClient.saveConfig();
                  if (VoidCyanClient.guiType == 1 && this.client != null) {
                     this.client.setScreen(new DropdownGuiScreen());
                  }

                  return true;
               } else {
                  // Edit GUI Key button (row at moduleCardY + 107)
                  int kbw2 = Math.max(70, this.textRenderer.getWidth(this.getKeyDisplayName(VoidCyanClient.editGuiKey)) + 16);
                  int kbx2 = x + 12 + 250 - kbw2;
                  if (mouseX >= kbx2 && mouseX <= kbx2 + kbw2 && mouseY >= moduleCardY + 107 && mouseY <= moduleCardY + 123) {
                     this.listeningForEditGuiKey = !this.listeningForEditGuiKey;
                     return true;
                  } else {
                  int toggleCardY = moduleCardY + 125;
                  if (mouseX >= x + 12 && mouseX <= x + width - 12 && mouseY >= toggleCardY + 40 && mouseY <= toggleCardY + 60) {
                     this.client.setScreen(new OverlaysSettingsScreen(this));
                     return true;
                  } else if (mouseX >= x + 12 && mouseX <= x + width - 12 && mouseY >= toggleCardY + 65 && mouseY <= toggleCardY + 85) {
                     this.client.setScreen(new ParticlesSettingsScreen(this));
                     return true;
                  } else if (mouseX >= x + 12 && mouseX <= x + width - 12 && mouseY >= toggleCardY + 90 && mouseY <= toggleCardY + 110) {
                     this.client.setScreen(new VisualSettingsScreen(this));
                     return true;
                  } else {
                     int animSliderY = moduleCardY + 35;
                     if (mouseX >= x + 12 && mouseX <= x + 12 + 250 && mouseY >= animSliderY && mouseY <= animSliderY + 15) {
                        this.draggingSlider = "anim";
                        double norm = (mouseX - x - 12.0) / 250.0;
                        norm = Math.max(0.0, Math.min(1.0, norm));
                        VoidCyanClient.guiAnimationDurationMs = (int)(norm * 2000.0);
                        VoidCyanClient.saveConfig();
                        return true;
                     } else {
                        int scaleSliderY = moduleCardY + 60;
                        if (mouseX >= x + 12 && mouseX <= x + 12 + 250 && mouseY >= scaleSliderY && mouseY <= scaleSliderY + 15) {
                           this.draggingSlider = "scale";
                           double norm = (mouseX - x - 12.0) / 250.0;
                           norm = Math.max(0.0, Math.min(1.0, norm));
                           float newScale = 0.5F + (float)norm * 1.5F;
                           newScale = Math.round(newScale * 10.0F) / 10.0F;
                           VoidCyanClient.clickGuiScale = newScale;
                           VoidCyanClient.saveConfig();
                           return true;                     } else {
                        return this.handleOptimizeClick(mouseX, mouseY, x, width, cardY)
                           || this.handleCritEffectsClick(mouseX, mouseY, x, width, cardY)
                           || this.handleSafeModeClick(mouseX, mouseY, x, width, cardY);
                     }
              }
                      }
                   }
                }
             }
    }

   private void openThemePicker() {
      int argb = 0xFF000000 | this.colorPickerR << 16 | this.colorPickerG << 8 | this.colorPickerB;
      this.clearTextFocus();
      this.colorModal = new ColorPickerModal(
         this.windowX + this.windowW / 2,
         this.windowY + this.windowH / 2,
         argb,
         "Theme & Colors",
         "Pick the client theme color",
         applied -> {
            this.colorPickerR = applied >> 16 & 0xFF;
            this.colorPickerG = applied >> 8 & 0xFF;
            this.colorPickerB = applied & 0xFF;
            VoidCyanClient.colorTheme = "Custom";
            VoidCyanClient.primaryColorR = this.colorPickerR;
            VoidCyanClient.primaryColorG = this.colorPickerG;
            VoidCyanClient.primaryColorB = this.colorPickerB;
            this.updateThemeColors();
            VoidCyanClient.saveConfig();
         }
      );
   }

      private boolean handleCritEffectsClick(double mouseX, double mouseY, int x, int width, int cardY) {
         int cardW = width - 20;
         int critY = cardY + 420 + 176;
         int toggleX = x + cardW - 58;
         if (GuiStyle.inRect(mouseX, mouseY, toggleX, critY + 12, 40, 16)) {
            CritEffectsManager.enabled = !CritEffectsManager.enabled;
            VoidCyanClient.saveConfig();
            return true;
         }

         int btnW = 110;
         int btnX = x + cardW - 12 - btnW;
         if (GuiStyle.inRect(mouseX, mouseY, btnX, critY + 38, btnW, 16)) {
            CritEffectsManager.reload();
            File dir = CritEffectsManager.getEffectsDir();
            if (!dir.exists()) dir.mkdirs();
            try {
               Util.getOperatingSystem().open(dir);
            } catch (Exception ignored) {
            }
            return true;
         }

         return false;
      }

   private boolean handleOptimizeClick(double mouseX, double mouseY, int x, int width, int cardY) {
      int cardW = width - 20;
      int optY = cardY + 420;
      int toggleX = x + cardW - 58;
      int[] rows = new int[]{35, 57, 79, 101};

      for (int i = 0; i < rows.length; i++) {
         if (GuiStyle.inRect(mouseX, mouseY, toggleX, optY + rows[i], 40, 16)) {
            if (i == 0) {
               OptimizeManager.optimizeItems = !OptimizeManager.optimizeItems;
            } else if (i == 1) {
               OptimizeManager.optimizeChests = !OptimizeManager.optimizeChests;
            } else if (i == 2) {
               OptimizeManager.optimizeSigns = !OptimizeManager.optimizeSigns;
            } else {
               OptimizeManager.optimizePlayers = !OptimizeManager.optimizePlayers;
            }

            VoidCyanClient.saveConfig();
            OptimizeManager.clearCache();
            return true;
         }
      }

      int trackY = optY + 130;
      if (GuiStyle.inRect(mouseX, mouseY, x + 12, trackY - 2, 250, 14)) {
         this.draggingSlider = "signdist";
         double norm = Math.max(0.0, Math.min(1.0, (mouseX - x - 12.0) / 250.0));
         OptimizeManager.signTextDistance = 1 + (int)Math.round(norm * 127.0);
         VoidCyanClient.saveConfig();
         return true;
      }

      return false;
   }

   private boolean handleFriendsClick(double mouseX, double mouseY, int x, int y, int width) {
      int settingsY = y + 20;
      if (mouseX >= x + 140 && mouseX <= x + 170 && mouseY >= settingsY && mouseY <= settingsY + 12) {
         VoidCyanClient.isFriendGreenNameTagsEnabled = !VoidCyanClient.isFriendGreenNameTagsEnabled;
         VoidCyanClient.saveConfig();
         return true;
      } else {
         int searchY = y + 45;
         if (mouseX >= x && mouseX <= x + width - 70 && mouseY >= searchY && mouseY <= searchY + 30) {
            this.isFriendSearchFocused = true;
            this.textCursor = this.searchInput.length();
            return true;
         } else {
            this.isFriendSearchFocused = false;
            if (mouseX >= x + width - 60 && mouseX <= x + width - 10 && mouseY >= searchY && mouseY <= searchY + 30) {
               if (!this.searchInput.trim().isEmpty()) {
                  ClickGuiScreen.Friend newFriend = new ClickGuiScreen.Friend(this.searchInput.trim());
                  this.friends.add(newFriend);
                  VoidCyanClient.friends.add(newFriend.name);
                  VoidCyanClient.saveConfig();
                  this.searchInput = "";
               }

               return true;
            } else {
               int friendY = searchY + 50;

               for (int i = 0; i < this.friends.size(); i++) {
                  if (mouseX >= x + width - 50 && mouseX <= x + width - 25 && mouseY >= friendY + 5 && mouseY <= friendY + 30) {
                     VoidCyanClient.friends.remove(this.friends.get(i).name);
                     this.friends.remove(i);
                     VoidCyanClient.saveConfig();
                     return true;
                  }

                  friendY += 45;
               }

               return false;
            }
         }
      }
   }

   private boolean handleConfigClick(double mouseX, double mouseY, int x, int y, int width) {
      int cardY = y + 40;
      int cardWidth = Math.min(320, width - 20);
      int buttonX = x + 12;
      int fieldY = cardY + 42;
      int fieldWidth = 178;
      if (mouseX >= buttonX && mouseX <= buttonX + fieldWidth && mouseY >= fieldY && mouseY <= fieldY + 25) {
         this.clearTextFocus();
         this.isConfigNameFocused = true;
         this.textCursor = this.configNameInput.length();
         return true;
      } else {
         int saveX = buttonX + fieldWidth + 8;
         int saveWidth = cardWidth - (saveX - x) - 12;
         if (mouseX >= saveX && mouseX <= saveX + saveWidth && mouseY >= fieldY && mouseY <= fieldY + 25) {
            this.saveNamedConfigFromInput();
            return true;
         } else {
            List<String> namedConfigs = VoidCyanClient.getNamedConfigNames();
            int loadX = x + cardWidth - 148;

            for (int i = 0; i < Math.min(6, namedConfigs.size()); i++) {
               int rowY = cardY + 91 + i * 29;
               String name = namedConfigs.get(i);
               if (mouseX >= loadX && mouseX <= loadX + 60 && mouseY >= rowY && mouseY <= rowY + 23) {
                  if (VoidCyanClient.loadNamedConfig(name)) {
                     this.client.setScreen(new ClickGuiScreen(this.parent));
                  } else {
                     this.namedConfigStatus = "Could not load config.";
                  }

                  return true;
               }

               int delX = loadX + 66;
               if (mouseX >= delX && mouseX <= delX + 60 && mouseY >= rowY && mouseY <= rowY + 23) {
                  if (VoidCyanClient.deleteNamedConfig(name)) {
                     this.namedConfigStatus = "Deleted \"" + name + "\".";
                  } else {
                     this.namedConfigStatus = "Could not delete config.";
                  }

                  return true;
               }
            }

            return false;
         }
      }
   }

   private void saveNamedConfigFromInput() {
      if (VoidCyanClient.saveNamedConfig(this.configNameInput)) {
         this.namedConfigStatus = "Saved \"" + this.configNameInput.trim() + "\".";
         this.configNameInput = "";
         this.clearTextFocus();
      } else {
         this.namedConfigStatus = "Enter a config name first.";
      }
   }

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


   private void renderAnimatedStatsTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      if (!(alpha <= 0.01F)) {
         GuiStyle.text(context, this.textRenderer, "Statistics", x, y, (int)(alpha * 255.0F) << 24 | (VoidCyanClient.getPrimaryColor() & 0x00FFFFFF));
         int off = Math.max(0, this.scrollOffset);
         int colAllTime = x + width - 220;
         int colSession = x + width - 80;
         int headerY = y + 24;
         int headerDrawY = headerY - off;
         int colLabel = x + 10;
         context.drawTextWithShadow(this.textRenderer, Text.literal("All-Time"), colAllTime, headerDrawY, VoidCyanClient.getPrimaryColor());
         context.drawTextWithShadow(this.textRenderer, Text.literal("Session"), colSession, headerDrawY, -5592406);
         context.fill(x + 8, headerDrawY + 14, x + width - 12, headerDrawY + 15, 1090519039);
         // Row values: [label, allTime (main), session (reference)]
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
         int rowY = headerY + 20;
         int rowHeight = 22;

         for (int i = 0; i < rows.length; i++) {
            int drawY = rowY - off;
            if (drawY + rowHeight >= y && drawY <= y + this.height) {
               boolean hover = mouseX >= x + 8 && mouseX <= x + width - 12 && mouseY >= drawY && mouseY <= drawY + rowHeight;
               if (hover) {
                  context.fill(x + 8, drawY, x + width - 12, drawY + rowHeight, 553648127);
               }

               context.drawTextWithShadow(this.textRenderer, Text.literal(rows[i][0]), colLabel, drawY + 6, -1);
               context.drawTextWithShadow(this.textRenderer, Text.literal(rows[i][1]), colAllTime, drawY + 6, VoidCyanClient.getPrimaryColor());
               context.drawTextWithShadow(this.textRenderer, Text.literal(rows[i][2]), colSession, drawY + 6, -5592406);
            }

            rowY += rowHeight;
         }

         int resetY = rowY + 10;
         int resetDrawY = resetY - off;
         boolean resetHover = mouseX >= x + 10 && mouseX <= x + 120 && mouseY >= resetDrawY && mouseY <= resetDrawY + 22;
         context.fill(x + 10, resetDrawY, x + 120, resetDrawY + 22, resetHover ? -1426107051 : 1627346261);
         context.drawTextWithShadow(this.textRenderer, Text.literal("Reset Session"), x + 14, resetDrawY + 6, -1);
         int streaksY = resetY + 40;
         int streaksTitleY = streaksY - off;
         if (streaksTitleY + 12 >= y) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("Login Streaks"), x + 10, streaksTitleY, VoidCyanClient.getPrimaryColor());
         }

         int streaksHeaderY = streaksY + 25;
         int streaksHeaderDrawY = streaksHeaderY - off;
         context.fill(x + 8, streaksHeaderDrawY + 14, x + width - 12, streaksHeaderDrawY + 15, 1090519039);
         List<StreakManager.Streak> streaks = StreakManager.streaks;
         if (streaks.isEmpty()) {
            int noStreaksY = streaksHeaderY + 25 - off;
            if (noStreaksY + 12 >= y) {
               context.drawTextWithShadow(this.textRenderer, Text.literal("No streaks yet. Keep playing!"), x + 10, noStreaksY, -5592406);
            }
         } else {
            int streakRowY = streaksHeaderY + 25;
            int streakCardHeight = 95;
            int streakCardGap = 12;

            for (int i = streaks.size() - 1; i >= 0; i--) {
               StreakManager.Streak streak = streaks.get(i);
               boolean isExpanded = this.expandedStreaks.contains(streak.streakNumber);
               int compactHeight = 55;
               int expandedHeight = 280;
               int currentCardHeight = isExpanded ? expandedHeight : compactHeight;
               int cardDrawY = streakRowY - off;
               if (cardDrawY + currentCardHeight >= y && cardDrawY <= y + this.height) {
                  boolean streakHover = mouseX >= x + 8 && mouseX <= x + width - 12 && mouseY >= cardDrawY && mouseY <= cardDrawY + currentCardHeight;
                  int cardBg = streakHover ? 822083583 : 419430399;
                  context.fill(x + 8, cardDrawY, x + width - 12, cardDrawY + currentCardHeight, cardBg);
                  if (streak.isActive) {
                     context.fill(x + 8, cardDrawY, x + 11, cardDrawY + currentCardHeight, -13318311);
                  }

                  int textX = x + 15;
                  int textY = cardDrawY + 8;
                  String streakTitle = "Streak #" + streak.streakNumber + (streak.isActive ? " (Active)" : "") + (isExpanded ? " ▼" : " ►");
                  context.drawTextWithShadow(this.textRenderer, Text.literal(streakTitle), textX, textY, VoidCyanClient.getPrimaryColor());
                  String dateRange = streak.startDate.toString() + " → " + streak.endDate.toString();
                  context.drawTextWithShadow(this.textRenderer, Text.literal(dateRange), textX, textY + 14, -3355444);
                  String daysInfo = "Days: " + streak.daysCount + " | Playtime: " + StreakManager.formatPlaytime(streak.totalPlaytimeMs);
                  context.drawTextWithShadow(this.textRenderer, Text.literal(daysInfo), textX, textY + 28, -5592406);
                  if (isExpanded) {
                     int statsStartY = textY + 45;
                     int statRowH = 16;
                     int labelX = textX;
                     int valueX = textX + 150;
                     context.drawTextWithShadow(this.textRenderer, Text.literal("Stats Gained"), textX, statsStartY, VoidCyanClient.getPrimaryColor());
                     context.fill(x + 15, statsStartY + 12, x + width - 20, statsStartY + 13, 1090519039);
                     int statY = statsStartY + 18;
                     String[][] streakStats = new String[][]{
                        {"Kills", "+" + streak.gainedStats.kills},
                        {"Deaths", "+" + streak.gainedStats.deaths},
                        {"K/D", this.kd(streak.gainedStats.kills, streak.gainedStats.deaths)},
                        {"Hits", "+" + streak.gainedStats.hits},
                        {"Clicks", "+" + streak.gainedStats.clicks},
                        {"Accuracy", this.accuracy(streak.gainedStats.hits, streak.gainedStats.clicks)},
                        {"Blocks Broken", "+" + streak.gainedStats.blocksBroken},
                        {"Blocks Placed", "+" + streak.gainedStats.blocksPlaced},
                        {"Crystals Placed", "+" + streak.gainedStats.crystalsPlaced},
                        {"Crystals Broken", "+" + streak.gainedStats.crystalsBroken}
                     };

                     for (String[] stat : streakStats) {
                        context.drawTextWithShadow(this.textRenderer, Text.literal(stat[0]), labelX, statY, -3355444);
                        context.drawTextWithShadow(this.textRenderer, Text.literal(stat[1]), valueX, statY, -7798904);
                        statY += statRowH;
                     }
                  }
               }

               streakRowY += currentCardHeight + streakCardGap;
            }
         }
      }
   }

   private boolean handleStatsClick(double mouseX, double mouseY, int x, int y, int width) {
      int off = this.scrollOffset - 35;
      int headerY = y + 60;
      int rowHeight = 22;
      int rowsCount = 19;
      int rowY = headerY + 20 + rowsCount * rowHeight + 10;
      int resetDrawY = rowY - off;
      if (mouseX >= x + 10 && mouseX <= x + 120 && mouseY >= resetDrawY && mouseY <= resetDrawY + 22) {
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
         List<StreakManager.Streak> streaks = StreakManager.streaks;
         if (!streaks.isEmpty()) {
            int resetY = rowY + 10;
            int streaksY = resetY + 40;
            int streaksHeaderY = streaksY + 25;
            int streakRowY = streaksHeaderY + 25;
            int streakCardGap = 12;

            for (int i = streaks.size() - 1; i >= 0; i--) {
               StreakManager.Streak streak = streaks.get(i);
               boolean isExpanded = this.expandedStreaks.contains(streak.streakNumber);
               int compactHeight = 55;
               int expandedHeight = 280;
               int currentCardHeight = isExpanded ? expandedHeight : compactHeight;
               int cardDrawY = streakRowY - off;
               if (mouseX >= x + 8 && mouseX <= x + width - 12 && mouseY >= cardDrawY && mouseY <= cardDrawY + currentCardHeight) {
                  if (this.expandedStreaks.contains(streak.streakNumber)) {
                     this.expandedStreaks.remove(streak.streakNumber);
                  } else {
                     this.expandedStreaks.add(streak.streakNumber);
                  }

                  return true;
               }

               streakRowY += currentCardHeight + streakCardGap;
            }
         }

         return false;
      }
   }

   public static void drawModuleSwitchStatic(DrawContext context, int switchX, int switchY, boolean enabled) {
      int toggleWidth = 24;
      int toggleHeight = 12;
      int radius = 3;
      int centerY = switchY + 6;
      int toggleX = switchX + 16;
      int trackColor = enabled ? -13318311 : -1447446;
      drawModernRoundedRect(context, toggleX, centerY, toggleWidth, toggleHeight, radius, trackColor);
      int thumbSize = 8;
      int thumbX = enabled ? toggleX + toggleWidth - thumbSize - 2 : toggleX + 2;
      int thumbY = centerY + (toggleHeight - thumbSize) / 2;
      drawModernRoundedRect(context, thumbX, thumbY, thumbSize, thumbSize, 2, -1);
   }

   private void renderAnimatedNotesTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      if (!(alpha < 0.05F)) {
         int primaryColor = VoidCyanClient.getPrimaryColor();
         int fullAlpha = (int)(alpha * 255.0F) << 24;
         this.drawGlowText(context, "Notes", x, y, primaryColor, alpha);
         int btnX = x + width - 100;
         int btnY = y - 2;
         boolean newHover = mouseX >= btnX && mouseX <= btnX + 96 && mouseY >= btnY && mouseY <= btnY + 18;
         int btnBg = newHover ? -872415232 | primaryColor & 16777215 : -2147483648 | primaryColor & 16777215;
         drawModernRoundedRect(context, btnX, btnY, 96, 18, 4, btnBg);
         context.drawTextWithShadow(this.textRenderer, Text.literal("+ New Note"), btnX + 8, btnY + 5, -1);
         if (this.selectedNoteIndex >= 0 && this.selectedNoteIndex < NoteManager.notes.size()) {
            this.renderNoteDetail(context, mouseX, mouseY, x, y + 30, width, alpha);
         } else {
            this.renderNotesList(context, mouseX, mouseY, x, y + 30, width, alpha);
         }
      }
   }

   private void renderNotesList(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      List<NoteManager.Note> notes = NoteManager.notes;
      int primaryColor = VoidCyanClient.getPrimaryColor();
      int cardH = 62;
      int cardGap = 8;
      int cols = Math.max(1, width / 200);
      int cardW = (width - (cols - 1) * cardGap) / cols;
      int rowY = y - this.scrollOffset;
      if (notes.isEmpty()) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("No notes yet. Click '+ New Note' to create one."), x, y + 20, -2130706433);
      } else {
         for (int i = 0; i < notes.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int cx = x + col * (cardW + cardGap);
            int cy = rowY + row * (cardH + cardGap);
            if (cy + cardH >= y - 40 && cy <= y + this.height + 40) {
               boolean hover = mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH;
               int cardBg = hover ? 822083583 : 419430399;
               if (hover) {
                  int var10000 = -2147483648 | primaryColor & 16777215;
               } else {
                  int var32 = 822083583;
               }

               drawModernRoundedRect(context, cx, cy, cardW, cardH, 6, cardBg);
               drawModernRoundedRect(context, cx, cy, 3, cardH, 3, 0xFF000000 | primaryColor & 16777215);
               String title = notes.get(i).title;
               if (title == null || title.isEmpty()) {
                  title = "(Untitled)";
               }

               context.drawTextWithShadow(this.textRenderer, Text.literal(title), cx + 10, cy + 8, -1);
               String content = notes.get(i).content;
               if (content == null) {
                  content = "";
               }

               String[] lines = content.split("\n", 4);
               int previewY = cy + 22;

               for (int li = 0; li < Math.min(2, lines.length) && previewY < cy + cardH - 8; li++) {
                  String line = lines[li];
                  if (line.length() > 28) {
                     line = line.substring(0, 28) + "…";
                  }

                  context.drawTextWithShadow(this.textRenderer, Text.literal(line), cx + 10, previewY, -1711276033);
                  previewY += 11;
               }

               int delX = cx + cardW - 16;
               int delY2 = cy + 4;
               boolean delHov = mouseX >= delX && mouseX <= delX + 12 && mouseY >= delY2 && mouseY <= delY2 + 12;
               drawModernRoundedRect(context, delX, delY2, 12, 12, 3, delHov ? -1426115789 : 1627337523);
               context.drawTextWithShadow(this.textRenderer, Text.literal("x"), delX + 3, delY2 + 2, -1);
            }
         }
      }
   }

   private void renderNoteDetail(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      NoteManager.Note note = NoteManager.notes.get(this.selectedNoteIndex);
      int primaryColor = VoidCyanClient.getPrimaryColor();
      int aInt = (int)(alpha * 255.0F);
      boolean backHov = mouseX >= x && mouseX <= x + 60 && mouseY >= y && mouseY <= y + 16;
      drawModernRoundedRect(context, x, y, 60, 16, 4, backHov ? 1627389951 : 822083583);
      context.drawTextWithShadow(this.textRenderer, Text.literal("← Back"), x + 6, y + 4, -1);
      int copyX = x + 68;
      boolean copyHov = mouseX >= copyX && mouseX <= copyX + 70 && mouseY >= y && mouseY <= y + 16;
      drawModernRoundedRect(context, copyX, y, 70, 16, 4, copyHov ? -1879048192 | primaryColor & 16777215 : 1342177280 | primaryColor & 16777215);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Copy Content"), copyX + 6, y + 4, -1);
      int delBtnX = x + width - 72;
      boolean delBtnHov = mouseX >= delBtnX && mouseX <= delBtnX + 68 && mouseY >= y && mouseY <= y + 16;
      drawModernRoundedRect(context, delBtnX, y, 68, 16, 4, delBtnHov ? -1426115789 : 1627337523);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Delete Note"), delBtnX + 4, y + 4, -1);
      int saveBtnX = x + width / 2 - 40;
      boolean saveBtnHov = mouseX >= saveBtnX && mouseX <= saveBtnX + 80 && mouseY >= y && mouseY <= y + 16;
      drawModernRoundedRect(context, saveBtnX, y, 80, 16, 4, saveBtnHov ? -1879004672 : 1342220800);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Save Notes"), saveBtnX + 4, y + 4, -1);
      int fieldY = y + 26;
      boolean titleFoc = this.isNoteTitleFocused;
      drawModernRoundedRect(context, x, fieldY, width, 20, 4, titleFoc ? 1090519039 : 553648127);
      if (titleFoc) {
         drawModernRoundedRect(context, x, fieldY + 19, width, 1, 0, 0xFF000000 | primaryColor & 16777215);
      }

      String titleText = note.title == null ? "" : note.title;
      String titleDisplay = titleText;
      if (titleFoc) {
         int safeCursor = Math.min(this.textCursor, titleText.length());
         String beforeCursor = titleText.substring(0, safeCursor);
         String afterCursor = titleText.substring(safeCursor);
         long time = System.currentTimeMillis();
         boolean cursorVisible = time % 1000L < 500L;
         if (cursorVisible) {
            titleDisplay = beforeCursor + "|" + afterCursor;
         } else {
            titleDisplay = titleText;
         }
      }

      if (titleDisplay.isEmpty()) {
         titleDisplay = "Note title…";
      }

      context.drawTextWithShadow(this.textRenderer, Text.literal(titleDisplay), x + 8, fieldY + 6, titleFoc ? -1 : -1426063361);
      fieldY += 28;
      int contentAreaH = this.height - fieldY - 20;
      if (contentAreaH < 40) {
         contentAreaH = 40;
      }

      boolean contentFoc = this.isNoteContentFocused;
      drawModernRoundedRect(context, x, fieldY, width, contentAreaH, 4, contentFoc ? 905969663 : 419430399);
      if (contentFoc) {
         drawModernRoundedRect(context, x, fieldY, 2, contentAreaH, 2, 0xFF000000 | primaryColor & 16777215);
      }

      String rawContent = note.content == null ? "" : note.content;
      String displayContent = rawContent;
      if (contentFoc) {
         int safeCursor = Math.min(this.textCursor, rawContent.length());
         String beforeCursor = rawContent.substring(0, safeCursor);
         String afterCursor = rawContent.substring(safeCursor);
         long time = System.currentTimeMillis();
         boolean cursorVisible = time % 1000L < 500L;
         if (cursorVisible) {
            displayContent = beforeCursor + "|" + afterCursor;
         } else {
            displayContent = rawContent;
         }
      }

      String[] lines = displayContent.split("\n", -1);
      int lineH = 11;
      int textX = x + 8;
      int textY = fieldY + 6 - this.noteContentScrollOffset;

      for (String line : lines) {
         for (int maxChars = Math.max(1, (width - 16) / 6); line.length() > maxChars; textY += lineH) {
            String segment = line.substring(0, maxChars);
            if (textY >= fieldY && textY <= fieldY + contentAreaH) {
               context.drawTextWithShadow(this.textRenderer, Text.literal(segment), textX, textY, contentFoc ? -1 : -855638017);
            }

            line = line.substring(maxChars);
         }

         if (textY >= fieldY && textY <= fieldY + contentAreaH) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(line), textX, textY, contentFoc ? -1 : -855638017);
         }

         textY += lineH;
      }

      if (!contentFoc && rawContent.isEmpty()) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("Start typing your note here…"), textX, fieldY + 6, 1442840575);
      }
   }

   private boolean handleNotesClick(double mouseX, double mouseY, int x, int y, int width) {
      int innerX = x + 15;
      int innerY = y + 26;
      int innerW = width - 30;
      int btnX = innerX + innerW - 100;
      int btnY = innerY - 2;
      if (mouseX >= btnX && mouseX <= btnX + 96 && mouseY >= btnY && mouseY <= btnY + 18) {
         NoteManager.addNote("New Note", "");
         this.selectedNoteIndex = NoteManager.notes.size() - 1;
         this.isNoteTitleFocused = true;
         this.isNoteContentFocused = false;
         this.textCursor = 0;
         this.noteContentScrollOffset = 0;
         return true;
      } else {
         List<NoteManager.Note> notes = NoteManager.notes;
         if (this.selectedNoteIndex >= 0 && this.selectedNoteIndex < notes.size()) {
            int fieldY = innerY + 10;
            int saveBtnX = innerX + innerW / 2 - 40;
            if (mouseX >= saveBtnX && mouseX <= saveBtnX + 80 && mouseY >= innerY && mouseY <= innerY + 16) {
               NoteManager.saveNotes();
               return true;
            }

            if (mouseX >= innerX && mouseX <= innerX + 60 && mouseY >= fieldY && mouseY <= fieldY + 16) {
               NoteManager.saveNotes();
               this.selectedNoteIndex = -1;
               this.clearTextFocus();
               this.noteContentScrollOffset = 0;
               return true;
            }

            int copyX = innerX + 68;
            if (mouseX >= copyX && mouseX <= copyX + 70 && mouseY >= fieldY && mouseY <= fieldY + 16) {
               String content = notes.get(this.selectedNoteIndex).content;
               if (content != null && !content.isEmpty()) {
                  this.client.keyboard.setClipboard(content);
               }

               return true;
            }

            int delBtnX = innerX + innerW - 72;
            if (mouseX >= delBtnX && mouseX <= delBtnX + 68 && mouseY >= fieldY && mouseY <= fieldY + 16) {
               notes.remove(this.selectedNoteIndex);
               this.selectedNoteIndex = -1;
               this.clearTextFocus();
               return true;
            }

            fieldY += 26;
            if (mouseX >= innerX && mouseX <= innerX + innerW && mouseY >= fieldY && mouseY <= fieldY + 20) {
               this.isNoteTitleFocused = true;
               this.isNoteContentFocused = false;
               this.textCursor = notes.get(this.selectedNoteIndex).title == null ? 0 : notes.get(this.selectedNoteIndex).title.length();
               return true;
            }

            fieldY += 28;
            int contentAreaH = this.height - fieldY - 20;
            if (contentAreaH < 40) {
               contentAreaH = 40;
            }

            if (mouseX >= innerX && mouseX <= innerX + innerW && mouseY >= fieldY && mouseY <= fieldY + contentAreaH) {
               this.isNoteContentFocused = true;
               this.isNoteTitleFocused = false;
               this.textCursor = notes.get(this.selectedNoteIndex).content == null ? 0 : notes.get(this.selectedNoteIndex).content.length();
               return true;
            }
         } else {
            int listY = innerY + 30;
            int cardH = 62;
            int cardGap = 8;
            int cols = Math.max(1, innerW / 200);
            int cardW = (innerW - (cols - 1) * cardGap) / cols;
            int rowY = listY - this.scrollOffset;

            for (int i = 0; i < notes.size(); i++) {
               int col = i % cols;
               int row = i / cols;
               int cx = innerX + col * (cardW + cardGap);
               int cy = rowY + row * (cardH + cardGap);
               int delX = cx + cardW - 16;
               int delY2 = cy + 4;
               if (mouseX >= delX && mouseX <= delX + 12 && mouseY >= delY2 && mouseY <= delY2 + 12) {
                  notes.remove(i);
                  return true;
               }

               if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH) {
                  this.selectedNoteIndex = i;
                  this.isNoteTitleFocused = false;
                  this.isNoteContentFocused = false;
                  this.noteContentScrollOffset = 0;
                  this.textCursor = 0;
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void renderAnimatedCalculatorTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      if (!(alpha < 0.05F)) {
         int primaryColor = VoidCyanClient.getPrimaryColor();
         int off = this.scrollOffset - 30;
         this.drawGlowText(context, "Calculator", x, y, primaryColor, alpha);

         int fieldY = y + 30 - off;
         int fieldWidth = Math.min(400, width - 20);
         int fieldX = x + (width - fieldWidth) / 2;
         int inputFieldH = 40;
         boolean inputHover = mouseX >= fieldX && mouseX <= fieldX + fieldWidth && mouseY >= fieldY && mouseY <= fieldY + inputFieldH;
         int inputBg = this.isCalculatorInputFocused ? 1627389951 : (inputHover ? 822083583 : 553648127);
         drawModernRoundedRect(context, fieldX, fieldY, fieldWidth, inputFieldH, 6, inputBg);
         String displayText = this.calculatorInput.isEmpty() ? "Enter expression..." : this.calculatorInput;
         int textColor = this.calculatorInput.isEmpty() ? -2130706433 : -1;
         context.drawTextWithShadow(this.textRenderer, Text.literal(displayText), fieldX + 10, fieldY + 14, textColor);
         if (this.isCalculatorInputFocused && System.currentTimeMillis() / 500L % 2L == 0L) {
            String textBeforeCursor = this.calculatorInput.substring(0, Math.min(this.textCursor, this.calculatorInput.length()));
            int cursorX = fieldX + 10 + this.textRenderer.getWidth(textBeforeCursor);
            context.fill(cursorX, fieldY + 10, cursorX + 1, fieldY + 30, -1);
         }

         int resultY = fieldY + inputFieldH + 10;
         if (!this.calculatorResult.isEmpty()) {
            drawModernRoundedRect(context, fieldX, resultY, fieldWidth, 35, 6, 1073741824 | primaryColor & 16777215);
            context.drawTextWithShadow(this.textRenderer, Text.literal("= " + this.calculatorResult), fieldX + 10, resultY + 12, primaryColor);
            resultY += 45;
         } else {
            resultY += 10;
         }

         int buttonSize = 60;
         int buttonGap = 8;
         int gridX = fieldX + (fieldWidth - (buttonSize * 4 + buttonGap * 3)) / 2;
         int gridY = resultY + 10;
         String[][] buttonLayout = new String[][]{{"7", "8", "9", "/"}, {"4", "5", "6", "*"}, {"1", "2", "3", "-"}, {"0", ".", "=", "+"}, {"C", "(", ")", "←"}};

         for (int row = 0; row < buttonLayout.length; row++) {
            for (int col = 0; col < buttonLayout[row].length; col++) {
               int btnX = gridX + col * (buttonSize + buttonGap);
               int btnY = gridY + row * (buttonSize + buttonGap);
               String btnLabel = buttonLayout[row][col];
               boolean btnHover = mouseX >= btnX && mouseX <= btnX + buttonSize && mouseY >= btnY && mouseY <= btnY + buttonSize;
               int btnTextColor = -1;
               int btnBg;
               if (btnLabel.equals("=")) {
                  btnBg = btnHover ? -872415232 | primaryColor & 16777215 : -2147483648 | primaryColor & 16777215;
               } else if (btnLabel.equals("C") || btnLabel.equals("←")) {
                  btnBg = btnHover ? -855681707 : -2130750123;
               } else if (btnLabel.matches("[+\\-*/()]")) {
                  btnBg = btnHover ? -867923713 : -2142992129;
               } else {
                  btnBg = btnHover ? 1358954495 : 822083583;
               }

               context.fill(btnX, btnY, btnX + buttonSize, btnY + buttonSize, btnBg);
               int textWidth = this.textRenderer.getWidth(btnLabel);
               int textX = btnX + (buttonSize - textWidth) / 2;
               int textY = btnY + (buttonSize - 8) / 2;
               context.drawTextWithShadow(this.textRenderer, Text.literal(btnLabel), textX, textY, btnTextColor);
            }
         }
      }
   }

   private boolean handleCalculatorClick(double mouseX, double mouseY, int x, int y, int width) {
      int off = this.scrollOffset - 30;
      int fieldY = y + 30 - off;
      int fieldWidth = Math.min(400, width - 20);
      int fieldX = x + (width - fieldWidth) / 2;
      int inputFieldH = 40;
      if (mouseX >= fieldX && mouseX <= fieldX + fieldWidth && mouseY >= fieldY && mouseY <= fieldY + inputFieldH) {
         this.isCalculatorInputFocused = true;
         this.textCursor = this.calculatorInput.length();
         return true;
      } else {
         int buttonSize = 60;
         int buttonGap = 8;
         int gridX = fieldX + (fieldWidth - (buttonSize * 4 + buttonGap * 3)) / 2;
         int resultY = fieldY + inputFieldH + (!this.calculatorResult.isEmpty() ? 55 : 10);
         int gridY = resultY + 10;
         String[][] buttonLayout = new String[][]{{"7", "8", "9", "/"}, {"4", "5", "6", "*"}, {"1", "2", "3", "-"}, {"0", ".", "=", "+"}, {"C", "(", ")", "←"}};

         for (int row = 0; row < buttonLayout.length; row++) {
            for (int col = 0; col < buttonLayout[row].length; col++) {
               int btnX = gridX + col * (buttonSize + buttonGap);
               int btnY = gridY + row * (buttonSize + buttonGap);
               if (mouseX >= btnX && mouseX <= btnX + buttonSize && mouseY >= btnY && mouseY <= btnY + buttonSize) {
                  this.handleCalculatorButton(buttonLayout[row][col]);
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void handleCalculatorButton(String button) {
      switch (button) {
         case "C":
            this.calculatorInput = "";
            this.calculatorResult = "";
            break;
         case "←":
            if (!this.calculatorInput.isEmpty()) {
               this.calculatorInput = this.calculatorInput.substring(0, this.calculatorInput.length() - 1);
            }
            break;
         case "=":
            try {
               this.calculatorResult = String.valueOf(this.evaluateExpression(this.calculatorInput));
            } catch (Exception var5) {
               this.calculatorResult = "Error";
            }
            break;
         default:
            this.calculatorInput = this.calculatorInput + button;
      }
   }

   private double evaluateExpression(String expression) throws Exception {
      expression = expression.replaceAll("\\s+", "");
      if (expression.isEmpty()) {
         throw new Exception("Empty expression");
      } else {
         return this.evaluateExpressionRecursive(expression);
      }
   }

   private double evaluateExpressionRecursive(String expr) throws Exception {
      while (expr.contains("(")) {
         int lastOpen = expr.lastIndexOf(40);
         int matchingClose = expr.indexOf(41, lastOpen);
         if (matchingClose == -1) {
            throw new Exception("Mismatched parentheses");
         }

         String subExpr = expr.substring(lastOpen + 1, matchingClose);
         double subResult = this.evaluateExpressionRecursive(subExpr);
         expr = expr.substring(0, lastOpen) + subResult + expr.substring(matchingClose + 1);
      }

      for (int i = expr.length() - 1; i >= 0; i--) {
         char c = expr.charAt(i);
         if ((c == '+' || c == '-') && i > 0) {
            String left = expr.substring(0, i);
            String right = expr.substring(i + 1);
            if (!left.isEmpty() && !right.isEmpty()) {
               double leftVal = this.evaluateExpressionRecursive(left);
               double rightVal = this.evaluateExpressionRecursive(right);
               return c == '+' ? leftVal + rightVal : leftVal - rightVal;
            }
         }
      }

      for (int ix = expr.length() - 1; ix >= 0; ix--) {
         char c = expr.charAt(ix);
         if ((c == '*' || c == '/') && ix > 0) {
            String left = expr.substring(0, ix);
            String right = expr.substring(ix + 1);
            if (!left.isEmpty() && !right.isEmpty()) {
               double leftVal = this.evaluateExpressionRecursive(left);
               double rightVal = this.evaluateExpressionRecursive(right);
               if (c == '/' && rightVal == 0.0) {
                  throw new Exception("Division by zero");
               }

               return c == '*' ? leftVal * rightVal : leftVal / rightVal;
            }
         }
      }

      try {
         return Double.parseDouble(expr);
      } catch (NumberFormatException var10) {
         throw new Exception("Invalid number: " + expr);
      }
   }

   private void renderAnimatedPlayerModelTab(DrawContext context, int mouseX, int mouseY, int x, int y, int width, float alpha) {
      if (alpha < 0.05F) return;
      int primary = VoidCyanClient.getPrimaryColor();
      this.drawGlowText(context, "Player Model", x, y, primary, alpha);

      int cardY = y + 36 - this.scrollOffset;
      int cardH = 280;
      int cardW = width - 20;
      this.drawAnimatedCard(context, x, cardY, cardW, cardH, primary, 0.6F, alpha);
      if (alpha < 0.3F) return;

      float textAlpha = Math.min(1.0F, (alpha - 0.3F) / 0.7F);
      int textColor = (int)(textAlpha * 235.0F) << 24 | 0x00FFFFFF;
      int subColor = (int)(textAlpha * 130.0F) << 24 | 0x00FFFFFF;

      // Left Column: 3D Preview Box
      int previewW = 150;
      int previewH = cardH - 24;
      int previewX = x + 12;
      int previewY = cardY + 12;
      GuiStyle.roundedRect(context, previewX, previewY, previewW, previewH, 6, 0x30000000);
      GuiStyle.roundedOutline(context, previewX, previewY, previewW, previewH, 6, (int)(textAlpha * 50.0F) << 24 | (primary & 0xFFFFFF));

      if (this.client != null && this.client.player != null) {
         InventoryScreen.drawEntity(
            context, previewX + 6, previewY + 6, previewX + previewW - 6, previewY + previewH - 20,
            46, 0.0625F, mouseX, mouseY, this.client.player
         );
      }
      String hint = "3D Interactive Preview";
      int hintW = this.textRenderer.getWidth(hint);
      GuiStyle.text(context, this.textRenderer, hint, previewX + (previewW - hintW) / 2, previewY + previewH - 14, subColor);

      // Right Column: Controls & Details
      int rightX = previewX + previewW + 16;
      int rightW = cardW - (rightX - x) - 14;

      // Header row with toggle switch
      this.drawGlowText(context, "Custom OBJ Model", rightX, cardY + 14, primary, textAlpha);
      int toggleW = 40;
      int toggleH = 16;
      int toggleX = rightX + rightW - toggleW;
      int toggleY = cardY + 14;
      GuiStyle.toggleSwitch(context, toggleX, toggleY, toggleW, toggleH, VoidCyanClient.isPlayerModelEnabled, VoidCyanClient.isPlayerModelEnabled ? 1.0F : 0.0F, primary, textAlpha);

      String activeStr = VoidCyanClient.isPlayerModelEnabled ? "● Custom model is ACTIVE" : "○ Disabled (vanilla skin)";
      int activeCol = VoidCyanClient.isPlayerModelEnabled ? 0xFF55FF99 : subColor;
      GuiStyle.text(context, this.textRenderer, activeStr, rightX, cardY + 30, activeCol);

      // Info Box
      int infoBoxY = cardY + 44;
      int infoBoxH = 64;
      GuiStyle.roundedRect(context, rightX, infoBoxY, rightW, infoBoxH, 4, 0x2214141E);
      GuiStyle.roundedOutline(context, rightX, infoBoxY, rightW, infoBoxH, 4, 0x25FFFFFF);

      boolean hasModel = PlayerModelManager.hasModel();
      boolean hasTex = PlayerModelManager.hasTexture();
      String modelInfo = hasModel
         ? "Model: model.obj (" + PlayerModelManager.getModel().tris.size() + " tris, bounds: "
            + String.format(java.util.Locale.ROOT, "%.1fx%.1fx%.1f", PlayerModelManager.getModel().widthX(), PlayerModelManager.getModel().heightY(), PlayerModelManager.getModel().depthZ()) + ")"
         : "Model: None loaded (vanilla player mesh)";
      String texInfo = hasTex ? "Texture: texture.png (loaded)" : (hasModel ? "Texture: Missing texture.png (white fallback)" : "Texture: Vanilla player skin");
      String statInfo = !this.clickGuiPmStatus.isEmpty() ? this.clickGuiPmStatus : (hasModel ? (VoidCyanClient.isPlayerModelEnabled ? "Model loaded and rendering in 3D" : "Model loaded (toggle switch ON to enable)") : "Click 'Import .obj' to load a 3D model");
      GuiStyle.text(context, this.textRenderer, modelInfo, rightX + 8, infoBoxY + 8, hasModel ? textColor : subColor);
      GuiStyle.text(context, this.textRenderer, texInfo, rightX + 8, infoBoxY + 24, hasTex ? 0xFF55FF99 : (hasModel ? 0xFFFFAA44 : subColor));
      GuiStyle.text(context, this.textRenderer, statInfo, rightX + 8, infoBoxY + 44, 0xFF00E5FF);

      // Buttons Row 1
      int btnGap = 8;
      int btnH = 20;
      int btnW = (rightW - btnGap) / 2;
      int row1Y = cardY + 118;
      this.drawClickGuiButton(context, mouseX, mouseY, rightX, row1Y, btnW, btnH, "Import .obj Model", primary, textAlpha);
      this.drawClickGuiButton(context, mouseX, mouseY, rightX + btnW + btnGap, row1Y, btnW, btnH, "Import Texture (.png)", primary, textAlpha);

      // Buttons Row 2
      int row2Y = row1Y + btnH + 6;
      this.drawClickGuiButton(context, mouseX, mouseY, rightX, row2Y, btnW, btnH, "Open Models Folder", primary, textAlpha);
      this.drawClickGuiButton(context, mouseX, mouseY, rightX + btnW + btnGap, row2Y, btnW, btnH, "Clear / Remove Model", primary, textAlpha);

      // Scale row: [-] Model Scale: 1.00x [+] [Reset]
      int row3Y = row2Y + btnH + 6;
      int smallW = 22;
      this.drawClickGuiButton(context, mouseX, mouseY, rightX, row3Y, smallW, btnH, "-", primary, textAlpha);
      String scaleLbl = String.format(java.util.Locale.ROOT, "Model Scale: %.2fx", VoidCyanClient.playerModelScale);
      GuiStyle.text(context, this.textRenderer, scaleLbl, rightX + smallW + 8, row3Y + 6, textColor);
      int plusX = rightX + smallW + 8 + this.textRenderer.getWidth(scaleLbl) + 8;
      this.drawClickGuiButton(context, mouseX, mouseY, plusX, row3Y, smallW, btnH, "+", primary, textAlpha);
      this.drawClickGuiButton(context, mouseX, mouseY, plusX + smallW + 6, row3Y, 44, btnH, "Reset", primary, textAlpha);

      // Footer
      int footY = row3Y + btnH + 10;
      GuiStyle.text(context, this.textRenderer, "• Place your Wavefront .obj file as model.obj and skin as texture.png", rightX, footY, subColor);
      GuiStyle.text(context, this.textRenderer, "• Triangles & quads; dense models are simplified automatically. Auto-fits to player height.", rightX, footY + 12, subColor);
   }

   private void drawClickGuiButton(DrawContext context, int mouseX, int mouseY, int bx, int by, int bw, int bh, String label, int primaryColor, float textAlpha) {
      boolean hov = GuiStyle.inRect(mouseX, mouseY, bx, by, bw, bh);
      GuiStyle.roundedRect(context, bx, by, bw, bh, 3, (int)(textAlpha * (hov ? 140 : 90)) << 24 | 0x2A2434);
      GuiStyle.roundedOutline(context, bx, by, bw, bh, 3, (int)(textAlpha * (hov ? 180 : 90)) << 24 | (primaryColor & 0xFFFFFF));
      int tw = this.textRenderer.getWidth(label);
      int tx = bx + (bw - tw) / 2;
      int ty = by + (bh - 8) / 2;
      GuiStyle.text(context, this.textRenderer, label, tx, ty, (int)(textAlpha * 240.0F) << 24 | 0x00FFFFFF);
   }

   private boolean handlePlayerModelClick(double mouseX, double mouseY, int x, int y, int width) {
      int cardY = y + 36 - this.scrollOffset;
      int cardW = width - 20;
      int previewW = 150;
      int previewX = x + 12;
      int rightX = previewX + previewW + 16;
      int rightW = cardW - (rightX - x) - 14;

      // Toggle switch
      int toggleW = 40;
      int toggleH = 16;
      int toggleX = rightX + rightW - toggleW;
      int toggleY = cardY + 14;
      if (GuiStyle.inRect(mouseX, mouseY, toggleX, toggleY, toggleW, toggleH)) {
         VoidCyanClient.isPlayerModelEnabled = !VoidCyanClient.isPlayerModelEnabled;
         VoidCyanClient.saveConfig();
         return true;
      }

      int btnGap = 8;
      int btnH = 20;
      int btnW = (rightW - btnGap) / 2;
      int row1Y = cardY + 118;
      int row2Y = row1Y + btnH + 6;

      // Button 1: Import .obj
      if (GuiStyle.inRect(mouseX, mouseY, rightX, row1Y, btnW, btnH)) {
         this.openClickGuiObjPicker();
         return true;
      }

      // Button 2: Import Texture
      if (GuiStyle.inRect(mouseX, mouseY, rightX + btnW + btnGap, row1Y, btnW, btnH)) {
         this.openClickGuiTexturePicker();
         return true;
      }

      // Button 3: Open Models Folder
      if (GuiStyle.inRect(mouseX, mouseY, rightX, row2Y, btnW, btnH)) {
         try {
            Files.createDirectories(PlayerModelManager.MODEL_DIR);
            Util.getOperatingSystem().open(PlayerModelManager.MODEL_DIR.toFile());
         } catch (Exception ignored) {}
         return true;
      }

      // Scale row buttons (must mirror the layout in renderAnimatedPlayerModelTab)
      int row3Y = row2Y + btnH + 6;
      int smallW = 22;
      String scaleLbl = String.format(java.util.Locale.ROOT, "Model Scale: %.2fx", VoidCyanClient.playerModelScale);
      int plusX = rightX + smallW + 8 + this.textRenderer.getWidth(scaleLbl) + 8;
      if (GuiStyle.inRect(mouseX, mouseY, rightX, row3Y, smallW, btnH)) {
         VoidCyanClient.playerModelScale = Math.max(0.05F, Math.round(VoidCyanClient.playerModelScale / 1.1F * 100.0F) / 100.0F);
         VoidCyanClient.saveConfig();
         return true;
      }
      if (GuiStyle.inRect(mouseX, mouseY, plusX, row3Y, smallW, btnH)) {
         VoidCyanClient.playerModelScale = Math.min(20.0F, Math.round(VoidCyanClient.playerModelScale * 1.1F * 100.0F) / 100.0F);
         VoidCyanClient.saveConfig();
         return true;
      }
      if (GuiStyle.inRect(mouseX, mouseY, plusX + smallW + 6, row3Y, 44, btnH)) {
         VoidCyanClient.playerModelScale = 1.0F;
         VoidCyanClient.saveConfig();
         return true;
      }

      // Button 4: Remove Model
      if (GuiStyle.inRect(mouseX, mouseY, rightX + btnW + btnGap, row2Y, btnW, btnH)) {
         PlayerModelManager.clearModel();
         this.clickGuiPmStatus = "Model removed";
         return true;
      }

      return false;
   }

   private void openClickGuiObjPicker() {
      if (this.clickGuiPmPickerRunning) return;
      this.clickGuiPmPickerRunning = true;
      this.clickGuiPmStatus = "Opening file dialog...";
      Thread t = new Thread(() -> {
         try {
            String result;
            try (MemoryStack stack = MemoryStack.stackPush()) {
               PointerBuffer filterPatterns = stack.pointers(stack.UTF8("*.obj"));
               result = TinyFileDialogs.tinyfd_openFileDialog(
                     "Import .obj Model", "", filterPatterns, "Wavefront OBJ (*.obj)", false);
            }
            if (result != null && !result.isBlank()) {
               try {
                  PlayerModelManager.importModel(Path.of(result));
                  this.clickGuiPmStatus = "Imported: " + Path.of(result).getFileName();
               } catch (Exception e) {
                  this.clickGuiPmStatus = "Error: " + e.getMessage();
               }
            } else {
               this.clickGuiPmStatus = "";
            }
         } finally {
            this.clickGuiPmPickerRunning = false;
         }
      }, "voidcyan-clickgui-obj-picker");
      t.setDaemon(true);
      t.start();
   }

   private void openClickGuiTexturePicker() {
      if (this.clickGuiPmPickerRunning) return;
      this.clickGuiPmPickerRunning = true;
      this.clickGuiPmStatus = "Opening file dialog...";
      Thread t = new Thread(() -> {
         try {
            String result;
            try (MemoryStack stack = MemoryStack.stackPush()) {
               PointerBuffer filterPatterns = stack.pointers(stack.UTF8("*.png"));
               result = TinyFileDialogs.tinyfd_openFileDialog(
                     "Import Texture", "", filterPatterns, "PNG Image (*.png)", false);
            }
            if (result != null && !result.isBlank()) {
               try {
                  Files.createDirectories(PlayerModelManager.MODEL_DIR);
                  Files.copy(Path.of(result), PlayerModelManager.TEXTURE_FILE, StandardCopyOption.REPLACE_EXISTING);
                  MinecraftClient.getInstance().execute(PlayerModelManager::ensureTextureRegistered);
                  this.clickGuiPmStatus = "Texture imported";
               } catch (Exception e) {
                  this.clickGuiPmStatus = "Error: " + e.getMessage();
               }
            } else {
               this.clickGuiPmStatus = "";
            }
         } finally {
            this.clickGuiPmPickerRunning = false;
         }
      }, "voidcyan-clickgui-tex-picker");
      t.setDaemon(true);
      t.start();
   }

   private static class BackgroundFile {
      String name;
      String displayName;
      float animationProgress = 0.0F;
      File file;
      boolean isDirectory;
      long size;

      BackgroundFile(String name, File file) {
         this.name = name;
         this.displayName = name.length() > 25 ? name.substring(0, 22) + "..." : name;
         this.file = file;
         this.isDirectory = file.isDirectory();
         this.size = file.length();
      }
   }

   private static class Friend {
      String name;
      float animationProgress = 0.0F;

      Friend(String name) {
         this.name = name;
      }
   }

   public static class HudElement {
      public String name;
      public Supplier<Boolean> isEnabled;
      public Supplier<Integer> getX;
      public Supplier<Integer> getY;
      public Supplier<Float> getScale;
      public Consumer<Integer> setX;
      public Consumer<Integer> setY;
      public Consumer<Float> setScale;
      public Supplier<Integer> getWidth;
      public Supplier<Integer> getHeight;

      public HudElement(
         String name,
         Supplier<Boolean> isEnabled,
         Supplier<Integer> getX,
         Supplier<Integer> getY,
         Supplier<Float> getScale,
         Consumer<Integer> setX,
         Consumer<Integer> setY,
         Consumer<Float> setScale,
         Supplier<Integer> getWidth,
         Supplier<Integer> getHeight
      ) {
         this.name = name;
         this.isEnabled = isEnabled;
         this.getX = getX;
         this.getY = getY;
         this.getScale = getScale;
         this.setX = setX;
         this.setY = setY;
         this.setScale = setScale;
         this.getWidth = getWidth;
         this.getHeight = getHeight;
      }
   }

   private static class ModuleInfo {
      String name;
      String description = "";
      Runnable toggle;
      boolean enabled;
      float animationProgress = 0.0F;
      float hoverProgress = 0.0F;
      float searchAnimationProgress = 1.0F;
      float currentX = -1.0F;
      float currentY = -1.0F;
      boolean wasEnabled = false;
      Runnable openSettings = null;
      boolean settingsButtonOpensSettings = false;
      Runnable resetHudPosition = null;

      ModuleInfo(String name, Runnable toggle, boolean enabled) {
         this.name = name;
         this.toggle = toggle;
         this.enabled = enabled;
         this.wasEnabled = enabled;
      }

      ModuleInfo(String name, Runnable toggle, boolean enabled, Runnable openSettings) {
         this.name = name;
         this.toggle = toggle;
         this.enabled = enabled;
         this.wasEnabled = enabled;
         this.openSettings = openSettings;
      }

      ModuleInfo(String name, Runnable toggle, boolean enabled, Runnable openSettings, Runnable resetHudPosition) {
         this(name, toggle, enabled, openSettings);
         this.resetHudPosition = resetHudPosition;
      }

      ModuleInfo(String name, Runnable toggle, boolean enabled, Runnable openSettings, boolean settingsButtonOpensSettings) {
         this(name, toggle, enabled, openSettings);
         this.settingsButtonOpensSettings = settingsButtonOpensSettings;
      }

      ClickGuiScreen.ModuleInfo desc(String d) {
         this.description = d;
         return this;
      }
   }

   private static class Screenshot {
      String name;
      String originalName;
      String editableName;
      float animationProgress = 0.0F;
      boolean isEditing = false;
      File file;

      Screenshot(String name) {
         this.name = name;
         this.originalName = name;
         this.editableName = name.substring(0, name.lastIndexOf(46));
         this.file = new File("screenshots", name);
      }

      private File sizeOf;
      private long size;

      long fileSize() {
         if (this.sizeOf != this.file) {
            this.sizeOf = this.file;
            this.size = this.file.exists() ? this.file.length() : 0L;
         }
         return this.size;
      }
   }
}
