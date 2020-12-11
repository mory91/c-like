import java.io.OutputStream;
import java.util.Stack;

public class CodeGenerator
{
    Scanner scanner; // This one way of informing CG about tokens detected by Scanner, you can do whatever you prefer


    // Define any variables needed for code generation

    private static final boolean calcExample = true;    // enable Calculator example
    /// *** BEGIN EXAMPLE FOR CALCULATOR ***
        private Stack<Integer> vals = new Stack<Integer>();
        private Stack<String> ops = new Stack<String>();
    /// *** END EXAMPLE FOR CALCULATOR ***

    public CodeGenerator(Scanner scanner)
    {
        this.scanner = scanner;

    }


    public void doSemantic(String sem)
    {
    	// System.out.println(sem + " " + scanner.getText());    // used for debug


        /// *** BEGIN EXAMPLE FOR CALCULATOR ***
        if (calcExample) {
            switch (sem) {
                // DO NOT FORGET TO break AT THE END OF EACH case !!!
                case "NoSem":
                    return;
                case "@push":
                    ops.add(scanner.getToken());
                    break;
                case "@pushi":
                    vals.add(Integer.valueOf(scanner.getText()));
                    break;
                case "@accept":
                    System.out.println(vals.pop());
                    break;
                case "@cal":
                    int a, b;
                    b = vals.pop();
                    a = vals.pop();
                    String op = ops.pop();
                    int r = 0;
                    switch (op) {
                        case "+":
                            r = a + b;
                            break;
                        case "-":
                            r = a - b;
                            break;
                        case "*":
                            r = a * b;
                            break;
                        case "/":
                            r = a / b;
                            break;
                    }
                    vals.push(r);
                    break;
                case "@eval":
                    System.out.println("= " + vals.pop());
                    break;
                default:
                    System.err.println("Code Generation Error -> Unknown semantic function " + sem);
            }
        }
        /// *** END EXAMPLE FOR CALCULATOR ***
    }

    /**
     * It is called after parsing is done without errors
     */
    void FinishCode() // You may need this, or may not
    {

    }

    /**
     * Used to write any needed output after the parsing is done.
     */
    public void WriteOutput(OutputStream os)
    {
        // This is called after the parsing is done.
    	// Can be used to print the generated code to output
    	// This is used because in the process of compiling,the generated code is stored in some sort of structure
    	// You can output code lines just when they are generated,
        // but this not recommended due to possible further errors, controls structures of program that change the order of generated code according to source code, ...
    }
}
