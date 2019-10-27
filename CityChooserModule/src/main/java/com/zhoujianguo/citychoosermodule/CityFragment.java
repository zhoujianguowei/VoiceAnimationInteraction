package com.klicen.citychoosermodule;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
/**
 * Created by Administrator on 2015/4/17.
 */
public class CityFragment extends Fragment
        implements Handler.Callback
{

    private static final int LOAD_CITY_OVER = 1;
    ListView cityLv;
    ArrayList<City> cities = new ArrayList<City>();
    Handler loadCitiesInfoHandler;
    CityAdapter cityAdapter;
    ProvinceFragment.Callback callback;
    CharacterParser parser = CharacterParser.getInstance();
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        View cityContainer = inflater.inflate(R.layout.fragment_city, null);
        cityLv = (ListView) cityContainer.findViewById(R.id.fragment_city_lv);
        loadCitiesInfoHandler = new Handler(this);
        //加载城市
        new Thread()
        {
            @Override
            public void run()
            {
                if (cities == null)
                {
                    cities = new ArrayList<City>();
                }
                try
                {
                    ArrayList<Province> provinces =
                            CSVReader.getInstance(getActivity())
                                    .readProvinceAndCityCSV();
                    for (Province province : provinces)
                    {
                        String provinceName = province.getProvince();
                        //是字母标签
                        if (province.getType() == Province.TYPE_SECTION)
                        {
                            continue;
                        }
                        City city = null;
                        if (provinceName.contains("北京") ||
                                provinceName.contains("重庆") ||
                                provinceName.contains("上海") ||
                                provinceName.contains("天津"))
                        {
                            city = new City();
                            city.setProvince("直辖市");
                            city.setCity(provinceName);
                            city.setTag(parser.getSelling(provinceName)
                                    .substring(0, 1).toUpperCase());
                            cities.add(city);
                            continue;
                        }
                        ArrayList<String> specity = province.getCitys();
                        for (String cityName : specity)
                        {
                            city = new City();
                            city.setProvince(provinceName);
                            city.setCity(cityName);
                            if (cityName.contains("重庆"))
                            {
                                city.setTag("C");
                            }
                            else
                            {
                                city.setTag(
                                        parser.getSelling(cityName).substring
                                                (0, 1).toUpperCase());
                            }
                            cities.add(city);
                        }
                    }
                    /**
                     * 按照城市名称排序
                     */
                    Collections.sort(cities, new Comparator<City>()
                    {
                        @Override public int compare(City lhs, City rhs)
                        {
                            return lhs.getTag().compareToIgnoreCase(rhs
                                    .getTag());
                        }
                    });
                    loadCitiesInfoHandler.sendEmptyMessage(LOAD_CITY_OVER);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
        return cityContainer;
    }
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            callback = (ProvinceFragment.Callback) activity;
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public boolean handleMessage(Message msg)
    {
        cityAdapter = new CityAdapter(getActivity(), cities);
        cityLv.setAdapter(cityAdapter);
        cityAdapter.notifyDataSetChanged();
        CityChooseActivity activity = (CityChooseActivity) getActivity();
        activity.searchAuto.setAdapter(cityAdapter);
        return true;
    }
}
