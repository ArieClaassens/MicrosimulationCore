/**
 * --Copyright notice-- 
 *
 * Copyright (c) School of Geography, University of Leeds. 
 * http://www.geog.leeds.ac.uk/
 * This software is licensed under 'The Artistic License' which can be found at 
 * the Open Source Initiative website at... 
 * http://www.opensource.org/licenses/artistic-license.php
 * Please note that the optional Clause 8 does not apply to this code.
 *
 * The Standard Version source code, and associated documentation can be found at... 
 * [online] http://mass.leeds.ac.uk/
 * 
 *
 * --End of Copyright notice-- 
 *
 */

import java.util.*;
import java.io.*;


/**
 * A class to encapsulate statistical tables.<P>
 * The table can be envisaged as a series of rows, each row being an 
 * area about which statistics exist. Each area/row has an ID and a set of 
 * values. Currently, there are only two values. Tables can be read from 
 * a file or built programmatically, and there are two appropriate constructors.
 * @author <A href="http://www.geog.leeds.ac.uk/people/a.evans/">Andy Evans</A>
 * @version 1.0
 */
public class Table {
    
    private String ids [] = null;	    // Array holding row/area IDs.
    private int values [][] = null;	    // Array holding row/area values.
    private int numberOfRows = 0;	    // The number of rows/areas.
    
    
    /**
     * A constructor for those not wishing to read the table from a file.<P>
     * @param size: the number of rows/areas you want in the table.
     **/
    public Table(int size) {
	ids = new String[size];
	values = new int[size][2];
    }
    
    
    
    
    
    /**  
     * A constructor for those wishing to fill the table from a file.<P>
     * The format should be comma separated, with a header line. Each row 
     * should contain the statistics for an area, in the format:<BR>
     * <CODE>Name, Value0, Value1</CODE><BR>
     * Currently this code only copes with two statistics relating to the the same variable, 
     * for example: 
     * <CODE>EDName, NumberOfFemales, NumberOfMales</CODE><P>
     * @param filename: the path and filename for the file containing the data.
     **/
    public Table(String filename) {
	read(filename);
    }
    
    
    
    
    
