package problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class represents the problem detailed in the assignment spec.
 * It contains functionality to parse the input file and load it into class
 * variable (see comments for each variable for more info).
 */
public class ProblemSpec {

    /** min and max values for car fuel level **/
    public static final int FUEL_MIN = 0;
    public static final int FUEL_MAX = 50;
    /** car move range [-4, 5] **/
    public static final int CAR_MIN_MOVE = -4;
    public static final int CAR_MAX_MOVE = 5;
    public static final int SLIP = 6;
    public static final int BREAKDOWN = 7;
    public static final int CAR_MOVE_RANGE = 12;
    /** number of different tyres **/
    public static final int NUM_TYRE_MODELS = 4;

    /** The level of the game **/
    private Level level;
    /** Discount factor **/
    private float discountFactor;
    /** Time to recover from a slip **/
    private int slipRecoveryTime;
    /** Breakdown repair time **/
    private int repairTime;
    /** The number of cells in map **/
    private int N;
    /** The maximum number of time-steps allowed for reaching goal **/
    private int maxT;
    /** Number of terrain types **/
    private int NT;
    /** The environment map as a 1D array of terrains in order **/
    private Terrain[] environmentMap;
    /** The terrain map which maps terrains to their cell indices on the
     * environment map */
    private LinkedHashMap<Terrain, List<Integer>> terrainMap;
    /** Ordering of terrain as they appear in input **/
    private List<Terrain> terrainOrder;
    /** Number of car types **/
    private int CT;
    /** Car probability mapping **/
    private LinkedHashMap<String, float[]> carMoveProbability;
    /** Ordering of car types as they appear in input **/
    private List<String> carOrder;
    /** Number of drivers **/
    private int DT;
    /** Driver to probability mapping **/
    private LinkedHashMap<String, float[]> driverMoveProbability;
    /** Ordering of drivers as they appear in input **/
    private List<String> driverOrder;
    /** Tyre model to probability mapping **/
    private LinkedHashMap<Tire, float[]> tireModelMoveProbability;
    /** Ordering of tire models as they appear in input **/
    private List<Tire> tireOrder;
    /** Fuel usage matrix
     * Size is NT rows * CT columns
     * Each row, i, represents the ith terrain type
     * Each column, j, represents the jth Car type */
    private int[][] fuelUsage;
    /** Slip probability matrix
     * Size s NT rows * CT columns
     * Each row, i, represents the ith terrain type
     * Each column, j, represents the jth Car type */
    private float[][] slipProbability;

    /**
     * Load problem spec from input file
     *
     * @param fileName path to input file
     * @throws IOException if can't find file or there is a format error
     */
    public ProblemSpec(String fileName) throws IOException {
        loadProblem(fileName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 1.
        sb.append("level: " + level.getLevelNumber() + "\n");
        // 2.
        sb.append("discount: " + discountFactor + "\n");
        sb.append("recoverTime: " + slipRecoveryTime + "\n");
        sb.append("repairTime: "+ repairTime + "\n");
        // 3.
        sb.append("N: " + N + "\n");
        sb.append("maxT: " + maxT + "\n");
        // 4.
        sb.append(environmentMap.toString()).append("\n");

        return sb.toString();
    }

    /**
     * Loads a problem from a problem text file.
     *
     * @param fileName
     *              the path of the text file to load.
     * @throws IOException
     *              if the text file doesn't exist or doesn't meet the
     *              assignment specifications.
     */
    private void loadProblem(String fileName) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(fileName));
        String line;
        String[] splitLine;
        int lineNo = 0;
        Scanner s;
        try {
            // 1. line 1
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            int levelNumber = s.nextInt();
            level = new Level(levelNumber);
            s.close();

            // 2. line 2
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            discountFactor = s.nextFloat();
            slipRecoveryTime = s.nextInt();
            repairTime = s.nextInt();
            s.close();

            // 3. line 3
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            N = s.nextInt();
            maxT = s.nextInt();
            s.close();

            // 4. line 4 to (3+NT)
            int NT = level.get_NT();
            environmentMap = new Terrain[N];
            terrainMap = new LinkedHashMap<>();
            terrainOrder = new ArrayList<>();
            for (int i = 0; i < NT; i++) {
                line = input.readLine();
                lineNo++;
                splitLine = line.split(":");
                // first part is name of terrain
                Terrain terrain = parseTerrain(splitLine[0], lineNo);
                List<Integer> terrainIndices = parseTerrainCellIndices(splitLine[1], lineNo);
                terrainMap.put(terrain, terrainIndices);
                for (Integer j: terrainIndices) {
                    environmentMap[j-1] = terrain;
                }
                terrainOrder.add(terrain);
            }

            // 5. line (3+NT+1)
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            CT = s.nextInt();
            s.close();

            // 6. line (3+NT+2) to (3+NT+2+CT)
            String car;
            carMoveProbability = new LinkedHashMap<>();
            carOrder = new ArrayList<>();
            for (int i = 0; i < CT; i++) {
                line = input.readLine();
                lineNo++;
                car = parseProbLine(line, carMoveProbability);
                carOrder.add(car);
            }

            // 7. Number of drivers line
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            DT = s.nextInt();
            s.close();

            // 8. Driver move probabilities
            String driver;
            driverMoveProbability = new LinkedHashMap<>();
            driverOrder = new ArrayList<>();
            for (int i = 0; i < DT; i++) {
                line = input.readLine();
                lineNo++;
                driver = parseProbLine(line, driverMoveProbability);
                driverOrder.add(driver);
            }

            // 9. Tyre model move probabilities
            Tire tire;
            tireModelMoveProbability = new LinkedHashMap<>();
            tireOrder = new ArrayList<>();
            for (int i = 0; i < NUM_TYRE_MODELS; i++) {
                line = input.readLine();
                lineNo++;
                tire = parseTireModelProbability(line, lineNo,
                        tireModelMoveProbability);
                tireOrder.add(tire);
            }

            // 10. Fuel usage by terrain and car matrix
            fuelUsage = new int[NT][CT];
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            for (int i = 0; i < NT; i++) {
                for (int j = 0; j < CT; j++) {
                    fuelUsage[i][j] = s.nextInt();
                }
            }
            s.close();

            // 11. Slip probability by terrain and car matrix
            slipProbability = new float[NT][CT];
            line = input.readLine();
            lineNo++;
            s = new Scanner(line);
            float rowSum;
            for (int i = 0; i < NT; i++) {
                rowSum = 0;
                for (int j = 0; j < CT; j++) {
                    slipProbability[i][j] = s.nextFloat();
                    rowSum += slipProbability[i][j];
                }
                if (Math.abs(rowSum - 1.0) > 0.001) {
                    throw new InputMismatchException("Slip probability for does not sum to one for row " + i);
                }
            }
            s.close();

        } catch (InputMismatchException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (NoSuchElementException e) {
            System.out.println("Not enough tokens on input file - line " + lineNo);
            System.exit(2);
        } catch (NullPointerException e) {
            System.out.format("Input file - line %d expected, but file ended.", lineNo);
            System.exit(3);
        } finally {
            input.close();
        }
    }

