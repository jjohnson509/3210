import java.util.*;
import java.io.*;


public class Lexer {

   // holds any number of tokens that have been put back
private Stack<Token> stack;

// the source of physical symbols
// (use BufferedReader instead of Scanner because it can
//  read a single physical symbol)
private BufferedReader input;
private int lookahead;

public Lexer( String fileName ) {
  try {
    input = new BufferedReader( new FileReader( fileName ) );
  }
  catch(Exception e) {
    error("Problem opening file named [" + fileName + "]" );
  }
  stack = new Stack<Token>();
  lookahead = 0;  // indicates no lookahead symbol present
}// constructor

private Token getNext() {
   if( ! stack.empty() ) {
      //  produce the most recently putback token
      Token token = stack.pop();
      return token;
   }

   else {
         // produce a token from the input source

         int state = 1;  // state of FA
         String data = "";  // specific info for the token
         boolean done = false;
         int sym;  // holds current symbol

         do {
            sym = getNextSymbol();

            if ( state == 1 ) {
               if ( sym == 9 || sym == 10 || sym == 13 ||
                    sym == 32 ) {// whitespace
                  state = 1;
               }

               else if( letter( sym ) ){
                 data += (char) sym;
                 state = 2;
               }
               else if( digit (sym) ){
                 data += (char) sym;
                 state = 3;
               }
               else if( sym == '-'){
                 data += (char) sym;
                 state = 4;
               }
               else if( sym == '(' || sym == ')'){
                 data += (char) sym;
                 state = 5;
                 done = true;
               }
