import model.Coordinate;
import model.House;
import model.Type;
import model.Well;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * model.Well problem
 * Inspired by the Hungarian Algorithm
 * @author Linh Le
 * @author Edgar Karapetyan
 */
public class WellProblem {

    public Well[] wells;
    public House[] houses;

    // the Hungarian Algorithm matrix
    public double[][] mat;

    int housesNumber;
    int wellsNumber;
    int N = 0;
    int K = 0;

    int coveredRows = 0;
    int coveredColumns = 0;

    int maxConnected = 0;
    int[] currentConnection;

    public WellProblem() {
        init();
    }

    public void execute() {
        calculateDistances();

        findMinAndSubtractInRows();
        findMinAndSubtractInColumns();

        coverWithLines();

        while (calculateCoveredLines() < housesNumber) { // O(N^3 * K^2)
            findUncoveredMinAndSubtract();
            clearMatrix();
            coverWithLines();
//            System.out.println(calculateCoveredLines() + "/" + housesNumber);
        }

        clearMatrix();

        int[] res = new int[housesNumber];
        Arrays.fill(res, -1);
        getOptimalSolution(0, res);
    }

    /**
     * Read input file, put them to arrays {@code wells} and {@code houses}
     * Initialize some static variables
     */
    public void init() {
        List<Well> wellList = new ArrayList<>();
        List<House> houseList = new ArrayList<>();

        try {
            File inputFile = new File("input.txt");
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);
            String str;
            String[] str_array;
            int x_cord, y_cord;
            while ((str = br.readLine()) != null) {

                if (str.matches("\\[\\d+,\\s*\\d+]")) {    // If it is a  well
                    str_array = str.split(",");
                    x_cord = Integer.parseInt(str_array[0].substring(1));
                    y_cord = Integer.parseInt(str_array[1].substring(0, str_array[1].length() - 1).trim());
                    wellsNumber++;
                    wellList.add(new Well(x_cord, y_cord));
                } else if (str.matches("\\(\\d+,\\s*\\d+\\)")) {   // If it is a house
                    str_array = str.split(",");
                    x_cord = Integer.parseInt(str_array[0].substring(1));
                    y_cord = Integer.parseInt(str_array[1].substring(0, str_array[1].length() - 1).trim());
                    housesNumber++;
                    houseList.add(new House(x_cord, y_cord));
                }
            }

            if (housesNumber > 0 && wellsNumber > 0 && housesNumber % wellsNumber != 0) {
                throw new IOException("Wrong input. The number of houses and wells are not corresponding to the requirements. Please read the readme.txt file!!!");
            }

            wells = wellList.toArray(new Well[0]);
            houses = houseList.toArray(new House[0]);

            mat = new double[housesNumber][wellsNumber];
            K = housesNumber / wellsNumber;
            // check if N, K are positive integer or not

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Write result to an output file with given format
     */
    public void writeOutput(){
        try{
            double sum = 0;
            File outputFile = new File("output.txt");
            outputFile.createNewFile();
            FileWriter fw = new FileWriter(outputFile);
            BufferedWriter bw = new BufferedWriter(fw);
            StringBuilder result;
            for (Well well : wells) {
                result = new StringBuilder("[" + well.getX() + "," + well.getY() + "] --> ");
                for (int h : well.getConnectedHouses()) {
                    result.append("(").append(houses[h].getX()).append(",").append(houses[h].getY()).append(") ");
                    sum += distance(well, houses[h]);
                }
                bw.append(result.toString()).append("\n");
            }
            bw.append("Sum : ").append(Double.toString(sum));
            bw.close();
            fw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Calculated total covered lines
     */
    private int calculateCoveredLines() {
        return  coveredRows + K * coveredColumns;
    }

    /**
     * Clear the Hungarian Matrix {@code mat}
     */
    private void clearMatrix() {
        for (int i = 0; i < housesNumber; i++) {
            houses[i].setConnectedWell(-1);
            houses[i].setMarked(false);
        }
        for (int j = 0; j < wellsNumber; j++) {
            wells[j].getConnectedHouses().clear();
            wells[j].setMarked(false);
        }

        coveredRows = coveredColumns = 0;
        maxConnected = 0;
        currentConnection = null;
    }

    /**
     * Cover rows and columns with lines
     */
    private void coverWithLines() {
        Type[][] types = new Type[housesNumber][wellsNumber];

        // If an element equals to 0, assign type for its neighbors, base on the zero value
        for (int row = 0; row < housesNumber; row++) {
            for (int col = 0; col < wellsNumber; col++) {
                if (mat[row][col] == 0) {
                    assignTypeOfNeighbors(types, row, col, zeroValue(row, col));
                }
            }
        }

        // Retrieve on each row, unmark it if that row does not contains any Type.HORIZONTAL or Type.VERTICAL
        for (int row = 0; row < housesNumber; row++) {
            boolean needToRemoved = true;
            for (int col = 0; col < wellsNumber; col++) {
                if (types[row][col] == Type.HORIZONTAL || types[row][col] == Type.VERTICAL) {
                    needToRemoved = false;
                    break;
                }
            }

            if (needToRemoved) {
                for (int col = 0; col < wellsNumber; col++) {
                    if (types[row][col] == Type.FOLLOWED) {
                        types[row][col] = null;
                    }
                }
                houses[row].setMarked(false);
                coveredRows--;
            }
        }

        // Retrieve on each column, unmark it if that column does not contains any Type.HORIZONTAL or Type.VERTICAL
        for (int col = 0; col < wellsNumber; col++) {
            boolean needToRemoved = true;
            for (int row = 0; row < housesNumber; row++) {
                if (types[row][col] == Type.HORIZONTAL || types[row][col] == Type.VERTICAL) {
                    needToRemoved = false;
                    break;
                }
            }

            if (needToRemoved) {
                for (int row = 0; row < housesNumber; row++) {
                    if (types[row][col] == Type.FOLLOWED) {
                        types[row][col] = null;
                    }
                }
                wells[col].setMarked(false);
                coveredColumns--;
            }
        }
    }

    /**
     * Calculate the maximum number of zeros at a position.
     * @param row house index
     * @param col well index
     * @return if number of zeros in column is bigger, return vertical
     * else return (-horizontal)
     */
    public int zeroValue(int row, int col) {
        int vertical = 0;
        int horizontal = 0;

        // check horizontal
        for (int i = 0; i < wellsNumber; i++) {
            if (mat[row][i] == 0)
                horizontal+=K;
        }

        // check vertical
        for (int i = 0; i < housesNumber; i++) {
            if (mat[i][col] == 0)
                vertical++;
        }

        // negative for horizontal, positive for vertical
        return vertical > horizontal ? vertical : horizontal * -1;
    }

    /**
     * Assign a type for neighbors of a zero element
     * @param types array of types
     * @param houseIdx
     * @param wellIdx
     * @param maxZeros max zeros value of that zero element
     */
    public void assignTypeOfNeighbors(Type[][] types, int houseIdx, int wellIdx, int maxZeros) {

        if (types[houseIdx][wellIdx] == Type.INTERCEPT) {
            return;
        }

        if (maxZeros > 0 && types[houseIdx][wellIdx] == Type.VERTICAL) {
            return;
        }

        if (maxZeros < 0 && types[houseIdx][wellIdx] == Type.HORIZONTAL) {
            return;
        }

        // Vertical
        if (maxZeros > 0) {
            for (int i = 0; i < housesNumber; i++) {
                if (mat[i][wellIdx] == 0) {
                    // If this neighbor already has type of HORIZONTAL, it will become INTERCEPT
                    types[i][wellIdx] = types[i][wellIdx] == Type.HORIZONTAL ? Type.INTERCEPT : Type.VERTICAL;
                } else {
                    // Non-zero neighbor only has Type.FOLLOWED
                    types[i][wellIdx] = Type.FOLLOWED;
                }
            }
            wells[wellIdx].setMarked(true);
            coveredColumns++;
        } else {
            for (int i = 0; i < wellsNumber; i++) {
                if (mat[houseIdx][i] == 0) {
                    types[houseIdx][i] = types[houseIdx][i] == Type.VERTICAL ? Type.INTERCEPT : Type.HORIZONTAL;
                } else {
                    types[houseIdx][i] = Type.FOLLOWED;
                }
            }
            houses[houseIdx].setMarked(true);
            coveredRows++;
        }

    }

    /**
     * Find minimum element in each column and subtract all elements by that min
     */
    private void findMinAndSubtractInColumns() {
        for (int j = 0; j < wellsNumber; j++) {
            // Find min in column
            double min = Double.MAX_VALUE;
            for (int i = 0; i < housesNumber; i++) {
                if (mat[i][j] < min) {
                    min = mat[i][j];
                }
            }

            // Subtract min
            for (int i = 0; i < housesNumber; i++) {
                mat[i][j] -= min;
            }
        }
    }

    /**
     * Find minimum element in each row and subtract all elements by that min
     */
    private void findMinAndSubtractInRows() {
        for (int i = 0; i < housesNumber; i++) {
            // Find min in row
            double min = Double.MAX_VALUE;
            for (int j = 0; j < wellsNumber; j++) {
                if (mat[i][j] < min) {
                    min = mat[i][j];
                }
            }

            // Subtract min
            for (int j = 0; j < wellsNumber; j++) {
                mat[i][j] -= min;
            }
        }
    }

    /**
     * Find minimum element in uncovered cells and subtract all elements by that min
     */
    private void findUncoveredMinAndSubtract() { // O(K*N^2) + O(K*N^2)
        double min = Double.MAX_VALUE;
        for (int i = 0; i < housesNumber; i++) {
            for (int j = 0; j < wellsNumber; j++) {
                if (mat[i][j] < min && !houses[i].isMarked() && !wells[j].isMarked()) {
                    min = mat[i][j];
                }
            }
        }

        for (int i = 0; i < housesNumber; i++) {
            for (int j = 0; j < wellsNumber; j++) {
                // Subtract min if uncovered
                if (!houses[i].isMarked() && !wells[j].isMarked()) {
                    mat[i][j] -= min;
                }

                // Add if this one is the interceptor of 2 marked lines
                if (houses[i].isMarked() && wells[j].isMarked()) {
                    mat[i][j] += min;
                }
            }
        }
    }

    /**
     * Calculate the distance between houses and wells
     */
    private void calculateDistances() {
        for (int i = 0; i < housesNumber; i++) {
            for (int j = 0; j < wellsNumber; j++) {
                mat[i][j] = distance(houses[i], wells[j]);
            }
        }
    }

    private double distance(Coordinate a, Coordinate b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    /**
     * Get optimal solution of current assignment
     * @param currentHouse
     * @param currentRes
     * @return array of picked well for each house
     */
    private int[] getOptimalSolution(int currentHouse, int[] currentRes) {

        if (currentRes == null) {
            return null;
        }

        if (currentHouse == housesNumber) {
            return currentRes;
        }

        for (int currentWell = 0; currentWell < wellsNumber; currentWell++) {

            int[] tempRes = currentRes.clone();
            double temDistance = mat[currentHouse][currentWell];

            if (temDistance == 0 && isConnectable(wells[currentWell], houses[currentHouse])) {
                wells[currentWell].getConnectedHouses().add(currentHouse);
                tempRes[currentHouse] = currentWell;

                tempRes = getOptimalSolution(currentHouse + 1, tempRes);
                if (tempRes != null) {
                    return tempRes;
                } else {
                    removeLastPickedHouse(wells[currentWell]);
                }
            }
        }

        return null;
    }

    /**
     * Check if {@code well} and {@code house} are connectable or not
     * @param well
     * @param house
     * @return true if connectable
     */
    private boolean isConnectable(Well well, House house) {
        return house.getConnectedWell() < 0 && well.getConnectedHouses().size() < K;
    }

    /**
     * Removed last picked house of well
     * @param well
     */
    private void removeLastPickedHouse(Well well) {
        if (well == null) {
            return;
        }
        well.getConnectedHouses().remove(well.getConnectedHouses().size() - 1);
    }
}
