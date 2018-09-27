package com.ak.ego.share_vehicle_module;

public class Ride {
    public String start_location;
    public String end_location;
    public String count_of_occupants;
    public String time_of_travel;
    public String ride_total_amount;
    public String getStart_location() {
        return start_location;
    }

    public void setStart_location(String start_location) {
        this.start_location = start_location;
    }

    public String getEnd_location() {
        return end_location;
    }

    public void setEnd_location(String end_location) {
        this.end_location = end_location;
    }

    public String getCount_of_occupants() {
        return count_of_occupants;
    }

    public void setCount_of_occupants(String count_of_occupants) {
        this.count_of_occupants = count_of_occupants;
    }

    public String getTime_of_travel() {
        return time_of_travel;
    }

    public void setTime_of_travel(String time_of_travel) {
        this.time_of_travel = time_of_travel;
    }

    public String getRide_total_amount() {
        return ride_total_amount;
    }

    public void setRide_total_amount(String ride_total_amount) {
        this.ride_total_amount = ride_total_amount;
    }
}
