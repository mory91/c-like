import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by root on 2/26/17.
 */
public class Tester {
    public static void main(String[] args) {
        try {
            FileReader rd = new FileReader(args[0]);
            FileWriter wr;
            try {
                wr = new FileWriter(args[1]);

                Scanner scanner = new Scanner(rd, wr);
                try {
                    String id = scanner.yylex();
                    while(!id.equals("68")) {
                        id = scanner.yylex();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
