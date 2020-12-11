import Codegen.SymbolTable;

import java.io.*;

public class Compiler {
    public static void main(String[] args) {

        InputStream is = null;
        OutputStream out = null;
        try {
            is = new FileInputStream(args[0]);
            out = new FileOutputStream(args[1]);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }


        // pass .npt file here
        Parser parser = ParserInitializer.createParser("PT_new_43.npt", is, out);

        if (parser == null) {
            System.err.println("Parser Initializing Error -> Parser not initialized");
            return;
        }

        try
        {
            parser.parse(); // parse all the input
        }
        catch (Exception ex)
        {
            System.err.println("Compile Error -> " + ex.getMessage());
        }

        //parser.WriteOutput(); // used for a full compiler
    }
}
