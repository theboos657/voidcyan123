package com.voidcyan.client;

import com.voidcyan.client.mixin.accessor.OverlayTextureAccessor;
import com.voidcyan.client.module.ArrayListManager;
import com.voidcyan.client.module.AttackHudRenderer;
import com.voidcyan.client.module.BlockIndicatorManager;
import com.voidcyan.client.module.BlockOverlayRenderer;
import com.voidcyan.client.module.ChinaHatRenderer;
import com.voidcyan.client.module.CustomF3Manager;
import com.voidcyan.client.module.DeathInfoManager;
import com.voidcyan.client.module.DropPreventionManager;
import com.voidcyan.client.module.EnemyCrucialsHudRenderer;
import com.voidcyan.client.module.FpsGraphManager;
import com.voidcyan.client.module.HealthBarRenderer;
import com.voidcyan.client.module.HealthHeartsRenderer;
import com.voidcyan.client.module.HitboxRenderer;
import com.voidcyan.client.module.KeybindsHudRenderer;
import com.voidcyan.client.module.KeystrokesModes;
import com.voidcyan.client.module.LogoutSpotsManager;
import com.voidcyan.client.module.LowHealthAlarmManager;
import com.voidcyan.client.module.MmoArmorHudRenderer;
import com.voidcyan.client.module.MouseStrokesRenderer;
import com.voidcyan.client.module.Notification;
import com.voidcyan.client.module.PlayerTrailManager;
import com.voidcyan.client.module.PotWarningManager;
import com.voidcyan.client.module.PotionStatusRenderer;
import com.voidcyan.client.module.ServerInfoManager;
import com.voidcyan.client.module.StopwatchManager;
import com.voidcyan.client.module.StreakManager;
import com.voidcyan.client.module.SystemResourcesManager;
import com.voidcyan.client.module.TargetHudRenderer;
import com.voidcyan.client.module.TextHudRenderer;
import com.voidcyan.client.module.TntTimerRenderer;
import com.voidcyan.client.module.TotemTraceManager;
import com.voidcyan.client.module.TransparentShieldRenderer;
import com.voidcyan.client.module.NameTagItemsRenderer;
import com.voidcyan.client.module.WatermarkManager;
import com.voidcyan.client.module.WaypointRenderer;
import com.voidcyan.client.module.damagehearts.DamageHeartsModule;
import com.voidcyan.client.module.damagehearts.HeartRenderer;
import com.voidcyan.client.module.soupvisuals.AttackIndicator;
import com.voidcyan.client.screen.AnimatedTexture;
import com.voidcyan.client.screen.ClickGuiScreen;
import com.voidcyan.client.screen.DropdownGuiScreen;
import com.voidcyan.client.screen.EditHudScreen;
import com.voidcyan.client.social.FriendManager;
import com.voidcyan.client.util.NameProtect;
import com.voidcyan.client.util.NameProtectMappings;
import com.voidcyan.client.util.NoteManager;
import com.voidcyan.client.util.WaypointManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.ModifyGame;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.AfterEntities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.biome.Biome;
import org.lwjgl.glfw.GLFW;

public class VoidCyanClient implements ClientModInitializer {
   public static Map<String, Integer> moduleKeybinds = new HashMap<>();
   private static Map<String, Boolean> moduleKeybindsWasPressed = new HashMap<>();
   public static Identifier customPreviewTexture = null;
   private static boolean guiKeyWasDown = false;
   private static boolean dropdownGuiKeyWasDown = false;
   public static int guiProfile = 0;
   public static int guiBgOpacity = 128;
   public static String guiBackgroundImagePath = "";
   public static boolean guiBgEnabled = false;
   public static boolean editGuiBgEnabled = false;
   public static boolean clickGuiBgEnabled = true;
   public static int primaryColorR = 0;
   public static int primaryColorG = 255;
   public static int primaryColorB = 255;
   public static int secondaryColorR = 255;
   public static int secondaryColorG = 0;
   public static int secondaryColorB = 255;
   public static boolean rgbChromaEnabled = false;
   public static String colorTheme = "Cyan";
   public static int rgbSpeed = 5;
   public static final String[] THEMES = new String[]{
      "Cyan", "Red", "OLED Black", "Rainbow", "White", "Pink", "Orange", "Blue", "Black", "Magenta", "Purple",
      "Lime", "Emerald", "Sunset", "Gold", "Rose", "Lavender", "Coral", "Ice Blue", "Mint", "Midnight"
   };
   public static int hudFpsLimit = 0;
   private static long lastHudUpdateTime = 0L;
   private static boolean configDirty = false;
   private static String cachedFpsText = "FPS: --";
   private static String cachedCoordsText = "XYZ: -- -- -- [-]";
   private static int cachedCoordsWidth = 130;
   public static boolean isInvHudEnabled = true;
   public static boolean invHudTransparent = false;
   public static boolean isFpsCounterEnabled = false;
   public static boolean isCoordinatesEnabled = false;
   public static boolean isCpsEnabled = false;
   public static boolean isKeystrokesEnabled = false;
   public static boolean isCompassEnabled = false;
   public static boolean isTargetHudEnabled = false;
   public static boolean isStatsHudEnabled = true;
   public static boolean isFreelookEnabled = true;
   public static boolean isZoomEnabled = true;
   public static int zoomKey = 67;
   public static boolean zoomActive = false;
   public static boolean zoomKeyWasPressed = false;
   public static boolean zoomToggleMode = false;
   public static double zoomLevel = 2.0;
   public static double zoomScrollSensitivity = 1.0;
   public static double currentZoomMultiplier = 1.0;
   public static boolean zoomSmoothAnimation = true;
   public static float zoomAnimationSpeed = 0.15F;
   public static int freelookKey = 342;
   public static boolean freelookKeyWasPressed = false;
   private static boolean prevZoomActive = false;
   private static boolean comboResetKeyWasPressed = false;
   private static boolean targetHudResetKeyWasPressed = false;
   private static boolean prevFreelookActive = false;
   public static boolean freelookToggleMode = false;
   public static boolean freelookShowOwnNametag = false;
   public static boolean freelookActive = false;
   public static float freelookYaw = 0.0F;
   public static float freelookPitch = 0.0F;
   public static Perspective preFreelookPerspective = Perspective.FIRST_PERSON;
   public static boolean isNickHiderEnabled = false;
   public static boolean isArmorStatusEnabled = false;
   public static int sessionKills = 0;
   public static int sessionDeaths = 0;
   public static int sessionCrystalDeaths = 0;
   public static int sessionPops = 0;
   public static int sessionCrystalKills = 0;
   public static int sessionAnchorKills = 0;
   public static int sessionClicks = 0;
   public static int sessionHits = 0;
   public static int sessionAttributeSwaps = 0;
   public static int allTimeAttributeSwaps = 0;
   public static int lastSelectedSlot = -1;
   public static long lastSlotSwapTime = 0L;
   public static double sessionDamage = 0.0;
   public static int sessionBlocksBroken = 0;
   public static int sessionBlocksPlaced = 0;
   public static int sessionCrystalsPlaced = 0;
   public static int sessionCrystalsBroken = 0;
   public static int sessionAnchorsPlaced = 0;
   public static int sessionAnchorsBlown = 0;
   public static int sessionAnchorsCharged = 0;
   public static long sessionPlayTimeTicks = 0L;
   public static int allTimeKills = 0;
   public static int allTimeDeaths = 0;
   public static int allTimeCrystalDeaths = 0;
   public static int allTimePops = 0;
   public static int allTimeCrystalKills = 0;
   public static int allTimeAnchorKills = 0;
   public static int allTimeClicks = 0;
   public static int allTimeHits = 0;
   public static double allTimeDamage = 0.0;
   public static int allTimeBlocksBroken = 0;
   public static int allTimeBlocksPlaced = 0;
   public static int allTimeCrystalsPlaced = 0;
   public static int allTimeCrystalsBroken = 0;
   public static int allTimeAnchorsPlaced = 0;
   public static int allTimeAnchorsBlown = 0;
   public static int allTimeAnchorsCharged = 0;
   public static long allTimePlayTimeTicks = 0L;
   public static final Set<Integer> damageCountedThisTick = new HashSet<>();
   public static Entity lastAttackedEntity = null;
   public static long lastAttackTime = 0L;
   public static long lastInteractTime = 0L;
   public static BlockPos lastInteractPos = null;
   private static boolean wasLocalPlayerDeadLastTick = false;
   private static int saveTicks = 0;
   public static boolean isCustomHitboxEnabled = false;
   public static boolean isCustomHitboxDynamic = false;
   public static int hitboxRed = 255;
   public static int hitboxGreen = 0;
   public static int hitboxBlue = 0;
   public static float hitboxThickness = 2.0F;
   public static boolean hitboxShowCrystal = true;
   public static int crystalHitboxRed = 0;
   public static int crystalHitboxGreen = 200;
   public static int crystalHitboxBlue = 255;
   public static boolean isDamageHeartsEnabled = false;
   public static boolean isHealthIndicatorsEnabled = false;
   public static int healthIndicatorsMode = 0;
   public static int healthIndicatorsType = 0;
   public static boolean healthIndicatorsFuseAbsorption = true;
   public static int healthIndicatorsRange = 20;
   public static float targetIndicatorMaxReach = 64.0F;
   public static boolean targetIndicatorSpinning = true;
   public static float targetIndicatorSpinSpeed = 1.0F;
   public static boolean targetIndicatorHitReachOnly = false;
   public static boolean attackIndicatorEnabled = false;
   public static boolean attackIndicatorOnlyPlayers = false;
   public static boolean attackIndicatorAllowNonLiving = false;
   public static boolean antiBot = true;
   public static boolean attackIndicatorAlwaysActive = true;
   public static String attackIndicatorStyle = "LEGACY";
   public static boolean isAttackHudEnabled = false;
   public static int attackHudMode = 0;
   public static int attackHudSize = 32;
   public static int attackHudX = 10;
   public static int attackHudY = 10;
   public static boolean isComboCounterEnabled = false;
   public static int comboCounterX = 10;
   public static int comboCounterY = 250;
   public static float comboCounterScale = 1.0F;
   public static int comboResetKeybind = 0;
   public static int currentCombo = 0;
   public static long lastComboHitTime = 0L;
   public static int comboTimeout = 2000;
   public static int comboVersion = 0;
   public static boolean isPingDisplayEnabled = false;
   public static int pingDisplayX = 10;
   public static int pingDisplayY = 280;
   public static float pingDisplayScale = 1.0F;
   public static boolean isServerInfoEnabled = false;
   public static int serverInfoX = 10;
   public static int serverInfoY = 300;
   public static float serverInfoScale = 1.0F;
   public static boolean isHurtcamEnabled = true;
   public static boolean isPortalEnabled = true;
   public static boolean isPumpkinEnabled = true;
   public static boolean isWaterFogEnabled = true;
   public static boolean isFireEnabled = true;
   public static boolean isViewModelEnabled = false;
   public static boolean isCustomFovEnabled = false;
   public static int customFov = 110;
   public static boolean isMainHandViewModelEnabled = false;
   public static boolean isOffHandViewModelEnabled = false;
   public static VoidCyanClient.ViewModelSettings mainHandGlobal = new VoidCyanClient.ViewModelSettings();
   public static VoidCyanClient.ViewModelSettings offHandGlobal = new VoidCyanClient.ViewModelSettings();
   public static Map<String, VoidCyanClient.ViewModelSettings> mainHandItemOverrides = new HashMap<>();
   public static Map<String, VoidCyanClient.ViewModelSettings> offHandItemOverrides = new HashMap<>();
   public static boolean isItemAnimationsEnabled = false;
   public static int itemAnimationMode = 0;
   public static float customSwingPitch = -20.0F;
   public static float customSwingYaw = -40.0F;
   public static float customSwingRoll = 20.0F;
   public static boolean isParticlesEnabled = true;
   public static boolean isExplosionParticlesEnabled = true;
   public static boolean isPotionParticlesEnabled = true;
   public static boolean isCriticalParticlesEnabled = true;
   public static boolean isDamageParticlesEnabled = true;
   public static OverlayTexture overlayTextureInstance = null;
   private static int hitColorEntityId = -1;
   private static long hitColorUntil = 0L;
   private static int totemPopColorEntityId = -1;
   private static long totemPopColorUntil = 0L;
   private static int damageColorEntityId = -1;
   private static long damageColorUntil = 0L;
   public static boolean isChatHeadsEnabled = false;
   public static boolean isNotificationsEnabled = true;
   public static boolean isAppleSkinEnabled = false;
   public static boolean isNotificationsAnimEnabled = true;
   public static int guiAnimationDurationMs = 300;
   public static float clickGuiScale = 1.0F;
   public static int guiType = 0;
   public static int notificationsAnimDirection = 0;
   public static int notificationsX = 10;
   public static int notificationsY = 100;
   public static float notificationsScale = 1.0F;
   public static List<Notification> activeNotifications = new ArrayList<>();
   public static boolean isSpeedDisplayEnabled = false;
   public static int speedDisplayX = 10;
   public static int speedDisplayY = 320;
   public static float speedDisplayScale = 1.0F;
   public static boolean isBiomeDisplayEnabled = false;
   public static int biomeDisplayX = 10;
   public static int biomeDisplayY = 340;
   public static float biomeDisplayScale = 1.0F;
   public static boolean isEntityCounterEnabled = false;
   public static int entityCounterX = 10;
   public static int entityCounterY = 200;
   public static float entityCounterScale = 1.0F;
   public static boolean entityCounterCurrentChunkOnly = false;
   public static boolean entityCounterShowPlayers = true;
   public static boolean entityCounterShowHostile = true;
   public static boolean entityCounterShowAnimals = true;
   public static boolean entityCounterShowItems = true;
   public static boolean entityCounterShowTotal = true;
   public static boolean isIrlClockEnabled = false;
   public static boolean isIrlDateEnabled = false;
   public static boolean isIrlClock24Hour = false;
   public static int irlClockX = 10;
   public static int irlClockY = 360;
   public static float irlClockScale = 1.0F;
   public static boolean isTotemCounterEnabled = false;
   public static int totemCounterX = 10;
   public static int totemCounterY = 380;
   public static float totemCounterScale = 1.0F;
   public static boolean isArrayListEnabled = false;
   public static int arrayListSortMode = 0;
   public static int arrayListX = 10;
   public static int arrayListY = 50;
   public static float arrayListScale = 1.0F;
   public static boolean arrayListBackground = true;
   public static boolean isArrowCounterEnabled = false;
   public static int arrowCounterX = 10;
   public static int arrowCounterY = 400;
   public static float arrowCounterScale = 1.0F;
   public static boolean isReachDisplayEnabled = false;
   public static int reachDisplayX = 10;
   public static int reachDisplayY = 420;
   public static float reachDisplayScale = 1.0F;
   public static float lastReachDistance = 0.0F;
   public static long lastReachTime = 0L;
   public static boolean isPackDisplayEnabled = false;
   public static int packDisplayX = 10;
   public static int packDisplayY = 310;
   public static float packDisplayScale = 1.0F;
   public static boolean isBlockIndicatorEnabled = false;
   public static int blockIndicatorX = 10;
   public static int blockIndicatorY = 340;
   public static float blockIndicatorScale = 1.0F;
   public static boolean isCustomCrosshairEnabled = false;
   public static int crosshairStyle = 0;
   public static int crosshairRed = 255;
   public static int crosshairGreen = 255;
   public static int crosshairBlue = 255;
   public static int crosshairAlpha = 255;
   public static int crosshairSize = 5;
   public static int crosshairThickness = 1;
   public static boolean crosshairDot = false;
   public static boolean isCustomF3Enabled = false;
   public static boolean isDeathInfoEnabled = false;
   public static boolean deathInfoShowOnScreen = true;
   public static int deathInfoX = 10;
   public static int deathInfoY = 370;
   public static float deathInfoScale = 1.0F;
   public static int lastDeathX = 0;
   public static int lastDeathY = 0;
   public static int lastDeathZ = 0;
   public static String lastDeathDimension = "";
   public static long lastDeathTime = 0L;
   public static boolean hasDeathInfo = false;
   public static boolean isWaypointsEnabled = false;
   public static boolean isTpsDisplayEnabled = false;
   public static int tpsDisplayX = 10;
   public static int tpsDisplayY = 400;
   public static float tpsDisplayScale = 1.0F;
   public static boolean isSystemResourcesEnabled = false;
   public static int systemResourcesX = 10;
   public static int systemResourcesY = 50;
   public static float systemResourcesScale = 1.0F;
   public static boolean isStopwatchEnabled = false;
   public static boolean stopwatchRunning = true;
   public static int stopwatchKey = -1;
   public static boolean stopwatchKeyWasPressed = false;
   public static int stopwatchX = 10;
   public static int stopwatchY = 50;
   public static float stopwatchScale = 1.0F;
   public static boolean stopwatchBackground = true;
   public static boolean isTextHudEnabled = false;
   public static String textHudString = "Custom Text";
   public static int textHudTextColor = -1;
   public static int textHudBgColor = 0;
   public static int textHudBgAlpha = 128;
   public static int textHudX = 10;
   public static int textHudY = 10;
   public static float textHudScale = 1.0F;
   public static boolean isWatermarkEnabled = true;
   public static int watermarkX = 10;
   public static int watermarkY = 10;
   public static float watermarkScale = 1.0F;
   public static boolean watermarkAnimatedColor = true;
   public static boolean watermarkUseThemeColor = true;
   public static int watermarkCustomColor = 16777215;
   public static boolean isMapTransformationEnabled = false;
   public static boolean isMapHoverPreviewEnabled = true;
   public static boolean isKeybindsDisplayEnabled = false;
   public static int keybindsHudX = 10;
   public static int keybindsHudY = 100;
   public static float keybindsHudScale = 1.0F;
   public static boolean isFpsGraphEnabled = false;
   public static int fpsGraphX = 10;
   public static int fpsGraphY = 50;
   public static float fpsGraphScale = 1.0F;
   public static boolean showFpsAvg = true;
   public static boolean showFpsMinMax = true;
   public static boolean showFps1Percent = true;
   public static boolean showFps01Percent = true;
   public static boolean fpsGraphBackground = true;
   public static boolean isTotemTraceEnabled = false;
   public static int totemTraceDuration = 10;
   public static int totemTraceColorRed = 0;
   public static int totemTraceColorGreen = 255;
   public static int totemTraceColorBlue = 0;
   public static int totemTraceColorAlpha = 255;
   public static boolean totemTraceShowColor = true;
   public static boolean totemTraceShowArmor = true;
   public static boolean totemTraceDetectSelf = true;
   public static boolean totemTraceDetectOthers = true;
   public static int totemPopHealthThreshold = 6;
   public static boolean isPlayerTrailEnabled = false;
   public static boolean playerTrailShowSelf = true;
   public static boolean playerTrailShowOthers = true;
   public static int playerTrailColor = 65280;
   public static int playerTrailColorR = 0;
   public static int playerTrailColorG = 255;
   public static int playerTrailColorB = 0;
   public static float playerTrailDuration = 5.0F;
   public static int playerTrailAnchor = 1;
   public static long stopwatchStartedAt = 0L;
   public static long stopwatchElapsedMs = 0L;
   public static boolean isTntTimerEnabled = false;
   private static long lastTickTime = 0L;
   private static final ArrayDeque<Long> tickIntervals = new ArrayDeque<>();
   public static double currentTps = 20.0;
   public static boolean isTimeChangerEnabled = false;
   public static long customTime = 6000L;
   public static float attackIndicatorLiveTime = 3.0F;
   public static String attackIndicatorLegacyTexture = "LEGACY";
   public static int attackIndicatorLegacyRollSpeed = 70;
   public static int attackIndicatorLegacyScale = 60;
   public static int attackIndicatorLegacyAlpha = 80;
   public static String attackIndicatorSoulStyle = "SMOKE";
   public static String attackIndicatorSoulTexture = "ALT";
   public static int attackIndicatorSoulLength = 5;
   public static float attackIndicatorSoulFactor = 3.0F;
   public static float attackIndicatorSoulShaking = 3.0F;
   public static float attackIndicatorSoulAmplitude = 3.0F;
   public static float attackIndicatorSoulRadius = 100.0F;
   public static float attackIndicatorSoulStartSize = 50.0F;
   public static float attackIndicatorSoulEndSize = 20.0F;
   public static float attackIndicatorSoulScale = 40.0F;
   public static int attackIndicatorSoulSubdivision = 3;
   public static float attackIndicatorTopkaRadius = 50.0F;
   public static float attackIndicatorTopkaSpeed = 30.0F;
   public static int invHudX = 10;
   public static int invHudY = 10;
   public static float invHudScale = 1.0F;
   public static int fpsCounterX = 10;
   public static int fpsCounterY = 35;
   public static float fpsCounterScale = 1.0F;
   public static int coordinatesX = 10;
   public static int coordinatesY = 60;
   public static float coordinatesScale = 1.0F;
   public static int cpsX = 10;
   public static int cpsY = 110;
   public static float cpsScale = 1.0F;
   public static int keystrokesX = 10;
   public static int keystrokesY = 135;
   public static float keystrokesScale = 1.0F;
   public static int keystrokesMode = 4;
   public static int compassX = 10;
   public static int compassY = 165;
   public static float compassScale = 1.0F;
   public static int armorStatusX = 10;
   public static int armorStatusY = 195;
   public static float armorStatusScale = 1.0F;
   public static int targetHudX = 150;
   public static int targetHudY = 150;
   public static float targetHudScale = 1.0F;
   public static int targetHudResetKeybind = 0;
   public static boolean targetHudMmoArmorDisplay = false;
   public static int targetHudMmoArmorX = 150;
   public static int targetHudMmoArmorY = 215;
   public static float targetHudMmoArmorScale = 1.0F;
   public static int statsHudX = 10;
   public static int statsHudY = 100;
   public static float statsHudScale = 1.0F;
   public static boolean statsHudShowKills = true;
   public static boolean statsHudShowDeaths = true;
   public static boolean statsHudShowKD = false;
   public static boolean statsHudShowPops = true;
   public static boolean statsHudShowClicks = false;
   public static boolean statsHudShowHits = false;
   public static boolean statsHudShowAttributeSwap = false;
   public static boolean statsHudShowAccuracy = true;
   public static boolean statsHudShowDamage = false;
   public static boolean statsHudShowBlocksBroken = false;
   public static boolean statsHudShowBlocksPlaced = false;
   public static boolean statsHudShowCrystalsPlaced = false;
   public static boolean statsHudShowCrystalsBroken = false;
   public static boolean statsHudShowAnchorsPlaced = false;
   public static boolean statsHudShowAnchorsBlown = false;
   public static boolean statsHudShowAnchorsCharged = false;
   public static boolean statsHudShowCrystalKills = false;
   public static boolean statsHudShowCrystalDeaths = false;
   public static boolean statsHudShowAnchors = true;
   public static boolean statsHudShowTime = true;
   public static int statsHudStatMode = 0;
   public static boolean statsHudShowInMultiplayer = true;
   public static boolean targetHudShowArmor = true;
   public static boolean targetHudShowArmorDurability = true;
   public static boolean targetHudShowEnemyCrucials = true;
   public static boolean targetHudShowEnemyPearls = true;
   public static boolean targetHudShowEnemyGapples = true;
   public static boolean targetHudShowEnemyCobwebs = true;
   public static boolean targetHudShowEnemyTotems = true;
   public static boolean targetHudShowEnemyPops = true;
   public static boolean targetHudShowEnemyWindCharges = false;
   public static boolean targetHudShowEnemyWindChargesUsed = false;
   public static boolean targetHudShowEnemyHealthPots = false;
   public static boolean targetHudStickyEnabled = true;
   public static int targetHudStickyDuration = 3;
   public static boolean targetHudAboveHead = false;
   public static float targetHudAboveHeadScale = 1.0F;
   public static int enemyCrucialsHudX = 320;
   public static int enemyCrucialsHudY = 150;
   public static float enemyCrucialsHudScale = 1.0F;
   public static boolean isPotionStatusEnabled = false;
   public static int potionStatusX = 10;
   public static int potionStatusY = 230;
   public static float potionStatusScale = 1.0F;
   public static int potionStatusOrientation = 0;
   public static boolean potionStatusShowDuration = true;
   public static boolean potionStatusShowAmplifier = true;
   public static int potionStatusIconType = 0;
   public static boolean disableHotbarLooping = false;
   public static boolean isFullbrightEnabled = false;
   public static boolean isBigItemsEnabled = false;
   public static boolean isItemPhysicsEnabled = false;
   public static float itemPhysicsRotationX = 90.0F;
   public static float itemPhysicsOffsetY = -0.1F;
   public static float itemPhysicsOffsetZ = -0.2F;
   public static boolean itemPhysicsFallFlips = true;
   public static int itemPhysicsFlipsPerBlock = 2;
   public static boolean isPlayerModelEnabled = false;
   public static String bigItemsItemIds = "minecraft:golden_apple";
   public static float bigItemsScale = 10.0F;
   public static boolean isLowHealthAlarmEnabled = false;
   public static boolean lowHealthAlarmSound = true;
   public static boolean lowHealthAlarmText = true;
   public static int lowHealthAlarmThreshold = 10;
   public static int lowHealthTotemThreshold = 1;
   public static int lowHealthAlarmX = 10;
   public static int lowHealthAlarmY = 10;
   public static float lowHealthAlarmScale = 1.0F;
   public static boolean lowHealthUseThemeColor = false;
   public static String lowHealthAlarmSoundFile = "Default Beep";
   public static boolean isPotWarningEnabled = false;
   public static boolean potWarnLowPots = true;
   public static int potWarnPotThreshold = 3;
   public static boolean potWarnLowEffects = true;
   public static int potWarnEffectThreshold = 400;
   public static boolean potWarnSound = true;
   public static boolean potWarnShowText = true;
   public static boolean potWarnUseThemeColor = true;
   public static String potWarnIgnoredEffects = "";
   public static String potWarnSoundFile = "Default Beep";
   public static int potWarningX = 10;
   public static int potWarningY = 30;
   public static float potWarningScale = 1.0F;
   public static boolean isDropPreventionEnabled = false;
   public static String dropPreventionItems = "minecraft:totem_of_undying";
   public static int dropPreventionX = 10;
   public static int dropPreventionY = 50;
   public static float dropPreventionScale = 1.0F;
   public static boolean isHitColorEnabled = false;
   public static boolean hitColorApplyToSelf = true;
   public static boolean hitColorApplyToArmor = true;
   public static boolean hitColorApplyToArmorStands = false;
   public static int hitColorRed = 255;
   public static int hitColorGreen = 0;
   public static int hitColorBlue = 0;
   public static int hitColorAlpha = 153;
   public static int hitColorDuration = 10;
   public static int customHitboxRange = 20;
   public static boolean isTotemPopColorEnabled = false;
   public static boolean totemPopColorDetectSelf = true;
   public static boolean totemPopColorDetectOthers = true;
   public static boolean totemPopColorApplyToArmor = true;
   public static boolean totemPopColorApplyToArmorStands = false;
   public static int totemPopColorRed = 255;
   public static int totemPopColorGreen = 215;
   public static int totemPopColorBlue = 0;
   public static int totemPopColorAlpha = 153;
   public static int totemPopColorDuration = 20;
   public static boolean isDamageColorEnabled = false;
   public static boolean damageColorApplyToSelf = true;
   public static boolean damageColorApplyToPlayers = false;
   public static boolean damageColorApplyToEntities = false;
   public static boolean damageColorApplyToArmor = true;
   public static boolean damageColorApplyToArmorStands = false;
   public static int damageColorRed = 255;
   public static int damageColorGreen = 0;
   public static int damageColorBlue = 0;
   public static int damageColorAlpha = 100;
   public static int damageColorDuration = 10;
   private static Vec3d lastTrackerPos = null;
   private static long lastTrackerTime = 0L;
   public static boolean isToggleSprintEnabled = false;
   public static boolean isToggleSneakEnabled = false;
   public static boolean sprintToggled = false;
   public static boolean sneakToggled = false;
   public static int toggleSprintX = 10;
   public static int toggleSprintY = 220;
   public static int toggleSneakX = 10;
   public static int toggleSneakY = 235;
   private static double dragOffsetX = 0.0;
   private static int dragOffsetY = 0;
   private static int[] lastDurability = new int[]{-1, -1, -1, -1};
   private static final WeakHashMap<LivingEntity, Float> entityHealthMap = new WeakHashMap<>();
   public static final List<Long> leftClickTimestamps = new ArrayList<>();
   public static final List<Long> rightClickTimestamps = new ArrayList<>();
   private static boolean wasLeftMouseDown = false;
   private static boolean wasRightMouseDown = false;
   private static final List<Long> keystrokeTimestamps = new ArrayList<>();
   public static Set<Integer> pressedKeys = new HashSet<>();
   public static boolean keystrokesShowMouse = true;
   public static String nickHiderTargetName = "";
   public static String nickHiderReplacementName = "Hidden";
   public static boolean nickHiderUseCustomColor = false;
   public static int nickHiderColor = 16733695;
   public static boolean isPeerNickEnabled = false;
   public static List<String> friends = new ArrayList<>();
   public static boolean isFriendGreenNameTagsEnabled = false;
   public static boolean hideInvisNametags = false;
   public static boolean safeModeEnabled = false;
   public static boolean isNameTagItemsEnabled = false;
   public static int nameTagItemsDisplayMode = 0;
   public static boolean nameTagItemsGreyWhenUsing = true;
   public static boolean nameTagItemsShowMainHand = true;
   public static boolean nameTagItemsShowOffHand = true;
   public static boolean nameTagItemsShowTotemPop = true;
   public static boolean nameTagItemsShowArmor = true;
   public static boolean nameTagItemsShowDurability = true;
   public static boolean nameTagItemsShowSelf = false;
   public static boolean nameTagItemsOnlyFriends = false;
   public static float nameTagItemsScale = 1.0F;
   public static int invHudBorderThickness = 2;
   public static int invHudBorderColor = -16711681;
   public static boolean invHudShowHotbar = false;
   public static boolean invHudShowItemCount = true;
   public static int armorStatusDisplayMode = 0;
   public static boolean armorStatusShowDurabilityBar = true;
   public static int armorStatusDurabilityBarColor = -16711936;
   public static boolean armorStatusShowEmptySlots = false;
   public static int armorStatusOrientation = 0;
   public static int armorStatusWarningThreshold = 20;
   public static boolean armorStatusWarningSound = true;
   public static int armorStatusWarningSoundType = 0;
   public static String armorStatusWarningSoundFile = "Default Beep";
   public static boolean armorStatusTransparentBg = false;
   public static int editGuiKey = 93; // ] key
   public static boolean isBigHeadEnabled = false;
   public static boolean bigHeadSelf = true;
   public static boolean bigHeadFriends = true;
   public static boolean bigHeadOthers = false;
   public static float bigHeadScale = 1.5F;
   public static boolean isBlockOverlayEnabled = false;
   /** 0 = Full Block, 1 = Air Exposed (see BlockOverlayRenderer). */
   public static int blockOverlayMode = 0;
   public static boolean blockOverlayOutline = true;
   public static boolean blockOverlayFill = false;
   public static boolean blockOverlayGlow = false;
   public static int blockOverlayRed = 0;
   public static int blockOverlayGreen = 255;
   public static int blockOverlayBlue = 255;
   public static float blockOverlayThickness = 2.0F;
   public static int blockOverlayFillOpacity = 25;
   public static float blockOverlayGlowStrength = 1.0F;
   public static boolean isChinaHatEnabled = false;
   public static int chinaHatRed = 0;
   public static int chinaHatGreen = 0;
   public static int chinaHatBlue = 0;
   public static boolean chinaHatFilled = false;
   public static float chinaHatSize = 1.0F;
   public static boolean isInvHighlightEnabled = false;
   public static String invHighlightItems = "minecraft:totem_of_undying";
   public static int invHighlightRed = 255;
   public static int invHighlightGreen = 0;
   public static int invHighlightBlue = 0;
   public static boolean isMouseStrokesEnabled = false;
   public static int mouseStrokesX = 100;
   public static int mouseStrokesY = 100;
   public static float mouseStrokesScale = 1.0F;
   public static boolean isTotemPopNotifierEnabled = false;
   public static boolean mouseStrokesShowCross = true;
   public static float mouseStrokesSensitivity = 2.0F;
   public static int mouseStrokesSymbolRed = 0;
   public static int mouseStrokesSymbolGreen = 150;
   public static int mouseStrokesSymbolBlue = 255;
   public static int mouseStrokesBoxRed = 255;
   public static int mouseStrokesBoxGreen = 255;
   public static int mouseStrokesBoxBlue = 255;
   public static boolean isTransparentShieldEnabled = false;
   public static int transparentShieldX = 10;
   public static int transparentShieldY = 25;
   public static float transparentShieldScale = 1.0F;
   public static int transparentShieldStatusColor = -2147418368;
   public static int transparentShieldStatusOutlineColor = -1;
   public static boolean isLogoutSpotsEnabled = false;
   public static boolean logoutSpotsDetectDisappears = true;
   public static boolean logoutSpotsLeaveMessages = true;
   public static boolean logoutSpotsIgnoreFriends = true;
   public static boolean logoutSpotsCreateWaypoints = false;
   public static boolean logoutSpotsSaveRotation = false;
   public static boolean logoutSpotsSaveHeldItem = false;
   private static final File CONFIG_FILE = new File("config/voidcyan-client.properties");
   private static final File NAMED_CONFIG_DIRECTORY = new File("config/voidcyan-client-configs");
   private static int lastKnownPing = 0;
   public static AnimatedTexture mainBackground = null;

