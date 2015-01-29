package com.example.weatherguy.util;

import java.util.Iterator;

import android.R.array;
import android.text.TextUtils;

import com.example.weatherguy.db.WeatherGuyDB;
import com.example.weatherguy.db.WeatherGuyOpenHelper;
import com.example.weatherguy.model.City;
import com.example.weatherguy.model.County;
import com.example.weatherguy.model.Province;

public class Utility {
	// �����ʹ������������ʡ������
	public synchronized static boolean handleProvinceResponse(WeatherGuyDB weatherGuyDB,
			String response) {
		if (!TextUtils.isEmpty(response)) {
				String[] allProvinces = response.split(",");
				if (allProvinces != null && allProvinces.length > 0) {
					for (String p : allProvinces) {
						String[] array = p.split("\\|");
						Province province = new Province();
						province.setProvinceCode(array[0]);
						province.setProvinceName(array[1]);
						// ���������������ݴ洢��Province��
						weatherGuyDB.saveProvince(province);
					}
					return true;
				}
		}
		return false;
		
	}
	public synchronized static boolean handleCitiesResponse(WeatherGuyDB weatherGuyDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// ���������������ݴ洢��City��
					weatherGuyDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	public synchronized static boolean handleCountiesResponse(WeatherGuyDB weatherGuyDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
				if (allCounties != null && allCounties.length > 0) {
					for (String c : allCounties) {
						String[] array = c.split("\\|");
						County county = new County();
						county.setCountyCode(array[0]);
						county.setCountyName(array[1]);
						county.setCityId(cityId);
						// ����������洢��County��
						weatherGuyDB.saveCounty(county);
					}
					return true;
				}
		}
		return false;
	}
}
