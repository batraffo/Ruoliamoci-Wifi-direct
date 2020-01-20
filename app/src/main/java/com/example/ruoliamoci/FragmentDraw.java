package com.example.ruoliamoci;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import androidx.fragment.app.Fragment;

public class FragmentDraw extends Fragment {

    View mContentView=null;
    DrawingView dw;
    int pos;

    public FragmentDraw(int pos) {
        this.pos=pos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.layoutdraw, null);

        mContentView.findViewById(R.id.clear_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().findViewById(R.id.frag_dm).setVisibility(View.VISIBLE);
            }
        });

        mContentView.findViewById(R.id.send_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((DmActivity) getActivity()).sendDrawing(dw.saveDrawing(),pos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().findViewById(R.id.frag_dm).setVisibility(View.VISIBLE);
            }
        });

        dw=mContentView.findViewById(R.id.drawing);

        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        dw.setmPaint(mPaint);

        return mContentView;
    }

}
