// Should not be modified

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.HeaderTokenizer;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Parser
{
	Scanner scanner;    // *** THIS IS NOT java.util.Scanner ***
	OutputStream os;
	CodeGenerator cg;
	PTBlock[][] parseTable;	// a 2D array of blocks, forming a parse table
	Stack<Integer> parseStack = new Stack<Integer>();
	String[] symbols;
	ErrorHandler eh;
	int tokenID;
	int currrentNode;

	ArrayList[] graphGOTO,graphSHIFT ;
	TreeNode[] treeNodes ;

	// ArrayList<TreeNode> treeNodes = new ArrayList<>();
	/**
     * Creates a new parser
     * @param is input stream from source text file
     * @param os output stream to write the output there (if any)
     * @param symbols symbols known by parser (tokens + graph nodes)
     * @param parseTable all of the actions describing the parser behaviour
     */
	public Parser(InputStream is, OutputStream os, String[] symbols, PTBlock[][] parseTable)
	{
		try
		{
			this.parseTable = parseTable;
			this.symbols = symbols;
			scanner = new Scanner(is, os);	// *** THIS IS NOT java.util.Scanner ***
			this.os = os;
			cg = new CodeGenerator(scanner);
			eh = new ErrorHandler(this);
			treeNodes = new TreeNode[400] ;
			buildGraph();
		}
		catch (IOException e)
		{
			System.err.println("Parsing Error -> IOException at opening input stream");
		}
	}
	private void buildGraph() {
		graphGOTO = new ArrayList[parseTable.length];
		for (int i = 0; i < parseTable.length; i++)
			graphGOTO[i] = new ArrayList<>();
		graphSHIFT = new ArrayList[parseTable.length];
		for (int i = 0; i < parseTable.length; i++)
			graphSHIFT[i] = new ArrayList<>();

		for (int i = 0; i < parseTable.length; i++)
		{
			for (int j = 0; j < parseTable[i].length; j++)
			{
				if (parseTable[i][j].getAct() == PTBlock.ActionType.Shift)
					graphSHIFT[i].add(j);
				if (parseTable[i][j].getAct() == PTBlock.ActionType.Goto)
					graphGOTO[i].add(j);
			}
		}
	}
	/**
	 * All the parsing operations is here.
	 * operations were defined in .npt file, and now they are loaded into parseTable
	 */
	public void parse()
	{
		try
		{
			tokenID = nextTokenID();
			currrentNode = 0;   // start node
			treeNodes[0] = new TreeNode("Main", new ArrayList<TreeNode>());
			boolean accepted = false;   // is input accepted by parser?
			Stack<Integer> TokensStack = new Stack<>();
			TokensStack.add(tokenID);  // for error handling
			while (!accepted)
			{

                // current token's text
				String tokenText = symbols[tokenID];

				// current block of parse table
	            PTBlock ptb = parseTable[currrentNode /* the node that parser is there */][tokenID /* the token that parser is receiving at current node */];
				switch (ptb.getAct())
				{
	                case PTBlock.ActionType.Error:
	                    {
	                    	eh.addError(TokensStack, currrentNode);
	                    }
	                    break;

					case PTBlock.ActionType.Shift:
						{
							if (treeNodes[parseStack.peek()+1] == null) {
								treeNodes[parseStack.peek()+1] = new TreeNode("", new ArrayList<TreeNode>());
							}
							treeNodes[parseStack.peek()+1].children.add(new TreeNode(symbols[tokenID],new ArrayList<TreeNode>())) ;
							cg.doSemantic(ptb.getSem());
							tokenID = nextTokenID();
							TokensStack.add(tokenID); // for error handling
							currrentNode = ptb.getIndex();  // index is pointing to Shift location for next node
						}
						break;

                    case PTBlock.ActionType.Goto:
                        {
                            cg.doSemantic(ptb.getSem());
                            currrentNode = ptb.getIndex();  // index is pointing to Goto location for next node
                        }
                        break;

					case PTBlock.ActionType.PushGoto:
						{
							parseStack.push(currrentNode);
							currrentNode = ptb.getIndex();  // index is pointing to Goto location for next node
						}
						break;
					case PTBlock.ActionType.Reduce:
						{
							if (parseStack.size() == 0)
	                        {
                                throw new Exception("Compile Error trying to Reduce(Return) at token \"" + tokenText + "\" at line " + scanner.lineNumber + " ; node@" + currrentNode);
	                        }

                            int graphToken = ptb.getIndex();    // index is the graphToken to be returned
							int preNode = parseStack.pop();     // last stored node in the parse stack
							if(treeNodes[preNode+1] == null){
								treeNodes[preNode+1] = new TreeNode("",new ArrayList<TreeNode>()) ;
							}
							treeNodes[preNode+1].name = symbols[graphToken] ;

							if (!parseStack.empty()){
								int parent = parseStack.peek();
								if (treeNodes[parent+1] == null)
									treeNodes[parent+1] = new TreeNode("",new ArrayList<TreeNode>());
								treeNodes[parent+1].children.add(treeNodes[preNode+1]) ;
							}else{
								treeNodes[0].children.add(treeNodes[preNode+1]) ;
							}
							cg.doSemantic(parseTable[preNode][graphToken].getSem());
                            currrentNode = parseTable[preNode][graphToken].getIndex(); // index is pointing to Goto location for next node
						}
						break;
	
					case PTBlock.ActionType.Accept:
						{
							accepted = true;
						}
						break;
						
				}
	        }
			if (eh.hasError())
			{
				Stack<CompileError> es = eh.getErrorStack();
				while (es.size() > 0) {
					System.out.println(es.pop().getMessage());
				}
			}
			else
			{
				treeNodes[0].print();
			}
			cg.FinishCode();
		}
		catch (Exception e)
		{
            System.err.println(e.getMessage());
		}
	}

	protected int nextTokenID() throws Exception
	{
		String t = null;
		try
		{
			t = scanner.NextToken();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		int i;
		
		for (i = 0; i < symbols.length; i++)
			if (symbols[i].equals(t))
				return i;

		throw new Exception("Undefined token: " + t);
	}

    /**
     * Used to write any needed output after the parsing is done.
     */
	public void WriteOutput()
	{
        // this is common that the code generator does it
        cg.WriteOutput(os);
	}
}

