package de.unima.info.ki.minesweeper.api;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.ArrayList;
import java.util.logging.Logger;

public class OurMSAgent extends MSAgent {

    private static final Logger log = Logger.getLogger(OurMSAgent.class.getName());

    private int[][] fieldView;
    private boolean[][] mineView;

    private boolean displayActivated = false;
    private boolean firstDecision = true;

    private ArrayList<Integer> variables = new ArrayList<>();

    ISolver solver;

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

        displayActivated = true;

        do {
            moveDone = false;
            if (displayActivated) System.out.println(field);
            if (firstDecision) {
                feedback = doMove(0, 0);
                firstDecision = false;
            } else {
                for (int i = 0; i < field.getNumOfCols(); i++) {
                    for (int j = 0; j < field.getNumOfRows(); j++) {
                        if (validPosition(i, j) && fieldView[i][j] == -1 && !isSatisfiable(i, j)) {
                            feedback = doMove(i, j);
                            mineView[i][j] = false;
                            moveDone = true;
                        }
                        if (validPosition(i, j) && fieldView[i][j] == -1 && isSatisfiable(i, j)) {
                            mineView[i][j] = true;
                        }
                    }
                }
                if (!moveDone) {
                    System.out.println(field);
                    // doRandomMove
                    break;
                }
            }
        } while (feedback >= 0 && !field.solved());
        return field.solved();
    }

    private boolean isSatisfiable(int x, int y) {
        int[] negLiteral = {-calculateVariableNumber(x, y)};
        IConstr c = null;

        try {
            c = solver.addClause(new VecInt(negLiteral));

            IProblem problem = solver;
            if (problem.isSatisfiable()) {
                solver.removeConstr(c);
                return true;
            } else {
                solver.removeConstr(c);
                return false;
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry");
        } catch (ContradictionException e) {
            System.out.println(field);
        }
        if (c != null) {
            solver.removeConstr(c);
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

    //Deckt das Feld (x,y) auf und ändert den Wert in fieldView[x][y] auf die entsprechende Anzahl an Minen, die um das Feld herum liegen
    private int doMove(int x, int y) {
        if (validPosition(x, y) && fieldView[x][y] == -1) {
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

    private int getNumberOfBombsInNeighborhood(int x, int y) {
        int counter = 0;
        if (mineView[x + 1][y + 1]) {
            counter++;
        }
        if (mineView[x + 1][y]) {
            counter++;
        }
        if (mineView[x + 1][y - 1]) {
            counter++;
        }
        if (mineView[x][y + 1]) {
            counter++;
        }
        if (mineView[x][y - 1]) {
            counter++;
        }
        if (mineView[x - 1][y + 1]) {
            counter++;
        }
        if (mineView[x - 1][y]) {
            counter++;
        }
        if (mineView[x - 1][y - 1]) {
            counter++;
        }
        return counter;
    }

    /**
     * Calculates all uncovered neighbours of an covered field.
     *
     * @param x row position
     * @param y column position
     */
    private void getNeighbors(int x, int y) {
        if (validPosition(x + 1, y + 1) && fieldView[x + 1][y + 1] == -1) {
            variables.add(calculateVariableNumber(x + 1, y + 1));
        }
        if (validPosition(x + 1, y) && fieldView[x + 1][y] == -1) {
            variables.add(calculateVariableNumber(x + 1, y));
        }
        if (validPosition(x + 1, y - 1) && fieldView[x + 1][y - 1] == -1) {
            variables.add(calculateVariableNumber(x + 1, y - 1));
        }
        if (validPosition(x, y + 1) && fieldView[x][y + 1] == -1) {
            variables.add(calculateVariableNumber(x, y + 1));
        }
        if (validPosition(x, y - 1) && fieldView[x][y - 1] == -1) {
            variables.add(calculateVariableNumber(x, y - 1));
        }
        if (validPosition(x - 1, y + 1) && fieldView[x - 1][y + 1] == -1) {
            variables.add(calculateVariableNumber(x - 1, y + 1));
        }
        if (validPosition(x - 1, y) && fieldView[x - 1][y] == -1) {
            variables.add(calculateVariableNumber(x - 1, y));
        }
        if (validPosition(x - 1, y - 1) && fieldView[x - 1][y - 1] == -1) {
            variables.add(calculateVariableNumber(x - 1, y - 1));
        }
    }

    private int calculateVariableNumber(int posX, int posY) {
        return posX + 1 + posY * field.getNumOfCols();
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
    private int[] createTruthTable(int numberOfBombs) {
        int numberOfVariables = variables.size();
        String[] binaryCode = new String[(int) Math.pow(2.0, numberOfVariables)];
        int[][] truthTable = new int[numberOfVariables][(int) Math.pow(2.0, numberOfVariables)];

        for (int i = 0; i < (int) Math.pow(2.0, numberOfVariables); i++) {
            binaryCode[i] = Integer.toBinaryString(i);
            while (binaryCode[i].length() < numberOfVariables) {
                binaryCode[i] = "0" + binaryCode[i];
            }
        }

        int[] clause = new int[numberOfVariables];
        int bombCounter;
        for (int i = 0; i < binaryCode.length; i++) {
            bombCounter = 0;
            String strClause;
            for (int j = 0; j < numberOfVariables; j++) {
                truthTable[j][i] = binaryCode[i].charAt(j) == '0' ? 0 : 1;
                if (Integer.parseInt("" + truthTable[j][i]) == 0 /*|| mineView[(variables.get(j) - 1) / field.getNumOfCols()][(variables.get(j) - 1) % field.getNumOfCols()]*/) {
                    strClause = "-" + variables.get(j);

                } else {
                    strClause = "" + variables.get(j);
                    bombCounter++;
                }
                clause[j] = Integer.parseInt(strClause);
            }
            if (bombCounter != numberOfBombs) {
                try {
                    if (clause.length != 0) {
                        solver.addClause(new VecInt(clause));
                    }
                } catch (ContradictionException e) {
                    System.out.println("Empty Constrain?!?");
                }
            }
        }
        variables.clear();
        return clause;
    }
}
