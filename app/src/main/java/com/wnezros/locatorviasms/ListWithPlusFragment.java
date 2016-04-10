package com.wnezros.locatorviasms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ListWithPlusFragment extends Fragment {

    protected ArrayAdapter<String> _adapter;
    protected ListView _listView;
    private TextView _headerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_with_plus, container, false);

        _headerView = (TextView) view.findViewById(R.id.description);

        _adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        _listView = (ListView) view.findViewById(R.id.list);
        _listView.setAdapter(_adapter);
        registerForContextMenu(_listView);

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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(String.valueOf(_listView.getAdapter().getItem(info.position)));
            menu.add(R.string.delete);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(info.targetView.getParent() != _listView)
            return false;

        if (item.getItemId() == 0) {
            onRemoveItem(info.position);
            return true;
        }
        return false;
    }

    protected void setDescription(int resId) {
        _headerView.setText(resId);
    }

    protected abstract void onPlusClick();
    protected abstract void onRemoveItem(int index);
}
