package com.voidcyan.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;

public class TextureTarget {
   public enum Category {
      ALL("All"),
      BLOCKS("Blocks"),
      ITEMS("Items"),
      MOBS("Mobs"),
      TOOLS("Tools"),
      FOOD("Food");

      private final String label;
      Category(String label) { this.label = label; }
      public String getLabel() { return this.label; }
   }

   private final String id;
   private final String displayName;
   private final ItemStack icon;
   private final Category category;
   private final List<Identifier> candidatePaths;
   private final boolean isMob;
   private final int defaultWidth;
   private final int defaultHeight;
   // For mob targets: the EntityType used to spawn a preview entity for 3D rendering
   private final EntityType<? extends LivingEntity> entityType;

   public TextureTarget(String id, String displayName, ItemStack icon, Category category, List<Identifier> candidatePaths, boolean isMob, int defaultWidth, int defaultHeight) {
      this(id, displayName, icon, category, candidatePaths, isMob, defaultWidth, defaultHeight, null);
   }

   @SuppressWarnings("unchecked")
   public TextureTarget(String id, String displayName, ItemStack icon, Category category, List<Identifier> candidatePaths, boolean isMob, int defaultWidth, int defaultHeight, EntityType<? extends LivingEntity> entityType) {
      this.id = id;
      this.displayName = displayName;
      this.icon = icon;
      this.category = category;
      this.candidatePaths = Collections.unmodifiableList(candidatePaths);
      this.isMob = isMob;
      this.defaultWidth = defaultWidth;
      this.defaultHeight = defaultHeight;
      this.entityType = entityType;
   }

   public String getId() { return this.id; }
   public String getDisplayName() { return this.displayName; }
   public ItemStack getIcon() { return this.icon; }
   public Category getCategory() { return this.category; }
   public List<Identifier> getCandidatePaths() { return this.candidatePaths; }
   public boolean isMob() { return this.isMob; }
   public int getDefaultWidth() { return this.defaultWidth; }
   public int getDefaultHeight() { return this.defaultHeight; }
   public EntityType<? extends LivingEntity> getEntityType() { return this.entityType; }

   /** Create a fresh preview entity for 3D rendering in the GUI, or null if not a mob target. */
   public LivingEntity createPreviewEntity() {
      if (this.entityType == null || net.minecraft.client.MinecraftClient.getInstance().world == null) return null;
      try {
         return this.entityType.create(net.minecraft.client.MinecraftClient.getInstance().world, SpawnReason.COMMAND);
      } catch (Exception e) {
         return null;
      }
   }

   public static TextureTarget fromItem(Item item) {
      Identifier id = Registries.ITEM.getId(item);
      String path = id.getPath();
      String ns = id.getNamespace();

      List<Identifier> candidates = new ArrayList<>();
      candidates.add(Identifier.of(ns, "textures/item/" + path + ".png"));
      if (item instanceof BlockItem) {
         candidates.add(Identifier.of(ns, "textures/block/" + path + ".png"));
      }
      candidates.add(Identifier.of("minecraft", "textures/item/" + path + ".png"));
      candidates.add(Identifier.of("minecraft", "textures/block/" + path + ".png"));

      Category cat = determineItemCategory(item);
      return new TextureTarget("item:" + id, item.getName().getString(), new ItemStack(item), cat, candidates, false, 16, 16);
   }

   public static Category determineItemCategory(Item item) {
      if (item instanceof BlockItem) return Category.BLOCKS;
      ItemStack stack = new ItemStack(item);
      if (stack.contains(DataComponentTypes.FOOD) || stack.contains(DataComponentTypes.POTION_CONTENTS)) {
         return Category.FOOD;
      }
      String path = Registries.ITEM.getId(item).getPath();
      if (stack.contains(DataComponentTypes.TOOL) || path.contains("pickaxe") || path.contains("axe")
            || path.contains("shovel") || path.contains("hoe") || path.contains("fishing_rod")
            || path.contains("shears") || path.contains("flint_and_steel") || path.contains("brush")
            || path.contains("compass") || path.contains("clock") || path.contains("spyglass") || path.contains("lead")) {
         return Category.TOOLS;
      }
      return Category.ITEMS;
   }

