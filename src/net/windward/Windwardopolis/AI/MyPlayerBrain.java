package net.windward.Windwardopolis.AI;


// Created by Windward Studios, Inc. (www.windward.net). No copyright claimed - do anything you want with this code.


import net.windward.Windwardopolis.api.*;
import net.windward.Windwardopolis.api.Map;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * The sample Java AI. Start with this project but write your own code as this is a very simplistic implementation of the AI.
 */
public class MyPlayerBrain implements net.windward.Windwardopolis.AI.IPlayerAI {
    // bugbug - put your team name here.
    private static String NAME = "Orange Black Tree";

    // bugbug - put your school name here. Must be 11 letters or less (ie use MIT, not Massachussets Institute of Technology).
    public static String SCHOOL = "Princeton";
    private static final double ACCELERATION = 0.1;

    /**
     * The name of the player.
     */
    private String privateName;

    public final String getName() {
        return privateName;
    }

    private void setName(String value) {
        privateName = value;
    }

    /**
     * The game map.
     */
    private Map privateGameMap;
    private ArrayList<Path>[][] j2jPaths; // junction to junction paths of tiles [start][end] gives list of paths
    private ArrayList<Path>[][] d2dPaths; // destination to destination paths of junctions [start][end] gives list of paths

    private ArrayList<Passenger> myPickup;
    private ArrayList<Point> myPath;

    private ArrayList<Point> junctions;

    public final Map getGameMap() {
        return privateGameMap;
    }

    private void setGameMap(Map value) {
        privateGameMap = value;
    }

    /**
     * All of the players, including myself.
     */
    private java.util.ArrayList<Player> privatePlayers;

    public final java.util.ArrayList<Player> getPlayers() {
        return privatePlayers;
    }

    private void setPlayers(java.util.ArrayList<Player> value) {
        privatePlayers = value;
    }

    /**
     * All of the companies.
     */
    private java.util.ArrayList<Company> privateCompanies;

    public final java.util.ArrayList<Company> getCompanies() {
        return privateCompanies;
    }

    private void setCompanies(java.util.ArrayList<Company> value) {
        privateCompanies = value;
    }

    /**
     * All of the passengers.
     */
    private java.util.ArrayList<Passenger> privatePassengers;

    public final java.util.ArrayList<Passenger> getPassengers() {
        return privatePassengers;
    }

    private void setPassengers(java.util.ArrayList<Passenger> value) {
        privatePassengers = value;
    }

    /**
     * Me (my player object).
     */
    private Player privateMe;

    public final Player getMe() {
        return privateMe;
    }

    private void setMe(Player value) {
        privateMe = value;
    }

    private PlayerAIBase.PlayerOrdersEvent sendOrders;

    private static final java.util.Random rand = new java.util.Random();

    public MyPlayerBrain(String name) {
        setName(!net.windward.Windwardopolis.DotNetToJavaStringHelper.isNullOrEmpty(name) ? name : NAME);
    }