    /**
     * Parse a line of the below form and add entry to map:
     *
     *      thingName : p0 p1 p2 ... p11
     *
     * where pi represents probability of ith possible car move distance,
     * starting at -4 upto 5, then slip and breakdown.
     *
     * @param line the line text
     * @param probMap map to add the entry to
     * @return the name of thing
     */
    private String parseProbLine(String line, Map<String, float[]> probMap) {
        String[] splitLine = line.split(":");
        String thingName = splitLine[0];
        Scanner s = new Scanner(splitLine[1]);
        float[] probabilities = new float[CAR_MOVE_RANGE];
        float pSum = 0;
        for (int j = 0; j < CAR_MOVE_RANGE; j++) {
            probabilities[j] = s.nextFloat();
            pSum += probabilities[j];
        }
        probMap.put(thingName, probabilities);
        s.close();

        if (Math.abs(pSum - 1.0) > 0.001) {
            throw new InputMismatchException("Car move probability does not sum to 1.0");
        }

        return thingName;
    }

    /**
     * Parse a line of the below form and add entry to map:
     *
     *      tireModel : p0 p1 p2 ... p11
     *
     * where pi represents probability of ith possible car move distance,
     * starting at -4 upto 5, then slip and breakdown.
     *
     * @param line the line of text
     * @param lineNo the line no
     * @param probMap map to add entry to
     * @return the tire model
     */
    private Tire parseTireModelProbability(String line, int lineNo,
                                           Map<Tire, float[]> probMap) {
        String[] splitLine = line.split(":");
        Tire tireModel = parseTireModel(splitLine[0], lineNo);
        Scanner s = new Scanner(splitLine[1]);
        float[] probabilities = new float[CAR_MOVE_RANGE];
        float pSum = 0;
        for (int j = 0; j < CAR_MOVE_RANGE; j++) {
            probabilities[j] = s.nextFloat();
            pSum += probabilities[j];
        }
        probMap.put(tireModel, probabilities);
        s.close();

        if (Math.abs(pSum - 1.0) > 0.001) {
            throw new InputMismatchException("Car move probability does not sum to 1.0");
        }

        return tireModel;
    }

    private List<Integer> parseTerrainCellIndices(String indexText, int lineNo) {

        List<Integer> indices = new ArrayList<>();

        String[] splitText = indexText.split(",");
        String[] splitIndices;
        int start, end;

        for (String s: splitText) {
            splitIndices = s.split("-");

            if (splitIndices.length == 1) {
                indices.add(Integer.parseInt(splitIndices[0]));
            } else if (splitIndices.length == 2) {
                start = Integer.parseInt(splitIndices[0]);
                end = Integer.parseInt(splitIndices[1]);
                for (int i = start; i <= end; i++) {
                    indices.add(i);
                }
            }
            // else empty so no terrain of this type
        }
        return indices;
    }

