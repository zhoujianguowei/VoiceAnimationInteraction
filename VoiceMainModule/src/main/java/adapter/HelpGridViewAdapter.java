package adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.voiceanimationinteraction.R;

import java.util.ArrayList;
import java.util.HashMap;

import utils.Constant;
/**
 * Created by Administrator on 2015/6/4.
 */
public class HelpGridViewAdapter extends BaseAdapter
{

    Context context;
    ArrayList<HashMap<String, Object>> mapList;
    public HelpGridViewAdapter(Context context, ArrayList<HashMap<String,
            Object>>
            mapList)
    {
        this.context = context;
        this.mapList = mapList;
    }
    @Override public int getCount()
    {
        return mapList.size();
    }
    @Override public Object getItem(int position)
    {
        return mapList.get(position);
    }
    @Override public long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = LayoutInflater.from(context).inflate(R.layout
                .help_layout_child_item, null);
        ImageView helpIcon =
                (ImageView) convertView.findViewById(R.id.help_icon);
        TextView helpLabel =
                (TextView) convertView.findViewById(R.id.help_label);
        TextView helpContent =
                (TextView) convertView.findViewById(R.id.help_example);
        HashMap<String, Object> map =
                (HashMap<String, Object>) getItem(position);
        helpIcon.setBackgroundResource((Integer) map.get("icon"));
        helpLabel.setText((String) map.get("label"));
        helpContent.setText((String) map.get("content"));
        RelativeLayout.LayoutParams helpIconParams =
                (RelativeLayout.LayoutParams) helpIcon.getLayoutParams();
        helpIconParams.width = (int) (Constant.screenWidth * 1.0f / 5);
        helpIconParams.height = helpIconParams.width;
        helpIcon.setLayoutParams(helpIconParams);
        return convertView;
    }
}
