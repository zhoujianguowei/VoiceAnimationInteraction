package entity;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by Administrator on 2015/6/3.
 */
public class Weather implements Parcelable
{

    private String city;
    private String current_temp;
    private String temperature;
    private String wind;
    private String weather;
    public String getTemperature()
    {
        return temperature;
    }
    @Override public String toString()
    {
        return "Weather{" +
                "city='" + city + '\'' +
                ", current_temp='" + current_temp + '\'' +
                ", temperature='" + temperature + '\'' +
                ", wind='" + wind + '\'' +
                ", weather='" + weather + '\'' +
                '}';
    }
    public void setTemperature(String temperature)
    {
        this.temperature = temperature;
    }
    public String getCurrent_temp()
    {
        return current_temp;
    }
    public void setCurrent_temp(String current_temp)
    {
        this.current_temp = current_temp;
    }
    public String getWind()
    {
        return wind;
    }
    public void setWind(String wind)
    {
        this.wind = wind;
    }
    public String getWeather()
    {
        return weather;
    }
    public void setWeather(String weather)
    {
        this.weather = weather;
    }
    public String getCity()
    {
        return city;
    }
    public void setCity(String city)
    {
        this.city = city;
    }
    @Override public int describeContents()
    {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeStringArray(
                new String[]{city, current_temp, temperature, wind,
                        weather});
    }
    public static final Creator<Weather> CREATOR = new Creator<Weather>()
    {
        @Override public Weather createFromParcel(Parcel source)
        {
            Weather weather = new Weather();
            String[] weatherArray = source.createStringArray();
            weather.city = weatherArray[0];
            weather.current_temp = weatherArray[1];
            weather.temperature = weatherArray[2];
            weather.wind = weatherArray[3];
            weather.weather = weatherArray[4];
            return weather;
        }
        @Override public Weather[] newArray(int size)
        {
            return new Weather[size];
        }
    };
}
