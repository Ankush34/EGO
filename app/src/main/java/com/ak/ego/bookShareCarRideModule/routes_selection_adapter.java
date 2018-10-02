package com.ak.ego.bookShareCarRideModule;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ak.ego.R;
import com.ak.ego.recyclerViewItemClickListener;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;

import java.util.ArrayList;
import java.util.List;

public class routes_selection_adapter extends RecyclerView.Adapter<routes_selection_adapter.ViewHolder> {

    private ArrayList<Route> routes = new ArrayList<>();
    private Activity mActivity;
    private recyclerViewItemClickListener routes_selection_listener;
    public String start_location;
    public String end_location;
    public routes_selection_adapter(Activity activity, ArrayList<Route> routes, recyclerViewItemClickListener routes_selection_listener, String start_location, String end_location)
    {
        this.routes = routes;
        this.mActivity = activity;
        this.routes_selection_listener = routes_selection_listener;
        this.start_location = start_location;
        this.end_location =end_location;
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_presentation_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.start_location_text.setText(start_location);
        holder.end_location_text.setText(end_location);
        List<Leg> legs = routes.get(position).getLegList();
        String time_to_travel = " "+legs.get(0).getDuration().getText();
        for(int i = 1; i<legs.size();i++)
        {
            time_to_travel = time_to_travel +" + "+legs.get(i).getDuration().getText();
        }
        holder.time_to_travel_text.setText(time_to_travel);
        holder.via_location_text.setText(routes.get(position).getSummary());
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView start_location_text;
        private TextView end_location_text;
        private TextView via_location_text;
        private TextView time_to_travel_text;

        public ViewHolder(View itemView) {
            super(itemView);
            start_location_text = (TextView)itemView.findViewById(R.id.start_location_text);
            end_location_text = (TextView)itemView.findViewById(R.id.end_location_text);
            via_location_text = (TextView)itemView.findViewById(R.id.via_location);
            time_to_travel_text = (TextView)itemView.findViewById(R.id.time_to_travel);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            routes_selection_listener.onItemClick(v,getAdapterPosition());
        }
    }
}
