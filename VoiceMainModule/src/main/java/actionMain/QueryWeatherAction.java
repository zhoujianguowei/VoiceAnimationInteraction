package actionMain;
import android.text.TextUtils;

import com.example.voiceanimationinteraction.SpeechMainActivity;

import services.WeatherService;
import utils.Constant;
/**
 * Created by Administrator on 2015/6/4.
 */
public class QueryWeatherAction extends SupAction
{

    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        super.handleDictation(context, dictation);
        String currentCity = TextUtils.isEmpty(Constant.getCurrentCity())
                ? Constant.getCurrentProvince() : Constant.getCurrentCity();
        WeatherService.startQueryWeather(context, currentCity);
    }
}
