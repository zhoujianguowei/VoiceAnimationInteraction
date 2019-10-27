package com.klicen.citychoosermodule;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
/**
 * Created on 15-3-26. 用来解析assets目录下的文件
 */
public class CSVReader
{

    private static CSVReader instance;
    /**
     * 从asset文件中读取的数据
     */
    private static ArrayList<Province> assetProvince;
    private Context context;
    private CSVReader()
    {
    }
    public static CSVReader getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new CSVReader();
            instance.context = context;
        }
        return instance;
    }
    public ArrayList<HashMap<String, String>> readCSV(String file_name)
            throws IOException
    {
        InputStream is = context.getAssets().open(file_name);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        String cvsSplitBy = ",";
        br.readLine();
        ArrayList<HashMap<String, String>> CSVData = new ArrayList<>();
        while ((line = br.readLine()) != null)
        {
            String[] row = line.split(cvsSplitBy);
            HashMap<String, String> hm = new HashMap<>();
            for (int i = 0; i < row.length; i++)
            {
                hm.put("row[" + i + "]", row[i]);
            }
            CSVData.add(hm);
        }
        return CSVData;
    }
    public ArrayList<HashMap<String, String>> readCSV(
            String file_name, String charset) throws IOException
    {
        InputStream is = context.getAssets().open(file_name);
        InputStreamReader isr = new InputStreamReader(is, charset);
        BufferedReader br = new BufferedReader(isr);
        String line;
        String cvsSplitBy = ",";
        br.readLine();          //为什么要事先readLine()呢
        ArrayList<HashMap<String, String>> CSVData = new ArrayList<>();
        while ((line = br.readLine()) != null)
        {
            String[] row = line.split(cvsSplitBy);
            HashMap<String, String> hm = new HashMap<>();
            for (int i = 0; i < row.length; i++)
            {
                hm.put("row[" + i + "]", row[i]);
            }
            CSVData.add(hm);
        }
        return CSVData;
    }
    /**
     * 读取assets文件夹中的文件，并解析配置Province
     * 同时添加字母Province,为了避免重复读取，添加了provinces变量
     */
    public synchronized ArrayList<Province> readProvinceAndCityCSV()
            throws IOException
    {
        if (assetProvince != null)
        {
            return new ArrayList<Province>(assetProvince);
        }
        InputStream is = context.getAssets().open("province.csv");
        InputStreamReader isr = new InputStreamReader(is, "gbk");
        BufferedReader br = new BufferedReader(isr);
        String line;
        String cvsSplitBy = ",";
        Province province;
        HashMap<String, Province> provinces = new HashMap<>();
        CharacterParser cp = CharacterParser.getInstance();
        Locale locale = Locale.getDefault();
        while ((line = br.readLine()) != null)
        {
            province = new Province();
            String[] row = line.split(cvsSplitBy);
            province.setProvince(row[1]);
            province.setType(Province.TYPE_ITEM);
            //多音字处理
            if (row[1].contains("重庆"))
            {
                province.setTag("C");
            }
            else
            {
                province.setTag(
                        cp.getSelling(row[1]).substring(0, 1).toUpperCase(locale));
            }
            provinces.put(row[0], province);
        }
        br.close();
        isr.close();
        is.close();
        is = context.getAssets().open("city.csv");
        isr = new InputStreamReader(is, "gbk");
        br = new BufferedReader(isr);
        while ((line = br.readLine()) != null)
        {
            String[] row = line.split(cvsSplitBy);
            if (provinces.get(row[2]) != null)
            {
                provinces.get(row[2]).addCity(row[1]);
            }
        }
        HashSet<String> letters = new HashSet<>();
        ArrayList<Province> tmp = new ArrayList<>();
        tmp.addAll(provinces.values());
        Collections.sort(tmp, new Comparator<Province>()
        {
            @Override
            public int compare(Province lhs, Province rhs)
            {
                return lhs.getTag().compareTo(rhs.getTag());
            }
        });
        ArrayList<Province> result = new ArrayList<>();
        for (Province p : tmp)
        {
            if (letters.add(p.getTag()))
            {
                Province section = new Province();
                section.setTag(p.getTag());
                section.setType(Province.TYPE_SECTION);
                result.add(section);
            }
            result.add(p);
        }
        br.close();
        isr.close();
        is.close();
        assetProvince = result;
        return result;
    }
}
