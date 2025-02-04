package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fast_reset.client.FastReset;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {

    @WrapOperation(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerChunkManager;save(Z)V"
            )
    )
    private void skipSaving(ServerChunkManager manager, boolean flush, Operation<Void> original) {
        if (FastReset.shouldFastClose()) {
            if (flush) {
                manager.chunkLoadingManager.completeAll();
            }
        } else {
            original.call(manager, flush);
        }
    }
}
