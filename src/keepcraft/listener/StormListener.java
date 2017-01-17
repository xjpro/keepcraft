package keepcraft.listener;

import java.util.Random;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class StormListener implements Listener {

    private final Random r = new Random();

    @EventHandler(priority = EventPriority.LOW)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) // true == rain
        {
            if (r.nextDouble() < 0.75) {
                event.setCancelled(true);
            }
        }
    }
}
