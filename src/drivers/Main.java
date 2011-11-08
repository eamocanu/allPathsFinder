package drivers;
import eamocanu.pathfinder.Model;


/**
 * Test driver for all paths finder.
 * 
 * @author Adrian
 *
 */
public class Main {

	public static void main(String[] args) {
		long start= System.currentTimeMillis(); 
		Model model= new Model();
		model.findNumberOfPaths();
		long end=System.currentTimeMillis();
		
		System.out.println("exec time: " + (end-start) + "ms");
	}
	

	
	
}

