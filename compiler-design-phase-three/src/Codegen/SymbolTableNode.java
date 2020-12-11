package Codegen;

import javafx.util.Pair;

import java.util.*;

/**
 * Created by root on 6/20/17.
 */
public class SymbolTableNode {
    public IdType type;
    public String varType ;
    public int address;
    public boolean isDynamiclyAllocated = false ;
    public boolean isConst = false ;
    public MemType addressingType = MemType.STACK ;
    public String name;
    public int size;
    public int cellSize ;
    public int intValue ;
    public String stringValue ;
    public float floatValue ;
    public double doubleValue ;
    public MemType memType;
    public ArrayList<Integer> dimension = new ArrayList<Integer>() ;
    public ArrayList<Parameter> arguments = new ArrayList<>() ;
	public int argSize = 0 ;
    public ArrayList<SymbolTableNode> overloads = new ArrayList<>() ;
    public int lastCharPlace = 0 ;
    public ArrayList<Pair<Integer,Integer>> chunks = new ArrayList<>() ;


    public SwitchCaseUtility switchCaseUtility = new SwitchCaseUtility();
    public SymbolTableNode() {

    }

    public SymbolTableNode(IdType type, int address, String name, int size) {

        this.type = type;
        this.address = address;
        this.name = name;
        this.size = size;
    }

}
