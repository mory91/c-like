package Codegen;

import java.util.ArrayList;

/**
 * Created by bardia on 6/29/17.
 */
public class Parameter {
	public String varType ;
	public int address;
	public String name;
	public int size;
	public ArrayList<Integer> dimension = new ArrayList<Integer>() ;

	public Parameter(String varType, String name, int size) {
		this.varType = varType;
		this.name = name;
		this.size = size;
	}
}
