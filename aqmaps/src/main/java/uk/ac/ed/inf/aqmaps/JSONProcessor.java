package uk.ac.ed.inf.aqmaps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.gson.JsonParser.parseString;

/*
JSONProcessor
    is used to transform sensors from JSON String to Mapbox geometry Points.
 */
public class JSONProcessor {
    private final Command command;
    private AddressHashMap addressMap;
    /*
    Constructor function initialize the addressMap to store locations and coordinates.
        This will be used in converting Sensor to Point.
    @param command: command is used to generate URL, where WebClient is used.
     */
    public JSONProcessor(Command command){
        this.command = command;
        String urlString = WebClient.buildURL(command.getDate());

        this.addressMap = new AddressHashMap(urlString);
    }

    /*
     *Set the color of each sensor.
     *@param battery: minimum 0.0 maximum 100.0, lower than 10.0 is unreliable
     *@param airquality: integer in range 0 to 255
     *@return RGBString: the color of a point.
     */
    private static String isColor(double battery, String reading){
        String RGBString = null;
        double airquality = Double.valueOf(reading);

        if(reading == "NaN" || battery < 10.0) {
            RGBString = "#000000";
        }else if(0.0 <= airquality && airquality < 32.0) {
            RGBString = "#00ff00";
        }else if(32.0 <= airquality && airquality <64.0) {
            RGBString = "#40ff00";
        }else if( 64.0 <= airquality && airquality <96.0) {
            RGBString = "#80ff00";
        }else if(96.0 <= airquality && airquality < 128.0) {
            RGBString = "#c0ff00";
        }else if(128.0 <= airquality && airquality < 160.0) {
            RGBString = "#ffc000";
        }else if(160.0 <= airquality && airquality < 192.0) {
            RGBString = "#ff8000";
        }else if(192.0 <= airquality && airquality < 224.0) {
            RGBString = "#ff4000";
        }else if(224.0 <= airquality && airquality <= 256.0) {
            RGBString = "#ff0000";
        }else {
            RGBString = "#aaaaaa";
        }
        return RGBString;
    }

    /*
    display the air qualities as symbol
    @param battery: minimum 0.0 maximum 100.0, lower than 10.0 is unreliable
    @param reading: air quality
    @return symbol: the symbol represents air quality.
     */
    public static String isSymbol(double battery, String reading) {
        String symbol = null;
        double airquality = Double.valueOf(reading);

        if (battery < 10.0){
            symbol = "cross";
        }else if (0.0 <= airquality && airquality < 128){
            symbol = "lighthouse";
        }else if(128.0 <= airquality && airquality <= 256){
            symbol = "danger";
        }else symbol = "";

        return symbol;
    }

    /*
    The following 2 method are together. toPoint2D converts com.mapbox.geojson.Point to java.awt.geom.Point2D, and
    another method toPoint converts Point2D to Point.
    @param Point/Point2D: the point is needed to be convert.
    @return Point2D/Point: the point successfully convert.
     */
    public static Point2D toPoint2D(Point point){
        Point2D point2D = new Point2D.Double(point.longitude(), point.latitude());
        return point2D;
    }
    public static Point toPoint(Point2D point2D){
        return Point.fromLngLat(point2D.getX(), point2D.getY());
    }

    /*
    request the local hashMap to convert location in What3Words to coordinates
    @param sensor: the sensor which to find coordite
    @return coordiate: the coordinate of sensor
     */
    public Point findPointFromSensor(Sensor sensor){
        Objects.requireNonNull(sensor, "Sensor object must not be null");

        var location = sensor.getLocation();
        String coor = addressMap.getCoordinate(location);
        var arr = coor.split(",");
        return Point.fromLngLat(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]));
    }

    /*
    The following 2 methods are together.
    toSensor converts JSON Object read from JSON file to an object of sensor.
    @param JsonObject: read from JSON file
    @return sensor: successfully convert JSONString to sensor.
    */
    public static Sensor toSensor(JsonObject jsonObject){
        String location = jsonObject.get("location").getAsString();
        Double battery = jsonObject.get("battery").getAsDouble();
        String reading = jsonObject.get("reading").getAsString();

        Sensor sensor = new Sensor(location, battery, reading);
        return sensor;
    }
    /*
    toSensorList request sensors from WebServer and add them to an ArrayList.
    @param urlString used to send request to WebsServer.
    @return sensors. All sensors are stored in an array list.
    */
    public static ArrayList<Sensor> toSensorList(String urlString){
        String httpResponse = WebClient.getResponse(urlString);

        JsonArray jsonArray = parseString(httpResponse).getAsJsonArray();

        ArrayList<Sensor> sensorList = new ArrayList<>();
        for(int i = 0; i < jsonArray.size(); i++){
            sensorList.add(toSensor((JsonObject) jsonArray.get(i)));
        }
        return sensorList;
    }

    /*
    Convert sensor to com.mapbox.geojson.Point
    @param sensor: an object of sensor
    @return Point: an geojson Point
     */

    public static Point toPoint(Sensor sensor){
        if (sensor == null){
            return null;
        }
        String what3Words = sensor.getLocation();

        String fileContent = WebClient.getResponse(WebClient.buildURL(what3Words));

        var jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();

        double lng = jsonObject.get("coordinates").getAsJsonObject().get("lng").getAsDouble();
        double lat = jsonObject.get("coordinates").getAsJsonObject().get("lat").getAsDouble();

        com.mapbox.geojson.Point point = Point.fromLngLat(lng,lat);
        return point;
    }
    /*
    Convert sensor to com.mapbox.geojson.Feature
        @param sensor: an object of sensor
    @return Feature: an geojson Feature
     */
    public Feature toPointFeature(Sensor sensor){
        String what3Words = sensor.getLocation();
        double battery = sensor.getBattery();
        String reading = sensor.getReading();
        Point point = findPointFromSensor(sensor);

        Feature pointFeature = Feature.fromGeometry(point);
        pointFeature.addStringProperty("marker-size","medium");
        pointFeature.addStringProperty("location",what3Words);
        pointFeature.addStringProperty("rgb-string",isColor(battery, reading));
        pointFeature.addStringProperty("marker-color",isColor(battery, reading));
        pointFeature.addStringProperty("marker-symbol",isSymbol(battery, reading));

        return pointFeature;
    }

    /*
    Convert many sensors to com.mapbox.geojson.FeatureCollection
    @param ArrayList<Sensors>: many objects of sensor
    @return FeatureCollection: an geojson FeatureCollection
     */
    public FeatureCollection toPointCollection(ArrayList<Sensor> sensors){
        List<Feature> featureList = new ArrayList<Feature>();
        for (int i = 0; i < sensors.size(); i++){
            featureList.add(toPointFeature(sensors.get(i)));
        }

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featureList);
        return featureCollection;
    }
}

