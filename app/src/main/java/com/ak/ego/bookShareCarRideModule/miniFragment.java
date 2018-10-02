package com.ak.ego.bookShareCarRideModule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ak.ego.R;

public class miniFragment extends Fragment {
    public RelativeLayout mini_basic_layout;
    public RelativeLayout mini_grand_latout;
    public RelativeLayout mini_classic_layout;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mini_card_recycler_card_layout, container, false);
        mini_basic_layout = (RelativeLayout)view.findViewById(R.id.mini_basic_layout);
        mini_grand_latout = (RelativeLayout)view.findViewById(R.id.mini_grand_layout);
        mini_classic_layout = (RelativeLayout)view.findViewById(R.id.mini_classic_layout);

        mini_basic_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForRideActivity.search_ride("Mini_Basic");
            }
        });

        mini_grand_latout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForRideActivity.search_ride("Mini_Grand");
            }
        });

        mini_classic_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForRideActivity.search_ride("Mini_Classic");
            }
        });

        return view;
    }
}
