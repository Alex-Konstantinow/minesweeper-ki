package de.unima.info.ki.minesweeper.api;

public class OurMSAgent extends MSAgent{

    private int[][] fieldView;
    private boolean[][] mineView;

    private boolean displayActivated = false;
    private boolean firstDecision = true;

    @Override
    public boolean solve() {
        int x, y, feedback;

        fieldView = new int[field.getNumOfCols()][field.getNumOfRows()];
        mineView = new boolean[field.getNumOfCols()][field.getNumOfRows()];

        do {
            if (displayActivated) System.out.println(field);
            if (firstDecision) {
                x = 0;
                y = 0;
                firstDecision = false;
            }
            else {
                //hier gibts noch was zu tun.
                x = bestCellChooser();
                y = bestCellChooser();
            }
            if (displayActivated) System.out.println("Uncovering (" + x + "," + y + ")");
            feedback = field.uncover(x,y);
            fieldView[x][y] = feedback;
        } while(feedback >= 0 && !field.solved());

        return false;
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
