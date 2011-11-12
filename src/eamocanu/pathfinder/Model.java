package eamocanu.pathfinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import eamocanu.utils.GraphReader;


/**
 * @author Adrian
 * 
 * As always, I protect myself with the BSD license.
 * 
 * Released under the BSD license
 * Copyright (c) 2011, Adrian M
 * All rights reserved.
 * 
 * A model which uses a 2D array to record the graph.
 * The problem is to find all paths between 2 nodes in a graph.
 * 
 * You are given a start point, end point, and a graph. Some nodes
 * may be traversable, others not.
 * 
 * I focused here on optimizing runtime from days to a few seconds or
 * even ms (depending on your machine). The actual coding style 
 * could be greatly improved. I get lazy sometimes!
 * 
 * For memory and optimization analysis see the readme file.
 */
public class Model {
	private Node [][] graph;
	private int iSize, jSize; //graph size
	private Node startNode, endNode;
	
	//collect some stats 
	private int totalNodes; //max # traversable nodes
	private int numSols=0; //number of solutions found
	private int branches=0; //recursion branches
	
	private GraphReader graphReader;
	
	

	public Model(GraphReader graphReader) {
		this.graphReader= graphReader;
		init();
	}
	
	
	private void init() {
		graph = graphReader.readInGraph();
		iSize=graphReader.getGraphRows();
		jSize=graphReader.getGraphColumns();
		startNode=graphReader.getStartNode();
		endNode=graphReader.getEndNode();
		totalNodes=graphReader.getTotalNodes();
	}


	/** Calculates number of paths and prints results */
	public void findNumberOfPaths() {
		findNumPaths(startNode, 1, null);
		
		System.out.println("Total solutions: "+numSols);
		System.out.println("Total steps: "+branches);
	}
	
	
	/** The actual method which finds all paths from A to B recursively; 
	 * where A, B are nodes in graph
	 * 
	 * Recursive method, hence not fully optimized regarding stack.
	 * 
	 * @param crtNode			node to go to 
	 * @param crtDepth			index of this node in the path 
	 * @param prevBridgeCell	last bridge node detected
	 * 							'bridge' as defined in graph theory
	 */
	private void findNumPaths(Node crtNode, int crtDepth, Node prevBridgeCell){
		if (crtNode==null) return; //reached end or node we don't own
		if (crtNode.isVisited()) return; //been here already
		if (crtNode == endNode  &&  totalNodes != crtDepth) return; //reached end too early
		
		//look for bridges
		if (prevBridgeCell !=null){
			if (prevBridgeCell!= crtNode){
				//start BST from End and try to reach crtCell w/o going thru bridge node
				//if can reach crtCell from End RETURN
				if (isPath(graph, endNode, crtNode, prevBridgeCell)){
					//crt node and end are on the same side of the bridge => bad return
					//we can't visit the other side and return to this side 
					//through the same bridge node
					return;
				}
			} else {
				prevBridgeCell=null;
			}
		}
		
		if (isDisconnected(graph, crtDepth)) return; //check for disconnected components (by the path)
		
		//visit current node
		crtNode.setVisited(true);//or inc counter
		crtNode.solutionIndex= crtDepth;//XXX
		branches++;
		
		//found path -> record it
		if ( crtNode == endNode  &&  totalNodes == crtDepth ){
			numSols++;
			//for illustration purposes you can see solutions found by
			//uncommenting these lines
//			System.out.println("numsols "+numSols);
//			printSolution(true);
			
			//unvisit end node
			crtNode.setVisited(false);
			crtNode.solutionIndex=0;//XXX
			return;//unwind stack
		}

		Node adjBridge=findAdjBridge(crtNode);//find bridge adjacent to crt node
		List<Node> degOneNodes= getNeighboursOfDegreeOne(crtNode);
		if (degOneNodes.size() > 0 && !degOneNodes.contains(endNode)){
			//if we have nodes of degree 1 go only there right away
			for (Node r: degOneNodes){
				findNumPaths(r, crtDepth+1, adjBridge);
			}
		} else {
			//continue visiting in all directions
			Node next= getNodeAtDirection(crtNode, Node.DIRECTIONS.DOWN);
			findNumPaths(next, crtDepth+1, adjBridge);
			next= getNodeAtDirection(crtNode, Node.DIRECTIONS.LEFT);
			findNumPaths(next, crtDepth+1, adjBridge);
			next= getNodeAtDirection(crtNode, Node.DIRECTIONS.RIGHT);
			findNumPaths(next, crtDepth+1, adjBridge);
			next= getNodeAtDirection(crtNode, Node.DIRECTIONS.UP);
			findNumPaths(next, crtDepth+1, adjBridge);
		}
		
		//unvisit this node and unwind stack
		crtNode.setVisited(false);
		crtNode.solutionIndex=0;
	}


