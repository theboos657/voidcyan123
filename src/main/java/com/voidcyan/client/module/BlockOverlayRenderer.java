package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.joml.Matrix4f;

/**
 * Block Overlay: replaces the vanilla black block outline with a customizable one.
 *
 * Modes:
 *  - Full Block:  every edge (and, with Fill, every face) of the targeted block.
 *  - Air Exposed: only the faces that touch air/non-solid neighbours, and only the edges
 *                 bordering such a face, so buried sides stay clean.
 */
public final class BlockOverlayRenderer {
   public static final int MODE_FULL = 0;
   public static final int MODE_AIR_EXPOSED = 1;
   private static final double EPS = 0.002;

   private BlockOverlayRenderer() {
   }

   /** True when vanilla's outline should be suppressed for the current target. */
   public static boolean shouldReplaceVanilla(HitResult hit) {
      return VoidCyanClient.isBlockOverlayEnabled && hit != null && hit.getType() == HitResult.Type.BLOCK;
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world == null || client.player == null) return;
      if (!(client.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
      VertexConsumerProvider consumers = context.consumers();
      if (consumers == null) return;
      if (!VoidCyanClient.blockOverlayOutline && !VoidCyanClient.blockOverlayFill) return;

      World world = client.world;
      BlockPos pos = hit.getBlockPos();
      BlockState state = world.getBlockState(pos);
      if (state.isAir()) return;
      VoxelShape shape = state.getOutlineShape(world, pos, ShapeContext.of(client.getCameraEntity() != null ? client.getCameraEntity() : client.player));
      if (shape.isEmpty()) return;

      Vec3d cam = client.gameRenderer.getCamera().getCameraPos();
      Box local = shape.getBoundingBox().expand(EPS);
      Box box = local.offset(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);

      boolean airMode = VoidCyanClient.blockOverlayMode == MODE_AIR_EXPOSED;
      boolean[] exposed = new boolean[6];
      for (Direction d : Direction.values()) {
         exposed[d.ordinal()] = !airMode || isExposed(world, pos.offset(d));
      }

      int r = VoidCyanClient.blockOverlayRed & 0xFF;
      int g = VoidCyanClient.blockOverlayGreen & 0xFF;
      int b = VoidCyanClient.blockOverlayBlue & 0xFF;
      MatrixStack matrices = context.matrices();

      if (VoidCyanClient.blockOverlayFill) {
         drawFill(consumers, matrices, box, exposed, r, g, b, VoidCyanClient.blockOverlayFillOpacity / 100.0F);
      }

      if (VoidCyanClient.blockOverlayOutline) {
         List<VoxelShape> lines = new ArrayList<>();
         if (airMode) {
            addExposedEdges(lines, box, exposed);
         } else {
            lines.add(shape.offset(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z));
         }
         VertexConsumer vc = consumers.getBuffer(RenderLayers.linesTranslucent());
         float thickness = Math.max(0.5F, VoidCyanClient.blockOverlayThickness);
         if (VoidCyanClient.blockOverlayGlow) {
            float strength = Math.max(0.1F, VoidCyanClient.blockOverlayGlowStrength);
            for (int i = 3; i >= 1; i--) {
               int alpha = (int)(70.0F / i * Math.min(strength, 2.0F));
               int argb = Math.min(alpha, 255) << 24 | r << 16 | g << 8 | b;
               float w = thickness + i * 2.0F * strength;
               for (VoxelShape s : lines) {
                  VertexRendering.drawOutline(matrices, vc, s, 0.0, 0.0, 0.0, argb, w);
               }
            }
         }
         int argb = 0xFF000000 | r << 16 | g << 8 | b;
         for (VoxelShape s : lines) {
            VertexRendering.drawOutline(matrices, vc, s, 0.0, 0.0, 0.0, argb, thickness);
         }
      }
   }

   /** Exposed = the neighbour does not fill the space (air, fluids, plants, torches...). */
   private static boolean isExposed(World world, BlockPos neighbour) {
      BlockState n = world.getBlockState(neighbour);
      return n.isAir() || n.getCollisionShape(world, neighbour).isEmpty();
   }

   /** Adds a thin box per edge that borders at least one exposed face. */
   private static void addExposedEdges(List<VoxelShape> out, Box b, boolean[] exposed) {
      final double t = 0.0005;
      // Edge along each axis; the two perpendicular coordinates pick a corner.
      for (int axis = 0; axis < 3; axis++) {
         for (int hi1 = 0; hi1 < 2; hi1++) {
            for (int hi2 = 0; hi2 < 2; hi2++) {
               // Perpendicular axes in order (p1, p2); faces bordering the edge are p1's and p2's sides.
               int p1 = (axis + 1) % 3;
               int p2 = (axis + 2) % 3;
               Direction f1 = faceFor(p1, hi1 == 1);
               Direction f2 = faceFor(p2, hi2 == 1);
               if (!exposed[f1.ordinal()] && !exposed[f2.ordinal()]) continue;
               double[] min = {b.minX, b.minY, b.minZ};
               double[] max = {b.maxX, b.maxY, b.maxZ};
               double[] lo = min.clone();
               double[] hi = max.clone();
               double c1 = hi1 == 1 ? max[p1] : min[p1];
               double c2 = hi2 == 1 ? max[p2] : min[p2];
               lo[p1] = c1 - t; hi[p1] = c1 + t;
               lo[p2] = c2 - t; hi[p2] = c2 + t;
               out.add(VoxelShapes.cuboid(lo[0], lo[1], lo[2], hi[0], hi[1], hi[2]));
            }
         }
      }
   }

   private static Direction faceFor(int axis, boolean high) {
      return switch (axis) {
         case 0 -> high ? Direction.EAST : Direction.WEST;
         case 1 -> high ? Direction.UP : Direction.DOWN;
         default -> high ? Direction.SOUTH : Direction.NORTH;
      };
   }

   private static void drawFill(VertexConsumerProvider consumers, MatrixStack matrices, Box b, boolean[] exposed, int ri, int gi, int bi, float alpha) {
      if (alpha <= 0.0F) return;
      VertexConsumer vc = consumers.getBuffer(RenderLayers.translucentMovingBlock());
      Matrix4f m = matrices.peek().getPositionMatrix();
      Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.WHITE_CONCRETE.getDefaultState());
      float u = sprite.getFrameU(0.5F);
      float v = sprite.getFrameV(0.5F);
      float r = ri / 255.0F, g = gi / 255.0F, bl = bi / 255.0F;
      float x0 = (float)b.minX, y0 = (float)b.minY, z0 = (float)b.minZ;
      float x1 = (float)b.maxX, y1 = (float)b.maxY, z1 = (float)b.maxZ;

      // Each face is emitted with both windings so it shows from either side without culling.
      if (exposed[Direction.DOWN.ordinal()]) quad(vc, m, r, g, bl, alpha, u, v, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0, -1, 0);
      if (exposed[Direction.UP.ordinal()]) quad(vc, m, r, g, bl, alpha, u, v, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0, 1, 0);
      if (exposed[Direction.NORTH.ordinal()]) quad(vc, m, r, g, bl, alpha, u, v, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, 0, 0, -1);
      if (exposed[Direction.SOUTH.ordinal()]) quad(vc, m, r, g, bl, alpha, u, v, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0, 0, 1);
      if (exposed[Direction.WEST.ordinal()]) quad(vc, m, r, g, bl, alpha, u, v, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, -1, 0, 0);
      if (exposed[Direction.EAST.ordinal()]) quad(vc, m, r, g, bl, alpha, u, v, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, 1, 0, 0);
   }

