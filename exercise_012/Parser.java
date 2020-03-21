import java.util.*;
import java.io.*;

public class Parser {

  private Lexer lex;

  public Parser(Lexer lexer){
    lex = lexer;
  }

  public Node parseDefs(){
      System.out.println("-----> parsing <defs>:");
      Node first = parseDef();

      // look ahead to see if there are more funcDef's
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         return new Node( "defs", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseDefs();
         return new Node( "defs", first, second, null );
      }

  }

  public Node parseDef(){
    System.out.println("-----> parsing <def>:");

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
      Node first = parseExpr();
      token = lex.getNextToken();
      errorCheck(token, "rparen");
      return new Node("def", name.getDetails(), first, null, null); //maybe change to second, if issues
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
    System.out.println("-----> parsing <params>:");
    Token token = lex.getNextToken();
    errorCheck( token, "name");

    Node first = new Node("name", token.getDetails(), null, null, null);

    token = lex.getNextToken();
    if(token.isKind("rparen")){
      lex.putBackToken(token);
      return new Node("params", first, null, null);
    }
    else if(token.isKind("name")){
      lex.putBackToken(token);
      Node second = parseParams();
      return new Node("params", first, second, null);
    }
    else{
      System.out.println("expected 'name' or ) and saw " + token );
      System.exit(1);
      return null;
    }
  }

  public Node parseExpr(){
    System.out.println("-----> parsing <expr>:");
    Token token = lex.getNextToken();
    if(token.isKind("num")){
      return new Node("num", token.getDetails(), null, null, null);
    }
    else if(token.isKind("name")){
      return new Node("name", token.getDetails(), null, null, null);
    }
    else if(token.isKind("lparen")){
      lex.putBackToken(token);
      Node first = parseList();
      return first;
    }
     // else if(token.isKind("rparen")){
     //   lex.putBackToken(token);
     //   return null
     // }
    else{
      System.out.println("expected <list>, NAME, or NUMBER and saw " + token);
      System.exit(1);
      return null;
    }

  }

  public Node parseList(){
    System.out.println("-----> parsing <list>:");
    Token token = lex.getNextToken();
    errorCheck(token, "lparen");
    token = lex.getNextToken();
    if(token.isKind("rparen")){
      return new Node("list", token.getDetails(), null, null, null);
    }
    else{
      lex.putBackToken(token);
      Node first = parseItems();
      return new Node("list", token.getDetails(), first, null, null);
    }
  }

  public Node parseItems(){
    System.out.println("-----> parsing <items>:");
    //Token token = lex.getNextToken();
    Node first = parseExpr();
    Token token = lex.getNextToken();
    if(token.isKind("lparen") || token.isKind("num") || token.isKind("name")){
      lex.putBackToken(token);
      Node second = parseItems();
      return new Node("items", first, second, null);
    }

    else{
      //lex.putBackToken(token);
      return new Node("items", first, null, null);
    }
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
