import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

/**
 * Converter class converts #define integers to xml Android resource files
 * first argument original headers file
 * second argument output file
 * third argument prefix if desired
 */

public class Converter
{
    public static void main(String[] args)
    {
        try {
            System.out.println(args[0] + " " + args[1]);

            String prefix = "";

            if(args.length > 2)
            {
                prefix = args[2] + "_";
            }

            PrintWriter writer = new PrintWriter(args[1], "UTF-8");
            writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            writer.println("<resources>");


            File file = new File(args[0]);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null)
            {
                String[] splitStr = st.split("\\s+");
                if(splitStr.length >= 3 && splitStr[0].equals("#define"))
                {
                    writer.println("    <integer name=\"" + prefix + splitStr[1] + "\">" + splitStr[2] + "</integer>");
                }
            }

            writer.println("</resources>");
            writer.close();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

