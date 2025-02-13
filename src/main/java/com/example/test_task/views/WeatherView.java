package com.example.test_task.views;

import com.example.test_task.entities.RequestHistory;
import com.example.test_task.repositories.RequestHistoryRepository;
import com.example.test_task.services.WeatherService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("/")
public class WeatherView extends VerticalLayout {

    public WeatherView(WeatherService weatherService, RequestHistoryRepository requestHistoryRepository) {

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setSizeFull();

        Grid<Map<String, String>> weatherHistoryGrid = new Grid<>();
        weatherHistoryGrid.addColumn(map -> map.get("date")).setHeader("Date");
        weatherHistoryGrid.addColumn(map -> map.get("explanation")).setHeader("Explanation");
        weatherHistoryGrid.addColumn(map -> map.get("latitude")).setHeader("Latitude");
        weatherHistoryGrid.addColumn(map -> map.get("longitude")).setHeader("Longitude");

        List<Map<String, String>> weatherData = new ArrayList<>();

        List<RequestHistory> historyList = requestHistoryRepository.findAll();
        for (RequestHistory history : historyList) {
            Map<String, String> weatherEntry = new HashMap<>();
            weatherEntry.put("date", history.getTime().toString());
            weatherEntry.put("explanation", history.getExplanation());
            weatherEntry.put("latitude", history.getLatitude().toString());
            weatherEntry.put("longitude", history.getLongitude().toString());
            weatherData.add(weatherEntry);
        }

        weatherHistoryGrid.setItems(weatherData);

        weatherHistoryGrid.setItems(weatherData);
        weatherHistoryGrid.setWidthFull();
        rightLayout.add(weatherHistoryGrid);

        Div separator = new Div();
        separator.getStyle().set("width", "2px").set("background-color", "#ccc");

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setSizeFull();
        leftLayout.setAlignItems(Alignment.CENTER);

        NativeLabel titleLabel = new NativeLabel("Weather Information");
        titleLabel.getStyle().set("font-weight", "bold").set("font-size", "24px");

        TextField latField = new TextField("Latitude");
        TextField lonField = new TextField("Longitude");
        Button fetchWeatherButton = new Button("Show Weather");

        NativeLabel weatherLabel = new NativeLabel();
        NativeLabel temperatureLabel = new NativeLabel();
        NativeLabel humidityLabel = new NativeLabel();

        weatherLabel.getStyle().set("font-size", "18px").set("color", "#6C8CD5").set("font-weight", "bold");
        temperatureLabel.getStyle().set("font-size", "18px").set("color", "#6C8CD5").set("font-weight", "bold");
        humidityLabel.getStyle().set("font-size", "18px").set("color", "#6C8CD5").set("font-weight", "bold");

        Image weatherImage = new Image();
        weatherImage.setVisible(false);

        leftLayout.add(titleLabel, latField, lonField, fetchWeatherButton, weatherLabel, temperatureLabel, humidityLabel, weatherImage);

        mainLayout.add(leftLayout, separator, rightLayout);
        add(mainLayout);

        fetchWeatherButton.addClickListener(event -> {
            try {
                String latText = latField.getValue();
                String lonText = lonField.getValue();

                if (!isValidCoordinate(latText, -90, 90)) {
                    Notification.show("Invalid latitude! Must be between -90 and 90.", 5000, Notification.Position.TOP_END);
                    weatherLabel.setVisible(false);
                    temperatureLabel.setVisible(false);
                    humidityLabel.setVisible(false);
                    weatherImage.setVisible(false);
                    return;
                }

                if (!isValidCoordinate(lonText, -180, 180)) {
                    Notification.show("Invalid longitude! Must be between -180 and 180.", 5000, Notification.Position.TOP_END);
                    weatherLabel.setVisible(false);
                    temperatureLabel.setVisible(false);
                    humidityLabel.setVisible(false);
                    weatherImage.setVisible(false);
                    return;
                }

                weatherLabel.setVisible(true);
                temperatureLabel.setVisible(true);
                humidityLabel.setVisible(true);

                double latitude = Double.parseDouble(latText);
                double longitude = Double.parseDouble(lonText);

                Map<String, Object> weatherResponse = weatherService.getWeather(latitude, longitude);

                String temperature = weatherResponse.get("main") != null ? ((Map<?, ?>) weatherResponse.get("main")).get("temp") + "°C" : "N/A";
                String condition = weatherResponse.get("weather") != null ? ((Map<?, ?>) ((java.util.List<?>) weatherResponse.get("weather")).get(0)).get("description") + "" : "N/A";
                String humidity = weatherResponse.get("main") != null ? ((Map<?, ?>) weatherResponse.get("main")).get("humidity") + "%" : "N/A";
                String iconCode = weatherResponse.get("weather") != null ? ((Map<?, ?>) ((java.util.List<?>) weatherResponse.get("weather")).get(0)).get("icon") + "" : "";

                RequestHistory newEntry = new RequestHistory(latitude, longitude, condition);
                requestHistoryRepository.save(newEntry);

                Map<String, String> newWeatherEntry = new HashMap<>();
                newWeatherEntry.put("date", newEntry.getTime().toString());
                newWeatherEntry.put("explanation", newEntry.getExplanation());
                newWeatherEntry.put("latitude", Double.toString(newEntry.getLatitude()));
                newWeatherEntry.put("longitude", Double.toString(newEntry.getLongitude()));

                weatherData.add(weatherData.size(), newWeatherEntry);
                weatherHistoryGrid.setItems(weatherData);

                weatherLabel.setText("Condition: " + condition);
                temperatureLabel.setText("Temperature: " + temperature);
                humidityLabel.setText("Humidity: " + humidity);

                if (!iconCode.isEmpty()) {
                    weatherImage.setSrc("https://openweathermap.org/img/wn/" + iconCode + "@2x.png");
                    weatherImage.setVisible(true);
                } else {
                    weatherImage.setVisible(false);
                }

            } catch (Exception e) {
                Notification.show("Error fetching weather data.", 5000, Notification.Position.TOP_END);
                weatherLabel.setVisible(false);
                temperatureLabel.setVisible(false);
                humidityLabel.setVisible(false);
                weatherImage.setVisible(false);
            }
        });
    }

    private boolean isValidCoordinate(String value, double minBoundary, double maxBoundary) {
        if (!NumberUtils.isCreatable(value)) {
            return false;
        }
        double num = Double.parseDouble(value);
        return num >= minBoundary && num <= maxBoundary;
    }
}