   private static void quad(VertexConsumer vc, Matrix4f m, float r, float g, float b, float a, float u, float v,
                            float ax, float ay, float az, float bx, float by, float bz,
                            float cx, float cy, float cz, float dx, float dy, float dz,
                            float nx, float ny, float nz) {
      vert(vc, m, r, g, b, a, u, v, ax, ay, az, nx, ny, nz);
      vert(vc, m, r, g, b, a, u, v, bx, by, bz, nx, ny, nz);
      vert(vc, m, r, g, b, a, u, v, cx, cy, cz, nx, ny, nz);
      vert(vc, m, r, g, b, a, u, v, dx, dy, dz, nx, ny, nz);
      vert(vc, m, r, g, b, a, u, v, dx, dy, dz, -nx, -ny, -nz);
      vert(vc, m, r, g, b, a, u, v, cx, cy, cz, -nx, -ny, -nz);
      vert(vc, m, r, g, b, a, u, v, bx, by, bz, -nx, -ny, -nz);
      vert(vc, m, r, g, b, a, u, v, ax, ay, az, -nx, -ny, -nz);
   }

   private static void vert(VertexConsumer vc, Matrix4f m, float r, float g, float b, float a, float u, float v,
                            float x, float y, float z, float nx, float ny, float nz) {
      vc.vertex(m, x, y, z).color(r, g, b, a).texture(u, v).overlay(0, 10).light(15728880).normal(nx, ny, nz);
   }
}
