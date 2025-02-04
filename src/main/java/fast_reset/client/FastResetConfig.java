package fast_reset.client;

import fast_reset.client.gui.TimeSliderWidget;
import me.contaria.speedrunapi.config.SpeedrunConfigAPI;
import me.contaria.speedrunapi.config.api.SpeedrunConfig;
import me.contaria.speedrunapi.config.api.SpeedrunOption;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class FastResetConfig implements SpeedrunConfig {
    public ButtonLocation buttonLocation = ButtonLocation.BOTTOM_RIGHT;
    public int alwaysSaveAfter = 0;

    {
        FastReset.config = this;
    }

    @Override
    public @Nullable SpeedrunOption<?> parseField(Field field, SpeedrunConfig config, String... idPrefix) {
        if ("alwaysSaveAfter".equals(field.getName())) {
            return new SpeedrunConfigAPI.CustomOption.Builder<Integer>(this, this, field, idPrefix)
                    .createWidget((option, innerConfig, configStorage, optionField) -> new TimeSliderWidget(0, 0, 150, 20, option))
                    .build();
        }
        return SpeedrunConfig.super.parseField(field, config, idPrefix);
    }

    @Override
    public String modID() {
        return "fast_reset";
    }

    public enum ButtonLocation {
        BOTTOM_RIGHT,
        CENTER,
        REPLACE_SQ,
        HIDE
    }
}
