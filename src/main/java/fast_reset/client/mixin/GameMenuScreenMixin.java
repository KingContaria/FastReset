package fast_reset.client.mixin;

import fast_reset.client.FastReset;
import fast_reset.client.FastResetConfig;
import fast_reset.client.interfaces.FRMinecraftServer;
import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    @Shadow
    @Nullable
    private ButtonWidget exitButton;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "initWidgets",
            at = @At("TAIL")
    )
    private void createFastResetButton(CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isInSingleplayer() || !this.shouldFastReset()) {
            return;
        }

        ButtonWidget saveButton = Objects.requireNonNull(this.exitButton);
        Text menuQuitWorld = TextUtil.translatable("fast_reset.menu.quitWorld");
        int height = 20;
        int width;
        int x;
        int y;
        switch (FastReset.config.buttonLocation) {
            case REPLACE_SQ:
                width = saveButton.getWidth();
                x = saveButton.getX();
                y = saveButton.getY();

                saveButton.setWidth(this.textRenderer.getWidth(saveButton.getMessage()) + 30);
                saveButton.setX(this.width - saveButton.getWidth() - 4);
                saveButton.setY(this.height - saveButton.getHeight() - 4);
                break;
            case CENTER:
                width = saveButton.getWidth();
                x = saveButton.getX();
                y = saveButton.getY() + 24;
                break;
            case BOTTOM_RIGHT:
            default:
                width = this.textRenderer.getWidth(menuQuitWorld) + 30;
                x = this.width - width - 4;
                y = this.height - height - 4;
        }

        ClickableWidget fastResetButton = this.addDrawableChild(ButtonWidget.builder(menuQuitWorld, button -> {
            if (MinecraftClient.getInstance().getServer() != null) {
                ((FRMinecraftServer) MinecraftClient.getInstance().getServer()).fastReset$fastReset();
            }
            saveButton.onPress();
        }).dimensions(x, y, width, height).build());

        fastResetButton.visible = FastReset.config.buttonLocation != FastResetConfig.ButtonLocation.HIDE;
    }

    @Unique
    private boolean shouldFastReset() {
        if (FastReset.config.alwaysSaveAfter == 0) {
            return true;
        }
        return MinecraftClient.getInstance().getServer() != null && MinecraftClient.getInstance().getServer().getTicks() <= FastReset.config.alwaysSaveAfter * 20;
    }
}