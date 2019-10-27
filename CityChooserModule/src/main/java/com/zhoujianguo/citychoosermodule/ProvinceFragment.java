package com.klicen.citychoosermodule;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.TreeSet;
/**
 * Created on 15-4-1.
 */
public class ProvinceFragment extends Fragment
        implements Handler.Callback, AdapterView.OnItemClickListener
{

    public static final String TAG = ProvinceFragment.class.getName();
    private DrawerLayout drawerLayout;
    private PinnedSectionListView lvProvince;
    private ListView lvCity;
    private ProvinceAdatper adapter;
    private ArrayList<Province> provinces = new ArrayList<>();
    private ArrayAdapter<String> cityAdapter;
    private ArrayList<String> citys = new ArrayList<>();
    private String chosenProvince;
    private Handler handler;
    private Callback callback;
    private TreeSet<String> alphabet;
    public PinnedSectionListView getLvProvince()
    {
        return lvProvince;
    }
    /**
     * 将适配器的数据暴露出去
     */
    public ArrayList<Province> getDataSet()
    {
        return provinces;
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_province, container, false);
        drawerLayout = (DrawerLayout) view
                .findViewById(R.id.fragment_province_dl_drawerlayout);
        lvProvince =
                (PinnedSectionListView) view
                        .findViewById(R.id.fragment_province_pclv_province);
        lvCity = (ListView) view.findViewById(R.id.fragment_province_lv_citys);
        lvProvince.setOnItemClickListener(this);
        lvCity.setOnItemClickListener(this);
        adapter = new ProvinceAdatper(getActivity(), provinces);
        lvProvince.setAdapter(adapter);
        /**
         * 为ListView添加滑动监听器
         */
        lvProvince.setOnScrollListener((CityChooseActivity) getActivity());
        cityAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, citys);
        lvCity.setAdapter(cityAdapter);
        handler = new Handler(this);
        new Thread(new ProvinceReadRunnable()).start();
        return view;
    }
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            callback = (Callback) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(
                    activity.toString() + " must implement " + TAG + " Callback");
        }
    }
    @Override
    public boolean handleMessage(Message msg)
    {
        adapter.notifyDataSetChanged();
        //为AutoCompleteTextView设置适配器，必须实现Filterable接口
        //        ((CityChooseActivity) getActivity()).searchAuto.setAdapter(adapter);
        //如果Activity实现了ProvinceDataLoadCompletedlistener接口，更新字母表信息
        if (getActivity() instanceof ProvinceDataLoadCompletedListener)
        {
            ((ProvinceDataLoadCompletedListener) getActivity())
                    .onProvinceDateCompleted(alphabet);
        }
        return true;
    }
    /**
     * 更新Provinces数据
     */
    private void updateProvinceData()
    {
        provinces = adapter.getUpdateProvinceData();
    }
    @Override
    public void onItemClick(
            AdapterView<?> parent, View view, int position, long id)
    {
        updateProvinceData();
        if (parent.getId() == R.id.fragment_province_pclv_province)
        {
            Province province = provinces.get(position);
            String pName = province.getProvince();
            if (pName == null)
            {
                return;
            }
            if (pName.contains("北京") || pName.contains("重庆") ||
                    pName.contains("上海") ||
                    pName.contains("天津"))
            {
                callback.onCityChosen(province.getProvince(), pName);
                return;
            }
            if (province.getType() == Province.TYPE_ITEM)
            {
                citys.clear();
                citys.addAll(province.getCitys());
                cityAdapter.notifyDataSetChanged();
                drawerLayout.openDrawer(lvCity);
                chosenProvince = provinces.get(position).getProvince();
            }
        }
        else if (parent.getId() == R.id.fragment_province_lv_citys)
        {
            callback.onCityChosen(chosenProvince, citys.get(position));
        }
    }
    public interface Callback
    {

        public void onCityChosen(String province, String city);
    }

    private class ProvinceReadRunnable implements Runnable
    {

        @Override
        public void run()
        {
            try
            {
                long start = System.currentTimeMillis();
                provinces.addAll(CSVReader.getInstance(getActivity())
                        .readProvinceAndCityCSV());
                if (alphabet == null)
                    alphabet = new TreeSet<String>();
                for (Province province : provinces)
                {
                    String tag = province.getTag();
                    if (tag != null && !alphabet.contains(tag))
                        alphabet.add(tag);
                }
                Log.e("initResult", provinces.toString());
                handler.sendEmptyMessage(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据加载完毕之后回调
     */
    public interface ProvinceDataLoadCompletedListener
    {

        public void onProvinceDateCompleted(TreeSet<String> alphabet);
    }
}
