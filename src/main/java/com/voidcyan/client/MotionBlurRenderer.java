package com.voidcyan.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Temporal motion blur. At the end of the world pass the previous output is blended over the new frame with a
 * GPU shader, then the blended result is stored again, so every frame leaves an exponentially fading trail
 * (true feedback blur). The HUD is drawn afterwards, so it never smears.
 */
public final class MotionBlurRenderer {
   private MotionBlurRenderer() {
   }

   private static final RenderPipeline PIPELINE = RenderPipeline.builder()
      .withLocation(Identifier.of("voidcyan", "pipeline/motion_blur"))
      .withVertexShader("core/screenquad")
      .withFragmentShader(Identifier.of("voidcyan", "core/vc_blur"))
      .withSampler("InSampler")
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthWrite(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withColorWrite(true, false)
      .withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
      .build();

   private static Hist hist;
   private static int texW = 0;
   private static int texH = 0;
   private static boolean valid = false;
   private static long lastNanos = 0L;
   private static boolean broken = false;

   private static final class Hist extends AbstractTexture {
      Hist(int w, int h) {
         var dev = RenderSystem.getDevice();
         this.glTexture = dev.createTexture("voidcyan_motion_blur", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, w, h, 1, 1);
         this.glTextureView = dev.createTextureView(this.glTexture);
         this.sampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
      }
   }

   private static void release() {
      if (hist != null) {
         hist.close();
         hist = null;
      }

      valid = false;
      texW = 0;
      texH = 0;
   }

   private static boolean active(MinecraftClient mc) {
      return FeatureModules.on[FeatureModules.MBLUR] && !broken && mc.world != null && mc.player != null && mc.currentScreen == null && !mc.options.hudHidden;
   }

   private static float lastYaw = 0.0F;
   private static float lastPitch = 0.0F;
   private static boolean haveRot = false;
   private static float turn = 0.0F;

   /**
    * Blend weight of the previous frame. Camera-driven: standing still or walking straight stays perfectly
    * crisp (no ghosting, no washed-out look); fast turning blends in a trail. Strength 1-10 scales the trail.
    */
   private static float weight(MinecraftClient mc, float dt) {
      dt = Math.max(0.001F, Math.min(0.1F, dt));
      var cam = mc.gameRenderer.getCamera();
      float yaw = cam.getYaw();
      float pitch = cam.getPitch();
      float speed = 0.0F;
      if (haveRot) {
         float dy = yaw - lastYaw;
         dy = ((dy + 180.0F) % 360.0F + 360.0F) % 360.0F - 180.0F;
         speed = (float) Math.sqrt(dy * dy + (pitch - lastPitch) * (pitch - lastPitch)) / dt;
      }

      lastYaw = yaw;
      lastPitch = pitch;
      haveRot = true;
      float target = Math.max(0.0F, Math.min(1.0F, (speed - 40.0F) / 360.0F));
      target = target * target * (3.0F - 2.0F * target);
      turn += (target - turn) * Math.min(1.0F, dt * (target > turn ? 40.0F : 14.0F));
      float s = FeatureModules.blurStrength;
      float decay = Math.min(0.9F, 0.35F + 0.055F * s);
      return (float) Math.pow(decay, dt * 60.0F) * turn;
   }

   /** END_MAIN: blend the previous frame over the finished world, then remember the result. */
   public static void capture() {
      MinecraftClient mc = MinecraftClient.getInstance();
      long now = System.nanoTime();
      float dt = lastNanos == 0L ? 0.016F : (now - lastNanos) / 1.0E9F;
      lastNanos = now;
      if (!active(mc)) {
         valid = false;
         haveRot = false;
         turn = 0.0F;
         return;
      }

      try {
         run(mc, dt);
      } catch (Throwable t) {
         broken = true;
         FeatureModules.on[FeatureModules.MBLUR] = false;
         System.err.println("[VoidCyan] Motion blur disabled after error: " + t);
         t.printStackTrace();
      }
   }

   private static void run(MinecraftClient mc, float dt) {
      {
         var fb = mc.getFramebuffer();
         int w = fb.textureWidth;
         int h = fb.textureHeight;
         if (hist == null || w != texW || h != texH) {
            release();
            hist = new Hist(w, h);
            texW = w;
            texH = h;
         }

         var enc = RenderSystem.getDevice().createCommandEncoder();
         if (valid) {
            var slice = RenderSystem.getDynamicUniforms().write(new Matrix4f(), new Vector4f(1.0F, 1.0F, 1.0F, weight(mc, dt)), new Vector3f(), new Matrix4f());
            try (RenderPass pass = enc.createRenderPass(() -> "voidcyan motion blur", fb.getColorAttachmentView(), OptionalInt.empty())) {
               pass.setPipeline(PIPELINE);
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("DynamicTransforms", slice);
               pass.bindTexture("InSampler", hist.getGlTextureView(), hist.getSampler());
               pass.draw(0, 3);
            }
         }

         enc.copyTextureToTexture(fb.getColorAttachment(), hist.getGlTexture(), 0, 0, 0, 0, 0, w, h);
         valid = true;
      }
   }

}
