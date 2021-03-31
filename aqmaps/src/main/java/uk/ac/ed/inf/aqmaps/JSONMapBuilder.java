package uk.ac.ed.inf.aqmaps;

public class JSONMapBuilder {
    private JSONMapBuilder(){
    }

    public static JSONMap buildMap(Command command){
        JSONMap map = new JSONMap(command);

        map.setSensors();
        map.setPath();

        return map;
    }
}