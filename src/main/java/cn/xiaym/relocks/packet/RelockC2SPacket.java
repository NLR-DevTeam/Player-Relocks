package cn.xiaym.relocks.packet;

import cn.xiaym.relocks.PlayerRelocks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.PlayerUnlock;
import org.jetbrains.annotations.NotNull;

public record RelockC2SPacket(Holder<PlayerUnlock> unlock) implements CustomPacketPayload {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(PlayerRelocks.MOD_ID, "relock-player-unlock");
    public static final Type<RelockC2SPacket> TYPE = new CustomPacketPayload.Type<>(IDENTIFIER);
    public static final StreamCodec<RegistryFriendlyByteBuf, RelockC2SPacket> CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.PLAYER_UNLOCK), RelockC2SPacket::unlock, RelockC2SPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}