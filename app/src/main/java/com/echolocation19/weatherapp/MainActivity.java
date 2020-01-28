package com.echolocation19.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.echolocation19.weatherapp.Utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String TAG = "CheckErrors";
  private TextView titleTextView;
  private TextView locationInfoTextView;
  private EditText cityEditText;
  private Button getWeatherButton;
  private SharedPreferences sharedPreferences;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    cityEditText = findViewById(R.id.cityEditText);
    titleTextView = findViewById(R.id.titleTextView);
    locationInfoTextView = findViewById(R.id.locationInfoTextView);
    locationInfoTextView.setVisibility(View.GONE);
    getWeatherButton = findViewById(R.id.getWeatherButton);
    getWeatherButton.setOnClickListener(this);

    sharedPreferences = getSharedPreferences(Utils.WEATHER_ID, MODE_PRIVATE);
  }

  @Override
  public void onClick(View v) {
    String city = cityEditText.getText().toString().trim();
    Log.d(TAG, "onClick: " + city);
    titleTextView.setVisibility(View.GONE);
    getLocationInfo(city);
  }

  private void getLocationInfo(final String city) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    String unitsName = sharedPreferences.getString("units", "celsius");
    String units;
    final String deg;
    if (unitsName.equals("kelvin")) {
      units = "";
      deg = "K";
    } else if (unitsName.equals("fahrenheit")) {
      units = "imperial";
      deg = "°F";
    } else {
      units = "metric";
      deg = "°C";
    }
    String url = MessageFormat.format("https://api.openweathermap.org/data/2.5/weather?q={0}&units={1}&APPID={2}", city, units, BuildConfig.WEATHER_API_KEY);
    RequestQueue queue = Volley.newRequestQueue(this);
    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {
        try {
          titleTextView.setVisibility(View.GONE);
          cityEditText.setText("");
          locationInfoTextView.setText(response.toString());
          JSONObject main = response.getJSONObject("main");
          JSONObject wind = response.getJSONObject("wind");
          locationInfoTextView.setText(MessageFormat.format("City: {0}\nTemperature: {1}{2}\nMin Temperature: {3}{4}\nMax Temperature: {5}{6}\nWind: {7}m/s",
                  city,
                  main.getString("temp"),
                  deg,
                  main.getString("temp_min"),
                  deg,
                  main.getString("temp_max"),
                  deg,
                  wind.getString("speed")));
          locationInfoTextView.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Toast.makeText(MainActivity.this, "Oops, something went wrong", Toast.LENGTH_SHORT).show();
      }
    });
    queue.add(jsonObjectRequest);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.settings:
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
