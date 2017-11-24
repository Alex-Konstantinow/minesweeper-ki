package de.unima.info.ki.minesweeper.api;

public class UsageExample {


	public static void main(String[] args) {
		// use smaller numbers for larger fields
		int iterations = 10;
		int chosenField = 23;
		
		// if you want to iterate over all of them, this might help
		String[] fields = {
			"anfaenger1-9x9-10.txt",//0 Nicht geschafft -> bei 1000 iterationen 49.3% gelöst
			"anfaenger2-9x9-10.txt",//1 Nicht geschafft
			"anfaenger3-9x9-10.txt",//2 geschafft
			"anfaenger4-9x9-10.txt",//3 geschafft
			"anfaenger5-9x9-10.txt",//4 geschafft
			"baby1-3x3-0.txt",//5 geschafft
			"baby2-3x3-1.txt",//6 Nicht geschafft -> bei 1000 iterationen 40.9% gelöst
			"baby3-5x5-1.txt",//7 geschafft
			"baby4-5x5-3.txt",//8 geschafft
			"baby5-5x5-5.txt",//9 geschafft
			"baby6-7x7-1.txt",//10 geschafft
			"baby7-7x7-3.txt",//11 geschafft
			"baby8-7x7-5.txt",//12 geschafft
			"baby9-7x7-10.txt",//13 nicht geschafft
			"fortgeschrittene1-16x16-40.txt",//14 geschafft
			"fortgeschrittene2-16x16-40.txt",//15 nicht geschafft
			"fortgeschrittene3-16x16-40.txt",//16 nicht geschafft -> bei 1000 iterationen 19.1% gelöst
			"fortgeschrittene4-16x16-40.txt",//17 nicht geschafft -> bei 1000 iterationen 3.8% gelöst
			"fortgeschrittene5-16x16-40.txt",//18 nicht geschafft
			"profi1-30x16-99.txt",//19 nicht geschafft
			"profi2-30x16-99.txt",//20 nicht geschafft
			"profi3-30x16-99.txt",//21 nicht geschafft
			"profi4-30x16-99.txt",//22 nicht geschafft
			"profi5-30x16-99.txt"//23 nicht geschafft -> bei 10 iterationen 100% gelöst
		};
		
		int success = 0;
		for (int i = 0; i < iterations; i++) {
			MSField f = new MSField("fields/" + fields[chosenField]);
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
