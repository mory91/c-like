/**
 * This is the template of class 'scanner'.
 * You should place your own object of scanner class (i.e. LexicalAnalyser) here.
 * your scanner should match this template.
 *
 */

// *** THIS IS NOT java.util.Scanner ***

import Codegen.BlockManager;
import Codegen.IdType;
import Codegen.SymbolTable;
import Codegen.SymbolTableNode;

import java.io.*;

public class Scanner
{
    private boolean errorMode = false;
    private boolean gotoMode = false;

    public String token;    // current token received
    public int lineNumber;
    LexicalAnalyzer analyzer;
    public SymbolTableNode stp;
    public BlockManager blockManager = new BlockManager();

    Scanner(InputStream is, OutputStream os) throws IOException
    {
        // create the inner scanner, made by jflex
        analyzer = new LexicalAnalyzer(new InputStreamReader(is), new OutputStreamWriter(os));
    }

    public String NextToken() throws Exception
    {
        token = analyzer.yylex();
        lineNumber = analyzer.getLine();

		// TODO: Try to handel this with graphs
        if (errorMode && !token.equals(":"))
            throw new Exception("Compile Error at token \"" + token + "\" at line " + lineNumber + " ;");
        else if (token.equals(":"))
            errorMode = false;
        if (token.equals("goto"))
            gotoMode = true;
        if (token.equals("id_") && !gotoMode)
		{
			//TODO : Debag
			if (blockManager.get(getText())!= null && blockManager.get(getText()).type == IdType.FUNC && (stp.name.equals("bool") || stp.equals("double")
					|| stp.name.equals("byte") || stp.name.equals("string") ||
					stp.name.equals("int") || stp.equals("char") ||
					stp.name.equals("float") || stp.name.equals("void"))) {
				SymbolTableNode tmp = blockManager.get(getText()) ;
				tmp.overloads.add(new SymbolTableNode(IdType.FUNC, 0, getText(), stp.size)) ;
				stp = tmp ;
			}
			else if (blockManager.get(getText())!=null && !(stp.name.equals("bool") || stp.equals("double")
					|| stp.name.equals("byte") || stp.name.equals("string") ||
					stp.name.equals("int") || stp.equals("char") ||
					stp.name.equals("float") || stp.name.equals("void"))) {
				stp = blockManager.get(getText());
			}
			else if (!blockManager.getLastSymTab().symTab.containsKey(getText()) && (stp.name.equals("bool") || stp.equals("double")
					|| stp.name.equals("byte") || stp.name.equals("string") ||
					stp.name.equals("int") || stp.equals("char") ||
					stp.name.equals("float") || stp.name.equals("void"))) {
				SymbolTableNode addStp = new SymbolTableNode(IdType.VAR, 0, getText(), stp.size);
				blockManager.getLastSymTab().symTab.put(getText(), addStp);
				addStp.varType = stp.name;
				stp = blockManager.getLastSymTab().symTab.get(getText());
//				blockManager.getLastSymTab().symTab.put(getText(), new SymbolTableNode(IdType.VAR, 0, getText(), stp.size));
//				stp = blockManager.getLastSymTab().symTab.get(getText());
			}
			else {
			    if (errorMode)
                    throw new Exception("Compile Error at token \"" + token + "\" at line " + lineNumber + " ;");
			    else {
                    errorMode = true;
                    SymbolTableNode addStp = new SymbolTableNode(IdType.LABEL, 0, getText(), stp.size);
                    stp = addStp;
                }
			}
		}
		if (token.equals("id_") && gotoMode) {
            SymbolTableNode addStp = new SymbolTableNode(IdType.LABEL, 0, getText(), stp.size);
            stp = addStp;
            gotoMode = false;
        }
		if (token.equals("bool") || token.equals("double")
				|| token.equals("byte") || token.equals("string") ||
				token.equals("int") || token.equals("char") ||
				token.equals("float") || token.equals("void") )
			stp =  blockManager.getLastSymTab().symTab.get(getText());


        return token;
    }

    public String getToken()
    {
        return token;
    }

    public String getText()
    {
        return analyzer.yytext();
    }



}
