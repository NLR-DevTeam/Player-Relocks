package cn.xiaym.relocks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Path CONF_FILE = FabricLoader.getInstance().getConfigDir().resolve("player-relocks.json");
    public static float expGiveBackRatio = 0.0f;

    protected static void init() throws IOException {
        if (Files.notExists(CONF_FILE)) {
            Files.createFile(CONF_FILE);
            save();
        } else {
            JsonObject jsonObject = JsonParser.parseString(Files.readString(CONF_FILE)).getAsJsonObject();
            expGiveBackRatio = jsonObject.get("expGiveBackRatio").getAsFloat();
        }
    }

    private static void save() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("expGiveBackRatio", expGiveBackRatio);

        Files.writeString(CONF_FILE, PlayerRelocks.GSON.toJson(jsonObject));
    }
}
