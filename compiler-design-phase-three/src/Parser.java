// Should not be modified

import Codegen.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class Parser
{
	Scanner scanner;    // *** THIS IS NOT java.util.Scanner ***
	OutputStream os;
	CodeGenerator cg;
	PTBlock[][] parseTable;	// a 2D array of blocks, forming a parse table
	SymbolTable symbolTable = new SymbolTable();
	Stack<Integer> parseStack = new Stack<Integer>();
	String[] symbols;

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
		}
		catch (IOException e)
		{
			System.err.println("Parsing Error -> IOException at opening input stream");
		}
	}

	public Parser(InputStream is, OutputStream os, String[] symbols, PTBlock[][] parseTable, BlockManager bm)
	{
		try
		{
			this.parseTable = parseTable;
			this.symbols = symbols;
			scanner = new Scanner(is, os);	// *** THIS IS NOT java.util.Scanner ***
			this.os = os;
			cg = new CodeGenerator(scanner);
			scanner.blockManager = bm;
		}
		catch (IOException e)
		{
			System.err.println("Parsing Error -> IOException at opening input stream");
		}
	}

	/**
	 * All the parsing operations is here.
	 * operations were defined in .npt file, and now they are loaded into parseTable
	 */
	public Code parse()
	{
		try
		{
			int tokenID = nextTokenID();
			String tokenText = symbols[tokenID];
			int currrentNode = 0;   // start node
			boolean accepted = false;   // is input accepted by parser?
			while (!accepted)
			{

				// current token's text
				tokenText = symbols[tokenID];

				// current block of parse table
				PTBlock ptb = parseTable[currrentNode /* the node that parser is there */][tokenID /* the token that parser is receiving at current node */];
				switch (ptb.getAct())
				{
					case PTBlock.ActionType.Error:
					{
						throw new Exception("Compile Error at token \"" + tokenText + "\" at line " + scanner.lineNumber + " ; node@" + currrentNode);
					}

					case PTBlock.ActionType.Shift:
					{
						cg.doSemantic(ptb.getSem());
						tokenID = nextTokenID();
						tokenText = symbols[tokenID];
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

			cg.Optimize();
			cg.FinishCode();
			WriteOutput();
			return cg.outputCode;
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private int nextTokenID() throws Exception
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

