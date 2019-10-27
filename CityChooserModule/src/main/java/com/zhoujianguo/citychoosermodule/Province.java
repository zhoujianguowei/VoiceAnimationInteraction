package com.klicen.citychoosermodule;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
/**
 * Created on 15-4-1.
 */
public class Province implements Parcelable
{

    /**
     * 字母标签
     */
    public static final int TYPE_SECTION = 1;
    /**
     * 省或者直辖市的标签
     */
    public static final int TYPE_ITEM = 0;
    private String province;
    private ArrayList<String> citys = new ArrayList<>();
    /**
     * 省的首字母
     */
    private String tag;
    private int type = -1;
    public static Creator<Province> CREATOR = new Creator<Province>()
    {
        @Override
        public Province createFromParcel(Parcel source)
        {
            Province province = new Province();
            province.type = source.readInt();
            province.tag = source.readString();
            province.province = source.readString();
            province.citys = source.createStringArrayList();
            return province;
        }
        @Override
        public Province[] newArray(int size)
        {
            return new Province[size];
        }
    };
    public String getTag()
    {
        return tag;
    }
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }
    public ArrayList<String> getCitys()
    {
        return citys;
    }
    public void addCity(String city)
    {
        this.citys.add(city);
    }
    public String getProvince()
    {
        return province;
    }
    public void setProvince(String province)
    {
        this.province = province;
    }
    @Override
    public String toString()
    {
        return "Province{" +
                "province='" + province + '\'' +
                ", citys=" + citys +
                ", tag='" + tag + '\'' +
                ", type=" + type +
                '}';
    }
    @Override
    public int describeContents()
    {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.type);
        dest.writeString(this.tag);
        dest.writeString(this.province);
        dest.writeStringList(this.citys);
    }
}
