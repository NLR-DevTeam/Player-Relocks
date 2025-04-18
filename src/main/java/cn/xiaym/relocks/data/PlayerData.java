package cn.xiaym.relocks.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.PlayerUnlock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerData {
    public HashMap<Holder<PlayerUnlock>, Integer> unlockCosts = new HashMap<>();

    public static PlayerData deserialize(JsonObject jsonObject) {
        PlayerData playerData = new PlayerData();

        if (jsonObject.has("unlockCosts")) {
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("unlockCosts").getAsJsonObject().entrySet()) {
                Optional<Holder.Reference<PlayerUnlock>> unlockOptional = BuiltInRegistries.PLAYER_UNLOCK.get(ResourceLocation.parse(entry.getKey()));
                if (unlockOptional.isEmpty()) {
                    continue;
                }

                Holder<PlayerUnlock> unlockHolder = unlockOptional.get();
                int cost = entry.getValue().getAsInt();

                playerData.unlockCosts.put(unlockHolder, cost);
            }
        }

        return playerData;
    }

    public JsonObject serialize() {
        JsonObject costsObject = new JsonObject();
        for (Map.Entry<Holder<PlayerUnlock>, Integer> entry : unlockCosts.entrySet()) {
            costsObject.addProperty(entry.getKey().getRegisteredName(), entry.getValue());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("unlockCosts", costsObject);

        return jsonObject;
    }
}
