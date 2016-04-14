package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class ChooseAreaActivity extends Activity{

	private static final int PROVINCE_LEVEL = 0;
	private static final int CITY_LEVEL = 1;
	private static final int COUNTY_LEVEL = 2;
	private TextView titleText;
	private ListView listView;
	private CoolWeatherDB coolWeatherDB;
	private ArrayAdapter<String> adapter;
	private ProgressDialog progressDialog;
	
	private List<String> dataList = new ArrayList<String>();
	private List<Province> provincesList ;
	private List<City> citiesList ;
	private List<County> countiesList ;
	private Province selectedProvince;
	private City selectedCity;
	private int currentLevel;
	private Boolean isFromWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeather = getIntent().getBooleanExtra("isFromWeather", false);
		SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
		if(pre.getBoolean("selectedCounty", false)&&!isFromWeather){
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		titleText = (TextView)findViewById(R.id.title_text);
		listView = (ListView)findViewById(R.id.list_view);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentLevel == PROVINCE_LEVEL){
					selectedProvince = provincesList.get(position);
					queryCity();
				}else if(currentLevel == CITY_LEVEL){
					selectedCity = citiesList.get(position);
					queryCounty();
				}else if(currentLevel == COUNTY_LEVEL){
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					String countyCode = countiesList.get(position).getCountyCode();
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
				}
			}
		});
		queryProvince();
	}
	
	private void queryProvince(){
		provincesList = coolWeatherDB.loadProvinces();
		if(provincesList.size()>0){
			dataList.clear();
			for(Province p:provincesList){
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = PROVINCE_LEVEL;
		}else{
			queryFromHttp(null,"province");
		}
	}
	
	private void queryCity(){
		citiesList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(citiesList.size()>0){
			dataList.clear();
			for(City c:citiesList){
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = CITY_LEVEL;
		}else{
			queryFromHttp(selectedProvince.getProvinceCode(),"city");
		}
	}
	
	private void queryCounty(){
		countiesList = coolWeatherDB.loadCounties(selectedCity.getId());
		if(countiesList.size()>0){
			dataList.clear();
			for(County c:countiesList){
				dataList.add(c.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = COUNTY_LEVEL;
		}else{
			queryFromHttp(selectedCity.getCityCode(),"county");
		}
	}
	
	private void queryFromHttp(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallBackListener(){
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, 
								"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if(type.equals("province")){
					result = Utility.handleProvinceResponse(response, coolWeatherDB);
				}else if(type.equals("city")){
					result = Utility.handleCityResponse(response, coolWeatherDB, selectedProvince.getId());
				}else if(type.equals("county")){
					result = Utility.handlrCountyResponse(response, coolWeatherDB, selectedCity.getId());
				}
				if(result){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							closeProgressDialog();
							if(type.equals("province")){
								queryProvince();
							}else if(type.equals("city")){
								queryCity();
							}else if(type.equals("county")){
								queryCounty();
							}
						}
					});
				}
			}
		});
	}
	
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(currentLevel==COUNTY_LEVEL){
			queryCity();
		}else if(currentLevel==CITY_LEVEL){
			queryProvince();
		}else{
			if(isFromWeather){
				Intent intent = new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
}
