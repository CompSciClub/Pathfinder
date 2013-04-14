package com.csc2013;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.MoveType;
import com.sun.tools.javac.util.List;

/**
 * 
 * [To be completed by students]
 * 
 * @author [School/Team Name]
 *
 */
public class SchoolPlayer {
	private ArrayList<Action> moves = new ArrayList(); // a list of moves to execute
	private ArrayList<Point>  exits = new ArrayList(); // holds all of the known exits
	private ArrayList<Point>  keys  = new ArrayList(); // holds all of the known keys
	
	public enum BoxContainer{ // our custom BoxType enum that allows for unknowns
		Open, Blocked, Door, Exit, Key, Unkown;
	}
	
	// Holds information about a specifid point. Including its x,y position relative to our origin and its BoxContainer type
	public class Point {
		public int x;
		public int y;
		public BoxContainer type;
		public Point(int x, int y, BoxContainer type){
			this.x    = x;
			this.y    = y;
			this.type = type;
		}
	}
	
	public class Map{
		//private ArrayList<ArrayList<BoxContainer>> map = new ArrayList<ArrayList<BoxContainer>>();
		private HashMap<ArrayList<Integer>, BoxContainer> map = new HashMap();
		
		private int originX = 0;
		private int originY = 0;
		
		// constructor
		public Map(){
		
		}
		
		public void addElement(int x, int y, BoxContainer element){
			ArrayList<Integer> coordinates = new ArrayList<Integer>();
			coordinates.add(x);
			coordinates.add(y);
			map.put(coordinates, element);
			/*int i;
			
			// correct for the offset
			int realX = x - originX;
			int realY = y - originY;
			
			if (realX < 0){
				// this is furthest the point to the left that we've seen. It's the new originX
				originX = x;
				realX   = 0;
				map.add(0, new ArrayList<BoxContainer>());
			}
			if (realY < 0){
				// this is furthest the point up that we've seen. It's the new originY
				originY = y;
				realY   = 0;
				
				// bump every piece down one on the y axis
				for (i = 0; i < map.size(); i++){
					ArrayList<BoxContainer> col = map.get(i);
					col.add(0, BoxContainer.Unkown);
				}
			}
			
			if (realX >= map.size()){
				for (i = map.size(); i < realX + 1; i++){
					map.add(new ArrayList<BoxContainer>());
				}
			}
			
			ArrayList<BoxContainer> col = map.get(realX);
			if (realY >= col.size()){
				for (i = col.size(); i < realY + 1; i++){
					col.add(BoxContainer.Unkown);
				}
			}
			col.set(realY, element);*/
		}
		
		public BoxContainer getElement(int x, int y){
			ArrayList<Integer> coordinates = new ArrayList<Integer>();
			coordinates.add(x);
			coordinates.add(y);
			BoxContainer point = map.get(coordinates);
			if (point == null){
				return BoxContainer.Unkown;
			} else {
				return point;
			}
			/*if (x - originX < 0 || x - originX >= map.size())
				return BoxContainer.Unkown;
			
			ArrayList<BoxContainer> row = map.get(x - originX);
			
			if (y - originY < 0 || y - originY >= row.size())
				return BoxContainer.Unkown;
			
			return row.get(y - originY);*/
		}
		
		private class SearchNode{
			public int posX, posY;
			public int gScore, fScore;
			public int keysLeft;
			public SearchNode cameFrom;
			public Action direction;
			
			@Override
			public boolean equals(Object _other){
				if (_other == this) return true;
				if (!(_other instanceof SearchNode)) return false;
				
				SearchNode other = (SearchNode)_other;
				return other.posX == posX && other.posY == posY;
			}
			
			@Override
			public int hashCode(){
				return posX * 100000 + posY;
			}
		}
		
		//We need to do this each time because the fScore may change
		private SearchNode findFirstNode(HashSet<SearchNode> set){
			SearchNode min = null;
			for (SearchNode cur : set){
				if (min == null || cur.fScore < min.fScore)
					min = cur;
			}
			return min;
		}
		
		private SearchNode findNodeWithCoords(HashSet<SearchNode> set, int x, int y){
			for (SearchNode cur : set){
				if (cur.posX == x && cur.posY == y)
					return cur;
			}
			return null;
		}
		
		private ArrayList<Action> recoverPath(SearchNode cur){
			ArrayList<Action> moves = new ArrayList<Action>();
			while (cur.cameFrom != null){
				moves.add(0, cur.direction);
				
				BoxContainer curBox = getElement(cur.posX, cur.posY);
				if (curBox == BoxContainer.Door)
					moves.add(0, Action.Use);
				if (curBox == BoxContainer.Key)
					moves.add(1, Action.Pickup);
				
				cur = cur.cameFrom;	
			}
			
			return moves;
		}
		
