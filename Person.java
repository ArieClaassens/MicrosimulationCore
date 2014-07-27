package uk.ac.leeds.ccg.modeling.microsim;

/**
 * A class to encapsulate a person.<P>
 * Currently people have a id and a value.
 * @author <A href="http://www.geog.leeds.ac.uk/people/a.evans/">Andy Evans</A>
 * @version 1.0
 */
public class Person {
    
    private String id = "";	// The person's id.
    private int value = 0;
    
    
    /** 
     * Creates a new Person. 
     */
    public Person() {
    }
    
    
    
    
    
    /**
     * Sets the person's ID.
     **/
    public void setID (String i) {
	id = i;
    }
    
    
    
    
    
    /**
     * Sets the person's value.
     **/
    public void setValue (int val) {
	value = val;
    }
    
    
    
    
    
    /**
     * Gets the person's ID.
     **/
    public String getID () {
	return id;
    }
    
    
    
    
    
    /**
     * Gets the person's value.
     **/
    public int getValue () {
	return value;
    }

// End of class.
}
