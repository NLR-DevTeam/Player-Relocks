package cn.xiaym.relocks.mixin;

import cn.xiaym.relocks.packet.RelockC2SPacket;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.unlocks.PlayerUnlockWidget;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.players.PlayerUnlock;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

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

    @WrapMethod(method = "getDescriptionText")
    public List<FormattedCharSequence> getDescriptionText(Operation<List<FormattedCharSequence>> original) {
        List<FormattedCharSequence> raw = original.call();
        if (!this.unlocked) {
            return raw;
        }

        ArrayList<FormattedCharSequence> processed = new ArrayList<>(raw);
        FormattedCharSequence insertion = Language.getInstance()
                .getVisualOrder(Component.translatable("player-relocks.client.tooltip." + (Screen.hasShiftDown() ? "click-to-relock" : "hold-shift"))
                        .withStyle(ChatFormatting.GRAY));
        processed.add(FormattedCharSequence.EMPTY);
        processed.add(FormattedCharSequence.composite(FormattedCharSequence.forward(" - ", Style.EMPTY.withColor(ChatFormatting.GRAY)), insertion));
        return processed;
    }
}