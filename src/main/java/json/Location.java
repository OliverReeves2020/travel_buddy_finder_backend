package json;

public class Location {

        private long placeID;

        private String lat;
        private String lon;
        private String display_name;

        public long getPlaceID() { return placeID; }
        public void setPlaceID(long value) { this.placeID = value; }



        public String getLat() { return lat; }
        public void setLat(String value) { this.lat = value; }

        public String getLon() { return lon; }
        public void setLon(String value) { this.lon = value; }

        public String getDisplayName() { return display_name; }
        public void setDisplayName(String value) { this.display_name = value; }


}