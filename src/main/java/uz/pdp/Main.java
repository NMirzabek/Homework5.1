package uz.pdp;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static TelegramBot bot = new TelegramBot("7688885294:AAEKpk3kHun9qKUvi9avTMeCWIrU1_k6KrU");
    static ExecutorService executorService = Executors.newFixedThreadPool(10);

    static {
        try {
            DB.inport();
        } catch (FileNotFoundException e) {

        }
    }

    public static void main(String[] args) {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                executorService.execute(() -> {
                    BotServer.server(update);
                });
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
