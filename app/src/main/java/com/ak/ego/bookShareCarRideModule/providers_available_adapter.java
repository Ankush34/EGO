package com.ak.ego.bookShareCarRideModule;
import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ak.ego.R;
import com.ak.ego.recyclerViewItemClickListener;
import com.ak.ego.share_vehicle_module.SharingUserModule.userProfile;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class providers_available_adapter extends RecyclerView.Adapter<providers_available_adapter.ViewHolder> {

    private ArrayList<userProfile> drivers = new ArrayList<>();
    private Activity mActivity;
    private recyclerViewItemClickListener driver_selection_listener;
    public providers_available_adapter(Activity activity, ArrayList<userProfile> drivers, recyclerViewItemClickListener listener)
    {
        this.mActivity = activity;
        this.drivers = drivers;
        this.driver_selection_listener = listener;
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.present_ride_selection_card_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
     userProfile user = drivers.get(position);
     holder.driver_name.setText(user.getUser_phone());
     holder.driver_name.setText(user.getName());
     holder.car_type.setText("Ego Mini");
     holder.time_to_reach.setText(user.getTime_to_reach());
     holder.cost_of_ride.setText("");
     int time = Integer.parseInt(user.getTime_to_reach().split(" ")[0]);
     if( time < 5)
     {
         holder.layout_correction.setBackgroundColor(Color.BLUE);
     }
     else if ( time > 5 && time < 10)
     {
         holder.layout_correction.setBackgroundColor(Color.RED);
     }
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView car_type;
        private TextView car_model;
        private TextView driver_name;
        private TextView driver_contact;
        private TextView time_to_reach;
        private CardView confirm_ride_button;
        private TextView cost_of_ride;
        private RelativeLayout layout_correction;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            layout_correction = itemView.findViewById(R.id.timing_correction_layout);
            confirm_ride_button = (CardView) itemView.findViewById(R.id.confirm_driver_button);
            car_type = (TextView) itemView.findViewById(R.id.car_type);
            car_model = (TextView) itemView.findViewById(R.id.car_model);
            driver_name = (TextView)itemView.findViewById(R.id.driver_name);
            driver_contact = (TextView)itemView.findViewById(R.id.driver_contact);
            time_to_reach = (TextView)itemView.findViewById(R.id.time_to_reach);
            cost_of_ride = (TextView)itemView.findViewById(R.id.total_cost);

        }

        @Override
        public void onClick(View v) {
            driver_selection_listener.onItemClick(v,getAdapterPosition());
        }
    }
}
