package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;
import org.apache.commons.lang3.StringUtils;

import java.time.Month;
import java.util.Objects;

/*
Command
    A String input from terminal.
    This class will decode the input String to 4 properties: date, initialPoint, seed and port.
    Creating an object will pass the properties to other classes(i.e. *Processor).
    No setter for this class, all properties are set while creating an object.
 */

public class Command {
    //local variables can only be initialized
    private final String date;
    private final String initialPoint;
    private final String seed;
    private final String port;
    private final String commandStr;
    private final String day;
    private final String month;
    private final String year;

    /*
    Constructor funtion
        decode input String and initialize properties: date, initialPoint, seed and port.
     */
    public Command(String commandStr){
        if (commandStr == null){
            System.out.println("Null command is not accepted.");
        }

        String[] commandArray = commandStr.split(" ");

        if (commandArray.length != 7) {
            throw new IllegalArgumentException("Command must be in correct format:" +
                    "'DD MM YYYY Latitude Longitude Seed Port'");
        }
        if (!commandArray[0].matches("\\d\\d+")){
            System.out.println("Day format is wrong");
        }else if (!commandArray[1].matches("\\d\\d+")){
            System.out.println("Month format is wrong");
        }else if (!commandArray[2].matches("\\d\\d\\d\\d+")){
            System.out.println("Year format is wrong");
        }else if (!commandArray[3].matches("\\d+.\\d+")){
            System.out.println("Longitude format is wrong");
        }else if (!commandArray[4].matches("\\D+\\d+.\\d+")){
            System.out.println("Latitude format is wrong");
        }else if (!commandArray[5].matches("\\d+")){
            System.out.println("Latitude format is wrong");
        }else if (!commandArray[6].matches("\\d+")) {
            System.out.println("Latitude format is wrong");
        }
        day = commandArray[0];
        month = commandArray[1];
        year = commandArray[2];

        date = commandArray[0] + " " + commandArray[1] + " " + commandArray[2];
        initialPoint = commandArray[3] + " " + commandArray[4];
        seed = commandArray[5];
        port = commandArray[6];

        this.commandStr = commandStr;

        WebClient.setPort(getPort());
    }

	/*
    reverse an array
    @param array: input array to be reversed.
    @return arrayReverse: reversed array.
     */
    private static String[] reserve(String[] array){
        String[] arrayReverse = new String[array.length];
        for( int i = 0; i < array.length ; i++ ){
            arrayReverse[i] = array[array.length-i-1];
        }
        return arrayReverse ;
    }

    /*
    Getters
        are used to get properties of command.
     */

    /*
    Get date
    @return newDate: date is reconstrcted in the format of Year/Month/Day.
     */
    public String getDate() {
        String[] dateStrArr = this.date.split(" ");
        String newDate = StringUtils.join(reserve(dateStrArr), "/");
        return newDate;
    }

    /*
    Get Initial Point
    @return point: the initial position of drone. returned as com.mapbox.geojson.Point
     */
    public Point getInitialPoint() {
        String[] strArr = this.initialPoint.split(" ");
        double lng= Double.parseDouble(strArr[1]);
        double lat = Double.parseDouble(strArr[0]);

        return Point.fromLngLat(lng, lat);
    }

    /*
    @return seed: used to generate random number.
     */
    public int getSeed() {
        int seedInt = Integer.valueOf(this.seed);
        return seedInt;
    }

    /*
    @return port: the port that used to visit local server.
     */
    public int getPort() {
        int portInt = Integer.valueOf(this.port);
        return portInt;
    }

    /*
    @return string consists of date, seed, and port. This String is uesd in FileBuilder.
     */
    public String getOther(){
        return this.initialPoint + " " + this.getSeed() + " " + this.getPort();
    }

    /*
    @return commandStr: the copy of original input String.
     */
    public String getCommandStr() {
        return commandStr;
    }

    public String generateFileDate(){
        return day + "-" + month + "-" + year;
    }

}

