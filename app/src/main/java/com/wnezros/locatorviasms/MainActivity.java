package com.wnezros.locatorviasms;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter _sectionsPagerAdapter;
    private ViewPager _viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Settings.initialize(this);

        _sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        _viewPager = (ViewPager) findViewById(R.id.container);
        _viewPager.setAdapter(_sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(_viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            _viewPager.setCurrentItem(0);
            return true;
        }
        if(id == R.id.action_phones) {
            _viewPager.setCurrentItem(1);
            return true;
        }
        if(id == R.id.action_phrases) {
            _viewPager.setCurrentItem(2);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if(requestCode == 1) {
            if(grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                return;

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.location_permissions);
            dialog.setMessage(R.string.location_permission_message);
            dialog.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    requestPermissions();
                }
            });
            dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.finish();
                }
            });
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<Pair<Integer, Class>> _pages = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            _pages.add(new Pair<Integer, Class>(R.string.settings, SettingsFragment.class));
            _pages.add(new Pair<Integer, Class>(R.string.phones, PhonesFragment.class));
            _pages.add(new Pair<Integer, Class>(R.string.phrases, PhrasesFragment.class));
        }

        @Override
        public Fragment getItem(int position) {
            Class fragmentClass = _pages.get(position).second;

            try {
                Object result = fragmentClass.getMethod("newInstance").invoke(null);
                return (Fragment) result;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            throw new IllegalArgumentException("Exception on " + position + " position");
        }

        @Override
        public int getCount() {
            return _pages.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(_pages.get(position).first);
        }
    }
}
