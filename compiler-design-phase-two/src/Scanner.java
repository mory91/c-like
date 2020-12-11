/**
 * This is the template of class 'scanner'.
 * You should place your own object of scanner class (i.e. LexicalAnalyser) here.
 * your scanner should match this template.
 *
 */

// *** THIS IS NOT java.util.Scanner ***

import java.io.*;

public class Scanner
{
    public String token;    // current token received
    public int lineNumber;
    LexicalAnalyzer analyzer;

    Scanner(InputStream is, OutputStream os) throws IOException
    {
        // create the inner scanner, made by jflex
        analyzer = new LexicalAnalyzer(new InputStreamReader(is), new OutputStreamWriter(os));
    }

    public String NextToken() throws Exception
    {
        token = analyzer.yylex();
        lineNumber = analyzer.getLine();
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
