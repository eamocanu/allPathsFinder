/**
 * 
 */
package eamocanu.utils;

import java.util.Scanner;

import eamocanu.pathfinder.Commons;
import eamocanu.pathfinder.Node;

/**
 * @author Adrian
 * 
 * As always, I protect myself with the BSD license.
 * 
 * Released under the BSD license
 * Copyright (c) 2011, Adrian M
 * All rights reserved.
 * */

public class GraphReader {
	private int jSize;
	private int iSize;
	private int totalNodes;
	private Node startNode=null, endNode=null;
	
	
	
	/**
	 * Reades in graph from file into a matrix updating number
	 * of rows and columns
	 */
	public GraphReader() {
		super();
	}
	
	
	/** Read in the graph from stdin into a Node 2D matrix 
     */
	public Node [][] readInGraph() {
		Node [][] graph;
		
        Scanner sc = new Scanner( System.in );
		
        jSize= sc.nextInt();
		iSize= sc.nextInt();
		graph = new Node[iSize][jSize];
		
		//read graph
		for (int i=0; i<iSize;i++){
			for (int j=0; j<jSize;j++){
				graph[i][j]=new Node(Integer.parseInt(sc.next()), i, j);
				if (graph[i][j].nodeData == Commons.START_NODE ){
					startNode=graph[i][j]; 
				}
				if (graph[i][j].nodeData == Commons.END_NODE ){
					endNode=graph[i][j]; 
				}
				
				//keep track of how many nodes we need to go through
				if ( !(graph[i][j].nodeData == Commons.BLOCKED_NODE) ){
					totalNodes++;
				}
			}
		}
		
		//set neighbours
		for (int i=0; i<iSize; i++){
			for (int j=0; j<jSize; j++){
				if (j!=0 && !(graph[i][j-1].nodeData == Commons.BLOCKED_NODE)){
					graph[i][j].neighbours.add(graph[i][j-1]);
				}
				if (j!=jSize-1 && !(graph[i][j+1].nodeData == Commons.BLOCKED_NODE)){
					graph[i][j].neighbours.add(graph[i][j+1]);
				}
				if (i!=0 && !(graph[i-1][j].nodeData == Commons.BLOCKED_NODE)){
					graph[i][j].neighbours.add(graph[i-1][j]);
				}
				if (i!=iSize-1 && !(graph[i+1][j].nodeData == Commons.BLOCKED_NODE)){
					graph[i][j].neighbours.add(graph[i+1][j]);
				}
			}
		}
		return graph;
	}
	
	
	public int getGraphRows(){return iSize;}
	public int getGraphColumns(){return jSize;}
	public int getTotalNodes(){return totalNodes;}
	public Node getStartNode(){return startNode;}
	public Node getEndNode(){return endNode;}

}
