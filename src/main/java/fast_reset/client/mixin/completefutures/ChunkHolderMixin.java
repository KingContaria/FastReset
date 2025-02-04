package fast_reset.client.mixin.completefutures;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BiFunction;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin {

    @ModifyArg(
            method = "combineSavingFuture(Ljava/util/concurrent/CompletableFuture;Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;thenCombine(Ljava/util/concurrent/CompletionStage;Ljava/util/function/BiFunction;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 1
    )
    private BiFunction<Chunk, ? extends OptionalChunk<? extends Chunk>, Chunk> handleCancelledFuture(BiFunction<?, ?, ?> fn) {
        return (chunk, otherChunk) -> otherChunk != null ? OptionalChunk.orElse(otherChunk, chunk) : chunk;
    }
}
