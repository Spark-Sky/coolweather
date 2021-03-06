package com.tdd.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tdd.coolweather.gson.Forecast;
import com.tdd.coolweather.gson.Weather;
import com.tdd.coolweather.service.AutoUpdateService;
import com.tdd.coolweather.util.HttpUtil;
import com.tdd.coolweather.util.Utility;

import java.io.IOException;
import java.nio.file.Watchable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity
{

    private static final String TAG = "WeatherActivity";

    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView apiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        //?????????????????????
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        apiText = (TextView) findViewById(R.id.api_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.white));//????????????????????????
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null)
        {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else
        {
            loadBingPic();
        }

        String weatherId;
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null)
        {
            Weather weather = Utility.handleWeatherResponse(weatherString);//??????JSON
            /**
             * ????????????
             */
            weatherId = weather.mBasic.weatherId;
            showWeatherInfo(weather);
        }
        else
        {
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            /**
             * ????????????
             */
            requestWeather(weatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                requestWeather((weatherId));
            }
        });

        navButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


    }

    /**
     * ????????????id????????????????????????
     */
    public void requestWeather(final String weatherId)
    {
        Log.i(TAG, "requestWeather");
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=57bf2573de79438a8bb14a09e213a4f0";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(WeatherActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);//????????????????????????????????????????????????
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseText = response.body().string();//?????????????????????
                Log.i(TAG, responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);//???????????????Weather??????
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (weather != null && "ok".equals(weather.status))
                        {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            Log.i(TAG, "max:" + weather.mForecastList.get(0).temperature.max);
                            /**
                             * ??????????????????
                             */
                            showWeatherInfo(weather);
                        }
                        else
                        {
                            Toast.makeText(WeatherActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }


        });
        loadBingPic();
    }

    /**
     * ???????????????Weather?????????????????????
     */
    private void showWeatherInfo(Weather weather)
    {
        if(weather!=null && "ok".equals(weather.status))
        {
            String cityName = weather.mBasic.cityName;//??????
            String updateTime = weather.mBasic.update.updateTime.split(" ")[1];//05:15
            String degree = weather.mNow.temperature + "^C";//14^c
            String weatherInfo = weather.mNow.more.info;//???
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();
            for (Forecast forecast : weather.mForecastList)
            {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
                TextView dateText = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                TextView maxText = (TextView) view.findViewById(R.id.max_text);
                TextView minText = (TextView) view.findViewById(R.id.min_text);

                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max);
                maxText.setText(forecast.temperature.min);
                forecastLayout.addView(view);
            }
            if (weather.mAQI != null)
            {
                apiText.setText(weather.mAQI.city.aqi);
                pm25Text.setText(weather.mAQI.city.pm25);
            }

            String comfort = "????????????" + weather.mSuggestion.comfort.info;
            String carWash = "???????????????" + weather.mSuggestion.CarWash.info;
            String sport = "???????????????" + weather.mSuggestion.sport.info;

            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            weatherLayout.setVisibility(View.VISIBLE);

            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }
        else
        {
            Toast.makeText(WeatherActivity.this,"??????????????????",Toast.LENGTH_LONG).show();
        }


    }

    /**
     * ????????????????????????
     */
    private void loadBingPic()
    {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

}