package uk.ac.ed.inf.aqmaps;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	String a = args[0] + " "+ args[1] + " "+ args[2] + " "+ args[3] + " "+ args[4] + " "+ args[5]+ " " + args[6];
    	System.out.println(a);
    	Command command = new Command(a);
        FileProcessor.buildSinglePath(command);
        FileProcessor.buildSingleReading(command);
    }
}