   public static List<TextureTarget> buildMobTargets() {
      List<TextureTarget> mobs = new ArrayList<>();

      addMob(mobs, "creeper",          "Creeper",          Items.CREEPER_SPAWN_EGG,          EntityType.CREEPER,          "textures/entity/creeper/creeper.png",        64, 64);
      addMob(mobs, "zombie",           "Zombie",           Items.ZOMBIE_SPAWN_EGG,           EntityType.ZOMBIE,           "textures/entity/zombie/zombie.png",          64, 64);
      addMob(mobs, "skeleton",         "Skeleton",         Items.SKELETON_SPAWN_EGG,         EntityType.SKELETON,         "textures/entity/skeleton/skeleton.png",      64, 64);
      addMob(mobs, "spider",           "Spider",           Items.SPIDER_SPAWN_EGG,           EntityType.SPIDER,           "textures/entity/spider/spider.png",          64, 32);
      addMob(mobs, "cave_spider",      "Cave Spider",      Items.CAVE_SPIDER_SPAWN_EGG,      EntityType.CAVE_SPIDER,      "textures/entity/spider/cave_spider.png",     64, 32);
      addMob(mobs, "enderman",         "Enderman",         Items.ENDERMAN_SPAWN_EGG,         EntityType.ENDERMAN,         "textures/entity/enderman/enderman.png",      64, 64);
      addMob(mobs, "pig",              "Pig",              Items.PIG_SPAWN_EGG,              EntityType.PIG,              "textures/entity/pig/temperate_pig.png",      "textures/entity/pig/pig.png",      64, 32);
      addMob(mobs, "cow",              "Cow",              Items.COW_SPAWN_EGG,              EntityType.COW,              "textures/entity/cow/temperate_cow.png",      "textures/entity/cow/cow.png",      64, 32);
      addMob(mobs, "red_mooshroom",    "Mooshroom",        Items.MOOSHROOM_SPAWN_EGG,        EntityType.MOOSHROOM,        "textures/entity/cow/red_mooshroom.png",      64, 32);
      addMob(mobs, "sheep",            "Sheep",            Items.SHEEP_SPAWN_EGG,            EntityType.SHEEP,            "textures/entity/sheep/sheep.png",            64, 32);
      addMob(mobs, "chicken",          "Chicken",          Items.CHICKEN_SPAWN_EGG,          EntityType.CHICKEN,          "textures/entity/chicken/temperate_chicken.png", "textures/entity/chicken/chicken.png", 64, 32);
      addMob(mobs, "iron_golem",       "Iron Golem",       Items.IRON_GOLEM_SPAWN_EGG,       EntityType.IRON_GOLEM,       "textures/entity/iron_golem/iron_golem.png",  128, 128);
      addMob(mobs, "snow_golem",       "Snow Golem",       Items.SNOW_GOLEM_SPAWN_EGG,       EntityType.SNOW_GOLEM,       "textures/entity/snow_golem.png",             64, 64);
      addMob(mobs, "wolf",             "Wolf / Dog",       Items.WOLF_SPAWN_EGG,             EntityType.WOLF,             "textures/entity/wolf/wolf.png",              64, 32);
      addMob(mobs, "cat",              "Cat",              Items.CAT_SPAWN_EGG,              EntityType.CAT,              "textures/entity/cat/all_black.png",          64, 32);
      addMob(mobs, "warden",           "Warden",           Items.WARDEN_SPAWN_EGG,           EntityType.WARDEN,           "textures/entity/warden/warden.png",          128, 64);
      addMob(mobs, "wither",           "Wither",           Items.WITHER_SPAWN_EGG,           EntityType.WITHER,           "textures/entity/wither/wither.png",          64, 64);
      addMob(mobs, "ender_dragon",     "Ender Dragon",     Items.ENDER_DRAGON_SPAWN_EGG,     EntityType.ENDER_DRAGON,     "textures/entity/enderdragon/dragon.png",     128, 128);
      addMob(mobs, "blaze",            "Blaze",            Items.BLAZE_SPAWN_EGG,            EntityType.BLAZE,            "textures/entity/blaze.png",                  64, 32);
      addMob(mobs, "slime",            "Slime",            Items.SLIME_SPAWN_EGG,            EntityType.SLIME,            "textures/entity/slime/slime.png",            64, 32);
      addMob(mobs, "magma_cube",       "Magma Cube",       Items.MAGMA_CUBE_SPAWN_EGG,       EntityType.MAGMA_CUBE,       "textures/entity/slime/magmacube.png",        64, 32);
      addMob(mobs, "ghast",            "Ghast",            Items.GHAST_SPAWN_EGG,            EntityType.GHAST,            "textures/entity/ghast/ghast.png",            64, 32);
      addMob(mobs, "phantom",          "Phantom",          Items.PHANTOM_SPAWN_EGG,          EntityType.PHANTOM,          "textures/entity/phantom.png",                64, 64);
      addMob(mobs, "bee",              "Bee",              Items.BEE_SPAWN_EGG,              EntityType.BEE,              "textures/entity/bee/bee.png",                64, 32);
      addMob(mobs, "axolotl",          "Axolotl",          Items.AXOLOTL_SPAWN_EGG,          EntityType.AXOLOTL,          "textures/entity/axolotl/axolotl_lucy.png",   64, 64);
      addMob(mobs, "allay",            "Allay",            Items.ALLAY_SPAWN_EGG,            EntityType.ALLAY,            "textures/entity/allay/allay.png",            32, 32);
      addMob(mobs, "breeze",           "Breeze",           Items.BREEZE_SPAWN_EGG,           EntityType.BREEZE,           "textures/entity/breeze/breeze.png",          64, 64);
      addMob(mobs, "creaking",         "Creaking",         Items.CREAKING_SPAWN_EGG,         EntityType.CREAKING,         "textures/entity/creaking/creaking.png",      64, 64);
      addMob(mobs, "armadillo",        "Armadillo",        Items.ARMADILLO_SPAWN_EGG,        EntityType.ARMADILLO,        "textures/entity/armadillo.png",              64, 64);
      addMob(mobs, "sniffer",          "Sniffer",          Items.SNIFFER_SPAWN_EGG,          EntityType.SNIFFER,          "textures/entity/sniffer/sniffer.png",        128, 128);
      addMob(mobs, "camel",            "Camel",            Items.CAMEL_SPAWN_EGG,            EntityType.CAMEL,            "textures/entity/camel/camel.png",            128, 128);
      addMob(mobs, "horse",            "Horse",            Items.HORSE_SPAWN_EGG,            EntityType.HORSE,            "textures/entity/horse/horse_brown.png",      128, 128);
      addMob(mobs, "goat",             "Goat",             Items.GOAT_SPAWN_EGG,             EntityType.GOAT,             "textures/entity/goat/goat.png",              64, 64);
      addMob(mobs, "fox",              "Fox",              Items.FOX_SPAWN_EGG,              EntityType.FOX,              "textures/entity/fox/fox.png",                64, 32);
      addMob(mobs, "frog",             "Frog",             Items.FROG_SPAWN_EGG,             EntityType.FROG,             "textures/entity/frog/temperate_frog.png",    64, 32);
      addMob(mobs, "squid",            "Squid",            Items.SQUID_SPAWN_EGG,            EntityType.SQUID,            "textures/entity/squid/squid.png",            64, 32);
      addMob(mobs, "glow_squid",       "Glow Squid",       Items.GLOW_SQUID_SPAWN_EGG,       EntityType.GLOW_SQUID,       "textures/entity/squid/glow_squid.png",       64, 32);
      addMob(mobs, "dolphin",          "Dolphin",          Items.DOLPHIN_SPAWN_EGG,          EntityType.DOLPHIN,          "textures/entity/dolphin.png",                64, 64);
      addMob(mobs, "turtle",           "Sea Turtle",       Items.TURTLE_SPAWN_EGG,           EntityType.TURTLE,           "textures/entity/turtle/sea_turtle.png",      128, 64);
      addMob(mobs, "panda",            "Panda",            Items.PANDA_SPAWN_EGG,            EntityType.PANDA,            "textures/entity/panda/panda.png",            64, 64);
      addMob(mobs, "parrot",           "Parrot",           Items.PARROT_SPAWN_EGG,           EntityType.PARROT,           "textures/entity/parrot/parrot_red_blue.png", 32, 32);
      addMob(mobs, "villager",         "Villager",         Items.VILLAGER_SPAWN_EGG,         EntityType.VILLAGER,         "textures/entity/villager/villager.png",      64, 64);
      addMob(mobs, "witch",            "Witch",            Items.WITCH_SPAWN_EGG,            EntityType.WITCH,            "textures/entity/witch.png",                  64, 64);
      addMob(mobs, "pillager",         "Pillager",         Items.PILLAGER_SPAWN_EGG,         EntityType.PILLAGER,         "textures/entity/illager/pillager.png",       64, 64);
      addMob(mobs, "vindicator",       "Vindicator",       Items.VINDICATOR_SPAWN_EGG,       EntityType.VINDICATOR,       "textures/entity/illager/vindicator.png",     64, 64);
      addMob(mobs, "evoker",           "Evoker",           Items.EVOKER_SPAWN_EGG,           EntityType.EVOKER,           "textures/entity/illager/evoker.png",         64, 64);
      addMob(mobs, "ravager",          "Ravager",          Items.RAVAGER_SPAWN_EGG,          EntityType.RAVAGER,          "textures/entity/illager/ravager.png",        128, 128);
      addMob(mobs, "piglin",           "Piglin",           Items.PIGLIN_SPAWN_EGG,           EntityType.PIGLIN,           "textures/entity/piglin/piglin.png",          64, 64);
      addMob(mobs, "piglin_brute",     "Piglin Brute",     Items.PIGLIN_BRUTE_SPAWN_EGG,     EntityType.PIGLIN_BRUTE,     "textures/entity/piglin/piglin_brute.png",    64, 64);
      addMob(mobs, "zombified_piglin", "Zombified Piglin", Items.ZOMBIFIED_PIGLIN_SPAWN_EGG, EntityType.ZOMBIFIED_PIGLIN, "textures/entity/piglin/zombified_piglin.png", 64, 64);
      addMob(mobs, "hoglin",           "Hoglin",           Items.HOGLIN_SPAWN_EGG,           EntityType.HOGLIN,           "textures/entity/hoglin/hoglin.png",          128, 64);
      addMob(mobs, "shulker",          "Shulker",          Items.SHULKER_SPAWN_EGG,          EntityType.SHULKER,          "textures/entity/shulker/shulker.png",        64, 64);
      addMob(mobs, "strider",          "Strider",          Items.STRIDER_SPAWN_EGG,          EntityType.STRIDER,          "textures/entity/strider/strider.png",        64, 64);
      addMob(mobs, "rabbit",           "Rabbit",           Items.RABBIT_SPAWN_EGG,           EntityType.RABBIT,           "textures/entity/rabbit/brown.png",           64, 64);
      addMob(mobs, "bat",              "Bat",              Items.BAT_SPAWN_EGG,              EntityType.BAT,              "textures/entity/bat.png",                    64, 64);
      addMob(mobs, "silverfish",       "Silverfish",       Items.SILVERFISH_SPAWN_EGG,       EntityType.SILVERFISH,       "textures/entity/silverfish.png",             64, 32);
      addMob(mobs, "endermite",        "Endermite",        Items.ENDERMITE_SPAWN_EGG,        EntityType.ENDERMITE,        "textures/entity/endermite.png",              64, 32);
      addMob(mobs, "wither_skeleton",  "Wither Skeleton",  Items.WITHER_SKELETON_SPAWN_EGG,  EntityType.WITHER_SKELETON,  "textures/entity/skeleton/wither_skeleton.png", 64, 64);
      addMob(mobs, "husk",             "Husk",             Items.HUSK_SPAWN_EGG,             EntityType.HUSK,             "textures/entity/zombie/husk.png",            64, 64);
      addMob(mobs, "drowned",          "Drowned",          Items.DROWNED_SPAWN_EGG,          EntityType.DROWNED,          "textures/entity/zombie/drowned.png",         64, 64);
      addMob(mobs, "stray",            "Stray",            Items.STRAY_SPAWN_EGG,            EntityType.STRAY,            "textures/entity/skeleton/stray.png",         64, 64);
      addMob(mobs, "bogged",           "Bogged",           Items.BOGGED_SPAWN_EGG,           EntityType.BOGGED,           "textures/entity/skeleton/bogged.png",        64, 64);
      // Player skin targets have no EntityType (use the local player entity instead)
      addMob(mobs, "steve", "Player (Steve)", Items.PLAYER_HEAD, "textures/entity/player/wide/steve.png",  64, 64);
      addMob(mobs, "alex",  "Player (Alex)",  Items.PLAYER_HEAD, "textures/entity/player/slim/alex.png",   64, 64);

      return mobs;
   }

