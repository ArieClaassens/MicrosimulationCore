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
 * Class for encapsulating sample microdata.<P>
 * Holds a set of People class objects read from a file. You can get 
 * hold of the number of people, and pull out an individual person 
 * based on their position in the original file.
 * @author <A href="http://www.geog.leeds.ac.uk/people/a.evans/">Andy Evans</A>
 * @version 1.0
 */
public class MicroData {
    
    private Person [] people = null;	    // The people in our sample.
    private int numberOfRows = 0;	    // The number of people.
    
    
    /** 
     * Creates a new instance of MicroData.<P>
     * Takes in a file path and reads the data from it.The file should 
     * be in the form:<BR>
     * <CODE>Name, Value</CODE><BR>
     * Currently each person has only one attribute that 
     * can be one of two values (the value should be either zero or one).
     */
    public MicroData(String filename) {
	read(filename);
    }
    
    
    
    
    
    /**
     * Reads in a file of people.<P>
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
	
	int type = 0;
	Vector row = new Vector(2);
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
	// so we can make an array to hold them as Person objects, which is 
	// much more useful and neat than holding them in the Vectors.
	
	people = new Person [numberOfRows];
	
	for (int i = 0; i < numberOfRows; i++) {
	    row = (Vector) rows.elementAt(i);
	    people[i] = new Person();
	    people[i].setID((String)(row.elementAt(0)));
	    people[i].setValue(((Integer)(row.elementAt(1))).intValue());
	}

    } // End of read method.
    

    
    
    
    /**
     * Returns the number of people in the sample microdata set.
     **/
    public int getNumberOfPeople () {
	return numberOfRows;
    }
    
    
    
    
    
    /** 
     * Returns a particular person from inside the microdata set.
     * @param position: their row number in the original file, starting with zero.
     **/
    public Person getPerson(int position) {
	return people[position];
    }
    
    
    
    
    
    /**
     * Returns the microdata as a text String that can be displayed.
     **/
    public String toString() {
	String microDataAsText = "Read people: \n";
	for (int i = 0; i < people.length; i++) 
	    microDataAsText = microDataAsText + people[i].getID() + " " + people[i].getValue() + "\n";
	return microDataAsText;
    }
    
// End of class.  
}
