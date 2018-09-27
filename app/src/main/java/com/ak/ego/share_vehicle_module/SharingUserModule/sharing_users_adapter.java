package com.ak.ego.share_vehicle_module.SharingUserModule;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ak.ego.R;
import com.ak.ego.recyclerViewItemClickListener;

import java.util.ArrayList;

public class sharing_users_adapter extends RecyclerView.Adapter<sharing_users_adapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<userProfile> users_asking_for_taxi;
    private recyclerViewItemClickListener listener;
    private recyclerViewItemClickListener mlistener;

    public sharing_users_adapter(Activity mActivity, ArrayList<userProfile> users_asking_for_taxi, recyclerViewItemClickListener listener, recyclerViewItemClickListener mlistener)
    {
        this.mActivity = mActivity;
        this.users_asking_for_taxi = users_asking_for_taxi;
        this.listener = listener;
        this.mlistener = mlistener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_asking_share_presentation_card, parent, false);
        sharing_users_adapter.ViewHolder viewHolder = new sharing_users_adapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        userProfile user = users_asking_for_taxi.get(position);
        holder.user_name.setText(user.getName());
        holder.user_phone_number.setText(user.getUser_phone());
        holder.user_source_location.setText(user.getPickup_location());
        holder.user_destination_location.setText(user.getDrop_location());
    }

    @Override
    public int getItemCount() {
        return users_asking_for_taxi.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView user_name;
        private TextView user_source_location;
        private TextView user_destination_location;
        private ImageView call_user;
        private ImageView track_user;
        private TextView user_phone_number;
        private Button confirm_ride_of_user;


        public ViewHolder(View itemView) {
            super(itemView);
            user_name = (TextView)itemView.findViewById(R.id.user_name);
            user_phone_number = (TextView)itemView.findViewById(R.id.user_phone);
            user_destination_location = (TextView)itemView.findViewById(R.id.user_destination_area);
            user_source_location = (TextView)itemView.findViewById(R.id.user_source_area);
            track_user = (ImageView)itemView.findViewById(R.id.track_user_on_road);
            call_user = (ImageView)itemView.findViewById(R.id.call_user_on_road);
            track_user.setOnClickListener(this);
            confirm_ride_of_user = (Button)itemView.findViewById(R.id.confirm_ride_of_user);
            confirm_ride_of_user.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.confirm_ride_of_user)
            {
                mlistener.onItemClick(v,getAdapterPosition());
            }
            else
            {
                listener.onItemClick(v,getAdapterPosition());
            }
        }
    }
}
