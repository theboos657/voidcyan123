package com.voidcyan.client;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class TestTiny {
   public static void test() {
      BufferUtils.createPointerBuffer(1);
      TinyFileDialogs.tinyfd_openFileDialog("Select Image", "", null, null, false);
   }
}
