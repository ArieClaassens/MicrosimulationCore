package uk.ac.leeds.ccg.modeling.microsim;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
 * This is a toy application that does a very basic population redistribution for microsimulation.<P>
 * It's a kind of cut down version of the SimLeeds reweighter classes by 
 * <A href="http://www.geog.leeds.ac.uk/people/d.ballas/>Dimitris Ballas</A>. The idea 
 * is to take a small set of people with an attribute, and a set of areas with aggregate 
 * statistics about that attribute, and put enough people into each area to get the right 
 * statistics. For example, say we have a small sample of people, and we know their gender. 
 * We also have census areas, for which we have total numbers of men and women. We want 
 * to use this small sample of people to generate a convincing population of people for 
 * each census area, such that the amounts of each gender match the statistics for each area 
 * we have. We want to randomly pick from our sample of people and dump each random person 
 * into an area, and keep doing that until the stats are correct, using each person multiple 
 * times if necessary. The problem is getting the stats to match. This program uses a 
 * method called Simulated Annealing (SA).
 * @author <A href="http://www.geog.leeds.ac.uk/people/a.evans/">Andy Evans</A>
 * @version 1.0
 */
public class Distributor extends Frame {
    
    // Variables used throughout the code. They're up here so all the code can see them.
    
    private TextArea messageBox = null;		// An area on the user interface for messages.
    private Vector[] world = null;		// A representation of the world. Has "areas" filled with "people".
    private int numberOfAreas = 0;		// The number of areas we're trying to redistribute people into.
    private Table tableToReplicate = null;	// The statistics table we'd like to replicate.
    private Table currentTable = null;		// The statistics table representing our world at any given moment in the process.
    private MicroData microData = null;		// The sample of people we have to work with.
    private int maxRuns = 2;			// The maximum attempts at getting each area right before giving up.
    private int errorMargin = 0;		// If the error falls below this for any area, we stop for that area and don't keep going until maxRuns.
    private int maxTemperature = 20;		// The maximum temperature for the Simulated Annealing.
    private int temperatureConversion = 5;	// Alters rate of temperature change each Simulated Annealing iteration.
    
    
    /** 
     * Creates a new instance of Distributor.<P>
     * Sets up a little GUI first that asks you to pick the table to 
     * replicate and then the sample of people. 
     */
    public Distributor() {
	
	// Set up GUI.
	
	messageBox = new TextArea("",20,20,TextArea.SCROLLBARS_BOTH);
	add(messageBox);
	setLocation(200,200);
	setSize(300,300);
	
	// The next bit of code sets up an anonymous inner class that just handles shutting down.
	// when the X-icon on the frame is hit.
	
	addWindowListener(new WindowAdapter(){
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	
	// Show the GUI.
	
	setVisible(true);
	
	// Get the user to pick the data files.
	
	readData();
	
	// Start by randomly filling the areas without thinking about the statistics.
	
	randomizeStartingDistribution();
	
	// Redistribute until the statistics are correct.
	
	redistribute();
	
	// Write the people with their area ids to a file.
	
	write();
	
    } // End of Constructor.
    
    
    
    
    
    /**
     * Asks the user to pick two files.<P>
     * The user is first asked to select a file containing the statistics data to be 
     * replicated. The format should be comma separated, with a header line. Each row 
     * should contain the statistics for an area, in the format:<BR>
     * <CODE>Name, Value0, Value1</CODE><BR>
     * Currently this code only copes with two statistics relating to the the same variable, 
     * for example: 
     * <CODE>EDName, NumberOfFemales, NumberOfMales</CODE><P>
     * The user is then asked to pick a file containing the individuals (MicroData) 
     * we shall use to try and replicate these statistics. The file should 
     * be in the form:<BR>
     * <CODE>Name, Value</CODE><BR>
     * Currently the value should be either zero or one, depending on whether the 
     * person should fall in the Value0 or Value1 column (above) respectively.
     **/
    private void readData () {
	
	// Open a suitable dialog. The program waits after this is setVisible until 
	// the user picks something.
	
	FileDialog openDialog = new FileDialog(this, "Pick a Table to replicate", FileDialog.LOAD);
	openDialog.setVisible(true);
	
	// Check they've picked something sensible - if not (for example, they've pushed 
	// 'Cancel', then shut down the program.
	
	if ((openDialog.getDirectory() == null) || (openDialog.getFile() == null)) {
	    System.exit(0);
	} 
	
	// Turn the tableToReplicate object into a new, filled, table using 
	// the Table constructor that reads in files.
	
	tableToReplicate = new Table(openDialog.getDirectory()+openDialog.getFile());
	
	// Use the Table.toString method to print out to the user what we've read in.
	
	messageBox.setText(tableToReplicate.toString());
        
	numberOfAreas = tableToReplicate.getNumberOfAreas(); 
	
	// Do the same for the people in the sample file.
	
	openDialog = new FileDialog(this, "Pick a set of MicroData", FileDialog.LOAD);
	openDialog.setVisible(true);
	
	if ((openDialog.getDirectory() == null) || (openDialog.getFile() == null)) {
	    System.exit(0);
	} 
	
	microData = new MicroData(openDialog.getDirectory()+openDialog.getFile());
	messageBox.append(microData.toString());
	
    } // End of readData.
    
    
    
    
    
    /**
     * Start by randomly filling the areas without thinking about the statistics.<P>
     **/
    private void randomizeStartingDistribution() {
	
	// Make the world anew, with the correct number of areas in it we want.
	
	world = new Vector[numberOfAreas];
	
	// For each area in the world, find out what the total population is 
	// and fill it with that number of people drawn randomly from our sample.
	
	for (int area = 0; area < numberOfAreas; area++) {
	    world[area] = new Vector(0);
	    for (int person = 0; person < tableToReplicate.getTotalAreaPopulation(area); person++) {
		world[area].addElement(getRandomPerson());
	    }
	}
	
	// Make a suitably sized Table so we can calculate the current statistics for 
	// our brave new world, then calcuate each area's statistics. Each area is a 
	// row in our table, just like the one we're trying to replicate.
	
	currentTable = new Table(numberOfAreas);
	
	for (int area = 0; area < numberOfAreas; area++) {
	    buildCurrentTableRow(area);
	}
	
	// Tell the user the starting conditions so they can see how 
	// much things change. The Table class has a method for outputting rows as text summaries.
	
	messageBox.append("\n\nStarting conditions:\n");
	for (int area = 0; area < numberOfAreas; area++) {
	    messageBox.append(tableToReplicate.rowToString(area) + " Target " + currentTable.rowToString(area) + "\n");
	}
	
    } // End of randomizeStartingDistribution.
    
    
    
    
    
    /**
     * Returns a person randomly drawn from the sample microdata.
     **/
    private Person getRandomPerson() {
	
	// Math.random returns a double between 0 and 1 so we need to stretch 
	// this over the range of our posible microdata and make it an int.
	
	int randomPosition = (int)(microData.getNumberOfPeople() * Math.random()); 
	return microData.getPerson(randomPosition);  
    }
    
    
    
    
    
    /**
     * Calculates the statistics for one area based on our made up world.<P>
     * These are stored in the currentTable for comparison with the table we 
     * want to replicate.
     **/
    private void buildCurrentTableRow(int area) {
	
	// This line is just for safety - sometimes Vectors can fill with nulls.
	
	world[area].trimToSize();
	
	// Zero the current table values for this area.
	
	currentTable.setValue(area, 0, 0);
	currentTable.setValue(area, 1, 0);
	
	// Run through the people in the area, incrementing the statistics in 
	// our table.
	
	for (int person = 0; person < world[area].size(); person++) {
	    
	    Person currentPerson = (Person)(world[area].elementAt(person));
	    
	    if (currentPerson.getValue() == 0)
		currentTable.increment(area, 0);
	    else
		currentTable.increment(area, 1);
	}
	
    } // End of buildCurrentTableRow.
    
    
    
    
    
    /**
     * The meat of this particular program. Takes each area in turn, and swaps people in and out until statistics ok.<P>
     * The code works broadly by swapping random people out of the area, and replacing them by
     * another random person. The new error between the current statistics and those we're hoping
     * for is assessed, and if there's an improvement the change is kept, otherwise the old person is
     * put back in and the new one removed. This "gradient descent" style method is adjusted by the
     * Simulated Annealing algorithm, which allows worse errors to be kept with a probablity that
     * reduces over time. As this is a toy application with only one
     * attribute of two values, the SA routine actually slows down the basic gradient
     * descent algorithm, but if there were multiple attributes that needed fitting, it would
     * be a real boon.
     **/
    private void redistribute() {
	
	for (int area = 0; area < numberOfAreas; area++) {
	    
	    messageBox.append("\n\nDoing area " + area + "\n");
	    
	    // Set up the SA temperature to drop. 
	    
	    double temperature = 0;
	    int minError = -1;
	    Vector minErrorPeople = null;
	    int areaError = 0;
	    
	    for (int i = maxTemperature; i > 0; i--) {
		
		// The next line is lifted almost entirely from Dimitris' SimLeeds.
		
		temperature = (double)temperatureConversion*((double)i/(double)maxTemperature); 
		
		// Calculate the current error for the area. We're going
		// to carry on until the error is low, or we exceed a fixed
		// number of runs. In addition, each time a minimum error set 
		// of people is generated, we record this, as the algorithm 
		// can wander off into bad solutions and get lost.
		
		areaError = calculateError(area);
		if (minError == -1) {
		    minError = areaError;
		    minErrorPeople = (Vector)(world[area].clone());
		} 
		int runs = 0;
		
		// Start swapping.
		
		while ((areaError > errorMargin) && (runs < maxRuns)) {
		    
		    // Replace one of the people in the area with someone new.
		    
		    int person = (int)(world[area].size() * Math.random());
		    Person oldPerson = (Person) world[area].elementAt(person);
		    world[area].remove(oldPerson);
		    Person newPerson = getRandomPerson();
		    world[area].addElement(newPerson);

		    // Recalculate the error and decide whether to keep them or not.
		    
		    int newAreaError = calculateError(area);
		    if (newAreaError > areaError) {
			
			// Keep bad choices with a probablity relating to how bad they are and 
			// the current temperature.
			
			// The next line is lifted almost entirely from Dimitris' SimLeeds.
			
			if (Math.random() < Math.exp((-1 * ((double)newAreaError - (double)areaError))/temperature)) {
			    areaError = newAreaError;
			} else {
			    world[area].remove(newPerson);
			    world[area].addElement(oldPerson);
			}

		    } else {
			areaError = newAreaError;
			
			// If this is the lowest error we've seen, record the people.
			if (areaError < minError) {
			    minError = areaError;
			    minErrorPeople = (Vector)(world[area].clone());
			}
		    }
		    
		    messageBox.append(tableToReplicate.rowToString(area) + " Target " + currentTable.rowToString(area) + " error = " + areaError + "\n");
		    runs++;
		    
		} // End of swapping while loop.
		
		// If we're ok with the current answer, don't bother reducing the temperature.
		
		if (areaError < errorMargin) break;
		
	    } // End of temperature decrease for loop.

	    // Check to make sure the error is the lowest we've seen, and if it isn't, replace the people.
	    
	    if (areaError > minError) {
		world[area] = minErrorPeople;
	    }
	    
	} // End of doing all the areas.
	
	messageBox.append("\n\nDone:\n");
	for (int area = 0; area < numberOfAreas; area++) {
	    messageBox.append(tableToReplicate.rowToString(area) + " Target " + currentTable.rowToString(area) + "\n");
	}
	
    } // End of redistribute.
    
    
    
    
    
    /**
     * Compares the current state of our made up world with the statistics we want and gives an error.<P>
     * The error in this case is just the absolute difference for each cell associated with the area, for 
     * example, if our original table had 10 men and 12 women, and our made up area currently had 
     * 5 men and 17 women, the error would be 10.
     **/
    private int calculateError(int area) {
	
	buildCurrentTableRow(area);
	int error = 0;
	error = Math.abs(tableToReplicate.getValue(area,0) - currentTable.getValue(area,0));
	error = error + Math.abs(tableToReplicate.getValue(area,1) - currentTable.getValue(area,1));
	return error;
	
    }
    
    
    
    
    
    /**
     * Write the people to a file with their area id.<P>
     * The file contains a header:<BR>
     * <CODE>Area,Person,Value</CODE><BR>
     * and then a line containing this data for each person.
     **/
    private void write() {
    
        // Get a filename from the user.
        
        FileDialog saveDialog = new FileDialog(new Frame(), "Save file of people and area ids", FileDialog.SAVE);
        saveDialog.show();
        File file = new File(saveDialog.getDirectory() + saveDialog.getFile());
        
        if ((saveDialog.getDirectory() == null) || (saveDialog.getFile() == null)) return;
        
        // Start writing process.
        
        try {
            
            BufferedWriter fw = new BufferedWriter(new FileWriter(file));
            
	    // Write header.
	    
	    fw.write("Area,Person,Value");
	    fw.newLine();
	    
	    // Run though areas and write each person.
	    
            for (int area = 0; area < world.length; area++) {
		
		String areaID = tableToReplicate.getID(area);  
		
		for (int person = 0; person < world[area].size(); person++) {
		    Person p = (Person)world[area].elementAt(person); 
		    fw.write(areaID + "," + p.getID() + "," + p.getValue());
		    fw.newLine();
		}
		
            } 
            
            fw.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
	messageBox.append("\n\nFinished writing file");
	
    } // End of write.

    
    
    
    
    /**
     * Just starts everything off.<P>
     * We do everything in the constructor to avoid having to deal 
     * with the problems associated with static.
     */
    public static void main(String[] args) {
	new Distributor();
    }

// End of class.
}