	/** Get adjacent nodes of degree 1.
	 * 
	 * @param crtNode	the node of which neighbours to check
	 * @return			a list of degree 1 neighbours
	 * 						empty list if no such neighbour exists
	 */
	private List<Node> getNeighboursOfDegreeOne(Node crtNode) {
		List<Node> ls = new ArrayList<Node>(4);
		for ( Iterator<Node> it = crtNode.neighbours.iterator(); it.hasNext(); ) {
			Node n=it.next();
			if ( /*!isCorner(n) &&*/ !inPath(n) ){ //XXX isCorner remove 
				if (getDegree(n)==1){
					//return n;
					ls.add(n);//XXX research: how ab dont add if adj=endNode
				}
			}
		}
		return ls;
	}

	
	/** Checks if crtNode is a corner node
	 * 
	 * @param crtNode	the node to check
	 * @return			true if corner
	 * 						false otherwise
	 */
	private boolean isCorner(Node crtNode){
		return (crtNode==graph[0][0] || crtNode==graph[iSize-1][jSize-1] || crtNode==graph[0][jSize-1] || crtNode==graph[iSize-1][0] );			
	}

	
	/** Returns degree of node/vertex. The degree is the number of adjacent nodes
	 * which are not in the current path.
	 * 
	 * @param vertex	Node for which to get the degree
	 * @return			the degree of vertex
	 */
	private int getDegree(Node vertex){
		int degree=0;
		for ( Iterator<Node> it = vertex.neighbours.iterator(); it.hasNext(); ) {
			if ( !inPath(it.next()) ) 
				degree++;
		}
		return degree;
	}
	
	
	/** Returns only one of the bridge nodes from the list of adjacent nodes
	 * 
	 * @param crtNode	Node for which to get the adjacent bridge if existent
	 * @return			adjacent bridge to crtNode, if such bridge exists
	 * 						null otherwise
	 */
	private Node findAdjBridge(Node crtNode) {
		List<Node> adjNodes= crtNode.neighbours;
		
		for ( Iterator<Node> it = adjNodes.iterator(); it.hasNext(); ) {
			Node node=it.next();
	    	if (node!=endNode && !node.isVisited()  &&  isBridge(node) )
	    		return node;
	    }

	    return null;
	}
	
	
	/** Checks if crtNode is a bridge in the graph.
	 * 
	 * @param crtNode	node to be checked 
	 * @return			true if crtNode is bridge
	 * 						false otherwise
	 */
	private boolean isBridge(Node crtNode){
		//cannot be corner
//		if (crtNode==graph[0][0] || crtNode==graph[iSize-1][jSize-1] || crtNode==graph[0][jSize-1] || crtNode==graph[iSize-1][0] ){
//			return false;
//		}
		if (isCorner(crtNode)){
			return false;
		}
		
		Iterator<Node> it = crtNode.neighbours.iterator();
		int degree=0;
	    while ( it.hasNext() ){
	    	if ( !inPath(it.next()) ) 
	    		degree++; 
	    }
	    return 
	    	degree < 3  && 
	    	(isHorizontalBridge(crtNode) || 
	    			isVerticalBridge(crtNode) );//==2; hmm test
	}


	
	private boolean isVerticalBridge(Node node) {
		Node left= getNodeAtDirection(node, Node.DIRECTIONS.UP);
		Node right= getNodeAtDirection(node, Node.DIRECTIONS.DOWN);

		if ( left==null || (left!=null && inPath(left) ) ){
			if ( right==null || (right!=null && inPath(right)) ){
				return true;
			}
		}
		
		return false;
	}


