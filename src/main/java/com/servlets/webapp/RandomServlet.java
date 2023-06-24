package com.servlets.webapp;

import function.jsontojavaBuilder;
import json.Rand;
import json.Trip;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

@WebServlet(name = "Random", value = "/Random")
public class RandomServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //get random number id
        System.out.println("recieved");
        try {
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
            System.out.println("new");
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




                PrintWriter out = response.getWriter();
                if(UUID==null){System.out.println("null");}

                out.println("{\"UUID\":\"" +UUID.getResult().getRandom().getData()[0]+ "\"}");
                out.flush();
                out.close();
                con.disconnect();
            }
        } catch (IOException e) {

        }

    }
        }


