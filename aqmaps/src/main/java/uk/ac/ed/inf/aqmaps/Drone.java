package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.mapbox.turf.TurfJoins;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Drone {
    /*
    Drone
        can detect, move and read. A drone is used to get reading from sensors.
     */

    private final long seed;
    private final Point initialPoint;
    private static final int moveLimit = 150;
    private static final double range = 0.0002;
    private static final double flyUnit = 0.0003;


    private Point currentPoint;
    private int moveCount = 0;

    private final ArrayList<Sensor> sensorList;
    static AddressHashMap addressMap;
    private final JSONProcessor jsonProcessor;

    private final ArrayList<Sensor> visitedSensors;
    private final ArrayList<String> readingRecord;
    private ArrayList<Sensor> notVisitedSensors;
    private ArrayList<String> locations;
    private final ArrayList<Point> flyPath;
    private ArrayList<Integer> directionRecord;

    //noFlyZone and Boundaries
    private static final FeatureCollection noFly = FeatureCollection.fromJson(WebClient.getResponse(WebClient.buildURL("noFly")));
    private static final List<Point> AT = Polygon.fromJson(noFly.features().get(0).geometry().toJson()).coordinates().get(0);
    private static final List<Point> DHT = Polygon.fromJson(noFly.features().get(1).geometry().toJson()).coordinates().get(0);
    private static final List<Point> ML = Polygon.fromJson(noFly.features().get(2).geometry().toJson()).coordinates().get(0);
    private static final List<Point> IF = Polygon.fromJson(noFly.features().get(3).geometry().toJson()).coordinates().get(0);
    private static final Point pointLeftUp = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point pointRightUp = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point pointRightDown = Point.fromLngLat(-3.184319, 55.942617);
    private static final Point pointLeftDown = Point.fromLngLat(-3.192473, 55.942617);

    private String[] outputCount;
    private String[] outputStart;
    private String[] outputDirection;
    private String[] outputEnd;
    private String[] outputReading;

    /*
    Constructor function
    @param command: drone receive the command and download neccessary data from local server.
     */

    public Drone(Command command) {
        Point location = command.getInitialPoint();
        seed = command.getSeed();
        String urlString = WebClient.buildURL(command.getDate());

        addressMap = new AddressHashMap(urlString);

        jsonProcessor = new JSONProcessor(command);

        this.initialPoint = location;
        this.currentPoint = location;

        //initialize overall sensorList
        this.sensorList = JSONProcessor.toSensorList(urlString);

        //initialize visitedSensors
        this.visitedSensors = new ArrayList<>();

        //initialize readingRecord
        this.readingRecord = new ArrayList<>();

        //initialize flyPath
        this.flyPath = new ArrayList<>();
        flyPath.add(location);

        //initialize notVisitedSensors
        notVisitedSensors = sensorList;

        outputCount = new String[moveLimit + 1];
        outputStart = new String[moveLimit+ 1];
        outputDirection = new String[moveLimit+ 1];
        outputEnd = new String[moveLimit+ 1];
        outputReading = new String[moveLimit+ 1];
    }

    public long getSeed() {
        return seed;
    }

    public Point getInitialPoint() {
        return initialPoint;
    }
    public int getMoveLimit() {
        return moveLimit;
    }
    public double getRange() {
        return range;
    }
    public double getFlyUnit() {
        return flyUnit;
    }

    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
    }
    public Point getCurrentPoint() {
        return currentPoint;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public AddressHashMap getAddressMap() {
        return addressMap;
    }

    public ArrayList<Sensor> getSensorList() {
        return sensorList;
    }

    public void appendVisitedSensors(Sensor sensor) {
        this.visitedSensors.add(sensor);
    }
    public Sensor[] getVisitedSensors() {
        return visitedSensors.toArray(new Sensor[visitedSensors.size()]);
    }

    public ArrayList<String> getLocations() {
        for (int i = 0; i < visitedSensors.size(); i++){
            locations.add(getVisitedSensors()[i].getLocation());
        }
        return locations;
    }

    public void appendReadingRecord(String reading) {
        this.readingRecord.add(reading);
    }
    public ArrayList<String> getReadingRecord() {
        return readingRecord;
    }

    public ArrayList<Point> getFlyPath() {
        return flyPath;
    }
    public void appendFlyPath(Point current) {
        this.flyPath.add(current);
    }

    public void setNotVisitedSensors(ArrayList<Sensor> notVisitedSensors) {
        this.notVisitedSensors = notVisitedSensors;
    }

    public void deleteNotVisitedSensors(Sensor sensor){
        this.notVisitedSensors.remove(notVisitedSensors.indexOf(sensor));
    }
    public Sensor[] getNotVisitedSensors() {
        return notVisitedSensors.toArray(new Sensor[notVisitedSensors.size()]);
    }

    public ArrayList<Point> getNotVisitedPoints(){
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < this.notVisitedSensors.size(); i++){
            points.add(jsonProcessor.findPointFromSensor(this.notVisitedSensors.get(i)));
        }
        return points;
    }

    public List<Point> getAT() {
        return AT;
    }

    public List<Point> getDHT() {
        return DHT;
    }

    public List<Point> getIF() {
        return IF;
    }

    public List<Point> getML() {
        return ML;
    }

    /*
    get all No Fly buildings
     */
    public List<Point> getAllBuildings(){
        ArrayList<Point> overall = new ArrayList<>();
        overall.addAll(this.AT);
        overall.addAll(DHT);
        overall.addAll(IF);
        overall.addAll(ML);
        return overall;
    }

    public void appendOutputCount(int outputCount, int i) {
        this.outputCount[i] = String.valueOf(outputCount);
    }

    public void appendOutputStart(Point outputStart, int i) {
        String str = outputStart.longitude() + "," + outputStart.latitude();
        this.outputStart[i] = (str);
    }

    public void appendOutputDirection(int outputDirection, int i) {
        this.outputDirection[i] = (String.valueOf(outputDirection));
    }

    public void appendOutputEnd(Point outputEnd, int i) {
        String str = outputEnd.longitude() + "," + outputEnd.latitude();
        this.outputEnd[i] = (str);
    }

    public void appendOutputReading(String outputReading, int i) {
        this.outputReading[i] = (outputReading);
    }

    /*
                        check whether the given sensor is in the detect range of drone
                        @param pointSensor: the location of sensor
                        @return true - the sensor is in detect region, that is, can be detected
                         */
    public boolean isInRange(Point pointSensor){
        var distance = RoutePlanner.calEucDistance(this.currentPoint, pointSensor);
        return distance <= this.range;
    }

    /*
    check whether the given point is in NoFlyRegion
    @param point to be checked
    @return true - YES, In No Fly Region and this is not expected
     */
    public static boolean checkInRegion(Point point){
        List<Point> pointList = new ArrayList<Point>();
        pointList.add(pointLeftUp);
        pointList.add(pointRightUp);
        pointList.add(pointRightDown);
        pointList.add(pointLeftDown);
        pointList.add(pointLeftUp);

        List<Point> boundariesL = pointList;

        //expected false
        Polygon AT = Polygon.fromJson(noFly.features().get(0).geometry().toJson());
        Polygon DHT = Polygon.fromJson(noFly.features().get(1).geometry().toJson());
        Polygon ML = Polygon.fromJson(noFly.features().get(2).geometry().toJson());
        Polygon IF = Polygon.fromJson(noFly.features().get(3).geometry().toJson());
        Polygon boundaries = Polygon.fromLngLats(Collections.singletonList(boundariesL));

        boolean check = (TurfJoins.inside(point, AT)
                || TurfJoins.inside(point, DHT)
                || TurfJoins.inside(point, ML)
                || TurfJoins.inside(point, IF))
                || !TurfJoins.inside(point, boundaries);
        return check;
    }

    /*
    for a given list of points, check whether the line between current location of drone and the predict end point
    intersects with polygons
    @param pointList: NoFlyZone as polygons
    @param point: the predict point
    @return true - YES, In No Fly Region and this is not expected
     */
    public static boolean interSLine2D(List<Point> pointList, Point currentPoint, Point point5){
        double x3 = currentPoint.longitude();
        double y3 = currentPoint.latitude();

        double x5 = point5.longitude();
        double y5 = point5.latitude();

        for (int i = 0; i < pointList.size(); i++) {
            int j = i + 1 >= pointList.size() ? 0 : (i + 1);

            Point p1 = pointList.get(i);
            Point p2 = pointList.get(j);

            double x1 = p1.longitude();
            double y1 = p1.latitude();
            double x2 = p2.longitude();
            double y2 = p2.latitude();

            Line2D line1 = new Line2D.Double(x3, y3, x5, y5);
            Line2D line2 = new Line2D.Double(x1, y1, x2, y2);

            if (line1.intersectsLine(line2)){
                //System.out.println(JSONProcessor.toPoint(line1.getP1()).toJson());
                //System.out.println(JSONProcessor.toPoint(line1.getP2()).toJson());
                //System.out.println(JSONProcessor.toPoint(line2.getP1()).toJson());
                //System.out.println(JSONProcessor.toPoint(line2.getP2()).toJson());
                return true;
            }
        }

        return false;
    }

    /*
    check whether the given line2D intersect with the NoFly Region
    @param currentPoint: the start of the line
    @param point5: the end of the line
    @return true - YES, In No Fly Region and this is not expected
     */
    public static boolean checkWithLine2D(Point currentPoint, Point point5){
        List<Point> pointList = new ArrayList<Point>();
        pointList.add(pointLeftUp);
        pointList.add(pointRightUp);
        pointList.add(pointRightDown);
        pointList.add(pointLeftDown);
        pointList.add(pointLeftUp);

        List<Point> boundariesL = pointList;
        //expected false
        boolean check = false;
        check = (interSLine2D(AT, currentPoint, point5)
                || interSLine2D(DHT, currentPoint, point5)
                || interSLine2D(IF, currentPoint, point5)
                || interSLine2D(ML, currentPoint, point5))
                || interSLine2D(boundariesL, currentPoint, point5);
        return check;
    }

    public static boolean checkBoundaries(Point currentPoint, Point point5){
        List<Point> pointList = new ArrayList<Point>();
        pointList.add(pointLeftUp);
        pointList.add(pointRightUp);
        pointList.add(pointRightDown);
        pointList.add(pointLeftDown);
        pointList.add(pointLeftUp);

        List<Point> boundariesL = pointList;
        return interSLine2D(boundariesL, currentPoint, point5);
    }

    /*
    predict the destination if move in direction
    @param direction: predict along this direction.
     */
    public Point predictPoint(int direction){
        var radians = Math.toRadians(direction);
        double unit = this.flyUnit;

        double lng = currentPoint.longitude() + unit * (Math.cos(radians));
        double lat = currentPoint.latitude() + unit * (Math.sin(radians));

        Point predictPoint = Point.fromLngLat(lng, lat);

        return predictPoint;
    }

    public static Point predictPoint(Point currentPoint, int direction){
        var radians = Math.toRadians(direction);
        double unit = flyUnit;

        double lng = currentPoint.longitude() + unit * (Math.cos(radians));
        double lat = currentPoint.latitude() + unit * (Math.sin(radians));

        Point predictPoint = Point.fromLngLat(lng, lat);

        return predictPoint;
    }

    public String getCoorFromPath(Point point){
        String str = point.longitude() + "," + point.latitude();
        return str;
    }

    /*
    drone moves in direction and add record to flypath
    @param direction drone moves in this direction
     */
    public void move(int direction){
        if (moveCount < moveLimit){
            Point predict = predictPoint(this.currentPoint, direction);
            appendOutputStart(currentPoint, moveCount);
            setCurrentPoint(predict);
            appendFlyPath(this.currentPoint);

            this.moveCount ++;
            appendOutputCount(moveCount, moveCount);
            appendOutputDirection(direction, moveCount);
            appendOutputEnd(predict, moveCount);
        }else ;
    }

    /*
    drone take readings from sensor;
    @param sensor: the one to be read.
     */
    public void read(Sensor sensor){
        if (sensor == null){
            appendReadingRecord("null");

            appendOutputReading("null", moveCount);
        }else{
            appendVisitedSensors(sensor);
            deleteNotVisitedSensors(sensor);
            appendReadingRecord(sensor.getReading());

            appendOutputReading(sensor.getLocation(), moveCount);
        }


    }

    public String generateOutPut(int i){
        String str = outputCount[i] + ","
            + outputStart[i - 1] + ","
            + outputDirection[i] + ","
            + outputEnd[i] + ","
            + outputReading[i];
        return str;
    }
}

