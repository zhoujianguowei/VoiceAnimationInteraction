package entity;
import android.os.Parcel;
import android.os.Parcelable;
public class OilPrice implements Parcelable
{

    private String province;
    private String oilType;
    private float oilPrice;
    public OilPrice()
    {}
    public OilPrice(String province)
    {
        this.province = province;
    }
    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    public String getProvince()
    {
        return province;
    }
    public void setProvince(String province)
    {
        this.province = province;
    }
    public String getOilType()
    {
        return oilType;
    }
    public void setOilType(String oilType)
    {
        this.oilType = oilType;
    }
    public float getOilPrice()
    {
        return oilPrice;
    }
    public void setOilPrice(float oilPrice)
    {
        this.oilPrice = oilPrice;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        // TODO Auto-generated method stub
        dest.writeStringArray(new String[]{province, oilType});
        dest.writeFloat(oilPrice);
    }
    public static final Creator<OilPrice> CREATOR = new Creator<OilPrice>()
    {
        @Override
        public OilPrice createFromParcel(Parcel source)
        {
            OilPrice oilPrice = new OilPrice();
            String[] strs = source.createStringArray();
            oilPrice.province = strs[0];
            oilPrice.oilType = strs[1];
            oilPrice.oilPrice = source.readFloat();
            // TODO Auto-generated method stub
            return oilPrice;
        }
        @Override
        public OilPrice[] newArray(int size)
        {
            // TODO Auto-generated method stub
            return new OilPrice[size];
        }
    };
}
