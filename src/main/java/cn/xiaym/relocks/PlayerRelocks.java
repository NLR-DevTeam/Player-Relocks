package cn.xiaym.relocks;

import cn.xiaym.relocks.packets.c2s.RelockC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerUnlock;
import net.minecraft.server.players.PlayerUnlocks;

import java.util.*;

public class PlayerRelocks implements ModInitializer {
    public static final String MOD_ID = "player_relocks";

    @Override
    public void onInitialize() {
        // Register packets :D
        PayloadTypeRegistry.playC2S().register(RelockC2SPacket.TYPE, RelockC2SPacket.CODEC);

        // Server Handling
        List<Holder<PlayerUnlock>> unlocks = new ArrayList<>();
        for (Holder<PlayerUnlock> holder : BuiltInRegistries.PLAYER_UNLOCK.asHolderIdMap()) {
            unlocks.add(holder);
        }

        ServerPlayNetworking.registerGlobalReceiver(RelockC2SPacket.TYPE, (packet, context) -> {
            Holder<PlayerUnlock> requested = packet.unlock();
            if (!context.player().isUnlocked(requested)) {
                return;
            }

            String key = requested.value().key();
            for (Holder<PlayerUnlock> unlockHolder : unlocks) {
                Optional<Holder<PlayerUnlock>> parent = unlockHolder.value().parent();

                if (parent.isPresent() && Objects.equals(parent.get().value().key(), key) && context.player()
                        .isUnlocked(unlockHolder)) {
                    context.player().sendSystemMessage(Component.translatable("player-relocks.error.has-child")
                            .withStyle(ChatFormatting.RED));
                    return;
                }
            }

            context.player().playerUnlocks.revoke(packet.unlock());
            context.player().sendSystemMessage(Component.translatable("player-relocks.info.done")
                    .withStyle(ChatFormatting.GREEN).append(Component.translatable("unlocks.unlock." + key + ".name")));
        });
    }
}
