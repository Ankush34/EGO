package com.ak.ego.share_vehicle_module;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ak.ego.R;
import com.ak.ego.share_vehicle_module.SharingUserModule.userProfile;

import java.util.ArrayList;

public class provided_share_rides_info_adapter extends RecyclerView.Adapter<provided_share_rides_info_adapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<Ride> rides;
    public provided_share_rides_info_adapter(Activity mActivity, ArrayList<Ride> rides)
    {
        this.mActivity = mActivity;
        this.rides = rides;
    }

    @NonNull
    @Override
    public provided_share_rides_info_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.provider_share_rides_completed_recycler_card, parent, false);
        provided_share_rides_info_adapter.ViewHolder viewHolder = new provided_share_rides_info_adapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull provided_share_rides_info_adapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView start_location;
        private TextView end_location;
        private TextView total_occupants;
        private TextView ride_total_amount;
        private TextView total_ride_time;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

