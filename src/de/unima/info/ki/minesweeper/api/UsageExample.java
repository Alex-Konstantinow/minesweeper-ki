package de.unima.info.ki.minesweeper.api;

public class UsageExample {


	public static void main(String[] args) {
		long startTime;
		long endTime;
		// use smaller numbers for larger fields
		int iterations = 10;
		int chosenField = 23;
		
		// if you want to iterate over all of them, this might help
		String[] fields = {
			"anfaenger1-9x9-10.txt",//0 Nicht geschafft -> bei 1000 Iterationen 32.5% gelöst
			"anfaenger2-9x9-10.txt",//1 Nicht geschafft -> bei 1000 Iterationen 66.5% gelöst
			"anfaenger3-9x9-10.txt",//2 geschafft 100%
			"anfaenger4-9x9-10.txt",//3 geschafft 100%
			"anfaenger5-9x9-10.txt",//4 geschafft 100%
			"baby1-3x3-0.txt",//5 geschafft 100%
			"baby2-3x3-1.txt",//6 Nicht geschafft -> bei 10000 Iterationen 67.37% gelöst
			"baby3-5x5-1.txt",//7 geschafft 100%
			"baby4-5x5-3.txt",//8 geschafft 100%
			"baby5-5x5-5.txt",//9 geschafft 100%
			"baby6-7x7-1.txt",//10 geschafft 100%
			"baby7-7x7-3.txt",//11 geschafft 100%
			"baby8-7x7-5.txt",//12 geschafft 100%
			"baby9-7x7-10.txt",//13 nicht geschafft -> bei 10000 Iterationen 50.31% gelöst
			"fortgeschrittene1-16x16-40.txt",//14 geschafft 100%
			"fortgeschrittene2-16x16-40.txt",//15 nicht geschafft -> bei 100 Iterationen 9.0% gelöst
			"fortgeschrittene3-16x16-40.txt",//16 nicht geschafft -> bei 100 Iterationen 53.0% gelöst
			"fortgeschrittene4-16x16-40.txt",//17 nicht geschafft -> bei 100 Iterationen 0.0% gelöst
			"fortgeschrittene5-16x16-40.txt",//18 nicht geschafft -> bei 100 Iterationen 0.0% gelöst
			"profi1-30x16-99.txt",//19 nicht geschafft -> bei 10 Iterationen 0.0% gelöst
			"profi2-30x16-99.txt",//20 nicht geschafft -> bei 10 Iterationen 100.0% gelöst
			"profi3-30x16-99.txt",//21 nicht geschafft -> bei 10 Iterationen 30.0% gelöst
			"profi4-30x16-99.txt",//22 nicht geschafft -> bei 10 Iterationen 0.0% gelöst
			"profi5-30x16-99.txt"//23 nicht geschafft -> bei 10 Iterationen 100% gelöst
		};
		
		int success = 0;
		startTime = System.currentTimeMillis();
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
		endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime + "ms");
		System.out.println("Erfolgsquote: " + rate + " bei " + iterations + " Wiederholungen für das feld " + fields[chosenField]);
	}

}
