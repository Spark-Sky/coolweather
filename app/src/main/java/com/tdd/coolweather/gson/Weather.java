package com.tdd.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather
{
    @SerializedName("status")
    public String status;
    @SerializedName("basic")
    public Basic mBasic;
    @SerializedName("aqi")
    public AQI mAQI;
    @SerializedName("now")
    public Now mNow;
    @SerializedName("suggestion")
    public Suggestion mSuggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> mForecastList;
}
