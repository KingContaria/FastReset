package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fast_reset.client.FastReset;
import fast_reset.client.interfaces.FRThreadExecutor;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Shadow
    @Final
    private ServerChunkManager.MainThreadExecutor mainThreadExecutor;

    @WrapWithCondition(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerChunkManager;save(Z)V"
            )
    )
    private boolean skipSaving(ServerChunkManager chunkManager, boolean flush) {
        return !FastReset.shouldFastClose();
    }

    @Inject(
            method = "close",
            at = @At("RETURN")
    )
    private void cancelRemainingTasks(CallbackInfo ci) {
        if (FastReset.shouldFastClose()) {
            //noinspection DataFlowIssue
            ((FRThreadExecutor) (Object) this.mainThreadExecutor).fast_reset$cancelFutures();
        }
    }
}
