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
import function.jsontojavaBuilder;
import json.Login;
import json.Rand;
import json.express;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@WebServlet(name = "Login", urlPatterns = "/Login")
public class checkUserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("get issued");

        //load response and request data
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String contentType = request.getContentType();
        Login q = new Login();
        if (contentType == null) {
            throw new IOException("no request body");
        }
        if (contentType.equals("application/json")) {

            InputStream body = request.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(body));
            //now convert json into
            jsontojavaBuilder build = new jsontojavaBuilder();
            q = (Login) build.convert(read, "login");
            if (q == null) {
                throw new AccessDeniedException("User id and password not entered");
            }
            if (q.getUserID() == null || q.getPassKey() == null) {
                throw new AccessDeniedException("User id and password not entered");
            }
        } else {
            throw new IllegalStateException("content type not supported");
        }
        InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
        //FirebaseApp.initializeApp(options);
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<DocumentSnapshot> doc = db.collection("Users").document(q.getUserID()).get();
        try {
            if (!doc.get().exists()) {
                throw new IOException("user does not exist");
            }
            if (!doc.get().get("password").equals(q.getPassKey())) {
                throw new IOException("incorrect password");
            }
            response.setStatus(200);
            PrintWriter out = response.getWriter();
            out.print("{\t\"match\": \"valid\"\n}");
            out.flush();
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
            response.setStatus(501);
            PrintWriter out = response.getWriter();
            out.print("{\t\"match\": \"invalid\"\n}");
            out.flush();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            response.setStatus(501);
            PrintWriter out = response.getWriter();
            out.print("{\t\"match\": \"invalid\"\n}");
            out.flush();
        }


    }

    //create user using password and user id
    //we assume that the client utilises the random id generator
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        //load response and request data
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String contentType = request.getContentType();
        Login q = new Login();
        if (contentType == null) {
            throw new IOException("no request body");
        }
        if (contentType.equals("application/json")) {
            InputStream body = request.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(body));
            //now convert json into
            jsontojavaBuilder build = new jsontojavaBuilder();
            q = (Login) build.convert(read, "login");
            if (q == null) {
                throw new AccessDeniedException("User id and password not entered");
            }
            if (q.getUserID() == null || q.getPassKey() == null) {
                throw new AccessDeniedException("User id and password not entered");
            }
        } else {
            throw new IllegalStateException("content type not supported");
        }

        try {
            //!FirebaseApp.length ? firebase.initializeApp(config) : firebase.app()
            InputStream serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            //FirebaseApp.initializeApp(options);
            Firestore db = FirestoreClient.getFirestore();

            ApiFuture<DocumentSnapshot> doc = db.collection("Users").document(q.getUserID()).get();
            if (doc.get().exists()) {
                throw new IOException("user does not exist");
            }
            HashMap<String, Object> vals = new HashMap<String, Object>();
            vals.put("password", q.getPassKey());
            db.collection("Users").document(q.getUserID()).set(vals);
            response.setStatus(201);
            PrintWriter out = response.getWriter();
            out.println("{\t\"result\": \"created\"\n}");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }


    }

}