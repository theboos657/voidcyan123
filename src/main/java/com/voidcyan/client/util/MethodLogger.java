package com.voidcyan.client.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import net.minecraft.client.gui.hud.ChatHud;
import org.slf4j.LoggerFactory;

public class MethodLogger {
   public static void logChatHudMethods() {
      try {
         LoggerFactory.getLogger("MethodLogger").info("ChatHud class: {}", ChatHud.class.getName());
         Method[] methods = ChatHud.class.getDeclaredMethods();

         for (Method method : methods) {
            LoggerFactory.getLogger("MethodLogger")
               .info("ChatHud method: {} with params: {}", method.getName(), Arrays.toString((Object[])method.getParameterTypes()));
         }
      } catch (Exception var5) {
         LoggerFactory.getLogger("MethodLogger").error("Failed to log methods", var5);
      }
   }
}
