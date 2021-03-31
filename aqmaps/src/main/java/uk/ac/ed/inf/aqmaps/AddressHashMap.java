package uk.ac.ed.inf.aqmaps;

import java.util.HashMap;
import java.util.Objects;

/*
AddressHashMap
    is used to store the location and its corresponding coordinate.
 */

public class AddressHashMap {
    private HashMap<String, String> hashmap;

    /*
    Create an object of hashmap from URL.
    @param urlString: Generating hashmap needs request coordinate from server, and URL is required.
    @return hashmap: key - location of sensor in What3Words. coordinates get as String in fomat - longitude,latitude
     */
    public AddressHashMap(String urlString){
        this.hashmap = new HashMap<>();
        Objects.requireNonNull(urlString, "URL must not be null.");

        var sensors = JSONProcessor.toSensorList(urlString);

        for (int i = 0; i < sensors.size(); i++){
            var sensor = sensors.get(i);
            var point = JSONProcessor.toPoint(sensors.get(i));
            String lngLatStr = Double.toString(point.longitude()) + "," + Double.toString(point.latitude());
            hashmap.put(sensor.getLocation(), lngLatStr);
        }
    }

    public String getCoordinate(String what3Words) {
        return hashmap.get(what3Words);
    }
}
