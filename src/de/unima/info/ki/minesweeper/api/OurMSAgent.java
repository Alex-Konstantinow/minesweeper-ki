package de.unima.info.ki.minesweeper.api;

import java.util.ArrayList;
import java.util.Random;

//SAT4J muss noch importiert werden

public class OurMSAgent extends MSAgent{

    private int[][] fieldView;
    private boolean[][] mineView;

    private boolean displayActivated = false;
    private boolean firstDecision = true;
    private ArrayList<Integer> variables = new ArrayList<>();

    private Random rand = null;

    KnowledgeBase clauses = new KnowledgeBase();

    @Override
    public boolean solve() {
        int x, y, feedback, randomMove;

        boolean secureMove = false;

        fieldView = new int[field.getNumOfCols()][field.getNumOfRows()];
        mineView = new boolean[field.getNumOfCols()][field.getNumOfRows()];
//        viableMoves = new int[field.getNumOfRows() * field.getNumOfCols()]; //Random moves
        this.rand = new Random();

        //Initialisiert fieldView mit -1, da zu Beginn noch kein Feld aufgedeckt ist
        for(int i = 0; i<fieldView.length; i++) {
            for(int j = 0; j<fieldView[i].length; j++) {
                fieldView[i][j] = -1;
            }
        }
        do {
            if (displayActivated) System.out.println(field);
            if (firstDecision) {
                x = 0;
                y = 0;
                feedback = doMove(0,0);
                firstDecision = false;
            }
            else {
//                secureMove = false;
                //Geht das gesamte Feld durch und prüft, ob dieses aufgedeckt weden kann/muss
                for(int i = 0; i<field.getNumOfCols(); i++) {
                    for(int j = 0; i<field.getNumOfRows(); j++) {
//                        if(validPosition(i,j) && fieldView[i][j] == -1 && sat(i,j)) {
//                            feedback = doMove(i,j);
//                            secureMove = true;
//                        }
                        int numberOfNeighbors = numberOfOpenNeighbor(i,j);
                        int numberOfBombsInNeighborhood = getNumberOfBombsInNeighborhood(i, j);
                        if(fieldView[i][j] != -1 && numberOfNeighbors > 0) {
                            createTruthTable(variables, fieldView[i][j] - numberOfBombsInNeighborhood);
                        }
                    }
                }
                KnowledgeBase KB = new KnowledgeBase();
                //Falls nach dem Durchlauf des gesamten Feldes kein sicherer Zug gefunden werden konnte, wird hier ein zufälliger Zug ausgewählt
                if(!secureMove) {
//                    randomMove = rand.nextInt(viableMoves.length);
//                    feedback = doMove(randomMove/field.getNumOfCols(), randomMove%field.getNumOfCols());
//                    secureMove = true;
                }
                feedback = 0;
            }
            //if (displayActivated) System.out.println("Uncovering (" + x + "," + y + ")");
            //feedback = doMove(x,y);
        } while(feedback >= 0 && !field.solved());

        return false;
    }

    //Deckt das Feld (x,y) auf und ändert den Wert in fieldView[x][y] auf die entsprechende Anzahl an Minen, die um das Feld herum liegen
    private int doMove(int x, int y) {
        if(validPosition(x,y) && fieldView[x][y] == -1) {
            fieldView[x][y] = field.uncover(x,y);
        }
        return fieldView[x][y];
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
        for(int i = 1; i<field.getNumOfRows(); i++) {
            for(int j = 1; j<field.getNumOfCols(); j++) {
                if(validPosition(i,j) && fieldView[i][j] == -1) {
                    amountViableCells++;
                }
            }
        }
        return amountViableCells;
    }

    private int getNumberOfBombsInNeighborhood(int x, int y) {
        int counter = 0;
        if (mineView[x+1][y+1]) {
            counter++;
        }
        if (mineView[x+1][y]) {
            counter++;
        }
        if (mineView[x+1][y-1]) {
            counter++;
        }
        if (mineView[x][y+1]) {
            counter++;
        }
        if (mineView[x][y-1]) {
            counter++;
        }
        if (mineView[x-1][y+1]) {
            counter++;
        }
        if (mineView[x-1][y]) {
            counter++;
        }
        if (mineView[x-1][y-1]) {
            counter++;
        }
        return counter;
    }

    /**
     * Calculates all uncovered neighbours of an covered field.
     * @param x
     * @param y
     * @return number of varables for the truthtable
     */
    private int numberOfOpenNeighbor(int x, int y){
        int counter = 0;
        if (validPosition(x+1, y+1) && fieldView[x+1][y+1] != -1) {
            variables.add((x+1) * field.getNumOfCols() + (y+1) + 1);
            counter++;
        }
        if (validPosition(x+1, y)   && fieldView[x+1][y] != -1) {
            variables.add((x+1) * field.getNumOfCols() + (1) + 1);
            counter++;
        }
        if (validPosition(x+1, y-1) && fieldView[x+1][y-1] != -1) {
            variables.add((x+1) * field.getNumOfCols() + (y-1) + 1);
            counter++;
        }
        if (validPosition(x, y+1) && fieldView[x][y+1] != -1) {
            variables.add((x) * field.getNumOfCols() + (y+1) + 1);
            counter++;
        }
        if (validPosition(x, y-1) && fieldView[x][y-1] != -1) {
            variables.add((x) * field.getNumOfCols() + (y-1) + 1);
            counter++;
        }
        if (validPosition(x-1, y+1) && fieldView[x-1][y+1] != -1) {
            variables.add((x-1) * field.getNumOfCols() + (y+1) + 1);
            counter++;
        }
        if (validPosition(x-1, y)   && fieldView[x-1][y] != -1) {
            variables.add((x-1) * field.getNumOfCols() + (y) + 1);
            counter++;
        }
        if (validPosition(x-1, y-1) && fieldView[x-1][y-1] != -1) {
            variables.add((x-1) * field.getNumOfCols() + (y-1) + 1);
            counter++;
        }

        return counter;
    }

    /**
     * Checks if the cell is at the border.
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
        String[] binarycode = new String[(int) Math.pow(2.0, numberOfVariables)];
        int[][] truthTable = new int[(int) Math.pow(2.0, numberOfVariables)][numberOfVariables];

        /**
         * Somehow magic create solver here
         */
        for(int i = 0; i < (int) Math.pow(2.0, numberOfVariables); i++) {
            binarycode[i] = Integer.toBinaryString(i);
            while(binarycode[i].length() < numberOfVariables){
                binarycode[i] = "0" + binarycode[i];
            }
        }
        int[] clausel;
        int bombCounter;
        for(int i = 0; i < truthTable.length; i++) {
            clausel = new int[numberOfVariables];
            bombCounter = 0;
            String strClausel;
            for(int j = 0; j < numberOfVariables; j++) {
                truthTable[i][j] = binarycode[i].charAt(j) == '0' ?  0 : 1;

                if(Integer.parseInt("" + truthTable[i][j]) == 0) {
                    strClausel = "-" + String.valueOf(variables.get(j));
                    bombCounter++;
                } else {
                    strClausel = "" + String.valueOf(variables.get(j));
                }
                clausel[j] = Integer.parseInt(strClausel);
            }
            if(bombCounter != numberOfBombs) {
                // solver.addClausel(new VecInt(clausel));
            }
        }
        variables.clear();
    }
}
