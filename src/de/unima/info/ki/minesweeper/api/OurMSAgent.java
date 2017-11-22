package de.unima.info.ki.minesweeper.api;

import java.util.ArrayList;
import java.util.Random;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.sat4j.core.VecInt;

public class OurMSAgent extends MSAgent {

    private int[][] fieldView;
    private boolean[][] mineView;

    private boolean displayActivated = false;
    private boolean firstDecision = true;

    private ArrayList<Integer> variables = new ArrayList<>();

    private Random rand = null;

    ISolver solver;

    public OurMSAgent(int row, int column) {
        initSolver(row, column);
    }

    private void initSolver(int row, int column) {
        solver = SolverFactory.newDefault();
        solver.newVar(row * column);
        solver.setExpectedNumberOfClauses(getExpectedTotalAmountClauses(row, column));
    }

    @Override
    public boolean solve() {
        int x, y, randomMove;
        int feedback = -1;

        boolean secureMove = false;

        fieldView = new int[field.getNumOfCols()][field.getNumOfRows()];
        mineView = new boolean[field.getNumOfCols()][field.getNumOfRows()];

        //Initialisiert fieldView mit -1, da zu Beginn noch kein Feld aufgedeckt ist
        for (int i = 0; i < fieldView.length; i++) {
            for (int j = 0; j < fieldView[i].length; j++) {
                fieldView[i][j] = -1;
                displayActivated = true;
            }
        }
        do {
            if (displayActivated) System.out.println(field);
            if (firstDecision) {
                x = 0;
                y = 0;
                feedback = doMove(x, y);
                firstDecision = false;
            } else {
                //Geht das gesamte Feld durch und prüft, ob dieses aufgedeckt weden kann/muss
                for (int i = 0; i < field.getNumOfCols(); i++) {
                    for (int j = 0; j < field.getNumOfRows(); j++) {
                        if (validPosition(i, j) && fieldView[i][j] == -1 && !isSatisfiable(i, j)) {
                            feedback = doMove(i, j);
                             System.out.println(field);
                             System.out.println("* * * * *");
                        }
                        if (validPosition(i, j) && fieldView[i][j] == -1 && isSatisfiable(i, j)) {
                            // mineView[i][j] = true;
                            System.out.println("not today bijatch\n* * * * *");
                        }
                    }
                }
            }

        } while (feedback >= 0 && !field.solved());

        return field.solved();
    }

    private boolean isSatisfiable(int x, int y) {
        int[] negLiteral = {-calculateVariableNumber(x, y)};
        IConstr c = null;
        boolean satisfied = false;

        try {
            System.out.println(solver.nConstraints());
            c = solver.addClause(new VecInt(negLiteral));

            IProblem problem = solver;
            if (problem.isSatisfiable()) {
                satisfied = true;
            } else {
                satisfied = false;
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry");
        } catch (ContradictionException e) {
            System.out.println(field);
        }
        if(c != null) {
            solver.removeConstr(c);
        }

        return satisfied;
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
        int[] clauses = new int[amountClauses(amountNeighbors(x, y), feedback)];
        // System.out.println("Anzahl Klauseln: " + clauses.length);
        if (feedback != -1) {
                    getNeighbors(x, y);
                    createTruthTable(feedback);
        }
    }

    private int amountNeighbors(int x, int y) {
        if (x == 0 && (y == 0 || y == (field.getNumOfCols() - 1))
                || y == (field.getNumOfRows() - 1) && (x == 0 || x == field.getNumOfCols() - 1)) {
            return 3;
        } else if (x == 0 || y == 0 || x == (field.getNumOfRows() - 1) || y == (field.getNumOfCols() - 1)) {
            return 5;
        } else return 8;
    }

    private int amountClauses(int amountNeighbors, int amountMines) {
        return (int) (Math.pow(2.0, amountNeighbors) - binomial(amountNeighbors, amountMines));
    }

    private long binomial(long n, long k) {
        // System.out.println("Nachbern: " + n + ", Bomben: " + k);
        if (k == 0 || n == k || k == -1) return 1;
        else if (k > n) return 0;
        else return (n /k) * binomial(n - 1, k - 1);
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
     * @param x
     * @param y
     * @return number of varables for the truthtable
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
     * @param x
     * @param y
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
    public int[] createTruthTable(int numberOfBombs) {
        int numberOfVariables = variables.size();
        // System.out.println("Anz. Nachbarn: " + numberOfVariables);
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
                    strClause = "-" + String.valueOf(variables.get(j));

                } else {
                    strClause = "" + String.valueOf(variables.get(j));
                    bombCounter++;
                }
                clause[j] = Integer.parseInt(strClause);
            }
            if(bombCounter != numberOfBombs) {
                try {
                    if(clause.length != 0 && clause != null) {
                        // printClauses(clause);
                        solver.addClause(new VecInt(clause));
                    }
                } catch (ContradictionException e) {
                    // printClauses(clause);
                    System.out.println("Empty Constrain?!?");
                }
            }
        }
        variables.clear();
        return clause;
    }

    private void printClauses(int[] clauses) {
        System.out.print("Klauseln: ");
        for(int i = 0; i < clauses.length; i++) {
            System.out.print(clauses[i] + " ");
        }
        System.out.println();
    }
}
