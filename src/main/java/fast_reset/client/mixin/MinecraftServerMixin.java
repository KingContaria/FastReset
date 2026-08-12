package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fast_reset.client.FastReset;
import fast_reset.client.interfaces.FRMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> implements FRMinecraftServer {
    @Shadow
    private volatile boolean isReady;

    @Unique
    private volatile boolean fastReset;

    public MinecraftServerMixin(String string) {
        super(string, false);
    }

    @Inject(
            method = "stopServer",
            at = @At("HEAD")
    )
    private void enableFastClose(CallbackInfo ci) {
        if (!this.fastReset$shouldSave()) {
            FastReset.enableFastClose();
        }
    }

    @WrapWithCondition(
            method = "stopServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;saveAll()V"
            )
    )
    private boolean disablePlayerSaving(PlayerList playerList) {
        return this.fastReset$shouldSave();
    }

    @ModifyExpressionValue(
            method = "stopServer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"
            )
    )
    private boolean disableShutdownDelay(boolean anyMatch) {
        return anyMatch && this.fastReset$shouldSave();
    }

    @WrapWithCondition(
            method = "stopServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;saveAllChunks(ZZZ)Z"
            )
    )
    private boolean disableSaving(MinecraftServer server, boolean bl, boolean bl2, boolean bl3) {
        return this.fastReset$shouldSave();
    }

    @Override
    public void fastReset$fastReset() {
        this.fastReset = true;
    }

    // We check readiness to skip saving on resets that happen while a world is still loading.
    @Override
    public boolean fastReset$shouldSave() {
        return !this.fastReset && this.isReady;
    }
}
