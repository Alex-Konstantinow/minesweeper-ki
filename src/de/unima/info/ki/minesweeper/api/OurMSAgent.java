package de.unima.info.ki.minesweeper.api;

import java.util.ArrayList;
import java.util.Random;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.sat4j.core.VecInt;

public class OurMSAgent extends MSAgent {

    private int[][] fieldView;
    private boolean[][] mineView;
    //private int[] viableMoves; --> RandomMove

    private boolean displayActivated = false;
    private boolean firstDecision = true;

    private ArrayList<Integer> variables = new ArrayList<>();

    private Random rand = null;

    ISolver solver;

    public OurMSAgent() {
        solver = SolverFactory.newDefault();
    }

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
        int x, y, feedback, randomMove;

        boolean secureMove = false;

        fieldView = new int[field.getNumOfCols()][field.getNumOfRows()];
        mineView = new boolean[field.getNumOfCols()][field.getNumOfRows()];
        //viableMoves = new int[field.getNumOfRows() * field.getNumOfCols()]; --> Random moves
        //this.rand = new Random(); --> Random moves

        //Initialisiert fieldView mit -1, da zu Beginn noch kein Feld aufgedeckt ist
        for (int i = 0; i < fieldView.length; i++) {
            for (int j = 0; j < fieldView[i].length; j++) {
                fieldView[i][j] = -1;
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
                //secureMove = false;
                //Geht das gesamte Feld durch und prüft, ob dieses aufgedeckt weden kann/muss
                for (int i = 0; i < field.getNumOfCols(); i++) {
                    for (int j = 0; i < field.getNumOfRows(); j++) {
                        if (validPosition(i, j) && fieldView[i][j] == -1 && !sat(i, j)) {
                            feedback = doMove(i, j);
                            secureMove = true;
                        }
                        /*int numberOfOpenNeighbor = numberOfOpenNeighbor(i, j);
                        int numberOfBombsInNeighborhood = getNumberOfBombsInNeighborhood(i, j);
                        if (fieldView[i][j] != -1 && numberOfOpenNeighbor > 0) {
                            createTruthTable(variables, fieldView[i][j] - numberOfBombsInNeighborhood);
                        }*/
                    }
                }

                //Falls nach dem Durchlauf des gesamten Feldes kein sicherer Zug gefunden werden konnte, wird hier ein zufälliger Zug ausgewählt
                if (!secureMove) {
//                    randomMove = rand.nextInt(viableMoves.length);
//                    feedback = doMove(randomMove/field.getNumOfCols(), randomMove%field.getNumOfCols());
//                    secureMove = true;
                }

                feedback = 0;

            }
            //if (displayActivated) System.out.println("Uncovering (" + x + "," + y + ")");
            //feedback = doMove(x,y);
        } while (feedback >= 0 && !field.solved());

