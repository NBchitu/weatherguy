package com.example.weatherguy.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.weatherguy.db.WeatherGuyDB;
import com.example.weatherguy.model.City;
import com.example.weatherguy.model.County;
import com.example.weatherguy.model.Province;
import com.example.weatherguy.util.HttpCallbackListener;
import com.example.weatherguy.util.HttpUtil;
import com.example.weatherguy.util.Utility;


//import android.R;
import android.R.integer;
import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.weatherguy.R;

public class ChooseAreaActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WeatherGuyDB weatherGuyDB;
	private List<String> dataList = new ArrayList<>();
	
	// ʡ�б�
	private List<Province> provinceList;
	// ���б�
	private List<City> cityList;
	// �ر�
	private List<County> countyList;
	
	// ѡ���ʡ��
	private Province selectedProvince;
	
	// ѡ��ĳ���
	private City selectedCity;
	
	// ��ǰѡ�еļ���
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
				dataList);
		listView.setAdapter(adapter);
		weatherGuyDB = WeatherGuyDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,long arg3){
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
					
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				} 
			}
		});
		queryProvinces();
	}
	
	// ��ѯȫ������ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	private void queryProvinces(){
		provinceList = weatherGuyDB.loadProvinces();
		if (provinceList.size() > 0) {
			Log.d("1023", "queryProvinces>> from DB");
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
			
		} else {
			Log.d("1023", "queryProvinces>> from Web");
			queryFromServer(null, "province");
		}
	}
	
	// ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û���ٵ���������ѯ
	private void queryCities() {
		
		cityList = weatherGuyDB.loadCities(selectedProvince.getId());
		Log.d("1025", "queryCities.size()" + cityList.size());
		Log.d("1025", "loadCities.selectedProvince.getId()" + selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city  : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			Log.d("1203", "selectedProvince>>" + selectedProvince.getProvinceCode());
			queryFromServer(selectedProvince.getProvinceCode(), "city");			
		}
	}
	
	// ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û����ȥ�������ϲ�ѯ
	private void queryCounties() {
		countyList = weatherGuyDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");

		}
	}
	// ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ������
	 private void queryFromServer(final String code, final String type) {
		 String address;
		 if (!TextUtils.isEmpty(code)) {
			 Log.d("1023", "QueryFromServer city"+code);
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		} else {
			Log.d("1023", "QueryFromServer city all");
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				Log.d("1023", "ChooseAreaActivity: onFinish()|type>>" + type);
				
				if ("province".equals(type)) {
					result = Utility.handleProvinceResponse(weatherGuyDB, response);
					Log.d("1023", "ChooseAreaActivity: handleCitiesResponse>>province");
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(weatherGuyDB, response, selectedProvince.getId());
					Log.d("1023", "ChooseAreaActivity: handleCitiesResponse>>city");
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(weatherGuyDB, response, selectedCity.getId());
					Log.d("1023", "ChooseAreaActivity: handleCitiesResponse>>county");
				}
				Log.d("1025", "result>>" + result );
				if (result) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							Log.d("1025", "closeProgressDialog()" );
							if ("province".equals(type)) {
								queryProvinces();
								Log.d("1025", "queryProvinces()" );
							} else if ("city".equals(type)) {
								queryCities();
								Log.d("1025", "queryCities()" );
							} else if ("county".equals(type)) {
								queryCounties();
								Log.d("1025", "queryCounties()" );
							}

						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				// ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
						
					}
				});
				
			}
		});
	}
	 
	 // ��ʾ���ȶԻ���
	 private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	 // �رս��ȶԻ���
	 private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	 
	 // ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�
	 @Override
	 public void onBackPressed(){
		 if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			finish();
		}
	 }
}
