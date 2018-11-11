package by.bsuir.bot.service;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceContext;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import java.util.TimeZone;

public class DialogflowService {
    public static Result getTalkResponseResult(String messageText) {
        return getResponseResult(messageText, "b04fb410ab6a4de7aac4be7e20fc1454", "TalkBot");
    }

    public static Result getPredictionResponseResult(String messageText) {
        return getResponseResult(messageText, "a9a4609d17814bf2a565b2715dd88cd5", "PredictionBot");
    }

    public static Result getWeatherResponseResult(String messageText) {
        return getResponseResult(messageText, "6598e948a5604b33a28b67db36b0cbd3", "WeatherBot");
    }

    public static Result getTranslationResponseResult(String messageText) {
        return getResponseResult(messageText, "1f4da2f256184a48b26ae65d0487fe81", "TranslationBot");
    }

    public static Result getFactResponseResult(String messageText) {
        return getResponseResult(messageText, "12027a431ca44454a3cdcf83914d23d0", "FactBot");
    }

    public static Result getResponseResult(String messageText, String token, String sessionId) {
        AIConfiguration config = new AIConfiguration(token, AIConfiguration.SupportedLanguages.Russian);
        AIServiceContext aiServiceContext = new AIServiceContext() {
            @Override
            public String getSessionId() {
                return sessionId;
            }

            @Override
            public TimeZone getTimeZone() {
                return null;
            }
        };
        AIDataService aiDataService = new AIDataService(config, aiServiceContext);
        AIRequest aiRequest = new AIRequest();
        aiRequest.setLanguage("ru");
        aiRequest.setSessionId(sessionId);
        aiRequest.setQuery(messageText);

        AIResponse response = null;
        try {
            response = aiDataService.request(aiRequest);
        } catch (AIServiceException e) {
            e.printStackTrace();
        }

        return response.getResult();
    }
}
