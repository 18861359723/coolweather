package com.coolweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {
	
	public static synchronized boolean handleProvinceResponse(
			 String response, CoolWeatherDB coolWeatherDB){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces = response.split(",");
			if(allProvinces!=null&&allProvinces.length>0){
			for(String province:allProvinces){
				String[] array = province.split("\\|");
				Province p = new Province();
				p.setProvinceCode(array[0]);
				p.setProvinceName(array[1]);
				coolWeatherDB.saveProvinces(p);
			}
				return true;
			}
		}
		return false;
	}
	
	public static boolean handleCityResponse(
			String response,CoolWeatherDB coolWeatherDB,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities = response.split(",");
			if(allCities!=null&&allCities.length>0){
				for(String city:allCities){
					String[] array = city.split("\\|");
					City c = new City();
					c.setCityCode(array[0]);
					c.setCityName(array[1]);
					c.setProvinceId(provinceId);
					coolWeatherDB.saveCity(c);
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean handlrCountyResponse(
			String response,CoolWeatherDB coolWeatherDB,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties = response.split(",");
			if(allCounties!=null&&allCounties.length>0){
				for(String county:allCounties){
					String[] array = county.split("\\|");
					County c = new County();
					c.setCountyCode(array[0]);
					c.setCountyName(array[1]);
					c.setCityId(cityId);
					coolWeatherDB.saveCounty(c);
				}
				return true;
			}
		}
		return false;
	}
	
	public static void handleWeatherReaponse(Context context,String response){
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weather = weatherInfo.getString("weather");
			String ptime = weatherInfo.getString("ptime");
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weather,ptime);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,
			String temp2,String weather,String ptime){
		SharedPreferences.Editor editor = PreferenceManager.
				getDefaultSharedPreferences(context).edit();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
		editor.putBoolean("selectedCounty", true);
		editor.putString("cityName", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weather);
		editor.putString("current_date", sdf.format(new Date()));
		editor.putString("publish_time", ptime);
		editor.commit();
	}
}
