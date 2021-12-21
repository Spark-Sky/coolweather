package com.tdd.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tdd.coolweather.db.City;
import com.tdd.coolweather.db.County;
import com.tdd.coolweather.db.Province;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaFragment extends Fragment
{
    public static final int LEVEL_PROVINCE =0;
    public static final int LEVEL_CITY =1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog mProgressDialog;
    private TextView mTitleText;
    private Button mBackButton;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();
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
        View view = inflater.inflate(R.layout.choose_area,container,false);
        mTitleText = (TextView) view.findViewById(R.id.title_text);
        mBackButton = (Button) view.findViewById(R.id.back_button);
        mListView = (ListView) view.findViewById(R.id.list_item);
        mAdapter = new ArrayAdapter<>()
    }
}
