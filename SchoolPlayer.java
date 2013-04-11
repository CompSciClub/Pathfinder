package com.csc2013;

import java.util.ArrayList;
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
	
	public enum BoxContainer{ // our custom BoxType class that allows for unknowns
		Open, Blocked, Door, Exit, Key, Unkown;
	}
	
	public class Map{
		private ArrayList<ArrayList> map = new ArrayList();
		
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
			}
			if (realY < 0){
				// this is furthest the point up that we've seen. It's the new originY
				originY = y;
				realY   = 0;
			}
			
			if (realX >= map.size()){
				for (i = map.size(); i < realX + 1; i++){
					map.add(new ArrayList<BoxContainer>());
				}
			}
			
			ArrayList<BoxContainer> row = map.get(realX);
			if (realY >= row.size()){
				for (i = row.size(); i < realY + 1; i++){
					row.add(BoxContainer.Unkown);
				}
				row.set(realY, element);
			} else {
				row.add(realY, element);
			}
		}
		
		public BoxContainer getElement(int x, int y){
			ArrayList<BoxContainer> row = map.get(x - originX);
			return row.get(y - originY);
		}
	}
	
	private int east  = 0;
	private int south = 0;
	
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
		updateMap(vision);
		south++;
		return Action.South;
	}
	
	private void updateMap(final PlayerVision vision){
		int i;
		
		// add everything west to the map
		for (i = 0; i < vision.mWest; i++){
			int x = -i - east - 1;
			
			addToMap(x, south, vision.West[i]);
		}
		
		// add everything east to the map
		for (i = 0; i < vision.mEast; i++){
			int x = i + east + 1;
			
			addToMap(x, south, vision.East[i]);
		}
		
		// add everything north to the map
		for (i = 0; i < vision.mNorth; i++){
			int y = -i - south - 1;
			
			addToMap(east, y, vision.North[i]);
		}
		
		// add everything south to the map
		for (i = 0; i < vision.mSouth; i++){
			int y = i + south + 1;
			
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
	
	}
}
