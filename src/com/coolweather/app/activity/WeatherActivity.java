package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{
	
	private LinearLayout weatherLayout ;
	private TextView cityName;
	private TextView publishTime;
	private TextView currentDate;
	private TextView weatherDesp;
	private TextView temp1;
	private TextView temp2;
	
	private Button home;
	private Button refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather);
		weatherLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
		cityName = (TextView)findViewById(R.id.city_name);
		publishTime = (TextView)findViewById(R.id.publish);
		currentDate = (TextView)findViewById(R.id.current_date);
		weatherDesp = (TextView)findViewById(R.id.weather_desp);
		temp1 = (TextView)findViewById(R.id.temp1);
		temp2 = (TextView)findViewById(R.id.temp2);
		home = (Button)findViewById(R.id.home);
		refresh = (Button)findViewById(R.id.refresh);
		home.setOnClickListener(this);
		refresh.setOnClickListener(this);
		String countyCode = getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode)){
			publishTime.setText("更新中...");
			currentDate.setText("某年某月某日");
			weatherLayout.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			searchWeatherCode(countyCode);
		}else {
			showWeatherInfo();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.home:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("isFromWeather", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh:
			publishTime.setText("同步中...");
			SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = pre.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
			searchWeatherInfo(weatherCode);
			}
			break;
			default:
				break;
		}
	}
	
	private void searchWeatherCode(String countyCode){
		String address = "http://www.weather.com.cn/data/" +
				"list3/city"+countyCode+".xml";
		searchFromServer(address,"county");
	}
	
	private void searchWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/" +
				"cityinfo/"+weatherCode+".html";
		searchFromServer(address,"weather");
	}
	
	private void searchFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
			
			@Override
			public void onFinish(String response) {
				if(!TextUtils.isEmpty(response)){
					if("county".equals(type)){
						String[] array = response.split("\\|");
						if(array!=null&&array.length==2){
							String weatherCode = array[1];
							searchWeatherInfo(weatherCode);
						}
					}else if("weather".equals(type)){
						Utility.handleWeatherReaponse(WeatherActivity.this, response);
						runOnUiThread(new Runnable(){
							@Override
							public void run() {
								showWeatherInfo();								
							}
						});
					}
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						publishTime.setText("同步失败");
					}
				});
			}
		});
	}
	
	private void showWeatherInfo(){
		SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
		 cityName.setText(pre.getString("cityName", ""));
		 temp1.setText(pre.getString("temp1", ""));
		 temp2.setText(pre.getString("temp2", ""));
		 weatherDesp.setText(pre.getString("weather_desp", ""));
		 currentDate.setText(pre.getString("current_date", ""));
		 publishTime.setText("今天"+pre.getString("publish_time", "")+"发布");
		 weatherLayout.setVisibility(View.VISIBLE);
		 cityName.setVisibility(View.VISIBLE);
		 Intent intent = new Intent(this,AutoUpdateService.class);
		 startService(intent);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
}
