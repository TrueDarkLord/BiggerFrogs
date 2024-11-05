package me.truedarklord.biggerFrogs;

import me.truedarklord.biggerFrogs.listeners.FeedFrog;
import me.truedarklord.biggerFrogs.metrics.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class BiggerFrogs extends JavaPlugin {

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 23821);

        saveDefaultConfig();
        advertise();

        new FeedFrog(this);

    }

    private void advertise() {
        this.getServer().getConsoleSender().sendMessage(
                """
  
 §#00AA00================================§#ee2222

 ____  _                      \s
| __ )(_) __ _  __ _  ___ _ __\s
|  _ \\| |/ _` |/ _` |/ _ \\ '__|
| |_) | | (_| | (_| |  __/ |  \s
|____/|_|\\__, |\\__, |\\___|_|  \s
|  ___| _|___/ |___/_ ___     \s
| |_ | '__/ _ \\ / _` / __|    \s
|  _|| | | (_) | (_| \\__ \\    \s
|_|  |_|  \\___/ \\__, |___/    \s
                |___/         \s

§#f5da2aBy TrueDarkLord.
§#00AA00================================
§#f5da2aFeel free to buy me a coffee:  §#00AA00|
§bhttps://ko-fi.com/truedarklord §#00AA00|
§#00AA00================================
                        """
        );
    }
}
