package fast_reset.client.mixin.completefutures;

import com.bawnorton.mixinsquared.TargetHandler;
import fast_reset.client.completefutures.AsyncSupply;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(value = ServerChunkManager.class, priority = 1500)
public abstract class ServerChunkManagerMixin {

    // Lithium overwrites getChunk and separates the
    // CompletableFuture#supplyAsync into another method
    @Dynamic
    @TargetHandler(
            mixin = "me.jellysquid.mods.lithium.mixin.world.chunk_access.ServerChunkManagerMixin",
            name = "getChunkOffThread"
    )
    @Redirect(
            method = {
                    "@MixinSquared:Handler",
                    "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;",
                    "getChunkFutureSyncOnMainThread"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <T> CompletableFuture<T> redirectCompletableFuture(Supplier<T> supplier, Executor executor) {
        return AsyncSupply.supplyAsync(supplier, executor);
    }
}
