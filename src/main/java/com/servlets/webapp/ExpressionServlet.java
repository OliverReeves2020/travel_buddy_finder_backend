package com.servlets.webapp;



import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import function.jsontojavaBuilder;
import json.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@WebServlet(name = "Express", value = "/Express")
public class ExpressionServlet extends HttpServlet {
    //get interests of current user and their current trips
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("get express");
        //load response and request data
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String contentType = request.getContentType();
        String Auth= request.getHeader("userID");
        express q=new express();
        if (contentType.equals("application/json")) {
            InputStream body = request.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(body));
            jsontojavaBuilder build = new jsontojavaBuilder();
            q = (express) build.convert(read, "express");

        } else {
            throw new IllegalStateException("content type not supported");
        }
        String trip = q.getTripID();
        String user = q.getUserID();
        if (!user.equals(Auth)){throw new AccessDeniedException("user does not have access to this trip");}
        InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if(FirebaseApp.getApps().isEmpty()){FirebaseApp.initializeApp(options);}
        //FirebaseApp.initializeApp(options);

        Firestore db = FirestoreClient.getFirestore();


                Query query= db.collection("Trips").whereEqualTo("userID",user);
                //get query response
                query=query.orderBy("timestamp", Query.Direction.DESCENDING);
                ApiFuture<QuerySnapshot> resp = query.get();
                JsonObject respond=new JsonObject();
                JsonArray olist=new JsonArray();
        System.out.println("get express");
                //for each document created by the user get the interest amount and list of users
        try {
            for (DocumentSnapshot document : resp.get().getDocuments()) {
                JsonObject doc=new JsonObject();
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
                //add to response json object
                doc.addProperty("tripID",document.getId());
                //responder.add(document.getId(), doc);
                olist.add(doc);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        respond.add("trips",olist);
        PrintWriter out = response.getWriter();
                out.print(respond);
                out.flush();


        //check that user matches user in header
        //check that user exists
        //check that trip exists
        //re


    }



    //update interest of a specific post
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //check that trip exists
        //check that we do not have intrest in trip
        //if we do reduce trip express amount and remove from array
        //if we don't increase trip express amount and add to array
        System.out.println("here");
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String contentType = request.getContentType();
        express q=new express();
        if (contentType.equals("application/json")) {
            InputStream body = request.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(body));
            //now convert json into
            jsontojavaBuilder build = new jsontojavaBuilder();
            q = (express) build.convert(read, "express");
        } else {
            throw new IllegalStateException("content type not supported");
        }
        if(q==null){throw new IOException("user id or trip id not provided");}
        String trip = q.getTripID();
        String user = request.getHeader("userID");
        if(trip ==null|| user ==null){throw new IOException("user or trip params not provided");}
        try{
            System.out.println("here");
            InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if(FirebaseApp.getApps().isEmpty()){FirebaseApp.initializeApp(options);}
            //FirebaseApp.initializeApp(options);
            Firestore db = FirestoreClient.getFirestore();

            DocumentReference documentReference = db.collection("Trips").document(trip);
            ApiFuture<DocumentSnapshot>doc=documentReference.get();
            DocumentSnapshot document = doc.get();
            if(!document.exists()){throw new IOException("trip does not exist");}

            ArrayList interest = (ArrayList) document.get("interest");

            //if in list remove and decrease input amount
            if(interest==null||!interest.contains(user)){
                documentReference.update("interest", FieldValue.arrayUnion(user));
                documentReference.update("interestamount", FieldValue.increment(1));
            }

            //if not in list add and increase
            else{
                documentReference.update("interest", FieldValue.arrayRemove(user));
                if(!(document.get("interestamount") ==null) || !((long)document.get("interestamount") ==0L)){
                    documentReference.update("interestamount", FieldValue.increment(-1));}

            }

    } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



}
