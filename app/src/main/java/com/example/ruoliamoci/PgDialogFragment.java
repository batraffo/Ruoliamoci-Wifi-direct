package com.example.ruoliamoci;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class PgDialogFragment extends DialogFragment {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    Uri uri=null;

    View mContentView = null;
    ImageView image;

    boolean formcopiled=false;

    @Override
    public View onCreateView(LayoutInflater infl, ViewGroup container, Bundle savedInstanceState) {
        mContentView = infl.inflate(R.layout.pgdialoglayout, container, false);
        image=mContentView.findViewById(R.id.pg_image);

        if(savedInstanceState!=null) {
            String uris=savedInstanceState.getString("uri");
            if(uris!=null) {
                uri=Uri.parse(uris);
                if(!RuoliamociUtilities.setImage(uri,image,getActivity()))
                    uri=null;
            }
            //rimetto il testo che c'era prima
            ((EditText)mContentView.findViewById(R.id.name_edit)).setText(savedInstanceState.getString("name"));
            ((EditText)mContentView.findViewById(R.id.race_edit)).setText(savedInstanceState.getString("race"));
            ((EditText)mContentView.findViewById(R.id.class_edit)).setText(savedInstanceState.getString("classs"));
        }

        mContentView.findViewById(R.id.chooser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Allow user to pick an image from Gallery or other
                // registered apps
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
            }
        });

        mContentView.findViewById(R.id.sendtodm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formcopiled=true;
                String name = ((EditText) mContentView.findViewById(R.id.name_edit)).getText().toString();
                String race = ((EditText) mContentView.findViewById(R.id.race_edit)).getText().toString();
                String classs = ((EditText) mContentView.findViewById(R.id.class_edit)).getText().toString();
                ((PlayerActivity)getActivity()).setAndSend(name,race,classs,uri);
                dismiss();
            }
        });

        getDialog().setTitle("Give me your pg's info");
        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null) {
            uri = data.getData();
            if(!RuoliamociUtilities.setImage(uri,image,getActivity()))
                uri=null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(uri!=null)
            outState.putString("uri",uri.toString());
        String name = ((EditText) mContentView.findViewById(R.id.name_edit)).getText().toString();
        String race = ((EditText) mContentView.findViewById(R.id.race_edit)).getText().toString();
        String classs = ((EditText) mContentView.findViewById(R.id.class_edit)).getText().toString();
        outState.putString("name",name);
        outState.putString("race",race);
        outState.putString("classs",classs);
    }


}
