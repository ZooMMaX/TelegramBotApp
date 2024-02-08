import ru.zoommax.botapp.BotApp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        new Thread(new BotApp("BOT_TOKEN", new Listeners())).start();
    }
}
