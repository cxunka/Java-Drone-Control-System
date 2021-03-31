package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.List;

/*
JSONMap
    a map in geojson format.
    Display all sensors.
    Display the fly path of plane.
    This class make use of WebClient, RoutePlanner and JSONProcessor.
    Constructor must be call while using.
 */

public class JSONMap {

    private Command command;
    private String date;

    private List<Feature> sensors;
    private List<Feature> path;

    private JSONProcessor jsonProcessor;
    private RoutePlanner routePlanner;

    /*
    Create a map consists of sensors and flypath of drone.
    Initialize jsonProcessor and routePlanner
    @param command: used to buildURL
     */
    public JSONMap(Command command) {
        this.command = command;
        this.date = command.getDate();
        jsonProcessor = new JSONProcessor(command);
    }

    public void setSensors() {
        String url = WebClient.buildURL(this.date);
        this.sensors = jsonProcessor.toPointCollection(JSONProcessor.toSensorList(url)).features();
    }

    public void setPath() {
        RoutePlanner routePlanner = new RoutePlanner(this.command);
        routePlanner.run();
        System.out.println(routePlanner.getVisitedSensors().length + "," + routePlanner.getMoveCount());
        this.path = routePlanner.planAStar();
    }

    public List<Feature> getSensors() {
        return sensors;
    }

    public List<Feature> getPath() {
        return path;
    }

    public static String toJSONStr(JSONMap map){
        String jsonStr;

        List<Feature> featureList = new ArrayList<>();

        if (map.getSensors() != null){
            featureList.addAll(map.getSensors());
        }else{ }

        if (map.getPath() != null){
            featureList.addAll(map.getPath());
        }else{ }

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featureList);
        jsonStr = featureCollection.toJson();

        return jsonStr;
    }

}
