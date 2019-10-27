package adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.voiceanimationinteraction.R;

import java.util.ArrayList;

import actionMain.SendSmsAction;
import entity.SimpleContact;
/**
 * Created by Administrator on 2015/6/6.
 */
public class SendSmsAdapter extends BaseAdapter
{

    ArrayList<SimpleContact> simpleContacts;
    Context context;
    SendSmsAction smsAction;
    public SendSmsAdapter(ArrayList<SimpleContact> simpleContacts,
                          Context context, SendSmsAction smsAction)
    {
        this.context = context;
        this.simpleContacts = simpleContacts;
        this.smsAction = smsAction;
    }
    @Override public int getCount()
    {
        return simpleContacts.size();
    }
    @Override public Object getItem(int position)
    {
        return simpleContacts.get(position);
    }
    @Override public long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
//        return null;
        convertView = LayoutInflater.from(context).inflate(R.layout
                .send_sms_item, null);
        final TextView name =
                (TextView) convertView.findViewById(R.id.sms_name);
        final TextView phone =
                (TextView) convertView.findViewById(R.id.sms_phone_number);
        convertView.findViewById(R.id.sms_send_icon).
                setOnClickListener(new
                                           View.OnClickListener()
                                           {
                                               @Override
                                               public void onClick(
                                                       View v)
                                               {
                                                   smsAction.sendMsg(phone
                                                                   .getText()
                                                                   .toString().trim(),
                                                           name
                                                                   .getText()
                                                                   .toString()
                                                                   .trim());
                                               }
                                           });
        SimpleContact simpleContact = (SimpleContact) getItem(position);
        name.setText(simpleContact.getName());
        phone.setText(simpleContact.getPhone());
        return convertView;
    }
}
