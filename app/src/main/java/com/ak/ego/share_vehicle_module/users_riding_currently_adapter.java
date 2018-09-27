package com.ak.ego.share_vehicle_module;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ak.ego.R;
import com.ak.ego.recyclerViewItemClickListener;
import com.ak.ego.share_vehicle_module.SharingUserModule.userProfile;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class users_riding_currently_adapter extends RecyclerView.Adapter<users_riding_currently_adapter.ViewHolder> {
    public ArrayList<userProfile> users = new ArrayList<>();
    public Activity mActivity;
    public recyclerViewItemClickListener cancel_listener;

    public users_riding_currently_adapter(Activity mActivity, ArrayList<userProfile> users , recyclerViewItemClickListener mlistener)
    {
        this.mActivity = mActivity;
        this.users = users;
        this.cancel_listener = mlistener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_sharing_ride_presentation_card, parent, false);
        users_riding_currently_adapter.ViewHolder viewHolder = new users_riding_currently_adapter.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        userProfile user = users.get(position);
        holder.rider_name.setText(user.getName());
        Geocoder geocoder = new Geocoder(mActivity.getApplicationContext());
        try {
            List<Address> start_addresses = geocoder.getFromLocation(user.getStart_location_latitude(),user.getStart_location_longitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            holder.rider_pickup_location.setText(start_addresses.get(0).getFeatureName() + " "+start_addresses.get(0).getAddressLine(0));

            List<Address> end_addresses = geocoder.getFromLocation(user.getEnd_location_latitude(),user.getEnd_location_longitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            holder.rider_drop_location.setText(end_addresses.get(0).getFeatureName() + " "+end_addresses.get(0).getAddressLine(0));

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        holder.rider_contact_number.setText(user.getUser_phone());
        holder.rider_ride_cost.setText(user.getRide_cost());
        holder.ride_status.setText("Pickup Pending");

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView rider_name;
        public TextView rider_contact_number;
        public TextView rider_pickup_location;
        public TextView rider_drop_location;
        public TextView rider_ride_cost;
        public TextView ride_status;

        public ViewHolder(View itemView)
        {
            super(itemView);
            rider_name = (TextView)itemView.findViewById(R.id.rider_name);
            rider_contact_number = (TextView)itemView.findViewById(R.id.rider_phone);
            rider_pickup_location = (TextView)itemView.findViewById(R.id.rider_pickup_address);
            rider_drop_location = (TextView)itemView.findViewById(R.id.rider_drop_address);
            rider_ride_cost = (TextView)itemView.findViewById(R.id.ride_total_cost);
            ride_status = (TextView)itemView.findViewById(R.id.ride_status);
        }
    }
}
