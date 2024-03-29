// SWEN30006 S1 2018
// Project Part C
// Group 99

package mycontroller.utilities;

import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.WorldSpatial.Direction;

import java.util.ArrayList;
import java.util.HashMap;

public class Utilities {
    
    private static final String LAVA = "lava";
    private static final String HEALTH = "health";

    /**
     * Logical XOR method.
     * @param b1 is the first input.
     * @param b2 is the second input.
     * @return is b1 and b2 xor'd together.
     */
    public static boolean XOR(boolean b1, boolean b2) {
        return (b1 && !b2) || (!b1 && b2);
    }

    /**
     * Returns the euclidean distance between two coordinates.
     * @param from is the 'from' coordinate.
     * @param to is the 'to' coordinate.
     * @return the euclidean distance from 'from' to 'to'.
     */
    public static float getEuclideanDistance(Coordinate from, Coordinate to) {
        return getEuclideanDistance(from.x, from.y, to.x, to.y);
    }

    /**
     * Given float coordinates for two points, determines the euclidean distance between them.
     * @param x1 is the x coordinate for point 1.
     * @param y1 is the y coordinate for point 1.
     * @param x2 is the x coordinate for point 2.
     * @param y2 is the y coordinate for point 2.
     * @return the euclidean distance between the two given points.
     */
    public static float getEuclideanDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.pow(Math.abs(Math.pow(x1 - x2, 2)) + Math.pow(y1 - y2, 2), 0.5);
    }

    /**
     * Returns the manhattan distance between two coordinates.
     * @param from is the 'from' coordinate.
     * @param to is the 'to' coordinate.
     * @return the manhattan distance from 'from' to 'to'.
     */
    public static float getManhattanDistance(Coordinate from, Coordinate to) {
        return (float) (Math.abs(to.x - from.x) + Math.abs(to.y - from.y));
    }

    /**
     * Given two consecutive coordinates, returns the direction from 'from' to 'to'.
     * @param from is the 'from' coordinate.
     * @param to is the 'to' coordinate.
     * @return the direction from 'from' to 'to'.
     */
    public static Direction getRelativeDirection(Coordinate from, Coordinate to) {
        // They must be either vertical or horizontal from one another.
        assert (Utilities.XOR(from.x == to.x, from.y == to.y));

        final int xDisplacement = to.x - from.x;
        final int yDisplacement = to.y - from.y;

        if (xDisplacement > 0) {
            return Direction.EAST;
        } else if (xDisplacement < 0) {
            return Direction.WEST;
        } else if (yDisplacement > 0) {
            return Direction.NORTH;
        } else if (yDisplacement < 0) {
            return Direction.SOUTH;
        }

        // This shouldn't happen, due to the assert statement at the beginning.
        return null;
    }

    /**
     * Given an x and y position as floats, returns it as a coordinate position.
     * @param x is the x position.
     * @param y is the y position.
     * @return the coordinate for the (x, y) position.
     */
    public static Coordinate getCoordinatePosition(float x, float y) {
        return new Coordinate(Math.round(x), Math.round(y));
    }

    /**
     * Gets the coordinate behind the car, based on its orientation. It is passed to 'AStar' as the position that the
     * car was in previous to its current position. This isn't always true, but it's true most of the time and causes
     * the intended effect in 'AStar'.
     * @return the coordinate behind the car.
     */
    public static Coordinate getBehindCoordinate(Coordinate coordinate, Direction orientation) {
        if (orientation == Direction.EAST) {
            return new Coordinate(coordinate.x - 1, coordinate.y);
        } else if (orientation == Direction.NORTH) {
            return new Coordinate(coordinate.x, coordinate.y - 1);
        } else if (orientation == Direction.WEST) {
            return new Coordinate(coordinate.x + 1, coordinate.y);
        } else { // If facing south.
            return new Coordinate(coordinate.x, coordinate.y + 1);
        }
    }

    /**
     * Determines whether the give coordinate is a lava tile.
     * @param map is the map to check with
     * @param coordinate is the coordinate to be checked.
     * @return whether the given coordinate is a lava tile according to the given map.
     */
    public static boolean isLava(HashMap<Coordinate, MapTile> map, Coordinate coordinate) {
        MapTile mapTile = map.get(coordinate);
        if (mapTile != null && mapTile.isType(MapTile.Type.TRAP)) {
            TrapTile trapTile = (TrapTile) mapTile;

            return trapTile.getTrap().equals(LAVA);
        }

        return false;
    }

    /**
     * Determines whether the give coordinate is a health tile.
     * @param map is the map to check with
     * @param coordinate is the coordinate to be checked.
     * @return whether the given coordinate is a health tile according to the given map.
     */
    public static boolean isHealth(HashMap<Coordinate, MapTile> map, Coordinate coordinate) {
        MapTile mapTile = map.get(coordinate);
        if (mapTile != null && mapTile.isType(MapTile.Type.TRAP)) {
            TrapTile trapTile = (TrapTile) mapTile;

            return trapTile.getTrap().equals(HEALTH);
        }

        return false;
    }

    /**
     * Counts the number of lava tiles in a given path.
     * @param map is the map to check against.
     * @param path is the list of coordinates to check.
     * @return the number lava tiles in the path.
     */
    public static int getLavaCount(HashMap<Coordinate, MapTile> map, ArrayList<Coordinate> path) {
        if (path == null) {
            return 0;
        }

        int numLavaTiles = 0;

        for (Coordinate coordinate : path) {
            if (isLava(map, coordinate)) {
                numLavaTiles++;
            }
        }

        return numLavaTiles;
    }
}