		public ArrayList<Action> findShortestPath(int startX, int startY, int endX, int endY, int numKeys, boolean toUnknown){
			HashSet<SearchNode> visited = new HashSet<SearchNode>();
			HashSet<SearchNode> work = new HashSet<SearchNode>();
			
			SearchNode start = new SearchNode();
			start.posX = startX;
			start.posY = startY;
			start.gScore = 0;
			if (toUnknown)
				start.fScore = 0;
			else
				start.fScore = Math.abs(startX - endX) + Math.abs(startY - endY);
			start.keysLeft = numKeys;
			start.cameFrom = null;
			work.add(start);
			
			while(work.size() > 0){
				SearchNode current = findFirstNode(work);
				if (!toUnknown && current.posX == endX && current.posY == endY){
					//We've found the end node, reconstruct the path
					return recoverPath(current);
				}
				
				if (toUnknown && getElement(current.posX, current.posY) == BoxContainer.Unkown){
					System.out.println("Found path to unkown " + current.posX + ", " + current.posY + " " + getElement(current.posX, current.posY));
					return recoverPath(current);
				}
				
				work.remove(current);
				visited.add(current);
				
				for (int i = 0; i < 4; i++){
					int newX, newY;
					Action direction;
					if (i == 0){
						newX = current.posX + 1;
						newY = current.posY;
						direction = Action.East;
					}
					else if (i == 1){
						newX = current.posX;
						newY = current.posY - 1;
						direction = Action.North;
					}
					else if (i == 2){
						newX = current.posX - 1;
						newY = current.posY;
						direction = Action.West;
					}
					else{
						newX = current.posX;
						newY = current.posY + 1;
						direction = Action.South;
					}
					
					//Check to make sure we can move into this node
					BoxContainer newBox = getElement(newX, newY);
					if (newBox == BoxContainer.Blocked || (!toUnknown && newBox == BoxContainer.Unkown))
						continue;
					
					if (newBox == BoxContainer.Door && current.keysLeft == 0)
						continue;
					
					int new_gScore = current.gScore + 1;
					if (newBox == BoxContainer.Door || newBox == BoxContainer.Key)
						new_gScore++;
					
					
					//try to find the node if we've already searched it, otherwise create it
					SearchNode newNode = findNodeWithCoords(visited, newX, newY);
					if (newNode == null){
						newNode = findNodeWithCoords(work, newX, newY);
						if (newNode == null)
						{
							newNode = new SearchNode();
							newNode.posX = newX;
							newNode.posY = newY;
						}
					}
					
					if (visited.contains(newNode) && new_gScore >= newNode.gScore)
						continue;
					
					if (!work.contains(newNode) || new_gScore < newNode.gScore){
						newNode.cameFrom = current;
						newNode.direction = direction;
						newNode.gScore = new_gScore;
						if (toUnknown)
							newNode.fScore = newNode.gScore;
						else
							newNode.fScore = newNode.gScore + Math.abs(newNode.posX - endX) + Math.abs(newNode.posY - endY);
						newNode.keysLeft = current.keysLeft;
						if (newBox == BoxContainer.Door)
							newNode.keysLeft--;
						if (newBox == BoxContainer.Key)
							newNode.keysLeft++;
						if (!work.contains(newNode))
							work.add(newNode);
					}
				}
			}
			
			return null;
		}
		
		/*public void testPathfinding(){
			map.clear();
			map.add(new ArrayList<BoxContainer>(Arrays.asList(BoxContainer.Door, BoxContainer.Exit)));
			map.add(new ArrayList<BoxContainer>(Arrays.asList(BoxContainer.Key, BoxContainer.Blocked)));
			map.add(new ArrayList<BoxContainer>(Arrays.asList(BoxContainer.Open, BoxContainer.Open)));
			originX = -2;
			originY = 0;
			
			ArrayList<Action> moves = findShortestPath(-1, 0, -2, 1, 0, false);
			for (Action move : moves){
				System.out.println(move.toString());
			}
		}*/
	}
	
	private int east  = 0;
	private int north = 0;
	
	private Map map;
	
	/**
	 * Constructor.
	 * 
	 * @throws SlickException
	 */
	public SchoolPlayer() throws SlickException {
		// complete
		map = new Map();
	}

	/** 
	 * To properly implement this class you simply must return an Action in the function nextMove below.
	 * 
	 * You are allowed to define any helper variables or methods as you see fit
	 * 
	 * For a full explanation of the variables please reference the instruction manual provided
	 * 
	 * @param vision
	 * @param keyCount
	 * @param lastAction
	 * @return Action
	 */
	public Action nextMove(final PlayerVision vision, final int keyCount, final boolean lastAction) {
		addToMap(east, north, vision.CurrentPoint);
		// add everything we can see to our map
		updateMap(vision);
		
		if(vision.CurrentPoint.hasKey()) { // if there is a key on the current spot always pick it up
			if (moves.get(0) == Action.Pickup){ // if the moves array was already telling us to pick it up, remove that command
				moves.remove(0);
			}
 			return Action.Pickup;
		}
		
		// check if there are any accessible exits, and if so go to them
		ArrayList<Action> possibleMoves = new ArrayList<Action>();
		if (exits.size() > 0){
			for (int i = 0; i < exits.size(); i++){
				Point exit = exits.get(i);
				ArrayList<Action> movesToThisExit = map.findShortestPath(east, north, exit.x, exit.y, keyCount, false);
				if (movesToThisExit != null){
					// this is a valid way to get to the exit!
					
					// make sure it's shorter than any previously found exit
					if (movesToThisExit.size() < possibleMoves.size()){
						possibleMoves = movesToThisExit;
					}
				}
			}
		}
		
		if (possibleMoves.size() > 0){
			System.out.println("Found Exit");
			// we have a way to get to the exit
			moves = possibleMoves; // save the moves
			return doNextMove(); // get going
		}
		
		if (moves.size() > 0){
			// we are moving to a goal so keep going
			return doNextMove();
		}
		
		// we don't currently have a goal and there are no accessible exits. Go Explore!
		moves = map.findShortestPath(east, north, 0, 0, keyCount, true);
		return doNextMove(); // get going
	}
	
