package cn.xiaym.relocks.mixin;

import cn.xiaym.relocks.data.GlobalDataManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.Holder;
import net.minecraft.server.players.PlayerUnlock;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    public int experienceLevel;
    @Shadow
    @Final
    private GameProfile gameProfile;

    @WrapMethod(method = "buyUnlock")
    private boolean buyUnlock(Holder<PlayerUnlock> holder, Operation<Boolean> original) {
        int before = experienceLevel;

        boolean result = original.call(holder);
        if (result) {
            int cost = 0, after = experienceLevel;
            for (int i = before; i >= after; i--) {
                cost += Player.getXpNeededForLevel(i);
            }

            GlobalDataManager.forPlayer(gameProfile.getId()).unlockCosts.put(holder, cost);
        }

        return result;
    }
}
