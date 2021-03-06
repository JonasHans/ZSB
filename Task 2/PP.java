/*
 * PP.java
 * Assignment for the Path planning part of the ZSB lab course.
 *
 * This you will work on writing a function called highPath() to move a
 * chesspiece across the board at a safe height. By raising the gripper 20 cm
 * above the board before moving it over the board you don't risk hitting any
 * other pieces on the board. This means you don't have to do any pathplanning
 * yet.
 *
 * Input of this program is a commandline argument, specifying the computer 
 * (white) move. Your job is to find the correct sequence of GripperPositions
 * (stored in Vector p) to pick up the correct white piece and deposit it at
 * its desired new location. Read file
 * /opt/stud/robotics/hints/HIGHPATH_POSITIONS to see what intermediate
 * positions you should calculate.
 *
 * To run your program, fire up playchess or one of its derviates endgame* and
 * the umirtxsimulator. In the simulator you can see the effect of your path
 * planning although the board itself is not simulated. When you think you've
 * solved this assignment ask one of the lab assistents to verify it and let
 * it run on the real robot arm.
 * 
 * You can also compare your solution with the standard PP solution outside
 * playchess by running in a shell:
 * java PPstandard e2e4
 * cat positions.txt
 * java PP e2e4
 * cat positions.txt
 *
 *
 * 
 * Nikos Massios, Matthijs Spaan <mtjspaan@science.uva.nl>
 * $Id: Week2.java,v a4f44ea5d321 2008/06/16 09:18:44 obooij $
 */

import java.io.*;
import java.lang.*;
import java.util.Vector;

public class PP {
  private static double SAFE_HEIGHT=200;
  private static double LOW_HEIGHT=40;
  private static double LOWPATH_HEIGHT=20;
  private static double OPEN_GRIP=30;
  private static double CLOSED_GRIP=0;


  public static void main(String[] args){
    Vector <GripperPosition> p = new Vector<GripperPosition>();
    ChessBoard b;
    String computerFrom, computerTo;

    System.out.println("**** THIS IS THE STUDENT PP MODULE IN JAVA"); 
    System.out.println("**** The computer move was "+ args[0]); 

    /* Read possibly changed board position */
    if(args.length > 1)
    {
      double x=Double.parseDouble(args[1]),
             y=Double.parseDouble(args[2]),
             theta=Double.parseDouble(args[3]);
      Point boardPosition=new Point(x,y,0);

      System.out.println("**** Chessboard is at (x,y,z,theta): ("
                               + x + ", " + y + ", 0, " + theta + ")");

      b = new ChessBoard(boardPosition, theta);
    }
    else
      b = new ChessBoard();

    /* Read the board state*/
    b.read();
    /* print the board state*/
    System.out.println("**** The board before the move was:");       
    b.print();
    
    computerFrom = args[0].substring(0,2);
    computerTo = args[0].substring(2,4);
    
    /* plan a path for the move */
    highPath(computerFrom, computerTo, b, p);

    /* move the computer piece */
    try {
      b.movePiece(computerFrom, computerTo);
    } catch (ChessBoard.NoPieceAtPositionException e) {
      System.out.println(e);
      System.exit(1);
    }

    System.out.println("**** The board after the move was:");       
    /* print the board state*/
    b.print();
    
    /* after done write the gripper positions */
    GripperPosition.write(p);
  }

