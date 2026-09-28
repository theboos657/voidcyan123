package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class NameTagItemsSettingsScreen extends BaseSettingsScreen {

    public NameTagItemsSettingsScreen(Screen parent) {
        super(parent, "Name Tag Items Settings");
    }

    @Override
    protected void buildSettings() {
        this.addEnum("Display Mode", new String[]{"Player Doll", "3D Model", "Item Row"},
                () -> VoidCyanClient.nameTagItemsDisplayMode,
                val -> VoidCyanClient.nameTagItemsDisplayMode = val);

        this.addBoolean("Grey Items When Using",
                () -> VoidCyanClient.nameTagItemsGreyWhenUsing,
                val -> VoidCyanClient.nameTagItemsGreyWhenUsing = val);

        this.addBoolean("Show Main Hand Item",
                () -> VoidCyanClient.nameTagItemsShowMainHand,
                val -> VoidCyanClient.nameTagItemsShowMainHand = val);

        this.addBoolean("Show Off Hand Item",
                () -> VoidCyanClient.nameTagItemsShowOffHand,
                val -> VoidCyanClient.nameTagItemsShowOffHand = val);

        this.addBoolean("Show Totem Pop",
                () -> VoidCyanClient.nameTagItemsShowTotemPop,
                val -> VoidCyanClient.nameTagItemsShowTotemPop = val);

        this.addBoolean("Show Armor",
                () -> VoidCyanClient.nameTagItemsShowArmor,
                val -> VoidCyanClient.nameTagItemsShowArmor = val);

        this.addBoolean("Show Durability & Count",
                () -> VoidCyanClient.nameTagItemsShowDurability,
                val -> VoidCyanClient.nameTagItemsShowDurability = val);

        this.addBoolean("Show Above Self",
                () -> VoidCyanClient.nameTagItemsShowSelf,
                val -> VoidCyanClient.nameTagItemsShowSelf = val);

        this.addBoolean("Only Friends",
                () -> VoidCyanClient.nameTagItemsOnlyFriends,
                val -> VoidCyanClient.nameTagItemsOnlyFriends = val);

        this.addSlider("Item Scale", 0.5f, 2.0f,
                () -> VoidCyanClient.nameTagItemsScale,
                val -> VoidCyanClient.nameTagItemsScale = val);
    }

    @Override
    protected boolean includeAutoToggleKeybind() {
        return true;
    }
}