    /**
     * The avatar of the player. Must be 32 x 32.
     */
    public final byte[] getAvatar() {
        try {
            // open image
            File file = new File(getClass().getResource("/net/windward/Windwardopolis/res/OBTavatar.png").getFile());

            FileInputStream fisAvatar = new FileInputStream(file);
            byte [] avatar = new byte[fisAvatar.available()];
            fisAvatar.read(avatar, 0, avatar.length);
            return avatar;

        } catch (IOException e) {
            System.out.println("error reading image");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Called at the start of the game.
     *
     * @param map         The game map.
     * @param me          You. This is also in the players list.
     * @param players     All players (including you).
     * @param companies   The companies on the map.
     * @param passengers  The passengers that need a lift.
     * @param ordersEvent Method to call to send orders to the server.
     */
    public final void Setup(Map map, Player me, java.util.ArrayList<Player> players, java.util.ArrayList<Company> companies, java.util.ArrayList<Passenger> passengers, PlayerAIBase.PlayerOrdersEvent ordersEvent) {

        try {
            setGameMap(map);
            setPlayers(players);
            setMe(me);
            setCompanies(companies);
            setPassengers(passengers);
            sendOrders = ordersEvent;

            java.util.ArrayList<Passenger> pickup = AllPickups(me, passengers);
            findPaths();

            // get the path from where we are to the dest.
            java.util.ArrayList<Point> path = CalculatePathPlus1(me, pickup.get(0).getLobby().getBusStop());
            sendOrders.invoke("ready", path, pickup);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    private Path whatNext() {
//        if (getMe().getLimo().getPassenger() == null) {
//            figureOutWhoToPickUp();
//            pickThemUp();
//            return thePath;
//        } else {
//            dropThemOff();
//            return thePath;
//        }
        return null;
    }

    private void findPaths() {
        findJunctions();
        findPathsOfTiles();
        findPathsOfJunctions();
    }

    private void costBetween(Point p1, Point p2) {

    }

    private void findJunctions() {
        junctions = new ArrayList<Point>();
        MapSquare[][] squares = privateGameMap.getSquares();
        for (int row = 0; row < privateGameMap.getHeight(); row++) {
            for (int col = 0; col < privateGameMap.getWidth(); col++) {
                MapSquare sq = squares[row][col];
                MapSquare.DIRECTION d = sq.getDirection();
                if (sq.getType() == MapSquare.TYPE.BUS_STOP || d == MapSquare.DIRECTION.INTERSECTION || d == MapSquare.DIRECTION.T_EAST ||
                        d == MapSquare.DIRECTION.T_NORTH || d == MapSquare.DIRECTION.T_WEST || d == MapSquare.DIRECTION.T_SOUTH)
                    junctions.add(new Point(row, col));
            }
        }
    }

    private boolean isJunction(Point p) {
        for (Point j : junctions) {
            if (p.equals(j)) {
                return true;
            }
        }
        return false;
    }

    private void findPathsOfTiles() {
        // store junction -> junction costs
        // j2jPaths

        HashSet<Point> explored = new HashSet<Point>();
        for (int i = 0; i < junctions.size(); i++) {
            // performBFS(i);
            Queue<Node> frontier = new LinkedList<Node>();
            Node start = new Node(junctions.get(i), null, 3.0);
            frontier.add(start);
            while (!frontier.isEmpty()) {
                Node current = frontier.poll();
                if (isJunction(current.p)) {
                    addJ2J(i, junctions.indexOf(current.p), current.getPath());
                } else {
                    for (Point neighbor : neighbors(current.p)) {
                        MapSquare sq = privateGameMap.SquareOrDefault(neighbor);
                        if (sq == null)
                            continue;
                        if (sq.getIsDriveable() && isConnected(current.p, neighbor)) {
                            double maxSpeed = 6;
                            if (isCurve(sq)) {
                                maxSpeed = 3;
                            } else if(isStop(sq)) { //todo: fix, and check direction
                                maxSpeed = 0;
                            }
                            double speed = Math.min(maxSpeed, current.speed + ACCELERATION);
                            frontier.add(new Node(neighbor, current, speed));
                        }
                    }
                }
            }
        }
    }

    private boolean isStop(MapSquare sq) {
        return sq.getStopSigns() != MapSquare.NONE;
    }

    ArrayList<MapSquare.DIRECTION> ups = new ArrayList<MapSquare.DIRECTION>(Arrays.asList(new MapSquare.DIRECTION[] {
            MapSquare.DIRECTION.T_NORTH,
            MapSquare.DIRECTION.T_EAST,
            MapSquare.DIRECTION.T_WEST,
            MapSquare.DIRECTION.INTERSECTION,
            MapSquare.DIRECTION.CURVE_NE,
            MapSquare.DIRECTION.CURVE_NW,
            MapSquare.DIRECTION.NORTH_SOUTH,
            MapSquare.DIRECTION.SOUTH_UTURN
    }));

    ArrayList<MapSquare.DIRECTION> downs = new ArrayList<MapSquare.DIRECTION>(Arrays.asList(new MapSquare.DIRECTION[] {
            MapSquare.DIRECTION.T_SOUTH,
            MapSquare.DIRECTION.T_EAST,
            MapSquare.DIRECTION.T_WEST,
            MapSquare.DIRECTION.INTERSECTION,
            MapSquare.DIRECTION.CURVE_SE,
            MapSquare.DIRECTION.CURVE_SW,
            MapSquare.DIRECTION.NORTH_SOUTH,
            MapSquare.DIRECTION.NORTH_UTURN
    }));

    ArrayList<MapSquare.DIRECTION> lefts = new ArrayList<MapSquare.DIRECTION>(Arrays.asList(new MapSquare.DIRECTION[] {
            MapSquare.DIRECTION.T_NORTH,
            MapSquare.DIRECTION.T_SOUTH,
            MapSquare.DIRECTION.T_WEST,
            MapSquare.DIRECTION.INTERSECTION,
            MapSquare.DIRECTION.CURVE_NW,
            MapSquare.DIRECTION.CURVE_SW,
            MapSquare.DIRECTION.EAST_WEST,
            MapSquare.DIRECTION.EAST_UTURN
    }));

    ArrayList<MapSquare.DIRECTION> rights = new ArrayList<MapSquare.DIRECTION>(Arrays.asList(new MapSquare.DIRECTION[] {
            MapSquare.DIRECTION.T_NORTH,
            MapSquare.DIRECTION.T_SOUTH,
            MapSquare.DIRECTION.T_EAST,
            MapSquare.DIRECTION.INTERSECTION,
            MapSquare.DIRECTION.CURVE_NE,
            MapSquare.DIRECTION.CURVE_SE,
            MapSquare.DIRECTION.EAST_WEST,
            MapSquare.DIRECTION.WEST_UTURN
    }));

    public boolean canUp(Point s1) {
        MapSquare s = privateGameMap.SquareOrDefault(s1);
        return s.getIsDriveable() && ups.contains(s.getDirection());
    }

    public boolean canDown(Point s1) {
        MapSquare s = privateGameMap.SquareOrDefault(s1);
        return s.getIsDriveable() && downs.contains(s.getDirection());
    }

    public boolean canLeft(Point s1) {
        MapSquare s = privateGameMap.SquareOrDefault(s1);
        return s.getIsDriveable() && lefts.contains(s.getDirection());
    }

    public boolean canRight(Point s1) {
        MapSquare s = privateGameMap.SquareOrDefault(s1);
        return s.getIsDriveable() && rights.contains(s.getDirection());
    }

    private boolean isConnected(Point s1, Point s2) {
        if (canUp(s1) && s2.getX() == s1.getX() && s2.getY() == s1.getY() - 1) {
            return true;
        }
        if (canDown(s1) && s2.getX() == s1.getX() && s2.getY() == s1.getY() + 1) {
            return true;
        }
        if (canLeft(s1) && s2.getX() == s1.getX() + 1 && s2.getY() == s1.getY()) {
            return true;
        }
        if (canRight(s1) && s2.getX() == s1.getX() - 1 && s2.getY() == s1.getY()) {
            return true;
        }
        return false;
    }

    private Point[] neighbors(Point p) {
        return new Point[] {new Point(p.x+1, p.y), new Point(p.x-1, p.y), new Point(p.x, p.y-1), new Point(p.x, p.y+1)};
    }

    private void addJ2J(int i, int j, Path path) {
        if (j2jPaths[i][j] == null) {
            j2jPaths[i][j] = new ArrayList<Path>();
        }
        j2jPaths[i][j].add(path);
    }

    private void findPathsOfJunctions() {
        // store destination -> destination costs
        // d2dPaths

        int[][] bestCosts;
        for (int i = 0; i < junctions.size(); i++) {
            for (int j = 0; j < junctions.size(); j++) {
                for (int k = 0; k < junctions.size(); k++) {
//                    if (bestCosts[i][k] + bestCosts[k][j] < bestCosts[i][j]) {
//                        updateBestCost(i,j);
//                    }
                }
            }
        }
    }

    /**
     * Called to send an update message to this A.I. We do NOT have to send orders in response.
     *
     * @param status     The status message.
     * @param plyrStatus The player this status is about. THIS MAY NOT BE YOU.
     * @param players    The status of all players.
     * @param passengers The status of all passengers.
     */
    public final void GameStatus(PlayerAIBase.STATUS status, Player plyrStatus, java.util.ArrayList<Player> players, java.util.ArrayList<Passenger> passengers) {

        // bugbug - Framework.cs updates the object's in this object's Players, Passengers, and Companies lists. This works fine as long
        // as this app is single threaded. However, if you create worker thread(s) or respond to multiple status messages simultaneously
        // then you need to split these out and synchronize access to the saved list objects.

        try {
            // bugbug - we return if not us because the below code is only for when we need a new path or our limo hit a bus stop.
            // if you want to act on other players arriving at bus stops, you need to remove this. But make sure you use Me, not
            // plyrStatus for the Player you are updatiing (particularly to determine what tile to start your path from).
            if (plyrStatus != getMe()) {
                return;
            }

            Point ptDest = null;
            java.util.ArrayList<Passenger> pickup = new java.util.ArrayList<Passenger>();
            switch (status) {
                case UPDATE:
                    // where's the next intersection?
                    // what are some good people to pick up?
                    // let's figure out the path to them
                    // unless we're already driving them. then let's drop them off.

                    return;
                case NO_PATH:
                case PASSENGER_NO_ACTION:
                    if (plyrStatus.getLimo().getPassenger() == null) {
                        pickup = AllPickups(plyrStatus, passengers);
                        ptDest = pickup.get(0).getLobby().getBusStop();
                    } else {
                        ptDest = plyrStatus.getLimo().getPassenger().getDestination().getBusStop();
                    }
                    break;
                case PASSENGER_DELIVERED:
                case PASSENGER_ABANDONED:
                    pickup = AllPickups(plyrStatus, passengers);
                    ptDest = pickup.get(0).getLobby().getBusStop();
                    break;
                case PASSENGER_REFUSED:
                    //add in random so no refuse loop
                    for (Company cpy : getCompanies()) {
                        if (cpy != plyrStatus.getLimo().getPassenger().getDestination()) {
                            ptDest = cpy.getBusStop();
                            break;
                        }
                    }
                    break;
                case PASSENGER_DELIVERED_AND_PICKED_UP:
                case PASSENGER_PICKED_UP:
                    pickup = AllPickups(plyrStatus, passengers);
                    ptDest = plyrStatus.getLimo().getPassenger().getDestination().getBusStop();
                    break;
                default:
                    throw new RuntimeException("unknown status");
            }

            // get the path from where we are to the dest.
            java.util.ArrayList<Point> path = CalculatePathPlus1(plyrStatus, ptDest);

            // update our saved Player to match new settings
            if (path.size() > 0) {
                getMe().getLimo().getPath().clear();
                getMe().getLimo().getPath().addAll(path);
            }
            if (pickup.size() > 0) {
                getMe().getPickUp().clear();
                getMe().getPickUp().addAll(pickup);
            }

            sendOrders.invoke("move", path, pickup);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    private java.util.ArrayList<Point> CalculatePathPlus1(Player me, Point ptDest) {
        java.util.ArrayList<Point> path = SimpleAStar.CalculatePath(getGameMap(), me.getLimo().getMapPosition(), ptDest);
        // add in leaving the bus stop so it has orders while we get the message saying it got there and are deciding what to do next.
        if (path.size() > 1) {
            path.add(path.get(path.size() - 2));
        }
        return path;
    }

    private static java.util.ArrayList<Passenger> AllPickups(Player me, Iterable<Passenger> passengers) {
        java.util.ArrayList<Passenger> pickup = new java.util.ArrayList<Passenger>();

        for (Passenger psngr : passengers) {
            if ((!me.getPassengersDelivered().contains(psngr)) && (psngr != me.getLimo().getPassenger()) && (psngr.getCar() == null) && (psngr.getLobby() != null) && (psngr.getDestination() != null))
                pickup.add(psngr);
        }

        //add sort by random so no loops for can't pickup
        return pickup;
    }

    public boolean isCurve(MapSquare s) {
        return s.getDirection() == MapSquare.DIRECTION.CURVE_NW || s.getDirection() == MapSquare.DIRECTION.CURVE_NE ||
                s.getDirection() == MapSquare.DIRECTION.CURVE_SE || s.getDirection() == MapSquare.DIRECTION.CURVE_SW;
    }

    private class Node {
        Point p;
        Node previous;
        double speed;

        public Node(Point p, Node n, double s) {
            this.p = p;
            this.previous = n;
            this.speed = s;
        }

        public Path getPath() {
            Path path = new Path();
            Node current = this;

            path.cost = 0;
            while (current != null) {
                path.points.add(0, current.p);
                current = current.previous;
                path.cost += 24.0/current.speed;
            }
            return path;
        }
    }
}