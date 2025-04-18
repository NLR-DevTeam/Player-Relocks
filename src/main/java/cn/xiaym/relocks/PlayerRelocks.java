package cn.xiaym.relocks;

import cn.xiaym.relocks.data.GlobalDataManager;
import cn.xiaym.relocks.packet.RelockC2SPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerUnlock;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class PlayerRelocks implements ModInitializer {
    public static final String MOD_ID = "player_relocks";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<Holder<PlayerUnlock>, List<Holder<PlayerUnlock>>> UNLOCK_CHILDREN_MAP = new HashMap<>();

    @Override
    public void onInitialize() {
        try {
            Config.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Register packets :D
        PayloadTypeRegistry.playC2S().register(RelockC2SPacket.TYPE, RelockC2SPacket.CODEC);

        // Server Handling
        for (Holder<PlayerUnlock> holder : BuiltInRegistries.PLAYER_UNLOCK.asHolderIdMap()) {
            if (holder.value().parent().isPresent()) {
                Holder<PlayerUnlock> parent = holder.value().parent().get();
                UNLOCK_CHILDREN_MAP.computeIfAbsent(parent, k -> new ArrayList<>()).add(holder);
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(RelockC2SPacket.TYPE, (packet, context) -> {
            ServerPlayer player = context.player();

            Holder<PlayerUnlock> requested = packet.unlock();
            if (!player.isUnlocked(requested)) {
                return;
            }

            String key = requested.value().key();
            List<Holder<PlayerUnlock>> children = UNLOCK_CHILDREN_MAP.getOrDefault(requested, Collections.emptyList());

            for (Holder<PlayerUnlock> child : children) {
                if (child != null && player.isUnlocked(child)) {
                    player.sendSystemMessage(Component.translatable("player-relocks.error.has-child")
                            .withStyle(ChatFormatting.RED));
                    return;
                }
            }

            player.playerUnlocks.revoke(requested);

            var costsMap = GlobalDataManager.forPlayer(player.getGameProfile().getId()).unlockCosts;
            if (costsMap.containsKey(requested)) {
                int giveBackPoints = costsMap.remove(requested);
                System.out.println(giveBackPoints);
                System.out.println(giveBackPoints * Config.expGiveBackRatio);
                player.giveExperiencePoints((int) (giveBackPoints * Config.expGiveBackRatio));
            }

            player.sendSystemMessage(Component.translatable("player-relocks.info.done", Component.translatable("unlocks.unlock." + key + ".name"))
                    .withStyle(ChatFormatting.GREEN));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Path globalDataPath = server.getWorldPath(LevelResource.ROOT).resolve("player-relocks.json");

            try {
                GlobalDataManager.init(globalDataPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ServerLifecycleEvents.BEFORE_SAVE.register((server, b1, b2) -> {
            try {
                GlobalDataManager.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}