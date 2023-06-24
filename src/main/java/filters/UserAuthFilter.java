package filters;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@WebFilter(filterName = "UserAuthFilter")
public class UserAuthFilter implements Filter {
    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        //when filter is called it reads the local cache for firebase for user accounts
        //only allowing accounts of user and password matches
        //will be a different filter than one
        // of admin type in which the user will be of admin group


        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if(httpRequest.getHeader("userID")!=null&&httpRequest.getHeader("passKey")!=null){
            InputStream in=getClass().getClassLoader().getResourceAsStream("DB/cloudcompdata-407ae-firebase-adminsdk-ngouw-a1fa920dd1.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();
            if(FirebaseApp.getApps().isEmpty()){FirebaseApp.initializeApp(options);}
            //FirebaseApp.initializeApp(options);
            Firestore db = FirestoreClient.getFirestore();

            ApiFuture<DocumentSnapshot> doc=db.collection("Users").document(httpRequest.getHeader("userID")).get();
            try {

                if (!doc.get().exists()){throw new IOException("user does not exist");}
                if(!doc.get().get("password").equals(httpRequest.getHeader("passKey"))){throw new IOException("incorrect password");}



            System.out.println("filter called"+httpRequest.getHeader("userID"));







            chain.doFilter(request, response);
        } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }}
        else{throw new ServletException("userid or pass not in headers");}



}}
