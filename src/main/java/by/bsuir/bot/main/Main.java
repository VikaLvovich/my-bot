package by.bsuir.bot.main;

import by.bsuir.bot.entity.BalabolBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new BalabolBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
