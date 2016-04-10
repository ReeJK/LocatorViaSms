package com.wnezros.locatorviasms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class PhrasesFragment extends ListWithPlusFragment {

    public PhrasesFragment() {
    }

    @SuppressWarnings("unused")
    public static PhrasesFragment newInstance() {
        return new PhrasesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setDescription(R.string.phrases_description);

        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String[] phrases = Settings.getPhrases(prefs);
        _adapter.addAll(phrases);

        return view;
    }

    private void save() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Settings.setPhrases(prefs, _adapter.toArray(new String[0]));
    }

    @Override
    protected void onPlusClick() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.phrase_dialog_title);
        dialog.setMessage(R.string.phrase_dialog_message);

        final EditText editText = new EditText(getContext());
        dialog.setView(editText);

        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                _adapter.add(editText.getText().toString());
                save();
            }
        });

        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.show();
    }

    @Override
    protected void onRemoveItem(int index) {
        _adapter.removeAt(index);
        save();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
