package Codegen;

import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by root on 6/20/17.
 */
public class SymbolTable {
    public TreeMap<String, SymbolTableNode> symTab;
    public int offset = 0 ;
    public SymbolTable() {
        symTab = new TreeMap<>() ;

        SymbolTableNode intNode = new SymbolTableNode();
        intNode.name = "int";
        intNode.size = 4;
        intNode.type = IdType.TYPE;
       	symTab.put(intNode.name,intNode) ;

        SymbolTableNode boolNode = new SymbolTableNode();
        boolNode.name = "bool";
        boolNode.size = 1;
        boolNode.type = IdType.TYPE;
        symTab.put(boolNode.name, boolNode);

        SymbolTableNode byteNode = new SymbolTableNode();
        byteNode.name = "byte";
        byteNode.size = 1;
        byteNode.type = IdType.TYPE;
        symTab.put(byteNode.name, byteNode);

        SymbolTableNode longNode = new SymbolTableNode();
        longNode.name = "long";
        longNode.size = 8;
        longNode.type = IdType.TYPE;
        symTab.put(longNode.name, longNode);

        SymbolTableNode charNode = new SymbolTableNode();
        charNode.name = "char";
        charNode.size = 1;
        charNode.type = IdType.TYPE;
        symTab.put(charNode.name, charNode);

        SymbolTableNode doubleNode = new SymbolTableNode();
        doubleNode.name = "double";
        doubleNode.size = 10;
        doubleNode.type = IdType.TYPE;
        symTab.put(doubleNode.name, doubleNode);

        SymbolTableNode floatNode = new SymbolTableNode();
        floatNode.name = "float";
        floatNode.size = 8;
        floatNode.type = IdType.TYPE;
        symTab.put(floatNode.name, floatNode);

        SymbolTableNode stringNode = new SymbolTableNode();
        stringNode.name = "string";
        stringNode.size = 4;
        stringNode.type = IdType.TYPE;
        symTab.put(stringNode.name, stringNode);

        SymbolTableNode voidNode = new SymbolTableNode();
        voidNode.name = "void";
        voidNode.size = 1;
        voidNode.type = IdType.TYPE;
        symTab.put(voidNode.name, voidNode);
    }
    // TODO : functions for symtab

//    public SymbolTableNode find() {
//
//    }
//    public void remove() {
//
//    }
}
