package json;

public class Trip {
    private String tripname;
    private String location;
    private String date;



    public String getName() { return tripname; }
    public void setName(String value) { this.tripname = value; }

    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }

    public String getDate() { return date; }
    public void setDate(long value) { this.date = String.valueOf(value); }
}