   private static void addMob(List<TextureTarget> list, String mobId, String name, Item egg, EntityType<? extends LivingEntity> entityType, int w, int h, String... paths) {
      List<Identifier> ids = new ArrayList<>(paths.length);
      for (String p : paths) ids.add(Identifier.of("minecraft", p));
      list.add(new TextureTarget("mob:" + mobId, name, new ItemStack(egg), Category.MOBS, ids, true, w, h, entityType));
   }

   private static void addMob(List<TextureTarget> list, String mobId, String name, Item egg, String path, int w, int h) {
      addMob(list, mobId, name, egg, null, w, h, path);
   }

   private static void addMob(List<TextureTarget> list, String mobId, String name, Item egg, String path1, String path2, int w, int h) {
      addMob(list, mobId, name, egg, null, w, h, path1, path2);
   }

   private static <E extends LivingEntity> void addMob(List<TextureTarget> list, String mobId, String name, Item egg, EntityType<E> entityType, String path, int w, int h) {
      addMob(list, mobId, name, egg, (EntityType<? extends LivingEntity>)entityType, w, h, path);
   }

   private static <E extends LivingEntity> void addMob(List<TextureTarget> list, String mobId, String name, Item egg, EntityType<E> entityType, String path1, String path2, int w, int h) {
      addMob(list, mobId, name, egg, (EntityType<? extends LivingEntity>)entityType, w, h, path1, path2);
   }

