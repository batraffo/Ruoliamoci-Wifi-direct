package com.example.ruoliamoci;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

public class FragmentDm extends ListFragment {

    private List<PlayerCharacter> giocatori = new ArrayList<PlayerCharacter>();
    private Map<Integer,Integer> ggValue=new HashMap<Integer,Integer>();//i map the position in the list giocatori with the position of the correspondent thread in the arraylist of the service
    int giocatoriAggiunti=0;

    View mContentView=null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new PgAdapter(getActivity(), R.layout.layoutpg, giocatori));
        ((PgAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("FragmentDm","----------------------->here");
        mContentView = inflater.inflate(R.layout.fragmentdm, null);
        if(savedInstanceState!=null) {
            giocatori = savedInstanceState.getParcelableArrayList("player");
            ggValue= (Map<Integer, Integer>) savedInstanceState.getSerializable("values");
            giocatoriAggiunti=savedInstanceState.getInt("num");
        }
        mContentView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DmActivity) getActivity()).setDate();
            }
        });
        return mContentView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("player", (ArrayList<? extends Parcelable>) giocatori);
        outState.putSerializable("values", (Serializable) ggValue);
        outState.putInt("num",giocatoriAggiunti);
    }

    public void addList(int pos, PlayerCharacter player) {
        Log.d("Fragment","new player incoming! "+pos);
        giocatori.add(player);
        ggValue.put(pos,giocatori.size()-1);
        giocatoriAggiunti++;
        ((PgAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void removeList(int pos){
        Log.d("FragmentDm","rip player "+pos);
        int posi=ggValue.get(pos);
        giocatori.remove(posi);
        //i update the hashmap
        for(int i=pos; i<giocatoriAggiunti; i++){
            ggValue.put(i,ggValue.get(i)-1);
        }
        ((PgAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void reloadView(){
        ((PgAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        PlayerCharacter pg = (PlayerCharacter) getListAdapter().getItem(position);
        int pos = pg.pos;
        Log.d("FragmentDm","thing "+position+" thread "+pos);
        //i open the fragmentdraw
        ((DmActivity) getActivity()).sendTest(pos);
    }

        private class PgAdapter extends ArrayAdapter<PlayerCharacter> {

        private List<PlayerCharacter> items;

        public PgAdapter(@NonNull Context context, int resource, List<PlayerCharacter> list) {
            super(context, resource,list);
            items=list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.layoutpg, null);
            }
            PlayerCharacter tipo = items.get(position);
            if (tipo != null) {
                TextView top = (TextView) v.findViewById(R.id.pgname);
                TextView bottom = (TextView) v.findViewById(R.id.pgdetails);
                ImageView image=v.findViewById(R.id.icon1);
                if (top != null) {
                    top.setText(tipo.name);
                }
                if (bottom != null) {
                    bottom.setText(tipo.race+" "+tipo.classs);
                }
                if(image!=null){
                    RuoliamociUtilities.setImage(tipo.uriPhoto,image,getActivity());
                }
            }

            return v;

        }

    }

}