  private static void highPath(String from, String to, 
           ChessBoard b, Vector<GripperPosition> p) {

    System.out.println("**** In high path"); 

    // ???? Write this function

    // Use the boardLocation and toCartesian methods you wrote:
    StudentBoardTrans studentBoardTransFrom = new StudentBoardTrans(from);
    StudentBoardTrans studentBoardTransTo = new StudentBoardTrans(to);
    
    Point coordsTo = new Point ();
    int toColumn = studentBoardTransTo.boardLocation.column;
    int toRow = studentBoardTransTo.boardLocation.row;
    
    coordsTo = studentBoardTransTo.toCartesian(toColumn, toRow);
    double startX = coordsTo.x;
    double startY = coordsTo.y;
    double startZ = coordsTo.z;

    Point coordsFrom = new Point ();
    int fromColumn = studentBoardTransFrom.boardLocation.column;
    int fromRow = studentBoardTransFrom.boardLocation.row;
    
    coordsFrom = studentBoardTransFrom.toCartesian(fromColumn, fromRow);
    double endX = coordsFrom.x;
    double endY = coordsFrom.y;
    double endZ = coordsFrom.z;

    double pieceHeight = 0;
    try {
        pieceHeight = b.getHeight(from); 
    } catch (ChessBoard.NoPieceAtPositionException e) {
        System.out.println(e);
        System.exit(1);
    }

    if (b.hasPiece(to)) {
        moveToGarbage(to, b, p, coordsTo);
    }

    Point tempPoint = new Point(startX, startY, SAFE_HEIGHT);
    GripperPosition temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    p.add(temp);

    tempPoint = new Point(startX, startY, LOW_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    p.add(temp);

    tempPoint = new Point(startX, startY, startZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    p.add(temp);

    tempPoint = new Point(startX, startY, startZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    p.add(temp);

    tempPoint = new Point(startX, startY, SAFE_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    p.add(temp);
    
    tempPoint = new Point(endX, endY, SAFE_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    p.add(temp);

    tempPoint = new Point(endX, endY, LOW_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    p.add(temp);

    tempPoint = new Point(endX, endY, endZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    p.add(temp);

    tempPoint = new Point(endX, endY, endZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    p.add(temp);

    tempPoint = new Point(endX, endY, SAFE_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    p.add(temp);
  }

  private static void moveToGarbage(String to, ChessBoard b, Vector<GripperPosition> g, Point startCoords) {

    /* When you're done with highPath(), incorporate this function.
     * It should remove a checked piece from the board.
     * In main() you have to detect if the computer move checks a white
     * piece, and if so call this function to remove the white piece from
     * the board first.
     */
    System.out.println("**** In movoToGarbage");  
    
    double startX = startCoords.x;
    double startY = startCoords.y;
    double startZ = startCoords.z;

    double endX = b.coords.x + 100;
    double endY = b.coords.y - 100;
    double endZ = 0;

    double pieceHeight = 0;
    try {
        pieceHeight = b.getHeight(to); 
    } catch (ChessBoard.NoPieceAtPositionException e) {
        System.out.println(e);
        System.exit(1);
    }
    
    Point tempPoint = new Point(startX, startY, SAFE_HEIGHT);
    GripperPosition temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, LOW_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, startZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, startZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, SAFE_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);
    
    tempPoint = new Point(endX, endY, SAFE_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, LOW_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, endZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, endZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, SAFE_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);
  }

  private static void lowPath(String from, String to, ChessBoard b, Vector p) {
    
    System.out.println("**** In low path"); 

    // Use the boardLocation and toCartesian methods you wrote:
    StudentBoardTrans studentBoardTransFrom = new StudentBoardTrans(from);
    StudentBoardTrans studentBoardTransTo = new StudentBoardTrans(to);
    
    Point coordsTo = new Point ();
    int toColumn = studentBoardTransTo.boardLocation.column;
    int toRow = studentBoardTransTo.boardLocation.row;
    
    coordsTo = studentBoardTransTo.toCartesian(toColumn, toRow);
    double startX = coordsTo.x;
    double startY = coordsTo.y;
    double startZ = coordsTo.z;

    Point coordsFrom = new Point ();
    int fromColumn = studentBoardTransFrom.boardLocation.column;
    int fromRow = studentBoardTransFrom.boardLocation.row;
    
    coordsFrom = studentBoardTransFrom.toCartesian(fromColumn, fromRow);
    double endX = coordsFrom.x;
    double endY = coordsFrom.y;
    double endZ = coordsFrom.z;

    double pieceHeight = 0;
    try {
        pieceHeight = b.getHeight(from); 
    } catch (ChessBoard.NoPieceAtPositionException e) {
        System.out.println(e);
        System.exit(1);
    }

    if (b.hasPiece(to)) {
        moveToGarbage(to, b, p, coordsTo);
    }
    
    Point tempPoint = new Point(startX, startY, SAFE_HEIGHT);
    GripperPosition temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, LOW_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, startZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, startZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(startX, startY, LOWPATH_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, LOWPATH_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, endZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, CLOSED_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, endZ + (pieceHeight / 2));
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);

    tempPoint = new Point(endX, endY, SAFE_HEIGHT);
    temp = new GripperPosition(tempPoint, 0, OPEN_GRIP);
    g.add(temp);
    
  }

// Heuristic = Manhattan distance

// Real cost = 1, blocked path = 9001

}
