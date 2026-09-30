package com.voidcyan.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/** World-space drawing for Chunk Borders, Block Overlay and Trajectories (camera-facing ribbons through the debug quad layer). */
public final class FeatureWorld {
   private FeatureWorld() {
   }

   private static MatrixStack.Entry entry;
   private static VertexConsumer buf;
   private static VertexConsumer see;
   private static VertexConsumer cur;
   private static Vec3d cam;

   /** Set when a frame threw, so a bug can never crash the client every frame. */
   private static boolean broken = false;

   public static void render(WorldRenderContext context) {
      if (broken) return;
      boolean any = FeatureModules.on[FeatureModules.CHUNK] || FeatureModules.on[FeatureModules.OVERLAY] || FeatureModules.on[FeatureModules.TRAJ] || FeatureModules.on[FeatureModules.PIXEL];
      if (!any) return;
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null || mc.player == null) return;
      MatrixStack matrices = context.matrices();
      if (matrices == null || context.consumers() == null) return;
      try {
         entry = matrices.peek();
         buf = context.consumers().getBuffer(RenderLayers.debugQuads());
         see = context.consumers().getBuffer(RenderLayers.textBackgroundSeeThrough());
         cur = buf;
         cam = mc.gameRenderer.getCamera().getCameraPos();
         if (FeatureModules.on[FeatureModules.CHUNK]) chunkBorders(mc);
         if (FeatureModules.on[FeatureModules.OVERLAY]) blockOverlay(mc);
         if (FeatureModules.on[FeatureModules.TRAJ]) trajectory(mc);
         if (FeatureModules.on[FeatureModules.PIXEL] && FeatureModules.pixelHighlight) pixelHighlight(mc);
      } catch (Throwable t) {
         broken = true;
         System.err.println("[VoidCyan] Feature world render disabled after error: " + t);
      }
   }

   // ---------------- primitives ----------------

   private static void quad(Vec3d a, Vec3d b, Vec3d c, Vec3d d, int argb) {
      v(a, argb);
      v(b, argb);
      v(c, argb);
      v(d, argb);
      if (cur == see) {
         // The see-through layer culls back faces, so emit the reverse winding as well.
         v(d, argb);
         v(c, argb);
         v(b, argb);
         v(a, argb);
      }
   }

   private static void v(Vec3d p, int argb) {
      cur.vertex(entry, (float) (p.x - cam.x), (float) (p.y - cam.y), (float) (p.z - cam.z));
      cur.color(argb);
      if (cur == see) cur.light(15728880);
   }

   /** A camera-facing ribbon between two world points. */
   private static void edge(Vec3d a, Vec3d b, double thickness, int argb) {
      Vec3d dir = b.subtract(a);
      if (dir.lengthSquared() < 1.0e-9) return;
      Vec3d mid = a.add(b).multiply(0.5).subtract(cam);
      Vec3d side = dir.crossProduct(mid);
      if (side.lengthSquared() < 1.0e-12) return;
      side = side.normalize().multiply(thickness * 0.5);
      quad(a.add(side), b.add(side), b.subtract(side), a.subtract(side), argb);
   }

   private static int argb(int rgb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 0xFFFFFF;
   }

   // ---------------- chunk borders ----------------

   private static void chunkBorders(MinecraftClient mc) {
      int r = Math.max(0, Math.min(4, FeatureModules.chunkRadius));
      int pcx = Math.floorDiv((int) Math.floor(mc.player.getX()), 16);
      int pcz = Math.floorDiv((int) Math.floor(mc.player.getZ()), 16);
      int py = (int) Math.floor(mc.player.getY());
      double y0 = Math.max(mc.world.getBottomY(), py - 40);
      double y1 = Math.min(mc.world.getTopYInclusive() + 1, py + 40);
      int col = FeatureModules.chunkColor;
      double t = 0.03;
      for (int cx = pcx - r; cx <= pcx + r + 1; cx++) {
         for (int cz = pcz - r; cz <= pcz + r + 1; cz++) {
            boolean own = cx >= pcx && cx <= pcx + 1 && cz >= pcz && cz <= pcz + 1;
            edge(new Vec3d(cx * 16.0, y0, cz * 16.0), new Vec3d(cx * 16.0, y1, cz * 16.0), own ? t * 1.6 : t, argb(col, own ? 230 : 120));
         }
      }

      double x0 = pcx * 16.0;
      double z0 = pcz * 16.0;
      int step = FeatureModules.chunkSubchunks ? 16 : 0;
      int from = (int) Math.ceil(y0 / 16.0) * 16;
      if (step > 0) {
         for (int y = from; y <= y1; y += 16) {
            int c = argb(0xFFDD55, 170);
            edge(new Vec3d(x0, y, z0), new Vec3d(x0 + 16, y, z0), t, c);
            edge(new Vec3d(x0 + 16, y, z0), new Vec3d(x0 + 16, y, z0 + 16), t, c);
            edge(new Vec3d(x0 + 16, y, z0 + 16), new Vec3d(x0, y, z0 + 16), t, c);
            edge(new Vec3d(x0, y, z0 + 16), new Vec3d(x0, y, z0), t, c);
         }
      }

      // Own chunk boundary loop at the player's feet level.
      double fy = Math.floor(mc.player.getY());
      int c = argb(col, 220);
      edge(new Vec3d(x0, fy, z0), new Vec3d(x0 + 16, fy, z0), t, c);
      edge(new Vec3d(x0 + 16, fy, z0), new Vec3d(x0 + 16, fy, z0 + 16), t, c);
      edge(new Vec3d(x0 + 16, fy, z0 + 16), new Vec3d(x0, fy, z0 + 16), t, c);
      edge(new Vec3d(x0, fy, z0 + 16), new Vec3d(x0, fy, z0), t, c);
   }

   // ---------------- block overlay ----------------

   /** True when Block Overlay replaces the vanilla outline. */
   public static boolean overlayActive() {
      return FeatureModules.on[FeatureModules.OVERLAY] && !broken;
   }

   private static void blockOverlay(MinecraftClient mc) {
      if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
      World world = mc.world;
      BlockPos pos = hit.getBlockPos();
      BlockState state = world.getBlockState(pos);
      if (state.isAir()) return;
      var shape = state.getOutlineShape(world, pos, ShapeContext.of(mc.player));
      if (shape.isEmpty()) return;
      Box bb = shape.getBoundingBox().offset(pos).expand(0.002);
      boolean fullCube = state.isOpaqueFullCube();
      boolean exposedMode = FeatureModules.overlayMode == 1;

      // exp[axis][side]: side 0 = min face, 1 = max face
      boolean[][] exp = new boolean[3][2];
      for (Direction dir : Direction.values()) {
         int axis = dir.getAxis().ordinal();
         int side = dir.getDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
         boolean open = !exposedMode || !fullCube || !world.getBlockState(pos.offset(dir)).isOpaqueFullCube();
         exp[axis][side] = open;
      }

      double[][] c = {{bb.minX, bb.maxX}, {bb.minY, bb.maxY}, {bb.minZ, bb.maxZ}};

      if (FeatureModules.overlayFill) {
         int fa = FeatureModules.overlayFillAlpha;
         int fc = argb(FeatureModules.overlayFillColor, fa);
         for (int axis = 0; axis < 3; axis++) {
            int u = (axis + 1) % 3;
            int w = (axis + 2) % 3;
            for (int side = 0; side < 2; side++) {
               if (!exp[axis][side]) continue;
               double[] p = new double[3];
               Vec3d[] q = new Vec3d[4];
               int[][] uv = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
               for (int k = 0; k < 4; k++) {
                  p[axis] = c[axis][side];
                  p[u] = c[u][uv[k][0]];
                  p[w] = c[w][uv[k][1]];
                  q[k] = new Vec3d(p[0], p[1], p[2]);
               }

               quad(q[0], q[1], q[2], q[3], fc);
               quad(q[3], q[2], q[1], q[0], fc);
            }
         }
      }

      if (FeatureModules.overlayOutline) {
         double th = FeatureModules.overlayThickness * 0.004;
         int oc = FeatureModules.overlayOutlineColor;
         for (int axis = 0; axis < 3; axis++) {
            int u = (axis + 1) % 3;
            int w = (axis + 2) % 3;
            for (int su = 0; su < 2; su++) {
               for (int sw = 0; sw < 2; sw++) {
                  if (!(exp[u][su] || exp[w][sw])) continue;
                  double[] a = new double[3];
                  double[] b = new double[3];
                  a[axis] = c[axis][0];
                  b[axis] = c[axis][1];
                  a[u] = b[u] = c[u][su];
                  a[w] = b[w] = c[w][sw];
                  Vec3d pa = new Vec3d(a[0], a[1], a[2]);
                  Vec3d pb = new Vec3d(b[0], b[1], b[2]);
                  if (FeatureModules.overlayGlow) {
                     edge(pa, pb, th * 7.0, argb(oc, 26));
                     edge(pa, pb, th * 3.5, argb(oc, 55));
                  }

                  edge(pa, pb, th, argb(oc, 255));
                  // Same edge again through the block (no depth test) so the 3D box shows, including the far edges.
                  cur = see;
                  edge(pa, pb, th, argb(oc, 90));
                  cur = buf;
               }
            }
         }
      }
   }

   // ---------------- pixel look ----------------

   /** Outlines the single texture pixel under the crosshair on the block face. */
   private static void pixelHighlight(MinecraftClient mc) {
      int[] t = FeatureModules.pixelTarget();
      if (t == null || !(mc.crosshairTarget instanceof BlockHitResult hit)) return;
      Direction d = Direction.values()[t[2]];
      BlockPos bp = hit.getBlockPos();
      double u0 = t[0] / 16.0;
      double u1 = (t[0] + 1) / 16.0;
      double v0 = t[1] / 16.0;
      double v1 = (t[1] + 1) / 16.0;
      double off = 0.004;
      Vec3d[] c = new Vec3d[4];
      double[][] uv = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
      for (int k = 0; k < 4; k++) {
         double u = uv[k][0];
         double v = uv[k][1];
         double x;
         double y;
         double z;
         switch (d) {
            case UP -> {
               x = u;
               y = 1 + off;
               z = v;
            }
            case DOWN -> {
               x = u;
               y = -off;
               z = v;
            }
            case NORTH -> {
               x = u;
               y = 1 - v;
               z = -off;
            }
            case SOUTH -> {
               x = u;
               y = 1 - v;
               z = 1 + off;
            }
            case WEST -> {
               x = -off;
               y = 1 - v;
               z = u;
            }
            default -> {
               x = 1 + off;
               y = 1 - v;
               z = u;
            }
         }

         c[k] = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + z);
      }

      int col = argb(0xFFFF55, 255);
      for (int k = 0; k < 4; k++) edge(c[k], c[(k + 1) % 4], 0.012, col);
      quad(c[0], c[1], c[2], c[3], argb(0xFFFF55, 70));
      quad(c[3], c[2], c[1], c[0], argb(0xFFFF55, 70));
   }

   // ---------------- trajectories ----------------

   private static void trajectory(MinecraftClient mc) {
      var p = mc.player;
      ItemStack main = p.getMainHandStack();
      ItemStack off = p.getOffHandStack();
      ItemStack st = null;
      double speed = 0.0;
      double gravity = 0.03;
      double pitchOffset = 0.0;
      for (ItemStack s : new ItemStack[]{main, off}) {
         if (s.isEmpty()) continue;
         if (FeatureModules.trajBow && s.isOf(Items.BOW) && p.isUsingItem() && p.getActiveItem() == s) {
            float pull = net.minecraft.item.BowItem.getPullProgress(p.getItemUseTime());
            if (pull < 0.1F) continue;
            st = s;
            speed = pull * 3.0F;
            gravity = 0.05;
         } else if (FeatureModules.trajBow && s.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(s)) {
            st = s;
            speed = 3.15;
            gravity = 0.05;
         } else if (FeatureModules.trajTrident && s.isOf(Items.TRIDENT) && p.isUsingItem() && p.getActiveItem() == s && p.getItemUseTime() >= 10) {
            st = s;
            speed = 2.5;
            gravity = 0.05;
         } else if (FeatureModules.trajPearl && s.isOf(Items.ENDER_PEARL)) {
            st = s;
            speed = 1.5;
         } else if (FeatureModules.trajThrowables && (s.isOf(Items.SNOWBALL) || s.isOf(Items.EGG))) {
            st = s;
            speed = 1.5;
         } else if (FeatureModules.trajThrowables && (s.isOf(Items.SPLASH_POTION) || s.isOf(Items.LINGERING_POTION))) {
            st = s;
            speed = 0.5;
            gravity = 0.05;
            pitchOffset = -20.0;
         } else if (FeatureModules.trajThrowables && s.isOf(Items.EXPERIENCE_BOTTLE)) {
            st = s;
            speed = 0.7;
            gravity = 0.07;
            pitchOffset = -20.0;
         }

         if (st != null) break;
      }

      if (st == null) return;
      float pitch = p.getPitch() + (float) pitchOffset;
      float yaw = p.getYaw();
      double cy = Math.cos(Math.toRadians(-yaw - 180.0F * 0.0F));
      double vx = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
      double vy = -Math.sin(Math.toRadians(pitch));
      double vz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
      Vec3d vel = new Vec3d(vx, vy, vz).normalize().multiply(speed);
      Vec3d pos = p.getEyePos().add(0.0, -0.1, 0.0);
      int col = FeatureModules.trajColor;
      Vec3d prev = pos;
      Vec3d end = null;
      for (int i = 0; i < 240; i++) {
         Vec3d next = pos.add(vel);
         BlockHitResult hit = mc.world.raycast(new RaycastContext(pos, next, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
         boolean stop = hit.getType() != HitResult.Type.MISS;
         if (stop) next = hit.getPos();
         if (i > 0) edge(prev, next, 0.03, argb(col, 200));
         prev = next;
         if (stop) {
            end = next;
            break;
         }

         pos = next;
         boolean water = mc.world.getFluidState(BlockPos.ofFloored(pos)).isIn(net.minecraft.registry.tag.FluidTags.WATER);
         vel = vel.multiply(water ? 0.8 : 0.99).add(0.0, -gravity, 0.0);
         if (pos.y < mc.world.getBottomY() - 8) break;
      }

      if (end != null) {
         double s = 0.18;
         int c2 = argb(col, 255);
         edge(end.add(-s, 0.02, 0), end.add(s, 0.02, 0), 0.04, c2);
         edge(end.add(0, 0.02, -s), end.add(0, 0.02, s), 0.04, c2);
      }
   }
}