        return false;
    }

    private boolean sat(int x, int y) {
        int[] negLiteral = {-getVariableNumber(x, y)};
        IConstr c = addToSolver(negLiteral);

        try {
            IProblem problem = solver;
            if (problem.isSatisfiable()) {
                return true;
            } else {
                return false;
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry");
        }
        solver.removeConstr(c);

        return true;
    }

    private int getVariableNumber(int x, int y) {
        return x * field.getNumOfCols() + y + 1;
    }

    private int getExpectedTotalAmountClauses(int row, int column) {
        int amountClauses = 0;

        amountClauses += 4 * Math.pow(2.0, 3.0);
        amountClauses += 2 * (row - 2) * Math.pow(2.0, 5.0);
        amountClauses += 2 * (column - 2) * Math.pow(2.0, 8.0);

        return amountClauses;
    }

    //Deckt das Feld (x,y) auf und ändert den Wert in fieldView[x][y] auf die entsprechende Anzahl an Minen, die um das Feld herum liegen
    private int doMove(int x, int y) {
        if (validPosition(x, y) && fieldView[x][y] == -1) {
            fieldView[x][y] = field.uncover(x, y);
            int[] clauses = createKNF(x, y, fieldView[x][y]);
            for (int i = 0; i < clauses.length; i++) {
                System.out.println(clauses[i] + " ");
            }
            addToSolver(clauses);
        }
        return fieldView[x][y];
    }

    private IConstr addToSolver(int[] clauses) {
        IConstr c = null;
        try {
            c = solver.addClause(new VecInt(clauses));
        } catch (ContradictionException e) {
            System.out.println("Exception: Unsatisfiable due to trivial clause");
        }
        return c;
    }

    private int[] createKNF(int x, int y, int feedback) {
        int[] clauses = new int[amountClauses(amountNeighbors(x, y), feedback)];
        if (feedback != -1) {
            for (int i = 0; i < 8; i++) {
                if (i == feedback) continue;
                else {
                    //Algorithmus, um KNF für feedback Minen im Umfeld des Feldes x,y aufzustellen
                }
            }
        }
        return clauses;
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
        if (k == 0) return 1;
        else if (k > n) return 0;
        else return binomial(n - 1, k - 1) + binomial(n - 1, k);
    }

    //Gibt ein Array zurück mit allen derzeit möglichen Zellen (RandomMove)
//    private int[] getViableMoves() {
//        viableMoves = new int[countViableCells()];
//        for(int i = 1; i<field.getNumOfRows(); i++) {
//            for(int j = 1; j<field.getNumOfCols(); j++) {
//                if(validPosition(i,j) && fieldView[i][j] == -1 && sat(i,j)) {
//                    viableMoves[i] = i*field.getNumOfCols() + j +1;
//                }
//            }
//        }
//
//        return viableMoves;
//    }

    //Zählt alle derzeit möglichen Zellen
    private int countViableCells() {
        int amountViableCells = 0;
        for (int i = 1; i < field.getNumOfRows(); i++) {
            for (int j = 1; j < field.getNumOfCols(); j++) {
                if (validPosition(i, j) && fieldView[i][j] == -1) {
                    amountViableCells++;
                }
            }
        }
        return amountViableCells;
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
    private int numberOfOpenNeighbor(int x, int y) {
        int counter = 0;
        if (validPosition(x + 1, y + 1) && fieldView[x + 1][y + 1] != -1) {
            variables.add((x + 1) * field.getNumOfCols() + (y + 1) + 1);
            counter++;
        }
        if (validPosition(x + 1, y) && fieldView[x + 1][y] != -1) {
            variables.add((x + 1) * field.getNumOfCols() + (1) + 1);
            counter++;
        }
        if (validPosition(x + 1, y - 1) && fieldView[x + 1][y - 1] != -1) {
            variables.add((x + 1) * field.getNumOfCols() + (y - 1) + 1);
            counter++;
        }
        if (validPosition(x, y + 1) && fieldView[x][y + 1] != -1) {
            variables.add((x) * field.getNumOfCols() + (y + 1) + 1);
            counter++;
        }
        if (validPosition(x, y - 1) && fieldView[x][y - 1] != -1) {
            variables.add((x) * field.getNumOfCols() + (y - 1) + 1);
            counter++;
        }
        if (validPosition(x - 1, y + 1) && fieldView[x - 1][y + 1] != -1) {
            variables.add((x - 1) * field.getNumOfCols() + (y + 1) + 1);
            counter++;
        }
        if (validPosition(x - 1, y) && fieldView[x - 1][y] != -1) {
            variables.add((x - 1) * field.getNumOfCols() + (y) + 1);
            counter++;
        }
        if (validPosition(x - 1, y - 1) && fieldView[x - 1][y - 1] != -1) {
            variables.add((x - 1) * field.getNumOfCols() + (y - 1) + 1);
            counter++;
        }

        return counter;
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
    public void createTruthTable(ArrayList<Integer> variables, int numberOfBombs) {
        int numberOfVariables = variables.size();
        String[] binaryCode = new String[(int) Math.pow(2.0, numberOfVariables)];
        int[][] truthTable = new int[(int) Math.pow(2.0, numberOfVariables)][numberOfVariables];

        /**
         * Somehow magic create solver here
         */
        for (int i = 0; i < (int) Math.pow(2.0, numberOfVariables); i++) {
            binaryCode[i] = Integer.toBinaryString(i);
            while (binaryCode[i].length() < numberOfVariables) {
                binaryCode[i] = "0" + binaryCode[i];
            }
        }
        int[] clause;
        int bombCounter;
        for (int i = 0; i < truthTable.length; i++) {
            clause = new int[numberOfVariables];
            bombCounter = 0;
            String strClause;
            for (int j = 0; j < numberOfVariables; j++) {
                truthTable[i][j] = binaryCode[i].charAt(j) == '0' ? 0 : 1;

                if (Integer.parseInt("" + truthTable[i][j]) == 0) {
                    strClause = "-" + String.valueOf(variables.get(j));
                    bombCounter++;
                } else {
                    strClause = "" + String.valueOf(variables.get(j));
                }
                clause[j] = Integer.parseInt(strClause);
            }
            if (bombCounter != numberOfBombs) {
                try {
                    solver.addClause(new VecInt(clause));
                } catch (ContradictionException e) {
                    System.out.println("Exception: Unsatisfiable due to trivial clause");
                }

            }
        }
        variables.clear();
    }
}
