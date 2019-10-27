package adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.voiceanimationinteraction.R;

import java.util.ArrayList;

import actionMain.TelAction;
import entity.SimpleContact;
/**
 * Created by Administrator on 2015/6/3.
 */
public class QueryContactsAdapter extends BaseAdapter
{

    Context context;
    ArrayList<SimpleContact> contacts;
    LayoutInflater layoutInflator;
    public QueryContactsAdapter(Context context,
                                ArrayList<SimpleContact> contacts)
    {
        this.context = context;
        this.contacts = contacts;
        layoutInflator = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
    }
    @Override public int getCount()
    {
        return contacts.size();
    }
    @Override public Object getItem(int position)
    {
        return contacts.get(position);
    }
    @Override public long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView =
                layoutInflator.inflate(R.layout.query_contact_layout, null);
        TextView queryContactsOrder = (TextView) convertView.findViewById(R
                .id.query_contancts_order);
        TextView queryContactsName = (TextView) convertView.findViewById(R
                .id.query_contacts_name);
        TextView queryContactsPhone = (TextView) convertView.findViewById(R
                .id.query_contacts_phonenumber);
        ImageView dial = (ImageView) convertView.findViewById(R.id
                .query_contacts_dial);
        final SimpleContact contact = (SimpleContact) getItem(position);
        queryContactsOrder.setText((position + 1) + "");
        queryContactsName.setText(contact.getName());
        queryContactsPhone.setText(contact.getPhone());
        dial.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                TelAction.callPhone(contact.getPhone(), contact.getName());
            }
        });
        return convertView;
    }
}