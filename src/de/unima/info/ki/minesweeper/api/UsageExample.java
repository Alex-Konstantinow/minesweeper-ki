package de.unima.info.ki.minesweeper.api;

import java.awt.*;

public class UsageExample {


	public static void main(String[] args) {
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;
		// use smaller numbers for larger fields
		int iterations = 1000;
		int chosenField = 18;
		
		// if you want to iterate over all of them, this might help
		String[] fields = {
			"anfaenger1-9x9-10.txt",//0 Nicht geschafft -> bei 1000 Iterationen 65.6% gelöst 66.872s gebraucht
			"anfaenger2-9x9-10.txt",//1 Nicht geschafft -> bei 1000 Iterationen 77.3% gelöst 46.794s gebraucht
			"anfaenger3-9x9-10.txt",//2 geschafft -> bei 1000 Iterationen 100% 17.249s gebraucht
			"anfaenger4-9x9-10.txt",//3 geschafft -> bei 1000 Iterationen 100% 23.541s gebraucht
			"anfaenger5-9x9-10.txt",//4 geschafft -> bei 1000 Iterationen 100% 20.966s gebraucht
			"baby1-3x3-0.txt",//5 geschafft -> bei 10000 Iterationen 100% gelöst  7.363s gebraucht
			"baby2-3x3-1.txt",//6 Nicht geschafft -> bei 10000 Iterationen 66.18% gelöst 15.279s gebraucht
			"baby3-5x5-1.txt",//7 geschafft -> bei 10000 Iterationen 100% gelöst 19.978s gebraucht
			"baby4-5x5-3.txt",//8 geschafft -> bei 10000 Iterationen 100% gelöst 21.528s gebraucht
			"baby5-5x5-5.txt",//9 geschafft -> bei 10000 Iterationen 100% gelöst 33.317s gebraucht
			"baby6-7x7-1.txt",//10 geschafft -> bei 10000 Iterationen 100% gelöst 50.040s gebraucht
			"baby7-7x7-3.txt",//11 geschafft -> bei 10000 Iterationen 100% gelöst 34.872s gebraucht
			"baby8-7x7-5.txt",//12 geschafft -> bei 10000 Iterationen 100% gelöst 73.423s gebraucht
			"baby9-7x7-10.txt",//13 nicht geschafft -> bei 10000 Iterationen 50.26% gelöst 127.627s gebraucht
			"fortgeschrittene1-16x16-40.txt",//14 geschafft 100% -> bei 100 Iterationen 32.619s gebraucht
			"fortgeschrittene2-16x16-40.txt",//15 nicht geschafft -> bei 100 Iterationen 17.0% gelöst 24.897s gebraucht
			"fortgeschrittene3-16x16-40.txt",//16 nicht geschafft -> bei 1000 Iterationen 60.0% gelöst 38.712s gebraucht
			"fortgeschrittene4-16x16-40.txt",//17 nicht geschafft -> bei 1000 Iterationen 53.2% gelöst 440.445s gebraucht
			"fortgeschrittene5-16x16-40.txt",//18 nicht geschafft -> bei 1000 Iterationen 0.0% gelöst 65.027s gebraucht
			"profi1-30x16-99.txt",//19 nicht geschafft -> bei 10 Iterationen 0.0% gelöst 10.410s gebraucht
			"profi2-30x16-99.txt",//20 nicht geschafft -> bei 10 Iterationen 40.0% gelöst 59798s gebraucht
			"profi3-30x16-99.txt",//21 nicht geschafft -> bei 10 Iterationen 60.0% gelöst 45.928s gebraucht
			"profi4-30x16-99.txt",//22 nicht geschafft -> bei 10 Iterationen 0.0% gelöst 25.043s gebraucht
			"profi5-30x16-99.txt"//23 nicht geschafft -> bei 10 Iterationen 100% gelöst 64.751s
		};
		
		int success = 0;
		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			System.out.println("Durchgang: " + i);
			MSField f = new MSField("fields/" + fields[chosenField]);
			OurMSAgent agent = new OurMSAgent(f.getNumOfRows(), f.getNumOfCols());
			agent.setField(f);
			// to see what happens in the first iteration
			if (i == 1) agent.activateDisplay();
			else agent.deactivateDisplay();
			boolean solved = agent.solve();
			if (solved) {
				success++;
				neededTime += (endTime - startTime);
			}
		}
		Toolkit.getDefaultToolkit().beep();
		double rate = (double)success / (double)iterations;
		double timeMiddle = neededTime / (iterations * rate);
		endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime + "ms");
		System.out.println("Erfolgsquote: " + rate + " bei " + iterations + " Wiederholungen für das feld " + fields[chosenField]);
	}

}
