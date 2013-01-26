package net.windward.Windwardopolis.AI;

import java.awt.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 1/26/13
 * Time: 12:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Path {
    Point start, stop;
    ArrayList<Point> points;
    int cost;

    public Path() {
        points = new ArrayList<Point>();
        cost = -1;
    }
}
