



%%
%class LexicalAnalyzer

%unicode
%line
%column
%type String
%{
    public int getLine() {
        return yyline;
    }
%}

EOL = \r|\n|\r\n        // end of line
WhiteSpace = " " | "\t"
DecIntegerLiteral = 0 | [1-9][0-9]*

%%

<YYINITIAL> {
    {DecIntegerLiteral} { return "ic"; }
    "+" {return "+";}
    "-" {return "-";}
    "*" { return "*";}
    "/" {return "/";}
    "(" {return "(";}
    ")" {return ")";}
    {EOL} {return "EOL";}
    {WhiteSpace} { }
    <<EOF>> { return "$"; }
}
