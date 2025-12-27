package cn.xiaym.relocks.mixin;

import cn.xiaym.relocks.data.GlobalDataManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerUnlock;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @WrapMethod(method = "buyUnlock")
    private boolean buyUnlock(Holder<PlayerUnlock> holder, Operation<Boolean> original) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        int before = self.experienceLevel;

        boolean result = original.call(holder);
        if (result) {
            int cost = 0, after = self.experienceLevel;
            for (int i = before; i > after; i--) {
                cost += Player.getXpNeededForLevel(i);
            }

            GlobalDataManager.forPlayer(self.getGameProfile().getId()).unlockCosts.put(holder, cost);
        }

        return result;
    }
}