    /**
     * Reads in a file of area statistics.<P>
     **/
    private void read(String filename) {
	
	// Open the file and attach an input stream to it.
	// The data from the file will flood down the input stream, 
	// and we'll catch it in a StreamTokenizer.
	
	File file = new File(filename);
	FileInputStream is = null;
	try {
	    is = new FileInputStream(file);
	} catch (FileNotFoundException fnfe) {
	    fnfe.printStackTrace();
	}
	Reader reader = new BufferedReader(new InputStreamReader(is),4096);
	StreamTokenizer st = new StreamTokenizer(reader);
	
	// The StreamTokenizer breaks up the stream into chunks called "Tokens" 
	// everytime it hits a comma or colon. We set it to do this below.
	
	st.parseNumbers();
	st.eolIsSignificant(true);
	st.whitespaceChars((int)',',(int)',');
	st.whitespaceChars((int)':',(int)':');
	
	// We'll pull in a row at a time, and store the elements in a Vector. 
	// As Vectors store "java.lang.Object" class objects we'll need to 
	// convert any data to subclasses of java.lang.Object like "String" or "Integer".
	// We'll then store each row Vector in a "Vector of Vectors" called 'rows'. 
	
	int type;
	Vector row = new Vector(3);
	Vector rows = new Vector(0);
	
	// The following code just ditches the header tokens which the program isn't interested in.
	
	try {
	    type=st.nextToken();
	    type=st.nextToken();
	    type=st.nextToken();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	
	// The "loop:" below names a block of code so we can jump out of it based 
	// on criteria detailed below. We enter an infinite while loop and keep 
	// going until we've met one of these criteria (like reaching the end of 
	// the file) and we jump out.
	
	loop: {
	    
	    while(true) {
		
		try {
		    
		    // Get the next block of text between commas and 
		    // look at its type. 
		    
		    type=st.nextToken();
		    
		    switch(type) {
						    
			case StreamTokenizer.TT_EOF:
			    
			    // If the type suggests it's the end of the 
			    // file (EOF) jump out of the loop.
			    
			    rows.addElement(row.clone());
			    row = new Vector(0);
			    numberOfRows++;
			    break loop;
			    
			case StreamTokenizer.TT_EOL:
			    
			    // If the type suggests it's the end of a line (EOL) 
			    // just jump back to the top of the while loop.
			    
			    rows.addElement(row.clone());
			    row = new Vector(0);
			    numberOfRows++;
			    break;
			    
			case StreamTokenizer.TT_WORD:
			    
			    // If its a word, add a String version of the token 
			    // to the row Vector. 			    
			    
			    row.addElement(st.sval);
			    break;
			    
			case StreamTokenizer.TT_NUMBER:
			    
			    // If its a number, add an Integer version of the token 
			    // to the row Vector by converting it's int version.			    
			    
			    row.addElement(new Integer((int)st.nval));
			    break;
		    
		    } // End of switch. 
		    
		} catch(Exception e) {
		    e.printStackTrace();
		}
		
	    } // End of while loop.
	    
	} // End of loop: block.

	// Now we've read through the file, we know how many people there are, 
	// so we can make an array to hold them as String and int objects, which is 
	// much more useful and neat than holding them in the Vectors.
	
	// Not sure why, but this seems necessary for this file, but not the people file(?!?).
	
	numberOfRows--;
	
	ids = new String [numberOfRows];
	values = new int [numberOfRows][2];
	
	for (int i = 0; i < numberOfRows; i++) {
	    row = (Vector) rows.elementAt(i+1);
	    ids[i] = (String)(row.elementAt(0));
	    values[i][0] = ((Integer)(row.elementAt(1))).intValue();
	    values[i][1] = ((Integer)(row.elementAt(2))).intValue();
	}
    
    } // End of read method.

    
    
    
    
    /** 
     * Gets the number of areas stored in the Table.
     **/
    public int getNumberOfAreas() {
	return numberOfRows;
    }
    
    
    
    
    
    /** 
     * Gets the total population stored in one area.<P>
     * This just adds up all the cell figures.
     * @param area: the row number of the area in the original file, starting with zero.
     **/    
    public int getTotalAreaPopulation(int area) {
	return values[area][0] + values[area][1];
    }
    
    
    
    
    
    /**
     * Increases the statistic in a row's column by one.<P>
     * @param area: the row number of the area in the original file, starting with zero.
     * @param valuePosition: the value column in the original file to increase. zero is the first value column, one the second.
     **/
    public void increment(int area, int valuePosition) {
	values[area][valuePosition]++;
    } 
    
    
    
    
    
    /**
     * Gets a value from the Table.<P>
     * @param area: the row number of the area in the original file, starting with zero.
     * @param valuePosition: the value column in the original file. zero is the first value column, one the second.
     **/    
    public int getValue(int area, int valuePosition) {
	return values[area][valuePosition];
    }
   
    
   
    
    
    /**
     * Sets a value from the Table.<P>
     * @param area: the row number of the area in the original file, starting with zero.
     * @param valuePosition: the value column in the original file. zero is the first value column, one the second.
     **/    
    public void setValue(int area, int valuePosition, int value) {
	values[area][valuePosition] = value;
    }
    
    
    
    
 
    /**
     * Gets an ID from the Table.<P>
     * @param area: the row number of the area in the original file, starting with zero.
     **/    
    public String getID(int area) {
	return ids[area]; 
    }
    
    
    
    
    
    /**
     * Returns the Table as a text String that can be displayed.
     **/
    public String toString() {
	String tableAsText = "Read table: \n";
	for (int i = 0; i < ids.length; i++) 
	    tableAsText = tableAsText + ids[i] + " " + values[i][0] + " " + values[i][1] + "\n";
	return tableAsText;
    }
    
    
    
    
    
    /**
     * Returns a Table row as a text String that can be displayed.
     **/   
    public String rowToString(int position) {
	if (ids[position] != null)
	    return "Area " + ids[position] + " Males " + values[position][0] + " Females " + values[position][1]; 
	else 
	    return " Males " + values[position][0] + " Females " + values[position][1];
    }
    
    
// End of class.    
}
