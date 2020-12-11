package Codegen;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by root on 6/21/17.
 */
public class BlockManager {
    public HashSet<String> overloadNames = new HashSet<>() ; //TODO : yekam tof e
    private ArrayList<SymbolTable> list = new ArrayList<>();
    private int pointer = 0;

    public BlockManager() {
        list.add(pointer, new SymbolTable());
        pointer++;
    }
    public SymbolTable getLastSymTab(){
        return list.get(pointer-1);
    }
    public void add(SymbolTable symbolTable) {

        if (pointer < list.size()) {
            list.add(pointer, symbolTable);
        }
        else
        {
            list.add(pointer, symbolTable);
        }
        pointer++;
    }
    public void delete() {
        list.remove(pointer-1);
        pointer--;
    }
    public int getMaxDepth(){
        return pointer ;
    }
    public int getDepth(String x){
		int tmp = pointer - 1;
		while(tmp >= 0)
		{
			if (list.get(tmp).symTab.containsKey(x))
				return tmp+1;
			tmp--;
		}
		return 0;
	}
    public SymbolTableNode get(String x) { // Null As Error
        int tmp = pointer - 1;
        while(tmp >= 0)
        {
            if (list.get(tmp).symTab.containsKey(x))
                return list.get(tmp).symTab.get(x);
            tmp--;
        }
        return null;
    }

}
