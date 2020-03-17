/*  This little app
takes a nicely human-readable
data file and produces Metapost
file to draw the corresponding
diagram,
  where diagrams are all of the
  form of various states (circles)
  with labeled arcs connecting them
*/

import java.util.*;
import java.io.*;

public class ChartToMP {

   private static ArrayList<ChartState> states = new ArrayList<ChartState>();
   public static double unit;  // standard unit for Metapost
   public static double radius;  // radius of all states
   public final static double FRACTION = 0.8;  // fraction of outward radius of state for accepting state inner circle
   public static double labelOffset = 0.1;

   public static boolean proof;  // proof means to draw control points and light gray grid

   // monitor general bounding box for the diagram for
   // drawing a proofing grid
   public static double minX = 100, maxX = -100, minY = 100, maxY = -100;

   // expand bounding box by this amount
   public final static double extraGrid = 2;

   public static void main(String[] args){
      
      int lineNumber = 0;

      try{

        if (args.length != 1 && args.length != 2) {
           System.out.println("Usage:  java ChartToMP <input file name> or java ChartToMP <file> proof");
           System.exit(4);
        }

        if (args.length == 2 ) {
           if (args[1].equals("proof") ) {
              proof = true;
           }
           else {
              System.out.println("Only possible second command line argument is:  proof");
              System.exit(6);
           }
        }
        else {
           proof = false;
        }

        // get name of data file from command line
        Scanner input = new Scanner(new File(args[0]));

        // make Metapost file with same name
        PrintWriter output = new PrintWriter(new File(args[0]+".mp"));

        // make text file with same name, irrelevant details stripped
        PrintWriter output2 = new PrintWriter(new File(args[0]+".txt"));


        unit = 1;

        if ( input.next().equals("unit") ) {// check and toss the keyword "unit"
           unit = input.nextDouble();
           input.nextLine();
        }

        radius = 0.5;

        if ( input.next().equals("radius") ) {// check and toss the keyword "radius"
           radius = input.nextDouble();
           input.nextLine();
        }

        output.println("u = " + unit + "cm;");
        output.println("beginfig(1);");
        output.println("labeloffset := " + labelOffset + "u;" );
        output.println("path p;");  // declare for reuse below
        output.println("pair z;");  // declare as a variable to avoid only allowing one assignment

        // scan all remaining data lines and process
        lineNumber = 3;

        do {
           lineNumber++;

           System.out.println("Scanning line " + lineNumber + " of data file");

           String command = input.next();

           if (command.equals("state")) {
              String name = input.next();
              String junk = input.next();
              if (junk.equals("at")) {
                 double x = input.nextDouble();
                 double y = input.nextDouble();

                 // update bounding box for this state
                 minX = Math.min(minX,x);
                 maxX = Math.max(maxX,x);
                 minY = Math.min(minY,y);
                 maxY = Math.max(maxY,y);

System.out.println("bounds: " + minX + " " + maxX + " " + minY + " " + maxY );

                 String special = input.nextLine().trim();
                 ChartState state = new ChartState(name,x,y,special);
                 states.add(state);
                 state.draw(output);

                 output2.println("state " + name);
              }
              else {
                 System.out.println("expected 'at', got [" + junk + "]");
                 System.exit(3);
              }
           }// state command
         
           else if (command.equals("arc")) {
              String state1 = input.next();
              String state2 = input.next();

              String rest = input.nextLine().trim();

System.out.println("after arc have " + state1 + " " + state2 + " " + rest );

              String label;  
   
              // extract label
              Scanner more;
              if (rest.charAt(0) == '\"' ) {// have label with embedded spaces enclosed in quotes
                 label = "";
                 int index = 1;
                 while (rest.charAt(index) != '\"' ) {// get next symbol in label
                    label += rest.charAt(index);
                    index++;
                 }

                 // index pointing to closing "
                 more = new Scanner( rest.substring(index+1) );
              }
              else {// have label with no embedded spaces
                 more = new Scanner( rest );
                 
                 label = more.next();
              }

              // now have label either way (in quotes or not)

              // more has rest of line after label, as a scanner

              output2.println("arc " + state1 + " " + state2 + " " + label);

              if ( ! more.hasNext()) {// straight line arc---nothing after the label
                 ChartState s1 = findState( state1 );
                 ChartState s2 = findState( state2 );
                 ChartState.drawArc( output, s1, s2, label );
              }
              else {// have points on states, control points and label positioning

                 ChartState s1 = findState( state1 );
                 ChartState s2 = findState( state2 );

                 // get outgoing angle from s1
                 double outAngle = more.nextDouble();
                 // and incoming angle into s2
                 double inAngle = more.nextDouble();

                 // number of control points to specify
                 int numControl = more.nextInt();

                 ArrayList<Double> xControl = new ArrayList<Double>();
                 ArrayList<Double> yControl = new ArrayList<Double>();

                 // control points are offsets from s1
                 // (handy for self-loop)
                 for (int j=0; j<numControl; j++) {
                    xControl.add( more.nextDouble() );
                    yControl.add( more.nextDouble() );
                 }

                 String labelLoc = more.next();

                 ChartState.drawArc( output, 
                                     s1, s2, label,
                                     outAngle, inAngle,
                                     xControl, yControl,
                                     labelLoc );

              }// arc with control points

           }// arc command

           else {
              System.out.println("Error:  unknown command ["+
                                   command + "] on line " + lineNumber);
              System.exit(2);
           }
          
        } while( input.hasNext() );
 
        // if proofing draw a grid for positioning things
        if ( proof ) {
           minX = minX - radius - extraGrid;
           maxX = maxX + radius + extraGrid;
           minY = minY - radius - extraGrid;
           maxY = maxY + radius + extraGrid;

           // generate code to draw the grid

           // vertical lines
           output.println("% draw proofing grid");
           output.println("for k = " + ( (int) Math.floor(minX)) + 
                            " step 1 until " + 
                                     ((int) Math.ceil(maxX)) + " :" ); 
           output.println("  draw (k*u, " + ((int) Math.floor(minY)) + "*u) -- " +
                                    "(k*u, " + ((int) Math.ceil(maxY)) + "u) withcolor blue;");
           output.println("endfor");

           // horizontal lines
           output.println("for k = " + ( (int) Math.floor(minY)) + 
                            " step 1 until " + 
                                     ((int) Math.ceil(maxY)) + " :" ); 
           output.println("  draw (" + ((int) Math.floor(minX)) + "*u, k*u) -- (" +
                                    ((int) Math.ceil(maxX)) + "u, k*u) withcolor blue;");
           output.println("endfor");

        }

        output.println("endfig;");
        output.println("end;");

        output.close();
        output2.close();
      }
      catch(Exception e) {
         System.out.println("Exception on line " + lineNumber);
         e.printStackTrace();
         System.exit(1);
      }

   }// main

   // find in states list the state with give name
   private static ChartState findState(String name) {
      for (int k=0; k<states.size(); k++) {
         ChartState s = states.get(k);
         if (s.name.equals(name)) {
            return s;
         }
      }
      
      System.out.println("could not find state with name " + name );
      System.exit(5);
      return null;
   }

}// ChartToMP
