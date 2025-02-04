package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fast_reset.client.FastReset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(StorageIoWorker.class)
public abstract class StorageIoWorkerMixin {
    @Unique
    private volatile boolean fastClosed;

    @WrapWithCondition(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private boolean suppressErrorsWhenFastClosed(Logger logger, String s, Object a, Object b) {
        return !this.fastClosed;
    }

    @WrapWithCondition(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/storage/RegionBasedStorage;write(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"
            )
    )
    private boolean doNotWriteToStorage(RegionBasedStorage storage, ChunkPos pos, CompoundTag tag) {
        return !this.fastClosed;
    }

    @WrapOperation(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;completeExceptionally(Ljava/lang/Throwable;)Z",
                    remap = false
            )
    )
    private boolean doNotCompleteExceptionally(CompletableFuture<?> future, Throwable throwable, Operation<Boolean> original) {
        if (this.fastClosed) {
            return future.complete(null);
        }
        return original.call(future, throwable);
    }

    @Inject(
            method = "close",
            at = @At("HEAD")
    )
    private void setFastClosed(CallbackInfo ci) {
        this.fastClosed = FastReset.shouldFastClose();
    }

    @WrapWithCondition(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;join()Ljava/lang/Object;"
            )
    )
    private boolean doNotWaitForTasksToFinish(CompletableFuture<Void> future) {
        return !this.fastClosed;
    }
}
