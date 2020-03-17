import java.io.PrintWriter;

import java.util.ArrayList;

public class ChartState {

   // distance for labels perpendicular away from straight line arcs

   public String name;  // name of the state
   public double x, y;  // location of center point of state
   public String kind;  // "a" for accepting, "s" for start, "" for neither

   public ChartState(String n, double xin, double yin, String k) {
      name = n;
      x = xin;  y = yin;
      kind = k;
   }

   public void draw(PrintWriter output) {

      output.println("draw fullcircle" +
                     " scaled " + (2*ChartToMP.radius) + "u" +
                     " shifted (" + x + "u," + y + "u);");

      output.println("label(btex $" + name + "$ etex, (" +
                       x + "u," + y + "u) ) withcolor black;");

      if (kind.equals("a")) {// accepting state, draw extra inner circle
         output.println("draw fullcircle" +
                        " scaled " + (2*ChartToMP.FRACTION*ChartToMP.radius) + "u" +
                        " shifted (" + x + "u," + y + "u);");
      }
      else if (kind.equals("s")) {// start state, draw little arrow
         output.println("drawarrow (" + (x - 2.0*ChartToMP.radius) + "u," +
                          y + "u)..(" + (x-ChartToMP.radius) + "u," + y + "u);" );
      }
      else if (kind.equals("sa") || kind.equals("as") ) {
         output.println("draw fullcircle" +
                        " scaled " + (2*ChartToMP.FRACTION*ChartToMP.radius) + "u" +
                        " shifted (" + x + "u," + y + "u);");
         output.println("drawarrow (" + (x - 2.0*ChartToMP.radius) + "u," +
                          y + "u)..(" + (x-ChartToMP.radius) + "u," + y + "u);" );
      }
                      
   }// draw

   // draw straight line arc from s1 to s2 with given label
   public static void drawArc( PrintWriter output,
                               ChartState s1, ChartState s2, String label ) {
      
      // get v as unit vector along perp to the left of direction from s1 to s2
      double x1 = s1.x, y1 = s1.y, x2 = s2.x, y2 = s2.y;
      double vx = x2-x1, vy = y2-y1;
      double len = Math.sqrt( vx*vx + vy*vy );
      vx /= len;  vy /= len;
     
      double r = ChartToMP.radius;

      // points on circles along line are p1 and p2
      double p1x = x1 + r*vx, p1y = y1 + r*vy;
      double p2x = x2 - r*vx, p2y = y2 - r*vy;

      output.println("drawarrow (" + p1x + "u," + p1y + "u)..(" +
                                p2x + "u," + p2y + "u) withcolor black;" );

      // label offset from center point
      double zx = (p1x+p2x)/2, zy = (p1y+p2y)/2;

      String location = "";

      // set location to a metapost option for label locating, depending on angle of the line (vx,vy)
      double ax = Math.abs(vx), ay = Math.abs(vy);
      final double factor = 0.5;

      if ( ay <= factor * ax ) {// close to x axis
         if ( vx >= 0 ) {
            location = ".top";
         }
         else {// vx < 0
            location = ".bot";
         }
      }
      else if ( ax <= factor * ay ) {// close to y axis
         if ( vy >= 0 ) {
            location = ".lft";
         }
         else {// vx < 0
            location = ".rt";
         }
      }
      else {// ay > factor * ax && ax > factor * ay --- away from axes
         if ( vx > 0 && vy > 0 ) {// quadrant 1
            location = ".ulft";
         }
         else if ( vx > 0 && vy < 0 ) {// quadrant 4
            location = ".urt";
         }
         else if ( vx < 0 && vy > 0 ) {// quadrant 2
            location = ".llft";
         }
         else {// quadrant 3---vx, vy both negative
            location = ".lrt";
         }
      }

      output.println("label" + location + "(btex $" + label + "$ etex, (" +
                       zx + "u," + 
                       zy + "u) ) withcolor black;");
      
   }
   
   // draw Bezier curve with 1 or more control points
   // outAngle and inAngle specify points on s1 and s2,
   // the control points are given as offsets from the center of s1,
   // and labelLoc specifies how to position label near middle of curve
   public static void drawArc( PrintWriter output,
                               ChartState s1, ChartState s2, String label,
                               double outAngle, double inAngle,
                               ArrayList<Double> controlX, ArrayList<Double> controlY,
                               String labelLoc
                             ) {

      // compute start and end points on edges of states at given angles
      double r = ChartToMP.radius;
      double startX = nice(s1.x + r*Math.cos( Math.toRadians( outAngle ) )),
             startY = nice( s1.y + r*Math.sin( Math.toRadians( outAngle ) )),
             endX = nice( s2.x + r*Math.cos( Math.toRadians( inAngle ) )),
             endY = nice( s2.y + r*Math.sin( Math.toRadians( inAngle ) ));

      // draw the bezier curve
      output.print( "p := (" + startX + "u," + startY + "u).." );  // point on edge of s1

      for (int k=0; k<controlX.size(); k++) {// control point(s)
         output.print( "(" + (s1.x+controlX.get(k)) + "u," + (s1.y+controlY.get(k)) + "u).." );
      }

      output.println( "(" + endX + "u," + endY + "u);" );  // point on edge of s2
 
      output.println("drawarrow p;");

      output.println("z := point " + ((controlX.size()+1.0)/2) + " of p;" );

      // if doing a "proof" show control points and middle point
      if (ChartToMP.proof) {
         output.println("pickup pencircle scaled 2mm;");
         for (int k=0; k<controlX.size(); k++) {
            output.println("drawdot (" + (s1.x+controlX.get(k)) + "u," + 
                                      (s1.y+controlY.get(k)) + "u) withcolor red;" );
         }
         output.println("drawdot z withcolor green;");
         output.println("pickup defaultpen;");
      }// proofing

      // draw the label
      output.println("label." + labelLoc + "(btex $" + label + "$ etex, z) withcolor black;");

   }

   // return a rounded to 3rd decimal place
   private static double nice( double a ) {
      int temp = (int) Math.round( 1000*a );
      double x = temp;
      return x / 1000;
   }

}
