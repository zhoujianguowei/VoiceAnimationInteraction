package com.klicen.citychoosermodule;
/**
 * Created by Administrator on 2015/4/17.
 */
public class City
{

    private String province;
    private String city;
    private String tag;
    public String getCity()
    {
        return city;
    }
    public void setCity(String city)
    {
        this.city = city;
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
        return "province:" + province + "  city:" + city + " tag:" + tag;
    }
    public String getTag()
    {
        return tag;
    }
    public void setTag(String tag)
    {
        this.tag = tag;
    }
}
