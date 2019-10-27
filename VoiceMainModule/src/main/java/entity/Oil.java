package entity;
import android.os.Parcel;
import android.os.Parcelable;
public class Oil implements Parcelable
{

    private String city;
    private String oilType;
    private float oilPrice;
    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public String toString()
    {
        return "OilPrice [city=" + city + ", oilType=" + oilType + ", oilPrice="
                + oilPrice + "]";
    }
    public String getCity()
    {
        return city;
    }
    public void setCity(String city)
    {
        this.city = city;
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
        dest.writeStringArray(new String[]{city, oilType});
        dest.writeFloat(oilPrice);
    }
    public static final Creator<Oil> CREATOR = new Creator<Oil>()
    {
        @Override
        public Oil createFromParcel(Parcel source)
        {
            Oil oilPrice = new Oil();
            String[] strs = source.createStringArray();
            oilPrice.city = strs[0];
            oilPrice.oilType = strs[1];
            oilPrice.oilPrice = source.readFloat();
            // TODO Auto-generated method stub
            return oilPrice;
        }
        @Override
        public Oil[] newArray(int size)
        {
            // TODO Auto-generated method stub
            return new Oil[size];
        }
    };
}
