package uk.ac.ed.inf.aqmaps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileProcessor {
    private FileProcessor(){
        throw new UnsupportedOperationException("This constructor should never be used.");
    }


    public static void buildSinglePath(Command command){
        String flightPathName = "flightpath-" + command.generateFileDate() + ".txt";;

        String fileName = flightPathName;
        System.out.println(fileName);
        RoutePlanner routePlanner = new RoutePlanner(command);
        routePlanner.run();
        var lf = routePlanner.planAStar();
        System.out.println(routePlanner.getVisitedSensors().length + "," + routePlanner.getMoveCount());
        File file = new File(fileName);

        try {
            FileWriter writer = new FileWriter(file);
            for (int j = 1; j <= routePlanner.getMoveCount(); j++){
                String str = routePlanner.generateOutPut(j);
                writer.write(str);
                writer.write("\r\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildSingleReading(Command command){
        String defaultName =  "readings-" + command.generateFileDate() + ".geojson";;

        String fileName = defaultName;
        JSONMap jsonMap = new JSONMap(command);
        System.out.println(defaultName);
        String jsonStr = JSONMap.toJSONStr(JSONMapBuilder.buildMap(command));
        File file = new File(fileName);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(jsonStr);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