   public static List<TextureTarget> buildArmorTargets() {
      List<TextureTarget> list = new ArrayList<>();
      Object[][] sets = {
         {"diamond", "Diamond", Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS},
         {"netherite", "Netherite", Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS},
         {"iron", "Iron", Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS},
         {"gold", "Gold", Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS},
         {"chainmail", "Chainmail", Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS},
         {"leather", "Leather", Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS},
         {"copper", "Copper", Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS},
      };
      for (Object[] s : sets) {
         String id = (String)s[0], name = (String)s[1];
         addArmor(list, "armor:" + id + "_body", name + " Armor (Body)", (Item)s[2], "textures/entity/equipment/humanoid/" + id + ".png", 64, 32);
         addArmor(list, "armor:" + id + "_legs", name + " Armor (Leggings)", (Item)s[3], "textures/entity/equipment/humanoid_leggings/" + id + ".png", 64, 32);
      }
      addArmor(list, "armor:turtle_body", "Turtle Shell (Body)", Items.TURTLE_HELMET, "textures/entity/equipment/humanoid/turtle_scute.png", 64, 32);
      return list;
   }

   private static void addArmor(List<TextureTarget> list, String id, String name, Item icon, String path, int w, int h) {
      list.add(new TextureTarget(id, name, new ItemStack(icon), Category.ITEMS, List.of(Identifier.of("minecraft", path)), false, w, h));
   }
}

