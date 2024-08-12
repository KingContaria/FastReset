package fast_reset.client.widget;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.mcsr.speedrunapi.config.api.SpeedrunOption;

import java.util.LinkedHashMap;

public class TimeSliderWidget extends SliderWidget {
    private final SpeedrunOption<Integer> option;

    public TimeSliderWidget(int x, int y, int width, int height, Text text, SpeedrunOption<Integer> option) {
        super(x, y, width, height, text, (double) option.get() / (5 * 60));
        this.option = option;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        String baseKey = "speedrunapi.config." + option.getModID() + ".option." + option.getID();
        int current = option.get();
        int hours = current / (60 * 60);
        LinkedHashMap<String, Integer> times = new LinkedHashMap<>(3);
        times.put("hours", hours);
        times.put("minutes", (current) % (60 * 60) / 60);
        times.put("seconds", current % 60);
        this.setMessage(times
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> (MutableText) new TranslatableText(baseKey + "." + entry.getKey(), entry.getValue()))
                .reduce((text, text2) -> text.append(" ").append(text2)).orElse(new TranslatableText(baseKey + ".unknown"))
        );
    }

    @Override
    protected void applyValue() {
        option.set((int) (this.value * 5 * 60));
    }
}
