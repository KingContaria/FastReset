package fast_reset.client.mixin.completefutures;

import fast_reset.client.completefutures.AsyncSupply;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ServerChunkLoadingManagerMixin {

    @Redirect(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <T, U> CompletableFuture<U> redirectCompletableFuture(CompletableFuture<T> from, Function<T, U> function, Executor executor) {
        return AsyncSupply.thenApplyAsync(from, function, executor);
    }

    @Redirect(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;exceptionallyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <T> CompletableFuture<T> redirectCompletableFuture2(CompletableFuture<T> from, Function<Throwable, T> function, Executor executor) {
        return AsyncSupply.exceptionallyAsync(from, function, executor);
    }
}
