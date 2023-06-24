package json;

public class createTrip {
    private String location;
    private String tripName;
    private String tripID;
    private String weather;
    private String userID;

    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }

    public String getTripName() { return tripName; }
    public void setTripName(String value) { this.tripName = value; }

    public String getTripID() { return tripID; }
    public void setTripID(String value) { this.tripID = value; }

    public String getWeather() { return weather; }
    public void setWeather(String value) { this.weather = value; }

    public String getUserID() { return userID; }
    public void setUserID(String value) { this.userID = value; }
}