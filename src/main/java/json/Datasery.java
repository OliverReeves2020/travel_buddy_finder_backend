package json;

public class Datasery {
    private long date;
    private String weather;
    private Temp2M temp2M;
    private long wind10MMax;

    public long getDate() { return date; }
    public void setDate(long value) { this.date = value; }

    public String getWeather() { return weather; }
    public void setWeather(String value) { this.weather = value; }

    public Temp2M getTemp2M() { return temp2M; }
    public void setTemp2M(Temp2M value) { this.temp2M = value; }

    public long getWind10MMax() { return wind10MMax; }
    public void setWind10MMax(long value) { this.wind10MMax = value; }
}