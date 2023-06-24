package json;

public class updateTrip {
    private String tripID;
    private String tripname;
    private String location;
    private String date;


    public String getTripID() {return tripID;}
    public void setTripID(String value){this.tripID=value;}

    public String getName() { return tripname; }
    public void setName(String value) { this.tripname = value; }

    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }

    public String getDate() { return date; }
    public void setDate(long value) { this.date = String.valueOf(value); }
}