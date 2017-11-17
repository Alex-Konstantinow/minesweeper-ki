package de.unima.info.ki.minesweeper.api;

//SAT4J muss noch importiert werden

import java.util.ArrayList;

public class KnowledgeBase {
    private ArrayList<Clause> clauses = new ArrayList<Clause>();

    //Diese Methode fügt eine neue Klausel zur KnowledgeBase hinzu
    public void addClause(Clause c) {
        this.clauses.add(c);
    }

    //Diese Methode gibt die Klausel mit dem Index i zurück
    public Clause getClause(int i) {

        return clauses.get(i);
    }

    public String toString(int NumberOfVariables){
        String s = "";
        s = s + "p cnf " + NumberOfVariables + " " + clauses.size();
        for(Clause c:clauses){
            s = s + "\n" + c.toString();
        }
        return s;
    }
}
