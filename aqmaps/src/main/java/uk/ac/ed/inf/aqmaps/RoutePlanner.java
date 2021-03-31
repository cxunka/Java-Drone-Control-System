package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.mapbox.turf.TurfClassification;
import com.mapbox.turf.TurfJoins;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RoutePlanner extends Drone {
    private  JSONProcessor jsonProcessor;
    private ArrayList<Point> array1;

    FeatureCollection noFly = FeatureCollection.fromJson(WebClient.getResponse(WebClient.buildURL("noFly")));
    Polygon AT = Polygon.fromJson(noFly.features().get(0).geometry().toJson());
    Polygon DHT = Polygon.fromJson(noFly.features().get(1).geometry().toJson());
    Polygon ML = Polygon.fromJson(noFly.features().get(2).geometry().toJson());
    Polygon IF = Polygon.fromJson(noFly.features().get(3).geometry().toJson());

    static Point pointLeftUp = Point.fromLngLat(-3.192473, 55.946233);
    static Point pointRightUp = Point.fromLngLat(-3.184319, 55.946233);
    static Point pointRightDown = Point.fromLngLat(-3.184319, 55.942617);
    static Point pointLeftDown = Point.fromLngLat(-3.192473, 55.942617);

    static Point pointUpMid = Point.fromLngLat(-3.1885, 55.946233);
    static Point pointDownMid = Point.fromLngLat(-3.1866, 55.942617);

    private static Polygon region1 = Polygon.fromJson("{\"type\":\"Polygon\",\"coordinates\":[[[-3.192473,55.946233],[-3.1885,55.946233],[-3.1866,55.942617],[-3.192473,55.942617],[-3.192473,55.946233]]]}");

    private static Polygon region2 = Polygon.fromJson("{\"type\":\"Polygon\",\"coordinates\":[[[-3.1885,55.946233],[-3.184319,55.946233],[-3.184319,55.942617],[-3.1866,55.942617],[-3.1885,55.946233]]]}");

    public RoutePlanner(Command command){
        super(command);
        jsonProcessor = new JSONProcessor(command);
        findCenters();
    }

    public static Polygon getRegion1() {
        return region1;
    }

    public static Polygon getRegion2() {
        return region2;
    }

    public static double calEucDistance(Point start, Point end) {
        return Math.sqrt(Math.pow(start.latitude()-end.latitude(),2)
                + Math.pow(start.longitude()-end.longitude(),2));
    }

    public static int getMinIndex(Double[] array) {
        int minIndex = 0;
        for(int i = 0; i < array.length - 1; i++){
            if(array[minIndex] > array[i+1]){
                minIndex = i + 1;
            }
        }
        return minIndex;
    }

    public static int getMaxIndex(Double[] array) {
        int maxIndex = 0;
        for(int i = 0; i < array.length - 1; i++){
            if(array[maxIndex] < array[i+1]){
                maxIndex = i + 1;
            }
        }
        return maxIndex;
    }

    public Point findClosestSensor(){
        return TurfClassification.nearestPoint(getCurrentPoint(),
                getNotVisitedPoints());
    }

    public static int limitDirection(double direction){

        direction = direction % 360;

        direction = (int) (Math.round(direction/10)*10);
        return (int) direction;
    }

    public static double findAccurateDirection(Point start, Point end){
        double direction = 0;

        var x = Math.abs(start.longitude() - end.longitude());
        var y = Math.abs(start.latitude() - end.latitude());
        var z = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));

        var cosValueX = (Math.pow(x,2) + Math.pow(z,2) - Math.pow(y,2)) / (2 * x * z);
        var degree = Math.toDegrees(Math.acos(cosValueX));

        direction = degree;

        if (end.longitude() > start.longitude() && end.latitude() > start.latitude()){
            direction = direction;
        }else if (end.longitude() < start.longitude() && end.latitude() > start.latitude()){
            direction = 180 - direction;
        }else if (end.longitude() < start.longitude() && end.latitude() < start.latitude()){
            direction = 180 + direction;
        }else if (end.longitude() > start.longitude() && end.latitude() < start.latitude()){
            direction = 360 - direction;
        }

        return direction;
    }

    public static int findDirection(Point start, Point end){
        double degree = findAccurateDirection(start, end);

        int direction = limitDirection(degree);

        return direction;
    }

    public FeatureCollection generatePolygons(){
        int num = 20;
        int size = num * num;
        ArrayList<Feature> polygons = new ArrayList<>();

        String strLatUp 	=	"55.946233";
        String strLatDown	=	"55.942617";
        String strLngLeft	=	"3.192473";
        String strLngRight =	"3.184319";
        double latUp = Double.parseDouble(strLatUp);
        double latDown = Double.parseDouble(strLatDown);
        double lngLeft = 0 - Double.parseDouble(strLngLeft);
        double lngRight = 0 - Double.parseDouble(strLngRight);

        double latUnit = (latUp - latDown)/num;
        double lngUnit = (lngRight - lngLeft)/num;

        double[] latUpArray = new double[size];
        double[] latDownArray = new double[size];
        double[] lngLeftArray = new double[size];
        double[] lngRightArray = new double[size];

        for (int j = 0; j < num; j++) {
            double latEachUp = latUp - j * latUnit;
            double latEachDown = latEachUp - 1 * latUnit;

            for (int i = 0; i < num; i++) {
                double lngEachLeft = lngLeft + i * lngUnit;
                double lngEachRight = lngEachLeft + 1 * lngUnit;

                latUpArray[num*j + i] = latEachUp;
                latDownArray[num*j + i] = latEachDown;
                lngLeftArray[num*j + i] = lngEachLeft;
                lngRightArray[num*j + i] = lngEachRight;
            }
        }

        for (int i = 0; i < size; i++){
            ArrayList <Point> points = new ArrayList<>();
            points.add(Point.fromLngLat(lngLeftArray[i], latUpArray[i]));
            points.add(Point.fromLngLat(lngRightArray[i], latUpArray[i]));
            points.add(Point.fromLngLat(lngRightArray[i], latDownArray[i]));
            points.add(Point.fromLngLat(lngLeftArray[i], latDownArray[i]));
            points.add(Point.fromLngLat(lngLeftArray[i], latUpArray[i]));
            polygons.add(Feature.fromGeometry(Polygon.fromLngLats(Collections.singletonList(points))));
        }

        return FeatureCollection.fromFeatures(polygons);
    }

    public List<Point> findCenters(){
        ArrayList<Feature> array = (ArrayList<Feature>) generatePolygons().features();
        ArrayList<Point> array2 = new ArrayList<>();

        for (int i = 0; i < array.size(); i++){
            array.get(i).addNumberProperty("num", TurfJoins.pointsWithinPolygon(
                    jsonProcessor.toPointCollection(getSensorList()),
                    FeatureCollection.fromFeature(array.get(i))).features().size());
            var center = (Point) TurfMeasurement.center(array.get(i)).geometry();
            if (TurfJoins.inside(center, AT) || TurfJoins.inside(center, DHT)
                    || TurfJoins.inside(center, ML) || TurfJoins.inside(center, IF)){
                array.remove(i);
            }else array2.add(center);
        }
        array1 = array2;

        //System.out.println(array1.size() + " findCenter");

        return array2;
    }

    public ArrayList<Point> generateAvailable(){
        int coefficient = 5;
        var points = new ArrayList<Point>();

        for (int i = 0; i < array1.size(); i++){
            if (checkInRegion(array1.get(i))) {

            }else if (checkWithLine2D(getCurrentPoint(),array1.get(i))){

            }else if(calEucDistance(getCurrentPoint(), array1.get(i)) > coefficient * getFlyUnit()
                    || calEucDistance(getCurrentPoint(), array1.get(i)) < getFlyUnit()){

            }else points.add(array1.get(i));
        }

        return points;
    }

    public Point nearestPoint(ArrayList<Point> tryArray, Point closestSensor){
        if (tryArray.size() == 0){
            Random random = new Random(getSeed());
            return null;
        }

        Double[] distance = new Double[tryArray.size()];
        for (int i = 0; i < tryArray.size(); i++){
            distance[i] = Math.abs(calEucDistance(closestSensor,
                    tryArray.get(i)));
        }

        return tryArray.get(getMinIndex(distance));
    }

    public Point corner(Point current){
        ArrayList<Point> points = new ArrayList<>();
        points.addAll(getAllBuildings());

        var corner = TurfClassification.nearestPoint(getCurrentPoint(),points);

        return corner;
    }

    public int decideNextDirection(){
        Point minPoint;

        var standard = findClosestSensor();
        int directionToS = findDirection(getCurrentPoint(), standard);
        Point predictionS = predictPoint(directionToS);
        //ArrayList<Point> list1 = new ArrayList<>();
        //list1.add(standard);
        //list1.add(drone.getCurrentPoint());
        //list1.add(predictionS);
        //System.out.println(drone.checkWithLine2D(drone.getCurrentPoint(),standard));
        //System.out.println(LineString.fromLngLats(list1).toJson());
        //System.out.println(drone.getCurrentPoint().toJson());

        while(true){
            ArrayList<Point> centerList = new ArrayList<>();
            ArrayList<Point> badList = new ArrayList<>();
            centerList = generateAvailable();

            //Mark wall
            centerList.removeAll(badList);
            //System.out.println(centerList);

            //System.out.println(MultiPoint.fromLngLats(centerList).toJson());
            if (centerList.size() == 0){
                //System.out.println(drone.getCurrentPoint().toJson());
                for (int i = 0; i < 8; i++){
                    var direction = limitDirection(i * 45);
                    //System.out.println(direction);
                    if (!checkWithLine2D(getCurrentPoint(), predictPoint(direction))
                        && !getFlyPath().contains(predictPoint(direction))){
                        return direction;
                    }
                }
            }

            minPoint = nearestPoint(centerList, standard);
            int directionToC = findDirection(getCurrentPoint(), minPoint);
            double degreeToC = findAccurateDirection(getCurrentPoint(), minPoint);
            Point predictionC = predictPoint(getCurrentPoint(), directionToC);

            ArrayList<Point> list = new ArrayList<>();
            list.add(getCurrentPoint());
            list.add(minPoint);
            list.add(predictionC);
            //System.out.println(LineString.fromLngLats(list).toJson());

            if (getFlyPath().contains(predictionC)){
                badList.add(minPoint);
            }

            if (!checkWithLine2D(getCurrentPoint(), predictionC)
                    && calEucDistance(getCurrentPoint(), predictionC) > getFlyUnit()) {
                return directionToC;
            }else if (!checkWithLine2D(getCurrentPoint(), predictionC)
                    && checkWithLine2D(getCurrentPoint(), minPoint)){
                if (degreeToC < directionToC){
                    directionToC = directionToC - 10;
                    return directionToC;
                }else if (degreeToC > directionToC){
                    directionToC = directionToC + 10;
                    return directionToC;
                }
            }else {
                for (int i = 0; i < 8; i++) {
                    var direction = limitDirection(i * 45);
                    if (!checkWithLine2D(getCurrentPoint(),predictPoint(direction))
                            && !getFlyPath().contains(predictPoint(direction))) {
                        return direction;
                    }
                }
            }
        }
    }



    public Sensor getInRangeSensor(){
        ArrayList<Sensor> sensors = getSensorList();
        ArrayList<Sensor> arrInRange = new ArrayList<>();
        for (int i = 0; i < sensors.size(); i++){
            if (isInRange(jsonProcessor.findPointFromSensor(sensors.get(i)))){
                arrInRange.add(sensors.get(i));
            }
        }

        if (arrInRange.size() != 0){
            ArrayList<Double> distance = new ArrayList<Double>();
            for (int i = 0; i < arrInRange.size(); i++){
                distance.add(calEucDistance(getCurrentPoint(), jsonProcessor.findPointFromSensor(arrInRange.get(i))));
            }

            Double min = distance.get(0);

            for(int i=0;i< distance.size() - 1;i++){
                if(min> distance.get(i)){
                    min= distance.get(i);
                }
            }

            return arrInRange.get(distance.indexOf(min));
        } else return null;
    }

    public ArrayList<Point> getSensorsPoints(ArrayList<Sensor> sensors){
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < sensors.size(); i++){
            points.add(jsonProcessor.findPointFromSensor(sensors.get(i)));
        }
        return points;
    }




    public List<Feature> planAStar(){
        run();
        var route = getFlyPath();
        var lineStr = LineString.fromLngLats(route);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(
                Feature.fromGeometry(lineStr));

        return featureCollection.features();
    }

    public void run(){
        String jsonStr = null;
        double precision = 0.0003;

        for (int i = 0; i < getMoveLimit(); i++){
            if (getNotVisitedSensors().length == 0){
                break;
            }else{
                if (getCurrentPoint() == null){
                    setCurrentPoint(getInitialPoint());
                }
                var closestPoint = TurfClassification.nearestPoint(getCurrentPoint(),
                        getNotVisitedPoints());
                var directionToS = findDirection(getCurrentPoint(),
                        closestPoint);

                var predict = predictPoint(getCurrentPoint(), directionToS);

                int directionToC = 0;

                if (checkWithLine2D(getCurrentPoint(),predict)){
                    directionToC = decideNextDirection();
                    move(directionToC);
                    read(getInRangeSensor());
                }else {
                    move(directionToS);
                    read(getInRangeSensor());
                }
            }
        }

        for (int i = getMoveCount(); i < getMoveLimit(); i++){
            var directionToS = findDirection(getCurrentPoint(),
                    getInitialPoint());

            var predict = predictPoint(getCurrentPoint(), directionToS);

            int directionToC = 0;

            if (checkWithLine2D(getCurrentPoint(),predict)){
                directionToC = decideNextDirection();
                move(directionToC);
                read(getInRangeSensor());
            }else {
                move(directionToS);
                read(getInRangeSensor());
            }

            if (calEucDistance(getCurrentPoint(), getInitialPoint()) < precision){
                break;
            }
        }
    }

}
