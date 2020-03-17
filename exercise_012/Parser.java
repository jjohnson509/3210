import java.util.*;
import java.io.*;

public class Parser {

  private Lexer lex;

  public Parser(Lexer lexer){
    lex = lexer;
  }

  public Node parseDefs(){
      System.out.println("-----> parsing <Defs>:");
      Node first = parseDef();

      // look ahead to see if there are more funcDef's
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         return new Node( "Defs", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseDefs();
         return new Node( "Defs", first, second, null );
      }

  }

  public Node parseDef(){
    System.out.println("-----> parsing <funcDef>:");

    Token token = lex.getNextToken();
    errorCheck(token, "lparen");

    token = lex.getNextToken();
    errorCheck(token, "define");

    token = lex.getNextToken();
    errorCheck(token, "lparen");

    Token name = lex.getNextToken();
    errorCheck(name, "name");

    token = lex.getNextToken();

    if(token.isKind("rparen")){
      Node second = parseExpr();
      token = lex.getNextToken();
      errorCheck(token, "rparen");
      return new Node("def", name.getDetails(), null, second, null);
    }
    else{
      lex.putBackToken(token);
      Node first = parseParams();
      token = lex.getNextToken();
      errorCheck(token, "rparen");
      Node second = parseExpr();
      token = lex.getNextToken();
      errorCheck(token, "rparen");
      return new Node("def", name.getDetails(), first, second, null);
    }
  }

  public Node parseParams(){
    System.out.println("-----> parsing <params>:")
    Token token = lex.getNextToken();
  }

  public Node parseExpr(){
    System.out.println("-----> parsing <expr>:")
    Token token = lex.getNextToken();

  }

  public Node parseList(){
    System.out.println("-----> parsing <list>:")
    Token token = lex.getNextToken();
  }

  public Node parseItems(){
    System.out.println("-----> parsing <items>:")
    Token token = lex.getNextToken();

  }

  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token +
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

    // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) ||
        ! token.getDetails().equals( details ) ) {
      System.out.println("Error:  expected " + token +
                          " to be kind= " + kind +
                          " and details= " + details );
      System.exit(1);
    }
  }

}
