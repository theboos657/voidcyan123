package com.voidcyan.client.screen;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class MethodDump {
   public static void main(String[] args) {
      try (PrintWriter writer = new PrintWriter(new FileWriter("draw_methods.txt", true))) {
         writer.println("\n=== NativeImageBackedTexture Constructors ===");

         for (Constructor<?> c : NativeImageBackedTexture.class.getConstructors()) {
            writer.println(c.toString());
         }

         writer.println("=== Done ===");
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }
}