	private boolean isHorizontalBridge(Node node) {
		
		Node left= getNodeAtDirection(node, Node.DIRECTIONS.LEFT);
		Node right= getNodeAtDirection(node, Node.DIRECTIONS.RIGHT);

		if ( left==null || (left!=null && inPath(left) ) ){
			if ( right==null || (right!=null && inPath(right)) ){
				return true;
			}
		}
		
		return false;
	}
	
	
	/** Checks if a path from start to dest exists not going through notAllowed
	 * 
	 * @param start			node where to start the search
	 * @param dest			goal node to reach and end search
	 * @param notAllowed	path cannot go through this node
	 */
	private boolean isPath(Node[][] graph, Node start, Node dest, Node notAllowed) {
		resetInQueuePointers(graph);
		
		//find path from src to dest not through bridge
		return isPathThroughBFS(start, dest, notAllowed);
	}
	
	
	/** FIXME combine with findReachableNodesFromEnd
	 * Checks if a path from start to dest exists not going through bridgeNode
	 * 
	 * @param start			node where to start the search
	 * @param dest			goal node to reach and end search
	 * @param bridgeNode	path cannot go through this node
	 */	
	private boolean isPathThroughBFS(Node start, Node dest, Node bridgeNode) {
		Node crtNode,adjNode;
		Queue<Node> q= new LinkedList<Node>();
		q.add(start);
		start.inBFSQueue=true;
		
		while(!q.isEmpty()){
			crtNode=q.poll();
			if (crtNode == dest)
				return true;

			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.DOWN);
			if (adjNode!=bridgeNode) addToQueue(adjNode, q);
			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.RIGHT);
			if (adjNode!=bridgeNode) addToQueue(adjNode, q);
			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.LEFT);
			if (adjNode!=bridgeNode) addToQueue(adjNode, q);
			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.UP);
			if (adjNode!=bridgeNode) addToQueue(adjNode, q);
		}
		
		return false;
	}

	
	/** Check if the node graph is disconnected.
	 * 
	 * If the graph is disconnected, the number of reachable nodes is
	 * less than the actual free nodes.
	 * 
	 * @param graph			the graph to check
	 * @param pathLength	length of our current path
	 * @return				true if the graph is disconnected
	 * 							false otherwise
	 */
	private boolean isDisconnected(Node[][] graph, int pathLength) {
		resetInQueuePointers(graph);
		int numNodesInGraph= findReachableNodesFromEnd();
		return (numNodesInGraph < totalNodes-pathLength);
	}
	
	
	/** FIXME combine with isPathThroughBFS(..)
	 * Find how many nodes not in path are reachable from End node.
	 * 
	 * @return	the number of reachable nodes
	 */
	private int findReachableNodesFromEnd() {
		Queue<Node> q= new LinkedList<Node>();
		Node unvisitedVertex= findUnvisitedVertex(endNode);
		if (unvisitedVertex==null) return 0;
		
		addToQueue(findUnvisitedVertex(endNode), q/*,visitedNodes*/);
		int numNodes=0;
		Node crtNode,adjNode;
		
		while(!q.isEmpty()){
			crtNode=q.poll();
			numNodes++;

			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.DOWN);
			if (adjNode!=endNode ) addToQueue(adjNode, q/*,visitedNodes*/);
			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.RIGHT);
			if (adjNode!=endNode ) addToQueue(adjNode, q/*,visitedNodes*/);
			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.LEFT);
			if (adjNode!=endNode ) addToQueue(adjNode, q/*,visitedNodes*/);
			adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.UP);
			if (adjNode!=endNode ) addToQueue(adjNode, q/*,visitedNodes*/);
		}
		
		return numNodes;
	}


	/** Find a reachable node adjacent to the node passed in.
	 * 
	 * @param crtNode	node to check for reachable adjacent neighbours
	 * @return			found node or
	 * 						null is not such node exists
	 */
	private Node findUnvisitedVertex(Node crtNode) {
		Node adjNode;

		adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.DOWN);
		if (adjNode !=null  &&  !adjNode.isVisited()) return adjNode;
		adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.RIGHT);
		if (adjNode !=null  &&  !adjNode.isVisited()) return adjNode;
		adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.LEFT);
		if (adjNode !=null  &&  !adjNode.isVisited()) return adjNode;
		adjNode= getNodeAtDirection(crtNode, Node.DIRECTIONS.UP);
		if (adjNode !=null  &&  !adjNode.isVisited()) return adjNode;
		
		//all neighbours are visited
		return null;
	}


	/** Mark node as visited by BFS and add it to the BFS queue
	 * 
	 * @param crtNode	node to visit
	 * @param q			the BFS queue to add to
	 */
	private void addToQueue(Node crtNode, Queue<Node> q) {
		if (crtNode!=null && !inPath(crtNode) && !crtNode.inBFSQueue){
			crtNode.inBFSQueue=true;
			q.add(crtNode);
		}
	}


	/** Reset nodes: mark BFS unvisited 
	 * 
	 * @param graph	the graph of nodes
	 */
	private void resetInQueuePointers(Node[][] graph) {
		for (int i=0; i<iSize;i++){
			for (int j=0; j<jSize;j++){
				graph[i][j].inBFSQueue= false;
			}
		}
	}


	/** Checks current node in current path.
	 * 
	 * @param crtNode	the node to check if it belongs to current path
	 * @return			true if node belongs to current path
	 * 						false otherwise
	 */
	private boolean inPath(Node crtNode) {
		return crtNode.isVisited();
	}

	
	/** Get node at specified direction from current node. 
	 * If no node exists, or node is not owned/blocked, it returns null.
	 * 
	 * @param crtNode	current node
	 * @param dir		direction in which to go from current node		
	 * @return			node in direction
	 * 						null if node not owned or current node is an edge node
	 */
	private Node getNodeAtDirection(Node crtNode, Node.DIRECTIONS dir) {
		Node node=null;
		
		try {
			if (dir == Node.DIRECTIONS.DOWN){
				node= graph[crtNode.x+1][crtNode.y];
			} else if (dir == Node.DIRECTIONS.UP){
				node=graph[crtNode.x-1][crtNode.y];
			} else if (dir == Node.DIRECTIONS.LEFT){
				node= graph[crtNode.x][crtNode.y-1];
			} else if (dir == Node.DIRECTIONS.RIGHT){
				node= graph[crtNode.x][crtNode.y+1];
			}
			
			if (node.nodeData == Commons.BLOCKED_NODE){
				return null;
			} 
		} catch (Exception e) {
			return null;
		}

		return node;
	}



	
	//for fun and debugging
	private void printSolution(boolean colour){
		if (colour){
			System.out.println("SOLUTION: ");
		}
		
		for (int i=0; i<iSize;i++){
			for (int j=0; j<jSize;j++){

				if (!graph[i][j].isVisited())
					System.out.print(graph[i][j].solutionIndex+"]\t" );
				else
					System.out.print(graph[i][j].solutionIndex+"\t" );
			}
			System.out.println();
		}
		System.out.println();
	}


	
}
