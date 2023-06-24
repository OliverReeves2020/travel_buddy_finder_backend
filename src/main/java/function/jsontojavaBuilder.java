package function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import json.*;

import java.io.BufferedReader;
import java.io.IOException;

public class jsontojavaBuilder {
    public Object convert(BufferedReader read, String object){

            //json to java object

                try {
                    // Set up GSON

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    // Read the JSON from file
                    String jsonString = "";

                    while(read.ready()){
                        jsonString+=read.readLine();
                    }
                    //trip object

                    switch (object) {
                        case "trip":
                            return gson.fromJson(jsonString, Trip.class);
                        case "updateTrip":
                            return gson.fromJson(jsonString, updateTrip.class);
                        //location object
                        case "location":
                            System.out.println(jsonString);
                            if (!jsonString.equals("{}")) {
                                jsonString = jsonString.substring(1, jsonString.length() - 1);
                            }
                            System.out.println(jsonString);
                            return (gson.fromJson(jsonString, Location.class));
                        //seven timer object
                        case "login":
                            return gson.fromJson(jsonString, Login.class);
                        case "seven":
                            return (gson.fromJson(jsonString, SevenTimer.class));
                        case "rand":
                            return (gson.fromJson(jsonString, Rand.class));
                        case "createTrip":
                            return(gson.fromJson(jsonString,createTrip.class));
                        case"express":
                            return gson.fromJson(jsonString, express.class);
                        default:
                            System.out.println("error");
                            break;
                    }


                    // Convert the JSON to Java objects (These must reflect the structure of the JSON). In this case we are using the same structure as in the XML example so we can reuse the library.Book file


                } catch (IOException e) {
                    return null;
                }

        return null;
    }
}
