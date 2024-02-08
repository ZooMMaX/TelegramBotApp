import ru.zoommax.botapp.BotApp;

public class Main {
    public static void main(String[] args) {
        new Thread(new BotApp("BOT_TOKEN", new Listeners())).start();
    }
}
