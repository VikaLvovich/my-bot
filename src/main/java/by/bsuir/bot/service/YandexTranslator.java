package by.bsuir.bot.service;

import com.github.prominence.openweathermap.api.constants.Language;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

public class YandexTranslator {

    public static String translate(String lang, String input) {
        try {
            String urlStr = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20181110T131537Z.6b12ebb636fdfe41.8d3beb6c1c95e3c12e75e02a161bc0af0d6e91b9";
            URL urlObj = new URL(urlStr);
            HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes("text=" + URLEncoder.encode(input, "UTF-8") + "&lang=" + lang);

            InputStream response = connection.getInputStream();
            String json = new java.util.Scanner(response).nextLine();
            int start = json.indexOf("[");
            int end = json.indexOf("]");
            String translated = json.substring(start + 2, end - 1);
            if (translated.equals(input) && !lang.equals(Language.RUSSIAN)) {
                return translate(Language.RUSSIAN, input);
            }
            return translated;
        } catch (IOException e) {
            e.printStackTrace();
            return input;
        }
    }
}