    private Tire parseTireModel(String tireText, int lineNo) {
        switch (tireText) {
            case "all-terrain":
                return Tire.ALL_TERRAIN;
            case "mud":
                return Tire.MUD;
            case "low-profile":
                return Tire.LOW_PROFILE;
            case "performance":
                return Tire.PERFORMANCE;
            default:
                String errMsg = "Invalid tyre type " + tireText + "on line " + lineNo;
                throw new InputMismatchException(errMsg);
        }
    }

    private Terrain parseTerrain(String terrainText, int lineNo) {
        switch (terrainText) {
            case "dirt":
                return Terrain.DIRT;
            case "asphalt":
                return Terrain.ASPHALT;
            case "dirt-straight":
                return Terrain.DIRT_STRAIGHT;
            case "dirt-slalom":
                return Terrain.DIRT_SLALOM;
            case "asphalt-straight":
                return Terrain.ASPHALT_STRAIGHT;
            case "asphalt-slalom":
                return Terrain.ASPHALT_SLALOM;
            case "dirt-straight-hilly":
                return Terrain.DIRT_STRAIGHT_HILLY;
            case "dirt-straight-flat":
                return Terrain.DIRT_STRAIGHT_FLAT;
            case "dirt-slalom-hilly":
                return Terrain.DIRT_SLALOM_HILLY;
            case "dirt-slalom-flat":
                return Terrain.DIRT_SLALOM_FLAT;
            case "asphalt-straight-hilly":
                return Terrain.ASPHALT_STRAIGHT_HILLY;
            case "asphalt-straight-flat":
                return Terrain.ASPHALT_STRAIGHT_FLAT;
            case "asphalt-slalom-hilly":
                return Terrain.ASPHALT_SLALOM_HILLY;
            case "asphalt-slalom-flat":
                return Terrain.ASPHALT_SLALOM_FLAT;
            default:
                String errMsg = "Invalid terrain type " + terrainText + "on line " + lineNo;
                throw new InputMismatchException(errMsg);
        }
    }

    public Level getLevel() {
        return level;
    }

    public float getDiscountFactor() {
        return discountFactor;
    }

    public int getSlipRecoveryTime() {
        return slipRecoveryTime;
    }

    public int getRepairTime() {
        return repairTime;
    }

    public int getN() {
        return N;
    }

    public int getMaxT() {
        return maxT;
    }

    public int getNT() {
        return NT;
    }

    public Terrain[] getEnvironmentMap() {
        return environmentMap;
    }

    public int getCT() {
        return CT;
    }

    public int getDT() {
        return DT;
    }

    public LinkedHashMap<String, float[]> getCarMoveProbability() {
        return carMoveProbability;
    }

    public LinkedHashMap<Terrain, List<Integer>> getTerrainMap() {
        return terrainMap;
    }

    public LinkedHashMap<String, float[]> getDriverMoveProbability() {
        return driverMoveProbability;
    }

    public LinkedHashMap<Tire, float[]> getTireModelMoveProbability() {
        return tireModelMoveProbability;
    }

    public int[][] getFuelUsage() {
        return fuelUsage;
    }

    public float[][] getSlipProbability() {
        return slipProbability;
    }

    /**
     * Get the first car type in input file
     *
     * @return first car type in input file
     */
    public String getFirstCarType() {
        return carOrder.get(0);
    }

    /**
     * Get the first driver in input file
     *
     * @return first driver in input file
     */
    public String getFirstDriver() {
        return driverOrder.get(0);
    }

    /**
     * Get the first tire model in input file
     *
     * @return first tire model in input file
     */
    public Tire getFirstTireModel() {
        return tireOrder.get(0);
    }

    /**
     * Return the index of car in terms of order in which is appeared in input
     * file. This index can be used to access fuel and slip probability
     * matrices.
     *
     * @param car the car type
     * @return index of car type as it appeared in input
     */
    public int getCarIndex(String car) {
        int index = carOrder.indexOf(car);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid car type: " + car);
        }
        return index;
    }

    /**
     * Return the index of driver in terms of order in which is appeared in
     * input file.
     *
     * @param driver the driver
     * @return index of driver type as it appeared in input
     */
    public int getDriverIndex(String driver) {
        int index = driverOrder.indexOf(driver);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid driver type: " + driver);
        }
        return index;
    }

    /**
     * Return the index of terrain in terms of order in which is appeared in
     * input file.
     *
     * @param terrain the terrain
     * @return index of terrain as it appeared in input
     */
    public int getTerrainIndex(Terrain terrain) {
        int index = terrainOrder.indexOf(terrain);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid terrain: " + terrain);
        }
        return index;
    }

    /**
     * Return the index of tire Model in terms of order in which is appeared in
     * input file.
     *
     * @param tire the tire model
     * @return index of tire model as it appeared in input
     */
    public int getTireIndex(Tire tire) {
        int index = tireOrder.indexOf(tire);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid tire model: " + tire);
        }
        return index;
    }


}
