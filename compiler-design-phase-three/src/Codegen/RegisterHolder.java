package Codegen;

/**
 * Created by root on 7/8/17.
 */
public class RegisterHolder {
    private String name;
    private int start;
    private String val;

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }

    public String getVal() {
        return val;
    }

    public RegisterHolder(String name, int start, String val) {

        this.name = name;
        this.start = start;
        this.val = val;
    }
}
