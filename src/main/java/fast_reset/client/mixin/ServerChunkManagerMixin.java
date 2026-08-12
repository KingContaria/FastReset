package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fast_reset.client.FastReset;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkManagerMixin {

    @WrapOperation(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerChunkCache;save(Z)V"
            )
    )
    private void skipSaving(ServerChunkCache manager, boolean flush, Operation<Void> original) {
        if (FastReset.shouldFastClose()) {
            if (flush) {
                manager.chunkMap.synchronize(true).join();
            }
        } else {
            original.call(manager, flush);
        }
    }
}
