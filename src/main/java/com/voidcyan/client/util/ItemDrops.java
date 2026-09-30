package com.voidcyan.client.util;

import java.util.HashMap;
import java.util.Map;

public final class ItemDrops {
   private static final Map<String, String> DROPS = new HashMap<>();

   static {
      String[][] t = {
         {"rotten_flesh", "Zombie, Husk, Drowned, Zombie Villager"}, {"bone", "Skeleton, Stray, Bogged, Skeleton Horse"},
         {"arrow", "Skeleton, Stray, Bogged"}, {"string", "Spider, Cave Spider"}, {"spider_eye", "Spider, Cave Spider, Witch"},
         {"gunpowder", "Creeper, Ghast, Witch"}, {"ender_pearl", "Enderman"}, {"blaze_rod", "Blaze"}, {"ghast_tear", "Ghast"},
         {"slime_ball", "Slime"}, {"magma_cream", "Magma Cube"}, {"phantom_membrane", "Phantom"},
         {"leather", "Cow, Horse, Mule, Donkey, Llama"}, {"beef", "Cow, Mooshroom"}, {"porkchop", "Pig, Hoglin"},
         {"chicken", "Chicken"}, {"feather", "Chicken, Parrot"}, {"mutton", "Sheep"}, {"rabbit", "Rabbit"},
         {"rabbit_hide", "Rabbit"}, {"rabbit_foot", "Rabbit"}, {"ink_sac", "Squid"}, {"glow_ink_sac", "Glow Squid"},
         {"prismarine_shard", "Guardian, Elder Guardian"}, {"prismarine_crystals", "Guardian, Elder Guardian"},
         {"nautilus_shell", "Drowned"}, {"trident", "Drowned"}, {"copper_ingot", "Drowned"},
         {"gold_nugget", "Zombified Piglin"}, {"gold_ingot", "Zombified Piglin"}, {"iron_ingot", "Zombie, Husk, Iron Golem"},
         {"carrot", "Zombie, Husk"}, {"potato", "Zombie, Husk"}, {"totem_of_undying", "Evoker"}, {"emerald", "Villager Raid, Evoker, Vindicator, Pillager"},
         {"shulker_shell", "Shulker"}, {"nether_star", "Wither"}, {"dragon_breath", "Ender Dragon"}, {"dragon_egg", "Ender Dragon"},
         {"wither_skeleton_skull", "Wither Skeleton"}, {"coal", "Wither Skeleton"}, {"cod", "Cod"}, {"salmon", "Salmon"},
         {"tropical_fish", "Tropical Fish"}, {"pufferfish", "Pufferfish"}, {"scute", "Turtle (grown baby)"},
         {"armadillo_scute", "Armadillo"}, {"breeze_rod", "Breeze"}, {"honeycomb", "Bee Nest (shearing)"}, {"crossbow", "Pillager"},
         {"goat_horn", "Goat"}, {"sniffer_egg", "Sniffer"}, {"experience_bottle", "Villager (Cleric trade)"},
         {"white_wool", "Sheep"}, {"lead", "Wandering Trader"}, {"saddle", "Chests, Strider"}, {"zombie_head", "Charged Creeper kill"},
         {"creeper_head", "Charged Creeper kill"}, {"skeleton_skull", "Charged Creeper kill"}, {"netherite_scrap", "Ancient Debris (smelted)"},
      };
      for (String[] e : t) DROPS.put(e[0], e[1]);
   }

   private ItemDrops() {
   }

   public static String get(String itemPath) {
      return DROPS.get(itemPath);
   }
}
