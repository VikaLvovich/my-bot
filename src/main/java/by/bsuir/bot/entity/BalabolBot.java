package by.bsuir.bot.entity;

import ai.api.model.Result;
import by.bsuir.bot.service.DialogflowService;
import by.bsuir.bot.service.WeatherService;
import by.bsuir.bot.service.YandexTranslator;
import com.github.prominence.openweathermap.api.constants.Language;
import com.google.gson.JsonElement;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalabolBot extends TelegramLongPollingBot {

    private boolean needLocation;
    private Map<Long, TypeAgent> types = new HashMap<>();
    private Map<Long, String> languages = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String messageText = message.getText();
                switch (messageText) {
                    case "/start":
                        messageText = "Привет";
                        types.put(message.getChatId(), TypeAgent.TALK);
                        break;
                    case "/weather":
                        messageText = "Погода";
                        types.put(message.getChatId(), TypeAgent.WEATHER);
                        break;
                    case "/translation":
                        types.put(message.getChatId(), TypeAgent.TRANSLATION);
                        break;
                    case "/prediction":
                        types.put(message.getChatId(), TypeAgent.PREDICTION);
                        break;
                    case "/fact":
                        types.put(message.getChatId(), TypeAgent.FACT);
                        break;
                    case "/talk":
                        messageText = "Поговорим";
                        types.put(message.getChatId(), TypeAgent.TALK);
                        break;
                }
                if (!messageText.equals("/prediction") && types.get(message.getChatId()) == TypeAgent.PREDICTION) {
                    messageText = "ask";
                }
                sendAnswer(message.getChatId(), messageText);
            } else if (message.hasLocation()) {
                sendAnswer(message.getChatId(), message.getLocation());
            }
        }
    }

    public String getAnswer(String messageText, Long chatId) {
        Result responseResult;
        switch (types.get(chatId)) {
            case WEATHER:
                responseResult = DialogflowService.getWeatherResponseResult(messageText);
                break;
            case TRANSLATION:
                responseResult = DialogflowService.getTranslationResponseResult(messageText);
                break;
            case PREDICTION:
                responseResult = DialogflowService.getPredictionResponseResult(messageText);
                break;
            case FACT:
                responseResult = DialogflowService.getFactResponseResult(messageText);
                break;
            default:
                responseResult = DialogflowService.getTalkResponseResult(messageText);
                break;
        }
        if (responseResult.getAction().equals("weather")) {
            return doWeatherForecast(responseResult);
        } else if (responseResult.getAction().equals("prediction")) {
            return doPrediction(responseResult);
        } else if (types.get(chatId) == TypeAgent.TRANSLATION && !messageText.equals("/translation")) {
            return doTranslation(responseResult, chatId);
        } else {
            return doSpeech(responseResult);
        }
    }

    public String doWeatherForecast(Result responseResult) {
        HashMap<String, JsonElement> params = responseResult.getParameters();
        if (params.containsKey("address")) {
            String address = params.get("address").getAsJsonObject().get("city").getAsString();
            String weather = WeatherService.getOpenWeatherForecast(address);
            if (weather != null && !weather.equals("")) {
                return weather;
            } else {
                return "Не удалось найти информацию о локации " + address;
            }
        }
        needLocation = true;
        return "Уточни геопозицию. Без нее я не смогу тебе помочь.";
    }

    public String doPrediction(Result responseResult) {
        return responseResult.getFulfillment().getSpeech();
    }

    public String doTranslation(Result responseResult, Long chatId) {
        HashMap<String, JsonElement> params = responseResult.getParameters();
        String info = "";
        if (params != null && params.containsKey("language")) {
            switch (params.get("language").getAsString()) {
                case "английский":
                    languages.put(chatId, Language.ENGLISH);
                    break;
                case "немецкий":
                    languages.put(chatId, Language.DUTCH);
                    break;
                case "французский":
                    languages.put(chatId, Language.FRENCH);
                    break;
                case "испанский":
                    languages.put(chatId, Language.SPANISH);
                    break;
                case "итальянский":
                    languages.put(chatId, Language.ITALIAN);
                    break;
                default:
                    languages.put(chatId, Language.ENGLISH);
                    info = "К сожелению, я не знаю этого языка. Поэтому буду использовать английский." + '\n';
                    break;
            }
        } else {
            if (languages.containsKey(chatId)) {
                return YandexTranslator.translate(languages.get(chatId), responseResult.getResolvedQuery());
            } else {
                return YandexTranslator.translate(Language.ENGLISH, responseResult.getResolvedQuery());
            }
        }
        return info + responseResult.getFulfillment().getSpeech();
    }

    public String doSpeech(Result responseResult) {
        return responseResult.getFulfillment().getSpeech();
    }

    public synchronized void sendAnswer(Long chatId, String messageText) {
        SendMessage answer = new SendMessage();
        answer.enableMarkdown(true);
        answer.setChatId(chatId);
        answer.setText(getAnswer(messageText, chatId));
        if (needLocation) {
            setLocationButton(answer);
            needLocation = false;
        }
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }

    public synchronized void sendAnswer(Long chatId, Location location) {
        SendMessage answer = new SendMessage();
        answer.enableMarkdown(true);
        answer.setChatId(chatId);
        answer.setText(WeatherService.getOpenWeatherForecastWithLocation(location.getLatitude(), location.getLongitude()));
        if (needLocation) {
            setLocationButton(answer);
            needLocation = false;
        }
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }

    public synchronized void setLocationButton(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardButton kb = new KeyboardButton("Отправить данные о геопозиции");
        kb.setRequestLocation(true);
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(kb);
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    @Override
    public String getBotUsername() {
        return "Balabol2000Bot";
    }

    @Override
    public String getBotToken() {
        return "620063258:AAE3pRzKnZ23hMFWnKAoKpcooGRThtzBuNY";
    }
}