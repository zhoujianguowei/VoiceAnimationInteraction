package com.klicen.citychoosermodule;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klicen.citychoosermodule.ProvinceFragment.Callback;

import java.util.ArrayList;
/**
 * Created by Administrator on 2015/4/17.
 */
public class CityAdapter extends BaseAdapter implements Filterable
{

    ArrayList<City> cities = new ArrayList<City>();
    Context context;
    CityFilter cityFilter;
    Callback callback;
    /**
     * 原始全部数据
     */
    ArrayList<City> mOriginalCityData;
    public CityAdapter(Context context, ArrayList<City> cities)
    {
        this.context = context;
        this.cities = cities;
        mOriginalCityData = new ArrayList<City>(cities);
        callback = (CityChooseActivity) context;
    }
    @Override
    public View getView(
            int position, View convertView, ViewGroup parent)
    {
        CityViewHolder cityViewHolder = null;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.fragment_city_item, null);
            cityViewHolder = new CityViewHolder();
            cityViewHolder.cityItemNameTv =
                    (TextView) convertView.findViewById(R.id.city_item_name_tv);
            cityViewHolder.cityItemChooserIcon =
                    (ImageView) convertView
                            .findViewById(R.id.city_item_choose_icon);
            convertView.setTag(cityViewHolder);
        }
        else
        {
            cityViewHolder = (CityViewHolder) convertView.getTag();
        }
        City city = (City) getItem(position);
        cityViewHolder.cityItemNameTv.setText(city.getCity());
        cityViewHolder.cityItemChooserIcon.setVisibility(View.INVISIBLE);
        convertView.setOnClickListener(new CityOnClicKListener(city));
        return convertView;
    }
    @Override
    public int getCount()
    {
        return cities.size();
    }
    @Override
    public long getItemId(int position)
    {
        return position;
    }
    @Override
    public Object getItem(int position)
    {
        return cities.get(position);
    }
    @Override public Filter getFilter()
    {
        if (cityFilter == null)
        {
            cityFilter = new CityFilter();
        }
        return cityFilter;
    }
    private class CityFilter extends Filter
    {

        @Override protected FilterResults performFiltering(
                CharSequence constraint)
        {
            FilterResults filterResults = new FilterResults();
            synchronized (CityFilter.class)
            {
                if (TextUtils.isEmpty(constraint))
                {
                    filterResults.values = mOriginalCityData;
                    filterResults.count = mOriginalCityData.size();
                }
                else
                {
                    ArrayList<City> tempValues = new ArrayList<City>();
                    for (City city : mOriginalCityData)
                        if (city.getCity().startsWith(constraint.toString()))
                        {
                            tempValues.add(city);
                        }
                    filterResults.values = tempValues;
                    filterResults.count = tempValues.size();
                }
            }
            return filterResults;
        }
        @Override protected void publishResults(CharSequence constraint,
                                                FilterResults results)
        {
            cities = (ArrayList<City>) results.values;
            if (results.count > 0)
            {
                notifyDataSetChanged();
            }
            else
            {
                notifyDataSetInvalidated();
            }
        }
    }

    private class CityViewHolder
    {

        TextView cityItemNameTv;
        ImageView cityItemChooserIcon;
        public TextView getCityItemNameTv()
        {
            return cityItemNameTv;
        }
        public void setCityItemNameTv(TextView cityItemNameTv)
        {
            this.cityItemNameTv = cityItemNameTv;
        }
        public ImageView getCityItemChooserIcon()
        {
            return cityItemChooserIcon;
        }
        public void setCityItemChooserIcon(ImageView cityItemChooserIcon)
        {
            this.cityItemChooserIcon = cityItemChooserIcon;
        }
    }

    private class CityOnClicKListener implements View.OnClickListener
    {

        City city;
        public CityOnClicKListener(City city)
        {
            this.city = city;
        }
        @Override
        public void onClick(View v)
        {
            if (v instanceof RelativeLayout)
            {
                RelativeLayout cityItemRlContainer = (RelativeLayout) v;
                int childCount = cityItemRlContainer.getChildCount();
                for (int i = 0; i < childCount; i++)
                {
                    View childView = cityItemRlContainer.getChildAt(i);
                    if (childView.getId() == R.id.city_item_choose_icon)
                    {
                        childView.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(context, "你选择了" + city.getCity(),
                            Toast.LENGTH_SHORT).show();
                    callback.onCityChosen(city.getProvince(), city.getCity());
                }
            }
        }
    }
}
