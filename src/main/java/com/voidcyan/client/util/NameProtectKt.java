package com.voidcyan.client.util;

import java.util.List;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

@Metadata(
   mv = {2, 3, 0},
   k = 2,
   xi = 48,
   d1 = {"\u0000\u0016\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u001a-\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0001\u001a\u00020\u00002\u0006\u0010\u0002\u001a\u00020\u00002\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00000\u0003¢\u0006\u0004\b\u0006\u0010\u0007¨\u0006\b"},
   d2 = {"", "targetName", "replacementName", "", "otherPlayers", "", "updateNameProtectMappingsStatic", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "voidcyan-client"}
)
public final class NameProtectKt {
   public static final void updateNameProtectMappingsStatic(@NotNull String targetName, @NotNull String replacementName, @NotNull List<String> otherPlayers) {
      Intrinsics.checkNotNullParameter(targetName, "targetName");
      Intrinsics.checkNotNullParameter(replacementName, "replacementName");
      Intrinsics.checkNotNullParameter(otherPlayers, "otherPlayers");
      NameProtect.updateMappings$default(NameProtect.INSTANCE, targetName, replacementName, MapsKt.emptyMap(), otherPlayers, null, 16, null);
   }

   public static void updateNameProtectMappingsStatic$default(String targetName, String replacementName, java.util.List otherPlayers, int flags, Object ignored) {
      if ((flags & 4) != 0) { otherPlayers = CollectionsKt.emptyList(); }
      updateNameProtectMappingsStatic(targetName, replacementName, otherPlayers);
   }
}