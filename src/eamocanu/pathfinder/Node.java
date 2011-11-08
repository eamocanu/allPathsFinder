package eamocanu.pathfinder;
import java.util.ArrayList;
import java.util.List;

public class Node {
	public static enum DIRECTIONS { LEFT, RIGHT, UP, DOWN }
	
	public final int x,y;//node coords
	public final int nodeData;//node data: 0,1,2,3
	private boolean agentVisited; //true if agent has been in this node; ie node part of path
	public final List <Node>neighbours; //adjacent nodes
	public int solutionIndex=0;//used to print out path nodes in order
	public boolean inBFSQueue;//in BFS queue 
	
	
	public Node(int cellData, int x, int y) {
		super();
		this.nodeData=cellData;
		this.x=x;
		this.y=y;
		neighbours= new ArrayList<Node>();
	}
	
	
	public String toString(){
		return solutionIndex + " " + x + " " + y + " " + agentVisited + " inQ: "+inBFSQueue;//possibleDirections ;
	}

	
	/** Update this node's visited status. 
	 * For traversal. Seems faster than allocating a map each time
	 * a traversal is done. */
	public void setVisited(boolean visited){ 
		agentVisited=visited;
	}


	/** Retrieve this node's visited status */
	public boolean isVisited() {
		return agentVisited;
	}
	
}
