package com.ak.ego.share_vehicle_module.SharingUserModule;

public class userProfile {
    public String user_id;
    public String user_phone;
    public String name;
    public UserLocation location;
    public String pickup_location;
    public String drop_location;
    public Double start_location_latitude;
    public Double start_location_longitude;
    public Double end_location_latitude;
    public Double end_location_longitude;
    public Double latitude;
    public Double longitude;
    public String seeker_ride_id;
    public String provider_ride_user_id;
    public String provider_ride_id;
    public String ride_cost;
    public String time_to_reach;


    public String getTime_to_reach() {
        return time_to_reach;
    }

    public void setTime_to_reach(String time_to_reach) {
        this.time_to_reach = time_to_reach;
    }

    public String getRide_cost() {
        return ride_cost;
    }

    public void setRide_cost(String ride_cost) {
        this.ride_cost = ride_cost;
    }

    public String getSeeker_ride_id() {
        return seeker_ride_id;
    }

    public void setSeeker_ride_id(String seeker_ride_id) {
        this.seeker_ride_id = seeker_ride_id;
    }

    public String getProvider_ride_user_id() {
        return provider_ride_user_id;
    }

    public void setProvider_ride_user_id(String provider_ride_user_id) {
        this.provider_ride_user_id = provider_ride_user_id;
    }

    public String getProvider_ride_id() {
        return provider_ride_id;
    }

    public void setProvider_ride_id(String provider_ride_id) {
        this.provider_ride_id = provider_ride_id;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Double getStart_location_latitude() {
        return start_location_latitude;
    }

    public void setStart_location_latitude(Double start_location_latitude) {
        this.start_location_latitude = start_location_latitude;
    }

    public Double getStart_location_longitude() {
        return start_location_longitude;
    }

    public void setStart_location_longitude(Double start_location_longitude) {
        this.start_location_longitude = start_location_longitude;
    }

    public Double getEnd_location_latitude() {
        return end_location_latitude;
    }

    public void setEnd_location_latitude(Double end_location_latitude) {
        this.end_location_latitude = end_location_latitude;
    }

    public Double getEnd_location_longitude() {
        return end_location_longitude;
    }

    public void setEnd_location_longitude(Double end_location_longitude) {
        this.end_location_longitude = end_location_longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserLocation getLocation() {
        return location;
    }

    public void setLocation(UserLocation location) {
        this.location = location;
    }

    public String getPickup_location() {
        return pickup_location;
    }

    public void setPickup_location(String pickup_location) {
        this.pickup_location = pickup_location;
    }

    public String getDrop_location() {
        return drop_location;
    }

    public void setDrop_location(String drop_location) {
        this.drop_location = drop_location;
    }
}
