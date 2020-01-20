package com.example.ruoliamoci;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

public class FragmentPlayer extends ListFragment {

    View mContentView=null;
    private List<String> files = new ArrayList<String>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new FileAdapter(getActivity(), R.layout.layoutfile,files ));
        ((FileAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragmentplayer, null);
        if(savedInstanceState!=null)
            files=savedInstanceState.getStringArrayList("files");
        return mContentView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("files", (ArrayList<String>) files);
    }

    public void addElement(String stringa){
        Log.d("FragmentPlayer","new disegnino");
        files.add(stringa);
        ((FileAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String string= (String) getListAdapter().getItem(position);
        PlayerActivity.DrawDialog dd=new PlayerActivity.DrawDialog();
        dd.setUri(Uri.parse(string));
        dd.show(getActivity().getSupportFragmentManager(), "DialogDrawing");
    }

    private class FileAdapter extends ArrayAdapter<String> {

        private List<String> items;

        public FileAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            items=objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.layoutfile, null);
            }
            String string = items.get(position);
            if (string != null) {
                TextView bottom = (TextView) v.findViewById(R.id.uri_file);
                ImageView im= v.findViewById(R.id.little_draw);

                if (bottom != null) {
                    bottom.setText(string);
                }

                if(im!=null){
                    RuoliamociUtilities.setImage(Uri.parse(string),im,getActivity());
                }

            }

            return v;
        }
    }

}

