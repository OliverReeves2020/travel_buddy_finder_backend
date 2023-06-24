package listners;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.annotations.Nullable;

import javax.servlet.*;
import javax.servlet.annotation.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import function.sender;


//this listner utilises firestore add snapshot listner on the trip
@WebListener
public class FireListener implements ServletContextListener,Runnable{
    Thread Searcher;
    public FireListener() throws ServletException{
        System.out.println("running listening");
        Searcher= new Thread(this);
        Searcher.start();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        /* This method is called when the servlet context is initialized(when the Web application is deployed). */
        System.out.println("listening");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("deaf");
        /* This method is called when the servlet Context is undeployed or Application Server shuts down. */
    }



    @Override
    public void run() {
        System.out.println("run");

        System.out.println("listening");
        InputStream serviceAccount;
        serviceAccount=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(FirebaseApp.getApps().isEmpty()){
            FirebaseApp.initializeApp(options);}


        Firestore db = FirestoreClient.getFirestore();

        db.collection("Trips")
                .addSnapshotListener(
                        new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(
                                    @Nullable QuerySnapshot snapshots, @Nullable FirestoreException e) {
                                if (e != null) {
                                    System.err.println("Listen failed:" + e);
                                    return;
                                }
                                //send to rabbit mq allows for asynchrnous
                                System.out.println("new");
                                try {
                                    System.out.println("updates");
                                    //sender q=new sender();
                                    send();
                                } catch (Exception ex) {
                                    System.out.println(ex);
                                    throw new RuntimeException(ex);
                                }

                            }
                        });


    }
    public void send() throws Exception {
        try{
        sender q=new sender();
        q.sending("update");}
        catch(Exception e){System.out.println(e);}


    }





}
