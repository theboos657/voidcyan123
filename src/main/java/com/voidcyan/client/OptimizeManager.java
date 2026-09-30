package com.voidcyan.client;

import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Render culling backing the Optimize tab:
 * - Optimize Items: item entities hidden when solid blocks block the view ray.
 * - Optimize Chests: chest block entities hidden when occluded by solid blocks.
 * - Optimize Signs: sign text not rendered beyond a configurable distance (1-128 blocks).
 * Occlusion results are cached per block position (cleared every second) so
 * raycasts run at most ~2.5 per block per second instead of every frame.
 */
public final class OptimizeManager {
   private OptimizeManager() {}

   public static boolean optimizeItems = true;
   public static boolean optimizeChests = true;
   public static boolean optimizeSigns = true;
   public static boolean optimizePlayers = true;
   public static int signTextDistance = 48;

   private static final java.util.Map<Long, Boolean> OCCLUSION_CACHE = new java.util.HashMap<>();
   private static long lastCacheClear = 0L;

   public static void clearCache() {
      OCCLUSION_CACHE.clear();
   }

   private static void tickCache() {
      long now = System.currentTimeMillis();
      if (now - lastCacheClear > 1000L) {
         lastCacheClear = now;
         OCCLUSION_CACHE.clear();
      }
   }

   private static Vec3d cameraPos() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client == null || client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
         return null;
      }

      Camera camera = client.gameRenderer.getCamera();
      if (camera.getFocusedEntity() != null) {
         return camera.getFocusedEntity().getEyePos();
      }

      return Vec3d.ofCenter(camera.getBlockPos());
   }

   /** True when the given block position can be seen from the camera. */
   public static boolean isBlockVisible(BlockPos pos) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client == null ? null : client.world;
      Vec3d camPos = cameraPos();
      if (world == null || camPos == null) return true;

      tickCache();
      long key = pos.asLong();
      Boolean cached = OCCLUSION_CACHE.get(key);
      if (cached != null) return cached;

      boolean visible = computeVisible(world, pos, camPos);
      OCCLUSION_CACHE.put(key, visible);
      return visible;
   }

   private static boolean computeVisible(ClientWorld world, BlockPos pos, Vec3d camPos) {
      // 1. Walk the block line between the target and the camera block; any fully
      //    opaque block on the way occludes the target.
      BlockPos camBlock = BlockPos.ofFloored(camPos);
      int dx = camBlock.getX() - pos.getX();
      int dy = camBlock.getY() - pos.getY();
      int dz = camBlock.getZ() - pos.getZ();
      int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));

      BlockPos.Mutable cursor = new BlockPos.Mutable();
      for (int i = 1; i < steps; i++) {
         int bx = pos.getX() + dx * i / steps;
         int by = pos.getY() + dy * i / steps;
         int bz = pos.getZ() + dz * i / steps;
         cursor.set(bx, by, bz);
         if (world.getBlockState(cursor).isOpaqueFullCube()) {
            return false;
         }
      }

      // 2. Backstop: raycast from the target to just in front of the camera so
      //    diagonal corners the block walk misses are still detected.
      Vec3d target = Vec3d.ofCenter(pos);
      Vec3d end = camPos.subtract(target.subtract(camPos).normalize().multiply(0.5));
      RaycastContext ctx = new RaycastContext(
         target, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ShapeContext.absent()
      );
      BlockHitResult hit = world.raycast(ctx);
      return hit == null || hit.getType() == HitResult.Type.MISS;
   }

   /** Items: visible unless occluded. */
   public static boolean shouldRenderItem(double x, double y, double z) {
      if (!optimizeItems) return true;
      return isBlockVisible(BlockPos.ofFloored(x, y, z));
   }

   /** Chest-type check usable from a render state (no block entity needed). */
   public static boolean shouldRenderChestAt(BlockPos pos) {
      if (!optimizeChests) return true;
      return isBlockVisible(pos);
   }

   /** Players: visible unless occluded (own player never occluded — sits at the camera). */
   public static boolean shouldRenderPlayer(double x, double y, double z) {
      if (!optimizePlayers) return true;
      // A player counts as visible if their feet, torso or head can be seen.
      return isBlockVisible(BlockPos.ofFloored(x, y, z))
         || isBlockVisible(BlockPos.ofFloored(x, y + 0.9, z))
         || isBlockVisible(BlockPos.ofFloored(x, y + 1.7, z));
   }

   /** Sign text: only rendered within the configured distance (1-128 blocks). */
   public static boolean shouldRenderSignText(BlockPos pos) {
      if (!optimizeSigns || pos == null) return true;
      if (signTextDistance >= 128) return true;
      Vec3d camPos = cameraPos();
      if (camPos == null) return true;
      double dx = pos.getX() + 0.5 - camPos.x;
      double dy = pos.getY() + 0.5 - camPos.y;
      double dz = pos.getZ() + 0.5 - camPos.z;
      if (dx * dx + dy * dy + dz * dz > (double)signTextDistance * (double)signTextDistance) return false;
      return isBlockVisible(pos);
   }
}
