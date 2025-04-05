package cn.xiaym.relocks;

import cn.xiaym.relocks.packet.RelockC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerUnlock;

import java.util.*;

public class PlayerRelocks implements ModInitializer {

    public static final String MOD_ID = "player_relocks";
    public static final Map<Holder<PlayerUnlock>, List<Holder<PlayerUnlock>>> unlockChildrenMap = new HashMap<>();

    @Override
    public void onInitialize() {
        // Register packets :D
        PayloadTypeRegistry.playC2S().register(RelockC2SPacket.TYPE, RelockC2SPacket.CODEC);

        // Server Handling
        for (Holder<PlayerUnlock> holder : BuiltInRegistries.PLAYER_UNLOCK.asHolderIdMap()) {
            if (holder.value().parent().isPresent()) {
                Holder<PlayerUnlock> parent = holder.value().parent().get();
                unlockChildrenMap.computeIfAbsent(parent, k -> new ArrayList<>()).add(holder);
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(RelockC2SPacket.TYPE, (packet, context) -> {
            Holder<PlayerUnlock> requested = packet.unlock();
            if (!context.player().isUnlocked(requested)) {
                return;
            }

            String key = requested.value().key();
            List<Holder<PlayerUnlock>> children = unlockChildrenMap.getOrDefault(requested, Collections.emptyList());

            for (Holder<PlayerUnlock> child : children) {
                if (child != null && context.player().isUnlocked(child)) {
                    context.player().sendSystemMessage(Component.translatable("player-relocks.error.has-child")
                        .withStyle(ChatFormatting.RED));
                    return;
                }
            }

            context.player().playerUnlocks.revoke(requested);
            context.player().sendSystemMessage(
                Component.translatable("player-relocks.info.done", Component.translatable("unlocks.unlock." + key + ".name"))
                .withStyle(ChatFormatting.GREEN)
            );
        });
    }
}