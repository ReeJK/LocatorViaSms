package com.wnezros.locatorviasms;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class PhonesFragment extends ListWithPlusFragment {

    public PhonesFragment() {
    }

    @SuppressWarnings("unused")
    public static PhonesFragment newInstance() {
        return new PhonesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setDescription(R.string.phones_description);

        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String[] phones = Settings.getPhones(prefs);
        _adapter.addAll(phones);

        return view;
    }

    private void save() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Settings.setPhones(prefs, _adapter.toArray(new String[0]));
    }

    @Override
    protected void onPlusClick() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.phone_dialog_title);
        dialog.setMessage(R.string.phone_dialog_message);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.enter_phone, null);
        dialog.setView(view);

        final EditText phoneText = (EditText) view.findViewById(R.id.phone);
        final ImageButton pickContactButton = (ImageButton) view.findViewById(R.id.pick_phone);
        pickContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkAndRequestPermissions()) {
                    requestContact();
                }
            }
        });

        _numberReceiveListener = new OnNumberReceiveListener() {
            @Override
            public void onReceiveNumber(String number) {
                phoneText.setText(number);
            }
        };

        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                _adapter.add(phoneText.getText().toString());
                save();
                _numberReceiveListener = null;
            }
        });

        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                _numberReceiveListener = null;
            }
        });

        dialog.show();
    }

    private void requestContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }

    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.READ_CONTACTS }, 2);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestContact();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onRemoveItem(int index) {
        _adapter.removeAt(index);
        save();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || data == null)
            return;

        Uri uri = data.getData();

        if (uri != null) {
            Cursor c = null;
            try {
                c = getContext().getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    String number = c.getString(0);
                    if (_numberReceiveListener != null)
                        _numberReceiveListener.onReceiveNumber(number);
                }
            } catch (SecurityException e) {

            } finally {
                if (c != null) {
                    c.close();
                }
            }

        }
    }

    private OnNumberReceiveListener _numberReceiveListener;

    private interface OnNumberReceiveListener {
        void onReceiveNumber(String number);
    }
}
