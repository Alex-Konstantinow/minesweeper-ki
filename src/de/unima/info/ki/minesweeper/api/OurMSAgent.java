package de.unima.info.ki.minesweeper.api;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;

public class OurMSAgent extends MSAgent {


    private int[][] fieldView;
    private boolean[][] mineView;

    private boolean firstDecision = true;
    private boolean displayActivated;

    private ArrayList<Integer> variables = new ArrayList<>();
    private Map<Integer, ArrayList<Integer>> neighboursOfVariables = new HashMap<>();

    private ISolver solver;

    public OurMSAgent(int row, int column) {
        initSolver(row, column);
    }

    private void initSolver(int rows, int columns) {
        solver = SolverFactory.newDefault();
        solver.newVar(rows * columns);
        solver.setExpectedNumberOfClauses(getExpectedTotalAmountClauses(rows, columns));
    }

    @Override
    public boolean solve() {
        int feedback = -1;
        boolean moveDone;

        fieldView = new int[field.getNumOfCols()][field.getNumOfRows()];
        mineView = new boolean[field.getNumOfCols()][field.getNumOfRows()];

        for (int i = 0; i < fieldView.length; i++) {
            for (int j = 0; j < fieldView[i].length; j++) {
                fieldView[i][j] = -1;

            }
        }

        do {
            moveDone = false;
            if (firstDecision) {
                feedback = doMove(0, 0);
                firstDecision = false;
            } else {
                for (int i = 0; i < field.getNumOfCols(); i++) {
                    for (int j = 0; j < field.getNumOfRows(); j++) {
                        if (validPosition(i, j) && fieldView[i][j] == -1 && !isSatisfiable(i, j)) {
                            feedback = doMove(i, j);
                            moveDone = true;

                        }
                    }
                }
                // System.out.println(field);
                if (!moveDone) {
                    int randVar = doRandomMove() - 1;
                    // System.out.println(randVar);
                    if (randVar >= 0) {
                        feedback = doMove(randVar % field.getNumOfCols(), randVar / field.getNumOfCols());
                    }
                }
            }
        } while (feedback >= 0 && !field.solved());
        System.out.println(field);
        return field.solved();
    }

