package com.klicen.citychoosermodule;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.TreeSet;
/**
 * Created on 15-4-1.
 */
public class CityChooseActivity extends AppCompatActivity
implements ProvinceFragment.Callback,
        AlphabetFragment.AlphabetItemOnClickListener,
        ProvinceFragment.ProvinceDataLoadCompletedListener,
        AbsListView.OnScrollListener
{

    public static final String EXTRA_PROVINCE = "province";
    public static final String EXTRA_CITY = "city";
    AutoCompleteTextView searchAuto;
    android.support.v4.app.FragmentManager fragmentManager;
    FragmentTransaction transaction;
    ProvinceFragment provinceFragment;
    CityFragment cityFragment;
    AlphabetFragment alphabetFragment;
    private ImageView backIcon;
    private RelativeLayout searchIconRlContainer;
    /**
     * 编辑状态时的搜索图标
     */
    private ImageView editSearchIcon;
    /**
     * 当前ProvinceFragment是否可见
     */
    private volatile boolean isProvinceFragmentShow;
    /**
     * 保存上次选择的城市信息
     */
    public static final String CITY_CHOOSEN_INFO = "city_choosen_info";
    private static final String PREVIOUS_CHOOSEN_PROVINCE = "province";
    private static final String PREVIOUS_CHOOSEN_CITY = "city";
    /**
     * 保存上次选择的省
     */
    public static String getPrevousChoosenProvince(Context context)
    {
        return PreviousChoosenCity.getSingleInstance(context).getProvince();
    }
    /**
     * 保存上次选择的市
     */
    public static String getPreviousChoosenCity(Context context)
    {
        return PreviousChoosenCity.getSingleInstance(context).getCity();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_choose);
        ScreenSize.initial(this);
        backIcon = (ImageView) findViewById(R.id.activity_city_choose_iv_back);
        editSearchIcon = (ImageView) findViewById(R.id.edit_search_icon);
        searchIconRlContainer =
                (RelativeLayout) findViewById(R.id.search_rl_container);
        searchAuto = (AutoCompleteTextView) findViewById(R.id.search_auto);
        backIcon.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                CityChooseActivity.this.finish();
                return false;
            }
        });
        //使弹出框不可见
        searchAuto.setDropDownHeight(0);
        searchAuto.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count)
            {
            }
            @Override
            public void afterTextChanged(Editable s)
            {
                        /*当搜索框中出现字符时候，
                           同时CityFragment显示，ProvinceFragment隐藏
                         */
                if (s.length() > 0)
                {
                    transaction = fragmentManager.beginTransaction();
                    transaction.hide(provinceFragment);
                    transaction.hide(alphabetFragment);
                    transaction.show(cityFragment);
                    transaction.commit();
                    setProvinceFragmentShow(false);
                }
                else if (s.length() == 0)
                {
                    transaction = fragmentManager.beginTransaction();
                    transaction.hide(cityFragment);
                    transaction.show(alphabetFragment);
                    transaction.show(provinceFragment);
                    transaction.commit();
                    setProvinceFragmentShow(true);
                }
            }
        });
        searchAuto.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (editSearchIcon.getVisibility() != View.VISIBLE &&
                        event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    searchIconRlContainer.setVisibility(View.GONE);
                    /**
                     * view.setFocusable and view.setFocusableInTouchMode have to
                     * be set in the same time
                     * setFocusable() is for keyboard phone
                     */
                    searchAuto.setFocusable(true);
                    searchAuto.setFocusableInTouchMode(true);
                    searchAuto.requestFocus();
                    editSearchIcon.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        provinceFragment = new ProvinceFragment();
        cityFragment = new CityFragment();
        alphabetFragment = new AlphabetFragment();
        alphabetFragment.setAlphabteItemOnClickListener(this);
        FrameLayout activityChooserFl =
                (FrameLayout) findViewById(
                        R.id.activity_city_choose_fl_container);
        activityChooserFl.setLayoutParams(new LinearLayout.LayoutParams((int) (
                ScreenSize.getScreenWidth() * 15.0f / 16),
                LinearLayout.LayoutParams.MATCH_PARENT));
        FrameLayout activityAlphaFl =
                (FrameLayout) findViewById(R.id.activity_alphabet_fl_container);
        activityAlphaFl.setLayoutParams(new LinearLayout.LayoutParams((int) (
                ScreenSize.getScreenWidth() * 1.0 / 16),
                LinearLayout.LayoutParams.MATCH_PARENT));
        transaction
                .add(R.id.activity_city_choose_fl_container, provinceFragment);
        transaction.add(R.id.activity_city_choose_fl_container, cityFragment);
        transaction.add(R.id.activity_alphabet_fl_container, alphabetFragment);
        /**
         * 首次运行显示省份列表
         */
        transaction.hide(cityFragment);
        transaction.show(provinceFragment);
        transaction.commit();
        setProvinceFragmentShow(true);
    }
    public boolean isProvinceFragmentShow()
    {
        return isProvinceFragmentShow;
    }
    public void setProvinceFragmentShow(boolean isProvinceFragmentShow)
    {
        this.isProvinceFragmentShow = isProvinceFragmentShow;
    }
    @Override
    public void onCityChosen(String province, String city)
    {
        SharedPreferences preferences = getSharedPreferences
                (CITY_CHOOSEN_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREVIOUS_CHOOSEN_PROVINCE, province);
        editor.putString(PREVIOUS_CHOOSEN_CITY, city);
        editor.commit();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CITY, city);
        intent.putExtra(EXTRA_PROVINCE, province);
        setResult(RESULT_OK, intent);
        finish();
    }
    /**
     * 点击字母时候的情况
     */
    @Override
    public void onAlphabetItemClick(View view, int type)
    {
        /**
         * 当前显示的Fragment是ProvinceFragment
         */
        if (type == AlphabetFragment.AlphabetItemOnClickListener.PROVINCE_FLAG &&
                view instanceof TextView)
        {
            TextView tv = (TextView) view;
            String tag = tv.getText().toString();
            PinnedSectionListView pinnedSetionLv =
                    provinceFragment.getLvProvince();
            ArrayList<Province> provinceList =
                    ((ProvinceAdatper) pinnedSetionLv.getAdapter())
                            .getUpdateProvinceData();
            int position = 0;
            for (Province province : provinceList)
            {
                if (province.getTag().equals(tag))
                {
                    break;
                }
                position++;
            }
            //没有指定的tag
            if (position == provinceList.size())
            {
                return;
            }
            pinnedSetionLv.setSelection(position);
            alphabetFragment.setSelectedItem(tag);
        }
    }
    @Override public void onProvinceDateCompleted(TreeSet<String> alphabet)
    {
        alphabetFragment.updateView(alphabet);
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
    }
    @Override public void onScroll(AbsListView view, int firstVisibleItem,
                                   int visibleItemCount, int totalItemCount)
    {
//        Province firstVisibleProvince =
//                (Province) provinceFragment.getLvProvince()
//                        .getAdapter().getItem
//                                (firstVisibleItem);
        ArrayList<Province> provinces = ((ProvinceAdatper) provinceFragment
                .getLvProvince()
                .getAdapter()).getUpdateProvinceData();
        if (provinces != null && provinces.size() >= firstVisibleItem + 1)
        {
            Province firstVisibleProvince = provinces.get(firstVisibleItem);
            alphabetFragment
                    .setSelectedItem(firstVisibleProvince.getTag());
        }
    }
    private static class PreviousChoosenCity
    {

        private static Context context;
        private static PreviousChoosenCity singleInstance;
        static PreviousChoosenCity getSingleInstance(Context context)
        {
            if (singleInstance == null)
            {
                singleInstance = new PreviousChoosenCity();
            }
            PreviousChoosenCity.context = context;
            return singleInstance;
        }
        public String getCity()
        {
            SharedPreferences preferences = context.getSharedPreferences
                    (CITY_CHOOSEN_INFO, MODE_PRIVATE);
            return preferences.getString(PREVIOUS_CHOOSEN_CITY, null);
        }
        public String getProvince()
        {
            SharedPreferences preferences = context.getSharedPreferences
                    (CITY_CHOOSEN_INFO, MODE_PRIVATE);
            return preferences.getString(PREVIOUS_CHOOSEN_PROVINCE, null);
        }
    }
}
