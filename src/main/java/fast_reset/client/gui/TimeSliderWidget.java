package fast_reset.client.gui;

import me.contaria.speedrunapi.config.api.SpeedrunOption;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.resource.language.I18n;

import java.util.LinkedHashMap;

public class TimeSliderWidget extends SliderWidget {
    private final SpeedrunOption<Integer> option;

    public TimeSliderWidget(int x, int y, int width, int height, SpeedrunOption<Integer> option) {
        super(x, y, width, height, (double) option.get() / (5 * 60));
        this.option = option;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        String baseKey = "speedrunapi.config." + this.option.getModID() + ".option." + this.option.getID();
        int current = this.option.get();
        int hours = current / (60 * 60);
        LinkedHashMap<String, Integer> times = new LinkedHashMap<>(3);
        times.put("hours", hours);
        times.put("minutes", (current) % (60 * 60) / 60);
        times.put("seconds", current % 60);
        this.setMessage(times
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> I18n.translate(baseKey + "." + entry.getKey(), entry.getValue()))
                .reduce((text, text2) -> text + " " + text2).orElse(I18n.translate(baseKey + ".unknown"))
        );
    }

    @Override
    protected void applyValue() {
        option.set((int) (this.value * 5 * 60));
    }
}
