package com.wnezros.locatorviasms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PhonesAdapter extends ArrayAdapter<String> {
    public PhonesAdapter(Context context, boolean isDark) {
        super(context, isDark ? R.layout.dark_phone_item : R.layout.light_phone_item, android.R.id.text1);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text2 = (TextView)view.findViewById(android.R.id.text2);
        text2.setText(getContactDisplayNameByNumber(getItem(position)));

        return view;
    }

    private String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = number;

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }
}
