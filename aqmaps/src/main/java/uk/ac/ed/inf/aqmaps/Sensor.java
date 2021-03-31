package uk.ac.ed.inf.aqmaps;

/*
Sensor
    A sensor cannot be null and must have 3 properties: location, battery, reading.
    No setters for this class, all properties are set while creating an object.
 */

import java.util.Objects;

public class Sensor {
    //local variables can only be initialized.
    private final String location;
    private final double battery;
    private final String reading;

    /*
    Constructor function of Sensor. Create an object of Sensor.
    @param location: stored as What3Words, can be converted to a unique coordinate
    @param battery: minimum 0.0, maximum 100.0. Any sensor with battery < 10.0 is unreliable and needs to be changed.
    @param reading: minimum 0.0, maximum 256.0. Can be "null" or "NaN".
     */
    public Sensor(String location,
                  double battery,
                  String reading){
        Objects.requireNonNull(location, "A sensor must have a location.");
        Objects.requireNonNull(battery, "A sensor must have a battery.");
        Objects.requireNonNull(reading, "A sensor must have a reading.");

        this.location = location;

        if(0.0 <= battery && battery <= 100.0){
            this.battery = battery;
        }else{
            this.battery = 0.0;
        };

        if(battery < 10.0){
            this.reading = "NaN";
            //System.out.println("Sensor at location:" + getLocation() + " needs battery");
        }else {
            this.reading = reading;
        }
    }

    /*
    Getters
        are used to get the value of a special property of a sensor.
     */

    //@return location as String, in the format of What3Words.
    public String getLocation() {
        return location;
    }

    //@return battery as double.
    public double getBattery() {
        return battery;
    }

    //@return reading as String, can be "null" or "NaN".
    public String getReading() {
        return reading;
    }
}
