package com.voidcyan.client.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.util.Identifier;

public class TexturePackManager {
   public static final String PACK_NAME = "VoidCyanPack";
   public static final String PACK_ID = "file/" + PACK_NAME;

   public static Path getPackDir() {
      MinecraftClient client = MinecraftClient.getInstance();
      Path packsDir = client.getResourcePackDir();
      if (packsDir == null && client.runDirectory != null) {
         packsDir = client.runDirectory.toPath().resolve("resourcepacks");
      }
      return packsDir != null ? packsDir.resolve(PACK_NAME) : null;
   }

   public static void ensurePackExists() {
      Path packDir = getPackDir();
      if (packDir == null) return;
      try {
         Files.createDirectories(packDir);
         Path mcmeta = packDir.resolve("pack.mcmeta");
         if (!Files.exists(mcmeta)) {
            String json = "{\n  \"pack\": {\n    \"pack_format\": 34,\n    \"description\": \"VoidCyan Custom Textures\"\n  }\n}";
            Files.writeString(mcmeta, json);
         }
         Files.createDirectories(packDir.resolve("assets/minecraft/textures/item"));
         Files.createDirectories(packDir.resolve("assets/minecraft/textures/block"));
         Files.createDirectories(packDir.resolve("assets/minecraft/textures/entity"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static boolean hasCustomTexture(TextureTarget target) {
      if (target == null) return false;
      Path packDir = getPackDir();
      if (packDir == null) return false;
      for (Identifier id : target.getCandidatePaths()) {
         Path file = packDir.resolve("assets/" + id.getNamespace() + "/" + id.getPath());
         if (Files.exists(file)) return true;
      }
      return false;
   }

   public static BufferedImage loadCustomTexture(TextureTarget target) {
      if (target == null) return null;
      Path packDir = getPackDir();
      if (packDir == null) return null;
      for (Identifier id : target.getCandidatePaths()) {
         Path file = packDir.resolve("assets/" + id.getNamespace() + "/" + id.getPath());
         if (Files.exists(file)) {
            try {
               BufferedImage img = ImageIO.read(file.toFile());
               if (img != null) return img;
            } catch (Exception ignored) {}
         }
      }
      return null;
   }

   public static BufferedImage loadVanillaTexture(TextureTarget target) {
      if (target == null) return null;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.getResourceManager() == null) return null;

      for (Identifier candidate : target.getCandidatePaths()) {
         try {
            Optional<Resource> res = client.getResourceManager().getResource(candidate);
            if (res.isPresent()) {
               try (InputStream is = res.get().getInputStream()) {
                  BufferedImage img = ImageIO.read(is);
                  if (img != null) return img;
               }
            }
         } catch (Exception ignored) {}
      }
      return null;
   }

   public static void saveCustomTexture(TextureTarget target, BufferedImage img) {
      if (target == null || img == null) return;
      ensurePackExists();
      Path packDir = getPackDir();
      if (packDir == null) return;

      for (Identifier id : target.getCandidatePaths()) {
         try {
            Path file = packDir.resolve("assets/" + id.getNamespace() + "/" + id.getPath());
            Files.createDirectories(file.getParent());
            ImageIO.write(img, "PNG", file.toFile());
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public static void deleteCustomTexture(TextureTarget target) {
      if (target == null) return;
      Path packDir = getPackDir();
      if (packDir == null) return;

      for (Identifier id : target.getCandidatePaths()) {
         try {
            Path file = packDir.resolve("assets/" + id.getNamespace() + "/" + id.getPath());
            Files.deleteIfExists(file);
         } catch (Exception ignored) {}
      }
   }

   // --- Legacy item delegates for backward compatibility ---
   public static boolean hasCustomTexture(Item item) {
      return item != null && hasCustomTexture(TextureTarget.fromItem(item));
   }

   public static BufferedImage loadCustomTexture(Item item) {
      return item != null ? loadCustomTexture(TextureTarget.fromItem(item)) : null;
   }

   public static BufferedImage loadVanillaTexture(Item item) {
      return item != null ? loadVanillaTexture(TextureTarget.fromItem(item)) : null;
   }

   public static void saveCustomTexture(Item item, BufferedImage img) {
      if (item != null) saveCustomTexture(TextureTarget.fromItem(item), img);
   }

   public static void deleteCustomTexture(Item item) {
      if (item != null) deleteCustomTexture(TextureTarget.fromItem(item));
   }

   public static void deleteAllCustomTextures() {
      Path packDir = getPackDir();
      if (packDir == null) return;
      try {
         Path texturesDir = packDir.resolve("assets/minecraft/textures");
         if (Files.exists(texturesDir)) {
            try (var stream = Files.walk(texturesDir)) {
               stream.sorted(Comparator.reverseOrder())
                     .filter(p -> !p.equals(texturesDir))
                     .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                     });
            }
         }
      } catch (Exception ignored) {}
   }

   public static void applyAndReload() {
      MinecraftClient client = MinecraftClient.getInstance();
      ensurePackExists();

      // Debounce: coalesce bursts of reload requests (save + revert + revert-all in quick
      // succession) into a single resource reload, and defer it off the click handler so
      // the screen stays responsive. reloadResources() itself is async; the heavy part
      // was being triggered synchronously on every Save click.
      if (reloadPending) return;
      reloadPending = true;
      client.execute(() -> {
         reloadPending = false;
         try {
            ResourcePackManager manager = client.getResourcePackManager();
            manager.scanPacks();
            if (!manager.enable(PACK_ID)) {
               Collection<String> enabled = new ArrayList<>(manager.getEnabledIds());
               if (!enabled.contains(PACK_ID)) {
                  enabled.add(PACK_ID);
                  manager.setEnabledProfiles(enabled);
               }
            }
            client.reloadResources();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });
   }

   private static volatile boolean reloadPending = false;

   public static BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight) {
      if (source == null) return new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
      if (source.getWidth() == targetWidth && source.getHeight() == targetHeight) return source;
      BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = resized.createGraphics();
      g.drawImage(source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_FAST), 0, 0, null);
      g.dispose();
      return resized;
   }
}
