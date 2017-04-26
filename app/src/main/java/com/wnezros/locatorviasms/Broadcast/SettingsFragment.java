package com.wnezros.locatorviasms.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.wnezros.locatorviasms.BasePreferenceFragment;
import com.wnezros.locatorviasms.ContactsUtils;
import com.wnezros.locatorviasms.R;
import com.wnezros.locatorviasms.RequestCodes;
import com.wnezros.locatorviasms.Settings;
import com.wnezros.locatorviasms.TimePreference;
import com.wnezros.locatorviasms.TimePreferenceDialogFragmentCompat;

import java.util.ArrayList;

public class SettingsFragment extends BasePreferenceFragment implements ContactsUtils.OnNumberReceiveListener {
    public static final String UPDATE_ACTION = "com.wnezros.locatorviasms.updateevent";
    private static final int CONTACT_REQUEST_CODE = 1;
    private static final String PHONE_PREFERENCE_PREFIX = "phone_nmb_";

    class PreferenceGroupAdapterEx extends PreferenceGroupAdapter {
        private int _position;

        public PreferenceGroupAdapterEx(PreferenceGroup preferenceGroup) {
            super(preferenceGroup);
        }

        public int getPosition() {
            return _position;
        }

        @Override
        public PreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(final PreferenceViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    _position = holder.getLayoutPosition();
                    return false;
                }
            });
        }

        @Override
        public void onViewRecycled(PreferenceViewHolder holder) {
            holder.itemView.setOnLongClickListener(null);
            super.onViewRecycled(holder);
        }
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("unused")
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    private ContactsUtils.DialogBuilder _dialog;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkAndToggleStartStop();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_broadcast);

        bindPreferenceSummaryToValue(findPreference("broadcast_comment"));
        bindPreferenceSummaryToValue(findPreference("broadcast_interval"));

        loadPhones();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndToggleStartStop();
        getContext().registerReceiver(_updateReceiver, new IntentFilter(UPDATE_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(_updateReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ContactsUtils.checkGrantPanel(view, this);

        ImageButton plusButton = (ImageButton) view.findViewById(R.id.plus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlusClick();
            }
        });

        return view;
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView view = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        registerForContextMenu(view);
        return view;
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapterEx(preferenceScreen);
    }

    private void checkAndToggleStartStop() {
        Preference toggle = findPreference("broadcast_toggle");
        Preference interval = findPreference("broadcast_interval");
        if (BroadcastUtils.isBroadcasting(getContext())) {
            toggle.setTitle(R.string.broadcast_stop);
            toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    BroadcastUtils.cancelBroadcasting(getContext());
                    checkAndToggleStartStop();
                    return true;
                }
            });

            interval.setEnabled(false);
        } else {
            toggle.setTitle(R.string.broadcast_start);
            toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    BroadcastUtils.scheduleBroadcasting(getContext());
                    checkAndToggleStartStop();
                    return true;
                }
            });

            interval.setEnabled(true);
        }
    }

    private void onPlusClick() {
        _dialog = new ContactsUtils.DialogBuilder(this);
        _dialog.setMessage(R.string.phone_dialog_message_broadcast);
        _dialog.setPhoneNumberRequestCode(CONTACT_REQUEST_CODE);
        _dialog.setResultListener(this);
        _dialog.setOnCloseListener(new ContactsUtils.OnDialogCloseListener() {
            @Override
            public void onClose() {
                ContactsUtils.checkGrantPanel(getView(), SettingsFragment.this);
                _dialog = null;
            }
        });
        _dialog.show();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || data == null)
            return;

        ContactsUtils.receivePhoneNumber(getContext(), data, _dialog);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v == getListView())
        {
            PreferenceGroupAdapterEx adapter = (PreferenceGroupAdapterEx)getListView().getAdapter();
            Preference pref = getPreferenceScreen().getPreference(adapter.getPosition());
            if (pref.getKey() != null && pref.getKey().startsWith(PHONE_PREFERENCE_PREFIX)) {
                menu.setHeaderTitle(pref.getTitle());
                menu.add(R.string.delete);
            }
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            PreferenceGroupAdapterEx adapter = (PreferenceGroupAdapterEx)getListView().getAdapter();
            PreferenceScreen screen = getPreferenceScreen();
            removeNumber(screen, adapter.getPosition());
            savePhones();
            return true;
        }

        return false;
    }

    @Override
    public void onReceiveNumber(String number) {
        PreferenceScreen screen = getPreferenceScreen();
        addNumber(screen, number);

        savePhones();
    }

    private void addNumber(PreferenceScreen screen, String number) {
        Preference pref = new Preference(getContext());
        pref.setKey(PHONE_PREFERENCE_PREFIX + screen.getPreferenceCount());
        pref.setTitle(number);
        pref.setSummary(ContactsUtils.getContactDisplayNameByNumber(getContext(), number));
        screen.addPreference(pref);
    }

    private void removeNumber(PreferenceScreen screen, int position) {
        Preference pref = screen.getPreference(position);
        screen.removePreference(pref);
    }

    private void loadPhones() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String[] phones = Settings.getBroadcastPhones(prefs);
        PreferenceScreen screen = getPreferenceScreen();

        for(String phone : phones)
            addNumber(screen, phone);
    }

    private void savePhones() {
        ArrayList<String> phones = new ArrayList<>();
        PreferenceScreen screen = getPreferenceScreen();
        for(int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);
            if (pref.getKey() != null && pref.getKey().startsWith(PHONE_PREFERENCE_PREFIX))
                phones.add(pref.getTitle().toString());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Settings.setBroadcastPhones(prefs, phones.toArray(new String[0]));
    }
}
