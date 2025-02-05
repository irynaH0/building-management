package Aufgabe8;

import java.util.*;
import java.util.stream.Collectors;

public class CubeManipulation {
    private final Map<Coordinates, Integer> cubes;

    private final Set<Coordinates> candidates; // Precomputed candidates for optimization     // added

    private int minX = 0, maxX = 0, minY = 0, maxY = 0;


    // creating an empty map
    public CubeManipulation() {
        this.cubes = Map.of();
        this.candidates = new HashSet<>();  //added
    }

    //constructor with unchanged copy
    private CubeManipulation(Map<Coordinates, Integer> cubes, Set<Coordinates> candidates) {
        this.cubes = Collections.unmodifiableMap(cubes);
        this.candidates = Collections.unmodifiableSet(candidates);
    }

    /**
     * Creates a CubeArrangement with exactly 1 cube (start arrangement).
     * This is located at the ‘bottom’ (z=1) at position (0,0).
     */
    public static CubeManipulation singleCube() {
        Map<Coordinates, Integer> map = new HashMap<>();
        map.put(new Coordinates(0, 0), 1);
        Set<Coordinates> candidates = new HashSet<>(Arrays.asList(
                new Coordinates(1, 0), new Coordinates(-1, 0), new Coordinates(0, 1), new Coordinates(0, -1)
        ));
        return new CubeManipulation(map, candidates);
    }

    /**
     * Returns the height at position (x,y), or 0 if there is no cube there.
     */
    public int getHeightAt(int x, int y) {
        return cubes.getOrDefault(new Coordinates(x,y), 0);
    }

    /**
     * Returns true if (x,y) is occupied, i.e. height > 0.
     */
    public boolean hasCubeAt(int x, int y) {
        return getHeightAt(x,y) > 0;
    }

    /**
     * Returns the height at position c, or 0 if there is no cube there.
     */
    public int getHeightAt(Coordinates c) {
        return cubes.getOrDefault(c, 0);
    }

    /**
     * Returns a new arrangement in which a cube is stacked on top of (x, y).
     * is stacked on top. (If (x, y) does not yet exist, height = 1).
     * We assume that it is permissible to place a cube at (x,y).
     */
    public CubeManipulation withCubeAdded(int x, int y) {
        Map<Coordinates, Integer> newMap = new HashMap<>(this.cubes);
        Set<Coordinates> newCandidates = new HashSet<>(this.candidates);
        int oldHeight = cubes.getOrDefault(new Coordinates(x, y), 0);

        newMap.put(new Coordinates(x, y), oldHeight + 1);

        List<Coordinates> neighbors = Arrays.asList(
                new Coordinates(x + 1, y), new Coordinates(x - 1, y),
                new Coordinates(x, y + 1), new Coordinates(x, y - 1)
        );
        newCandidates.addAll(neighbors);
        newCandidates.remove(new Coordinates(x, y)); // no longer a candidate

        return new CubeManipulation(newMap, newCandidates);
    }

    /**
     * Creates all possible new arrangements in which add adds exactly one cube (if allowed).
     *
     * Admissibility rules:
     * - maxHeight must not be exceeded
     * - cube must stand ‘on the ground’ or on an existing cube
     * - structure must remain contiguous:
     * -> adjoin at least one side of an existing cube
     * or on top of an existing cube
     * - at least one face of each cube must be left free
     */
    public List<CubeManipulation> addOneCubeAllPossible(int maxHeight) {
        return candidates.stream()
                .filter(c -> getHeightAt(c.x, c.y) < maxHeight && hasAtLeastOneNeighbor(c))
                .map(c -> withCubeAdded(c.x, c.y))
                .collect(Collectors.toList());
    }

    private boolean isConnectedStructure() {
        if (cubes.isEmpty()) return true;

        // use BFS to check connectivity
        Set<Coordinates> visited = new HashSet<>();
        Queue<Coordinates> queue = new LinkedList<>();
        Coordinates start = cubes.keySet().iterator().next();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Coordinates current = queue.poll();
            List<Coordinates> neighbors = Arrays.asList(
                    new Coordinates(current.x + 1, current.y),
                    new Coordinates(current.x - 1, current.y),
                    new Coordinates(current.x, current.y + 1),
                    new Coordinates(current.x, current.y - 1)
            );
            for (Coordinates neighbor : neighbors) {
                if (cubes.containsKey(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visited.size() == cubes.size();
    }

    /**
     * Checks whether this coordinate field has an occupied neighbour (N, S, E, W).
     * (For contiguous structure.)
     */
    private boolean hasAtLeastOneNeighbor(Coordinates c) {        // maybe change
        int x = c.x;
        int y = c.y;
        return hasCubeAt(x+1, y) || hasCubeAt(x-1, y) ||
                hasCubeAt(x, y+1) || hasCubeAt(x, y-1);
    }

    /**
     * Simplified, check whether at least one side area remains ‘free’.
     * We do this by looking for at least one empty neighbour.
     */
    private boolean hasFreeSide(Coordinates c) {      // maybe change
        int x = c.x;
        int y = c.y;
        // check N, S, E, W
        boolean northFree = (getHeightAt(x, y+1) == 0);
        boolean southFree = (getHeightAt(x, y-1) == 0);
        boolean eastFree  = (getHeightAt(x+1, y) == 0);
        boolean westFree  = (getHeightAt(x-1, y) == 0);
        return (northFree || southFree || eastFree || westFree);
    }

    /**
     * ASCII representation:
     * 1) find minX, maxX, minY, maxY from cubes.
     * 2) iterate rows from maxY to minY (N->S) and columns from minX to maxX (W->O).
     * 3) height -> matching character
     */
    public String toAsciiArt() {
        if (cubes.isEmpty()) return "[empty arrangement]";

        int minX = cubes.keySet().stream().mapToInt(c->c.x).min().orElse(0);
        int maxX = cubes.keySet().stream().mapToInt(c->c.x).max().orElse(0);
        int minY = cubes.keySet().stream().mapToInt(c->c.y).min().orElse(0);
        int maxY = cubes.keySet().stream().mapToInt(c->c.y).max().orElse(0);

        StringBuilder sb = new StringBuilder();
        // from top (maxY) to bottom (minY)
        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                int h = getHeightAt(x, y);
                sb.append(heightToChar(h));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Converts height (0,1,2,...) into one character:
     * 0 -> space
     * 1..9 -> ‘1’...‘9’
     * 10 -> ‘A’, 11 -> ‘B’, etc.
     */
    private char heightToChar(int h) {
        if (h == 0) return ' ';
        if (h >= 1 && h <= 9) {
            return (char)('0' + h);
        }
        int offset = h - 10;
        return (char)('A' + offset);
    }

    /**
     * Outputs all coordinates as (x,y)->height.
     */
    @Override
    public String toString() {
        return cubes.entrySet().stream()
                .map(e -> e.getKey().toString() + "->" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    public Set<Coordinates> getAllOccupiedCoords() {
        // Wir filtern alle Einträge mit Höhe > 0
        return cubes.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)  // nur den Koordinatenschlüssel nehmen
                .collect(Collectors.toSet());
    }
}
