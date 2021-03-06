package de.unima.info.ki.minesweeper.api;

public class UsageExample {


	public static void main(String[] args) {

		// use smaller numbers for larger fields 
		int iterations = 10000;
		
		// if you want to iterate over all of them, this might help
		String[] fields = {
			"anfaenger1-9x9-10.txt",
			"anfaenger2-9x9-10.txt",
			"anfaenger3-9x9-10.txt",
			"anfaenger4-9x9-10.txt",
			"anfaenger5-9x9-10.txt",
			"baby1-3x3-0.txt",
			"baby2-3x3-1.txt",
			"baby3-5x5-1.txt",
			"baby4-5x5-3.txt",
			"baby5-5x5-5.txt",
			"baby6-7x7-1.txt",
			"baby7-7x7-3.txt",
			"baby8-7x7-5.txt",
			"baby9-7x7-10.txt",
			"fortgeschrittene1-16x16-40.txt",
			"fortgeschrittene2-16x16-40.txt",
			"fortgeschrittene3-16x16-40.txt",
			"fortgeschrittene4-16x16-40.txt",
			"fortgeschrittene5-16x16-40.txt",
			"profi1-30x16-99.txt",
			"profi2-30x16-99.txt",
			"profi3-30x16-99.txt",
			"profi4-30x16-99.txt",
			"profi5-30x16-99.txt"
		};
		
		int success = 0;
		for (int i = 0; i < iterations; i++) {

			i++;
			MSField f = new MSField("fields/baby3-5x5-1.txt");
			RandomMSAgent agent = new RandomMSAgent();
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
		System.out.println("Erfolgsquote: " + rate);
		

		

		
	}

}
