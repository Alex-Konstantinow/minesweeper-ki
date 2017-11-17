package de.unima.info.ki.minesweeper.api;

import java.util.Random;

//SAT4J muss noch importiert werden

public class OurMSAgent extends MSAgent{

    private int[][] fieldView;
    private boolean[][] mineView;
    private int[] viableMoves;

    private boolean displayActivated = false;
    private boolean firstDecision = true;

    private Random rand = null;

    KnowledgeBase clauses = new KnowledgeBase();

    @Override
    public boolean solve() {
        int x, y, feedback, randomMove;

        boolean secureMove = false;

        fieldView = new int[field.getNumOfCols()][field.getNumOfRows()];
        mineView = new boolean[field.getNumOfCols()][field.getNumOfRows()];
        viableMoves = new int[field.getNumOfRows() * field.getNumOfCols()];

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
                feedback = field.uncover(0,0);
                firstDecision = false;
            }
            else {
                //Geht das gesamte Feld durch und prüft, ob dieses aufgedeckt weden kann/muss
                for(int i = 0; i<field.getNumOfRows(); i++) {
                    for(int j = 0; i<field.getNumOfCols(); j++) {
                        if(validPosition(i,j) && fieldView[i][j] == -1 && sat(i,j)) {
                            feedback = uncover(i,j);
                            secureMove = true;
                        }
                    }
                }

                //Falls nach dem Durchlauf des gesamten Feldes kein sicherer Zug gefunden werden konnte, wird hier ein zufälliger Zug ausgewählt
                if(!secureMove) {
                    randomMove = rand.nextInt(viableMoves.length);
                    feedback = uncover(randomMove/field.getNumOfCols(), randomMove%field.getNumOfCols());
                    secureMove = true;
                }
                feedback = 0;
                //x = bestCellChooser();
                //y = bestCellChooser();
            }
            //if (displayActivated) System.out.println("Uncovering (" + x + "," + y + ")");
            //feedback = field.uncover(x,y);
            //fieldView[x][y] = feedback;
        } while(feedback >= 0 && !field.solved());

        return false;
    }

    //Prüft für das Feld (x,y), ob dieses satisfiable ist -> In dieser Methode kommt der SAT-Solver zum Einsatz
    private boolean sat(int x, int y) {
        return false;
    }

    //Deckt das Feld (x,y) auf und ändert den Wert in fieldView[x][y] auf die entsprechende Anzahl an Minen, die um das Feld herum liegen
    private int uncover(int x, int y) {
        if(validPosition(x,y) && fieldView[x][y] == -1) {
            fieldView[x][y] = field.uncover(x,y);
        }
        return fieldView[x][y];
    }

    //Gibt ein Array zurück mit allen derzeit möglichen Zellen
    private int[] getViableMoves() {
        viableMoves = new int[countViableCells()];
        for(int i = 1; i<field.getNumOfRows(); i++) {
            for(int j = 1; j<field.getNumOfCols(); j++) {
                if(validPosition(i,j) && fieldView[i][j] == -1 && sat(i,j)) {
                    viableMoves[i] = i*field.getNumOfCols() + j +1;
                }
            }
        }

        return viableMoves;
    }

    //Zählt alle derzeit möglichen Zellen
    private int countViableCells() {
        int amountViableCells = 0;
        for(int i = 1; i<field.getNumOfRows(); i++) {
            for(int j = 1; j<field.getNumOfCols(); j++) {
                if(validPosition(i,j) && fieldView[i][j] == -1 && sat(i,j)) {
                    amountViableCells++;
                }
            }
        }

        return amountViableCells;
    }

    // hier gibts auch noch etwas zu tun.
    private int bestCellChooser(){

        return 0;
    }

    /**
     * wenn du ein covered feld nach uncovered nachbarn abchecken willst, benutz das hier oder so.
     *
     */
    private boolean hasOpenNeighbor(int x, int y){
        int counter = 0;
        if (validPosition(x+1, y+1) && fieldView[x+1][y+1] != -1) counter++;
        if (validPosition(x+1, y)   && fieldView[x+1][y] != -1) counter++;
        if (validPosition(x+1, y-1) && fieldView[x+1][y-1] != -1) counter++;
        if (validPosition(x, y+1) && fieldView[x][y+1] != -1) counter++;
        if (validPosition(x, y-1) && fieldView[x][y-1] != -1) counter++;
        if (validPosition(x-1, y+1) && fieldView[x-1][y+1] != -1) counter++;
        if (validPosition(x-1, y)   && fieldView[x-1][y] != -1) counter++;
        if (validPosition(x-1, y-1) && fieldView[x-1][y-1] != -1) counter++;

        if(counter > 0)return true;
        return false;
    }

    private boolean validPosition(int x, int y) {
        if (x >= 0 && x < field.getNumOfCols() && y >= 0 && y < field.getNumOfRows()) return true;
        return false;
    }

    @Override
    public void activateDisplay() {

    }

    @Override
    public void deactivateDisplay() {

    }
}
