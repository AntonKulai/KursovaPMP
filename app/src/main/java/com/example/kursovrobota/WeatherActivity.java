package com.example.kursovrobota;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kursovrobota.model.WeatherResponse;
import com.example.kursovrobota.network.ApiClient;
import com.example.kursovrobota.network.WeatherService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WeatherActivity extends AppCompatActivity {

    private TextView textViewTemperature;
    private TextView textViewDescription;
    private TextView textViewFeelsLike;
    private TextView textViewHumidity;
    private TextView textViewWindSpeed;
    private String cityName;
    private String apiKey = "fcd4a100d6750cfd5d4a22cb0fc4ac1e";
    private static final String TAG = "WeatherActivity";

    private Map<String, String> weatherTranslations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewFeelsLike = findViewById(R.id.textViewFeelsLike);
        textViewHumidity = findViewById(R.id.textViewHumidity);
        textViewWindSpeed = findViewById(R.id.textViewWindSpeed);

        cityName = getIntent().getStringExtra("CITY_NAME");

        initializeTranslations();

        if (cityName == null || cityName.isEmpty()) {
            Toast.makeText(this, "Назва міста не передана", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Назва міста не передана");
        } else {
            getWeatherData(cityName);
        }
    }

    private void initializeTranslations() {
        weatherTranslations = new HashMap<>();
        weatherTranslations.put("clear sky", "ясне небо");
        weatherTranslations.put("few clouds", "мало хмар");
        weatherTranslations.put("scattered clouds", "розсіяні хмари");
        weatherTranslations.put("broken clouds", "розірвані хмари");
        weatherTranslations.put("shower rain", "зливовий дощ");
        weatherTranslations.put("rain", "дощ");
        weatherTranslations.put("thunderstorm", "гроза");
        weatherTranslations.put("snow", "сніг");
        weatherTranslations.put("mist", "туман");
    }

    private void getWeatherData(String cityName) {
        WeatherService service = ApiClient.getClient().create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeather(cityName, apiKey, "metric");

        Log.d(TAG, "Запит URL: " + call.request().url());

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        textViewTemperature.setText(String.format("%.2f°C", weatherResponse.getMain().getTemp()));
                        String description = weatherResponse.getWeather()[0].getDescription();
                        String translatedDescription = weatherTranslations.getOrDefault(description, description);
                        textViewDescription.setText(translatedDescription);
                        textViewFeelsLike.setText(String.format("Відчувається як: %.2f°C", weatherResponse.getMain().getFeelsLike()));
                        textViewHumidity.setText(String.format("Вологість: %d%%", weatherResponse.getMain().getHumidity()));
                        textViewWindSpeed.setText(String.format("Швидкість вітру: %.2f м/с", weatherResponse.getWind().getSpeed()));
                    } else {
                        Log.e(TAG, "Response body is null");
                        Toast.makeText(WeatherActivity.this, "Не вдалося отримати дані", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Response is not successful: " + errorBody);
                        Toast.makeText(WeatherActivity.this, "Помилка: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage());
                Toast.makeText(WeatherActivity.this, "Помилка запиту: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
