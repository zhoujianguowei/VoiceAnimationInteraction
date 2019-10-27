package com.klicen.citychoosermodule;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
/**
 * Created on 15-4-1.
 */
public class ProvinceAdatper extends BaseAdapter
        implements PinnedSectionListView.PinnedSectionListAdapter, Filterable
{

    //线程同步控制
    private static Object lock = new Object();
    private Context context;
    private ArrayList<Province> provinces;
    //省或者直辖市的过滤器
    private ProvinceFilter filter;
    //存储初始化时候的所有所有数据结果
    private ArrayList<Province> originalValues;
    public ProvinceAdatper(Context context, ArrayList<Province> provinces)
    {
        this.context = context;
        this.provinces = provinces;
    }
    /**
     * 利用AutoCompleteTextView筛选后的数据
     */
    public ArrayList<Province> getUpdateProvinceData()
    {
        return provinces;
    }
    @Override
    public int getCount()
    {
        return provinces.size();
    }
    @Override
    public Province getItem(int position)
    {
        return provinces.get(position);
    }
    @Override
    public long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.fragment_province_item, parent, false);
            holder = new ViewHolder();
            holder.tvProvince =
                    (TextView) convertView
                            .findViewById(R.id.fragment_province_tv_province);
            holder.tvChosen = (TextView) convertView
                    .findViewById(R.id.fragment_province_tv_chosen);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        Province province = getItem(position);
        if (province.getType() == Province.TYPE_ITEM)
        {
            holder.tvProvince.setText(province.getProvince());
        }
        else if (province.getType() == Province.TYPE_SECTION)
        {
            holder.tvChosen.setVisibility(View.INVISIBLE);
            holder.tvProvince.setText(province.getTag());
            convertView.setBackgroundColor(0xfff6f6f7);
        }
        return convertView;
    }
    @Override
    public int getViewTypeCount()
    {
        return 2;
    }
    @Override
    public int getItemViewType(int position)
    {
        return getItem(position).getType();
    }
    @Override
    public boolean isItemViewTypePinned(int viewType)
    {
        return viewType == Province.TYPE_SECTION;
    }
    @Override
    public ProvinceFilter getFilter()
    {
        if (filter == null)
        {
            filter = new ProvinceFilter();
        }
        return filter;
    }
    private class ViewHolder
    {

        private TextView tvProvince, tvChosen;
    }

    private class ProvinceFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(
                CharSequence constraint)
        {
            //从中过滤的结果
            ArrayList<Province> filterValues = null;
            FilterResults filterResults = new FilterResults();
            //初始化tempList内容
            if (originalValues == null)
            {
                synchronized (lock)
                {
                    originalValues = new ArrayList<Province>(provinces);
                }
            }
            synchronized (lock)
            {
                if (constraint == null || constraint.length() == 0)
                {
                    filterValues = new ArrayList<Province>(originalValues);
                }
                //开始正式筛选
                else
                {
                    filterValues = new ArrayList<Province>();
                    for (Province province : originalValues)
                    {
                        String provinceName = province.getProvince();
                        if (provinceName != null &&
                                provinceName.startsWith(constraint.toString()))
                        {
                            filterValues.add(province);
                        }
                    }
                }
                filterResults.values = filterValues;
                filterResults.count = filterValues.size();
            }
            return filterResults;
        }
        @Override
        protected void publishResults(
                CharSequence constraint, FilterResults results)
        {
            provinces = (ArrayList<Province>) results.values;
            Log.e("result", provinces.toString());
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
}
