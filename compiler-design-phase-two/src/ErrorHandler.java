import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by root on 5/5/17.
 */
public class ErrorHandler {
    private Parser parser;

    private PTBlock[][] parseTable;
    private String[] symbols;
    private ArrayList<Integer>[] graphGOTO;
    private ArrayList<Integer>[] graphSHIFT;

    private Stack<CompileError> errorStack = new Stack<>();

    public ErrorHandler(Parser parser) {
        this.parser = parser;
        this.parseTable = parser.parseTable;
        this.symbols = parser.symbols;
        buildGraph();
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

    public void addError(Stack<Integer> tokenStack, int Node) {
        // PANIC MODE
        int currentToken = tokenStack.pop();
        String message = "at line " + parser.scanner.lineNumber +": missing --> ";
        for (int  i = 0; i < graphSHIFT[Node].size(); i++)
            message += symbols[graphSHIFT[Node].get(i)] + " , ";
        for (int  i = 0; i < graphGOTO[Node].size(); i++)
            message += symbols[graphGOTO[Node].get(i)] + " , ";
        int tokenId = parser.tokenID;
        try {
            while (!symbols[tokenId].equals(";") && !symbols[tokenId].equals("}") && !symbols[tokenId].equals("$")) {
                if (tokenId == 0) {
                    setParserState(tokenId);
                    errorStack.add(new CompileError(message));
                    return;
                }
                tokenId = parser.nextTokenID();
            }
            if (tokenId == 0) {
                setParserState(tokenId);
                errorStack.add(new CompileError(message));
                return;
            }
            tokenId = parser.nextTokenID();

            setParserState(tokenId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        errorStack.add(new CompileError(message));

    }

    private void setParserState(int tokenId) {
        while (parseTable[parser.parseStack.peek()][tokenId].getAct() == PTBlock.ActionType.Error)
            parser.parseStack.pop();
        parser.tokenID = tokenId;
        parser.currrentNode = parser.parseStack.pop();
    }

    public Stack<CompileError> getErrorStack() {
        return errorStack;
    }

    public boolean hasError() {
        return errorStack.size() > 0;
    }

}
