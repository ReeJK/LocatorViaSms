package com.wnezros.locatorviasms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class PhonesFragment extends ListWithPlusFragment implements ContactsUtils.OnNumberReceiveListener {
    private static final int CONTACT_REQUEST_CODE = 1;

    class PhonesAdapter extends ArrayAdapter<String> {
        public PhonesAdapter(Context context, boolean isDark) {
            super(context, isDark ? R.layout.dark_phone_item : R.layout.light_phone_item, android.R.id.text1);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView text2 = (TextView)view.findViewById(android.R.id.text2);
            text2.setText(ContactsUtils.getContactDisplayNameByNumber(getContext(), getItem(position)));

            return view;
        }
    }

    private ContactsUtils.DialogBuilder _dialog;

    @SuppressWarnings("unused")
    public static PhonesFragment newInstance() {
        return new PhonesFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_phones_list;
    }

    @Override
    protected ArrayAdapter<String> createAdapter() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isDark = Settings.getIsPhonesBlacklist(prefs);
        return new PhonesAdapter(getContext(), isDark);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ContactsUtils.checkGrantPanel(view, this);

        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String[] phones = Settings.getPhones(prefs);
        _adapter.addAll(phones);

        view.findViewById(R.id.change_to_white).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Settings.setIsPhonesBlacklist(prefs, false);
                setTheme(getView(), false);
            }
        });

        view.findViewById(R.id.change_to_black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Settings.setIsPhonesBlacklist(prefs, true);
                setTheme(getView(), true);
            }
        });

        setTheme(view, Settings.getIsPhonesBlacklist(prefs));
        return view;
    }

    private void setTheme(View rootView, boolean isDark) {
        setDescription(isDark ? R.string.phones_description_black : R.string.phones_description_white);

        String[] oldItems = _adapter.toArray(new String[0]);
        _adapter = createAdapter();
        _adapter.addAll(oldItems);
        _listView.setAdapter(_adapter);

        rootView.setBackgroundResource(isDark ? R.color.colorDark : R.color.colorLight);
        _listView.setDivider(new ColorDrawable(getContext().getResources().getColor(isDark ? R.color.dividerDark : R.color.dividerLight)));
        _listView.setDividerHeight(1);

        rootView.findViewById(R.id.change_to_white).setVisibility(!isDark ? View.GONE : View.VISIBLE);
        rootView.findViewById(R.id.change_to_black).setVisibility(isDark ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onReceiveNumber(String number) {
        _adapter.add(number);
        save();
    }

    protected void save() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Settings.setPhones(prefs, _adapter.toArray(new String[0]));
    }

    @Override
    protected void onPlusClick() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isDark = Settings.getIsPhonesBlacklist(prefs);

        _dialog = new ContactsUtils.DialogBuilder(this);
        _dialog.setMessage(isDark ? R.string.phone_dialog_message_black : R.string.phone_dialog_message_white);
        _dialog.setPhoneNumberRequestCode(CONTACT_REQUEST_CODE);
        _dialog.setResultListener(this);
        _dialog.setOnCloseListener(new ContactsUtils.OnDialogCloseListener() {
            @Override
            public void onClose() {
                ContactsUtils.checkGrantPanel(getView(), PhonesFragment.this);
                _dialog = null;
            }
        });
        _dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        switch (requestCode) {
            case RequestCodes.CONTACT:
                ContactsUtils.requestPhoneNumber(this, CONTACT_REQUEST_CODE);
                break;
            case RequestCodes.GRANT:
                getView().findViewById(R.id.grant_panel).setVisibility(View.GONE);
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onRemoveItem(int index) {
        super.onRemoveItem(index);
        save();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || data == null)
            return;

        ContactsUtils.receivePhoneNumber(getContext(), data, _dialog);
    }
}