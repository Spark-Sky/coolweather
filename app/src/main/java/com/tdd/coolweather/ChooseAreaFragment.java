package com.tdd.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.tdd.coolweather.db.City;
import com.tdd.coolweather.db.County;
import com.tdd.coolweather.db.Province;
import com.tdd.coolweather.util.HttpUtil;
import com.tdd.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment
{
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog mProgressDialog;
    private TextView mTitleText;
    private Button mBackButton;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;//数据适配器，数据和视图之间的桥梁,其包含着mdataList
    private List<String> mdataList = new ArrayList<>();//显示视图的数据
    /**
     * 自定义类变量
     */
    private List<Province> mProvinceList;//省列表
    private List<City> mCityList;//市列表
    private List<County> mCountyList;//县列表
    private Province mSelectedProvince;//选中的省份
    private City mSelectedCity;//选中的城市
    private int mcurrentLevel;//当前选中的级别

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleText = (TextView) view.findViewById(R.id.title_text);
        mBackButton = (Button) view.findViewById(R.id.back_button);
        mListView = (ListView) view.findViewById(R.id.list_item);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mdataList);
        mListView.setAdapter(mAdapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (mcurrentLevel == LEVEL_PROVINCE)
                {
                    mSelectedProvince = mProvinceList.get(position);
                    queryCities();

                }
                else if (mcurrentLevel == LEVEL_CITY)
                {
                    mSelectedCity = mCityList.get(position);
                    queryCounties();
                }else if (mcurrentLevel == LEVEL_COUNTY)
                {
                    String weatherId = mCountyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity)
                    {
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity)
                    {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawer(GravityCompat.START);
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }



                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mcurrentLevel == LEVEL_COUNTY)
                {
                    queryCities();
                }
                else if(mcurrentLevel == LEVEL_CITY)
                {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    //私有方法
    /**
     * 查询全国所有的省，优先从数据库查询，如果没有再到服务器上查询
     */
    private void queryProvinces()
    {
        mTitleText.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0)
        {
            mdataList.clear();
            for (Province province : mProvinceList)
            {
                mdataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mcurrentLevel = LEVEL_PROVINCE;
        }
        else
        {
            String address = "http://guolin.tech/api/china";
            /**
             * 从服务器上获取数据
             */
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询省所有的市，优先从数据库查询，如果没有再到服务器上查询
     */
    private void queryCities()
    {
        Log.i(TAG,"queryCities");
        mTitleText.setText(mSelectedProvince.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceid = ?", String.valueOf(mSelectedProvince.getId())).find(City.class);//sql语句查询
        if (mCityList.size() > 0)
        {
            mdataList.clear();
            for (City city : mCityList)
            {
                mdataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);//归位？
            mcurrentLevel = LEVEL_CITY;
        }
        else
        {
            int provinceCode = mSelectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china"+"/" + provinceCode;
            /**
             * 从服务器上获取数据
             */
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询市所有的县，优先从数据库查询，如果没有再到服务器上查询
     */
    private void queryCounties()
    {
        mTitleText.setText(mSelectedCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityid = ?", String.valueOf(mSelectedCity.getId())).find(County.class);//sql语句查询
        if (mCountyList.size() > 0)
        {
            mdataList.clear();
            for (County county : mCountyList)
            {
                mdataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);//归位？
            mcurrentLevel = LEVEL_COUNTY;
        }
        else
        {
            int provinceCode = mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String address = "http://guolin.tech/api/china" +"/"+ provinceCode + "/" + cityCode;
            /**
             * 从服务器上获取数据
             */
            queryFromServer(address,"county");
        }
    }

    /**
     * 从服务上查询数据
     * @param address uri的地址
     * @param type 查询的类型，如province、city、county
     */
    private void queryFromServer(String address, final String type)
    {
        /**
         * 显示进度对话框
         */
        HttpUtil.sendOkHttpRequest(address, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        /**
                         * 关闭进度条
                         */
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                String responseText = response.body().string();
                //Log.i(TAG,responseText);
                boolean result = false;
                if ("province".equals(type))
                {
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if ("city".equals(type))
                {
                    result = Utility.handleCityResponse(responseText, mSelectedProvince.getId());
                }
                else if ("county".equals(type))
                {
                    result = Utility.handleCountyResponse(responseText, mSelectedCity.getId());
                }

                if (result)
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            closeProgressDialog();
                            if ("province".equals(type))
                            {
                                queryProvinces();
                            }
                            else if ("city".equals(type))
                            {
                                queryCities();
                            }
                            else if ("county".equals(type))
                            {
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }

    /**
     *显示进度对话框
     */
    private void showProgressDialog()
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    /**
     * 关闭对话框
     */
    private void closeProgressDialog()
    {
        if(mProgressDialog!=null)
        {
            mProgressDialog.dismiss();
        }
    }





}
