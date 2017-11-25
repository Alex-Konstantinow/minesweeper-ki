package de.unima.info.ki.minesweeper.api;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;

public class OurMSAgent extends MSAgent {

    private static final int MAX_NUMBER_OF_VARIABLES = 8;

    private static boolean initialized;
    private static HashMap<Integer, String[]> binaryCode = new HashMap<>();

    private int[][] fieldView;

    private boolean firstDecision = true;
    private boolean displayActivated;
    private int numberOfFields;

    private ArrayList<Integer> variables = new ArrayList<>();
    private HashMap<Integer, ArrayList<Integer>> neighboursOfVariables = new HashMap<>();

    private ISolver solver;

    public OurMSAgent(int row, int column) {
        initSolver(row, column);
        if (!initialized) {
            initBinaryCode();
            initialized = true;
        }
    }

    /**
     * Initializing the binayrycode Map depending on the amount of variables.
     */
    private void initBinaryCode() {
        for (int i = 1; i <= MAX_NUMBER_OF_VARIABLES; i++) {
            binaryCode.put(i, generateBinaryCode(i));
        }
    }

    /**
     * Generating all combinations of Binarycodes depending on the amount of variables.
     * @param numberOfVariables are representing the possible neighbours of a field
     * @return the binarycodes as an String Array
     */
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

    /**
     * Initializing the SAT-Solver.
     * @param rows number of rows on the field
     * @param columns number of columns of the field
     */
    private void initSolver(int rows, int columns) {
        solver = SolverFactory.newDefault();
        solver.newVar(rows * columns);
        solver.setExpectedNumberOfClauses(getExpectedTotalAmountClauses(rows, columns));
    }