   public static int getModuleKey(String moduleName) {
      return moduleKeybinds.getOrDefault(moduleName, -1);
   }

   public static void setModuleKey(String moduleName, int key) {
      moduleKeybinds.put(moduleName, key);
   }

   /** Modules that cannot be toggled on while Safe Mode is active. */
   private static final Set<String> SAFE_MODE_BLOCKED_MODULES = Set.of("Health Indicators", "Name Tag Items", "Target HUD");

   /** True when Safe Mode prevents toggling the given module on. */
   public static boolean isSafeModeBlocked(String moduleName) {
      return safeModeEnabled && moduleName != null && SAFE_MODE_BLOCKED_MODULES.contains(moduleName);
   }

   public static void showSafeModeBlockedNotification(String moduleName) {
      showNotification("Safe Mode: " + moduleName + " is locked");
   }

   public static void toggleModule(String name) {
      switch (name) {
         case "Inv Highlight":
            isInvHighlightEnabled = !isInvHighlightEnabled;
            break;
         case "Target HUD":
            if (isSafeModeBlocked(name)) {
               showSafeModeBlockedNotification(name);
               break;
            }

            isTargetHudEnabled = !isTargetHudEnabled;
            break;
         case "Combo Counter":
            isComboCounterEnabled = !isComboCounterEnabled;
            break;
         case "Ping Display":
            isPingDisplayEnabled = !isPingDisplayEnabled;
            break;
         case "Server Info":
            isServerInfoEnabled = !isServerInfoEnabled;
            break;
         case "FreeLook":
            isFreelookEnabled = !isFreelookEnabled;
            break;
         case "Zoom":
            isZoomEnabled = !isZoomEnabled;
            break;
         case "Fullbright":
            isFullbrightEnabled = !isFullbrightEnabled;
            break;
         case "Big Items":
            isBigItemsEnabled = !isBigItemsEnabled;
            break;
         case "Item Physics":
            isItemPhysicsEnabled = !isItemPhysicsEnabled;
            break;
         case "Low Health Alarm":
            isLowHealthAlarmEnabled = !isLowHealthAlarmEnabled;
            break;
         case "Pot Warning":
            isPotWarningEnabled = !isPotWarningEnabled;
            break;
         case "Hit Color":
            isHitColorEnabled = !isHitColorEnabled;
            break;
         case "Drop Prevention":
            isDropPreventionEnabled = !isDropPreventionEnabled;
            break;
         case "Custom Hitbox":
            isCustomHitboxEnabled = !isCustomHitboxEnabled;
            break;
         case "Damage Hearts":
            isDamageHeartsEnabled = !isDamageHeartsEnabled;
            break;
         case "Health Indicators":
            if (isSafeModeBlocked(name)) {
               showSafeModeBlockedNotification(name);
               break;
            }

            isHealthIndicatorsEnabled = !isHealthIndicatorsEnabled;
            break;
         case "Custom Crosshair":
            isCustomCrosshairEnabled = !isCustomCrosshairEnabled;
            break;
         case "Waypoints":
            isWaypointsEnabled = !isWaypointsEnabled;
            break;
         case "Attack Indicator":
            attackIndicatorEnabled = !attackIndicatorEnabled;
            break;
         case "Custom F3":
            isCustomF3Enabled = !isCustomF3Enabled;
            break;
         case "Totem Trace":
            isTotemTraceEnabled = !isTotemTraceEnabled;
            break;
         case "Name Tag Items":
            if (isSafeModeBlocked(name)) {
               showSafeModeBlockedNotification(name);
               break;
            }

            isNameTagItemsEnabled = !isNameTagItemsEnabled;
            break;
         case "Nick Hider":
            isNickHiderEnabled = !isNickHiderEnabled;
            break;
         case "Peer Nick":
            isPeerNickEnabled = !isPeerNickEnabled;
            break;
         case "Notifications":
            isNotificationsEnabled = !isNotificationsEnabled;
            break;
         case "Saturation":
            isAppleSkinEnabled = !isAppleSkinEnabled;
            break;
         case "Player Trail":
            isPlayerTrailEnabled = !isPlayerTrailEnabled;
            break;
         case "Stopwatch":
            isStopwatchEnabled = !isStopwatchEnabled;
            break;
         case "TPS Display":
            isTpsDisplayEnabled = !isTpsDisplayEnabled;
            break;
         case "Watermark":
            isWatermarkEnabled = !isWatermarkEnabled;
            break;
         case "Keybinds Display":
            isKeybindsDisplayEnabled = !isKeybindsDisplayEnabled;
            break;
         case "ArrayList":
            isArrayListEnabled = !isArrayListEnabled;
            break;
         case "FPS Graph":
            isFpsGraphEnabled = !isFpsGraphEnabled;
            break;
         case "Speed Display":
            isSpeedDisplayEnabled = !isSpeedDisplayEnabled;
            break;
         case "Biome Display":
            isBiomeDisplayEnabled = !isBiomeDisplayEnabled;
            break;
         case "Entity Counter":
            isEntityCounterEnabled = !isEntityCounterEnabled;
            break;
         case "IRL Clock":
            isIrlClockEnabled = !isIrlClockEnabled;
            break;
         case "Totem Counter":
            isTotemCounterEnabled = !isTotemCounterEnabled;
            break;
         case "Arrow Counter":
            isArrowCounterEnabled = !isArrowCounterEnabled;
            break;
         case "Reach Display":
            isReachDisplayEnabled = !isReachDisplayEnabled;
            break;
         case "Pack Display":
            isPackDisplayEnabled = !isPackDisplayEnabled;
            break;
         case "Block Indicator":
            isBlockIndicatorEnabled = !isBlockIndicatorEnabled;
            break;
         case "Death Info":
            isDeathInfoEnabled = !isDeathInfoEnabled;
            break;
         case "System Resources":
            isSystemResourcesEnabled = !isSystemResourcesEnabled;
            break;
         case "Toggle Sprint":
            isToggleSprintEnabled = !isToggleSprintEnabled;
            break;
         case "Toggle Sneak":
            isToggleSneakEnabled = !isToggleSneakEnabled;
            break;
         case "Time Changer":
            isTimeChangerEnabled = !isTimeChangerEnabled;
            break;
         case "TNT Timer":
            isTntTimerEnabled = !isTntTimerEnabled;
            break;
         case "Custom Text":
            isTextHudEnabled = !isTextHudEnabled;
            break;
         case "Big Head":
            isBigHeadEnabled = !isBigHeadEnabled;
            break;
         case "China Hat":
            isChinaHatEnabled = !isChinaHatEnabled;
            break;
         case "Block Overlay":
            isBlockOverlayEnabled = !isBlockOverlayEnabled;
            break;
         case "Mouse Strokes":
            isMouseStrokesEnabled = !isMouseStrokesEnabled;
            break;
         case "Totem Pop Notifier":
            isTotemPopNotifierEnabled = !isTotemPopNotifierEnabled;
            break;
         case "Logout Spots":
            isLogoutSpotsEnabled = !isLogoutSpotsEnabled;
            break;
         case "Transparent Shield":
            isTransparentShieldEnabled = !isTransparentShieldEnabled;
            break;
         case "FPS Counter":
            isFpsCounterEnabled = !isFpsCounterEnabled;
            break;
         case "Coordinates":
            isCoordinatesEnabled = !isCoordinatesEnabled;
            break;
         case "CPS Counter":
            isCpsEnabled = !isCpsEnabled;
            break;
         case "Keystrokes":
            isKeystrokesEnabled = !isKeystrokesEnabled;
            break;
         case "Compass":
            isCompassEnabled = !isCompassEnabled;
            break;
         case "Stats HUD":
            isStatsHudEnabled = !isStatsHudEnabled;
            break;
         case "Effects":
            CritEffectsManager.enabled = !CritEffectsManager.enabled;
            saveConfig();
            break;
         case "Armor Status":
            isArmorStatusEnabled = !isArmorStatusEnabled;
            break;
         case "Potion Status":
            isPotionStatusEnabled = !isPotionStatusEnabled;
            break;
         case "Inv HUD":
            isInvHudEnabled = !isInvHudEnabled;
      }
   }

   public static void applyTheme(String theme) {
      if (theme == null) return;
      colorTheme = theme;
      String lower = theme.toLowerCase().trim();
      if (lower.startsWith("cyan")) {
         rgbChromaEnabled = false;
         primaryColorR = 0; primaryColorG = 255; primaryColorB = 255;
      } else if (lower.equals("red")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 30; primaryColorB = 30;
      } else if (lower.contains("oled")) {
         rgbChromaEnabled = false;
         primaryColorR = 5; primaryColorG = 5; primaryColorB = 5;
      } else if (lower.equals("rainbow")) {
         rgbChromaEnabled = true;
      } else if (lower.equals("white")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 255; primaryColorB = 255;
      } else if (lower.equals("pink")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 105; primaryColorB = 180;
      } else if (lower.equals("orange") || lower.equals("orenge")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 140; primaryColorB = 0;
      } else if (lower.equals("blue")) {
         rgbChromaEnabled = false;
         primaryColorR = 30; primaryColorG = 144; primaryColorB = 255;
      } else if (lower.equals("black")) {
         rgbChromaEnabled = false;
         primaryColorR = 25; primaryColorG = 25; primaryColorB = 25;
      } else if (lower.contains("magnte") || lower.contains("magenta")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 0; primaryColorB = 255;
      } else if (lower.equals("purple")) {
         rgbChromaEnabled = false;
         primaryColorR = 147; primaryColorG = 51; primaryColorB = 234;
      } else if (lower.equals("lime")) {
         rgbChromaEnabled = false;
         primaryColorR = 50; primaryColorG = 205; primaryColorB = 50;
      } else if (lower.equals("emerald")) {
         rgbChromaEnabled = false;
         primaryColorR = 0; primaryColorG = 201; primaryColorB = 87;
      } else if (lower.equals("sunset")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 94; primaryColorB = 77;
      } else if (lower.equals("gold")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 215; primaryColorB = 0;
      } else if (lower.equals("rose")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 51; primaryColorB = 102;
      } else if (lower.equals("lavender")) {
         rgbChromaEnabled = false;
         primaryColorR = 180; primaryColorG = 130; primaryColorB = 240;
      } else if (lower.equals("coral")) {
         rgbChromaEnabled = false;
         primaryColorR = 255; primaryColorG = 127; primaryColorB = 80;
      } else if (lower.equals("ice blue")) {
         rgbChromaEnabled = false;
         primaryColorR = 135; primaryColorG = 206; primaryColorB = 250;
      } else if (lower.equals("mint")) {
         rgbChromaEnabled = false;
         primaryColorR = 64; primaryColorG = 224; primaryColorB = 208;
      } else if (lower.equals("midnight")) {
         rgbChromaEnabled = false;
         primaryColorR = 25; primaryColorG = 25; primaryColorB = 112;
      }
      secondaryColorR = Math.max(0, primaryColorR * 2 / 3);
      secondaryColorG = Math.max(0, primaryColorG * 2 / 3);
      secondaryColorB = Math.max(0, primaryColorB * 2 / 3);
      saveConfig();
   }

   private static long rainbowPrimaryAt = -1L;
   private static int rainbowPrimarySpeed;
   private static int rainbowPrimary;

