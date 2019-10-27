package com.example.voiceanimationinteraction;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import entity.Contact;
import entity.SimpleContact;
import utils.PinyinUtils;
public class ContactList
{

    final static String CONTACTS = "content://com.android.contacts/contacts";
    static Uri uri = Uri.parse(CONTACTS);
    StringBuilder builder = new StringBuilder();
    private static ArrayList<Contact> contactsList = new ArrayList<Contact>();
    public static ArrayList<Contact> getContactsList(Context context, String
            filterName)
    {
        ArrayList<Contact> allContacts = getContactsList(context);
        ArrayList<Contact> resContacts = new ArrayList<>();
        for (Contact contact : allContacts)
            if (PinyinUtils.converterToSpell(contact.getName()).equals
                    (PinyinUtils.converterToSpell(filterName)))
            {
                resContacts.add(contact);
            }
        return resContacts;
    }
    public static ArrayList<SimpleContact> getSimpleContactList(Context
                                                                        context,
                                                                String filterName)
    {
        ArrayList<SimpleContact> simpleContacts = new ArrayList<>();
        ArrayList<Contact> contacts = getContactsList(context, filterName);
        for (Contact contact : contacts)
        {
            HashSet<String> phoneNumbers = contact.getPhones();
            Iterator<String> phoneNumberIterator = phoneNumbers.iterator();
            while (phoneNumberIterator.hasNext())
            {
                String phone = phoneNumberIterator.next();
                SimpleContact simpleContact = new SimpleContact();
                simpleContact.setName(contact.getName());
                simpleContact.setPhone(phone);
                simpleContacts.add(simpleContact);
            }
        }
        return simpleContacts;
    }
    /**
     * 获取手机所有联系人信息
     */
    public static ArrayList<Contact> getContactsList(Context context)
    {
        if (contactsList.size() > 0)
        {
            return contactsList;
        }
        String phoneNumber;
        String id;
        String name;
        // 默认是升序排序（ASC）,降序是(DESC)
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " asc");
        Cursor columnNameCursor = resolver.query(uri, null, null, null, null);
        System.out.println(columnNameCursor.getColumnNames());
        while (cursor.moveToNext())
        {
            phoneNumber = "";
            id = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int phoneNumberCount = cursor
                    .getInt(cursor
                            .getColumnIndex(
                                    ContactsContract.Contacts.HAS_PHONE_NUMBER));
            Contact contacts = new Contact();
            HashSet<String> phoneSet = new HashSet<String>();
            while (phoneNumberCount > 0)
            {
                Cursor phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.Data.CONTACT_ID + "=" + id,
                        null, null);
                while (phoneCursor.moveToNext())
                {
                    phoneNumber = phoneCursor
                            .getString(phoneCursor
                                    .getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phoneSet.add(phoneNumber);
                contacts.setName(name);
                contacts.setPhones(phoneSet);
                contactsList.add(contacts);
                phoneNumberCount--;
            }
        }
        return contactsList;
    }
}
