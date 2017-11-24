package de.unima.info.ki.minesweeper.api;

public class UsageExample {


	public static void main(String[] args) {
		// use smaller numbers for larger fields
		int iterations = 1;
		int chosenField = 0;
		
		// if you want to iterate over all of them, this might help
		String[] fields = {
			"anfaenger1-9x9-10.txt",//Nicht geschafft
			"anfaenger2-9x9-10.txt",//Nicht geschafft
			"anfaenger3-9x9-10.txt",//geschafft
			"anfaenger4-9x9-10.txt",//geschafft
			"anfaenger5-9x9-10.txt",//geschafft
			"baby1-3x3-0.txt",//geschafft
			"baby2-3x3-1.txt",//Nicht geschafft
			"baby3-5x5-1.txt",//geschafft
			"baby4-5x5-3.txt",//geschafft
			"baby5-5x5-5.txt",//geschafft
			"baby6-7x7-1.txt",//geschafft
			"baby7-7x7-3.txt",//geschafft
			"baby8-7x7-5.txt",//geschafft
			"baby9-7x7-10.txt",//nicht geschafft
			"fortgeschrittene1-16x16-40.txt",//geschafft
			"fortgeschrittene2-16x16-40.txt",//nicht geschafft
			"fortgeschrittene3-16x16-40.txt",//nicht geschafft
			"fortgeschrittene4-16x16-40.txt",//nicht geschafft
			"fortgeschrittene5-16x16-40.txt",//nicht geschafft
			"profi1-30x16-99.txt",//nicht geschafft
			"profi2-30x16-99.txt",//nicht geschafft
			"profi3-30x16-99.txt",//nicht geschafft
			"profi4-30x16-99.txt",//nicht geschafft
			"profi5-30x16-99.txt"//nicht geschafft
		};
		
		int success = 0;
		for (int i = 0; i < iterations; i++) {
			MSField f = new MSField("fields/" + fields[chosenField]);
			//RandomMSAgent agent = new RandomMSAgent();
			OurMSAgent agent = new OurMSAgent(f.getNumOfRows(), f.getNumOfCols());
			agent.setField(f);
			// to see what happens in the first iteration
			if (i == 1) agent.activateDisplay();
			else agent.deactivateDisplay();
			boolean solved = agent.solve();
			if (solved) {
				success++;
			}
		}
		double rate = (double)success / (double)iterations;
		System.out.println("Erfolgsquote: " + rate + " bei " + iterations + " Wiederholungen für das feld " + fields[chosenField]);
	}

}
