
%%

%public
%class Scanner

%unicode
%type String
%line
%column


%{
  StringBuilder string = new StringBuilder();
  java.io.Writer writer;
  String lineSeparator = System.getProperty( "line.separator" );
  public Scanner(java.io.Reader in, java.io.Writer writer) {
      this(in);
      this.writer = writer;
  }

  private long parseLong(int start, int end, int radix) {
    long result = 0;
    long digit;

    for (int i = start; i < end; i++) {
      digit  = Character.digit(yycharat(i),radix);
      result*= radix;
      result+= digit;
    }

    return result;
  }
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "%&" [^&] ~"&%" | "%&" "&"+ "%"
EndOfLineComment = "%%" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = ([:jletter:][:jletterdigit:]*[0-9a-zA-Z] |  [0-9a-zA-Z])
ErrorIdentifierOne = [:jletter:][:jletterdigit:]*"_"+
ErrorIdentifierTwo = [0-9]+[:jletter:][:jletterdigit:]*"_"*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexDigit          = [0-9a-fA-F]

/* floating point literals */
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]


%state STRING, CHARLITERAL

%%

<YYINITIAL> {

  /* keywords */
  "bool"                         { writer.write("token : " + "1" + " text : " + yytext() + lineSeparator);return "1"; }
  "break"                        { writer.write("token : " + "2" + " text : " + yytext() + lineSeparator);return "2"; }
  "byte"                         { writer.write("token : " + "3" + " text : " + yytext() + lineSeparator);return "3"; }
  "case"                         { writer.write("token : " + "4" + " text : " + yytext() + lineSeparator);return "4"; }
  "char"                         { writer.write("token : " + "5" + " text : " + yytext() + lineSeparator);return "5"; }
  "const"                        { writer.write("token : " + "6" + " text : " + yytext() + lineSeparator);return "6"; }
  "continue"                     { writer.write("token : " + "7" + " text : " + yytext() + lineSeparator);return "7"; }
  "double"                       { writer.write("token : " + "8" + " text : " + yytext() + lineSeparator);return "8"; }
  "else"                         { writer.write("token : " + "9" + " text : " + yytext() + lineSeparator);return "9"; }
  "float"                        { writer.write("token : " + "10" + " text : " + yytext() + lineSeparator);return "10"; }
  "for"                          { writer.write("token : " + "11" + " text : " + yytext() + lineSeparator);return "11"; }
  "default"                      { writer.write("token : " + "12" + " text : " + yytext() + lineSeparator);return "12"; }
  "int"                          { writer.write("token : " + "13" + " text : " + yytext() + lineSeparator);return "13"; }
  "long"                         { writer.write("token : " + "14" + " text : " + yytext() + lineSeparator);return "14"; }
  "goto"                         { writer.write("token : " + "15" + " text : " + yytext() + lineSeparator);return "15"; }
  "if"                           { writer.write("token : " + "16" + " text : " + yytext() + lineSeparator);return "16"; }
  "switch"                       { writer.write("token : " + "17" + " text : " + yytext() + lineSeparator);return "17"; }
  "return"                       { writer.write("token : " + "18" + " text : " + yytext() + lineSeparator);return "18"; }
  "while"                        { writer.write("token : " + "19" + " text : " + yytext() + lineSeparator);return "19"; }
  "extern"                       { writer.write("token : " + "20" + " text : " + yytext() + lineSeparator);return "20"; }
  "record"                       { writer.write("token : " + "21" + " text : " + yytext() + lineSeparator);return "21"; }
  "sizeof"                       { writer.write("token : " + "23" + " text : " + yytext() + lineSeparator);return "23"; }
  "of"                           { writer.write("token : " + "24" + " text : " + yytext() + lineSeparator);return "24"; }
  "until"                        { writer.write("token : " + "25" + " text : " + yytext() + lineSeparator);return "25"; }
  "void"                         { writer.write("token : " + "26" + " text : " + yytext() + lineSeparator);return "26"; }
  "include"                      { writer.write("token : " + "27" + " text : " + yytext() + lineSeparator);return "27"; }
  "string"                       { writer.write("token : " + "28" + " text : " + yytext() + lineSeparator);return "28"; }


  /* boolean literals */
  "true"                         { writer.write("token : " + "29" + " text : " + yytext() + lineSeparator);return "29"; }
  "false"                        { writer.write("token : " + "30" + " text : " + yytext() + lineSeparator);return "30"; }

  /* separators */
  "("                            { writer.write("token : " + "31" + " text : " + yytext() + lineSeparator);return "31"); }
  ")"                            { writer.write("token : " + "32" + " text : " + yytext() + lineSeparator);return "32"; }
  "{"                            { writer.write("token : " + "33" + " text : " + yytext() + lineSeparator);return "33"; }
  "}"                            { writer.write("token : " + "34" + " text : " + yytext() + lineSeparator);return "34"; }
  "["                            { writer.write("token : " + "35" + " text : " + yytext() + lineSeparator);return "35"; }
  "]"                            { writer.write("token : " + "36" + " text : " + yytext() + lineSeparator);return "36"; }
  ";"                            { writer.write("token : " + "37" + " text : " + yytext() + lineSeparator);return "37" }
  ","                            { writer.write("token : " + "38" + " text : " + yytext() + lineSeparator);return "38" }
  "."                            { writer.write("token : " + "39" + " text : " + yytext() + lineSeparator);return "39" }
  "."                            { writer.write("token : " + "40" + " text : " + yytext() + lineSeparator);return "40" }

  /* operators */
  "="                            { writer.write("token : " + "41" + " text : " + yytext() + lineSeparator);return "41" }
  ">"                            { writer.write("token : " + "42" + " text : " + yytext() + lineSeparator);return "42" }
  "<"                            { writer.write("token : " + "43" + " text : " + yytext() + lineSeparator);return "43" }
  "!"                            { writer.write("token : " + "45" + " text : " + yytext() + lineSeparator);return "44" }
  "~"                            { writer.write("token : " + "46" + " text : " + yytext() + lineSeparator);return "46" }
  ":"                            { writer.write("token : " + "47" + " text : " + yytext() + lineSeparator);return "47" }
  "=="                           { writer.write("token : " + "48" + " text : " + yytext() + lineSeparator);return "48" }
  "<="                           { writer.write("token : " + "49" + " text : " + yytext() + lineSeparator);return "49"; }
  ">="                           { writer.write("token : " + "50" + " text : " + yytext() + lineSeparator);return "50" }
  "!="                           { writer.write("token : " + "51" + " text : " + yytext() + lineSeparator);return "51" }
  "&&"                           { writer.write("token : " + "52" + " text : " + yytext() + lineSeparator);return "52" }
  "||"                           { writer.write("token : " + "53" + " text : " + yytext() + lineSeparator);return "53"; }
  "++"                           { writer.write("token : " + "54" + " text : " + yytext() + lineSeparator);return "54";; }
  "--"                           { writer.write("token : " + "55" + " text : " + yytext() + lineSeparator);return "55"; }
  "+"                            { writer.write("token : " + "56" + " text : " + yytext() + lineSeparator);return "56"; }
  "-"                            { writer.write("token : " + "57" + " text : " + yytext() + lineSeparator);return "57"; }
  "*"                            { writer.write("token : " + "58" + " text : " + yytext() + lineSeparator);return "58"; }
  "/"                            { writer.write("token : " + "59" + " text : " + yytext() + lineSeparator);return "59"; }
  "&"                            { writer.write("token : " + "60" + " text : " + yytext() + lineSeparator);return "60"; }
  "|"                            { writer.write("token : " + "61" + " text : " + yytext() + lineSeparator);return "61"; }
  "^"                            { writer.write("token : " + "62" + " text : " + yytext() + lineSeparator);return "62"; }
  "%"                            { writer.write("token : " + "63" + " text : " + yytext() + lineSeparator);return "63"; }


  /* string literal */
  \"                             { yybegin(STRING); string.setLength(0); }

  /* character literal */
  \'                             { yybegin(CHARLITERAL); }

  /* numeric literals */

  {DecIntegerLiteral}            { writer.write("token : " + "64" + " text : " + yytext() + " value : " + new Integer(yytext()) + lineSeparator);return "64"; }

  {HexIntegerLiteral}            { writer.write("token : " + "64" + " text : " + yytext() + " value : " + new Integer((int) parseLong(2, yylength(), 16)) + lineSeparator);return "64"; }


  {FloatLiteral}                 { writer.write("token : " + "64" + " text : " + yytext() + " value : " + new Float(yytext().substring(0,yylength()-1)) + lineSeparator);return "65"; }
  {DoubleLiteral}                { writer.write("token : " + "64" + " text : " + yytext() + " value : " + new Double(yytext()) + lineSeparator);return "65"; }

  /* comments */
  {Comment}                      { /* ignore */ writer.write("token : " + "comment" + " text : " + yytext() + lineSeparator);}

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* Error cases */
  {ErrorIdentifierOne}           { writer.write("Illegal identifier \""+yytext()+
                                                  "\" at line "+yyline+", column "+yycolumn+ lineSeparator); }
  {ErrorIdentifierTwo}           { writer.write("Illegal identifier \""+yytext()+
                                                    "\" at line "+yyline+", column "+yycolumn+ lineSeparator); }
  /* identifiers */
  {Identifier}                   { writer.write("token : " + "65" + " text : " + yytext() + lineSeparator );return "65"; }


}

