package com.voidcyan.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Single source of truth for module descriptions, shared by the Click GUI and
 * the dropdown GUI so search matches on what a module does, not just its name.
 */
public final class ModuleDescriptions {
   private static final Map<String, String> MAP = new HashMap<>();

   static {
         MAP.put("Inv HUD", "Shows your inventory on the HUD");
         MAP.put("FPS Counter", "Displays current frames per second");
         MAP.put("Coordinates", "Shows your XYZ position and facing");
         MAP.put("CPS Counter", "Tracks left and right clicks per second");
         MAP.put("Keystrokes", "Displays WASD and click key inputs");
         MAP.put("Compass", "Shows a directional compass on the HUD");
         MAP.put("Target HUD", "Displays targeted player info and health");
         MAP.put("Effects", "Crit/fight hit effects — previews in settings");
         MAP.put("Stats HUD", "Tracks kills, deaths, pops and more");
         MAP.put("Armor Status", "Shows armor durability and icons");
         MAP.put("Potion Status", "Displays active potion effects and timers");
         MAP.put("Combo Counter", "Counts consecutive hits on a target");
         MAP.put("Ping Display", "Shows your current server ping");
         MAP.put("Server Info", "Displays server address and version");
         MAP.put("Keybinds Display", "Lists your active module keybinds");
         MAP.put("FreeLook", "Rotate the camera without moving");
         MAP.put("Zoom", "Hold a key to zoom the view in");
         MAP.put("Fullbright", "Removes all darkness from the world");
         MAP.put("Big Items", "Makes selected items on the ground bigger");
         MAP.put("Item Physics", "Dropped items tumble and rotate");
         MAP.put("Low Health Alarm", "Plays a sound when health is critical");
         MAP.put("Pot Warning", "Warns when pots or effects are low");
         MAP.put("Drop Prevention", "Blocks accidental item drops");
         MAP.put("Hit Color", "Flashes a color on hit entities");
         MAP.put("Totem Pop Color", "Highlights players who pop a totem");
         MAP.put("Damage Color", "Tints entities when they take damage");
         MAP.put("Custom Hitbox", "Draws colored outlines around players");
         MAP.put("Damage Hearts", "Shows floating hearts for damage dealt");
         MAP.put("Health Indicators", "Renders health bars above entities");
         MAP.put("Custom Crosshair", "Replaces the default crosshair style");
         MAP.put("Waypoints", "Place and display world waypoints");
         MAP.put("Attack Indicator", "Visual effect on your attack target");
         MAP.put("Custom F3", "Customises which F3 lines are shown");
         MAP.put("Totem Trace", "Draws a line to nearby totems");
         MAP.put("Name Tag Items", "Shows held items, offhand, armor, and totem pops above player nametags");
         MAP.put("Nick Hider", "Hides your username from the tab list");
         MAP.put("Peer Nick", "Assigns nicknames to other players");
         MAP.put("Notifications", "Toast pop-ups for module events");
         MAP.put("Saturation", "Shows food saturation on the HUD");
         MAP.put("Player Trail", "Leaves a fading trail behind you");
         MAP.put("Stopwatch", "In-game stopwatch with HUD display");
         MAP.put("TPS Display", "Shows server ticks per second");
         MAP.put("Watermark", "Displays the VoidCyan logo on screen");
         MAP.put("ArrayList", "Lists all currently enabled modules");
         MAP.put("FPS Graph", "Real-time graph of frame rate history");
         MAP.put("Speed Display", "Shows your current movement speed");
         MAP.put("Biome Display", "Shows the biome you are standing in");
         MAP.put("Entity Counter", "Counts loaded entities around you");
         MAP.put("IRL Clock", "Shows your real-world time in-game");
         MAP.put("Totem Counter", "Tracks totems of undying in inventory");
         MAP.put("Arrow Counter", "Counts arrows in your inventory");
         MAP.put("Reach Display", "Shows the distance of your last hit");
         MAP.put("Pack Display", "Shows active resource pack names");
         MAP.put("Block Info HUD", "Info about the block you are looking at");
         MAP.put("Death Info", "Records cause and location of death");
         MAP.put("System Resources", "Shows CPU and RAM usage on the HUD");
         MAP.put("Toggle Sprint", "Hold sprint key to sprint indefinitely");
         MAP.put("Toggle Sneak", "Press sneak once to stay crouched");
         MAP.put("Time Changer", "Changes the visual sky time client-side");
         MAP.put("TNT Timer", "Counts down time until TNT explodes");
         MAP.put("Custom Text", "Displays a custom text string on the HUD");
         MAP.put("Big Head", "Enlarges player head models");
         MAP.put("Item Animations", "Old-style item swing animations");
         MAP.put("View Model", "Adjusts hand and item view model");
         MAP.put("Block Overlay", "Custom outline, fill and glow for the targeted block");
         MAP.put("China Hat", "Renders a conical hat on players");
         MAP.put("Inv Highlight", "Highlights important items in inventory");
         MAP.put("Mouse Strokes", "Visualises left and right mouse clicks");
         MAP.put("Totem Pop Notifier", "Plays a sound when a totem is used");
         MAP.put("Logout Spots", "Marks where players disconnected");
         MAP.put("Transparent Shield", "Makes the held shield transparent");
   }

   private ModuleDescriptions() {
   }

   /** Description for a module name, or "" when unknown. */
   public static String of(String moduleName) {
      if (moduleName == null) return "";
      String d = MAP.get(moduleName);
      return d == null ? "" : d;
   }

   /** Case-insensitive match of name or description against the query. */
   public static boolean matches(String moduleName, String query) {
      String q = query == null ? "" : query.trim().toLowerCase();
      if (q.isEmpty()) return true;
      if (moduleName != null && moduleName.toLowerCase().contains(q)) return true;
      return of(moduleName).toLowerCase().contains(q);
   }
}