    @Override
    public boolean solve() {
        int feedback = -1;
        boolean moveDone;
        initFieldView();
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
                if (!moveDone) {
                    feedback = doRandomMove();
                }
            }
        } while (feedback >= 0 && !field.solved());
        System.out.println(field);
        return field.solved();
    }

    /**
     * Initializes every cell of the field with -1 (covered).
     */
    private void initFieldView() {
        fieldView = new int[field.getNumOfCols()][field.getNumOfRows()];
        numberOfFields = field.getNumOfCols() * field.getNumOfRows();
        for (int i = 0; i < fieldView.length; i++) {
            for (int j = 0; j < fieldView[i].length; j++) {
                fieldView[i][j] = -1;
            }
        }
    }

    /**
     * Adding a negate clausel to the knowledgebase of the SAT solver to test if the clausels are satisfiable.
     * @param column column position of the move to be checked
     * @param row row position of the move to be checked
     * @return false means that the move can be done, true means that we haven´t enougth knowledge to do this move
     */
    private boolean isSatisfiable(int column, int row) {
        int[] negLiteral = {-calculateVariableNumber(column, row)};
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
            // e.printStackTrace();
        }
        return false;
    }

    /**
     * To initialize the SAT solver this method calculates the total possible amount of clauses.
     * @param row number of rows on the field
     * @param column number of columns on the field
     * @return maximum amount of clauses
     */
    private int getExpectedTotalAmountClauses(int row, int column) {
        int amountClauses = 0;

        amountClauses += 4 * Math.pow(2.0, 3.0);
        amountClauses += 2 * (row - 2) * Math.pow(2.0, 5.0);
        amountClauses += 2 * (column - 2) * Math.pow(2.0, 5.0);
        amountClauses += (row - 2) * (column - 2) * Math.pow(2.0, 8.0);
        amountClauses += row * column;
        return amountClauses;
    }

    /**
     * if the desired cell is covered we will uncover it and update the knowledgebase.
     * @param column column position of the uncovered cell
     * @param row row position of the uncovered cell
     * @return the number of bombs in neighbourhood
     */
    private int doMove(int column, int row) {
        if (fieldView[column][row] == -1) {
            fieldView[column][row] = field.uncover(column, row);
            createKNF(column, row, fieldView[column][row]);
            numberOfFields--;
        }
        return fieldView[column][row];
    }

    /**
     * Generating the KNF from an uncovered cell.
     * @param column column position of the uncovered cell
     * @param row row position of the uncovered cell
     * @param feedback number of bombs in neighbourhood of the uncovered cell
     */
    private void createKNF(int column, int row, int feedback) {
        if (feedback != -1) {
            getNeighbors(column, row);
            createTruthTable(feedback);
        }
    }

    /**
     * Calculates all uncovered neighbours of a cell.
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

    /**
     * Adding the Neighbour to the variables needed for the KNF.
     * @param column column position neighbour
     * @param row row position of the neighbour
     */
    private void addNeighbour(int column, int row) {
        if (validPosition(column, row) && fieldView[column][row] == -1) {
            variables.add(calculateVariableNumber(column, row));
        }
    }

    /**
     * Helping to calculate the variablenumber from the position on field.
     * @param column column position of the cell on field
     * @param row row position of the cell on field
     * @return variablenumber
     */
    private int calculateVariableNumber(int column, int row) {
        return column + 1 + row * field.getNumOfCols();
    }

    /**
     * In case we cant do a save move anymore we have to do a random move with the best probability.
     * @return number of bombs in neighbourhood of the uncovered cell
     */
    private int doRandomMove() {
        mapNeighboursToVariables();
        ArrayList<Integer> bestProbabilityValue = new ArrayList<>();
        double probability = 1.0;
        double newProbability;
        int cols = field.getNumOfCols();
        for (Map.Entry<Integer, ArrayList<Integer>> entry : neighboursOfVariables.entrySet()) {
            int key = entry.getKey();
            ArrayList<Integer> value = entry.getValue();
            int col = (key - 1) % cols;
            int row = (key - 1) / cols;
            newProbability = ((double) fieldView[col][row]) / value.size();
            if (0.0 < newProbability && newProbability < probability) {
                bestProbabilityValue = value;
                probability = newProbability;
            }
        }
        if(probability >= 0.5 && (double) numberOfFields / (field.getNumOfRows() * field.getNumOfCols()) >= 0.3) {
            return makeFullyRandomMove();
        } else {
            int randVar = makeDecision(bestProbabilityValue) - 1;
            int col = randVar % field.getNumOfCols();
            int row = randVar / field.getNumOfCols();
            int[] clause = {calculateVariableNumber(col, row)};
            if (fieldView[col][row] == -1) {
                fieldView[col][row] = field.uncover(col, row);
                try {
                    solver.addClause(new VecInt(clause));
                } catch (ContradictionException e) {
                    // e.printStackTrace();
                }
                createKNF(col, row, fieldView[col][row]);
                System.out.println(field);
                numberOfFields--;
            }
            return fieldView[col][row];
        }

    }

    /**
     * In case that the field position is very bad and we will do in any case a bad turn we will better try to find a
     * better position to play.
     * @return random move
     */
    private int makeFullyRandomMove(){
        Random random = new Random();
        boolean moveDone = false;
        int feedback = -1;
        while(!moveDone) {
            int col = random.nextInt(field.getNumOfCols());
            int row = random.nextInt(field.getNumOfRows());
            if (fieldView[col][row] == -1) {
                int[] clause = {calculateVariableNumber(col, row)};
                fieldView[col][row] = field.uncover(col, row);
                feedback = fieldView[col][row];
                try {
                    solver.addClause(new VecInt(clause));
                } catch (ContradictionException e) {
                    // e.printStackTrace();
                }
                createKNF(col, row, fieldView[col][row]);
                System.out.println(field);
                numberOfFields--;
                moveDone = true;
            }
        }
        return feedback;
    }

    /**
     * Generating a random turn with the potentail best chance of not finding a bomb.
     * @param value random selection is done from this List of variables
     * @return a random move
     */
    private int makeDecision(ArrayList<Integer> value) {
        Random randomVariable = new Random();
        ArrayList<Integer> possibleTurns = new ArrayList<>();
        possibleTurns.addAll(value);
        if (!possibleTurns.isEmpty()) {
            return possibleTurns.get(randomVariable.nextInt(possibleTurns.size()));
        } else {
            return -1;
        }

    }

    /**
     * Mapping Neighbours to variables.
     */
    private void mapNeighboursToVariables() {
        for (int i = 0; i < field.getNumOfCols(); i++) {
            for (int j = 0; j < field.getNumOfRows(); j++) {
                getNeighbors(i, j);
                if (fieldView[i][j] > 0 && !variables.isEmpty()) {
                    if (fieldView[i][j] != variables.size()) {
                        ArrayList<Integer> value = new ArrayList<>();
                        value.addAll(variables);
                        neighboursOfVariables.put(calculateVariableNumber(i, j), value);
                    } else {
                        flagBombs();
                    }
                }
                variables.clear();
            }
        }
    }

    /**
     * Checks if the cell is at the border.
     *
     * @param column column position of the cell on field
     * @param row row position of the cell on field
     * @return true if its not at the border
     */
    private boolean validPosition(int column, int row) {
        return (column >= 0 && column < field.getNumOfCols() && row >= 0 && row < field.getNumOfRows());
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
        if (numberOfBombs == numberOfVariables) {
            flagBombs();
        } else {
            String[] binary = binaryCode.get(numberOfVariables);
            int[] clause = new int[numberOfVariables];

            int bombCounter;
            StringBuilder strClause;

            for (int i = 0; i < binary.length; i++) {
                bombCounter = 0;
                for (int j = 0; j < numberOfVariables; j++) {
                    strClause = new StringBuilder("");
                    if (binary[i].charAt(j) == '0') {
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
        }
        variables.clear();
    }

    /**
     * In case that its only possible to have bombs in the neighbourhood the knowledge base well be updated about this
     * knowledg.
     */
    private void flagBombs() {
        int[] bombs = new int[1];
        for (int variable : variables) {
            bombs[0] = -variable;
            try {
                solver.addClause(new VecInt(bombs));
            } catch (ContradictionException e) {
                // e.printStackTrace();
            }
        }

    }

    /**
     * adding a clause to the SAT solver knowledgbase.
     * @param clause the new clause
     */
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
