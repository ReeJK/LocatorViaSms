package com.wnezros.locatorviasms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public final class ContactsUtils {
    public static void checkGrantPanel(View view, final Fragment fragment) {
        View grantPanel = view.findViewById(R.id.grant_panel);
        if (grantPanel != null) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                grantPanel.setVisibility(View.GONE);
            } else {
                grantPanel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkAndRequestPermissions(fragment, RequestCodes.GRANT);
                    }
                });
            }
        }
    }

    public static boolean checkAndRequestPermissions(Fragment fragment, int code) {
        if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[] { Manifest.permission.READ_CONTACTS }, code);
            return false;
        }
        return true;
    }

    public static void requestPhoneNumber(Fragment fragment, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void receivePhoneNumber(Context context, Intent data, OnNumberReceiveListener listener) {
        Uri uri = data.getData();

        if (uri != null) {
            Cursor c = null;
            try {
                c = context.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    String number = c.getString(0);
                    if (listener != null)
                        listener.onReceiveNumber(number);
                }
            } catch (SecurityException e) {
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public static String getContactDisplayNameByNumber(Context context, String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = number;

        Cursor contactLookup;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            contactLookup = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        } catch (IllegalArgumentException e) {
            return name;
        } catch (SecurityException e) {
            return name;
        }

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

    public interface OnNumberReceiveListener {
        void onReceiveNumber(String number);
    }

    public interface OnDialogCloseListener {
        void onClose();
    }

    public static final class DialogBuilder implements OnNumberReceiveListener {
        private final Fragment _owner;
        private final AlertDialog.Builder _dialog;
        private final View _view;
        private OnDialogCloseListener _closeListener;

        public DialogBuilder(final Fragment fragment) {
            _owner = fragment;

            _dialog = new AlertDialog.Builder(fragment.getContext());
            _dialog.setTitle(R.string.phone_dialog_title);

            _view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.enter_phone, null);
            _dialog.setView(_view);

            _dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (_closeListener != null)
                        _closeListener.onClose();
                }
            });
        }

        public void setMessage(int messageId) {
            _dialog.setMessage(messageId);
        }

        public void setPhoneNumberRequestCode(final int requestCode) {
            final ImageButton pickContactButton = (ImageButton) _view.findViewById(R.id.pick_phone);
            pickContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ContactsUtils.checkAndRequestPermissions(_owner, RequestCodes.CONTACT)) {
                        ContactsUtils.requestPhoneNumber(_owner, requestCode);
                    }
                }
            });
        }

        public void setResultListener(final OnNumberReceiveListener listener) {
            final EditText phoneText = (EditText) _view.findViewById(R.id.phone);
            _dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onReceiveNumber(phoneText.getText().toString());
                    if (_closeListener != null)
                        _closeListener.onClose();
                }
            });
        }

        public void setOnCloseListener(OnDialogCloseListener closeListener) {
            _closeListener = closeListener;
        }

        public AlertDialog show() {
            return _dialog.show();
        }

        @Override
        public void onReceiveNumber(String number) {
            final EditText phoneText = (EditText) _view.findViewById(R.id.phone);
            phoneText.setText(number);
        }
    }
}
