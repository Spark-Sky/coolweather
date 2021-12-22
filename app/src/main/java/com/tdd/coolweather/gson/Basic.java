package com.tdd.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic
{
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;//城市的id,其可以用来查询天气

    public Update update;

    public class Update
    {
        @SerializedName("loc")
        public String updateTime;
    }
}
