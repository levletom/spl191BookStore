package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.InputJsonReciver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {

    private static InputJsonReciver g;
    public static void main(String[] args) {
        String jsonInputFilePath = args[0];
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        File file = new File(jsonInputFilePath);

        try(Reader reader = new InputStreamReader(new FileInputStream(file))){
             g = gson.fromJson(reader,InputJsonReciver.class);

        }  catch (IOException e) {
            e.printStackTrace();
        }


    }
}