    private boolean isSatisfiable(int x, int y) {
        int[] negLiteral = {-calculateVariableNumber(x, y)};
        IConstr c;
        try {
            c = solver.addClause(new VecInt(negLiteral));
            IProblem problem = solver;
            if (problem.isSatisfiable()) {
                if (c != null) {
                    solver.removeConstr(c);
                }
                return true;
            } else {
                if (c != null) {
                    solver.removeConstr(c);
                }
                return false;
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry");
        } catch (ContradictionException e) {
        }
        return false;
    }

    private int getExpectedTotalAmountClauses(int row, int column) {
        int amountClauses = 0;

        amountClauses += 4 * Math.pow(2.0, 3.0);
        amountClauses += 2 * (row - 2) * Math.pow(2.0, 5.0);
        amountClauses += 2 * (column - 2) * Math.pow(2.0, 5.0);
        amountClauses += (row - 2) * (column - 2) * Math.pow(2.0, 8.0);
        amountClauses += row * column;
        return amountClauses;
    }

    private int doMove(int x, int y) {
        if (fieldView[x][y] == -1) {
            fieldView[x][y] = field.uncover(x, y);
            createKNF(x, y, fieldView[x][y]);
        }
        return fieldView[x][y];
    }

    private void createKNF(int x, int y, int feedback) {
        if (feedback != -1) {
            getNeighbors(x, y);
            createTruthTable(feedback);
        }
    }

    /**
     * Calculates all uncovered neighbours of an covered field.
     *
     * @param x row position
     * @param y column position
     */
    private void getNeighbors(int x, int y) {
        addNeighbour(x + 1, y + 1);
        addNeighbour(x + 1, y);
        addNeighbour(x + 1, y - 1);
        addNeighbour(x, y + 1);
        addNeighbour(x, y - 1);
        addNeighbour(x - 1, y + 1);
        addNeighbour(x - 1, y);
        addNeighbour(x - 1, y - 1);
    }

    private void addNeighbour(int col, int row){
        if (validPosition(col, row) && fieldView[col][row] == -1) {
            variables.add(calculateVariableNumber(col, row));
        }
    }

    private int calculateVariableNumber(int posX, int posY) {
        return posX + 1 + posY * field.getNumOfCols();
    }

    private int doRandomMove() {
        addNeighboursToVariables();
        ArrayList<Integer> bestProbabilityValue = new ArrayList<>();
        double probability = 1.0;
        double newProbability;
        int cols = field.getNumOfCols();
        for (Map.Entry<Integer, ArrayList<Integer>> entry : neighboursOfVariables.entrySet()) {
            int key = entry.getKey();
            ArrayList<Integer> value = entry.getValue();
            int col = (key - 1) % cols;
            int row = (key - 1) / cols;
            newProbability = ((double) fieldView[col][row] - numberOfBombsDetected(col, row)) / value.size();
            if(0.0 < newProbability && newProbability < probability) {
                bestProbabilityValue = value;
                probability = newProbability;
            }
        }
        return makeDecision(bestProbabilityValue);
    }

    private int makeDecision(ArrayList<Integer> value) {
        int col;
        int row;
        Random randomVariable = new Random();
        ArrayList<Integer> possibleTurns = new ArrayList<>();
        for(int variableNumber : value) {
            col = (variableNumber - 1) % field.getNumOfCols();
            row = (variableNumber - 1) / field.getNumOfCols();
            if (!mineView[col][row]) {
                possibleTurns.add(variableNumber);
            }
        }
        if (!possibleTurns.isEmpty()) {
            return possibleTurns.get(randomVariable.nextInt(possibleTurns.size()));
        } else {

            return -1;
        }

    }

    private void addNeighboursToVariables() {
        for (int i = 0; i < field.getNumOfCols(); i++) {
            for (int j = 0; j < field.getNumOfRows(); j++) {
                getNeighbors(i, j);
                if (fieldView[i][j] > 0 && !variables.isEmpty() && fieldView[i][j] != variables.size()) {
                    ArrayList<Integer> value = new ArrayList<>();
                    value.addAll(variables);
                    neighboursOfVariables.put(calculateVariableNumber(i, j), value);
                }
                variables.clear();
            }
        }
    }

    private int numberOfBombsDetected(int col, int row) {
        int numberOfBombs = 0;
        if (validPosition(col + 1, row + 1) && mineView[col + 1][col + 1]) {
            numberOfBombs++;
        }
        if (validPosition(col + 1, row) && mineView[col + 1][row]) {
            numberOfBombs++;
        }
        if (validPosition(col + 1, row - 1) && mineView[col + 1][row - 1]) {
            numberOfBombs++;
        }
        if (validPosition(col, row + 1) && mineView[col][row + 1]) {
            numberOfBombs++;
        }
        if (validPosition(col, row - 1) && mineView[col][row - 1]) {
            numberOfBombs++;
        }
        if (validPosition(col - 1, row + 1) && mineView[col - 1][row + 1]) {
            numberOfBombs++;
        }
        if (validPosition(col - 1, row) && mineView[col - 1][row]) {
            numberOfBombs++;
        }
        if (validPosition(col - 1, row - 1) && mineView[col - 1][row - 1]) {
            numberOfBombs++;
        }
        return numberOfBombs;
    }

    /**
     * Checks if the cell is at the border.
     *
     * @param x position of row
     * @param y position of column
     * @return true if its not at the border
     */
    private boolean validPosition(int x, int y) {
        return (x >= 0 && x < field.getNumOfCols() && y >= 0 && y < field.getNumOfRows());
    }

    @Override
    public void activateDisplay() {
        this.displayActivated = true;
    }

    @Override
    public void deactivateDisplay() {
        this.displayActivated = false;
    }

    /**
     * Creates a Truthtable dynamically to the amount of varables.
     */
    private void createTruthTable(int numberOfBombs) {
        int numberOfVariables = variables.size();
        flagBombs(numberOfBombs, numberOfVariables);
        String[] binaryCode = generateBinaryCode(numberOfVariables);

        int[][] truthTable = new int[numberOfVariables][(int) Math.pow(2.0, numberOfVariables)];
        int[] clause = new int[numberOfVariables];
        int bombCounter;
        StringBuilder strClause;
        StringBuilder check;

        for (int i = 0; i < binaryCode.length; i++) {
            bombCounter = 0;
            for (int j = 0; j < numberOfVariables; j++) {
                strClause = new StringBuilder("");
                check = new StringBuilder("");
                truthTable[j][i] = binaryCode[i].charAt(j) == '0' ? 0 : 1;
                if (Integer.parseInt(check.append(truthTable[j][i]).toString()) == 0) {
                    strClause.append("-");
                    strClause.append(variables.get(j));

                } else {
                    strClause.append(variables.get(j));
                    bombCounter++;
                }
                clause[j] = Integer.parseInt(strClause.toString());
            }
            if (bombCounter != numberOfBombs) {
                addClauseToSolver(clause);
            }
        }
        variables.clear();
    }

    private void flagBombs(int numberOfBombs, int numberOfVariables){
        if (numberOfBombs == numberOfVariables) {
            for (int variable : variables) {
                mineView[(variable - 1) % field.getNumOfCols()][(variable - 1) / field.getNumOfCols()] = true;
            }
        }
    }

    private String[] generateBinaryCode(int numberOfVariables) {
        String[] binaryCode = new String[(int) Math.pow(2.0, numberOfVariables)];
        for (int i = 0; i < (int) Math.pow(2.0, numberOfVariables); i++) {
            binaryCode[i] = Integer.toBinaryString(i);
            while (binaryCode[i].length() < numberOfVariables) {
                binaryCode[i] = "0" + binaryCode[i];
            }
        }
        return binaryCode;
    }

    private void addClauseToSolver(int[] clause) {
        try {
            if (clause.length != 0) {
                solver.addClause(new VecInt(clause));
            }
        } catch (ContradictionException e) {
            System.out.println("Empty Constrain?!?");
        }
    }
}
