package de.unima.info.ki.minesweeper.api;

public class Clause {
    private int[] clause;

    public Clause(int literal){
        clause = new int[1];
        clause[0] = literal;
    }

    public Clause(int[] clause){
        this.clause = new int[clause.length];
        for(int i = 0; i < clause.length; i++){
            this.clause[i] = clause[i];
        }
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
