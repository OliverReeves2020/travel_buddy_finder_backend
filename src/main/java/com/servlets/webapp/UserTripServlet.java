package com.servlets.webapp;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import function.jsontojavaBuilder;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import json.createTrip;

import javax.servlet.ServletException;
import java.io.*;
import java.util.concurrent.ExecutionException;

    @WebServlet(name = "UserTripServlet", value = "/UserTripServlet")
public class UserTripServlet extends HttpServlet {
    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");



        //System.out.println("get request received");
        //if request contains one of the values we are looking for


        String UserId=request.getHeader("userID");


        System.out.println("valid received");
        //get data from firebase
        try {
            JsonObject respond=getFireBase(UserId);
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

    private JsonObject getFireBase(String userID) throws IOException, ExecutionException, InterruptedException {

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

        //query constructor
        if(userID!=null){query=query.whereEqualTo("userID",userID);}
        query=query.orderBy("timestamp", Query.Direction.DESCENDING);
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



}

