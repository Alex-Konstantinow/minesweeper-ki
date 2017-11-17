package de.unima.info.ki.minesweeper.api;

public class Clause {
    private int[] clause;

    private Clause(int literal){
        clause = new int[1];
        clause[0] = literal;
    }

    public int[] getClause() {
        return clause;
    }

    public void addLiteral(int literal) {
        int[] cache = new int[clause.length + 1];
        for(int i = 0; i < clause.length; i++){
            cache[i] = clause[i];
        }
        cache[cache.length] = literal;
        this.clause = cache;
    }

    public String toString(){
        String s = "";
        for(int literal:clause){
            s = s + literal + " ";
        }
        s = s + "0";
        return s;
    }
}
