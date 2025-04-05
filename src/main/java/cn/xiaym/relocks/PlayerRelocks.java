package cn.xiaym.relocks;

import cn.xiaym.relocks.packets.c2s.RelockC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerUnlock;
import net.minecraft.server.players.PlayerUnlocks;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PlayerRelocks implements ModInitializer {
    public static final String MOD_ID = "player_relocks";

    @Override
    public void onInitialize() {
        // Register packets :D
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            PayloadTypeRegistry.playC2S().register(RelockC2SPacket.TYPE, RelockC2SPacket.CODEC);
        }

        // Server Handling
        @SuppressWarnings("unchecked") List<Holder<PlayerUnlock>> unlocks = Arrays.stream(PlayerUnlocks.class.getFields())
                .filter(it -> it.getType() == Holder.class).map(it -> {
                    try {
                        return (Holder<PlayerUnlock>) it.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();

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