<STRING> {
  \"                             { yybegin(YYINITIAL);writer.write("token : " + "66" + " text : " + string + lineSeparator); return "66"; }

  {StringCharacter}+             { string.append( yytext() ); }

  /* escape sequences */
  "\\b"                          { string.append( '\b' ); }
  "\\t"                          { string.append( '\t' ); }
  "\\n"                          { string.append( '\n' ); }
  "\\f"                          { string.append( '\f' ); }
  "\\r"                          { string.append( '\r' ); }
  "\\\""                         { string.append( '\"' ); }
  "\\'"                          { string.append( '\'' ); }
  "\\\\"                         { string.append( '\\' ); }


  /* error cases */
  \\.                            { writer.write("Illegal escape sequence \""+yytext()+"\"" + lineSeparator); }
  {LineTerminator}               { writer.write("Unterminated string at end of line" + lineSeparator); }
}

<CHARLITERAL> {
  {SingleCharacter}\'            { yybegin(YYINITIAL); return "67";}

  /* escape sequences */
  "\\b"\'                        { yybegin(YYINITIAL);writer.write("token : " + "67" + " text : " + yytext() + lineSeparator ); return "67";}
  "\\t"\'                        { yybegin(YYINITIAL);writer.write("token : " + "67" + " text : " + yytext() + lineSeparator ); return "67";}
  "\\n"\'                        { yybegin(YYINITIAL);writer.write("token : " + "67" + " text : " + yytext() + lineSeparator ); return "67";}
  "\\f"\'                        { yybegin(YYINITIAL);writer.write("token : " + "67" + " text : " + yytext() + lineSeparator ); return "67";}
  "\\r"\'                        { yybegin(YYINITIAL);writer.write("token : " + "67" + " text : " + yytext() + lineSeparator ); return "67";}
  "\\\""\'                       { yybegin(YYINITIAL);writer.write("token : " + "67" + " text : " + yytext() + lineSeparator ); return "67";}
  "\\'"\'                        { yybegin(YYINITIAL);writer.write("token : " + "67" + " text : " + yytext() + lineSeparator ); return "67";}
  "\\\\"\'                       { yybegin(YYINITIAL); writer.write("token : " + "67" + " text : " + yytext() + lineSeparator );return "67";}

  /* error cases */
  \\.                            { writer.write("Illegal escape sequence \""+yytext()+"\""+lineSeparator); }
  {LineTerminator}               { writer.write("Unterminated character literal at end of line"+lineSeparator); }
}

/* error fallback */
.|\n                             { writer.write("Illegal character \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn+lineSeparator); }
<<EOF>>                          { writer.write("token : " + "68" + " text : <<EOF>>" );writer.flush();writer.close();return "68"; }