	// executes the next move and correctly updates east and north
	private Action doNextMove(){
		if (moves.size() > 0){
			Action move = moves.remove(0); // remove this move from the list and return it
			
			// update east or north
			if (move == Action.East){
				east++;
			} else if (move == Action.West){
				east--;
			} else if (move == Action.North){
				north++;
			} else if (move == Action.South){
				north--;
			}
			return move;
		} else {
			System.out.println("AHHH. There are no moves");
			return null; // there are no more moves! (This shouldn't ever happen if this method was called...)
		}
	}
	
	private void updateMap(final PlayerVision vision){
		int i, y, x;
		
		// add everything west to the map
		for (i = 0; i < vision.mWest; i++){
			x = -i - east - 1;
			
			addToMap(x, north, vision.West[i]);
		}
		
		// add everything east to the map
		for (i = 0; i < vision.mEast; i++){
			x = i + east + 1;
			
			addToMap(x, north, vision.East[i]);
		}
		
		// add everything north to the map
		for (i = 0; i < vision.mNorth; i++){
			y = i + north + 1;
			
			addToMap(east, y, vision.North[i]);
		}
		
		// add everything south to the map
		for (i = 0; i < vision.mSouth; i++){
			y = -i - north - 1;
			
			addToMap(east, y, vision.South[i]);
		}
		
	}
	
	private void addToMap(int x, int y, MapBox piece){
		BoxContainer type;
		
		// determine the type of this square
		type = BoxContainer.Open;
		
		if (piece.hasKey()){
			type = BoxContainer.Key;
			Point thisPoint = new Point(x, y, BoxContainer.Key);
			if (!checkForPointInArr(keys, thisPoint)){ // make sure it's not already in the array
				keys.add(thisPoint); // add this to our key array
			}
		} else if (piece.isEnd()){
			type = BoxContainer.Exit;
			Point thisPoint = new Point(x, y, BoxContainer.Exit);
			if (!checkForPointInArr(exits, thisPoint)){ // make sure it's not already in the array
				exits.add(new Point(x, y, BoxContainer.Exit)); // add this to our exit array
			}
		}
		
		// add it to the map
		
		map.addElement(x, y, type);
		
		// add its surroundings to the map
		Point[] surrondings = new Point[4];
		
		surrondings[0] = new Point(x, y + 1, castToBoxContainer(piece.North)); // add the north piece
		surrondings[1] = new Point(x, y - 1, castToBoxContainer(piece.South)); // add the south piece
		surrondings[2] = new Point(x + 1, y, castToBoxContainer(piece.East)); // add the east piece
		surrondings[3] = new Point(x - 1, y, castToBoxContainer(piece.West)); // add the west piece
		
		for (int i = 0; i < surrondings.length; i++){
			// check if this is a key or an exit and if so add it to the respective arrays if it not already there
			if (surrondings[i].type == BoxContainer.Exit){
				if (!checkForPointInArr(exits, surrondings[i])){ // make sure it's not already there
					exits.add(surrondings[i]);
				}
			} else if (surrondings[i].type == BoxContainer.Key){
				if (!checkForPointInArr(keys, surrondings[i])){ // make sure it's not already there
					keys.add(surrondings[i]);
				}
			}
			map.addElement(surrondings[i].x, surrondings[i].y, surrondings[i].type);
		}
	}
	
	// Checks for a given point in the given array of points. Returns true if it exists otherwise it returns false.
	private boolean checkForPointInArr(ArrayList<Point> array, Point point){
		for (int i = 0; i < array.size(); i++){
			Point checkPoint = array.get(i);
			if (array.get(i).x == point.x && array.get(i).y == point.y){
				return true;
			}
		}
		return false;
	}
	
	private BoxContainer castToBoxContainer(BoxType boxType){
		if (boxType == BoxType.Blocked){
			return BoxContainer.Blocked;
		}
		if (boxType == BoxType.Door){
			return BoxContainer.Door;
		}
		if (boxType == BoxType.Exit){
			return BoxContainer.Exit;
		}
		if (boxType == BoxType.Key){
			return BoxContainer.Key;
		}
		if (boxType == BoxType.Open){
			return BoxContainer.Open;
		}
		return BoxContainer.Unkown;
		
	}
}
