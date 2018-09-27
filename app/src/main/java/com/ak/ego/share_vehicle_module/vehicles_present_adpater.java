package com.ak.ego.share_vehicle_module;

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

public class vehicles_present_adpater extends RecyclerView.Adapter<vehicles_present_adpater.ViewHolder> {

    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private Activity mActivity;
    private recyclerViewItemClickListener vehicle_selection_listner;
    public vehicles_present_adpater(Activity activity, ArrayList<Vehicle> vehicles, recyclerViewItemClickListener vehicle_selection_listener)
    {
        this.vehicles = vehicles;
        this.mActivity = activity;
        this.vehicle_selection_listner = vehicle_selection_listener;
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vehicles_recycler_view_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        if(vehicle.getType().equals("Taxi"))
        {
            holder.vehicle_logo.setImageResource(R.drawable.taxi_for_share);
        }
        holder.vehicle_number.setText(vehicle.getNumber());
        holder.vehicle_reg_number.setText(vehicle.getRegistration_number());
        holder.vehicle_city.setText(vehicle.getCity());
        holder.vehicle_type.setText(vehicle.getType());
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView vehicle_number;
        public TextView vehicle_reg_number;
        public TextView vehicle_type;
        public TextView vehicle_city;
        public ImageView vehicle_logo;
        public Button start_share_button;

        public ViewHolder(View itemView) {
            super(itemView);
            vehicle_number = (TextView)itemView.findViewById(R.id.vehicle_number);
            vehicle_reg_number = (TextView)itemView.findViewById(R.id.vehicle_registraion_number);
            vehicle_city = (TextView)itemView.findViewById(R.id.vehicle_city_name);
            vehicle_type = (TextView)itemView.findViewById(R.id.vehicle_type);
            vehicle_logo = (ImageView)itemView.findViewById(R.id.vehicle_logo);
            start_share_button = (Button)itemView.findViewById(R.id.start_share_button);
            start_share_button.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        vehicle_selection_listner.onItemClick(v,getAdapterPosition());
        }
    }
}
