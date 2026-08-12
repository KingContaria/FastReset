package fast_reset.client.mixin;

import fast_reset.client.FastReset;
import fast_reset.client.FastResetConfig;
import fast_reset.client.interfaces.FRMinecraftServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    @Shadow
    @Nullable
    private Button disconnectButton;

    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    @Inject(
            method = "createPauseMenu",
            at = @At("TAIL")
    )
    private void createFastResetButton(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.hasSingleplayerServer() || !this.shouldFastReset()) {
            return;
        }

        Button saveButton = Objects.requireNonNull(this.disconnectButton);
        Component menuQuitWorld = Component.translatable("fast_reset.menu.quitWorld");
        int height = 20;
        int width;
        int x;
        int y;
        switch (FastReset.config.buttonLocation) {
            case CENTER:
                width = saveButton.getWidth();
                x = saveButton.getX();
                y = saveButton.getY() + 24;
                break;
            case BOTTOM_RIGHT:
            default:
                width = this.font.width(menuQuitWorld) + 30;
                x = this.width - width - 4;
                y = this.height - height - 4;
        }

        AbstractWidget fastResetButton = this.addRenderableWidget(Button.builder(menuQuitWorld, button -> {
            if (minecraft.getSingleplayerServer() != null) {
                ((FRMinecraftServer) minecraft.getSingleplayerServer()).fastReset$fastReset();
            }
            saveButton.onPress(null);
        }).bounds(x, y, width, height).build());

        fastResetButton.visible = FastReset.config.buttonLocation != FastResetConfig.ButtonLocation.HIDE;
    }

    @Unique
    private boolean shouldFastReset() {
        if (FastReset.config.alwaysSaveAfter == 0) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getSingleplayerServer() != null && minecraft.getSingleplayerServer().getTickCount() <= FastReset.config.alwaysSaveAfter * 20;
    }
}
