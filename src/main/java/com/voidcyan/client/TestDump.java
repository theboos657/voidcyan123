package com.voidcyan.client;

import java.lang.reflect.Method;
import net.minecraft.client.world.ClientWorld;

public class TestDump {
   public static void main(String[] args) {
      for (Method m : ClientWorld.class.getMethods()) {
         if (m.getReturnType() == long.class) {
            System.out.println("TIME METHOD: " + m.getName());
         }
      }
   }
}
