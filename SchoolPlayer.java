package com.csc2013;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.MoveType;

/**
 * 
 * [To be completed by students]
 * 
 * @author [School/Team Name]
 *
 */
public class SchoolPlayer {
	
	public enum BoxContainer{ // our custom BoxType enum that allows for unknowns
		Open, Blocked, Door, Exit, Key, Unkown;
	}
	
	public class Map{
		private ArrayList<ArrayList<BoxContainer>> map = new ArrayList<ArrayList<BoxContainer>>();
		
		private int originX = 0;
		private int originY = 0;
		
		// constructor
		public Map(){
		
		}
		
		public void addElement(int x, int y, BoxContainer element){
			int i;
			
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
			col.set(realY, element);
		}
		
		public BoxContainer getElement(int x, int y){
			if (x - originX < 0 || x - originX >= map.size())
				return BoxContainer.Unkown;
			
			ArrayList<BoxContainer> row = map.get(x - originX);
			
			if (y - originY < 0 || y - originY >= row.size())
				return BoxContainer.Unkown;
			
			return row.get(y - originY);
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
		
		public void testPathfinding(){
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
		}
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
		Map tempMap = new Map();
		tempMap.testPathfinding();
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
		updateMap(vision);
		east++;
		return Action.East;
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
		} else if (piece.isEnd()){
			type = BoxContainer.Exit;
		}
		
		// add it to the map
		
		map.addElement(x, y, type);
		
		// add its surrondings to the map
		map.addElement(x, y - 1, castToBoxContainer(piece.North));
		map.addElement(x, y + 1, castToBoxContainer(piece.South));
		map.addElement(x - 1, y, castToBoxContainer(piece.West));
		map.addElement(x + 1, y, castToBoxContainer(piece.East));
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
