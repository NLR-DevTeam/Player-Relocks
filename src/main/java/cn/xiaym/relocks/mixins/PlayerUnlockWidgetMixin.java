package cn.xiaym.relocks.mixins;

import cn.xiaym.relocks.packets.c2s.RelockC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.unlocks.PlayerUnlockWidget;
import net.minecraft.core.Holder;
import net.minecraft.server.players.PlayerUnlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerUnlockWidget.class)
public class PlayerUnlockWidgetMixin {
    @Shadow
    private boolean unlocked;

    @Shadow
    @Final
    private Holder<PlayerUnlock> node;

    @Inject(method = "onClicked", at = @At("HEAD"))
    public void onClicked(CallbackInfo ci) {
        if (this.unlocked && Screen.hasShiftDown()) {
            ClientPlayNetworking.send(new RelockC2SPacket(this.node));
        }
    }
}
