package by.bsuir.bot.service;

import com.github.fedy2.weather.YahooWeatherService;
import com.github.fedy2.weather.data.Channel;
import com.github.fedy2.weather.data.unit.DegreeUnit;
import com.github.prominence.openweathermap.api.OpenWeatherMapManager;
import com.github.prominence.openweathermap.api.WeatherRequester;
import com.github.prominence.openweathermap.api.constants.Language;
import com.github.prominence.openweathermap.api.constants.Unit;
import com.github.prominence.openweathermap.api.exception.DataNotFoundException;
import com.github.prominence.openweathermap.api.exception.InvalidAuthTokenException;
import com.github.prominence.openweathermap.api.model.WeatherState;
import com.github.prominence.openweathermap.api.model.response.Weather;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {

    public static List<Channel> getYahooWeatherForecast(String location) {
        List<Channel> ch = new ArrayList<>();
        try {
            YahooWeatherService yws = new YahooWeatherService();
            YahooWeatherService.LimitDeclaration ld = yws.getForecastForLocation(location, DegreeUnit.CELSIUS);
            for (Channel l : ld.all()) {
                if (l.getLocation() != null) {
                    ch.add(l);
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ch;
    }

    public static String getOpenWeatherForecast(String location) {
        OpenWeatherMapManager owr = new OpenWeatherMapManager("0b1fa9161ce20830311b599a83b3cdff");
        Weather weather = null;
        try {
            WeatherRequester weatherRequester = owr.getWeatherRequester();
            weatherRequester.setUnitSystem(Unit.METRIC_SYSTEM); // градусы Цельсия
            weather = weatherRequester.getByCityName(location);
        } catch (InvalidAuthTokenException e) {
            e.printStackTrace();
            return null;
        } catch (DataNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return getInfo(weather, location);
    }

    public static String getOpenWeatherForecastWithLocation(double latitude, double longitude) {
        OpenWeatherMapManager owr = new OpenWeatherMapManager("0b1fa9161ce20830311b599a83b3cdff");
        Weather weather = null;
        try {
            WeatherRequester weatherRequester = owr.getWeatherRequester();
            weatherRequester.setUnitSystem(Unit.METRIC_SYSTEM); // градусы Цельсия
            weather = weatherRequester.getByCoordinates(latitude, longitude);
        } catch (InvalidAuthTokenException e) {
            e.printStackTrace();
            return null;
        } catch (DataNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return getInfo(weather, null);
    }

    private static String getInfo(Weather weather, String location) {
        String result = "Температура: " + weather.getTemperature() + weather.getTemperatureUnit() + '\n' +
                "Влажность: " + weather.getHumidityPercentage() + "%" + '\n' +
                "Ветер: " + weather.getWind().getSpeed() + YandexTranslator.translate("ru", weather.getWind().getUnit()) + '\n' +
                "Облачность: " + weather.getClouds().getCloudiness() + "%" + '\n' +
                "Давление: " + weather.getPressure() + weather.getPressureUnit();

        if (weather.getWeatherStates().size() > 0) {
            result += '\n' + "Атмосферные явления: ";
        }
        for (WeatherState w : weather.getWeatherStates()) {
            result += YandexTranslator.translate("ru", w.getDescription()).trim() + ", ";
        }
        if (weather.getWeatherStates().size() > 0) {
            result = result.substring(0, result.lastIndexOf(","));
        }
        String cityName = null;
        if (location != null && !location.equals("")) {
            cityName = location;
        } else if (weather.getCityName() != null && !weather.getCityName().equals("")) {
            cityName = YandexTranslator.translate(Language.RUSSIAN, weather.getCityName());
        }

        if (cityName != null && !cityName.equals("")) {
            result += '\n' + "Локация: " + cityName + " (" + weather.getCoordinates().getLatitude() + "," + weather.getCoordinates().getLongitude() + ")";
        }
        return result;
    }
}
