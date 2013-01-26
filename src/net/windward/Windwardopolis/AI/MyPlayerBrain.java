package net.windward.Windwardopolis.AI;


// Created by Windward Studios, Inc. (www.windward.net). No copyright claimed - do anything you want with this code.


import net.windward.Windwardopolis.api.Company;
import net.windward.Windwardopolis.api.Map;
import net.windward.Windwardopolis.api.Passenger;
import net.windward.Windwardopolis.api.Player;

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
    private Path[][][] j2jPaths; // junction to junction paths of tiles [start][end] gives list of paths
    private Path[][][] d2dPaths; // destination to destination paths of junctions [start][end] gives list of paths

    private ArrayList<Passenger> myPickup;
    private ArrayList<Point> myPath;

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
        if (getMe().getLimo().getPassenger() == null) {
            figureOutWhoToPickUp();
            pickThemUp();
            return thePath;
        } else {
            dropThemOff();
            return thePath;
        }
    }

    private void findPaths() {
        findPathsOfTiles();
        findPathsOfJunctions();
    }


    private void findPathsOfTiles() {
        // store junction -> junction costs
        // j2jPaths

        ArrayList<Point> junctions;

        for (int i = 0; i < junctions.size(); i++) {
            // performBFS(i);
            Queue<Point> frontier = new Queue<Point>();
            frontier.add(junctions.get(i));
            while (!frontier.isEmpty()) {
                Point current = frontier.poll();
                if (isJunction(current)) {
                    addPathToJunction;
                } else {
                    
                }
            }
        }
    }

    private void findPathsOfJunctions() {
        // store destination -> destination costs
        // d2dPaths

        ArrayList<Point> junctions;

        int[][] bestCosts;
        for (int i = 0; i < junctions.size(); i++) {
            for (int j = 0; j < junctions.size(); j++) {
                for (int k = 0; k < junctions.size(); k++) {
                    if (bestCosts[i][k] + bestCosts[k][j] < bestCosts[i][j]) {
                        updateBestCost(i,j);
                    }
                }
            }
        }
    }

    private Passenger nextToPickup;

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

        // where's the next intersection?
        // what are some good people to pick up?
        // let's figure out the path to them
        // unless we're already driving them. then let's drop them off.


        try {
            // bugbug - But make sure you use Me, not
            // plyrStatus for the Player you are updating (particularly to determine what tile to start your path from).

            Point ptDest = null;
            java.util.ArrayList<Passenger> pickup = new java.util.ArrayList<Passenger>();
            switch (status) {
                case UPDATE:
                    // if the one we're looking for got picked up, then reschedule
                    if (nextToPickup.getCar() != null) {
                        pickup = AllPickups(plyrStatus, passengers);
                        nextToPickup = pickup.get(0);
                         ptDest = pickup.get(0).getLobby().getBusStop();
                        break;
                    }
                    else return;
                case NO_PATH:
                case PASSENGER_NO_ACTION:
                    if (plyrStatus.getLimo().getPassenger() == null) {
                        pickup = AllPickups(plyrStatus, passengers);
                        nextToPickup = pickup.get(0);
                        ptDest = pickup.get(0).getLobby().getBusStop();
                    } else { // this should probably be dump at the enarest spot
                        ptDest = plyrStatus.getLimo().getPassenger().getDestination().getBusStop();
                    }
                    break;
                case PASSENGER_DELIVERED:
                case PASSENGER_ABANDONED:
                    pickup = AllPickups(plyrStatus, passengers);
                    nextToPickup = pickup.get(0);
                    ptDest = pickup.get(0).getLobby().getBusStop();
                    break;
                case PASSENGER_REFUSED:
                    //add in random so no refuse loop
                    double minCost = Double.POSITIVE_INFINITY;
                    Company minCpy = null;
                    cmp: for (Company cpy : getCompanies()) {
                        if (cpy != getMe().getLimo().getPassenger().getDestination() && costBetween(getMe().getLimo().getMapPosition(), cpy.getBusStop()) < minCost) {
                            // make sure no enemies there
                            for (Passenger p : cpy.getPassengers()) {
                                if (getMe().getLimo().getPassenger().getEnemies().contains(p)) {
                                    continue cmp;
                                }
                            }
                            minCost = costBetween(getMe().getLimo().getMapPosition(), cpy.getBusStop());
                            minCpy = cpy;
                        }
                    }
                    ptDest = minCpy.getBusStop();
                    break;
                case PASSENGER_DELIVERED_AND_PICKED_UP:
                case PASSENGER_PICKED_UP:
                    pickup = AllPickups(plyrStatus, passengers);
                    nextToPickup = pickup.get(0);
                    ptDest = getMe().getLimo().getPassenger().getDestination().getBusStop();
                    break;
                default:
                    throw new RuntimeException("unknown status");
            }

            // get the path from where we are to the dest.
            java.util.ArrayList<Point> path = CalculatePathPlus1(getMe(), ptDest);

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

    private static class PsngrComp implements Comparator<Passenger>{
        Player me;

        public PsngrComp(Player iAmI) {
            me = iAmI;
        }

        private int doMath(int distPickupA, int distPickupB, int distDestA, int distDestB, int valA, int valB, int enemyA, int enemyATravel, int enemyB, int enemyBTravel) {
            final double VALSCALAR = .1;
            final double ENEMYSCALAR = -.5;
            final double ENEMYTRAVELSCALAR = -.2;
            double aTotes = (distPickupA + distDestA) * (VALSCALAR*valA + ENEMYSCALAR*enemyA + ENEMYTRAVELSCALAR*enemyATravel);
            double bTotes = (distPickupB + distDestB) * (VALSCALAR*valB + ENEMYSCALAR*enemyB + ENEMYTRAVELSCALAR*enemyBTravel);
            if (aTotes > bTotes)
                return 1;
            else if (aTotes < bTotes)
                return -1;
            else
                return 0;
        }


        public int compare(Passenger a, Passenger b) {
            // distance to pickup
            Point myPos = me.getLimo().getMapPosition();
            Point pickupA = a.getLobby().getBusStop();
            Point pickupB = b.getLobby().getBusStop();
            int distPickupA = costBetween(myPos, pickupA);
            int distPickupB = costBetween(myPos, pickupB);
            // distance from pickup to destination
            Point destA = a.getDestination().getBusStop();
            Point destB = b.getDestination().getBusStop();
            int distDestA = costBetween(pickupA, destA);
            int distDestB = costBetween(pickupB, destB);
            // value of the passenger
            int valA = a.getPointsDelivered();
            int valB = b.getPointsDelivered();
            // is there currently an enemy at destination enemy travelling with goal of same destination?
            int enemyA = 0, enemyATravel = 0;
            int enemyB = 0, enemyBTravel = 0;
            for (Passenger e : a.getEnemies())
            {
                if (e.getLobby() != null && e.getLobby().getBusStop().equals(destA))
                    enemyA = 1;
                else if (e.getCar() != null && e.getDestination().equals(destA))
                    enemyATravel = 1;
                if (enemyA == 1 && enemyATravel == 1)
                    break;
            }
            for (Passenger e : b.getEnemies())
            {
                if (e.getLobby() != null && e.getLobby().getBusStop().equals(destB))
                    enemyB = 1;
                else if (e.getCar() != null && e.getDestination().equals(destB))
                    enemyBTravel = 1;
                if (enemyB == 1 && enemyBTravel == 1)
                    break;
            }
            return doMath(distPickupA, distPickupB, distDestA, distDestB, valA, valB, enemyA, enemyATravel, enemyB, enemyBTravel);
        }
    }

    private static java.util.ArrayList<Passenger> AllPickups(Player me, Iterable<Passenger> passengers) {
        java.util.ArrayList<Passenger> pickup = new java.util.ArrayList<Passenger>();
        java.util.PriorityQueue<Passenger> pq = new PriorityQueue<Passenger>(12, new PsngrComp(me));

        for (Passenger psngr : passengers) {
            if ((!me.getPassengersDelivered().contains(psngr)) && (psngr != me.getLimo().getPassenger()) && (psngr.getCar() == null) && (psngr.getLobby() != null) && (psngr.getDestination() != null))
                pq.add(psngr);
        }

        while(!pq.isEmpty())
            pickup.add(pq.poll());

        if (pickup.isEmpty()) {
            for (Passenger psngr : passengers) {
                if (!me.getPassengersDelivered().contains(psngr) && (psngr != me.getLimo().getPassenger()) && psngr.getCar() != null) {
                    pickup.add(psngr);
                    break;
                }
            }
        }

        return pickup;
    }
}