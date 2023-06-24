package com.servlets.webapp;
//import jakarta.faces.validator.ValidatorException;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.google.gson.*;
import function.jsontojavaBuilder;
import json.*;


@WebServlet(name = "trip", urlPatterns = "/trip")
public class tripServlet extends HttpServlet {
    //get trip in json fomrat from parameters quireied to google firebase
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String contentType= request.getHeader("Content-Type");
        //we accept location tripname trip id weather and specific users to query firebase



        String location = null;String tripName=null;String tripID=null;String weather=null;String userID=null;
        if (contentType != null) {
        if (contentType.equals("application/json")) {

            InputStream body = request.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(body));
            //now convert json into
            jsontojavaBuilder build = new jsontojavaBuilder();
            createTrip q = (createTrip) build.convert(read, "createTrip");
            location=q.getLocation();
            tripName=q.getTripName();
            tripID=q.getTripID();
            weather= q.getWeather();
            userID=q.getUserID();

            }else{throw new IOException("invalid body format");}}

            //System.out.println("get request received");
        //if request contains one of the values we are looking for





            System.out.println("valid received");
            //get data from firebase
            try {
                JsonObject respond=getFireBase(location,tripName,userID, weather);
                PrintWriter out = response.getWriter();
                out.print(respond);
                out.flush();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }





        //query data base
        //convert json into java object
        //return json of query

    }


    //create trip
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //post request will have params that authenticate current user

        //get request body
        System.out.println("post request");
        String type = request.getContentType();
        InputStream body = request.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(body));
        String line;
        Trip tripData = new Trip();
        //read request body into json or xml
        //if json
        if (type == null) {throw new IOException("no request body");}

        if (type.equals("application/json")) {

            //convert json body into java objects
            jsontojavaBuilder build=new jsontojavaBuilder();
            tripData =(Trip) build.convert(read,"trip");

        }
         else {
            throw new IOException("invalid body type");
        }
        System.out.println("location->" + tripData.getLocation());
        System.out.println("Date->" + tripData.getDate());
        System.out.println("name:" + tripData.getName());

        String location;
        int day;
        int month;
        int year;
        String date;
        String tripName;
        try {
            date = tripData.getDate();
            location = tripData.getLocation();
            tripName = tripData.getName();
            //check date is not negative

            if ((Integer.parseInt(date) < 0)) {
                throw new IOException("date contained a negative");
            }



            //convert date into string format to split data

            if (!(date.length() == 8)) {
                throw new IOException("date format should be DDMMYYYY");
            }

            day = Integer.parseInt(date.substring(0, 2));
            month = Integer.parseInt(date.substring(2, 4));
            year = Integer.parseInt(date.substring(4, 8));
            if (!(isValidDate(day, month, year))) {
                throw new IOException("date is not in correct format");
            }

        } catch (Exception exception) {
            throw new IOException(exception);
        }


        System.out.println("location api");
        Location geocode = locate(location);
        System.out.println("location api");
        //we now have lat long and a display name
        //call 7timer function
        //https://www.7timer.info/bin/api.pl?lon=-74.006&lat=40.713&product=civil&output=json


        //check if date provide is within time frame
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        int weekday = Integer.parseInt((date.substring(4, 8)) + date.substring(2,4)+date.substring(0,2));
        int numb=Integer.parseInt(LocalDate.now().format(formatter))-weekday;
        //check that weekday minus the current time is within 7days otherwise set weather to unavailbe
        //if valid date range
        String weather="unavailable";
        if(numb>=-6 &&numb<=0){
            System.out.println("working");
            numb=numb*-1;
            String params;
            params = "lon=" + geocode.getLon() + "&lat=" + geocode.getLat() + "&product=civillight&output=json";
            URL url = new URL("https://world.openfoodfacts.org/api/v2/product/4100290024758?fields=product_name");
            HttpURLConnection seven = (HttpURLConnection) url.openConnection();
            seven.setRequestMethod("GET");
            //set rapid api key
            seven.setDoOutput(true);
            seven.connect();

            //Read more: https://www.7timer.info/bin/api.pl?
            System.out.println("2");
            if (seven.getResponseCode() == 200) {
                BufferedReader sevenResponse = new BufferedReader(new InputStreamReader(seven.getInputStream()));
                SevenTimer sevenjson;
                try {
                    jsontojavaBuilder build=new jsontojavaBuilder();
                    sevenjson = (SevenTimer) build.convert(sevenResponse,"seven");
                    System.out.println("here->");
                } catch (Exception e) {
                    throw new UnavailableException("");
                }
                weather=sevenjson.getDataseries()[numb].getWeather();
                System.out.println("-<");
                //sevenData[0].


            } else {
                throw new IOException();
            }
        }
        System.out.println("6");
        //else weather equlas unavailable

        //we now have date,displayname,displayname==location,weather,userid,
        //we need to gen a unique trip id and create a blank area for intrests
        //then call firebase


        //get random number id
        try {
            System.out.println("6");
            String tripID = genTripid();
            System.out.println("7");
            //get weather data
            //call post function
            System.out.println(weather);
            postFireBase(tripID, geocode.getDisplayName(), tripName, request.getHeader("userID"), weather,date);
            System.out.println("trip created");


        } catch (Exception e) {
            throw new ServletException(e);
        }


    }
    //update trip user must be the same user that created trip
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {





        System.out.println("put request");
        String type = request.getContentType();
        InputStream body = request.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(body));

        //read request body into json or xml
        //if json
        updateTrip tripData;
        if (type.equals("application/json")) {

            //convert json body into java objects
            jsontojavaBuilder build=new jsontojavaBuilder();
            tripData = (updateTrip) build.convert(read,"updateTrip");
        }
        else{throw new IOException("invalid format in body");}
        //process request data to java from json or xml

        //validate processed data
        //take trip param and headers user id to search if doc exists
        //other params form update
        String User=request.getHeader("userID");
        String trip=tripData.getTripID();

        //run update function
        try {
            putFireBase(User,trip,tripData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //return response to confirm changes


    }




    //delete trip
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("delete request");
        String type = request.getContentType();
        InputStream body = request.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(body));

        //read request body into json or xml
        //if json
        updateTrip tripData;
        if (type.equals("application/json")) {

            //convert json body into java objects
            jsontojavaBuilder build=new jsontojavaBuilder();
            tripData = (updateTrip) build.convert(read,"updateTrip");


        }
        else{throw new IOException("invalid format in body");}
        //process request data to java from json or xml

        //validate processed data
        //take trip param and headers user id to search if doc exists
        //other params form update
        String User=request.getHeader("userID");


        InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if(FirebaseApp.getApps().isEmpty()){FirebaseApp.initializeApp(options);}
            //FirebaseApp.initializeApp(options);
            Firestore db = FirestoreClient.getFirestore();

            System.out.println("here");
            if(tripData.getTripID()==null){throw new IOException("no Trip ID supplied");}
            DocumentReference documentReference = db.collection("Trips").document(tripData.getTripID());
            ApiFuture<DocumentSnapshot>doc=documentReference.get();
        DocumentSnapshot document = null;
        System.out.println("here");
        try {
            document = doc.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println("here");
        if(!document.exists()){
            throw new IOException("trip does not exist");}

        System.out.println("doc exists");
        String checkUser= (String) document.get("userID");
        System.out.println(checkUser+User);
        //if userID not available
        if(checkUser==null){throw new IOException("document error contact admin");}

        if (checkUser.equals(User)){

            db.collection("Trips").document(tripData.getTripID()).delete();
            System.out.println("deleted document");
        }
        else{System.out.println("here");
            throw new AccessDeniedException("user does not have permission to delete this file");
        }
    }



    private String genTripid() throws ServletException,IOException{
        //get random number id


        URL url = new URL("https://api.random.org/json-rpc/2/invoke");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept","application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        //json string to request uuid
        String req="{\"jsonrpc\":\"2.0\",\"method\":\"generateUUIDs\",\"params\":{\"apiKey\":\"4fca824a-fb05-4710-99fe-39e6dc3c2166\",\"n\":1},\"id\":15998}";
        OutputStream os = con.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        osw.write(req);
        osw.flush();
        osw.close();
        os.close();
        con.connect();
        //if connection failed
        int status = con.getResponseCode();
        if (status != 200) {
            System.out.println("fail");
            con.disconnect();
            throw new IOException("error");
        }
        //if connection okay
        else {
            InputStream body = con.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(body));
            Rand UUID = new Rand();
            jsontojavaBuilder build=new jsontojavaBuilder();
            UUID= (Rand) build.convert(read,"rand");
            System.out.println("{\"UUID\":\"" +UUID.getResult().getRandom().getData()[0]+ "\"}");
            return String.valueOf(UUID.getResult().getRandom().getData()[0]);
        }










    }
    private JsonObject getFireBase(String location, String tripName, String userID, String weather) throws IOException, ExecutionException, InterruptedException {

        //connect to firebase database
        //!FirebaseApp.length ? firebase.initializeApp(config) : firebase.app()
        InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if(FirebaseApp.getApps().isEmpty()){FirebaseApp.initializeApp(options);}
        //FirebaseApp.initializeApp(options);
        Firestore db = FirestoreClient.getFirestore();
        Query query= db.collection("Trips");
        query=query.orderBy("timestamp", Query.Direction.DESCENDING);
        //query constructor
        if (location!=null){query=query.whereEqualTo("location",location);}
        if(tripName!=null){System.out.println("issue");
            query=query.whereEqualTo("tripName",tripName);}
        if(userID!=null){query=query.whereEqualTo("userID",userID);}
        //if(tripID!=null){query=query.whereEqualTo("trip")}
        if(weather!=null){query=query.whereEqualTo("weather",weather);}
        //String tripID=request.getParameter("tripID");
        //String weather=request.getParameter("weather");
        //String userID=request.getParameter("userID");

        ApiFuture<QuerySnapshot> resp = query.limit(20).get();

        JsonObject respond=new JsonObject();
        JsonArray olist=new JsonArray();
        System.out.println("query got created");
        //now we cycle through each found document and output into json format for get response
        for (DocumentSnapshot document : resp.get().getDocuments()) {
            JsonObject doc=new JsonObject();
            JsonObject responder=new JsonObject();
            doc.addProperty("tripName", (String) document.get("tripName"));
            doc.addProperty("location",(String) document.get("location"));
            doc.addProperty("userID", (String) document.get("userID"));
            doc.addProperty("weather",(String) document.get("weather"));
            doc.addProperty("date",(String) document.get("date"));

            JsonArray list=new JsonArray();
            if(document.get("interestamount")==null){doc.addProperty("interestamount",0L);}
            else{
                doc.addProperty("interestamount", (long) document.get("interestamount"));}
            //list of all userID that have intrest



            if(document.get("interest")!=null){
                Gson gson = new GsonBuilder().create();
                JsonArray interest = gson.toJsonTree(document.get("interest")).getAsJsonArray();
                list.addAll(interest);}
            doc.add("interest",list);
            //list of all userID that have interest
            //add to response json object
            doc.addProperty("tripID",document.getId());
            //responder.add(document.getId(), doc);
            olist.add(doc);

        }
        System.out.println("response json created");
        respond.add("trips",olist);
        return respond;




    }


    private void postFireBase(String tripID, String location, String tripName, String userID, String weather, String date){
        //we now have date,displayname,displayname==location,weather,userid,

        try {
            //

            //connect to firebase database
            //!FirebaseApp.length ? firebase.initializeApp(config) : firebase.app()
           InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if(FirebaseApp.getApps().isEmpty()){FirebaseApp.initializeApp(options);}
            //FirebaseApp.initializeApp(options);
            Firestore db = FirestoreClient.getFirestore();
            //define hash map of values to add to database
            HashMap<String,Object> vals=new HashMap<String,Object>();

            //create random trip id from 7timer

            vals.put("userID",userID);
            vals.put("tripName",tripName);
            vals.put("location",location);
            vals.put("weather",weather);
            vals.put("date",date);
            vals.put("interest",null);


            vals.put("timestamp",FieldValue.serverTimestamp());

            //db.collection("Trips").document(tripID).set(vals);
            ApiFuture<DocumentSnapshot>doc=db.collection("Trips").document(tripID).get();
            DocumentSnapshot document = doc.get();
            doc.get().getUpdateTime();
            //checks that our document is new and not replacing an old one
            if(document.exists()){throw new IOException("document exists");}
            else{db.collection("Trips").document(tripID).set(vals);
                System.out.println("file found");

            }
            //db.close();
            System.out.println("file found");




        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // final FirebaseDatabase database = FirebaseDatabase.getInstance();
            //DatabaseReference ref = database.getReference("https://console.firebase.google.com/project/cloudcompdata-407ae/firestore/data/~2FUsers~2FRoot");
    }
    private void putFireBase(String currentUser, String selectedTrip, updateTrip params) throws IOException {
        System.out.println("put called");
        try{
            InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if(FirebaseApp.getApps().isEmpty()){FirebaseApp.initializeApp(options);}
        //FirebaseApp.initializeApp(options);
        Firestore db = FirestoreClient.getFirestore();

        DocumentReference documentReference = db.collection("Trips").document(selectedTrip);
        ApiFuture<DocumentSnapshot>doc=documentReference.get();
        DocumentSnapshot document = doc.get();





        if(!document.exists()){throw new IOException("trip does not exist");}
            System.out.println("doc exists");
        String checkUser= (String) document.get("userID");
        System.out.println(checkUser);
        if(checkUser==null){throw new IOException("document error contact admin");}
        //check that selected trip belongs to current user that is supplied in document header
        if (checkUser.equals(currentUser)) {


            HashMap<String, Object> vals = new HashMap<>();
            //update user based of provided parameters
            //parameter pre-check
            String location = params.getLocation();
            String date = params.getDate();
            String name = params.getName();

            //if params has a provided parameter
            //if location is changed but not date
            System.out.println("here");
            if (location != null) {

                Location geocode = locate(location);

                vals.put("location",geocode.getDisplayName());

                //if date was changed use new date
                if(date != null){
                    int day = Integer.parseInt(date.substring(0, 2));
                    int month = Integer.parseInt(date.substring(2, 4));
                    int year = Integer.parseInt(date.substring(4, 8));
                    if(!(isValidDate(day,month,year))){throw new IOException("invalid date format");}
                    String weather=seven(geocode,date);
                    vals.put("date",date);
                    vals.put("weather",weather);
                }
                //if date was not change retrieve date from doc

                else{
                    System.out.println("here");
                    String oldDate = (String) document.get("date");
                    if(oldDate==null){throw new IOException("old date is not correct");}
                    String weather=seven(geocode,oldDate);
                    vals.put("weather",weather);
                }

            }

            //if date was changed but not location
            else if (date != null) {

                Location geocode = locate((String) document.get("location"));

                int day = Integer.parseInt(date.substring(0, 2));
                int month = Integer.parseInt(date.substring(2, 4));
                int year = Integer.parseInt(date.substring(4, 8));
                if(!(isValidDate(day,month,year))){throw new IOException("invalid date format");}
                String weather=seven(geocode,date);
                vals.put("date",date);
                vals.put("weather",weather);
            }
            if(name!=null){
                vals.put("tripName",name);
            }
            if(vals.isEmpty()) {
                throw new IOException("no params to update provided");
            }

            //if name was passed to be changed
            System.out.println("here");


            documentReference.update(vals);
            System.out.println("updated doc");
        }
        else{throw new IOException("trip was not created by the current user");}

        }
        catch (NullPointerException e){
            throw new IOException(e);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (UnavailableException e) {
            throw new RuntimeException(e);
        }


    }
    //adapted from https://www.geeksforgeeks.org/program-check-date-valid-not/
    private boolean isValidDate(int d, int m, int y) {
        // If year, month and day
        // are not in given range
        if (y > 9999||
                y < 2022)
            return false;
        if (m < 1 || m > 12)
            return false;
        if (d < 1 || d > 31)
            return false;

        // Handle February month
        // with leap year
        if (m == 2)
        {
            if (((y % 4 == 0) && (y % 100 != 0)) || (y % 400 == 0))
                return (d <= 29);
            else
                return (d <= 28);
        }
        // Months of April, June,
        // Sept and Nov must have
        // number of days less than
        // or equal to 30.
        if (m == 4 || m == 6 ||
                m == 9 || m == 11)
            return (d <= 30);

        return true;
    }
    private Location locate(String location) throws IOException {

            URL url = new URL("https://forward-reverse-geocoding.p.rapidapi.com/v1/search?q=" + location + "&accept-language=en&polygon_threshold=0.0");
            HttpURLConnection geocodingSearch = (HttpURLConnection) url.openConnection();
            geocodingSearch.setRequestMethod("GET");
            //con.setRequestProperty("Content-Type", "application/json");
            //set rapid api key
            geocodingSearch.setRequestProperty("X-RapidAPI-Key", "c6f3c20c80msh2b7717614e1b6b2p1b3b51jsna25772d690c0");
            geocodingSearch.setRequestProperty("X-RapidAPI-Host", "forward-reverse-geocoding.p.rapidapi.com");
            geocodingSearch.setDoOutput(true);
            if(geocodingSearch.getResponseCode()!=200){throw new IOException("location does not exist or invalid");}
            BufferedReader geocodeSearch = new BufferedReader(new InputStreamReader(geocodingSearch.getInputStream()));
            Location geocode = null;
            try {
                jsontojavaBuilder build=new jsontojavaBuilder();
                geocode = (Location) build.convert(geocodeSearch,"location");

            } catch (Exception e) {
                throw new IOException(e);
            }
            if ((geocode.getDisplayName() == null || geocode.getLat() == null || geocode.getLon() == null)) {
                throw new IOException("invalid location");
            }
            return geocode;
    }
    private String seven(Location geocode,String date) throws IOException, UnavailableException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        int weekday = Integer.parseInt((date.substring(4, 8)) + date.substring(2,4)+date.substring(0,2));
        int numb=Integer.parseInt(LocalDate.now().format(formatter))-weekday;
        //check that weekday minus the current time is within 7days otherwise set weather to unavailbe
        //if valid date range
        String weather="unavailable";
            System.out.println("number->"+numb+" weekday"+weekday+"minus"+Integer.parseInt(LocalDate.now().format(formatter)));
            if(numb>=-6 &&numb<=0){
            System.out.println("working");
            numb=numb*-1;
            String params;
            params = "lon=" + geocode.getLon() + "&lat=" + geocode.getLat() + "&product=civillight&output=json";
                URL url = new URL("https://www.7timer.info/bin/api.pl?" + params);
            HttpURLConnection seven = (HttpURLConnection) url.openConnection();
            seven.setRequestMethod("GET");
            //set rapid api key
            seven.setDoOutput(true);
            seven.connect();

            //Read more: https://www.7timer.info/bin/api.pl?

            if (seven.getResponseCode() == 200) {
                BufferedReader sevenResponse = new BufferedReader(new InputStreamReader(seven.getInputStream()));
                SevenTimer sevenjson;
                try {
                    jsontojavaBuilder build=new jsontojavaBuilder();
                    sevenjson = (SevenTimer) build.convert(sevenResponse,"seven");
                } catch (Exception e) {
                    throw new UnavailableException("");
                }

                assert sevenjson != null;
                weather=sevenjson.getDataseries()[numb].getWeather();

                //sevenData[0].


            } else {
                throw new IOException();
            }
            }
            return weather;
    }


}
