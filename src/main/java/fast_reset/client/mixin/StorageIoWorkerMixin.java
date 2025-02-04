package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fast_reset.client.FastReset;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(StorageIoWorker.class)
public abstract class StorageIoWorkerMixin {
    @Unique
    private volatile boolean fastClosed;

    @WrapWithCondition(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
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
                    target = "Lnet/minecraft/world/storage/RegionBasedStorage;write(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/NbtCompound;)V"
            )
    )
    private boolean doNotWriteToStorage(RegionBasedStorage storage, ChunkPos pos, NbtCompound tag) {
        return !this.fastClosed;
    }

    @Inject(
            method = "close",
            at = @At("HEAD")
    )
    private void setFastClosed(CallbackInfo ci) {
        this.fastClosed = FastReset.shouldFastClose();
    }

    @WrapOperation(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/thread/TaskExecutor;ask(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private CompletableFuture<?> doNotWaitForTasksToFinish(TaskExecutor<?> executor, Function<?, ?> function, Operation<CompletableFuture<?>> original) {
        if (this.fastClosed) {
            return CompletableFuture.completedFuture(null);
        }
        return original.call(executor, function);
    }
}
