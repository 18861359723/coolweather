package com.coolweather.app.util;

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
}
