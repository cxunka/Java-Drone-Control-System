package uk.ac.ed.inf.aqmaps;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

/*
Webclient
    cosists of 2 parts: URLBuilder and HttpClient.
    WebClient can visit server.
    URLBuilder can build url from several format of string.
 */

public class WebClient {
    //the default port inside class. the port in command have priority.
    public static int port = 80;

    //"This constructor should never be used."
    private WebClient(){
        throw new UnsupportedOperationException("This constructor should never be used.");
    }

    /*
    Setter and Getter for local port.
     */
    public static void setPort(int port) {
        WebClient.port = port;
    }

    public static int getPort() {
        return port;
    }

    /*
    decode3Words from "word.word.word" to "word/word/word"
    @param what3Words: "word.word.word"
    @return filePath: "word/word/word"
     */
    public static String decode3Words(String what3Words){
        String[] wordsArray = what3Words.split("\\.");
        String filePath = StringUtils.join(wordsArray,"/");
        return filePath;
    }

    /*
    URLBuilder can build url from String in special format.
    @param input: String can be "noFly" or "word/word/word" or  "word.word.word"
        or "Year/Month/Day"(4-digit/2-digit/2-digit).
    @return urlString: URL is get as String.
     */
    public static String buildURL(String input){
        Objects.requireNonNull(input, "");

        String urlString = null;
        String portStr = Integer.toString(port);

        if (input.matches("noFly")) {
            urlString = "http://localhost:" + portStr + "/buildings/" + "/no-fly-zones.geojson";
        }else if (input.matches("\\d*/\\d*/\\d*")) {
            urlString = "http://localhost:" + portStr + "/maps/" + input + "/air-quality-data.json";
        }else if(input.matches("[a-z]*/[a-z]*/[a-z]*")){
            urlString = "http://localhost:" + portStr + "/words/" + input + "/details.json";
        }else if(input.matches("[a-z]*.[a-z]*.[a-z]*")){
            urlString = "http://localhost:" + portStr + "/words/" + decode3Words(input) + "/details.json";
        }
        return urlString;
    }

    /*
    HttpClient
        is used to visit local server.
     @param urlString: URL String is used to request local server.
     @return body: the file content from server.
     */

    // Just have one HttpClient, shared between all HttpRequests
    private static final HttpClient client = HttpClient.newHttpClient();

    public static String getResponse(String urlString){
        Objects.requireNonNull(urlString, "Input URL String must not be null");

        // HttpClient assumes that it is a GET request by default.
        var request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();

        // The response object is of class HttpResponse<String>
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        var status = response.statusCode();
        var body = response.body();


        return body;
    }
}