   // Called dozens of times per frame; the rainbow value only depends on the current millisecond and speed, so memoize it.
   public static int getPrimaryColor() {
      if (rgbChromaEnabled || "Rainbow".equalsIgnoreCase(colorTheme)) {
         long now = System.currentTimeMillis();
         if (now != rainbowPrimaryAt || rgbSpeed != rainbowPrimarySpeed) {
            float speed = Math.max(1, rgbSpeed) * 0.15F;
            float hue = (float)((now * speed) % 3600.0) / 3600.0F;
            rainbowPrimary = 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 0.85F, 1.0F) & 0x00FFFFFF);
            rainbowPrimaryAt = now;
            rainbowPrimarySpeed = rgbSpeed;
         }
         return rainbowPrimary;
      }
      return 0xFF000000 | primaryColorR << 16 | primaryColorG << 8 | primaryColorB;
   }

   public static int getSecondaryColor() {
      if (rgbChromaEnabled || "Rainbow".equalsIgnoreCase(colorTheme)) {
         float speed = Math.max(1, rgbSpeed) * 0.15F;
         float hue = (float)(((System.currentTimeMillis() * speed) + 900.0) % 3600.0) / 3600.0F;
         int rgb = java.awt.Color.HSBtoRGB(hue, 0.85F, 0.8F);
         return 0xFF000000 | (rgb & 0x00FFFFFF);
      }
      return 0xFF000000 | secondaryColorR << 16 | secondaryColorG << 8 | secondaryColorB;
   }

   public static boolean shouldUpdateHudValues() {
      if (hudFpsLimit <= 0) {
         return true;
      } else {
         long now = System.currentTimeMillis();
         long interval = 1000L / hudFpsLimit;
         if (now - lastHudUpdateTime >= interval) {
            lastHudUpdateTime = now;
            return true;
         } else {
            return false;
         }
      }
   }

   public static void updateHitColorTexture() {
      if (isHitColorEnabled) {
         writeHitColorToTexture();
      } else {
         writeVanillaRedToTexture();
      }
   }

   private static int clampedArgb(int a, int r, int g, int b) {
      return MathHelper.clamp(a, 0, 255) << 24 | MathHelper.clamp(r, 0, 255) << 16 | MathHelper.clamp(g, 0, 255) << 8 | MathHelper.clamp(b, 0, 255);
   }

   // Row 3 is rewritten per entity per frame; skip the GPU upload when the colour hasn't changed.
   private static NativeImageBackedTexture lastOverlayTexture;
   private static int lastOverlayArgb;

   private static void writeOverlayRow3(int argbColor) {
      if (overlayTextureInstance != null) {
         try {
            NativeImageBackedTexture texture = ((OverlayTextureAccessor)overlayTextureInstance).voidcyan$getTexture();
            if (texture == null) {
               return;
            }

            NativeImage image = texture.getImage();
            if (image == null || texture == lastOverlayTexture && argbColor == lastOverlayArgb) {
               return;
            }

            for (int u = 0; u < 16; u++) {
               image.setColorArgb(u, 3, argbColor);
            }

            texture.upload();
            lastOverlayTexture = texture;
            lastOverlayArgb = argbColor;
         } catch (Exception ignored) {
         }
      }
   }

   public static void flashHitColor(Entity entity) {
      if (isHitColorEnabled && entity != null) {
         hitColorEntityId = entity.getId();
         hitColorUntil = System.currentTimeMillis() + hitColorDuration * 25;
      }
   }

   public static boolean shouldApplyHitColor(int entityId) {
      return isHitColorEnabled && entityId == hitColorEntityId && System.currentTimeMillis() < hitColorUntil;
   }

   public static void flashTotemPopColor(Entity entity) {
      if (isTotemPopColorEnabled && entity != null) {
         totemPopColorEntityId = entity.getId();
         totemPopColorUntil = System.currentTimeMillis() + totemPopColorDuration * 50;
      }
   }

   public static boolean shouldApplyTotemPopColor(int entityId) {
      return isTotemPopColorEnabled && entityId == totemPopColorEntityId && System.currentTimeMillis() < totemPopColorUntil;
   }

   public static void flashDamageColor(Entity entity) {
      if (isDamageColorEnabled && entity != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            boolean isSelf = entity.getId() == client.player.getId();
            boolean isPlayer = entity instanceof PlayerEntity;
            if ((!isSelf || damageColorApplyToSelf) && (isSelf || !isPlayer || damageColorApplyToPlayers) && (isSelf || isPlayer || damageColorApplyToEntities)) {
               damageColorEntityId = entity.getId();
               damageColorUntil = System.currentTimeMillis() + damageColorDuration * 50;
            }
         }
      }
   }

   public static boolean shouldApplyDamageColor(int entityId) {
      return isDamageColorEnabled && entityId == damageColorEntityId && System.currentTimeMillis() < damageColorUntil;
   }

   public static void writeVanillaRedToTexture() {
      writeOverlayRow3(-65536);
   }

   public static void writeHitColorToTexture() {
      writeOverlayRow3(clampedArgb(hitColorAlpha, hitColorRed, hitColorGreen, hitColorBlue));
   }

   public static void writeTotemPopColorToTexture() {
      writeOverlayRow3(clampedArgb(totemPopColorAlpha, totemPopColorRed, totemPopColorGreen, totemPopColorBlue));
   }

   public static void writeDamageColorToTexture() {
      writeOverlayRow3(clampedArgb(damageColorAlpha, damageColorRed, damageColorGreen, damageColorBlue));
   }

   public static void onHitColorChanged() {
      updateHitColorTexture();
   }

   public static void showNotification(String message) {
      activeNotifications.add(new Notification(message, 2000L));
   }

   public static int getTotemTraceColor() {
      return clampedArgb(totemTraceColorAlpha, totemTraceColorRed, totemTraceColorGreen, totemTraceColorBlue);
   }

   private static String bigItemsParsedSource;
   private static String[] bigItemsParsed = new String[0];

   public static boolean isBigItem(ItemStack stack) {
      if (isBigItemsEnabled && stack != null && !stack.isEmpty() && bigItemsItemIds != null) {
         // Re-split only when the setting string changes (this runs per item entity per frame).
         if (bigItemsItemIds != bigItemsParsedSource) {
            bigItemsParsed = Arrays.stream(bigItemsItemIds.split("[\\s,;]+")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
            bigItemsParsedSource = bigItemsItemIds;
         }

         Identifier itemId = Registries.ITEM.getId(stack.getItem());
         String actualPath = itemId.getPath();
         String actualId = itemId.toString();
         // accept both "minecraft:golden_apple" and bare "golden_apple"
         for (String req : bigItemsParsed) {
            if (actualId.equalsIgnoreCase(req) || actualPath.equalsIgnoreCase(req)) {
               return true;
            }
         }
      }
      return false;
   }

   public static List<String> getNamedConfigNames() {
      if (!NAMED_CONFIG_DIRECTORY.exists()) {
         return List.of();
      } else {
         File[] files = NAMED_CONFIG_DIRECTORY.listFiles((dir, name) -> name.endsWith(".properties"));
         if (files == null) {
            return List.of();
         } else {
            List<String> names = new ArrayList<>();

            for (File file : files) {
               String filename = file.getName();
               names.add(filename.substring(0, filename.length() - ".properties".length()));
            }

            names.sort(String.CASE_INSENSITIVE_ORDER);
            return names;
         }
      }
   }

   public static boolean saveNamedConfig(String requestedName) {
      String name = sanitizeConfigName(requestedName);
      if (name.isEmpty()) {
         return false;
      } else {
         saveConfig();

         try {
            NAMED_CONFIG_DIRECTORY.mkdirs();
            Files.copy(CONFIG_FILE.toPath(), new File(NAMED_CONFIG_DIRECTORY, name + ".properties").toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
         } catch (IOException var3) {
            return false;
         }
      }
   }

   public static boolean loadNamedConfig(String requestedName) {
      String name = sanitizeConfigName(requestedName);
      File source = new File(NAMED_CONFIG_DIRECTORY, name + ".properties");
      if (!name.isEmpty() && source.isFile()) {
         try {
            Files.copy(source.toPath(), CONFIG_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
            loadConfig();
            syncDamageHeartsToModule();
            return true;
         } catch (IOException var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean deleteNamedConfig(String requestedName) {
      String name = sanitizeConfigName(requestedName);
      if (name.isEmpty()) {
         return false;
      } else {
         File file = new File(NAMED_CONFIG_DIRECTORY, name + ".properties");
         return file.isFile() && file.delete();
      }
   }

   private static String sanitizeConfigName(String name) {
      return name == null ? "" : name.trim().replaceAll("[^a-zA-Z0-9 _-]", "").replace(' ', '_');
   }

   public static void updateNameProtectMappings(MinecraftClient client) {
      if (client.player != null && client.world != null) {
         String sessionUsername = client.getSession().getUsername();
         String localPlayerName = client.player != null && client.player.getGameProfile() != null ? client.player.getGameProfile().name() : "";
         String targetName = nickHiderTargetName.isEmpty() ? sessionUsername : nickHiderTargetName;
         String replacementName = nickHiderReplacementName.isEmpty() ? "Hidden" : nickHiderReplacementName;
         Map<String, String> extraReplacements = new HashMap<>();
         if (sessionUsername != null && !sessionUsername.isEmpty()) {
            extraReplacements.put(sessionUsername, replacementName);
         }

         if (localPlayerName != null && !localPlayerName.isEmpty()) {
            extraReplacements.put(localPlayerName, replacementName);
         }

         if (client.player != null) {
            String entityName = client.player.getName().getString();
            if (entityName != null && !entityName.isEmpty()) {
               extraReplacements.put(entityName, replacementName);
            }

            String displayName = client.player.getDisplayName().getString();
            if (displayName != null && !displayName.isEmpty()) {
               extraReplacements.put(displayName, replacementName);
            }
         }

         NameProtectMappings.ColoringInfo coloringInfo = new NameProtectMappings.ColoringInfo(() -> -5027790, () -> -16715265, () -> -12105913);
         List<String> otherPlayers = new ArrayList<>();
         NameProtect.INSTANCE.updateMappings(targetName, replacementName, extraReplacements, otherPlayers, coloringInfo);
      }
   }

   private static void loadConfig() {
      Properties props = new Properties();
      if (CONFIG_FILE.exists()) {
         try (FileReader reader = new FileReader(CONFIG_FILE)) {
            props.load(reader);
            isInvHudEnabled = Boolean.parseBoolean(props.getProperty("invHudEnabled", "true"));
            guiBgEnabled = Boolean.parseBoolean(props.getProperty("guiBgEnabled", "false"));
            editGuiBgEnabled = Boolean.parseBoolean(props.getProperty("editGuiBgEnabled", "false"));
            clickGuiBgEnabled = Boolean.parseBoolean(props.getProperty("clickGuiBgEnabled", "true"));
            guiBgOpacity = Integer.parseInt(props.getProperty("guiBgOpacity", "128"));
            guiBackgroundImagePath = props.getProperty("guiBackgroundImagePath", "");
            guiProfile = Integer.parseInt(props.getProperty("guiProfile", "0"));
            OptimizeManager.optimizeItems = Boolean.parseBoolean(props.getProperty("optimizeItems", "false"));
            OptimizeManager.optimizeChests = Boolean.parseBoolean(props.getProperty("optimizeChests", "false"));
            OptimizeManager.optimizeSigns = Boolean.parseBoolean(props.getProperty("optimizeSigns", "false"));
            OptimizeManager.optimizePlayers = Boolean.parseBoolean(props.getProperty("optimizePlayers", "false"));
            OptimizeManager.signTextDistance = Math.max(1, Math.min(128, Integer.parseInt(props.getProperty("optimizeSignTextDistance", "48"))));
            CritEffectsManager.enabled = Boolean.parseBoolean(props.getProperty("critEffects", "false"));
            isFpsCounterEnabled = Boolean.parseBoolean(props.getProperty("fpsCounterEnabled", "false"));
            isCoordinatesEnabled = Boolean.parseBoolean(props.getProperty("coordinatesEnabled", "false"));
            isCpsEnabled = Boolean.parseBoolean(props.getProperty("cpsEnabled", "false"));
            isKeystrokesEnabled = Boolean.parseBoolean(props.getProperty("keystrokesEnabled", "false"));
            isNickHiderEnabled = Boolean.parseBoolean(props.getProperty("nickHiderEnabled", "false"));
            isPeerNickEnabled = Boolean.parseBoolean(props.getProperty("peerNickEnabled", "false"));
            isFriendGreenNameTagsEnabled = Boolean.parseBoolean(props.getProperty("friendGreenNameTagsEnabled", "false"));
            hideInvisNametags = Boolean.parseBoolean(props.getProperty("hideInvisNametags", "false"));
            safeModeEnabled = Boolean.parseBoolean(props.getProperty("safeModeEnabled", "false"));
            isNameTagItemsEnabled = Boolean.parseBoolean(props.getProperty("nameTagItemsEnabled", "false"));
            nameTagItemsDisplayMode = Integer.parseInt(props.getProperty("nameTagItemsDisplayMode", "0"));
            nameTagItemsGreyWhenUsing = Boolean.parseBoolean(props.getProperty("nameTagItemsGreyWhenUsing", "true"));
            nameTagItemsShowMainHand = Boolean.parseBoolean(props.getProperty("nameTagItemsShowMainHand", "true"));
            nameTagItemsShowOffHand = Boolean.parseBoolean(props.getProperty("nameTagItemsShowOffHand", "true"));
            nameTagItemsShowTotemPop = Boolean.parseBoolean(props.getProperty("nameTagItemsShowTotemPop", "true"));
            nameTagItemsShowArmor = Boolean.parseBoolean(props.getProperty("nameTagItemsShowArmor", "true"));
            nameTagItemsShowDurability = Boolean.parseBoolean(props.getProperty("nameTagItemsShowDurability", "true"));
            nameTagItemsShowSelf = Boolean.parseBoolean(props.getProperty("nameTagItemsShowSelf", "false"));
            nameTagItemsOnlyFriends = Boolean.parseBoolean(props.getProperty("nameTagItemsOnlyFriends", "false"));
            nameTagItemsScale = Float.parseFloat(props.getProperty("nameTagItemsScale", "1.0"));
            String friendList = props.getProperty("friends", "");
            if (!friendList.isEmpty()) {
               friends.clear();
               Collections.addAll(friends, friendList.split(","));
            }

            isCompassEnabled = Boolean.parseBoolean(props.getProperty("compassEnabled", "false"));
            if (props.containsKey("isTargetHudEnabled")) {
               isTargetHudEnabled = Boolean.parseBoolean(props.getProperty("isTargetHudEnabled"));
            }

            if (props.containsKey("isStatsHudEnabled")) {
               isStatsHudEnabled = Boolean.parseBoolean(props.getProperty("isStatsHudEnabled"));
            }

            if (props.containsKey("isFreelookEnabled")) {
               isFreelookEnabled = Boolean.parseBoolean(props.getProperty("isFreelookEnabled"));
            }

            if (props.containsKey("isZoomEnabled")) {
               isZoomEnabled = Boolean.parseBoolean(props.getProperty("isZoomEnabled"));
            }

            for (String name : props.stringPropertyNames()) {
               if (name.startsWith("keybind_")) {
                  String mod = name.substring(8);
                  if (!mod.equals("Zoom") && !mod.equals("FreeLook")) {
                     moduleKeybinds.put(mod, Integer.parseInt(props.getProperty(name)));
                  }
               }
            }

            moduleKeybinds.remove("Zoom");
            moduleKeybinds.remove("FreeLook");
            if (props.containsKey("freelookKey")) {
               freelookKey = Integer.parseInt(props.getProperty("freelookKey"));
            }

            if (props.containsKey("freelookToggleMode")) {
               freelookToggleMode = Boolean.parseBoolean(props.getProperty("freelookToggleMode"));
            }

            if (props.containsKey("freelookShowOwnNametag")) {
               freelookShowOwnNametag = Boolean.parseBoolean(props.getProperty("freelookShowOwnNametag"));
            }

            if (props.containsKey("zoomKey")) {
               zoomKey = Integer.parseInt(props.getProperty("zoomKey"));
            }

            if (props.containsKey("zoomToggleMode")) {
               zoomToggleMode = Boolean.parseBoolean(props.getProperty("zoomToggleMode"));
            }

            if (props.containsKey("zoomLevel")) {
               zoomLevel = Double.parseDouble(props.getProperty("zoomLevel"));
            }

            if (props.containsKey("zoomScrollSensitivity")) {
               zoomScrollSensitivity = Double.parseDouble(props.getProperty("zoomScrollSensitivity"));
            }

            guiAnimationDurationMs = Integer.parseInt(props.getProperty("guiAnimationDurationMs", "300"));
            clickGuiScale = Float.parseFloat(props.getProperty("clickGuiScale", "1.0"));
            guiType = Integer.parseInt(props.getProperty("guiType", "0"));
            isArmorStatusEnabled = Boolean.parseBoolean(props.getProperty("isArmorStatusEnabled", "false"));
            allTimeKills = Integer.parseInt(props.getProperty("allTimeKills", "0"));
            allTimeDeaths = Integer.parseInt(props.getProperty("allTimeDeaths", "0"));
            allTimeCrystalDeaths = Integer.parseInt(props.getProperty("allTimeCrystalDeaths", "0"));
            allTimePops = Integer.parseInt(props.getProperty("allTimePops", "0"));
            allTimeCrystalKills = Integer.parseInt(props.getProperty("allTimeCrystalKills", "0"));
            allTimeAnchorKills = Integer.parseInt(props.getProperty("allTimeAnchorKills", "0"));
            allTimeClicks = Integer.parseInt(props.getProperty("allTimeClicks", "0"));
            allTimeHits = Integer.parseInt(props.getProperty("allTimeHits", "0"));
            allTimeDamage = Double.parseDouble(props.getProperty("allTimeDamage", "0.0"));
            allTimeBlocksBroken = Integer.parseInt(props.getProperty("allTimeBlocksBroken", "0"));
            allTimeBlocksPlaced = Integer.parseInt(props.getProperty("allTimeBlocksPlaced", "0"));
            allTimeCrystalsPlaced = Integer.parseInt(props.getProperty("allTimeCrystalsPlaced", "0"));
            allTimeCrystalsBroken = Integer.parseInt(props.getProperty("allTimeCrystalsBroken", "0"));
            allTimeAnchorsPlaced = Integer.parseInt(props.getProperty("allTimeAnchorsPlaced", "0"));
            allTimeAnchorsBlown = Integer.parseInt(props.getProperty("allTimeAnchorsBlown", "0"));
            allTimeAnchorsCharged = Integer.parseInt(props.getProperty("allTimeAnchorsCharged", "0"));
            allTimePlayTimeTicks = Long.parseLong(props.getProperty("allTimePlayTimeTicks", "0"));
            sessionBlocksBroken = Integer.parseInt(props.getProperty("sessionBlocksBroken", "0"));
            sessionBlocksPlaced = Integer.parseInt(props.getProperty("sessionBlocksPlaced", "0"));
            sessionCrystalsPlaced = Integer.parseInt(props.getProperty("sessionCrystalsPlaced", "0"));
            sessionCrystalsBroken = Integer.parseInt(props.getProperty("sessionCrystalsBroken", "0"));
            sessionAnchorsPlaced = Integer.parseInt(props.getProperty("sessionAnchorsPlaced", "0"));
            sessionAnchorsBlown = Integer.parseInt(props.getProperty("sessionAnchorsBlown", "0"));
            sessionAnchorsCharged = Integer.parseInt(props.getProperty("sessionAnchorsCharged", "0"));
            sessionPlayTimeTicks = Long.parseLong(props.getProperty("sessionPlayTimeTicks", "0"));
            disableHotbarLooping = Boolean.parseBoolean(props.getProperty("disableHotbarLooping", "false"));
            isCustomHitboxEnabled = Boolean.parseBoolean(props.getProperty("isCustomHitboxEnabled", "false"));
            isFullbrightEnabled = Boolean.parseBoolean(props.getProperty("isFullbrightEnabled", "false"));
            isBigItemsEnabled = Boolean.parseBoolean(props.getProperty("isBigItemsEnabled", "false"));
            isItemPhysicsEnabled = Boolean.parseBoolean(props.getProperty("isItemPhysicsEnabled", "false"));
            itemPhysicsRotationX = Float.parseFloat(props.getProperty("itemPhysicsRotationX", "90.0"));
            itemPhysicsOffsetY = Float.parseFloat(props.getProperty("itemPhysicsOffsetY", "-0.1"));
            itemPhysicsOffsetZ = Float.parseFloat(props.getProperty("itemPhysicsOffsetZ", "-0.2"));
            itemPhysicsFallFlips = Boolean.parseBoolean(props.getProperty("itemPhysicsFallFlips", "true"));
            itemPhysicsFlipsPerBlock = Integer.parseInt(props.getProperty("itemPhysicsFlipsPerBlock", "2"));
            isPlayerModelEnabled = Boolean.parseBoolean(props.getProperty("isPlayerModelEnabled", "false"));
            bigItemsItemIds = props.getProperty("bigItemsItemIds", "minecraft:golden_apple");
            bigItemsScale = Float.parseFloat(props.getProperty("bigItemsScale", "10.0"));
            isLowHealthAlarmEnabled = Boolean.parseBoolean(props.getProperty("isLowHealthAlarmEnabled", "false"));
            lowHealthAlarmSound = Boolean.parseBoolean(props.getProperty("lowHealthAlarmSound", "true"));
            lowHealthAlarmText = Boolean.parseBoolean(props.getProperty("lowHealthAlarmText", "true"));
            lowHealthAlarmThreshold = Integer.parseInt(props.getProperty("lowHealthAlarmThreshold", "10"));
            lowHealthTotemThreshold = Integer.parseInt(props.getProperty("lowHealthTotemThreshold", "1"));
            lowHealthAlarmX = Integer.parseInt(props.getProperty("lowHealthAlarmX", "10"));
            lowHealthAlarmY = Integer.parseInt(props.getProperty("lowHealthAlarmY", "10"));
            lowHealthAlarmScale = Float.parseFloat(props.getProperty("lowHealthAlarmScale", "1.0"));
            lowHealthUseThemeColor = Boolean.parseBoolean(props.getProperty("lowHealthUseThemeColor", "false"));
            isPotWarningEnabled = Boolean.parseBoolean(props.getProperty("isPotWarningEnabled", "false"));
            potWarnLowPots = Boolean.parseBoolean(props.getProperty("potWarnLowPots", "true"));
            potWarnPotThreshold = Integer.parseInt(props.getProperty("potWarnPotThreshold", "3"));
            potWarnLowEffects = Boolean.parseBoolean(props.getProperty("potWarnLowEffects", "true"));
            potWarnEffectThreshold = Integer.parseInt(props.getProperty("potWarnEffectThreshold", "400"));
            potWarnSound = Boolean.parseBoolean(props.getProperty("potWarnSound", "true"));
            potWarnShowText = Boolean.parseBoolean(props.getProperty("potWarnShowText", "true"));
            potWarnUseThemeColor = Boolean.parseBoolean(props.getProperty("potWarnUseThemeColor", "false"));
            potWarningX = Integer.parseInt(props.getProperty("potWarningX", "10"));
            potWarningY = Integer.parseInt(props.getProperty("potWarningY", "30"));
            potWarningScale = Float.parseFloat(props.getProperty("potWarningScale", "1.0"));
            isDropPreventionEnabled = Boolean.parseBoolean(props.getProperty("isDropPreventionEnabled", "false"));
            dropPreventionItems = props.getProperty("dropPreventionItems", "minecraft:totem_of_undying");
            isHitColorEnabled = Boolean.parseBoolean(props.getProperty("isHitColorEnabled", "false"));
            hitColorApplyToSelf = Boolean.parseBoolean(props.getProperty("hitColorApplyToSelf", "true"));
            hitColorApplyToArmor = Boolean.parseBoolean(props.getProperty("hitColorApplyToArmor", "true"));
            hitColorApplyToArmorStands = Boolean.parseBoolean(props.getProperty("hitColorApplyToArmorStands", "false"));
            hitColorRed = Integer.parseInt(props.getProperty("hitColorRed", "255"));
            hitColorGreen = Integer.parseInt(props.getProperty("hitColorGreen", "0"));
            hitColorBlue = Integer.parseInt(props.getProperty("hitColorBlue", "0"));
            hitColorAlpha = Integer.parseInt(props.getProperty("hitColorAlpha", "153"));
            hitColorDuration = Integer.parseInt(props.getProperty("hitColorDuration", "10"));
            customHitboxRange = Integer.parseInt(props.getProperty("customHitboxRange", "20"));
            isTotemPopColorEnabled = Boolean.parseBoolean(props.getProperty("isTotemPopColorEnabled", "false"));
            totemPopColorDetectSelf = Boolean.parseBoolean(props.getProperty("totemPopColorDetectSelf", "true"));
            totemPopColorDetectOthers = Boolean.parseBoolean(props.getProperty("totemPopColorDetectOthers", "true"));
            totemPopColorApplyToArmor = Boolean.parseBoolean(props.getProperty("totemPopColorApplyToArmor", "true"));
            totemPopColorApplyToArmorStands = Boolean.parseBoolean(props.getProperty("totemPopColorApplyToArmorStands", "false"));
            totemPopColorRed = Integer.parseInt(props.getProperty("totemPopColorRed", "255"));
            totemPopColorGreen = Integer.parseInt(props.getProperty("totemPopColorGreen", "215"));
            totemPopColorBlue = Integer.parseInt(props.getProperty("totemPopColorBlue", "0"));
            totemPopColorAlpha = Integer.parseInt(props.getProperty("totemPopColorAlpha", "153"));
            totemPopColorDuration = Integer.parseInt(props.getProperty("totemPopColorDuration", "20"));
            isDamageColorEnabled = Boolean.parseBoolean(props.getProperty("isDamageColorEnabled", "false"));
            damageColorApplyToSelf = Boolean.parseBoolean(props.getProperty("damageColorApplyToSelf", "true"));
            damageColorApplyToPlayers = Boolean.parseBoolean(props.getProperty("damageColorApplyToPlayers", "false"));
            damageColorApplyToEntities = Boolean.parseBoolean(props.getProperty("damageColorApplyToEntities", "false"));
            damageColorApplyToArmor = Boolean.parseBoolean(props.getProperty("damageColorApplyToArmor", "true"));
            damageColorApplyToArmorStands = Boolean.parseBoolean(props.getProperty("damageColorApplyToArmorStands", "false"));
            damageColorRed = Integer.parseInt(props.getProperty("damageColorRed", "255"));
            damageColorGreen = Integer.parseInt(props.getProperty("damageColorGreen", "0"));
            damageColorBlue = Integer.parseInt(props.getProperty("damageColorBlue", "0"));
            damageColorAlpha = Integer.parseInt(props.getProperty("damageColorAlpha", "100"));
            damageColorDuration = Integer.parseInt(props.getProperty("damageColorDuration", "10"));
            isCustomHitboxDynamic = Boolean.parseBoolean(props.getProperty("isCustomHitboxDynamic", "false"));
            hitboxRed = Integer.parseInt(props.getProperty("hitboxRed", "255"));
            hitboxGreen = Integer.parseInt(props.getProperty("hitboxGreen", "0"));
            hitboxBlue = Integer.parseInt(props.getProperty("hitboxBlue", "0"));
            hitboxThickness = Float.parseFloat(props.getProperty("hitboxThickness", "2.0"));
            hitboxShowCrystal = Boolean.parseBoolean(props.getProperty("hitboxShowCrystal", "true"));
            crystalHitboxRed = Integer.parseInt(props.getProperty("crystalHitboxRed", "0"));
            crystalHitboxGreen = Integer.parseInt(props.getProperty("crystalHitboxGreen", "200"));
            crystalHitboxBlue = Integer.parseInt(props.getProperty("crystalHitboxBlue", "255"));
            isDamageHeartsEnabled = Boolean.parseBoolean(props.getProperty("isDamageHeartsEnabled", "false"));
            isHealthIndicatorsEnabled = Boolean.parseBoolean(props.getProperty("isHealthIndicatorsEnabled", "false"));
            healthIndicatorsMode = Integer.parseInt(props.getProperty("healthIndicatorsMode", "0"));
            healthIndicatorsType = Integer.parseInt(props.getProperty("healthIndicatorsType", "0"));
            healthIndicatorsFuseAbsorption = Boolean.parseBoolean(props.getProperty("healthIndicatorsFuseAbsorption", "true"));
            healthIndicatorsRange = Integer.parseInt(props.getProperty("healthIndicatorsRange", "20"));
            targetIndicatorMaxReach = Float.parseFloat(props.getProperty("targetIndicatorMaxReach", "64.0"));
            targetIndicatorSpinning = Boolean.parseBoolean(props.getProperty("targetIndicatorSpinning", "true"));
            isHurtcamEnabled = Boolean.parseBoolean(props.getProperty("isHurtcamEnabled", "true"));
            isPortalEnabled = Boolean.parseBoolean(props.getProperty("isPortalEnabled", "true"));
            isPumpkinEnabled = Boolean.parseBoolean(props.getProperty("isPumpkinEnabled", "true"));
            isWaterFogEnabled = Boolean.parseBoolean(props.getProperty("isWaterFogEnabled", "true"));
            isFireEnabled = Boolean.parseBoolean(props.getProperty("isFireEnabled", "true"));
            isViewModelEnabled = Boolean.parseBoolean(props.getProperty("isViewModelEnabled", "false"));
            isMainHandViewModelEnabled = Boolean.parseBoolean(props.getProperty("isMainHandViewModelEnabled", "false"));
            isOffHandViewModelEnabled = Boolean.parseBoolean(props.getProperty("isOffHandViewModelEnabled", "false"));
            mainHandGlobal = VoidCyanClient.ViewModelSettings.deserialize(props.getProperty("mainHandGlobal", "true,0,0,0,0,0,0,1"));
            offHandGlobal = VoidCyanClient.ViewModelSettings.deserialize(props.getProperty("offHandGlobal", "true,0,0,0,0,0,0,1"));
            mainHandItemOverrides.clear();

            for (String key : props.stringPropertyNames()) {
               if (key.startsWith("vmMain_")) {
                  String itemId = key.substring(7);
                  mainHandItemOverrides.put(itemId, VoidCyanClient.ViewModelSettings.deserialize(props.getProperty(key)));
               } else if (key.startsWith("vmOff_")) {
                  String itemId = key.substring(6);
                  offHandItemOverrides.put(itemId, VoidCyanClient.ViewModelSettings.deserialize(props.getProperty(key)));
               }
            }

            isItemAnimationsEnabled = Boolean.parseBoolean(props.getProperty("isItemAnimationsEnabled", "false"));
            itemAnimationMode = Integer.parseInt(props.getProperty("itemAnimationMode", "0"));
            customSwingPitch = Float.parseFloat(props.getProperty("customSwingPitch", "-20.0"));
            customSwingYaw = Float.parseFloat(props.getProperty("customSwingYaw", "-40.0"));
            customSwingRoll = Float.parseFloat(props.getProperty("customSwingRoll", "20.0"));
            isCustomFovEnabled = Boolean.parseBoolean(props.getProperty("isCustomFovEnabled", "false"));
            customFov = Integer.parseInt(props.getProperty("customFov", "110"));
            isComboCounterEnabled = Boolean.parseBoolean(props.getProperty("isComboCounterEnabled", "false"));
            comboCounterX = Integer.parseInt(props.getProperty("comboCounterX", "10"));
            comboCounterY = Integer.parseInt(props.getProperty("comboCounterY", "250"));
            comboCounterScale = Float.parseFloat(props.getProperty("comboCounterScale", "1.0"));
            comboResetKeybind = Integer.parseInt(props.getProperty("comboResetKeybind", "0"));
            comboTimeout = Integer.parseInt(props.getProperty("comboTimeout", "2000"));
            comboVersion = Integer.parseInt(props.getProperty("comboVersion", "0"));
            isPingDisplayEnabled = Boolean.parseBoolean(props.getProperty("isPingDisplayEnabled", "false"));
            pingDisplayX = Integer.parseInt(props.getProperty("pingDisplayX", "10"));
            pingDisplayY = Integer.parseInt(props.getProperty("pingDisplayY", "280"));
            pingDisplayScale = Float.parseFloat(props.getProperty("pingDisplayScale", "1.0"));
            isHurtcamEnabled = Boolean.parseBoolean(props.getProperty("isHurtcamEnabled", "true"));
            isPumpkinEnabled = Boolean.parseBoolean(props.getProperty("isPumpkinEnabled", "true"));
            isWaterFogEnabled = Boolean.parseBoolean(props.getProperty("isWaterFogEnabled", "true"));
            isPortalEnabled = Boolean.parseBoolean(props.getProperty("isPortalEnabled", "true"));
            isFireEnabled = Boolean.parseBoolean(props.getProperty("isFireEnabled", "true"));
            isParticlesEnabled = Boolean.parseBoolean(props.getProperty("isParticlesEnabled", "true"));
            isExplosionParticlesEnabled = Boolean.parseBoolean(props.getProperty("isExplosionParticlesEnabled", "true"));
            isPotionParticlesEnabled = Boolean.parseBoolean(props.getProperty("isPotionParticlesEnabled", "true"));
            isCriticalParticlesEnabled = Boolean.parseBoolean(props.getProperty("isCriticalParticlesEnabled", "true"));
            isDamageParticlesEnabled = Boolean.parseBoolean(props.getProperty("isDamageParticlesEnabled", "true"));
            isChatHeadsEnabled = Boolean.parseBoolean(props.getProperty("isChatHeadsEnabled", "false"));
            isNotificationsEnabled = Boolean.parseBoolean(props.getProperty("isNotificationsEnabled", "true"));
            isAppleSkinEnabled = Boolean.parseBoolean(props.getProperty("isAppleSkinEnabled", "false"));
            isNotificationsAnimEnabled = Boolean.parseBoolean(props.getProperty("isNotificationsAnimEnabled", "true"));

            try {
               notificationsAnimDirection = Integer.parseInt(props.getProperty("notificationsAnimDirection", "0"));
            } catch (Exception var9) {
            }

            notificationsX = Integer.parseInt(props.getProperty("notificationsX", "10"));
            notificationsY = Integer.parseInt(props.getProperty("notificationsY", "100"));
            notificationsScale = Float.parseFloat(props.getProperty("notificationsScale", "1.0"));
            isSpeedDisplayEnabled = Boolean.parseBoolean(props.getProperty("isSpeedDisplayEnabled", "false"));
            speedDisplayX = Integer.parseInt(props.getProperty("speedDisplayX", "10"));
            speedDisplayY = Integer.parseInt(props.getProperty("speedDisplayY", "10"));
            speedDisplayScale = Float.parseFloat(props.getProperty("speedDisplayScale", "1.0"));
            isBiomeDisplayEnabled = Boolean.parseBoolean(props.getProperty("isBiomeDisplayEnabled", "false"));
            biomeDisplayX = Integer.parseInt(props.getProperty("biomeDisplayX", "10"));
            biomeDisplayY = Integer.parseInt(props.getProperty("biomeDisplayY", "10"));
            biomeDisplayScale = Float.parseFloat(props.getProperty("biomeDisplayScale", "1.0"));
            isEntityCounterEnabled = Boolean.parseBoolean(props.getProperty("isEntityCounterEnabled", "false"));
            entityCounterX = Integer.parseInt(props.getProperty("entityCounterX", "10"));
            entityCounterY = Integer.parseInt(props.getProperty("entityCounterY", "400"));
            entityCounterScale = Float.parseFloat(props.getProperty("entityCounterScale", "1.0"));
            entityCounterCurrentChunkOnly = Boolean.parseBoolean(props.getProperty("entityCounterCurrentChunkOnly", "false"));
            entityCounterShowPlayers = Boolean.parseBoolean(props.getProperty("entityCounterShowPlayers", "true"));
            entityCounterShowHostile = Boolean.parseBoolean(props.getProperty("entityCounterShowHostile", "true"));
            entityCounterShowAnimals = Boolean.parseBoolean(props.getProperty("entityCounterShowAnimals", "true"));
            entityCounterShowItems = Boolean.parseBoolean(props.getProperty("entityCounterShowItems", "true"));
            entityCounterShowTotal = Boolean.parseBoolean(props.getProperty("entityCounterShowTotal", "true"));
            isIrlClockEnabled = Boolean.parseBoolean(props.getProperty("isIrlClockEnabled", "false"));
            isIrlDateEnabled = Boolean.parseBoolean(props.getProperty("isIrlDateEnabled", "false"));
            isIrlClock24Hour = Boolean.parseBoolean(props.getProperty("isIrlClock24Hour", "false"));
            irlClockX = Integer.parseInt(props.getProperty("irlClockX", "10"));
            irlClockY = Integer.parseInt(props.getProperty("irlClockY", "10"));
            irlClockScale = Float.parseFloat(props.getProperty("irlClockScale", "1.0"));
            isTotemCounterEnabled = Boolean.parseBoolean(props.getProperty("isTotemCounterEnabled", "false"));
            totemCounterX = Integer.parseInt(props.getProperty("totemCounterX", "10"));
            totemCounterY = Integer.parseInt(props.getProperty("totemCounterY", "10"));
            totemCounterScale = Float.parseFloat(props.getProperty("totemCounterScale", "1.0"));
            isArrayListEnabled = Boolean.parseBoolean(props.getProperty("isArrayListEnabled", "false"));
            arrayListSortMode = Integer.parseInt(props.getProperty("arrayListSortMode", "0"));
            arrayListX = Integer.parseInt(props.getProperty("arrayListX", "320"));
            arrayListY = Integer.parseInt(props.getProperty("arrayListY", "50"));
            arrayListScale = Float.parseFloat(props.getProperty("arrayListScale", "1.0"));
            arrayListBackground = Boolean.parseBoolean(props.getProperty("arrayListBackground", "true"));
            isArrowCounterEnabled = Boolean.parseBoolean(props.getProperty("isArrowCounterEnabled", "false"));
            arrowCounterX = Integer.parseInt(props.getProperty("arrowCounterX", "10"));
            arrowCounterY = Integer.parseInt(props.getProperty("arrowCounterY", "10"));
            arrowCounterScale = Float.parseFloat(props.getProperty("arrowCounterScale", "1.0"));
            isReachDisplayEnabled = Boolean.parseBoolean(props.getProperty("isReachDisplayEnabled", "false"));
            reachDisplayX = Integer.parseInt(props.getProperty("reachDisplayX", "10"));
            reachDisplayY = Integer.parseInt(props.getProperty("reachDisplayY", "10"));
            reachDisplayScale = Float.parseFloat(props.getProperty("reachDisplayScale", "1.0"));
            isServerInfoEnabled = Boolean.parseBoolean(props.getProperty("isServerInfoEnabled", "false"));
            serverInfoX = Integer.parseInt(props.getProperty("serverInfoX", "10"));
            serverInfoY = Integer.parseInt(props.getProperty("serverInfoY", "300"));
            serverInfoScale = Float.parseFloat(props.getProperty("serverInfoScale", "1.0"));
            isPackDisplayEnabled = Boolean.parseBoolean(props.getProperty("isPackDisplayEnabled", "false"));
            packDisplayX = Integer.parseInt(props.getProperty("packDisplayX", "10"));
            packDisplayY = Integer.parseInt(props.getProperty("packDisplayY", "310"));
            packDisplayScale = Float.parseFloat(props.getProperty("packDisplayScale", "1.0"));
            isBlockIndicatorEnabled = Boolean.parseBoolean(props.getProperty("isBlockIndicatorEnabled", "false"));
            blockIndicatorX = Integer.parseInt(props.getProperty("blockIndicatorX", "10"));
            blockIndicatorY = Integer.parseInt(props.getProperty("blockIndicatorY", "340"));
            blockIndicatorScale = Float.parseFloat(props.getProperty("blockIndicatorScale", "1.0"));
            BlockIndicatorManager.load(props);
            isCustomCrosshairEnabled = Boolean.parseBoolean(props.getProperty("isCustomCrosshairEnabled", "false"));
            crosshairStyle = Integer.parseInt(props.getProperty("crosshairStyle", "0"));
            crosshairRed = Integer.parseInt(props.getProperty("crosshairRed", "255"));
            crosshairGreen = Integer.parseInt(props.getProperty("crosshairGreen", "255"));
            crosshairBlue = Integer.parseInt(props.getProperty("crosshairBlue", "255"));
            crosshairAlpha = Integer.parseInt(props.getProperty("crosshairAlpha", "255"));
            crosshairSize = Integer.parseInt(props.getProperty("crosshairSize", "5"));
            crosshairThickness = Integer.parseInt(props.getProperty("crosshairThickness", "1"));
            crosshairDot = Boolean.parseBoolean(props.getProperty("crosshairDot", "false"));
            isCustomF3Enabled = Boolean.parseBoolean(props.getProperty("isCustomF3Enabled", "false"));
            isDeathInfoEnabled = Boolean.parseBoolean(props.getProperty("isDeathInfoEnabled", "false"));
            deathInfoShowOnScreen = Boolean.parseBoolean(props.getProperty("deathInfoShowOnScreen", "true"));
            deathInfoX = Integer.parseInt(props.getProperty("deathInfoX", "10"));
            deathInfoY = Integer.parseInt(props.getProperty("deathInfoY", "370"));
            deathInfoScale = Float.parseFloat(props.getProperty("deathInfoScale", "1.0"));
            lastDeathX = Integer.parseInt(props.getProperty("lastDeathX", "0"));
            lastDeathY = Integer.parseInt(props.getProperty("lastDeathY", "0"));
            lastDeathZ = Integer.parseInt(props.getProperty("lastDeathZ", "0"));
            lastDeathDimension = props.getProperty("lastDeathDimension", "");
            lastDeathTime = Long.parseLong(props.getProperty("lastDeathTime", "0"));
            hasDeathInfo = Boolean.parseBoolean(props.getProperty("hasDeathInfo", "false"));
            isWaypointsEnabled = Boolean.parseBoolean(props.getProperty("isWaypointsEnabled", "false"));
            isTpsDisplayEnabled = Boolean.parseBoolean(props.getProperty("isTpsDisplayEnabled", "false"));
            tpsDisplayX = Integer.parseInt(props.getProperty("tpsDisplayX", "10"));
            tpsDisplayY = Integer.parseInt(props.getProperty("tpsDisplayY", "400"));
            tpsDisplayScale = Float.parseFloat(props.getProperty("tpsDisplayScale", "1.0"));
            isSystemResourcesEnabled = Boolean.parseBoolean(props.getProperty("isSystemResourcesEnabled", "false"));
            systemResourcesX = Integer.parseInt(props.getProperty("systemResourcesX", "10"));
            systemResourcesY = Integer.parseInt(props.getProperty("systemResourcesY", "50"));
            systemResourcesScale = Float.parseFloat(props.getProperty("systemResourcesScale", "1.0"));
            isStopwatchEnabled = Boolean.parseBoolean(props.getProperty("isStopwatchEnabled", "false"));
            stopwatchRunning = Boolean.parseBoolean(props.getProperty("stopwatchRunning", "true"));
            stopwatchKey = Integer.parseInt(props.getProperty("stopwatchKey", "0"));
            stopwatchX = Integer.parseInt(props.getProperty("stopwatchX", "10"));
            stopwatchY = Integer.parseInt(props.getProperty("stopwatchY", "50"));
            stopwatchScale = Float.parseFloat(props.getProperty("stopwatchScale", "1.0"));
            stopwatchBackground = Boolean.parseBoolean(props.getProperty("stopwatchBackground", "true"));
            isTextHudEnabled = Boolean.parseBoolean(props.getProperty("isTextHudEnabled", "false"));
            textHudString = props.getProperty("textHudString", "Custom Text");
            textHudTextColor = Integer.parseInt(props.getProperty("textHudTextColor", String.valueOf(16777215)));
            textHudBgColor = Integer.parseInt(props.getProperty("textHudBgColor", "0"));
            textHudBgAlpha = Integer.parseInt(props.getProperty("textHudBgAlpha", "128"));
            textHudX = Integer.parseInt(props.getProperty("textHudX", "10"));
            textHudY = Integer.parseInt(props.getProperty("textHudY", "10"));
            textHudScale = Float.parseFloat(props.getProperty("textHudScale", "1.0"));
            isWatermarkEnabled = Boolean.parseBoolean(props.getProperty("isWatermarkEnabled", "true"));
            watermarkX = Integer.parseInt(props.getProperty("watermarkX", "10"));
            watermarkY = Integer.parseInt(props.getProperty("watermarkY", "10"));
            watermarkScale = Float.parseFloat(props.getProperty("watermarkScale", "1.0"));
            watermarkAnimatedColor = Boolean.parseBoolean(props.getProperty("watermarkAnimatedColor", "true"));
            watermarkUseThemeColor = Boolean.parseBoolean(props.getProperty("watermarkUseThemeColor", "true"));
            watermarkCustomColor = Integer.parseInt(props.getProperty("watermarkCustomColor", "16777215"));
            isMapTransformationEnabled = Boolean.parseBoolean(props.getProperty("isMapTransformationEnabled", "false"));
            isMapHoverPreviewEnabled = Boolean.parseBoolean(props.getProperty("isMapHoverPreviewEnabled", "true"));
            isKeybindsDisplayEnabled = Boolean.parseBoolean(props.getProperty("isKeybindsDisplayEnabled", "false"));
            keybindsHudX = Integer.parseInt(props.getProperty("keybindsHudX", "10"));
            keybindsHudY = Integer.parseInt(props.getProperty("keybindsHudY", "100"));
            keybindsHudScale = Float.parseFloat(props.getProperty("keybindsHudScale", "1.0"));
            isFpsGraphEnabled = Boolean.parseBoolean(props.getProperty("isFpsGraphEnabled", "false"));
            fpsGraphX = Integer.parseInt(props.getProperty("fpsGraphX", "320"));
            fpsGraphY = Integer.parseInt(props.getProperty("fpsGraphY", "50"));
            fpsGraphScale = Float.parseFloat(props.getProperty("fpsGraphScale", "1.0"));
            showFpsAvg = Boolean.parseBoolean(props.getProperty("showFpsAvg", "true"));
            showFpsMinMax = Boolean.parseBoolean(props.getProperty("showFpsMinMax", "true"));
            showFps1Percent = Boolean.parseBoolean(props.getProperty("showFps1Percent", "true"));
            showFps01Percent = Boolean.parseBoolean(props.getProperty("showFps01Percent", "true"));
            fpsGraphBackground = Boolean.parseBoolean(props.getProperty("fpsGraphBackground", "true"));
            isTotemTraceEnabled = Boolean.parseBoolean(props.getProperty("isTotemTraceEnabled", "false"));
            totemTraceDuration = Integer.parseInt(props.getProperty("totemTraceDuration", "10"));
            totemTraceColorRed = Integer.parseInt(props.getProperty("totemTraceColorRed", "0"));
            totemTraceColorGreen = Integer.parseInt(props.getProperty("totemTraceColorGreen", "255"));
            totemTraceColorBlue = Integer.parseInt(props.getProperty("totemTraceColorBlue", "0"));
            totemTraceColorAlpha = Integer.parseInt(props.getProperty("totemTraceColorAlpha", "255"));
            totemTraceShowColor = Boolean.parseBoolean(props.getProperty("totemTraceShowColor", "true"));
            totemTraceShowArmor = Boolean.parseBoolean(props.getProperty("totemTraceShowArmor", "true"));
            totemTraceDetectSelf = Boolean.parseBoolean(props.getProperty("totemTraceDetectSelf", "true"));
            totemTraceDetectOthers = Boolean.parseBoolean(props.getProperty("totemTraceDetectOthers", "true"));
            totemPopHealthThreshold = Integer.parseInt(props.getProperty("totemPopHealthThreshold", "6"));
            isPlayerTrailEnabled = Boolean.parseBoolean(props.getProperty("isPlayerTrailEnabled", "false"));
            playerTrailShowSelf = Boolean.parseBoolean(props.getProperty("playerTrailShowSelf", "true"));
            playerTrailShowOthers = Boolean.parseBoolean(props.getProperty("playerTrailShowOthers", "true"));
            playerTrailColor = Integer.parseInt(props.getProperty("playerTrailColor", "65280"));
            playerTrailColorR = Integer.parseInt(props.getProperty("playerTrailColorR", "0"));
            playerTrailColorG = Integer.parseInt(props.getProperty("playerTrailColorG", "255"));
            playerTrailColorB = Integer.parseInt(props.getProperty("playerTrailColorB", "0"));
            playerTrailDuration = Float.parseFloat(props.getProperty("playerTrailDuration", "5.0"));
            playerTrailAnchor = Math.max(0, Math.min(2, Integer.parseInt(props.getProperty("playerTrailAnchor", "1"))));
            StopwatchManager.load(props);
            isTntTimerEnabled = Boolean.parseBoolean(props.getProperty("isTntTimerEnabled", "false"));
            TntTimerRenderer.maxDistance = Integer.parseInt(props.getProperty("tntTimerMaxDistance", "64"));
            isTimeChangerEnabled = Boolean.parseBoolean(props.getProperty("isTimeChangerEnabled", "false"));
            customTime = Long.parseLong(props.getProperty("customTime", "6000"));
            targetIndicatorSpinSpeed = Float.parseFloat(props.getProperty("targetIndicatorSpinSpeed", "1.0"));
            targetIndicatorHitReachOnly = Boolean.parseBoolean(props.getProperty("targetIndicatorHitReachOnly", "false"));
            attackIndicatorEnabled = Boolean.parseBoolean(props.getProperty("attackIndicatorEnabled", "false"));
            attackIndicatorOnlyPlayers = Boolean.parseBoolean(props.getProperty("attackIndicatorOnlyPlayers", "true"));
            attackIndicatorAllowNonLiving = Boolean.parseBoolean(props.getProperty("attackIndicatorAllowNonLiving", "false"));
            antiBot = Boolean.parseBoolean(props.getProperty("antiBot", "true"));
            attackIndicatorAlwaysActive = Boolean.parseBoolean(props.getProperty("attackIndicatorAlwaysActive", "true"));
            attackIndicatorStyle = props.getProperty("attackIndicatorStyle", "LEGACY");
            attackIndicatorLiveTime = Float.parseFloat(props.getProperty("attackIndicatorLiveTime", "3.0"));
            isAttackHudEnabled = Boolean.parseBoolean(props.getProperty("isAttackHudEnabled", "false"));
            attackHudMode = Integer.parseInt(props.getProperty("attackHudMode", "0"));
            attackHudSize = Integer.parseInt(props.getProperty("attackHudSize", "32"));
            attackHudX = Integer.parseInt(props.getProperty("attackHudX", "10"));
            attackHudY = Integer.parseInt(props.getProperty("attackHudY", "10"));
            attackIndicatorLegacyTexture = props.getProperty("attackIndicatorLegacyTexture", "LEGACY");
            attackIndicatorLegacyRollSpeed = Integer.parseInt(props.getProperty("attackIndicatorLegacyRollSpeed", "70"));
            attackIndicatorLegacyScale = Integer.parseInt(props.getProperty("attackIndicatorLegacyScale", "60"));
            attackIndicatorLegacyAlpha = Integer.parseInt(props.getProperty("attackIndicatorLegacyAlpha", "80"));
            attackIndicatorSoulStyle = props.getProperty("attackIndicatorSoulStyle", "SMOKE");
            attackIndicatorSoulTexture = props.getProperty("attackIndicatorSoulTexture", "ALT");
            attackIndicatorSoulLength = Integer.parseInt(props.getProperty("attackIndicatorSoulLength", "5"));
            attackIndicatorSoulFactor = Float.parseFloat(props.getProperty("attackIndicatorSoulFactor", "3.0"));
            attackIndicatorSoulShaking = Float.parseFloat(props.getProperty("attackIndicatorSoulShaking", "3.0"));
            attackIndicatorSoulAmplitude = Float.parseFloat(props.getProperty("attackIndicatorSoulAmplitude", "3.0"));
            attackIndicatorSoulRadius = Float.parseFloat(props.getProperty("attackIndicatorSoulRadius", "100.0"));
            attackIndicatorSoulStartSize = Float.parseFloat(props.getProperty("attackIndicatorSoulStartSize", "50.0"));
            attackIndicatorSoulEndSize = Float.parseFloat(props.getProperty("attackIndicatorSoulEndSize", "20.0"));
            attackIndicatorSoulScale = Float.parseFloat(props.getProperty("attackIndicatorSoulScale", "40.0"));
            attackIndicatorSoulSubdivision = Integer.parseInt(props.getProperty("attackIndicatorSoulSubdivision", "3"));
            attackIndicatorTopkaRadius = Float.parseFloat(props.getProperty("attackIndicatorTopkaRadius", "50.0"));
            attackIndicatorTopkaSpeed = Float.parseFloat(props.getProperty("attackIndicatorTopkaSpeed", "30.0"));
            DamageHeartsModule.showPlayers = Boolean.parseBoolean(props.getProperty("damageHeartsShowPlayers", "true"));
            DamageHeartsModule.showHostile = Boolean.parseBoolean(props.getProperty("damageHeartsShowHostile", "true"));
            DamageHeartsModule.showPassive = Boolean.parseBoolean(props.getProperty("damageHeartsShowPassive", "true"));
            DamageHeartsModule.showAnimals = Boolean.parseBoolean(props.getProperty("damageHeartsShowAnimals", "true"));
            DamageHeartsModule.showBosses = Boolean.parseBoolean(props.getProperty("damageHeartsShowBosses", "true"));
            DamageHeartsModule.heartScale = Float.parseFloat(props.getProperty("damageHeartsHeartScale", "1.0"));
            DamageHeartsModule.fadeTime = Float.parseFloat(props.getProperty("damageHeartsFadeTime", "2.0"));
            DamageHeartsModule.lifetime = Float.parseFloat(props.getProperty("damageHeartsLifetime", "2.5"));
            DamageHeartsModule.glowEnabled = Boolean.parseBoolean(props.getProperty("damageHeartsGlowEnabled", "true"));
            DamageHeartsModule.critGlowEnabled = Boolean.parseBoolean(props.getProperty("damageHeartsCritGlowEnabled", "true"));
            DamageHeartsModule.rainbowMode = Boolean.parseBoolean(props.getProperty("damageHeartsRainbowMode", "false"));
            DamageHeartsModule.showNumeric = Boolean.parseBoolean(props.getProperty("damageHeartsShowNumeric", "false"));
            DamageHeartsModule.distanceScaling = Boolean.parseBoolean(props.getProperty("damageHeartsDistanceScaling", "true"));
            DamageHeartsModule.rotationSpeed = Float.parseFloat(props.getProperty("damageHeartsRotationSpeed", "2.0"));
            DamageHeartsModule.movementSpeed = Float.parseFloat(props.getProperty("damageHeartsMovementSpeed", "0.02"));
            DamageHeartsModule.renderDistance = Float.parseFloat(props.getProperty("damageHeartsRenderDistance", "64.0"));
            DamageHeartsModule.combineDelayMs = Integer.parseInt(props.getProperty("damageHeartsCombineDelayMs", "150"));
            DamageHeartsModule.goldenHeartsMode = Boolean.parseBoolean(props.getProperty("damageHeartsGoldenHeartsMode", "true"));
            invHudX = Integer.parseInt(props.getProperty("invHudX", "10"));
            invHudY = Integer.parseInt(props.getProperty("invHudY", "10"));
            invHudScale = Float.parseFloat(props.getProperty("invHudScale", "1.0"));
            fpsCounterX = Integer.parseInt(props.getProperty("fpsCounterX", "10"));
            fpsCounterY = Integer.parseInt(props.getProperty("fpsCounterY", "35"));
            fpsCounterScale = Float.parseFloat(props.getProperty("fpsCounterScale", "1.0"));
            coordinatesX = Integer.parseInt(props.getProperty("coordinatesX", "10"));
            coordinatesY = Integer.parseInt(props.getProperty("coordinatesY", "60"));
            coordinatesScale = Float.parseFloat(props.getProperty("coordinatesScale", "1.0"));
            cpsX = Integer.parseInt(props.getProperty("cpsX", "10"));
            cpsY = Integer.parseInt(props.getProperty("cpsY", "110"));
            cpsScale = Float.parseFloat(props.getProperty("cpsScale", "1.0"));
            keystrokesX = Integer.parseInt(props.getProperty("keystrokesX", "10"));
            keystrokesY = Integer.parseInt(props.getProperty("keystrokesY", "135"));
            keystrokesScale = Float.parseFloat(props.getProperty("keystrokesScale", "1.0"));
            keystrokesMode = Integer.parseInt(props.getProperty("keystrokesMode", "4"));
            keystrokesShowMouse = Boolean.parseBoolean(props.getProperty("keystrokesShowMouse", "true"));
            nickHiderTargetName = props.getProperty("nickHiderTargetName", "");
            nickHiderReplacementName = props.getProperty("nickHiderReplacementName", "Hidden");
            nickHiderUseCustomColor = Boolean.parseBoolean(props.getProperty("nickHiderUseCustomColor", "false"));

            try {
               nickHiderColor = Integer.parseInt(props.getProperty("nickHiderColor", "16733695"));
            } catch (Exception var8) {
            }

            invHudBorderThickness = Integer.parseInt(props.getProperty("invHudBorderThickness", "2"));
            invHudBorderColor = Integer.parseInt(props.getProperty("invHudBorderColor", "16711935"));
            invHudShowHotbar = Boolean.parseBoolean(props.getProperty("invHudShowHotbar", "false"));
            invHudShowItemCount = Boolean.parseBoolean(props.getProperty("invHudShowItemCount", "true"));
            compassX = Integer.parseInt(props.getProperty("compassX", "10"));
            compassY = Integer.parseInt(props.getProperty("compassY", "165"));
            compassScale = Float.parseFloat(props.getProperty("compassScale", "1.0"));
            armorStatusX = Integer.parseInt(props.getProperty("armorStatusX", "10"));
            armorStatusY = Integer.parseInt(props.getProperty("armorStatusY", "195"));
            armorStatusScale = Float.parseFloat(props.getProperty("armorStatusScale", "1.0"));
            isTargetHudEnabled = Boolean.parseBoolean(props.getProperty("isTargetHudEnabled", "false"));
            targetHudX = Integer.parseInt(props.getProperty("targetHudX", "150"));
            targetHudY = Integer.parseInt(props.getProperty("targetHudY", "150"));
            targetHudScale = Float.parseFloat(props.getProperty("targetHudScale", "1.0"));
            targetHudMmoArmorDisplay = Boolean.parseBoolean(props.getProperty("targetHudMmoArmorDisplay", "false"));
            targetHudMmoArmorX = Integer.parseInt(props.getProperty("targetHudMmoArmorX", "150"));
            targetHudMmoArmorY = Integer.parseInt(props.getProperty("targetHudMmoArmorY", "215"));
            targetHudMmoArmorScale = Float.parseFloat(props.getProperty("targetHudMmoArmorScale", "1.0"));
            isStatsHudEnabled = Boolean.parseBoolean(props.getProperty("isStatsHudEnabled", "false"));
            statsHudX = Integer.parseInt(props.getProperty("statsHudX", "10"));
            statsHudY = Integer.parseInt(props.getProperty("statsHudY", "100"));
            statsHudScale = Float.parseFloat(props.getProperty("statsHudScale", "1.0"));
            allTimeKills = Integer.parseInt(props.getProperty("allTimeKills", "0"));
            allTimeDeaths = Integer.parseInt(props.getProperty("allTimeDeaths", "0"));
            allTimeCrystalDeaths = Integer.parseInt(props.getProperty("allTimeCrystalDeaths", "0"));
            allTimePops = Integer.parseInt(props.getProperty("allTimePops", "0"));
            allTimeCrystalKills = Integer.parseInt(props.getProperty("allTimeCrystalKills", "0"));
            allTimeAnchorKills = Integer.parseInt(props.getProperty("allTimeAnchorKills", "0"));
            allTimeClicks = Integer.parseInt(props.getProperty("allTimeClicks", "0"));
            allTimeHits = Integer.parseInt(props.getProperty("allTimeHits", "0"));
            allTimeDamage = Double.parseDouble(props.getProperty("allTimeDamage", "0.0"));
            allTimeBlocksBroken = Integer.parseInt(props.getProperty("allTimeBlocksBroken", "0"));
            allTimeBlocksPlaced = Integer.parseInt(props.getProperty("allTimeBlocksPlaced", "0"));
            allTimeCrystalsPlaced = Integer.parseInt(props.getProperty("allTimeCrystalsPlaced", "0"));
            allTimeCrystalsBroken = Integer.parseInt(props.getProperty("allTimeCrystalsBroken", "0"));
            allTimeAnchorsPlaced = Integer.parseInt(props.getProperty("allTimeAnchorsPlaced", "0"));
            allTimeAnchorsBlown = Integer.parseInt(props.getProperty("allTimeAnchorsBlown", "0"));
            allTimeAnchorsCharged = Integer.parseInt(props.getProperty("allTimeAnchorsCharged", "0"));
            allTimePlayTimeTicks = Long.parseLong(props.getProperty("allTimePlayTimeTicks", "0"));
            sessionBlocksBroken = Integer.parseInt(props.getProperty("sessionBlocksBroken", "0"));
            sessionBlocksPlaced = Integer.parseInt(props.getProperty("sessionBlocksPlaced", "0"));
            sessionCrystalsPlaced = Integer.parseInt(props.getProperty("sessionCrystalsPlaced", "0"));
            sessionCrystalsBroken = Integer.parseInt(props.getProperty("sessionCrystalsBroken", "0"));
            sessionAnchorsPlaced = Integer.parseInt(props.getProperty("sessionAnchorsPlaced", "0"));
            sessionAnchorsBlown = Integer.parseInt(props.getProperty("sessionAnchorsBlown", "0"));
            sessionAnchorsCharged = Integer.parseInt(props.getProperty("sessionAnchorsCharged", "0"));
            disableHotbarLooping = Boolean.parseBoolean(props.getProperty("disableHotbarLooping", "false"));
            isCustomHitboxEnabled = Boolean.parseBoolean(props.getProperty("isCustomHitboxEnabled", "false"));
            isCustomHitboxDynamic = Boolean.parseBoolean(props.getProperty("isCustomHitboxDynamic", "false"));
            hitboxRed = Integer.parseInt(props.getProperty("hitboxRed", "255"));
            hitboxGreen = Integer.parseInt(props.getProperty("hitboxGreen", "0"));
            hitboxBlue = Integer.parseInt(props.getProperty("hitboxBlue", "0"));
            hitboxThickness = Float.parseFloat(props.getProperty("hitboxThickness", "2.0"));
            hitboxShowCrystal = Boolean.parseBoolean(props.getProperty("hitboxShowCrystal", "true"));
            crystalHitboxRed = Integer.parseInt(props.getProperty("crystalHitboxRed", "0"));
            crystalHitboxGreen = Integer.parseInt(props.getProperty("crystalHitboxGreen", "200"));
            crystalHitboxBlue = Integer.parseInt(props.getProperty("crystalHitboxBlue", "255"));
            isDamageHeartsEnabled = Boolean.parseBoolean(props.getProperty("isDamageHeartsEnabled", "false"));
            isHealthIndicatorsEnabled = Boolean.parseBoolean(props.getProperty("isHealthIndicatorsEnabled", "false"));
            healthIndicatorsMode = Integer.parseInt(props.getProperty("healthIndicatorsMode", "0"));
            healthIndicatorsType = Integer.parseInt(props.getProperty("healthIndicatorsType", "0"));
            healthIndicatorsFuseAbsorption = Boolean.parseBoolean(props.getProperty("healthIndicatorsFuseAbsorption", "true"));
            healthIndicatorsRange = Integer.parseInt(props.getProperty("healthIndicatorsRange", "20"));
            targetIndicatorMaxReach = Float.parseFloat(props.getProperty("targetIndicatorMaxReach", "64.0"));
            targetIndicatorSpinning = Boolean.parseBoolean(props.getProperty("targetIndicatorSpinning", "true"));
            targetIndicatorSpinSpeed = Float.parseFloat(props.getProperty("targetIndicatorSpinSpeed", "1.0"));
            targetIndicatorHitReachOnly = Boolean.parseBoolean(props.getProperty("targetIndicatorHitReachOnly", "false"));
            attackIndicatorEnabled = Boolean.parseBoolean(props.getProperty("attackIndicatorEnabled", "false"));
            attackIndicatorOnlyPlayers = Boolean.parseBoolean(props.getProperty("attackIndicatorOnlyPlayers", "true"));
            attackIndicatorAllowNonLiving = Boolean.parseBoolean(props.getProperty("attackIndicatorAllowNonLiving", "false"));
            antiBot = Boolean.parseBoolean(props.getProperty("antiBot", "true"));
            attackIndicatorAlwaysActive = Boolean.parseBoolean(props.getProperty("attackIndicatorAlwaysActive", "true"));
            attackIndicatorStyle = props.getProperty("attackIndicatorStyle", "LEGACY");
            attackIndicatorLiveTime = Float.parseFloat(props.getProperty("attackIndicatorLiveTime", "3.0"));
            attackIndicatorLegacyTexture = props.getProperty("attackIndicatorLegacyTexture", "LEGACY");
            attackIndicatorLegacyRollSpeed = Integer.parseInt(props.getProperty("attackIndicatorLegacyRollSpeed", "70"));
            attackIndicatorLegacyScale = Integer.parseInt(props.getProperty("attackIndicatorLegacyScale", "60"));
            attackIndicatorLegacyAlpha = Integer.parseInt(props.getProperty("attackIndicatorLegacyAlpha", "80"));
            attackIndicatorSoulStyle = props.getProperty("attackIndicatorSoulStyle", "SMOKE");
            attackIndicatorSoulTexture = props.getProperty("attackIndicatorSoulTexture", "ALT");
            attackIndicatorSoulLength = Integer.parseInt(props.getProperty("attackIndicatorSoulLength", "5"));
            attackIndicatorSoulFactor = Float.parseFloat(props.getProperty("attackIndicatorSoulFactor", "3.0"));
            attackIndicatorSoulShaking = Float.parseFloat(props.getProperty("attackIndicatorSoulShaking", "3.0"));
            attackIndicatorSoulAmplitude = Float.parseFloat(props.getProperty("attackIndicatorSoulAmplitude", "3.0"));
            attackIndicatorSoulRadius = Float.parseFloat(props.getProperty("attackIndicatorSoulRadius", "100.0"));
            attackIndicatorSoulStartSize = Float.parseFloat(props.getProperty("attackIndicatorSoulStartSize", "50.0"));
            attackIndicatorSoulEndSize = Float.parseFloat(props.getProperty("attackIndicatorSoulEndSize", "20.0"));
            attackIndicatorSoulScale = Float.parseFloat(props.getProperty("attackIndicatorSoulScale", "40.0"));
            attackIndicatorSoulSubdivision = Integer.parseInt(props.getProperty("attackIndicatorSoulSubdivision", "3"));
            attackIndicatorTopkaRadius = Float.parseFloat(props.getProperty("attackIndicatorTopkaRadius", "50.0"));
            attackIndicatorTopkaSpeed = Float.parseFloat(props.getProperty("attackIndicatorTopkaSpeed", "30.0"));
            DamageHeartsModule.showPlayers = Boolean.parseBoolean(props.getProperty("damageHeartsShowPlayers", "true"));
            DamageHeartsModule.showHostile = Boolean.parseBoolean(props.getProperty("damageHeartsShowHostile", "true"));
            DamageHeartsModule.showPassive = Boolean.parseBoolean(props.getProperty("damageHeartsShowPassive", "true"));
            DamageHeartsModule.showAnimals = Boolean.parseBoolean(props.getProperty("damageHeartsShowAnimals", "true"));
            DamageHeartsModule.showBosses = Boolean.parseBoolean(props.getProperty("damageHeartsShowBosses", "true"));
            DamageHeartsModule.heartScale = Float.parseFloat(props.getProperty("damageHeartsHeartScale", "1.0"));
            DamageHeartsModule.fadeTime = Float.parseFloat(props.getProperty("damageHeartsFadeTime", "2.0"));
            DamageHeartsModule.lifetime = Float.parseFloat(props.getProperty("damageHeartsLifetime", "2.5"));
            DamageHeartsModule.glowEnabled = Boolean.parseBoolean(props.getProperty("damageHeartsGlowEnabled", "true"));
            DamageHeartsModule.critGlowEnabled = Boolean.parseBoolean(props.getProperty("damageHeartsCritGlowEnabled", "true"));
            DamageHeartsModule.rainbowMode = Boolean.parseBoolean(props.getProperty("damageHeartsRainbowMode", "false"));
            DamageHeartsModule.showNumeric = Boolean.parseBoolean(props.getProperty("damageHeartsShowNumeric", "false"));
            DamageHeartsModule.distanceScaling = Boolean.parseBoolean(props.getProperty("damageHeartsDistanceScaling", "true"));
            DamageHeartsModule.rotationSpeed = Float.parseFloat(props.getProperty("damageHeartsRotationSpeed", "2.0"));
            DamageHeartsModule.movementSpeed = Float.parseFloat(props.getProperty("damageHeartsMovementSpeed", "0.02"));
            DamageHeartsModule.renderDistance = Float.parseFloat(props.getProperty("damageHeartsRenderDistance", "64.0"));
            DamageHeartsModule.combineDelayMs = Integer.parseInt(props.getProperty("damageHeartsCombineDelayMs", "150"));
            DamageHeartsModule.goldenHeartsMode = Boolean.parseBoolean(props.getProperty("damageHeartsGoldenHeartsMode", "true"));
            invHudX = Integer.parseInt(props.getProperty("invHudX", "10"));
            invHudY = Integer.parseInt(props.getProperty("invHudY", "10"));
            invHudScale = Float.parseFloat(props.getProperty("invHudScale", "1.0"));
            fpsCounterX = Integer.parseInt(props.getProperty("fpsCounterX", "10"));
            fpsCounterY = Integer.parseInt(props.getProperty("fpsCounterY", "35"));
            fpsCounterScale = Float.parseFloat(props.getProperty("fpsCounterScale", "1.0"));
            coordinatesX = Integer.parseInt(props.getProperty("coordinatesX", "10"));
            coordinatesY = Integer.parseInt(props.getProperty("coordinatesY", "60"));
            coordinatesScale = Float.parseFloat(props.getProperty("coordinatesScale", "1.0"));
            cpsX = Integer.parseInt(props.getProperty("cpsX", "10"));
            cpsY = Integer.parseInt(props.getProperty("cpsY", "110"));
            cpsScale = Float.parseFloat(props.getProperty("cpsScale", "1.0"));
            keystrokesX = Integer.parseInt(props.getProperty("keystrokesX", "10"));
            keystrokesY = Integer.parseInt(props.getProperty("keystrokesY", "135"));
            keystrokesScale = Float.parseFloat(props.getProperty("keystrokesScale", "1.0"));
            keystrokesMode = Integer.parseInt(props.getProperty("keystrokesMode", "4"));
            keystrokesShowMouse = Boolean.parseBoolean(props.getProperty("keystrokesShowMouse", "true"));
            nickHiderTargetName = props.getProperty("nickHiderTargetName", "");
            nickHiderReplacementName = props.getProperty("nickHiderReplacementName", "Hidden");
            nickHiderUseCustomColor = Boolean.parseBoolean(props.getProperty("nickHiderUseCustomColor", "false"));

            try {
               nickHiderColor = Integer.parseInt(props.getProperty("nickHiderColor", "16733695"));
            } catch (Exception var7) {
            }

            invHudBorderThickness = Integer.parseInt(props.getProperty("invHudBorderThickness", "2"));
            invHudBorderColor = Integer.parseInt(props.getProperty("invHudBorderColor", "16711935"));
            invHudShowHotbar = Boolean.parseBoolean(props.getProperty("invHudShowHotbar", "false"));
            invHudTransparent = Boolean.parseBoolean(props.getProperty("invHudTransparent", "false"));
            invHudShowItemCount = Boolean.parseBoolean(props.getProperty("invHudShowItemCount", "true"));
            compassX = Integer.parseInt(props.getProperty("compassX", "10"));
            compassY = Integer.parseInt(props.getProperty("compassY", "165"));
            compassScale = Float.parseFloat(props.getProperty("compassScale", "1.0"));
            armorStatusX = Integer.parseInt(props.getProperty("armorStatusX", "10"));
            armorStatusY = Integer.parseInt(props.getProperty("armorStatusY", "195"));
            armorStatusScale = Float.parseFloat(props.getProperty("armorStatusScale", "1.0"));
            isTargetHudEnabled = Boolean.parseBoolean(props.getProperty("isTargetHudEnabled", "false"));
            targetHudX = Integer.parseInt(props.getProperty("targetHudX", "150"));
            targetHudY = Integer.parseInt(props.getProperty("targetHudY", "150"));
            targetHudScale = Float.parseFloat(props.getProperty("targetHudScale", "1.0"));
            targetHudMmoArmorDisplay = Boolean.parseBoolean(props.getProperty("targetHudMmoArmorDisplay", "false"));
            targetHudMmoArmorX = Integer.parseInt(props.getProperty("targetHudMmoArmorX", "150"));
            targetHudMmoArmorY = Integer.parseInt(props.getProperty("targetHudMmoArmorY", "215"));
            targetHudMmoArmorScale = Float.parseFloat(props.getProperty("targetHudMmoArmorScale", "1.0"));
            isStatsHudEnabled = Boolean.parseBoolean(props.getProperty("isStatsHudEnabled", "false"));
            statsHudX = Integer.parseInt(props.getProperty("statsHudX", "10"));
            statsHudY = Integer.parseInt(props.getProperty("statsHudY", "100"));
            statsHudScale = Float.parseFloat(props.getProperty("statsHudScale", "1.0"));
            statsHudShowKills = Boolean.parseBoolean(props.getProperty("statsHudShowKills", "true"));
            statsHudShowDeaths = Boolean.parseBoolean(props.getProperty("statsHudShowDeaths", "true"));
            statsHudShowKD = Boolean.parseBoolean(props.getProperty("statsHudShowKD", "false"));
            statsHudShowPops = Boolean.parseBoolean(props.getProperty("statsHudShowPops", "true"));
            statsHudShowClicks = Boolean.parseBoolean(props.getProperty("statsHudShowClicks", "false"));
            statsHudShowHits = Boolean.parseBoolean(props.getProperty("statsHudShowHits", "false"));
            statsHudShowAttributeSwap = Boolean.parseBoolean(props.getProperty("statsHudShowAttributeSwap", "false"));
            statsHudShowAccuracy = Boolean.parseBoolean(props.getProperty("statsHudShowAccuracy", "true"));
            statsHudShowDamage = Boolean.parseBoolean(props.getProperty("statsHudShowDamage", "false"));
            statsHudShowBlocksBroken = Boolean.parseBoolean(props.getProperty("statsHudShowBlocksBroken", "false"));
            statsHudShowBlocksPlaced = Boolean.parseBoolean(props.getProperty("statsHudShowBlocksPlaced", "false"));
            statsHudShowCrystalsPlaced = Boolean.parseBoolean(props.getProperty("statsHudShowCrystalsPlaced", "false"));
            statsHudShowCrystalsBroken = Boolean.parseBoolean(props.getProperty("statsHudShowCrystalsBroken", "false"));
            statsHudShowAnchorsPlaced = Boolean.parseBoolean(props.getProperty("statsHudShowAnchorsPlaced", "false"));
            statsHudShowAnchorsBlown = Boolean.parseBoolean(props.getProperty("statsHudShowAnchorsBlown", "false"));
            statsHudShowAnchorsCharged = Boolean.parseBoolean(props.getProperty("statsHudShowAnchorsCharged", "false"));
            statsHudShowCrystalKills = Boolean.parseBoolean(props.getProperty("statsHudShowCrystalKills", "false"));
            statsHudShowCrystalDeaths = Boolean.parseBoolean(props.getProperty("statsHudShowCrystalDeaths", "false"));
            statsHudShowAnchors = Boolean.parseBoolean(props.getProperty("statsHudShowAnchors", "true"));
            statsHudShowTime = Boolean.parseBoolean(props.getProperty("statsHudShowTime", "true"));
            statsHudStatMode = Integer.parseInt(props.getProperty("statsHudStatMode", "0"));
            statsHudShowInMultiplayer = Boolean.parseBoolean(props.getProperty("statsHudShowInMultiplayer", "true"));
            if (targetHudScale > 5.0F) {
               targetHudScale = 1.0F;
            }

            targetHudShowArmor = Boolean.parseBoolean(props.getProperty("targetHudShowArmor", "true"));
            targetHudShowArmorDurability = Boolean.parseBoolean(props.getProperty("targetHudShowArmorDurability", "true"));
            targetHudShowEnemyCrucials = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyCrucials", "true"));
            targetHudShowEnemyPearls = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyPearls", "true"));
            targetHudShowEnemyGapples = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyGapples", "true"));
            targetHudShowEnemyCobwebs = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyCobwebs", "true"));
            targetHudShowEnemyTotems = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyTotems", "true"));
            targetHudShowEnemyPops = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyPops", "true"));
            targetHudShowEnemyWindCharges = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyWindCharges", "false"));
            targetHudShowEnemyWindChargesUsed = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyWindChargesUsed", "false"));
            targetHudShowEnemyHealthPots = Boolean.parseBoolean(props.getProperty("targetHudShowEnemyHealthPots", "false"));
            targetHudStickyEnabled = Boolean.parseBoolean(props.getProperty("targetHudStickyEnabled", "true"));
            targetHudStickyDuration = Integer.parseInt(props.getProperty("targetHudStickyDuration", "3"));
            targetHudAboveHead = Boolean.parseBoolean(props.getProperty("targetHudAboveHead", "false"));
            targetHudAboveHeadScale = Float.parseFloat(props.getProperty("targetHudAboveHeadScale", "1.0"));
            enemyCrucialsHudX = Integer.parseInt(props.getProperty("enemyCrucialsHudX", "320"));
            enemyCrucialsHudY = Integer.parseInt(props.getProperty("enemyCrucialsHudY", "150"));
            enemyCrucialsHudScale = Float.parseFloat(props.getProperty("enemyCrucialsHudScale", "1.0"));
            armorStatusDisplayMode = Integer.parseInt(props.getProperty("armorStatusDisplayMode", "0"));
            armorStatusShowDurabilityBar = Boolean.parseBoolean(props.getProperty("armorStatusShowDurabilityBar", "true"));
            armorStatusDurabilityBarColor = Integer.parseInt(props.getProperty("armorStatusDurabilityBarColor", "65280"));
            armorStatusShowEmptySlots = Boolean.parseBoolean(props.getProperty("armorStatusShowEmptySlots", "false"));
            armorStatusOrientation = Integer.parseInt(props.getProperty("armorStatusOrientation", "0"));
            isPotionStatusEnabled = Boolean.parseBoolean(props.getProperty("isPotionStatusEnabled", "false"));
            potionStatusX = Integer.parseInt(props.getProperty("potionStatusX", "10"));
            potionStatusY = Integer.parseInt(props.getProperty("potionStatusY", "225"));
            potionStatusScale = Float.parseFloat(props.getProperty("potionStatusScale", "1.0"));
            potionStatusOrientation = Integer.parseInt(props.getProperty("potionStatusOrientation", "0"));
            potionStatusShowDuration = Boolean.parseBoolean(props.getProperty("potionStatusShowDuration", "true"));
            potionStatusShowAmplifier = Boolean.parseBoolean(props.getProperty("potionStatusShowAmplifier", "true"));
            potionStatusIconType = Integer.parseInt(props.getProperty("potionStatusIconType", "0"));
            armorStatusWarningThreshold = Integer.parseInt(props.getProperty("armorStatusWarningThreshold", "20"));
            armorStatusWarningSound = Boolean.parseBoolean(props.getProperty("armorStatusWarningSound", "true"));
            armorStatusWarningSoundType = Integer.parseInt(props.getProperty("armorStatusWarningSoundType", "0"));
            armorStatusTransparentBg = Boolean.parseBoolean(props.getProperty("armorStatusTransparentBg", "false"));
            armorStatusWarningSoundFile = props.getProperty("armorStatusWarningSoundFile", "Default Beep");
            lowHealthAlarmSoundFile = props.getProperty("lowHealthAlarmSoundFile", "Default Beep");
            potWarnSoundFile = props.getProperty("potWarnSoundFile", "Default Beep");
            zoomSmoothAnimation = Boolean.parseBoolean(props.getProperty("zoomSmoothAnimation", "true"));
            zoomAnimationSpeed = Float.parseFloat(props.getProperty("zoomAnimationSpeed", "0.15"));
            editGuiKey = Integer.parseInt(props.getProperty("editGuiKey", "93"));
            isBigHeadEnabled = Boolean.parseBoolean(props.getProperty("isBigHeadEnabled", "false"));
            bigHeadSelf = Boolean.parseBoolean(props.getProperty("bigHeadSelf", "true"));
            bigHeadFriends = Boolean.parseBoolean(props.getProperty("bigHeadFriends", "true"));
            bigHeadOthers = Boolean.parseBoolean(props.getProperty("bigHeadOthers", "false"));
            bigHeadScale = Float.parseFloat(props.getProperty("bigHeadScale", "1.5"));
            isBlockOverlayEnabled = Boolean.parseBoolean(props.getProperty("isBlockOverlayEnabled", "false"));
            blockOverlayMode = Integer.parseInt(props.getProperty("blockOverlayMode", "0"));
            blockOverlayOutline = Boolean.parseBoolean(props.getProperty("blockOverlayOutline", "true"));
            blockOverlayFill = Boolean.parseBoolean(props.getProperty("blockOverlayFill", "false"));
            blockOverlayGlow = Boolean.parseBoolean(props.getProperty("blockOverlayGlow", "false"));
            blockOverlayRed = Integer.parseInt(props.getProperty("blockOverlayRed", "0"));
            blockOverlayGreen = Integer.parseInt(props.getProperty("blockOverlayGreen", "255"));
            blockOverlayBlue = Integer.parseInt(props.getProperty("blockOverlayBlue", "255"));
            blockOverlayThickness = Float.parseFloat(props.getProperty("blockOverlayThickness", "2.0"));
            blockOverlayFillOpacity = Integer.parseInt(props.getProperty("blockOverlayFillOpacity", "25"));
            blockOverlayGlowStrength = Float.parseFloat(props.getProperty("blockOverlayGlowStrength", "1.0"));
            isChinaHatEnabled = Boolean.parseBoolean(props.getProperty("isChinaHatEnabled", "false"));
            chinaHatRed = Integer.parseInt(props.getProperty("chinaHatRed", "0"));
            chinaHatGreen = Integer.parseInt(props.getProperty("chinaHatGreen", "0"));
            chinaHatBlue = Integer.parseInt(props.getProperty("chinaHatBlue", "0"));
            chinaHatFilled = Boolean.parseBoolean(props.getProperty("chinaHatFilled", "false"));
            chinaHatSize = Float.parseFloat(props.getProperty("chinaHatSize", "1.0"));
            isInvHighlightEnabled = Boolean.parseBoolean(props.getProperty("isInvHighlightEnabled", "false"));
            invHighlightItems = props.getProperty("invHighlightItems", "minecraft:totem_of_undying");
            invHighlightRed = Integer.parseInt(props.getProperty("invHighlightRed", "255"));
            invHighlightGreen = Integer.parseInt(props.getProperty("invHighlightGreen", "0"));
            invHighlightBlue = Integer.parseInt(props.getProperty("invHighlightBlue", "0"));
            isMouseStrokesEnabled = Boolean.parseBoolean(props.getProperty("isMouseStrokesEnabled", "false"));
            mouseStrokesX = Integer.parseInt(props.getProperty("mouseStrokesX", "100"));
            mouseStrokesY = Integer.parseInt(props.getProperty("mouseStrokesY", "100"));
            mouseStrokesScale = Float.parseFloat(props.getProperty("mouseStrokesScale", "1.0"));
            mouseStrokesShowCross = Boolean.parseBoolean(props.getProperty("mouseStrokesShowCross", "true"));
            mouseStrokesSensitivity = Float.parseFloat(props.getProperty("mouseStrokesSensitivity", "2.0"));
            mouseStrokesSymbolRed = Integer.parseInt(props.getProperty("mouseStrokesSymbolRed", "0"));
            mouseStrokesSymbolGreen = Integer.parseInt(props.getProperty("mouseStrokesSymbolGreen", "150"));
            mouseStrokesSymbolBlue = Integer.parseInt(props.getProperty("mouseStrokesSymbolBlue", "255"));
            mouseStrokesBoxRed = Integer.parseInt(props.getProperty("mouseStrokesBoxRed", "255"));
            mouseStrokesBoxGreen = Integer.parseInt(props.getProperty("mouseStrokesBoxGreen", "255"));
            mouseStrokesBoxBlue = Integer.parseInt(props.getProperty("mouseStrokesBoxBlue", "255"));
            isTransparentShieldEnabled = Boolean.parseBoolean(props.getProperty("isTransparentShieldEnabled", "false"));
            transparentShieldX = Integer.parseInt(props.getProperty("transparentShieldX", "10"));
            transparentShieldY = Integer.parseInt(props.getProperty("transparentShieldY", "25"));
            transparentShieldScale = Float.parseFloat(props.getProperty("transparentShieldScale", "1.0"));
            transparentShieldStatusColor = Integer.parseInt(props.getProperty("transparentShieldStatusColor", "-16711681"));
            transparentShieldStatusOutlineColor = Integer.parseInt(props.getProperty("transparentShieldStatusOutlineColor", "-1"));
            isLogoutSpotsEnabled = Boolean.parseBoolean(props.getProperty("isLogoutSpotsEnabled", "false"));
            logoutSpotsDetectDisappears = Boolean.parseBoolean(props.getProperty("logoutSpotsDetectDisappears", "true"));
            logoutSpotsLeaveMessages = Boolean.parseBoolean(props.getProperty("logoutSpotsLeaveMessages", "true"));
            logoutSpotsIgnoreFriends = Boolean.parseBoolean(props.getProperty("logoutSpotsIgnoreFriends", "true"));
            logoutSpotsCreateWaypoints = Boolean.parseBoolean(props.getProperty("logoutSpotsCreateWaypoints", "false"));
            logoutSpotsSaveRotation = Boolean.parseBoolean(props.getProperty("logoutSpotsSaveRotation", "false"));
            logoutSpotsSaveHeldItem = Boolean.parseBoolean(props.getProperty("logoutSpotsSaveHeldItem", "false"));
            primaryColorR = Integer.parseInt(props.getProperty("primaryColorR", "0"));
            primaryColorG = Integer.parseInt(props.getProperty("primaryColorG", "255"));
            primaryColorB = Integer.parseInt(props.getProperty("primaryColorB", "255"));
            secondaryColorR = Integer.parseInt(props.getProperty("secondaryColorR", "255"));
            secondaryColorG = Integer.parseInt(props.getProperty("secondaryColorG", "0"));
            secondaryColorB = Integer.parseInt(props.getProperty("secondaryColorB", "255"));
            rgbChromaEnabled = Boolean.parseBoolean(props.getProperty("rgbChromaEnabled", "false"));
            colorTheme = props.getProperty("colorTheme", "Cyan");
            rgbSpeed = Integer.parseInt(props.getProperty("rgbSpeed", "5"));
            hudFpsLimit = Integer.parseInt(props.getProperty("hudFpsLimit", "0"));
            CustomF3Manager.load(props);
            DeathInfoManager.load(props);
         } catch (IOException var11) {
            var11.printStackTrace();
         }
      }
   }

   public static void syncDamageHeartsToModule() {
      DamageHeartsModule.isEnabled = isDamageHeartsEnabled;
   }

   public static void markConfigDirty() {
      configDirty = true;
   }

   public static void saveConfig() {
      configDirty = false;
      Properties props = new Properties();
      props.setProperty("invHudEnabled", String.valueOf(isInvHudEnabled));
      props.setProperty("guiBgEnabled", String.valueOf(guiBgEnabled));
      props.setProperty("editGuiBgEnabled", String.valueOf(editGuiBgEnabled));
      props.setProperty("clickGuiBgEnabled", String.valueOf(clickGuiBgEnabled));
      props.setProperty("guiBgOpacity", String.valueOf(guiBgOpacity));
      props.setProperty("guiBackgroundImagePath", guiBackgroundImagePath);
      props.setProperty("guiProfile", String.valueOf(guiProfile));
      props.setProperty("optimizeItems", String.valueOf(OptimizeManager.optimizeItems));
      props.setProperty("optimizeChests", String.valueOf(OptimizeManager.optimizeChests));
      props.setProperty("optimizeSigns", String.valueOf(OptimizeManager.optimizeSigns));
      props.setProperty("optimizePlayers", String.valueOf(OptimizeManager.optimizePlayers));
      props.setProperty("optimizeSignTextDistance", String.valueOf(OptimizeManager.signTextDistance));
      props.setProperty("critEffects", String.valueOf(CritEffectsManager.enabled));
      props.setProperty("fpsCounterEnabled", String.valueOf(isFpsCounterEnabled));
      props.setProperty("coordinatesEnabled", String.valueOf(isCoordinatesEnabled));
      props.setProperty("cpsEnabled", String.valueOf(isCpsEnabled));
      props.setProperty("keystrokesEnabled", String.valueOf(isKeystrokesEnabled));
      props.setProperty("nickHiderEnabled", String.valueOf(isNickHiderEnabled));
      props.setProperty("peerNickEnabled", String.valueOf(isPeerNickEnabled));
      props.setProperty("invHudX", String.valueOf(invHudX));
      props.setProperty("invHudY", String.valueOf(invHudY));
      props.setProperty("invHudScale", String.valueOf(invHudScale));
      props.setProperty("fpsCounterX", String.valueOf(fpsCounterX));
      props.setProperty("fpsCounterY", String.valueOf(fpsCounterY));
      props.setProperty("fpsCounterScale", String.valueOf(fpsCounterScale));
      props.setProperty("coordinatesX", String.valueOf(coordinatesX));
      props.setProperty("coordinatesY", String.valueOf(coordinatesY));
      props.setProperty("coordinatesScale", String.valueOf(coordinatesScale));
      props.setProperty("cpsX", String.valueOf(cpsX));
      props.setProperty("cpsY", String.valueOf(cpsY));
      props.setProperty("cpsScale", String.valueOf(cpsScale));
      props.setProperty("keystrokesX", String.valueOf(keystrokesX));
      props.setProperty("keystrokesY", String.valueOf(keystrokesY));
      props.setProperty("keystrokesScale", String.valueOf(keystrokesScale));
      props.setProperty("keystrokesMode", String.valueOf(keystrokesMode));
      props.setProperty("keystrokesShowMouse", String.valueOf(keystrokesShowMouse));
      props.setProperty("nickHiderTargetName", nickHiderTargetName);
      props.setProperty("nickHiderReplacementName", nickHiderReplacementName);
      props.setProperty("nickHiderUseCustomColor", String.valueOf(nickHiderUseCustomColor));
      props.setProperty("nickHiderColor", String.valueOf(nickHiderColor));
      props.setProperty("invHudBorderThickness", String.valueOf(invHudBorderThickness));
      props.setProperty("invHudBorderColor", String.valueOf(invHudBorderColor));
      props.setProperty("invHudShowHotbar", String.valueOf(invHudShowHotbar));
      props.setProperty("invHudTransparent", String.valueOf(invHudTransparent));
      props.setProperty("invHudShowItemCount", String.valueOf(invHudShowItemCount));
      props.setProperty("compassEnabled", String.valueOf(isCompassEnabled));
      props.setProperty("isTargetHudEnabled", String.valueOf(isTargetHudEnabled));
      props.setProperty("isStatsHudEnabled", String.valueOf(isStatsHudEnabled));
      props.setProperty("isFreelookEnabled", String.valueOf(isFreelookEnabled));
      props.setProperty("isZoomEnabled", String.valueOf(isZoomEnabled));
      props.setProperty("isFullbrightEnabled", String.valueOf(isFullbrightEnabled));
      props.setProperty("isBigItemsEnabled", String.valueOf(isBigItemsEnabled));
      props.setProperty("isItemPhysicsEnabled", String.valueOf(isItemPhysicsEnabled));
      props.setProperty("itemPhysicsRotationX", String.valueOf(itemPhysicsRotationX));
      props.setProperty("itemPhysicsOffsetY", String.valueOf(itemPhysicsOffsetY));
      props.setProperty("itemPhysicsOffsetZ", String.valueOf(itemPhysicsOffsetZ));
      props.setProperty("itemPhysicsFallFlips", String.valueOf(itemPhysicsFallFlips));
      props.setProperty("itemPhysicsFlipsPerBlock", String.valueOf(itemPhysicsFlipsPerBlock));
      props.setProperty("isPlayerModelEnabled", String.valueOf(isPlayerModelEnabled));
      props.setProperty("bigItemsItemIds", bigItemsItemIds != null ? bigItemsItemIds : "");
      props.setProperty("bigItemsScale", String.valueOf(bigItemsScale));
      props.setProperty("isLowHealthAlarmEnabled", String.valueOf(isLowHealthAlarmEnabled));
      props.setProperty("lowHealthAlarmSound", String.valueOf(lowHealthAlarmSound));
      props.setProperty("lowHealthAlarmText", String.valueOf(lowHealthAlarmText));
      props.setProperty("lowHealthAlarmThreshold", String.valueOf(lowHealthAlarmThreshold));
      props.setProperty("lowHealthTotemThreshold", String.valueOf(lowHealthTotemThreshold));
      props.setProperty("lowHealthAlarmX", String.valueOf(lowHealthAlarmX));
      props.setProperty("lowHealthAlarmY", String.valueOf(lowHealthAlarmY));
      props.setProperty("lowHealthAlarmScale", String.valueOf(lowHealthAlarmScale));
      props.setProperty("lowHealthUseThemeColor", String.valueOf(lowHealthUseThemeColor));
      props.setProperty("isPotWarningEnabled", String.valueOf(isPotWarningEnabled));
      props.setProperty("potWarnLowPots", String.valueOf(potWarnLowPots));
      props.setProperty("potWarnPotThreshold", String.valueOf(potWarnPotThreshold));
      props.setProperty("potWarnLowEffects", String.valueOf(potWarnLowEffects));
      props.setProperty("potWarnEffectThreshold", String.valueOf(potWarnEffectThreshold));
      props.setProperty("potWarnSound", String.valueOf(potWarnSound));
      props.setProperty("potWarnShowText", String.valueOf(potWarnShowText));
      props.setProperty("potWarnUseThemeColor", String.valueOf(potWarnUseThemeColor));
      props.setProperty("potWarnIgnoredEffects", potWarnIgnoredEffects);
      props.setProperty("potWarningX", String.valueOf(potWarningX));
      props.setProperty("potWarningY", String.valueOf(potWarningY));
      props.setProperty("potWarningScale", String.valueOf(potWarningScale));
      props.setProperty("isDropPreventionEnabled", String.valueOf(isDropPreventionEnabled));
      props.setProperty("dropPreventionItems", String.valueOf(dropPreventionItems));
      props.setProperty("isHitColorEnabled", String.valueOf(isHitColorEnabled));
      props.setProperty("hitColorApplyToSelf", String.valueOf(hitColorApplyToSelf));
      props.setProperty("hitColorApplyToArmor", String.valueOf(hitColorApplyToArmor));
      props.setProperty("hitColorApplyToArmorStands", String.valueOf(hitColorApplyToArmorStands));
      props.setProperty("hitColorRed", String.valueOf(hitColorRed));
      props.setProperty("hitColorGreen", String.valueOf(hitColorGreen));
      props.setProperty("hitColorBlue", String.valueOf(hitColorBlue));
      props.setProperty("hitColorAlpha", String.valueOf(hitColorAlpha));
      props.setProperty("hitColorDuration", String.valueOf(hitColorDuration));
      props.setProperty("customHitboxRange", String.valueOf(customHitboxRange));
      props.setProperty("isTotemPopColorEnabled", String.valueOf(isTotemPopColorEnabled));
      props.setProperty("totemPopColorDetectSelf", String.valueOf(totemPopColorDetectSelf));
      props.setProperty("totemPopColorDetectOthers", String.valueOf(totemPopColorDetectOthers));
      props.setProperty("totemPopColorApplyToArmor", String.valueOf(totemPopColorApplyToArmor));
      props.setProperty("totemPopColorApplyToArmorStands", String.valueOf(totemPopColorApplyToArmorStands));
      props.setProperty("totemPopColorRed", String.valueOf(totemPopColorRed));
      props.setProperty("totemPopColorGreen", String.valueOf(totemPopColorGreen));
      props.setProperty("totemPopColorBlue", String.valueOf(totemPopColorBlue));
      props.setProperty("totemPopColorAlpha", String.valueOf(totemPopColorAlpha));
      props.setProperty("totemPopColorDuration", String.valueOf(totemPopColorDuration));
      props.setProperty("isDamageColorEnabled", String.valueOf(isDamageColorEnabled));
      props.setProperty("damageColorApplyToSelf", String.valueOf(damageColorApplyToSelf));
      props.setProperty("damageColorApplyToPlayers", String.valueOf(damageColorApplyToPlayers));
      props.setProperty("damageColorApplyToEntities", String.valueOf(damageColorApplyToEntities));
      props.setProperty("damageColorApplyToArmor", String.valueOf(damageColorApplyToArmor));
      props.setProperty("damageColorApplyToArmorStands", String.valueOf(damageColorApplyToArmorStands));
      props.setProperty("damageColorRed", String.valueOf(damageColorRed));
      props.setProperty("damageColorGreen", String.valueOf(damageColorGreen));
      props.setProperty("damageColorBlue", String.valueOf(damageColorBlue));
      props.setProperty("damageColorAlpha", String.valueOf(damageColorAlpha));
      props.setProperty("damageColorDuration", String.valueOf(damageColorDuration));
      props.setProperty("disableHotbarLooping", String.valueOf(disableHotbarLooping));
      props.setProperty("isComboCounterEnabled", String.valueOf(isComboCounterEnabled));
      props.setProperty("comboCounterX", String.valueOf(comboCounterX));
      props.setProperty("comboCounterY", String.valueOf(comboCounterY));
      props.setProperty("comboCounterScale", String.valueOf(comboCounterScale));
      props.setProperty("comboResetKeybind", String.valueOf(comboResetKeybind));
      props.setProperty("comboTimeout", String.valueOf(comboTimeout));
      props.setProperty("comboVersion", String.valueOf(comboVersion));
      props.setProperty("isPingDisplayEnabled", String.valueOf(isPingDisplayEnabled));
      props.setProperty("pingDisplayX", String.valueOf(pingDisplayX));
      props.setProperty("pingDisplayY", String.valueOf(pingDisplayY));
      props.setProperty("pingDisplayScale", String.valueOf(pingDisplayScale));
      props.setProperty("isHurtcamEnabled", String.valueOf(isHurtcamEnabled));
      props.setProperty("isPumpkinEnabled", String.valueOf(isPumpkinEnabled));
      props.setProperty("isWaterFogEnabled", String.valueOf(isWaterFogEnabled));
      props.setProperty("isPortalEnabled", String.valueOf(isPortalEnabled));
      props.setProperty("isFireEnabled", String.valueOf(isFireEnabled));
      props.setProperty("isViewModelEnabled", String.valueOf(isViewModelEnabled));
      props.setProperty("isMainHandViewModelEnabled", String.valueOf(isMainHandViewModelEnabled));
      props.setProperty("isOffHandViewModelEnabled", String.valueOf(isOffHandViewModelEnabled));
      props.setProperty("mainHandGlobal", mainHandGlobal.serialize());
      props.setProperty("offHandGlobal", offHandGlobal.serialize());
      props.stringPropertyNames().stream().filter(k -> k.startsWith("vmMain_") || k.startsWith("vmOff_")).forEach(props::remove);

      for (Entry<String, VoidCyanClient.ViewModelSettings> e : mainHandItemOverrides.entrySet()) {
         props.setProperty("vmMain_" + e.getKey(), e.getValue().serialize());
      }

      for (Entry<String, VoidCyanClient.ViewModelSettings> e : offHandItemOverrides.entrySet()) {
         props.setProperty("vmOff_" + e.getKey(), e.getValue().serialize());
      }

      props.setProperty("isItemAnimationsEnabled", String.valueOf(isItemAnimationsEnabled));
      props.setProperty("itemAnimationMode", String.valueOf(itemAnimationMode));
      props.setProperty("customSwingPitch", String.valueOf(customSwingPitch));
      props.setProperty("customSwingYaw", String.valueOf(customSwingYaw));
      props.setProperty("customSwingRoll", String.valueOf(customSwingRoll));
      props.setProperty("isCustomFovEnabled", String.valueOf(isCustomFovEnabled));
      props.setProperty("customFov", String.valueOf(customFov));
      props.setProperty("isParticlesEnabled", String.valueOf(isParticlesEnabled));
      props.setProperty("isExplosionParticlesEnabled", String.valueOf(isExplosionParticlesEnabled));
      props.setProperty("isPotionParticlesEnabled", String.valueOf(isPotionParticlesEnabled));
      props.setProperty("isCriticalParticlesEnabled", String.valueOf(isCriticalParticlesEnabled));
      props.setProperty("isDamageParticlesEnabled", String.valueOf(isDamageParticlesEnabled));
      props.setProperty("isChatHeadsEnabled", String.valueOf(isChatHeadsEnabled));
      props.setProperty("isNotificationsEnabled", String.valueOf(isNotificationsEnabled));
      props.setProperty("isAppleSkinEnabled", String.valueOf(isAppleSkinEnabled));
      props.setProperty("isNotificationsAnimEnabled", String.valueOf(isNotificationsAnimEnabled));
      props.setProperty("guiAnimationDurationMs", String.valueOf(guiAnimationDurationMs));
      props.setProperty("clickGuiScale", String.valueOf(clickGuiScale));
      props.setProperty("guiType", String.valueOf(guiType));
      props.setProperty("notificationsAnimDirection", String.valueOf(notificationsAnimDirection));
      props.setProperty("notificationsX", String.valueOf(notificationsX));
      props.setProperty("notificationsY", String.valueOf(notificationsY));
      props.setProperty("notificationsScale", String.valueOf(notificationsScale));
      props.setProperty("isSpeedDisplayEnabled", String.valueOf(isSpeedDisplayEnabled));
      props.setProperty("speedDisplayX", String.valueOf(speedDisplayX));
      props.setProperty("speedDisplayY", String.valueOf(speedDisplayY));
      props.setProperty("speedDisplayScale", String.valueOf(speedDisplayScale));
      props.setProperty("isBiomeDisplayEnabled", String.valueOf(isBiomeDisplayEnabled));
      props.setProperty("biomeDisplayX", String.valueOf(biomeDisplayX));
      props.setProperty("biomeDisplayY", String.valueOf(biomeDisplayY));
      props.setProperty("biomeDisplayScale", String.valueOf(biomeDisplayScale));
      props.setProperty("isEntityCounterEnabled", String.valueOf(isEntityCounterEnabled));
      props.setProperty("entityCounterX", String.valueOf(entityCounterX));
      props.setProperty("entityCounterY", String.valueOf(entityCounterY));
      props.setProperty("entityCounterScale", String.valueOf(entityCounterScale));
      props.setProperty("entityCounterCurrentChunkOnly", String.valueOf(entityCounterCurrentChunkOnly));
      props.setProperty("entityCounterShowPlayers", String.valueOf(entityCounterShowPlayers));
      props.setProperty("entityCounterShowHostile", String.valueOf(entityCounterShowHostile));
      props.setProperty("entityCounterShowAnimals", String.valueOf(entityCounterShowAnimals));
      props.setProperty("entityCounterShowItems", String.valueOf(entityCounterShowItems));
      props.setProperty("entityCounterShowTotal", String.valueOf(entityCounterShowTotal));
      props.setProperty("isIrlClockEnabled", String.valueOf(isIrlClockEnabled));
      props.setProperty("isIrlDateEnabled", String.valueOf(isIrlDateEnabled));
      props.setProperty("isIrlClock24Hour", String.valueOf(isIrlClock24Hour));
      props.setProperty("irlClockX", String.valueOf(irlClockX));
      props.setProperty("irlClockY", String.valueOf(irlClockY));
      props.setProperty("irlClockScale", String.valueOf(irlClockScale));
      props.setProperty("isTotemCounterEnabled", String.valueOf(isTotemCounterEnabled));
      props.setProperty("totemCounterX", String.valueOf(totemCounterX));
      props.setProperty("totemCounterY", String.valueOf(totemCounterY));
      props.setProperty("totemCounterScale", String.valueOf(totemCounterScale));
      props.setProperty("isArrayListEnabled", String.valueOf(isArrayListEnabled));
      props.setProperty("arrayListSortMode", String.valueOf(arrayListSortMode));
      props.setProperty("arrayListX", String.valueOf(arrayListX));
      props.setProperty("arrayListY", String.valueOf(arrayListY));
      props.setProperty("arrayListScale", String.valueOf(arrayListScale));
      props.setProperty("arrayListBackground", String.valueOf(arrayListBackground));
      props.setProperty("isArrowCounterEnabled", String.valueOf(isArrowCounterEnabled));
      props.setProperty("arrowCounterX", String.valueOf(arrowCounterX));
      props.setProperty("arrowCounterY", String.valueOf(arrowCounterY));
      props.setProperty("arrowCounterScale", String.valueOf(arrowCounterScale));
      props.setProperty("isReachDisplayEnabled", String.valueOf(isReachDisplayEnabled));
      props.setProperty("reachDisplayX", String.valueOf(reachDisplayX));
      props.setProperty("reachDisplayY", String.valueOf(reachDisplayY));
      props.setProperty("reachDisplayScale", String.valueOf(reachDisplayScale));
      props.setProperty("isServerInfoEnabled", String.valueOf(isServerInfoEnabled));
      props.setProperty("serverInfoX", String.valueOf(serverInfoX));
      props.setProperty("serverInfoY", String.valueOf(serverInfoY));
      props.setProperty("serverInfoScale", String.valueOf(serverInfoScale));
      props.setProperty("isPackDisplayEnabled", String.valueOf(isPackDisplayEnabled));
      props.setProperty("packDisplayX", String.valueOf(packDisplayX));
      props.setProperty("packDisplayY", String.valueOf(packDisplayY));
      props.setProperty("packDisplayScale", String.valueOf(packDisplayScale));
      props.setProperty("isTimeChangerEnabled", String.valueOf(isTimeChangerEnabled));
      props.setProperty("customTime", String.valueOf(customTime));

      for (Entry<String, Integer> entry : moduleKeybinds.entrySet()) {
         props.setProperty("keybind_" + entry.getKey(), String.valueOf(entry.getValue()));
      }

      props.setProperty("freelookKey", String.valueOf(freelookKey));
      props.setProperty("freelookToggleMode", String.valueOf(freelookToggleMode));
      props.setProperty("freelookShowOwnNametag", String.valueOf(freelookShowOwnNametag));
      props.setProperty("zoomKey", String.valueOf(zoomKey));
      props.setProperty("zoomToggleMode", String.valueOf(zoomToggleMode));
      props.setProperty("zoomLevel", String.valueOf(zoomLevel));
      props.setProperty("zoomScrollSensitivity", String.valueOf(zoomScrollSensitivity));
      props.setProperty("compassX", String.valueOf(compassX));
      props.setProperty("compassY", String.valueOf(compassY));
      props.setProperty("compassScale", String.valueOf(compassScale));
      props.setProperty("isArmorStatusEnabled", String.valueOf(isArmorStatusEnabled));
      props.setProperty("armorStatusScale", String.valueOf(armorStatusScale));
      props.setProperty("allTimeKills", String.valueOf(allTimeKills));
      props.setProperty("allTimeDeaths", String.valueOf(allTimeDeaths));
      props.setProperty("allTimeCrystalDeaths", String.valueOf(allTimeCrystalDeaths));
      props.setProperty("allTimePops", String.valueOf(allTimePops));
      props.setProperty("allTimeCrystalKills", String.valueOf(allTimeCrystalKills));
      props.setProperty("allTimeAnchorKills", String.valueOf(allTimeAnchorKills));
      props.setProperty("allTimeClicks", String.valueOf(allTimeClicks));
      props.setProperty("allTimeHits", String.valueOf(allTimeHits));
      props.setProperty("allTimeDamage", String.valueOf(allTimeDamage));
      props.setProperty("allTimeBlocksBroken", String.valueOf(allTimeBlocksBroken));
      props.setProperty("allTimeBlocksPlaced", String.valueOf(allTimeBlocksPlaced));
      props.setProperty("allTimeCrystalsPlaced", String.valueOf(allTimeCrystalsPlaced));
      props.setProperty("allTimeCrystalsBroken", String.valueOf(allTimeCrystalsBroken));
      props.setProperty("allTimeAnchorsPlaced", String.valueOf(allTimeAnchorsPlaced));
      props.setProperty("allTimeAnchorsBlown", String.valueOf(allTimeAnchorsBlown));
      props.setProperty("allTimeAnchorsCharged", String.valueOf(allTimeAnchorsCharged));
      props.setProperty("allTimePlayTimeTicks", String.valueOf(allTimePlayTimeTicks));
      props.setProperty("sessionBlocksBroken", String.valueOf(sessionBlocksBroken));
      props.setProperty("sessionBlocksPlaced", String.valueOf(sessionBlocksPlaced));
      props.setProperty("sessionCrystalsPlaced", String.valueOf(sessionCrystalsPlaced));
      props.setProperty("sessionCrystalsBroken", String.valueOf(sessionCrystalsBroken));
      props.setProperty("sessionAnchorsPlaced", String.valueOf(sessionAnchorsPlaced));
      props.setProperty("sessionAnchorsBlown", String.valueOf(sessionAnchorsBlown));
      props.setProperty("sessionAnchorsCharged", String.valueOf(sessionAnchorsCharged));
      props.setProperty("sessionPlayTimeTicks", String.valueOf(sessionPlayTimeTicks));
      props.setProperty("isCustomHitboxEnabled", String.valueOf(isCustomHitboxEnabled));
      props.setProperty("isCustomHitboxDynamic", String.valueOf(isCustomHitboxDynamic));
      props.setProperty("hitboxRed", String.valueOf(hitboxRed));
      props.setProperty("hitboxGreen", String.valueOf(hitboxGreen));
      props.setProperty("hitboxBlue", String.valueOf(hitboxBlue));
      props.setProperty("hitboxThickness", String.valueOf(hitboxThickness));
      props.setProperty("hitboxShowCrystal", String.valueOf(hitboxShowCrystal));
      props.setProperty("crystalHitboxRed", String.valueOf(crystalHitboxRed));
      props.setProperty("crystalHitboxGreen", String.valueOf(crystalHitboxGreen));
      props.setProperty("crystalHitboxBlue", String.valueOf(crystalHitboxBlue));
      props.setProperty("isDamageHeartsEnabled", String.valueOf(isDamageHeartsEnabled));
      props.setProperty("isHealthIndicatorsEnabled", String.valueOf(isHealthIndicatorsEnabled));
      props.setProperty("healthIndicatorsMode", String.valueOf(healthIndicatorsMode));
      props.setProperty("healthIndicatorsType", String.valueOf(healthIndicatorsType));
      props.setProperty("healthIndicatorsFuseAbsorption", String.valueOf(healthIndicatorsFuseAbsorption));
      props.setProperty("healthIndicatorsRange", String.valueOf(healthIndicatorsRange));
      props.setProperty("targetIndicatorMaxReach", String.valueOf(targetIndicatorMaxReach));
      props.setProperty("targetIndicatorSpinning", String.valueOf(targetIndicatorSpinning));
      props.setProperty("targetIndicatorSpinSpeed", String.valueOf(targetIndicatorSpinSpeed));
      props.setProperty("targetIndicatorHitReachOnly", String.valueOf(targetIndicatorHitReachOnly));
      props.setProperty("attackIndicatorEnabled", String.valueOf(attackIndicatorEnabled));
      props.setProperty("attackIndicatorOnlyPlayers", String.valueOf(attackIndicatorOnlyPlayers));
      props.setProperty("attackIndicatorAllowNonLiving", String.valueOf(attackIndicatorAllowNonLiving));
      props.setProperty("antiBot", String.valueOf(antiBot));
      props.setProperty("attackIndicatorAlwaysActive", String.valueOf(attackIndicatorAlwaysActive));
      props.setProperty("attackIndicatorStyle", attackIndicatorStyle);
      props.setProperty("attackIndicatorLiveTime", String.valueOf(attackIndicatorLiveTime));
      props.setProperty("isAttackHudEnabled", String.valueOf(isAttackHudEnabled));
      props.setProperty("attackHudMode", String.valueOf(attackHudMode));
      props.setProperty("attackHudSize", String.valueOf(attackHudSize));
      props.setProperty("attackHudX", String.valueOf(attackHudX));
      props.setProperty("attackHudY", String.valueOf(attackHudY));
      props.setProperty("attackIndicatorLegacyTexture", attackIndicatorLegacyTexture);
      props.setProperty("attackIndicatorLegacyRollSpeed", String.valueOf(attackIndicatorLegacyRollSpeed));
      props.setProperty("attackIndicatorLegacyScale", String.valueOf(attackIndicatorLegacyScale));
      props.setProperty("attackIndicatorLegacyAlpha", String.valueOf(attackIndicatorLegacyAlpha));
      props.setProperty("attackIndicatorSoulStyle", attackIndicatorSoulStyle);
      props.setProperty("attackIndicatorSoulTexture", attackIndicatorSoulTexture);
      props.setProperty("attackIndicatorSoulLength", String.valueOf(attackIndicatorSoulLength));
      props.setProperty("attackIndicatorSoulFactor", String.valueOf(attackIndicatorSoulFactor));
      props.setProperty("attackIndicatorSoulShaking", String.valueOf(attackIndicatorSoulShaking));
      props.setProperty("attackIndicatorSoulAmplitude", String.valueOf(attackIndicatorSoulAmplitude));
      props.setProperty("attackIndicatorSoulRadius", String.valueOf(attackIndicatorSoulRadius));
      props.setProperty("attackIndicatorSoulStartSize", String.valueOf(attackIndicatorSoulStartSize));
      props.setProperty("attackIndicatorSoulEndSize", String.valueOf(attackIndicatorSoulEndSize));
      props.setProperty("attackIndicatorSoulScale", String.valueOf(attackIndicatorSoulScale));
      props.setProperty("attackIndicatorSoulSubdivision", String.valueOf(attackIndicatorSoulSubdivision));
      props.setProperty("attackIndicatorTopkaRadius", String.valueOf(attackIndicatorTopkaRadius));
      props.setProperty("attackIndicatorTopkaSpeed", String.valueOf(attackIndicatorTopkaSpeed));
      props.setProperty("damageHeartsShowPlayers", String.valueOf(DamageHeartsModule.showPlayers));
      props.setProperty("damageHeartsShowHostile", String.valueOf(DamageHeartsModule.showHostile));
      props.setProperty("damageHeartsShowPassive", String.valueOf(DamageHeartsModule.showPassive));
      props.setProperty("damageHeartsShowAnimals", String.valueOf(DamageHeartsModule.showAnimals));
      props.setProperty("damageHeartsShowBosses", String.valueOf(DamageHeartsModule.showBosses));
      props.setProperty("damageHeartsHeartScale", String.valueOf(DamageHeartsModule.heartScale));
      props.setProperty("damageHeartsFadeTime", String.valueOf(DamageHeartsModule.fadeTime));
      props.setProperty("damageHeartsLifetime", String.valueOf(DamageHeartsModule.lifetime));
      props.setProperty("damageHeartsGlowEnabled", String.valueOf(DamageHeartsModule.glowEnabled));
      props.setProperty("damageHeartsCritGlowEnabled", String.valueOf(DamageHeartsModule.critGlowEnabled));
      props.setProperty("damageHeartsRainbowMode", String.valueOf(DamageHeartsModule.rainbowMode));
      props.setProperty("damageHeartsShowNumeric", String.valueOf(DamageHeartsModule.showNumeric));
      props.setProperty("damageHeartsDistanceScaling", String.valueOf(DamageHeartsModule.distanceScaling));
      props.setProperty("damageHeartsRotationSpeed", String.valueOf(DamageHeartsModule.rotationSpeed));
      props.setProperty("damageHeartsMovementSpeed", String.valueOf(DamageHeartsModule.movementSpeed));
      props.setProperty("damageHeartsRenderDistance", String.valueOf(DamageHeartsModule.renderDistance));
      props.setProperty("damageHeartsCombineDelayMs", String.valueOf(DamageHeartsModule.combineDelayMs));
      props.setProperty("damageHeartsGoldenHeartsMode", String.valueOf(DamageHeartsModule.goldenHeartsMode));
      props.setProperty("armorStatusX", String.valueOf(armorStatusX));
      props.setProperty("armorStatusY", String.valueOf(armorStatusY));
      props.setProperty("armorStatusDisplayMode", String.valueOf(armorStatusDisplayMode));
      props.setProperty("armorStatusShowDurabilityBar", String.valueOf(armorStatusShowDurabilityBar));
      props.setProperty("armorStatusDurabilityBarColor", String.valueOf(armorStatusDurabilityBarColor));
      props.setProperty("armorStatusShowEmptySlots", String.valueOf(armorStatusShowEmptySlots));
      props.setProperty("armorStatusOrientation", String.valueOf(armorStatusOrientation));
      props.setProperty("armorStatusWarningThreshold", String.valueOf(armorStatusWarningThreshold));
      props.setProperty("armorStatusWarningSound", String.valueOf(armorStatusWarningSound));
      props.setProperty("armorStatusWarningSoundType", String.valueOf(armorStatusWarningSoundType));
      props.setProperty("armorStatusTransparentBg", String.valueOf(armorStatusTransparentBg));
      props.setProperty("armorStatusWarningSoundFile", armorStatusWarningSoundFile);
      props.setProperty("lowHealthAlarmSoundFile", lowHealthAlarmSoundFile);
      props.setProperty("potWarnSoundFile", potWarnSoundFile);
      props.setProperty("zoomSmoothAnimation", String.valueOf(zoomSmoothAnimation));
      props.setProperty("zoomAnimationSpeed", String.valueOf(zoomAnimationSpeed));
      props.setProperty("editGuiKey", String.valueOf(editGuiKey));
      props.setProperty("isTargetHudEnabled", String.valueOf(isTargetHudEnabled));
      props.setProperty("targetHudX", String.valueOf(targetHudX));
      props.setProperty("targetHudY", String.valueOf(targetHudY));
      props.setProperty("targetHudScale", String.valueOf(targetHudScale));
      props.setProperty("targetHudMmoArmorDisplay", String.valueOf(targetHudMmoArmorDisplay));
      props.setProperty("targetHudMmoArmorX", String.valueOf(targetHudMmoArmorX));
      props.setProperty("targetHudMmoArmorY", String.valueOf(targetHudMmoArmorY));
      props.setProperty("targetHudMmoArmorScale", String.valueOf(targetHudMmoArmorScale));
      props.setProperty("targetHudResetKeybind", String.valueOf(targetHudResetKeybind));
      props.setProperty("isStatsHudEnabled", String.valueOf(isStatsHudEnabled));
      props.setProperty("statsHudX", String.valueOf(statsHudX));
      props.setProperty("statsHudY", String.valueOf(statsHudY));
      props.setProperty("statsHudScale", String.valueOf(statsHudScale));
      props.setProperty("statsHudShowKills", String.valueOf(statsHudShowKills));
      props.setProperty("statsHudShowDeaths", String.valueOf(statsHudShowDeaths));
      props.setProperty("statsHudShowKD", String.valueOf(statsHudShowKD));
      props.setProperty("statsHudShowPops", String.valueOf(statsHudShowPops));
      props.setProperty("statsHudShowClicks", String.valueOf(statsHudShowClicks));
      props.setProperty("statsHudShowHits", String.valueOf(statsHudShowHits));
      props.setProperty("statsHudShowAttributeSwap", String.valueOf(statsHudShowAttributeSwap));
      props.setProperty("statsHudShowAccuracy", String.valueOf(statsHudShowAccuracy));
      props.setProperty("statsHudShowDamage", String.valueOf(statsHudShowDamage));
      props.setProperty("statsHudShowBlocksBroken", String.valueOf(statsHudShowBlocksBroken));
      props.setProperty("statsHudShowBlocksPlaced", String.valueOf(statsHudShowBlocksPlaced));
      props.setProperty("statsHudShowCrystalsPlaced", String.valueOf(statsHudShowCrystalsPlaced));
      props.setProperty("statsHudShowCrystalsBroken", String.valueOf(statsHudShowCrystalsBroken));
      props.setProperty("statsHudShowAnchorsPlaced", String.valueOf(statsHudShowAnchorsPlaced));
      props.setProperty("statsHudShowAnchorsBlown", String.valueOf(statsHudShowAnchorsBlown));
      props.setProperty("statsHudShowAnchorsCharged", String.valueOf(statsHudShowAnchorsCharged));
      props.setProperty("statsHudShowCrystalKills", String.valueOf(statsHudShowCrystalKills));
      props.setProperty("statsHudShowCrystalDeaths", String.valueOf(statsHudShowCrystalDeaths));
      props.setProperty("statsHudShowAnchors", String.valueOf(statsHudShowAnchors));
      props.setProperty("statsHudShowTime", String.valueOf(statsHudShowTime));
      props.setProperty("statsHudStatMode", String.valueOf(statsHudStatMode));
      props.setProperty("statsHudShowInMultiplayer", String.valueOf(statsHudShowInMultiplayer));
      props.setProperty("targetHudShowArmor", String.valueOf(targetHudShowArmor));
      props.setProperty("targetHudShowArmorDurability", String.valueOf(targetHudShowArmorDurability));
      props.setProperty("targetHudShowEnemyCrucials", String.valueOf(targetHudShowEnemyCrucials));
      props.setProperty("targetHudShowEnemyPearls", String.valueOf(targetHudShowEnemyPearls));
      props.setProperty("targetHudShowEnemyGapples", String.valueOf(targetHudShowEnemyGapples));
      props.setProperty("targetHudShowEnemyCobwebs", String.valueOf(targetHudShowEnemyCobwebs));
      props.setProperty("targetHudShowEnemyTotems", String.valueOf(targetHudShowEnemyTotems));
      props.setProperty("targetHudShowEnemyPops", String.valueOf(targetHudShowEnemyPops));
      props.setProperty("targetHudShowEnemyWindCharges", String.valueOf(targetHudShowEnemyWindCharges));
      props.setProperty("targetHudShowEnemyWindChargesUsed", String.valueOf(targetHudShowEnemyWindChargesUsed));
      props.setProperty("targetHudShowEnemyHealthPots", String.valueOf(targetHudShowEnemyHealthPots));
      props.setProperty("targetHudStickyEnabled", String.valueOf(targetHudStickyEnabled));
      props.setProperty("targetHudStickyDuration", String.valueOf(targetHudStickyDuration));
      props.setProperty("targetHudAboveHead", String.valueOf(targetHudAboveHead));
      props.setProperty("targetHudAboveHeadScale", String.valueOf(targetHudAboveHeadScale));
      props.setProperty("enemyCrucialsHudX", String.valueOf(enemyCrucialsHudX));
      props.setProperty("enemyCrucialsHudY", String.valueOf(enemyCrucialsHudY));
      props.setProperty("enemyCrucialsHudScale", String.valueOf(enemyCrucialsHudScale));
      props.setProperty("isPotionStatusEnabled", String.valueOf(isPotionStatusEnabled));
      props.setProperty("potionStatusX", String.valueOf(potionStatusX));
      props.setProperty("potionStatusY", String.valueOf(potionStatusY));
      props.setProperty("potionStatusScale", String.valueOf(potionStatusScale));
      props.setProperty("potionStatusOrientation", String.valueOf(potionStatusOrientation));
      props.setProperty("potionStatusShowDuration", String.valueOf(potionStatusShowDuration));
      props.setProperty("potionStatusShowAmplifier", String.valueOf(potionStatusShowAmplifier));
      props.setProperty("potionStatusIconType", String.valueOf(potionStatusIconType));
      props.setProperty("isBlockIndicatorEnabled", String.valueOf(isBlockIndicatorEnabled));
      props.setProperty("blockIndicatorX", String.valueOf(blockIndicatorX));
      props.setProperty("blockIndicatorY", String.valueOf(blockIndicatorY));
      props.setProperty("blockIndicatorScale", String.valueOf(blockIndicatorScale));
      BlockIndicatorManager.save(props);
      props.setProperty("isCustomCrosshairEnabled", String.valueOf(isCustomCrosshairEnabled));
      props.setProperty("crosshairStyle", String.valueOf(crosshairStyle));
      props.setProperty("crosshairRed", String.valueOf(crosshairRed));
      props.setProperty("crosshairGreen", String.valueOf(crosshairGreen));
      props.setProperty("crosshairBlue", String.valueOf(crosshairBlue));
      props.setProperty("crosshairAlpha", String.valueOf(crosshairAlpha));
      props.setProperty("crosshairSize", String.valueOf(crosshairSize));
      props.setProperty("crosshairThickness", String.valueOf(crosshairThickness));
      props.setProperty("crosshairDot", String.valueOf(crosshairDot));
      props.setProperty("isCustomF3Enabled", String.valueOf(isCustomF3Enabled));
      CustomF3Manager.save(props);
      DeathInfoManager.save(props);
      props.setProperty("isDeathInfoEnabled", String.valueOf(isDeathInfoEnabled));
      props.setProperty("deathInfoShowOnScreen", String.valueOf(deathInfoShowOnScreen));
      props.setProperty("deathInfoX", String.valueOf(deathInfoX));
      props.setProperty("deathInfoY", String.valueOf(deathInfoY));
      props.setProperty("deathInfoScale", String.valueOf(deathInfoScale));
      props.setProperty("lastDeathX", String.valueOf(lastDeathX));
      props.setProperty("lastDeathY", String.valueOf(lastDeathY));
      props.setProperty("lastDeathZ", String.valueOf(lastDeathZ));
      props.setProperty("lastDeathDimension", lastDeathDimension);
      props.setProperty("lastDeathTime", String.valueOf(lastDeathTime));
      props.setProperty("hasDeathInfo", String.valueOf(hasDeathInfo));
      props.setProperty("isWaypointsEnabled", String.valueOf(isWaypointsEnabled));
      props.setProperty("isTpsDisplayEnabled", String.valueOf(isTpsDisplayEnabled));
      props.setProperty("tpsDisplayX", String.valueOf(tpsDisplayX));
      props.setProperty("tpsDisplayY", String.valueOf(tpsDisplayY));
      props.setProperty("tpsDisplayScale", String.valueOf(tpsDisplayScale));
      props.setProperty("isSystemResourcesEnabled", String.valueOf(isSystemResourcesEnabled));
      props.setProperty("systemResourcesX", String.valueOf(systemResourcesX));
      props.setProperty("systemResourcesY", String.valueOf(systemResourcesY));
      props.setProperty("systemResourcesScale", String.valueOf(systemResourcesScale));
      props.setProperty("isStopwatchEnabled", String.valueOf(isStopwatchEnabled));
      props.setProperty("stopwatchRunning", String.valueOf(stopwatchRunning));
      props.setProperty("stopwatchKey", String.valueOf(stopwatchKey));
      props.setProperty("stopwatchX", String.valueOf(stopwatchX));
      props.setProperty("stopwatchY", String.valueOf(stopwatchY));
      props.setProperty("stopwatchScale", String.valueOf(stopwatchScale));
      props.setProperty("stopwatchBackground", String.valueOf(stopwatchBackground));
      props.setProperty("isTextHudEnabled", String.valueOf(isTextHudEnabled));
      props.setProperty("textHudString", textHudString != null ? textHudString : "Custom Text");
      props.setProperty("textHudTextColor", String.valueOf(textHudTextColor));
      props.setProperty("textHudBgColor", String.valueOf(textHudBgColor));
      props.setProperty("textHudBgAlpha", String.valueOf(textHudBgAlpha));
      props.setProperty("textHudX", String.valueOf(textHudX));
      props.setProperty("textHudY", String.valueOf(textHudY));
      props.setProperty("textHudScale", String.valueOf(textHudScale));
      props.setProperty("isWatermarkEnabled", String.valueOf(isWatermarkEnabled));
      props.setProperty("watermarkX", String.valueOf(watermarkX));
      props.setProperty("watermarkY", String.valueOf(watermarkY));
      props.setProperty("watermarkScale", String.valueOf(watermarkScale));
      props.setProperty("watermarkAnimatedColor", String.valueOf(watermarkAnimatedColor));
      props.setProperty("watermarkUseThemeColor", String.valueOf(watermarkUseThemeColor));
      props.setProperty("watermarkCustomColor", String.valueOf(watermarkCustomColor));
      props.setProperty("isMapTransformationEnabled", String.valueOf(isMapTransformationEnabled));
      props.setProperty("isMapHoverPreviewEnabled", String.valueOf(isMapHoverPreviewEnabled));
      props.setProperty("isKeybindsDisplayEnabled", String.valueOf(isKeybindsDisplayEnabled));
      props.setProperty("keybindsHudX", String.valueOf(keybindsHudX));
      props.setProperty("keybindsHudY", String.valueOf(keybindsHudY));
      props.setProperty("keybindsHudScale", String.valueOf(keybindsHudScale));
      props.setProperty("isArrayListEnabled", String.valueOf(isArrayListEnabled));
      props.setProperty("arrayListX", String.valueOf(arrayListX));
      props.setProperty("arrayListY", String.valueOf(arrayListY));
      props.setProperty("arrayListScale", String.valueOf(arrayListScale));
      props.setProperty("arrayListSortMode", String.valueOf(arrayListSortMode));
      props.setProperty("arrayListBackground", String.valueOf(arrayListBackground));
      props.setProperty("isFpsGraphEnabled", String.valueOf(isFpsGraphEnabled));
      props.setProperty("fpsGraphX", String.valueOf(fpsGraphX));
      props.setProperty("fpsGraphY", String.valueOf(fpsGraphY));
      props.setProperty("fpsGraphScale", String.valueOf(fpsGraphScale));
      props.setProperty("showFpsAvg", String.valueOf(showFpsAvg));
      props.setProperty("showFpsMinMax", String.valueOf(showFpsMinMax));
      props.setProperty("showFps1Percent", String.valueOf(showFps1Percent));
      props.setProperty("showFps01Percent", String.valueOf(showFps01Percent));
      props.setProperty("isTotemTraceEnabled", String.valueOf(isTotemTraceEnabled));
      props.setProperty("totemTraceDuration", String.valueOf(totemTraceDuration));
      props.setProperty("totemTraceColorRed", String.valueOf(totemTraceColorRed));
      props.setProperty("totemTraceColorGreen", String.valueOf(totemTraceColorGreen));
      props.setProperty("totemTraceColorBlue", String.valueOf(totemTraceColorBlue));
      props.setProperty("totemTraceColorAlpha", String.valueOf(totemTraceColorAlpha));
      props.setProperty("totemTraceShowColor", String.valueOf(totemTraceShowColor));
      props.setProperty("totemTraceShowArmor", String.valueOf(totemTraceShowArmor));
      props.setProperty("totemTraceDetectSelf", String.valueOf(totemTraceDetectSelf));
      props.setProperty("totemTraceDetectOthers", String.valueOf(totemTraceDetectOthers));
      props.setProperty("totemPopHealthThreshold", String.valueOf(totemPopHealthThreshold));
      props.setProperty("isPlayerTrailEnabled", String.valueOf(isPlayerTrailEnabled));
      props.setProperty("playerTrailShowSelf", String.valueOf(playerTrailShowSelf));
      props.setProperty("playerTrailShowOthers", String.valueOf(playerTrailShowOthers));
      props.setProperty("playerTrailColor", String.valueOf(playerTrailColor));
      props.setProperty("playerTrailColorR", String.valueOf(playerTrailColorR));
      props.setProperty("playerTrailColorG", String.valueOf(playerTrailColorG));
      props.setProperty("playerTrailColorB", String.valueOf(playerTrailColorB));
      props.setProperty("playerTrailDuration", String.valueOf(playerTrailDuration));
      props.setProperty("playerTrailAnchor", String.valueOf(playerTrailAnchor));
      StopwatchManager.save(props);
      props.setProperty("isTntTimerEnabled", String.valueOf(isTntTimerEnabled));
      props.setProperty("tntTimerMaxDistance", String.valueOf(TntTimerRenderer.maxDistance));
      props.setProperty("isBigHeadEnabled", String.valueOf(isBigHeadEnabled));
      props.setProperty("bigHeadSelf", String.valueOf(bigHeadSelf));
      props.setProperty("bigHeadFriends", String.valueOf(bigHeadFriends));
      props.setProperty("bigHeadOthers", String.valueOf(bigHeadOthers));
      props.setProperty("bigHeadScale", String.valueOf(bigHeadScale));
      props.setProperty("isBlockOverlayEnabled", String.valueOf(isBlockOverlayEnabled));
      props.setProperty("blockOverlayMode", String.valueOf(blockOverlayMode));
      props.setProperty("blockOverlayOutline", String.valueOf(blockOverlayOutline));
      props.setProperty("blockOverlayFill", String.valueOf(blockOverlayFill));
      props.setProperty("blockOverlayGlow", String.valueOf(blockOverlayGlow));
      props.setProperty("blockOverlayRed", String.valueOf(blockOverlayRed));
      props.setProperty("blockOverlayGreen", String.valueOf(blockOverlayGreen));
      props.setProperty("blockOverlayBlue", String.valueOf(blockOverlayBlue));
      props.setProperty("blockOverlayThickness", String.valueOf(blockOverlayThickness));
      props.setProperty("blockOverlayFillOpacity", String.valueOf(blockOverlayFillOpacity));
      props.setProperty("blockOverlayGlowStrength", String.valueOf(blockOverlayGlowStrength));
      props.setProperty("isChinaHatEnabled", String.valueOf(isChinaHatEnabled));
      props.setProperty("chinaHatRed", String.valueOf(chinaHatRed));
      props.setProperty("chinaHatGreen", String.valueOf(chinaHatGreen));
      props.setProperty("chinaHatBlue", String.valueOf(chinaHatBlue));
      props.setProperty("chinaHatFilled", String.valueOf(chinaHatFilled));
      props.setProperty("chinaHatSize", String.valueOf(chinaHatSize));
      props.setProperty("isInvHighlightEnabled", String.valueOf(isInvHighlightEnabled));
      props.setProperty("invHighlightItems", invHighlightItems != null ? invHighlightItems : "minecraft:totem_of_undying");
      props.setProperty("invHighlightRed", String.valueOf(invHighlightRed));
      props.setProperty("invHighlightGreen", String.valueOf(invHighlightGreen));
      props.setProperty("invHighlightBlue", String.valueOf(invHighlightBlue));
      props.setProperty("isMouseStrokesEnabled", String.valueOf(isMouseStrokesEnabled));
      props.setProperty("mouseStrokesX", String.valueOf(mouseStrokesX));
      props.setProperty("mouseStrokesY", String.valueOf(mouseStrokesY));
      props.setProperty("mouseStrokesScale", String.valueOf(mouseStrokesScale));
      props.setProperty("mouseStrokesShowCross", String.valueOf(mouseStrokesShowCross));
      props.setProperty("mouseStrokesSensitivity", String.valueOf(mouseStrokesSensitivity));
      props.setProperty("mouseStrokesSymbolRed", String.valueOf(mouseStrokesSymbolRed));
      props.setProperty("mouseStrokesSymbolGreen", String.valueOf(mouseStrokesSymbolGreen));
      props.setProperty("mouseStrokesSymbolBlue", String.valueOf(mouseStrokesSymbolBlue));
      props.setProperty("mouseStrokesBoxRed", String.valueOf(mouseStrokesBoxRed));
      props.setProperty("mouseStrokesBoxGreen", String.valueOf(mouseStrokesBoxGreen));
      props.setProperty("mouseStrokesBoxBlue", String.valueOf(mouseStrokesBoxBlue));
      props.setProperty("isTransparentShieldEnabled", String.valueOf(isTransparentShieldEnabled));
      props.setProperty("transparentShieldX", String.valueOf(transparentShieldX));
      props.setProperty("transparentShieldY", String.valueOf(transparentShieldY));
      props.setProperty("transparentShieldScale", String.valueOf(transparentShieldScale));
      props.setProperty("transparentShieldStatusColor", String.valueOf(transparentShieldStatusColor));
      props.setProperty("transparentShieldStatusOutlineColor", String.valueOf(transparentShieldStatusOutlineColor));
      props.setProperty("isLogoutSpotsEnabled", String.valueOf(isLogoutSpotsEnabled));
      props.setProperty("logoutSpotsDetectDisappears", String.valueOf(logoutSpotsDetectDisappears));
      props.setProperty("logoutSpotsLeaveMessages", String.valueOf(logoutSpotsLeaveMessages));
      props.setProperty("logoutSpotsIgnoreFriends", String.valueOf(logoutSpotsIgnoreFriends));
      props.setProperty("logoutSpotsCreateWaypoints", String.valueOf(logoutSpotsCreateWaypoints));
      props.setProperty("logoutSpotsSaveRotation", String.valueOf(logoutSpotsSaveRotation));
      props.setProperty("logoutSpotsSaveHeldItem", String.valueOf(logoutSpotsSaveHeldItem));
      props.setProperty("primaryColorR", String.valueOf(primaryColorR));
      props.setProperty("primaryColorG", String.valueOf(primaryColorG));
      props.setProperty("primaryColorB", String.valueOf(primaryColorB));
      props.setProperty("secondaryColorR", String.valueOf(secondaryColorR));
      props.setProperty("secondaryColorG", String.valueOf(secondaryColorG));
      props.setProperty("secondaryColorB", String.valueOf(secondaryColorB));
      props.setProperty("rgbChromaEnabled", String.valueOf(rgbChromaEnabled));
      props.setProperty("colorTheme", colorTheme);
      props.setProperty("rgbSpeed", String.valueOf(rgbSpeed));
      props.setProperty("hudFpsLimit", String.valueOf(hudFpsLimit));
      props.setProperty("friendGreenNameTagsEnabled", String.valueOf(isFriendGreenNameTagsEnabled));
      props.setProperty("hideInvisNametags", String.valueOf(hideInvisNametags));
      props.setProperty("safeModeEnabled", String.valueOf(safeModeEnabled));
      props.setProperty("nameTagItemsEnabled", String.valueOf(isNameTagItemsEnabled));
      props.setProperty("nameTagItemsDisplayMode", String.valueOf(nameTagItemsDisplayMode));
      props.setProperty("nameTagItemsGreyWhenUsing", String.valueOf(nameTagItemsGreyWhenUsing));
      props.setProperty("nameTagItemsShowMainHand", String.valueOf(nameTagItemsShowMainHand));
      props.setProperty("nameTagItemsShowOffHand", String.valueOf(nameTagItemsShowOffHand));
      props.setProperty("nameTagItemsShowTotemPop", String.valueOf(nameTagItemsShowTotemPop));
      props.setProperty("nameTagItemsShowArmor", String.valueOf(nameTagItemsShowArmor));
      props.setProperty("nameTagItemsShowDurability", String.valueOf(nameTagItemsShowDurability));
      props.setProperty("nameTagItemsShowSelf", String.valueOf(nameTagItemsShowSelf));
      props.setProperty("nameTagItemsOnlyFriends", String.valueOf(nameTagItemsOnlyFriends));
      props.setProperty("nameTagItemsScale", String.valueOf(nameTagItemsScale));
      props.setProperty("friends", String.join(",", friends));

      try {
         CONFIG_FILE.getParentFile().mkdirs();

         try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            props.store(writer, "Void Cyan Client Config");
         }
      } catch (IOException var6) {
         var6.printStackTrace();
      }
   }

   public static PlayerEntity findTargetPlayer(int id, String name) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world == null) {
         return null;
      } else {
         return client.world.getEntityById(id) instanceof PlayerEntity player ? player : null;
      }
   }

   private static List<String> friendPatternSource = List.of();
   private static Pattern[] friendPatterns = new Pattern[0];

   /** True if any friend name appears as a whole word in {@code text}; patterns are rebuilt only when the friend list changes. */
   public static boolean mentionsFriend(String text) {
      if (!friends.equals(friendPatternSource)) {
         friendPatternSource = new ArrayList<>(friends);
         friendPatterns = friendPatternSource.stream().map(f -> Pattern.compile(".*\\b" + Pattern.quote(f) + "\\b.*")).toArray(Pattern[]::new);
      }
      for (Pattern pattern : friendPatterns) {
         if (pattern.matcher(text).matches()) {
            return true;
         }
      }
      return false;
   }

   public void onInitializeClient() {
      loadConfig();
      CritEffectsManager.reload();
      AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
         CritEffectsManager.onAttack(player, world, entity);
         return net.minecraft.util.ActionResult.PASS;
      });
      FriendManager.init();
      LogoutSpotsManager.init();
      NoteManager.loadNotes();
      StreakManager.init();
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientStarted)client -> {
         updateMainBackground();
         if (isCustomF3Enabled) {
            CustomF3Manager.syncAllToVanillaProfile(client);
         }
      });
      SoundEvent armorWarningSound = SoundEvent.of(Identifier.of("voidcyan", "armor_warning"));
      Registry.register(Registries.SOUND_EVENT, Identifier.of("voidcyan", "armor_warning"), armorWarningSound);
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> {
         sessionKills = 0;
         sessionDeaths = 0;
         sessionCrystalDeaths = 0;
         sessionPops = 0;
         sessionCrystalKills = 0;
         sessionAnchorKills = 0;
         sessionClicks = 0;
         sessionHits = 0;
         sessionDamage = 0.0;
         sessionBlocksBroken = 0;
         sessionBlocksPlaced = 0;
         sessionCrystalsPlaced = 0;
         sessionCrystalsBroken = 0;
         sessionAnchorsPlaced = 0;
         sessionAnchorsBlown = 0;
         sessionAnchorsCharged = 0;
         sessionPlayTimeTicks = 0L;
         entityHealthMap.clear();
         WaypointManager.onServerJoin(client, handler);
         if (isCustomF3Enabled) {
            CustomF3Manager.syncAllToVanillaProfile(client);
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> {
         WaypointManager.onServerDisconnect();
         StreakManager.onSessionEnd();
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
         WaypointManager.saveWaypoints();
         NoteManager.saveNotes();
         StreakManager.onSessionEnd();
         saveConfig();
      });
      // Hide vanilla's black block outline while Block Overlay is drawing its own.
      WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> !BlockOverlayRenderer.shouldReplaceVanilla(hitResult));
      WorldRenderEvents.AFTER_ENTITIES.register((AfterEntities)context -> {
         if (isBlockOverlayEnabled) {
            BlockOverlayRenderer.render(context);
         }

         if (isDamageHeartsEnabled) {
            HeartRenderer.renderAll3D(context);
         }

         if (isHealthIndicatorsEnabled) {
            HealthBarRenderer.renderAll3D(context);
            HealthHeartsRenderer.renderAll3D(context);
         }

         if (attackIndicatorEnabled) {
            AttackIndicator.renderTarget(context);
         }

         if (isCustomHitboxEnabled) {
            HitboxRenderer.renderAll(context);
         }

         // Crit FX models + billboard sparks render whenever the module is on.
         if (CritEffectsManager.enabled) {
            CritModelRenderer.render(context);
            CritBillboardFX.render(context);
         }

         if (isWaypointsEnabled) {
            WaypointRenderer.renderAll3D(context);
         }

         if (isTotemTraceEnabled) {
            TotemTraceManager.renderAll3D(context);
         }

         if (isPlayerTrailEnabled) {
            PlayerTrailManager.updateTrails();
            PlayerTrailManager.renderTrails(context);
         }

         if (isChinaHatEnabled) {
            ChinaHatRenderer.renderAll(context);
         }

         if (isLogoutSpotsEnabled) {
            LogoutSpotsManager.renderAll(context);
         }

         if ((isHitColorEnabled || isDamageColorEnabled || isTotemPopColorEnabled) && context.consumers() != null) {
            MinecraftClient mcClient = MinecraftClient.getInstance();
            if (mcClient.world != null) {
               VertexConsumer vc = context.consumers().getBuffer(RenderLayers.linesTranslucent());
               Vec3d camPos = mcClient.gameRenderer.getCamera().getCameraPos();
               long now = System.currentTimeMillis();

               for (Entity entity : mcClient.world.getEntities()) {
                  int eid = entity.getId();
                  int outlineArgb = 0;
                  if (shouldApplyTotemPopColor(eid)) {
                     int a = MathHelper.clamp(totemPopColorAlpha, 0, 255);
                     int r = MathHelper.clamp(totemPopColorRed, 0, 255);
                     int g = MathHelper.clamp(totemPopColorGreen, 0, 255);
                     int b = MathHelper.clamp(totemPopColorBlue, 0, 255);
                     outlineArgb = a << 24 | r << 16 | g << 8 | b;
                  } else if (shouldApplyDamageColor(eid)) {
                     int a = MathHelper.clamp(damageColorAlpha, 0, 255);
                     int r = MathHelper.clamp(damageColorRed, 0, 255);
                     int g = MathHelper.clamp(damageColorGreen, 0, 255);
                     int b = MathHelper.clamp(damageColorBlue, 0, 255);
                     outlineArgb = a << 24 | r << 16 | g << 8 | b;
                  } else if (shouldApplyHitColor(eid)) {
                     int a = MathHelper.clamp(hitColorAlpha, 0, 255);
                     int r = MathHelper.clamp(hitColorRed, 0, 255);
                     int g = MathHelper.clamp(hitColorGreen, 0, 255);
                     int b = MathHelper.clamp(hitColorBlue, 0, 255);
                     outlineArgb = a << 24 | r << 16 | g << 8 | b;
                  }

                  if (outlineArgb != 0) {
                     Box box = entity.getBoundingBox().offset(-camPos.x, -camPos.y, -camPos.z).expand(0.05);
                     VoxelShape shape = VoxelShapes.cuboid(box);
                     VertexRendering.drawOutline(context.matrices(), vc, shape, 0.0, 0.0, 0.0, outlineArgb, 2.5F);
                  }
               }
            }
         }
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         CritBillboardFX.tickSparks(1.0F);
         // Effects module: fire "death" trigger when the local player dies.
         if (client.player != null && client.player.isDead() && !wasLocalPlayerDeadLastTick) {
            CritEffectsManager.onDeath(client);
         }
         wasLocalPlayerDeadLastTick = client.player != null && client.player.isDead();
         if (client.player != null) {
            int currentSlot = client.player.getInventory().getSelectedSlot();
            if (currentSlot != lastSelectedSlot) {
               lastSelectedSlot = currentSlot;
               lastSlotSwapTime = System.currentTimeMillis();
            }
         }

         if (isFullbrightEnabled && client.player != null) {
            client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 255, false, false, false));
         } else if (!isFullbrightEnabled && client.player != null) {
            StatusEffectInstance effect = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (effect != null && effect.getAmplifier() == 255) {
               client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
         }

         WaypointManager.tickServerCheck(client);
         TargetHudRenderer.trackNearbyPlayers();
         StreakManager.tick();
         long window = MinecraftClient.getInstance().getWindow().getHandle();
         boolean guiKeyDown = GLFW.glfwGetKey(window, 91) == 1 || (editGuiKey > 0 && GLFW.glfwGetKey(window, editGuiKey) == 1);
         if (guiKeyDown && !guiKeyWasDown) {
            if (client.currentScreen == null) {
               client.setScreen(new EditHudScreen(null));
            } else if (client.currentScreen instanceof EditHudScreen) {
               if (guiType == 0) {
                  client.setScreen(new ClickGuiScreen(null));
               } else {
                  client.setScreen(new DropdownGuiScreen());
               }
            }
         }

         guiKeyWasDown = guiKeyDown;
         boolean dropdownKeyDown = GLFW.glfwGetKey(window, 92) == 1;
         if (dropdownKeyDown && !dropdownGuiKeyWasDown) {
            if (client.currentScreen instanceof DropdownGuiScreen) {
               client.setScreen(null);
            } else if (client.currentScreen == null) {
               client.setScreen(new DropdownGuiScreen());
            }
         }

         dropdownGuiKeyWasDown = dropdownKeyDown;
         if (client.currentScreen == null) {
            for (Entry<String, Integer> entry : moduleKeybinds.entrySet()) {
               String mod = entry.getKey();
               if (!mod.equals("Zoom") && !mod.equals("FreeLook")) {
                  int key = entry.getValue();
                  if (key != -1) {
                     boolean isDown;
                     if (key >= 0 && key <= 7) {
                        isDown = GLFW.glfwGetMouseButton(window, key) == 1;
                     } else {
                        if (key < 32) {
                           continue;
                        }

                        isDown = GLFW.glfwGetKey(window, key) == 1;
                     }

                     boolean wasDown = moduleKeybindsWasPressed.getOrDefault(entry.getKey(), false);
                     if (isDown && !wasDown) {
                        toggleModule(entry.getKey());
                     }

                     moduleKeybindsWasPressed.put(entry.getKey(), isDown);
                  }
               }
            }
         }

         if (client.currentScreen == null && isComboCounterEnabled && comboResetKeybind != -1) {
            boolean isPressed = GLFW.glfwGetKey(window, comboResetKeybind) == 1;
            if (isPressed && !comboResetKeyWasPressed) {
               currentCombo = 0;
               lastComboHitTime = 0L;
            }

            comboResetKeyWasPressed = isPressed;
         }

         if (client.currentScreen == null && isTargetHudEnabled && targetHudResetKeybind != -1) {
            boolean isPressed = GLFW.glfwGetKey(window, targetHudResetKeybind) == 1;
            if (isPressed && !targetHudResetKeyWasPressed) {
               TargetHudRenderer.resetItemTracker();
            }

            targetHudResetKeyWasPressed = isPressed;
         }

         if (isFreelookEnabled && client.currentScreen == null) {
            if (freelookToggleMode) {
               boolean isPressed = GLFW.glfwGetKey(window, freelookKey) == 1;
               if (isPressed && !freelookKeyWasPressed) {
                  freelookActive = !freelookActive;
                  if (freelookActive && client.player != null) {
                     freelookYaw = client.player.getYaw();
                     freelookPitch = client.player.getPitch();
                     preFreelookPerspective = client.options.getPerspective();
                     client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                  } else {
                     client.options.setPerspective(preFreelookPerspective);
                  }
               }

               freelookKeyWasPressed = isPressed;
            } else {
               boolean isPressed = GLFW.glfwGetKey(window, freelookKey) == 1;
               if (isPressed) {
                  if (!freelookActive && client.player != null) {
                     freelookActive = true;
                     freelookYaw = client.player.getYaw();
                     freelookPitch = client.player.getPitch();
                     preFreelookPerspective = client.options.getPerspective();
                     client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                  }
               } else if (freelookActive) {
                  freelookActive = false;
                  client.options.setPerspective(preFreelookPerspective);
               }
            }
         } else if (freelookActive) {
            freelookActive = false;
            client.options.setPerspective(preFreelookPerspective);
         }

         if (isStopwatchEnabled && stopwatchKey != -1 && client.currentScreen == null) {
            boolean stopwatchKeyDown = GLFW.glfwGetKey(window, stopwatchKey) == 1;
            if (stopwatchKeyDown && !stopwatchKeyWasPressed) {
               StopwatchManager.toggleRunning();
            }

            stopwatchKeyWasPressed = stopwatchKeyDown;
         } else {
            stopwatchKeyWasPressed = false;
         }

         if (isZoomEnabled && client.currentScreen == null) {
            boolean zoomKeyDown = GLFW.glfwGetKey(window, zoomKey) == 1;
            if (zoomToggleMode) {
               if (zoomKeyDown && !zoomKeyWasPressed) {
                  zoomActive = !zoomActive;
               }

               zoomKeyWasPressed = zoomKeyDown;
            } else {
               zoomActive = zoomKeyDown;
               zoomKeyWasPressed = false;
            }
         } else {
            zoomActive = false;
            zoomKeyWasPressed = false;
         }

         if (zoomActive != prevZoomActive) {
            showNotification("Zoom: " + (zoomActive ? "ON" : "OFF"));
            prevZoomActive = zoomActive;
         }

         if (freelookActive != prevFreelookActive) {
            showNotification("FreeLook: " + (freelookActive ? "ON" : "OFF"));
            prevFreelookActive = freelookActive;
         }

         if (client.world != null && client.player != null) {
            sessionPlayTimeTicks++;
            allTimePlayTimeTicks++;
         }

         NameProtect.INSTANCE.setEnabled(isNickHiderEnabled);
         if (isNickHiderEnabled && client.world != null && client.player != null) {
            updateNameProtectMappings(client);
         }

         if (isDamageHeartsEnabled) {
            syncDamageHeartsToModule();
            DamageHeartsModule.update();
         }

         saveTicks++;
         if (saveTicks >= 200 && configDirty) {
            saveTicks = 0;
            saveConfig();
         }

         if (isTpsDisplayEnabled) {
            long now = System.currentTimeMillis();
            if (lastTickTime > 0L) {
               long interval = now - lastTickTime;
               tickIntervals.addLast(interval);
               if (tickIntervals.size() > 20) {
                  tickIntervals.removeFirst();
               }

               long total = 0L;

               for (long t : tickIntervals) {
                  total += t;
               }

               double avgInterval = (double)total / tickIntervals.size();
               currentTps = Math.min(20.0, 1000.0 / avgInterval);
            }

            lastTickTime = now;
         }
      });
      ClientTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         MinecraftClient mcClient = MinecraftClient.getInstance();
         if (mcClient != null && mcClient.player != null) {
            long now = System.currentTimeMillis();
            Vec3d currentPos = new Vec3d(mcClient.player.getX(), mcClient.player.getY(), mcClient.player.getZ());
            if (lastTrackerPos != null && now - lastTrackerTime <= 1000L && currentPos.squaredDistanceTo(lastTrackerPos) > 250000.0) {
               TargetHudRenderer.resetItemTracker();
            }

            lastTrackerPos = currentPos;
            lastTrackerTime = now;
         }

         LogoutSpotsManager.tick();

         for (Entity entity : world.getEntities()) {
            if (entity instanceof LivingEntity living) {
               float currentHealth = living.getHealth();
               Float lastHealth = entityHealthMap.get(living);
               if (lastHealth != null && currentHealth < lastHealth) {
                  float damage = lastHealth - currentHealth;
                  if (isDamageHeartsEnabled) {
                     DamageHeartsModule.onEntityDamaged(living, damage, null);
                  }

                  if (living == lastAttackedEntity && System.currentTimeMillis() - lastAttackTime < 1000L) {
                     sessionDamage += damage;
                     allTimeDamage += damage;
                  }
               }

               entityHealthMap.put(living, currentHealth);
            }
         }

         damageCountedThisTick.clear();
      });
      ClientReceiveMessageEvents.MODIFY_GAME.register((ModifyGame)(message, overlay) -> {
         if (NameProtect.INSTANCE.isEnabled() && message != null) {
            Text processed = NameProtect.processText(message);
            if (processed != null) {
               return processed;
            }
         }

         return message;
      });
      HudRenderCallback.EVENT
         .register(
            (HudRenderCallback)(drawContext, tickCounter) -> {
               MinecraftClient client = MinecraftClient.getInstance();
               if (client.currentScreen == null) {
                  boolean isLeftMouseDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), 0) == 1;
                  boolean isRightMouseDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), 1) == 1;
                  if (isLeftMouseDown && !wasLeftMouseDown) {
                     recordClick(0);
                  }

                  wasLeftMouseDown = isLeftMouseDown;
                  if (isRightMouseDown && !wasRightMouseDown) {
                     recordClick(1);
                  }

                  wasRightMouseDown = isRightMouseDown;
                  if (isKeystrokesEnabled) {
                     long window = client.getWindow().getHandle();
                     pressedKeys.clear();
                     long now = System.currentTimeMillis();
                     keystrokeTimestamps.removeIf(t -> now - t > 2000L);
                     pressedKeys.clear();
                     int[] allKeys = new int[]{
                        48,
                        49,
                        50,
                        51,
                        52,
                        53,
                        54,
                        55,
                        56,
                        57,
                        65,
                        66,
                        67,
                        68,
                        69,
                        70,
                        71,
                        72,
                        73,
                        74,
                        75,
                        76,
                        77,
                        78,
                        79,
                        80,
                        81,
                        82,
                        83,
                        84,
                        85,
                        86,
                        87,
                        88,
                        89,
                        90,
                        32,
                        256,
                        257,
                        258,
                        259,
                        340,
                        344,
                        341,
                        345,
                        342,
                        346,
                        280,
                        45,
                        61,
                        91,
                        93,
                        92,
                        59,
                        39,
                        44,
                        46,
                        47
                     };

                     for (int key : allKeys) {
                        if (GLFW.glfwGetKey(window, key) == 1) {
                           pressedKeys.add(key);
                        }
                     }
                  }
               }
            }
         );
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, tickCounter) -> {
         if (isFpsGraphEnabled) {
            FpsGraphManager.onFrame();
         }

         if (isInvHudEnabled) {
            applyScale(context, invHudX, invHudY, invHudScale);
            renderInvHud(context);
            resetScale(context);
         }

         if (isCoordinatesEnabled) {
            applyScale(context, coordinatesX, coordinatesY, coordinatesScale);
            renderCoordinates(context);
            resetScale(context);
         }

         if (isCpsEnabled) {
            applyScale(context, cpsX, cpsY, cpsScale);
            renderCps(context);
            resetScale(context);
         }

         if (isKeystrokesEnabled) {
            applyScale(context, keystrokesX, keystrokesY, keystrokesScale);
            renderKeystrokes(context);
            resetScale(context);
         }

         if (isCompassEnabled) {
            applyScale(context, compassX, compassY, compassScale);
            renderCompass(context);
            resetScale(context);
         }

         if (isArmorStatusEnabled) {
            applyScale(context, armorStatusX, armorStatusY, armorStatusScale);
            renderArmorStatus(context);
            resetScale(context);
         }

         if (isTargetHudEnabled) {
            applyScale(context, targetHudX, targetHudY, targetHudScale);
            TargetHudRenderer.render(context);
            resetScale(context);

            if (targetHudAboveHead) {
               TargetHudRenderer.renderAboveHead(context);
            }
         }

         if (isTargetHudEnabled && targetHudShowEnemyCrucials) {
            applyScale(context, enemyCrucialsHudX, enemyCrucialsHudY, enemyCrucialsHudScale);
            EnemyCrucialsHudRenderer.render(context);
            resetScale(context);
         }

         if (isTargetHudEnabled && targetHudMmoArmorDisplay) {
            applyScale(context, targetHudMmoArmorX, targetHudMmoArmorY, targetHudMmoArmorScale);
            MmoArmorHudRenderer.render(context);
            resetScale(context);
         }

         if (isStatsHudEnabled) {
            applyScale(context, statsHudX, statsHudY, statsHudScale);
            renderStatsHud(context);
            resetScale(context);
         }

         if (isPotionStatusEnabled) {
            applyScale(context, potionStatusX, potionStatusY, potionStatusScale);
            PotionStatusRenderer.render(context);
            resetScale(context);
         }

         if (isComboCounterEnabled) {
            applyScale(context, comboCounterX, comboCounterY, comboCounterScale);
            renderComboCounter(context);
            resetScale(context);
         }

         if (isNotificationsEnabled) {
            applyScale(context, notificationsX, notificationsY, notificationsScale);
            renderNotifications(context);
            resetScale(context);
         }

         if (isPingDisplayEnabled) {
            applyScale(context, pingDisplayX, pingDisplayY, pingDisplayScale);
            renderPingDisplay(context);
            resetScale(context);
         }

         if (isLowHealthAlarmEnabled) {
            applyScale(context, lowHealthAlarmX, lowHealthAlarmY, lowHealthAlarmScale);
            LowHealthAlarmManager.render(context);
            resetScale(context);
         }

         if (isPotWarningEnabled) {
            applyScale(context, potWarningX, potWarningY, potWarningScale);
            PotWarningManager.render(context);
            resetScale(context);
         }

         if (isDropPreventionEnabled) {
            applyScale(context, dropPreventionX, dropPreventionY, dropPreventionScale);
            DropPreventionManager.render(context);
            resetScale(context);
         }

         if (isServerInfoEnabled) {
            applyScale(context, serverInfoX, serverInfoY, serverInfoScale);
            ServerInfoManager.render(context);
            resetScale(context);
         }

         if (isSpeedDisplayEnabled) {
            applyScale(context, speedDisplayX, speedDisplayY, speedDisplayScale);
            renderSpeedDisplay(context);
            resetScale(context);
         }

         if (isBiomeDisplayEnabled) {
            applyScale(context, biomeDisplayX, biomeDisplayY, biomeDisplayScale);
            renderBiomeDisplay(context);
            resetScale(context);
         }

         if (isEntityCounterEnabled) {
            applyScale(context, entityCounterX, entityCounterY, entityCounterScale);
            renderEntityCounter(context);
            resetScale(context);
         }

         if (isIrlClockEnabled) {
            applyScale(context, irlClockX, irlClockY, irlClockScale);
            renderIrlClock(context);
            resetScale(context);
         }

         if (isTotemCounterEnabled) {
            applyScale(context, totemCounterX, totemCounterY, totemCounterScale);
            renderTotemCounter(context);
            resetScale(context);
         }

         if (isArrowCounterEnabled) {
            applyScale(context, arrowCounterX, arrowCounterY, arrowCounterScale);
            renderArrowCounter(context);
            resetScale(context);
         }

         if (isReachDisplayEnabled) {
            applyScale(context, reachDisplayX, reachDisplayY, reachDisplayScale);
            renderReachDisplay(context);
            resetScale(context);
         }

         if (isPackDisplayEnabled) {
            applyScale(context, packDisplayX, packDisplayY, packDisplayScale);
            renderPackDisplay(context);
            resetScale(context);
         }

         if (isBlockIndicatorEnabled) {
            applyScale(context, blockIndicatorX, blockIndicatorY, blockIndicatorScale);
            renderBlockIndicator(context);
            resetScale(context);
         }

         if (isDeathInfoEnabled && deathInfoShowOnScreen && hasDeathInfo) {
            applyScale(context, deathInfoX, deathInfoY, deathInfoScale);
            renderDeathInfo(context);
            resetScale(context);
         }

         if (isTpsDisplayEnabled) {
            applyScale(context, tpsDisplayX, tpsDisplayY, tpsDisplayScale);
            renderTpsDisplay(context);
            resetScale(context);
         }

         if (isSystemResourcesEnabled) {
            applyScale(context, systemResourcesX, systemResourcesY, systemResourcesScale);
            SystemResourcesManager.render(context);
            resetScale(context);
         }

         if (isStopwatchEnabled) {
            applyScale(context, stopwatchX, stopwatchY, stopwatchScale);
            StopwatchManager.render(context);
            resetScale(context);
         }

         if (isWatermarkEnabled) {
            applyScale(context, watermarkX, watermarkY, watermarkScale);
            WatermarkManager.render(context);
            resetScale(context);
         }

         if (isKeybindsDisplayEnabled) {
            KeybindsHudRenderer.render(context);
         }

         if (isArrayListEnabled) {
            applyScale(context, arrayListX, arrayListY, arrayListScale);
            ArrayListManager.render(context);
            resetScale(context);
         }

         if (isFpsCounterEnabled) {
            applyScale(context, fpsCounterX, fpsCounterY, fpsCounterScale);
            renderFpsCounter(context);
            resetScale(context);
         }

         if (isFpsGraphEnabled) {
            applyScale(context, fpsGraphX, fpsGraphY, fpsGraphScale);
            FpsGraphManager.render(context);
            resetScale(context);
         }

         if (isTntTimerEnabled) {
            TntTimerRenderer.renderHud(context);
         }

         if (isWaypointsEnabled) {
            renderWaypointHud(context);
         }

         if (isTextHudEnabled) {
            applyScale(context, textHudX, textHudY, textHudScale);
            TextHudRenderer.render(context, 0, 0);
            resetScale(context);
         }

         if (isChatHeadsEnabled) {
            applyScale(context, 10, 10, 1.0F);
            renderChatHeads(context);
            resetScale(context);
         }

         if (isMouseStrokesEnabled) {
            applyScale(context, mouseStrokesX, mouseStrokesY, mouseStrokesScale);
            MouseStrokesRenderer.render(context);
            resetScale(context);
         }

         if (isTransparentShieldEnabled) {
            applyScale(context, transparentShieldX, transparentShieldY, transparentShieldScale);
            TransparentShieldRenderer.render(context);
            resetScale(context);
         }

         if (isAttackHudEnabled) {
            applyScale(context, attackHudX, attackHudY, 1.0F);
            AttackHudRenderer.render(context);
            resetScale(context);
         }

         if (isNameTagItemsEnabled) {
            NameTagItemsRenderer.render(context);
         }
      });
   }

   public static void applyScale(DrawContext context, int x, int y, float scale) {
      MinecraftClient _mc = MinecraftClient.getInstance();
      if (_mc != null && _mc.getWindow() != null) {
         int sw = _mc.getWindow().getScaledWidth();
         int sh = _mc.getWindow().getScaledHeight();
         x = Math.max(0, Math.min(x, sw - 10));
         y = Math.max(0, Math.min(y, sh - 10));
      }

      context.getMatrices().pushMatrix();
      context.getMatrices().translate(x, y);
      context.getMatrices().scale(scale, scale);
   }

   public static void resetScale(DrawContext context) {
      context.getMatrices().popMatrix();
   }

   public static void renderFpsCounter(DrawContext context) {
      if (isFpsCounterEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null) {
            if (shouldUpdateHudValues()) {
               int fps = FpsGraphManager.currentFps > 0 ? FpsGraphManager.currentFps : client.getCurrentFps();
               cachedFpsText = "FPS: " + fps;
            }

            int x = 0;
            int y = 0;
            context.fill(x, y, x + 80, y + 20, Integer.MIN_VALUE);
            context.fill(x, y, x + 80, y + 1, getPrimaryColor());
            context.fill(x, y, x + 1, y + 20, getPrimaryColor());
            context.fill(x + 79, y, x + 80, y + 20, getPrimaryColor());
            context.fill(x, y + 19, x + 80, y + 20, getPrimaryColor());
            context.drawTextWithShadow(client.textRenderer, cachedFpsText, x + 5, y + 5, -1);
         }
      }
   }

   public static void renderCoordinates(DrawContext context) {
      if (isCoordinatesEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               if (shouldUpdateHudValues()) {
                  int playerX = (int)client.player.getX();
                  int playerY = (int)client.player.getY();
                  int playerZ = (int)client.player.getZ();
                  String direction = getCardinalDirection(client.player.getYaw());
                  cachedCoordsText = String.format("XYZ: %d %d %d [%s]", playerX, playerY, playerZ, direction);
                  cachedCoordsWidth = client.textRenderer.getWidth(cachedCoordsText) + 10;
               }

               int x = 0;
               int y = 0;
               int tw = cachedCoordsWidth;
               context.fill(x, y, x + tw, y + 20, Integer.MIN_VALUE);
               context.fill(x, y, x + tw, y + 1, getPrimaryColor());
               context.fill(x, y, x + 1, y + 20, getPrimaryColor());
               context.fill(x + tw - 1, y, x + tw, y + 20, getPrimaryColor());
               context.fill(x, y + 19, x + tw, y + 20, getPrimaryColor());
               context.drawTextWithShadow(client.textRenderer, cachedCoordsText, x + 5, y + 5, -1);
            }
         }
      }
   }

   public static int getCoordinatesHudWidth() {
      return cachedCoordsWidth;
   }

   private static String getCardinalDirection(float yaw) {
      float normalizedYaw = (yaw % 360.0F + 360.0F) % 360.0F;
      if (normalizedYaw >= 337.5 || normalizedYaw < 22.5) {
         return "S";
      } else if (normalizedYaw >= 22.5 && normalizedYaw < 67.5) {
         return "SW";
      } else if (normalizedYaw >= 67.5 && normalizedYaw < 112.5) {
         return "W";
      } else if (normalizedYaw >= 112.5 && normalizedYaw < 157.5) {
         return "NW";
      } else if (normalizedYaw >= 157.5 && normalizedYaw < 202.5) {
         return "N";
      } else if (normalizedYaw >= 202.5 && normalizedYaw < 247.5) {
         return "NE";
      } else {
         return normalizedYaw >= 247.5 && normalizedYaw < 292.5 ? "E" : "SE";
      }
   }

   public static void renderCps(DrawContext context) {
      if (isCpsEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null) {
            int x = 0;
            int y = 0;
            long currentTime = System.currentTimeMillis();
            leftClickTimestamps.removeIf(timestamp -> currentTime - timestamp > 1000L);
            rightClickTimestamps.removeIf(timestamp -> currentTime - timestamp > 1000L);
            int leftCps = leftClickTimestamps.size();
            int rightCps = rightClickTimestamps.size();
            String cpsText = leftCps + " | " + rightCps;
            int textWidth = client.textRenderer.getWidth(cpsText);
            context.fill(x, y, x + textWidth + 10, y + 20, Integer.MIN_VALUE);
            context.fill(x, y, x + textWidth + 10, y + 1, getPrimaryColor());
            context.fill(x, y, x + 1, y + 20, getPrimaryColor());
            context.fill(x + textWidth + 9, y, x + textWidth + 10, y + 20, getPrimaryColor());
            context.fill(x, y + 19, x + textWidth + 10, y + 20, getPrimaryColor());
            context.drawTextWithShadow(client.textRenderer, cpsText, x + 5, y + 5, -1);
         }
      }
   }

   public static void recordClick(int button) {
      long timestamp = System.currentTimeMillis();
      if (button == 0) {
         leftClickTimestamps.add(timestamp);
         sessionClicks++;
         allTimeClicks++;
      } else if (button == 1) {
         rightClickTimestamps.add(timestamp);
      }
   }

   public static void renderKeystrokes(DrawContext context) {
      if (isKeystrokesEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null) {
            KeystrokesModes.render(context, keystrokesMode);
         }
      }
   }

   public static void renderInvHud(DrawContext context) {
      if (isInvHudEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               int startX = 0;
               int startY = 0;
               int borderThickness = invHudBorderThickness;
               int borderColor = getPrimaryColor();
               int bgWidth = 176;
               int mainInvHeight = 58;
               int hotbarHeight = 22;
               int totalHeight = invHudShowHotbar ? mainInvHeight + hotbarHeight + 4 : mainInvHeight;
               if (!invHudTransparent) {
                  context.drawTexture(
                     RenderPipelines.GUI_TEXTURED, InventoryScreen.BACKGROUND_TEXTURE, startX, startY, 0.0F, 84.0F, bgWidth, mainInvHeight, 256, 256
                  );
                  if (invHudShowHotbar) {
                     int hotbarScreenY = startY + mainInvHeight + 4;
                     context.drawTexture(
                        RenderPipelines.GUI_TEXTURED, InventoryScreen.BACKGROUND_TEXTURE, startX, hotbarScreenY, 0.0F, 142.0F, bgWidth, hotbarHeight, 256, 256
                     );
                  }

                  for (int i = 0; i < borderThickness; i++) {
                     context.fill(startX - i, startY - i, startX + bgWidth + i, startY - i + 1, borderColor);
                     context.fill(startX - i, startY + totalHeight + i - 1, startX + bgWidth + i, startY + totalHeight + i, borderColor);
                     context.fill(startX - i, startY - i, startX - i + 1, startY + totalHeight + i, borderColor);
                     context.fill(startX + bgWidth + i - 1, startY - i, startX + bgWidth + i, startY + totalHeight + i, borderColor);
                  }
               }

               int slotSize = 18;
               int mainInvX = startX + 8;
               int mainInvY = startY;

               for (int row = 0; row < 3; row++) {
                  for (int col = 0; col < 9; col++) {
                     int index = 9 + row * 9 + col;
                     ItemStack stack = client.player.getInventory().getStack(index);
                     int x = mainInvX + col * slotSize;
                     int y = mainInvY + row * slotSize;
                     context.drawItem(stack, x, y);
                     if (invHudShowItemCount) {
                        context.drawStackOverlay(client.textRenderer, stack, x, y);
                     }
                  }
               }

               if (invHudShowHotbar) {
                  int hotbarScreenY = startY + mainInvHeight + 4;

                  for (int colx = 0; colx < 9; colx++) {
                     ItemStack stack = client.player.getInventory().getStack(colx);
                     int x = mainInvX + colx * slotSize;
                     int y = hotbarScreenY + 8;
                     context.drawItem(stack, x, y);
                     if (invHudShowItemCount) {
                        context.drawStackOverlay(client.textRenderer, stack, x, y);
                     }
                  }
               }
            }
         }
      }
   }

   public static void renderCompass(DrawContext context) {
      if (isCompassEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               int x = 0;
               int y = 0;
               float yaw = client.player.getYaw();
               float normalizedYaw = (yaw % 360.0F + 360.0F) % 360.0F;
               int barWidth = 200;
               int barHeight = 30;
               int centerX = x + barWidth / 2;
               context.fill(x, y, x + barWidth, y + barHeight, Integer.MIN_VALUE);
               context.fill(x, y, x + barWidth, y + 1, getPrimaryColor());
               context.fill(x, y + barHeight - 1, x + barWidth, y + barHeight, getPrimaryColor());
               context.fill(x, y, x + 1, y + barHeight, getPrimaryColor());
               context.fill(x + barWidth - 1, y, x + barWidth, y + barHeight, getPrimaryColor());
               int pixelsPerDegree = 2;
               int visibleDegrees = barWidth / pixelsPerDegree;
               int halfVisible = visibleDegrees / 2;

               for (int deg = -halfVisible; deg <= halfVisible; deg++) {
                  int screenDeg = (int)((normalizedYaw + deg + 360.0F) % 360.0F);
                  int drawX = centerX + deg * pixelsPerDegree;
                  if (drawX >= x + 5 && drawX <= x + barWidth - 5 && screenDeg % 15 == 0) {
                     int tickHeight = screenDeg % 45 == 0 ? 10 : 5;
                     context.fill(drawX, y + barHeight - tickHeight - 5, drawX + 1, y + barHeight - 5, -1);
                     if (screenDeg % 45 == 0) {
                        String label = getCompassLabel(screenDeg);
                        int textWidth = client.textRenderer.getWidth(label);
                        context.drawTextWithShadow(client.textRenderer, label, drawX - textWidth / 2, y + 3, -1);
                     }

                     if (screenDeg % 15 == 0 && screenDeg % 45 != 0) {
                        String degText = String.valueOf(screenDeg);
                        int textWidth = client.textRenderer.getWidth(degText);
                        if (textWidth < 20) {
                           context.drawText(client.textRenderer, degText, drawX - textWidth / 2, y + barHeight - 14, -1431655766, false);
                        }
                     }
                  }
               }

               int indicatorY = y + barHeight - 5;
               context.fill(centerX, indicatorY - 8, centerX + 1, indicatorY, getPrimaryColor());
               context.fill(centerX - 3, indicatorY - 5, centerX, indicatorY - 8, getPrimaryColor());
               context.fill(centerX + 1, indicatorY - 5, centerX + 4, indicatorY - 8, getPrimaryColor());
               String yawText = String.format("%.1f", normalizedYaw);
               int yawTextWidth = client.textRenderer.getWidth(yawText);
               context.drawTextWithShadow(client.textRenderer, yawText, centerX - yawTextWidth / 2, y + barHeight + 2, getPrimaryColor());
            }
         }
      }
   }

   private static String getCompassLabel(int degrees) {
      switch (degrees) {
         case 0:
            return "S";
         case 45:
            return "SW";
         case 90:
            return "W";
         case 135:
            return "NW";
         case 180:
            return "N";
         case 225:
            return "NE";
         case 270:
            return "E";
         case 315:
            return "SE";
         default:
            return "";
      }
   }

   public static void renderArmorStatus(DrawContext context) {
      if (isArmorStatusEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               int x = 0;
               int y = 0;
               int[] armorSlots = new int[]{3, 2, 1, 0};
               String[] var10000 = new String[]{"Helmet", "Chest", "Legs", "Boots"};
               int itemSize = 20;
               int gap = 2;
               int bgWidth;
               int bgHeight;
               if (armorStatusOrientation == 1) {
                  bgWidth = itemSize * 4 + gap * 5 + 5;
                  bgHeight = itemSize + gap * 2 + 5;
               } else {
                  bgWidth = 100;
                  bgHeight = itemSize * 4 + gap * 5 + 5;
               }

               if (!armorStatusTransparentBg) {
                  context.fill(x, y, x + bgWidth, y + bgHeight, Integer.MIN_VALUE);
                  context.fill(x, y, x + bgWidth, y + 1, getPrimaryColor());
                  context.fill(x, y + bgHeight - 1, x + bgWidth, y + bgHeight, getPrimaryColor());
                  context.fill(x, y, x + 1, y + bgHeight, getPrimaryColor());
                  context.fill(x + bgWidth - 1, y, x + bgWidth, y + bgHeight, getPrimaryColor());
               }

               for (int i = 0; i < 4; i++) {
                  int slot = armorSlots[i];

                  ItemStack stack = client.player.getEquippedStack(switch (slot) {
                     case 1 -> EquipmentSlot.LEGS;
                     case 2 -> EquipmentSlot.CHEST;
                     case 3 -> EquipmentSlot.HEAD;
                     default -> EquipmentSlot.FEET;
                  });
                  int itemX;
                  int itemY;
                  if (armorStatusOrientation == 1) {
                     itemX = x + gap + i * (itemSize + gap);
                     itemY = y + gap;
                  } else {
                     itemX = x + 5;
                     itemY = y + gap + i * (itemSize + gap);
                  }

                  if (!stack.isEmpty() || armorStatusShowEmptySlots) {
                     if (!stack.isEmpty()) {
                        context.drawItem(stack, itemX, itemY + 2);
                        context.drawStackOverlay(client.textRenderer, stack, itemX, itemY + 2);
                     }

                     if (!stack.isEmpty() && stack.isDamageable()) {
                        int maxDurability = stack.getMaxDamage();
                        int currentDurability = maxDurability - stack.getDamage();
                        int percent = currentDurability * 100 / maxDurability;
                        if (armorStatusWarningSound && percent <= armorStatusWarningThreshold && percent > 0) {
                           if (percent < lastDurability[i]) {
                              SoundEvent sound = (SoundEvent)Registries.SOUND_EVENT.get(Identifier.of("voidcyan", "armor_warning"));
                              client.player.playSound(sound, 1.0F, 1.0F);
                           }

                           lastDurability[i] = percent;
                        }

                        if (percent > lastDurability[i]) {
                           lastDurability[i] = percent;
                        }

                        String durabilityText;
                        if (armorStatusDisplayMode == 0) {
                           durabilityText = percent + "%";
                        } else if (armorStatusDisplayMode == 1) {
                           durabilityText = currentDurability + "/" + maxDurability;
                        } else {
                           durabilityText = percent + "% (" + currentDurability + "/" + maxDurability + ")";
                        }

                        int textX;
                        int textY;
                        if (armorStatusOrientation == 1) {
                           int textWidth = client.textRenderer.getWidth(durabilityText);
                           textX = itemX + (itemSize - textWidth) / 2;
                           textY = itemY - 10;
                        } else {
                           textX = x + 28;
                           textY = itemY + 6;
                        }

                        context.drawTextWithShadow(client.textRenderer, durabilityText, textX, textY, -1);
                        if (percent <= armorStatusWarningThreshold && percent > 0) {
                           int warningY;
                           int warningX;
                           if (armorStatusOrientation == 1) {
                              warningX = itemX - 8;
                              warningY = itemY - 2;
                           } else {
                              warningX = x + 2;
                              warningY = itemY + 2;
                           }

                           context.fill(warningX - 1, warningY - 1, warningX + 7, warningY + 7, -16777216);
                           context.fill(warningX, warningY, warningX + 6, warningY + 6, -65536);
                           context.drawTextWithShadow(client.textRenderer, "!", warningX + 1, warningY, -1);
                        }

                        if (armorStatusShowDurabilityBar) {
                           int barY;
                           int barWidth;
                           int barX;
                           if (armorStatusOrientation == 1) {
                              barWidth = 16;
                              barX = itemX;
                              barY = itemY - 3;
                           } else {
                              barWidth = bgWidth - 35;
                              barX = x + 28;
                              barY = itemY + itemSize - 2;
                           }

                           int barHeight = 3;
                           int filledWidth = barWidth * percent / 100;
                           context.fill(barX, barY, barX + barWidth, barY + barHeight, -14540254);
                           context.fill(barX, barY, barX + barWidth, barY + 1, -11184811);
                           context.fill(barX, barY + barHeight - 1, barX + barWidth, barY + barHeight, -11184811);
                           int barColor = armorStatusDurabilityBarColor;
                           if (percent < 25) {
                              barColor = -65536;
                           } else if (percent < 50) {
                              barColor = -30720;
                           } else if (percent < 75) {
                              barColor = -256;
                           } else {
                              barColor = -16711936;
                           }

                           context.fill(barX, barY + 1, barX + filledWidth, barY + barHeight - 1, barColor);
                        }
                     } else if (!stack.isEmpty()) {
                        int infiniteX = armorStatusOrientation == 1 ? itemX + 25 : x + 28;
                        context.drawTextWithShadow(client.textRenderer, "8", infiniteX, itemY + 6, -16711936);
                     }
                  }
               }
            }
         }
      }
   }

   public static boolean isBotOrFloatingText(Entity entity) {
      if (!antiBot) {
         return false;
      } else if (entity instanceof ArmorStandEntity) {
         return true;
      } else {
         return entity instanceof DisplayEntity
            ? true
            : entity instanceof PlayerEntity player
               && MinecraftClient.getInstance().getNetworkHandler() != null
               && MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(player.getUuid()) == null;
      }
   }

   public static String getKillType(LivingEntity living) {
      MinecraftClient mcClient = MinecraftClient.getInstance();
      if (mcClient != null && mcClient.player != null) {
         if (living == mcClient.player && mcClient.player.getHealth() <= 0.0F) {
            long now = System.currentTimeMillis();
            if (lastAttackedEntity instanceof EndCrystalEntity crystal && now - lastAttackTime < 5000L && mcClient.player.squaredDistanceTo(crystal) < 144.0) {
               return "crystal";
            } else {
               return lastInteractPos != null
                     && now - lastInteractTime < 5000L
                     && mcClient.player.squaredDistanceTo(lastInteractPos.getX(), lastInteractPos.getY(), lastInteractPos.getZ()) < 144.0
                  ? "anchor"
                  : "none";
            }
         } else if (living == mcClient.player) {
            return "none";
         } else {
            long now = System.currentTimeMillis();
            if (lastAttackedEntity instanceof EndCrystalEntity crystal && now - lastAttackTime < 5000L && living.squaredDistanceTo(crystal) < 144.0) {
               return "crystal";
            } else if (lastInteractPos != null
               && now - lastInteractTime < 5000L
               && living.squaredDistanceTo(lastInteractPos.getX(), lastInteractPos.getY(), lastInteractPos.getZ()) < 144.0) {
               return "anchor";
            } else {
               return living == lastAttackedEntity && now - lastAttackTime < 5000L ? "melee" : "none";
            }
         }
      } else {
         return "none";
      }
   }

   public static int getStatsHudWidth() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.textRenderer != null) {
         int lineHeight = 10;
         int padX = 7;
         List<String> lines = buildStatsHudLines();
         if (lines.isEmpty()) {
            return 30;
         } else {
            int maxW = 0;

            for (String l : lines) {
               int w = client.textRenderer.getWidth(l);
               if (w > maxW) {
                  maxW = w;
               }
            }

            return maxW + padX * 2;
         }
      } else {
         return 120;
      }
   }

   public static int getStatsHudHeight() {
      int lineHeight = 10;
      int padTop = 6;
      int padBottom = 6;
      List<String> lines = buildStatsHudLines();
      return lines.isEmpty() ? 12 : padTop + lines.size() * (lineHeight + 1) - 1 + padBottom;
   }

   private static List<String> buildStatsHudLines() {
      List<String> lines = new ArrayList<>();
      if (statsHudShowKills) {
         lines.add("Kills: " + sessionKills);
      }

      if (statsHudShowDeaths) {
         lines.add("Deaths: " + sessionDeaths);
      }

      if (statsHudShowKD) {
         String kd = sessionDeaths > 0 ? String.format("%.2f", (double)sessionKills / sessionDeaths) : sessionKills + ".00";
         lines.add("K/D: " + kd);
      }

      if (statsHudShowPops) {
         lines.add("Totem Pops: " + sessionPops);
      }

      if (statsHudShowAnchors) {
         lines.add("Anchors: " + sessionAnchorKills);
      }

      if (statsHudShowCrystalKills) {
         lines.add("Crystal K: " + sessionCrystalKills);
      }

      if (statsHudShowCrystalDeaths) {
         lines.add("Crystal D: " + sessionCrystalDeaths);
      }

      if (statsHudShowClicks) {
         lines.add("Clicks: " + sessionClicks);
      }

      if (statsHudShowHits) {
         lines.add("Hits: " + sessionHits);
      }

      if (statsHudShowAttributeSwap) {
         String acc = sessionHits > 0 ? String.format("%.1f%%", (double)sessionAttributeSwaps / sessionHits * 100.0) : "0.0%";
         lines.add("Attr Swap: " + acc);
      }

      if (statsHudShowAccuracy) {
         String acc = sessionClicks > 0 ? String.format("%.1f%%", (double)sessionHits / sessionClicks * 100.0) : "0.0%";
         lines.add("Accuracy: " + acc);
      }

      if (statsHudShowDamage) {
         lines.add("Damage: " + String.format("%.1f", sessionDamage));
      }

      if (statsHudShowBlocksBroken) {
         lines.add("Broken: " + sessionBlocksBroken);
      }

      if (statsHudShowBlocksPlaced) {
         lines.add("Placed: " + sessionBlocksPlaced);
      }

      if (statsHudShowTime) {
         long sSec = sessionPlayTimeTicks / 20L;
         long d = sSec / 86400L;
         long h = sSec % 86400L / 3600L;
         long m = sSec % 3600L / 60L;
         long ss = sSec % 60L;
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

         sb.append(ss).append("s");
         lines.add("Time: " + sb);
      }

      return lines;
   }

   public static void renderStatsHud(DrawContext context) {
      if (isStatsHudEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null) {
            int x = 0;
            int y = 0;
            int lineHeight = 10;
            int padTop = 6;
            int padBottom = 6;
            int padX = 7;
            boolean allTime = statsHudStatMode == 1;
            int kills = allTime ? allTimeKills : sessionKills;
            int deaths = allTime ? allTimeDeaths : sessionDeaths;
            int pops = allTime ? allTimePops : sessionPops;
            int anchorKills = allTime ? allTimeAnchorKills : sessionAnchorKills;
            int crystalKills = allTime ? allTimeCrystalKills : sessionCrystalKills;
            int crystalDeaths = allTime ? allTimeCrystalDeaths : sessionCrystalDeaths;
            int clicks = allTime ? allTimeClicks : sessionClicks;
            int hits = allTime ? allTimeHits : sessionHits;
            int attrSwaps = allTime ? allTimeAttributeSwaps : sessionAttributeSwaps;
            double damage = allTime ? allTimeDamage : sessionDamage;
            int blocksBroken = allTime ? allTimeBlocksBroken : sessionBlocksBroken;
            int blocksPlaced = allTime ? allTimeBlocksPlaced : sessionBlocksPlaced;
            int crystPlaced = allTime ? allTimeCrystalsPlaced : sessionCrystalsPlaced;
            int crystBroken = allTime ? allTimeCrystalsBroken : sessionCrystalsBroken;
            int anchPlaced = allTime ? allTimeAnchorsPlaced : sessionAnchorsPlaced;
            int anchBlown = allTime ? allTimeAnchorsBlown : sessionAnchorsBlown;
            int anchCharged = allTime ? allTimeAnchorsCharged : sessionAnchorsCharged;
            long playTicks = allTime ? allTimePlayTimeTicks : sessionPlayTimeTicks;
            List<String> lines = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            if (statsHudShowKills) {
               lines.add("Kills: " + kills);
               colors.add(-11141291);
            }

            if (statsHudShowDeaths) {
               lines.add("Deaths: " + deaths);
               colors.add(-43691);
            }

            if (statsHudShowKD) {
               String kd = deaths > 0 ? String.format("%.2f", (double)kills / deaths) : kills + ".00";
               lines.add("K/D: " + kd);
               colors.add(-22016);
            }

            if (statsHudShowPops) {
               lines.add("Totem Pops: " + pops);
               colors.add(-43521);
            }

            if (statsHudShowAnchors) {
               lines.add("Anchors: " + anchorKills);
               colors.add(-5614081);
            }

            if (statsHudShowCrystalKills) {
               lines.add("Crystal K: " + crystalKills);
               colors.add(-65451);
            }

            if (statsHudShowCrystalDeaths) {
               lines.add("Crystal D: " + crystalDeaths);
               colors.add(-65536);
            }

            if (statsHudShowClicks) {
               lines.add("Clicks: " + clicks);
               colors.add(-1);
            }

            if (statsHudShowHits) {
               lines.add("Hits: " + hits);
               colors.add(-11141121);
            }

            if (statsHudShowAttributeSwap) {
               String acc = hits > 0 ? String.format("%.1f%%", (double)attrSwaps / hits * 100.0) : "0.0%";
               lines.add("Attr Swap: " + acc);
               colors.add(-30465);
            }

            if (statsHudShowAccuracy) {
               String acc = clicks > 0 ? String.format("%.1f%%", (double)hits / clicks * 100.0) : "0.0%";
               lines.add("Accuracy: " + acc);
               colors.add(-171);
            }

            if (statsHudShowDamage) {
               lines.add("Damage: " + String.format("%.1f", damage));
               colors.add(-30635);
            }

            if (statsHudShowBlocksBroken) {
               lines.add("Broken: " + blocksBroken);
               colors.add(-11141291);
            }

            if (statsHudShowBlocksPlaced) {
               lines.add("Placed: " + blocksPlaced);
               colors.add(-11184641);
            }

            if (statsHudShowCrystalsPlaced) {
               lines.add("Cry. Placed: " + crystPlaced);
               colors.add(-43521);
            }

            if (statsHudShowCrystalsBroken) {
               lines.add("Cry. Broken: " + crystBroken);
               colors.add(-43691);
            }

            if (statsHudShowAnchorsPlaced) {
               lines.add("Anch. Placed: " + anchPlaced);
               colors.add(-11141121);
            }

            if (statsHudShowAnchorsBlown) {
               lines.add("Anch. Blown: " + anchBlown);
               colors.add(-171);
            }

            if (statsHudShowAnchorsCharged) {
               lines.add("Glowstone: " + anchCharged);
               colors.add(-13312);
            }

            if (statsHudShowTime) {
               long sSecs = playTicks / 20L;
               long sD = sSecs / 86400L;
               long sH = sSecs % 86400L / 3600L;
               long sM = sSecs % 3600L / 60L;
               long sS = sSecs % 60L;
               StringBuilder sb = new StringBuilder();
               if (sD > 0L) {
                  sb.append(sD).append("d ");
               }

               if (sH > 0L || sD > 0L) {
                  sb.append(sH).append("h ");
               }

               if (sM > 0L || sH > 0L || sD > 0L) {
                  sb.append(sM).append("m ");
               }

               sb.append(sS).append("s");
               lines.add("Time: " + sb);
               colors.add(-171);
            }

            if (!lines.isEmpty()) {
               int maxTextWidth = 0;
               String modeLabel = allTime ? "[All-Time]" : "[Session]";
               maxTextWidth = client.textRenderer.getWidth(modeLabel);

               for (String line : lines) {
                  int w = client.textRenderer.getWidth(line);
                  if (w > maxTextWidth) {
                     maxTextWidth = w;
                  }
               }

               int width = maxTextWidth + padX * 2;
               int height = padTop + lines.size() * (lineHeight + 1) - 1 + padBottom;
               context.fill(x, y, x + width, y + height, Integer.MIN_VALUE);
               context.fill(x, y, x + width, y + 1, getPrimaryColor());
               context.fill(x, y + height - 1, x + width, y + height, getPrimaryColor());
               context.fill(x, y, x + 1, y + height, getPrimaryColor());
               context.fill(x + width - 1, y, x + width, y + height, getPrimaryColor());

               for (int i = 0; i < lines.size(); i++) {
                  context.drawTextWithShadow(client.textRenderer, lines.get(i), x + padX, y + padTop + i * (lineHeight + 1), colors.get(i));
               }
            }
         }
      }
   }

   public static void renderComboCounter(DrawContext context) {
      if (currentCombo > 0) {
         if (System.currentTimeMillis() - lastComboHitTime > comboTimeout) {
            currentCombo = 0;
         } else {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen == null) {
               float timeSinceHit = (float)(System.currentTimeMillis() - lastComboHitTime);
               float scale = 1.0F;
               if (timeSinceHit < 200.0F) {
                  scale = 1.0F + 0.5F * (1.0F - timeSinceHit / 200.0F);
               }

               String comboText = currentCombo + " Combo";
               int textWidth = client.textRenderer.getWidth(comboText);
               int x = 0;
               int y = 0;
               context.fill(x, y, x + textWidth + 10, y + 20, Integer.MIN_VALUE);
               context.fill(x, y, x + textWidth + 10, y + 1, getPrimaryColor());
               context.fill(x, y, x + 1, y + 20, getPrimaryColor());
               context.fill(x + textWidth + 9, y, x + textWidth + 10, y + 20, getPrimaryColor());
               context.fill(x, y + 19, x + textWidth + 10, y + 20, getPrimaryColor());
               context.getMatrices().pushMatrix();
               context.getMatrices().translate(x + 5 + textWidth / 2.0F, y + 5 + 4.0F);
               context.getMatrices().scale(scale, scale);
               context.getMatrices().translate(-(x + 5 + textWidth / 2.0F), -(y + 5 + 4.0F));
               context.drawTextWithShadow(client.textRenderer, comboText, x + 5, y + 5, -43691);
               context.getMatrices().popMatrix();
            }
         }
      }
   }

   public static void renderNotifications(DrawContext context) {
      if (isNotificationsEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            activeNotifications.removeIf(Notification::isExpired);
            float slide = 1.0F;
            int yOffset = 0;

            for (Notification notif : activeNotifications) {
               String text = notif.message;
               int textWidth = client.textRenderer.getWidth(text);
               long elapsed = System.currentTimeMillis() - notif.startTime;
               if (elapsed < 300L) {
                  slide = (float)elapsed / 300.0F;
                  slide = (float)Math.pow(slide, 0.5);
               } else if (notif.duration - elapsed < 300L) {
                  slide = (float)(notif.duration - elapsed) / 300.0F;
                  slide = (float)Math.pow(slide, 2.0);
               } else {
                  slide = 1.0F;
               }

               int renderY = (int)(yOffset * slide);
               context.fill(0, renderY, textWidth + 10, renderY + 20, Integer.MIN_VALUE);
               context.fill(0, renderY, textWidth + 10, renderY + 1, getPrimaryColor());
               context.fill(0, renderY, 1, renderY + 20, getPrimaryColor());
               context.fill(textWidth + 9, renderY, textWidth + 10, renderY + 20, getPrimaryColor());
               context.fill(0, renderY + 19, textWidth + 10, renderY + 20, getPrimaryColor());
               int barWidth = (int)((textWidth + 10) * (1.0F - (float)elapsed / (float)notif.duration));
               if (barWidth < 0) {
                  barWidth = 0;
               }

               context.fill(0, renderY + 18, barWidth, renderY + 19, getPrimaryColor());
               context.drawTextWithShadow(client.textRenderer, text, 5, renderY + 5, -1);
               yOffset += 25;
            }
         }
      }
   }

   public static void renderPingDisplay(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.getNetworkHandler() != null) {
         if (client.currentScreen == null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry == null) {
               for (PlayerListEntry e : client.getNetworkHandler().getPlayerList()) {
                  if (e.getProfile() != null && e.getProfile().name().equals(client.player.getName().getString())) {
                     entry = e;
                     break;
                  }
               }
            }

            int ping = entry != null ? entry.getLatency() : 0;
            if (ping <= 0 && client.getCurrentServerEntry() != null) {
               ping = (int)client.getCurrentServerEntry().ping;
            }

            if (ping > 0) {
               lastKnownPing = ping;
            } else {
               ping = lastKnownPing;
            }

            boolean isLocal = client.isInSingleplayer() || client.getCurrentServerEntry() != null && client.getCurrentServerEntry().isLocal();
            String pingText = isLocal ? "Ping: SP" : "Ping: " + ping + "ms";
            int textWidth = client.textRenderer.getWidth(pingText);
            int boxW = textWidth + 10;
            int boxH = 20;
            int color = -11141291;
            if (!isLocal) {
               if (ping > 150) {
                  color = -43691;
               } else if (ping > 80) {
                  color = -171;
               }
            }

            int x = 0;
            int y = 0;
            context.fill(x, y, x + boxW, y + boxH, Integer.MIN_VALUE);
            int accent = getPrimaryColor();
            context.fill(x, y, x + boxW, y + 1, accent);
            context.fill(x, y, x + 1, y + boxH, accent);
            context.fill(x + boxW - 1, y, x + boxW, y + boxH, accent);
            context.fill(x, y + boxH - 1, x + boxW, y + boxH, accent);
            context.drawTextWithShadow(client.textRenderer, Text.literal(pingText), x + 5, y + 6, color);
         }
      }
   }

   public static void renderReachDisplay(DrawContext context) {
      if (isReachDisplayEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               if (System.currentTimeMillis() - lastReachTime <= 3000L) {
                  String reachText = String.format("Reach: %.2f", lastReachDistance);
                  int textWidth = client.textRenderer.getWidth(reachText);
                  context.fill(0, 0, textWidth + 10, 20, Integer.MIN_VALUE);
                  context.fill(0, 0, textWidth + 10, 1, getPrimaryColor());
                  context.fill(0, 0, 1, 20, getPrimaryColor());
                  context.fill(textWidth + 9, 0, textWidth + 10, 20, getPrimaryColor());
                  context.fill(0, 19, textWidth + 10, 20, getPrimaryColor());
                  context.drawTextWithShadow(client.textRenderer, reachText, 5, 5, -1);
               }
            }
         }
      }
   }

   public static void renderSpeedDisplay(DrawContext context) {
      if (isSpeedDisplayEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               double dx = client.player.getVelocity().x;
               double dz = client.player.getVelocity().z;
               double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
               String text = String.format("Speed: %.2f BPS", speed);
               int textWidth = client.textRenderer.getWidth(text);
               context.fill(0, 0, textWidth + 10, 20, Integer.MIN_VALUE);
               context.fill(0, 0, textWidth + 10, 1, getPrimaryColor());
               context.fill(0, 0, 1, 20, getPrimaryColor());
               context.fill(textWidth + 9, 0, textWidth + 10, 20, getPrimaryColor());
               context.fill(0, 19, textWidth + 10, 20, getPrimaryColor());
               context.drawTextWithShadow(client.textRenderer, text, 5, 5, -1);
            }
         }
      }
   }

   public static void renderBiomeDisplay(DrawContext context) {
      if (isBiomeDisplayEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.world != null) {
            if (client.currentScreen == null) {
               BlockPos pos = client.player.getBlockPos();
               RegistryEntry<Biome> biomeEntry = client.world.getBiome(pos);
               String biomeName = biomeEntry.getKey().map(key -> key.getValue().getPath()).orElse("unknown");
               String[] words = biomeName.split("_");
               StringBuilder formattedName = new StringBuilder();

               for (String word : words) {
                  if (word.length() > 0) {
                     formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                  }
               }

               String text = "Biome: " + formattedName.toString().trim();
               int textWidth = client.textRenderer.getWidth(text);
               context.fill(0, 0, textWidth + 10, 20, Integer.MIN_VALUE);
               context.fill(0, 0, textWidth + 10, 1, getPrimaryColor());
               context.fill(0, 0, 1, 20, getPrimaryColor());
               context.fill(textWidth + 9, 0, textWidth + 10, 20, getPrimaryColor());
               context.fill(0, 19, textWidth + 10, 20, getPrimaryColor());
               context.drawTextWithShadow(client.textRenderer, text, 5, 5, -1);
            }
         }
      }
   }

   public static int getEntityCounterWidth() {
      return !isEntityCounterEnabled ? 100 : 80;
   }

   public static int getEntityCounterHeight() {
      if (!isEntityCounterEnabled) {
         return 60;
      } else {
         int linesCount = 0;
         if (entityCounterShowPlayers) {
            linesCount++;
         }

         if (entityCounterShowHostile) {
            linesCount++;
         }

         if (entityCounterShowAnimals) {
            linesCount++;
         }

         if (entityCounterShowItems) {
            linesCount++;
         }

         if (entityCounterShowTotal) {
            linesCount++;
         }

         return linesCount == 0 ? 20 : linesCount * 10 + 8;
      }
   }

   public static void renderEntityCounter(DrawContext context) {
      if (isEntityCounterEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.world != null) {
            if (client.currentScreen == null || client.currentScreen instanceof EditHudScreen) {
               int players = 0;
               int hostile = 0;
               int animals = 0;
               int items = 0;
               ChunkPos playerChunk = entityCounterCurrentChunkOnly ? new ChunkPos(client.player.getBlockPos()) : null;

               for (Entity entity : client.world.getEntities()) {
                  if (entity != null && (!entityCounterCurrentChunkOnly || new ChunkPos(entity.getBlockPos()).equals(playerChunk))) {
                     if (entity instanceof PlayerEntity && entity != client.player) {
                        players++;
                     } else if (entity instanceof HostileEntity) {
                        hostile++;
                     } else if (entity instanceof AnimalEntity) {
                        animals++;
                     } else if (entity instanceof ItemEntity) {
                        items++;
                     }
                  }
               }

               List<String> lines = new ArrayList<>();
               List<Integer> colors = new ArrayList<>();
               if (entityCounterShowPlayers) {
                  lines.add("Players: " + players);
                  colors.add(-11141291);
               }

               if (entityCounterShowHostile) {
                  lines.add("Hostile: " + hostile);
                  colors.add(-43691);
               }

               if (entityCounterShowAnimals) {
                  lines.add("Animals: " + animals);
                  colors.add(-171);
               }

               if (entityCounterShowItems) {
                  lines.add("Items: " + items);
                  colors.add(-11141121);
               }

               if (entityCounterShowTotal) {
                  lines.add("Total: " + (players + hostile + animals + items));
                  colors.add(-1);
               }

               if (!lines.isEmpty()) {
                  int maxWidth = 0;

                  for (String line : lines) {
                     int w = client.textRenderer.getWidth(line);
                     if (w > maxWidth) {
                        maxWidth = w;
                     }
                  }

                  int pw = maxWidth + 10;
                  int ph = lines.size() * 10 + 8;
                  context.fill(0, 0, pw, ph, Integer.MIN_VALUE);
                  context.fill(0, 0, pw, 1, getPrimaryColor());
                  context.fill(0, 0, 1, ph, getPrimaryColor());
                  context.fill(pw - 1, 0, pw, ph, getPrimaryColor());
                  context.fill(0, ph - 1, pw, ph, getPrimaryColor());

                  for (int i = 0; i < lines.size(); i++) {
                     context.drawTextWithShadow(client.textRenderer, lines.get(i), 5, 4 + i * 10, colors.get(i));
                  }
               }
            }
         }
      }
   }

   private static final DateTimeFormatter CLOCK_24H = DateTimeFormatter.ofPattern("HH:mm:ss");
   private static final DateTimeFormatter CLOCK_12H = DateTimeFormatter.ofPattern("hh:mm:ss a");
   private static final DateTimeFormatter CLOCK_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   public static String irlClockText() {
      String text = "Time: " + LocalTime.now().format(isIrlClock24Hour ? CLOCK_24H : CLOCK_12H);
      return isIrlDateEnabled ? LocalDate.now().format(CLOCK_DATE) + " " + text : text;
   }

   public static void renderIrlClock(DrawContext context) {
      if (isIrlClockEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               String text = irlClockText();

               int textWidth = client.textRenderer.getWidth(text);
               float scale = irlClockScale;
               int boxWidth = textWidth + 10;
               int boxHeight = 20;
               context.fill(0, 0, boxWidth, boxHeight, Integer.MIN_VALUE);
               context.fill(0, 0, boxWidth, 1, getPrimaryColor());
               context.fill(0, 0, 1, boxHeight, getPrimaryColor());
               context.fill(boxWidth - 1, 0, boxWidth, boxHeight, getPrimaryColor());
               context.fill(0, boxHeight - 1, boxWidth, boxHeight, getPrimaryColor());
               context.drawTextWithShadow(client.textRenderer, text, 5, 5, -1);
            }
         }
      }
   }

   public static void renderTotemCounter(DrawContext context) {
      if (isTotemCounterEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               int totems = 0;

               for (int i = 0; i < client.player.getInventory().size(); i++) {
                  if (client.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                     totems += client.player.getInventory().getStack(i).getCount();
                  }
               }

               String text = "Totems: " + totems;
               int textWidth = client.textRenderer.getWidth(text);
               context.fill(0, 0, textWidth + 10, 20, Integer.MIN_VALUE);
               context.fill(0, 0, textWidth + 10, 1, getPrimaryColor());
               context.fill(0, 0, 1, 20, getPrimaryColor());
               context.fill(textWidth + 9, 0, textWidth + 10, 20, getPrimaryColor());
               context.fill(0, 19, textWidth + 10, 20, getPrimaryColor());
               context.drawTextWithShadow(client.textRenderer, text, 5, 5, -1);
            }
         }
      }
   }

   public static void renderArrowCounter(DrawContext context) {
      if (isArrowCounterEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            if (client.currentScreen == null) {
               int arrows = 0;

               for (int i = 0; i < client.player.getInventory().size(); i++) {
                  Item item = client.player.getInventory().getStack(i).getItem();
                  if (item == Items.ARROW || item == Items.SPECTRAL_ARROW || item == Items.TIPPED_ARROW) {
                     arrows += client.player.getInventory().getStack(i).getCount();
                  }
               }

               String text = "Arrows: " + arrows;
               int textWidth = client.textRenderer.getWidth(text);
               context.fill(0, 0, textWidth + 10, 20, Integer.MIN_VALUE);
               context.fill(0, 0, textWidth + 10, 1, getPrimaryColor());
               context.fill(0, 0, 1, 20, getPrimaryColor());
               context.fill(textWidth + 9, 0, textWidth + 10, 20, getPrimaryColor());
               context.fill(0, 19, textWidth + 10, 20, getPrimaryColor());
               context.drawTextWithShadow(client.textRenderer, text, 5, 5, -1);
            }
         }
      }
   }

   public static void renderPackDisplay(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.currentScreen == null) {
         Collection<String> enabledPacks = client.getResourcePackManager().getEnabledIds();
         List<String> displayPacks = new ArrayList<>();

         for (String id : enabledPacks) {
            if (id.startsWith("file/") || id.equals("programmer_art") || id.equals("high_contrast")) {
               displayPacks.add(id);
            }
         }

         if (displayPacks.isEmpty()) {
            displayPacks.add("Default");
         }

         int maxW = 120;
         int maxTextW = 0;
         List<String> renderedTexts = new ArrayList<>();

         for (String activePack : displayPacks) {
            String packText = activePack;
            if (activePack.startsWith("file/")) {
               packText = activePack.substring(5);
            }

            if (client.textRenderer.getWidth(packText) > maxW) {
               packText = client.textRenderer.trimToWidth(packText, maxW - 10) + "...";
            }

            int w = client.textRenderer.getWidth(packText);
            if (w > maxTextW) {
               maxTextW = w;
            }

            renderedTexts.add(packText);
         }

         int rowHeight = 16;
         int boxH = renderedTexts.size() * rowHeight + 10;
         int boxW = maxTextW + 16 + 10;
         int x = 0;
         int y = 0;
         context.fill(x, y, x + boxW, y + boxH, Integer.MIN_VALUE);
         context.fill(x, y, x + boxW, y + 1, getPrimaryColor());
         context.fill(x, y, x + 1, y + boxH, getPrimaryColor());
         context.fill(x + boxW - 1, y, x + boxW, y + boxH, getPrimaryColor());
         context.fill(x, y + boxH - 1, x + boxW, y + boxH, getPrimaryColor());
         int yOff = y + 5;

         for (String text : renderedTexts) {
            context.drawTextWithShadow(client.textRenderer, "- " + text, x + 5, yOff, -1);
            yOff += rowHeight;
         }
      }
   }

   public static void renderBlockIndicator(DrawContext context) {
      BlockIndicatorManager.render(context);
   }

   public static void renderTpsDisplay(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.currentScreen == null) {
         String tpsText = String.format("TPS: %.1f", currentTps);
         int textWidth = client.textRenderer.getWidth(tpsText);
         int color = -11141291;
         if (currentTps < 18.0) {
            color = -171;
         }

         if (currentTps < 15.0) {
            color = -43691;
         }

         int x = 0;
         int y = 0;
         context.fill(x, y, x + textWidth + 10, y + 20, Integer.MIN_VALUE);
         context.fill(x, y, x + textWidth + 10, y + 1, getPrimaryColor());
         context.fill(x, y, x + 1, y + 20, getPrimaryColor());
         context.fill(x + textWidth + 9, y, x + textWidth + 10, y + 20, getPrimaryColor());
         context.fill(x, y + 19, x + textWidth + 10, y + 20, getPrimaryColor());
         context.drawTextWithShadow(client.textRenderer, tpsText, x + 5, y + 5, color);
      }
   }

   public static void resetAllHudPositions(MinecraftClient client) {
      if (client != null && client.getWindow() != null) {
         int w = client.getWindow().getScaledWidth();
         int midX = w / 2;
         int topY = 50;
         invHudX = midX;
         invHudY = topY;
         fpsCounterX = midX;
         fpsCounterY = topY;
         coordinatesX = midX;
         coordinatesY = topY;
         cpsX = midX;
         cpsY = topY;
         keystrokesX = midX;
         keystrokesY = topY;
         compassX = midX;
         compassY = topY;
         targetHudX = midX;
         targetHudY = topY;
         statsHudX = midX;
         statsHudY = topY;
         armorStatusX = midX;
         armorStatusY = topY;
         comboCounterX = midX;
         comboCounterY = topY;
         pingDisplayX = midX;
         pingDisplayY = topY;
         serverInfoX = midX;
         serverInfoY = topY;
         packDisplayX = midX;
         packDisplayY = topY;
         blockIndicatorX = midX;
         blockIndicatorY = topY;
         tpsDisplayX = midX;
         tpsDisplayY = topY;
         systemResourcesX = midX;
         systemResourcesY = topY;
         stopwatchX = midX;
         stopwatchY = topY;
         watermarkX = midX;
         watermarkY = topY;
         arrayListX = midX;
         arrayListY = topY;
         fpsGraphX = midX;
         fpsGraphY = topY;
         mouseStrokesX = midX;
         mouseStrokesY = topY;
         saveConfig();
      }
   }

   public static void renderDeathInfo(DrawContext context) {
      DeathInfoManager.render(context);
   }

   public static void renderWaypointHud(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         if (client.currentScreen == null) {
            List<WaypointManager.Waypoint> waypoints = WaypointManager.waypoints;
            if (!waypoints.isEmpty()) {
               String currentDim = client.world.getRegistryKey().getValue().getPath();
               int screenW = client.getWindow().getScaledWidth();
               int screenH = client.getWindow().getScaledHeight();
               Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();

               for (WaypointManager.Waypoint wp : waypoints) {
                  if (wp.enabled && wp.dimension.equals(currentDim)) {
                     double wpX = wp.x + 0.5;
                     double wpY = wp.y + 1.05;
                     double wpZ = wp.z + 0.5;
                     double dist = Math.sqrt(
                        (wpX - cameraPos.x) * (wpX - cameraPos.x) + (wpY - cameraPos.y) * (wpY - cameraPos.y) + (wpZ - cameraPos.z) * (wpZ - cameraPos.z)
                     );
                     Vec3d projected = client.gameRenderer.project(new Vec3d(wpX, wpY, wpZ));
                     if (!(projected.z <= 0.0) && !(projected.z > 1.0)) {
                        int screenX = (int)((projected.x * 0.5 + 0.5) * screenW);
                        int screenY = (int)((1.0 - (projected.y * 0.5 + 0.5)) * screenH);
                        if (screenX >= -40 && screenX <= screenW + 40 && screenY >= -40 && screenY <= screenH + 40) {
                           int boxSize = 20;
                           int boxX = screenX - boxSize / 2;
                           int boxY = screenY - boxSize / 2;
                           int tintColor = 0xFF000000 | wp.color & 16777215;
                           boolean useCustomIcon = wp.iconItem != null && !wp.iconItem.isBlank() && !wp.iconItem.equals("minecraft:compass");
                           if (useCustomIcon) {
                              ItemStack iconStack = WaypointManager.getIconStack(wp.iconItem);
                              context.drawItemWithoutEntity(iconStack, screenX - 8, screenY - 8);
                           } else {
                              context.fill(boxX - 1, boxY - 1, boxX + boxSize + 1, boxY + boxSize + 1, -16777216);
                              context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, -1073741824);
                              String letter = wp.name != null && !wp.name.isEmpty() ? String.valueOf(Character.toUpperCase(wp.name.charAt(0))) : "?";
                              int letterW = client.textRenderer.getWidth(Text.literal(letter));
                              context.drawText(client.textRenderer, Text.literal(letter), boxX + (boxSize - letterW) / 2, boxY + 6, -1, false);
                           }

                           if (wp.showLabel) {
                              String distText = "[" + (dist < 1000.0 ? String.format("%.0fm", dist) : String.format("%.1fkm", dist / 1000.0)) + "]";
                              int distW = client.textRenderer.getWidth(Text.literal(distText));
                              int labelY = useCustomIcon ? screenY + 10 : boxY + boxSize + 2;
                              context.drawTextWithShadow(client.textRenderer, Text.literal(distText), screenX - distW / 2, labelY, tintColor);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void renderChatHeads(DrawContext context) {
      if (isChatHeadsEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.world != null) {
            if (client.currentScreen == null) {
               ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
               if (networkHandler != null) {
                  List<PlayerEntity> players = client.world
                     .getEntitiesByClass(PlayerEntity.class, client.player.getBoundingBox().expand(64.0), entity -> entity != client.player);
                  if (!players.isEmpty()) {
                     int x = 0;
                     int y = 0;
                     int iconSize = 24;
                     int gap = 4;
                     int rows = (int)Math.ceil(Math.sqrt(players.size()));
                     int startX = x;
                     int startY = y;
                     context.fill(x, y, x + rows * (iconSize + gap), y + rows * (iconSize + gap), Integer.MIN_VALUE);
                     context.fill(x, y, x + rows * (iconSize + gap), y + 1, getPrimaryColor());
                     context.fill(x, y, x + 1, y + rows * (iconSize + gap), getPrimaryColor());
                     context.fill(x + rows * (iconSize + gap) - 1, y, x + rows * (iconSize + gap), y + rows * (iconSize + gap), getPrimaryColor());
                     context.fill(x, y + rows * (iconSize + gap) - 1, x + rows * (iconSize + gap), y + rows * (iconSize + gap), getPrimaryColor());
                     int idx = 0;

                     for (PlayerEntity player : players) {
                        int row = idx / rows;
                        int col = idx % rows;
                        int iconX = startX + col * (iconSize + gap);
                        int iconY = startY + row * (iconSize + gap);
                        context.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, -2136298838);
                        context.fill(iconX + 2, iconY + 2, iconX + iconSize - 2, iconY + iconSize - 2, -2130706433);
                        String playerName = player.getName().getString();
                        int nameWidth = client.textRenderer.getWidth(playerName);
                        context.drawTextWithShadow(client.textRenderer, playerName, iconX + iconSize / 2 - nameWidth / 2, iconY + iconSize + 2, -1);
                        idx++;
                     }
                  }
               }
            }
         }
      }
   }

   public static void updateMainBackground() {
      if (mainBackground != null) {
         mainBackground.close();
      }

      if (guiBackgroundImagePath != null && !guiBackgroundImagePath.isEmpty()) {
         mainBackground = new AnimatedTexture(guiBackgroundImagePath, "voidcyan", "main_bg");
      } else {
         mainBackground = null;
      }
   }

   public static NativeImage loadAnyImage(String path) throws Exception {
      File file = new File(path);
      if (!file.exists()) {
         throw new FileNotFoundException("File not found: " + path);
      } else {
         try {
            InputStream is = Files.newInputStream(file.toPath());
            NativeImage img = NativeImage.read(is);
            is.close();
            return img;
         } catch (Exception var6) {
            BufferedImage bimg = ImageIO.read(file);
            if (bimg == null) {
               throw new Exception("ImageIO could not read the image");
            } else {
               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               ImageIO.write(bimg, "png", baos);
               byte[] bytes = baos.toByteArray();
               ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
               return NativeImage.read(bais);
            }
         }
      }
   }

   public static class ViewModelSettings {
      public boolean enabled = true;
      public float x = 0.0F;
      public float y = 0.0F;
      public float z = 0.0F;
      public float rotX = 0.0F;
      public float rotY = 0.0F;
      public float rotZ = 0.0F;
      public float scale = 1.0F;

      public String serialize() {
         return this.enabled + "," + this.x + "," + this.y + "," + this.z + "," + this.rotX + "," + this.rotY + "," + this.rotZ + "," + this.scale;
      }

      public static VoidCyanClient.ViewModelSettings deserialize(String s) {
         try {
            String[] p = s.split(",");
            VoidCyanClient.ViewModelSettings v = new VoidCyanClient.ViewModelSettings();
            v.enabled = Boolean.parseBoolean(p[0]);
            v.x = Float.parseFloat(p[1]);
            v.y = Float.parseFloat(p[2]);
            v.z = Float.parseFloat(p[3]);
            v.rotX = Float.parseFloat(p[4]);
            v.rotY = Float.parseFloat(p[5]);
            v.rotZ = Float.parseFloat(p[6]);
            v.scale = Float.parseFloat(p[7]);
            return v;
         } catch (Exception var3) {
            return new VoidCyanClient.ViewModelSettings();
         }
      }
   }
}
