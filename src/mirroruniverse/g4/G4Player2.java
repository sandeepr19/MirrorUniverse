package mirroruniverse.g4;

import java.awt.Point;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G4Player2 implements Player {

	public boolean started = false;
	public int sightRadius1;
	public int sightRadius2;
	public int intDeltaX;
	public int intDeltaY;
	public static final int MAX_SIZE = 100;
	public int[][] kb_p1;
	public int[][] kb_p2;
	private int[] p1Pos;
	private int[] p2Pos;
	private int leftExitX;
	private int leftExitY;
	private int rightExitX;
	private int rightExitY;
	private boolean leftExitSet;
	private boolean rightExitSet;
	private OurPoint p;
	// private int initialDir;
	private int turn;
	private ArrayList<Integer> path;
	private boolean singleSetThisPath;
	private boolean debugging = false;
	private boolean p1Exited;
	private boolean p2Exited;
	private boolean readyToExit = false;
	private int counter;
	private boolean exploreFurtherLeft = true;
	private boolean exploreFurtherRight = true;

	@Override
	public int lookAndMove(int[][] aintViewL, int[][] aintViewR) {
		if(turn == 45){
			System.out.println();
		}
		if (!started) {
			initialize(aintViewL, aintViewR);
		}
		turn++;

		// left player finding exit and updating kb
		for (int y = 0; y < aintViewL.length; ++y) {
			for (int x = 0; x < aintViewL[0].length; ++x) {
				try {
					kb_p1[p1Pos[1] - sightRadius1 + y][p1Pos[0] - sightRadius1
							+ x] = aintViewL[y][x];
				} catch (Exception e) {
					System.out.println("1");
				}
				if (aintViewL[y][x] == 2 && !leftExitSet) {
					leftExitX = p1Pos[0] - sightRadius1 + x;
					leftExitY = p1Pos[1] - sightRadius1 + y;

				}
			}
		}
		if (!leftExitSet && leftExitX != -1) {
			AStar_Single as = new AStar_Single(p1Pos[0], p1Pos[1], leftExitX,
					leftExitY, kb_p1);
			Node_Single ns = as.findPath();
			if (ns != null) {
				leftExitSet = true;
				path.clear();
			}
		}
		// right player finding exit and updating kb
		for (int y = 0; y < aintViewR.length; ++y) {
			for (int x = 0; x < aintViewR[0].length; ++x) {
				kb_p2[p2Pos[1] - sightRadius2 + y][p2Pos[0] - sightRadius2 + x] = aintViewR[y][x];

				if (aintViewR[y][x] == 2 && !rightExitSet) {
					rightExitX = p2Pos[0] - sightRadius2 + x;
					rightExitY = p2Pos[1] - sightRadius2 + y;

				}
			}
		}
		
		if (!rightExitSet && rightExitX != -1) {
			AStar_Single as = new AStar_Single(p2Pos[0], p2Pos[1], rightExitX,
					rightExitY, kb_p2);
			Node_Single ns = as.findPath();
			if (ns != null) {
				rightExitSet = true;
				path.clear();
			}
		}
		
//		if (isMapComplete(1)){
//			System.out.print("");
//		}

		// after you find the exits, call AStar
		// if not, call the modified AStar on some point given by
		// getNewSpace(playerNum)
		int direction = 0;
		Random rdmTemp = new Random();
		if (rightExitSet && leftExitSet) {
			if(path.isEmpty()){
				if(aintViewL[sightRadius1][sightRadius1] == 2)/*if p1 exited*/{
					AStar_Single as = new AStar_Single(p2Pos[0], p2Pos[1], rightExitX, rightExitY, kb_p2);
					path = as.findPath().getActionPath();
				} else if(aintViewR[sightRadius2][sightRadius2] == 2)/*if p2 exited*/{
					AStar_Single as = new AStar_Single(p1Pos[0], p1Pos[1], leftExitX, leftExitY, kb_p1);
					path = as.findPath().getActionPath();
				}
			}
			if(path.isEmpty() && readyToExit == false) //whether to explore more
			{
				AStar_2 as2temp = new AStar_2(p1Pos[0], p1Pos[1], p2Pos[0], p2Pos[1], kb_p1, kb_p2);
				as2temp.setExit1(leftExitX, leftExitY);
				as2temp.setExit2(rightExitX, rightExitY);
				if(as2temp.exitTogether())
				{
					if (as2temp.findZeroPath() != null || isMapComplete(3)) {
						path = as2temp.startFinding();
						readyToExit = true;
					}
					else //explore more
					{
						counter = 0;
						if (!isMapComplete(1) && exploreFurtherLeft) {
							path = exploreMore(1);
							if(path == null) { exploreFurtherLeft = false; }
						} else if(exploreFurtherRight){
							path = exploreMore(2);
							if(path == null) { exploreFurtherRight = false; }
						}
						if(!exploreFurtherLeft && !exploreFurtherRight)
							readyToExit = true;
					}
				}
			}
			if(!path.isEmpty())
			{
//				direction = path.remove(0);
				if (!readyToExit) {
					if (counter >= 20) {
						path.clear();
						counter = 0;
					} else {
						counter++;
					}
				}
			}
			if (path.isEmpty()) {
				System.out.println("p1: " + p1Pos[0] + "," + p1Pos[1]
						+ "   p2:" + p2Pos[0] + "," + p2Pos[1] + "   exits: "
						+ leftExitX + "," + leftExitY + "  " + rightExitX + ","
						+ rightExitY);
				if(debugging){
					for (int y = 0; y < (2 * sightRadius1) - 1; ++y) {
						for (int x = 0; x < (2 * sightRadius1) - 1; ++x) {
							if (x == sightRadius1 && y == sightRadius1) {
								System.out.print("* ");
							} else {
								System.out.print(Math.abs(kb_p1[p1Pos[1] - sightRadius1 + y][p1Pos[0] - sightRadius1 + x])	+ " ");
							}
						}
						System.out.print("\t");
						for (int x = 0; x < (2 * sightRadius1) - 1; ++x) {
							if (x == sightRadius1 && y == sightRadius1) {
								System.out.print("* ");
							} else {
								System.out.print(Math.abs(kb_p2[p2Pos[1] - sightRadius1 + y][p2Pos[0] - sightRadius1 + x]) + " ");
							}
						}
						System.out.println();
					}
				}
				AStar_2 a = new AStar_2(p1Pos[0], p1Pos[1], p2Pos[0], p2Pos[1],
						kb_p1, kb_p2);
				int minRadius = Math.min(sightRadius1, sightRadius2);
				int nextToVal = Math.max(2, minRadius/2);
				AStar_2.setNextToVal(nextToVal);
				AStar_2.invertGoNextToExit();
				a.setExit1(leftExitX, leftExitY);
				a.setExit2(rightExitX, rightExitY);
				path = a.findPath();

				/*
				 * if(a.findZeroPath() != null && isMapComplete(3)){ path =
				 * a.startFinding();
				 */
			}
			if (!path.isEmpty()) {
				direction = path.remove(0);
			}

		} else if (!leftExitSet) {
			if (path.isEmpty()) {
				singleSetThisPath = true;
				Node_Single pathNode = null;
				PriorityQueue<OurPoint> myPoints = getNewSpace(1);
				while (!myPoints.isEmpty()) {
					p = myPoints.poll();
					//AStar_Single myAStarSingle = new AStar_Single(p1Pos[0],
							//p1Pos[1], p.x, p.y, kb_p1);
					//pathNode = myAStarSingle.findPath();
					//if (pathNode != null) {
						path = p.pathToFollow;//pathNode.getActionPath();
						direction = path.remove(0);
						if (!isDirectionCorrect(direction, aintViewL, aintViewR)) {
							path.clear();
							continue;
						}
						myPoints.clear();
						singleSetThisPath = p.setBySingle;
						break;
					//}
				}
				if (path.isEmpty() && direction == 0) {
					direction = rdmTemp.nextInt(8) + 1;
					if(true){
						System.out.println("random1");
					}
				}
			} else {
				direction = path.remove(0);
				/*if(!isDirectionExit(direction, aintViewL, aintViewR)){
					singleSetThisPath = true;
				}*/
				/*
				 * if (p != null && !checkSurroundingCellsForFives(1, p.y, p.x))
				 * { if (!path.isEmpty()) path.clear(); }
				 */
			}

			while (singleSetThisPath && !isDirectionCorrect(direction, aintViewL, aintViewR)) {
				path.clear();
				direction = rdmTemp.nextInt(8) + 1;
				System.out.println("random2");
			}
			// direction = move(aintViewL, aintViewR);
		} else {
			if (path.isEmpty()) {
				singleSetThisPath = true;
				Node_Single pathNode = null;
				PriorityQueue<OurPoint> myPoints = getNewSpace(2);
				while (!myPoints.isEmpty()) {
					p = myPoints.poll();
					//AStar_Single myAStarSingle = new AStar_Single(p2Pos[0],
							//p2Pos[1], p.x, p.y, kb_p2);
					//pathNode = myAStarSingle.findPath();
					//if (pathNode != null) {
						path = p.pathToFollow;//pathNode.getActionPath();
						direction = path.remove(0);
						if (!isDirectionCorrect(direction, aintViewL, aintViewR)) {
							path.clear();
							continue;
						}
						myPoints.clear();
						singleSetThisPath = p.setBySingle;
						break;
					//}
				}
				if (path.isEmpty() && direction == 0){//pathNode == null) {
					direction = rdmTemp.nextInt(8) + 1;
					System.out.println("random3");
				}
			} else {
				direction = path.remove(0);
				/*if(!isDirectionExit(direction, aintViewL, aintViewR)){
					singleSetThisPath = true;
				}*/
				/*
				 * if (p != null && !checkSurroundingCellsForFives(2, p.y, p.x))
				 * { if (!path.isEmpty()) path.clear(); }
				 */
			}

			while (singleSetThisPath && !isDirectionCorrect(direction, aintViewL, aintViewR)) {
				path.clear();
				direction = rdmTemp.nextInt(8) + 1;
				System.out.println("random4");
			}
		}
		if(direction == 0){
			direction = this.lookAndMove(aintViewL, aintViewR);
		} else {
			turn++;

			// set new current position here
			setNewCurrentPosition(direction, aintViewL, aintViewR);
		}
		return direction;
	}

	private ArrayList<Integer> exploreMore(int player_n)
	{
		ArrayList<Integer> path_to_follow = new ArrayList<Integer>();
		boolean valid = false;
		PriorityQueue<OurPoint> points_to_see = new PriorityQueue<OurPoint>();
		points_to_see = getNewSpace(player_n);
		OurPoint to_go = points_to_see.poll();
		while(to_go!=null && valid == false)
		{
			AStar_Single ast;
			if(player_n == 1)
				ast = new AStar_Single(p1Pos[0], p1Pos[1], to_go.x, to_go.y, kb_p1);
			else //player_n = 2
				ast = new AStar_Single(p2Pos[0], p2Pos[1], to_go.x, to_go.y, kb_p2);
			path_to_follow = ast.findPath().getActionPath();
			valid = isPathValid(path_to_follow);
			if(valid == false)
				to_go = points_to_see.poll();
		}
		return path_to_follow;
	}

	private boolean isPathValid(ArrayList<Integer> pathToFollow) {
		boolean valid = true;
		boolean misalign = false;
		int[] p1Temp = p1Pos.clone();
		int[] p2Temp = p2Pos.clone();
		AStar_2 ast = new AStar_2(p1Temp[0], p1Temp[0], p1Temp[0], p1Temp[0], kb_p1, kb_p2);
		int currentDegree = ast.get_show_degree();
		int newDegree;
		int changeX;
		int changeY;
		
		for (int i = 0; i < pathToFollow.size() && valid == true; ++i)
		{
			changeX = MUMap.aintDToM[pathToFollow.get(i)][0];
			changeY = MUMap.aintDToM[pathToFollow.get(i)][1];
			p1Temp[0] = p1Temp[0] + changeX;
			p1Temp[1] = p1Temp[1] + changeY;
			p2Temp[0] = p1Temp[0] + changeX;
			p2Temp[1] = p1Temp[1] + changeY;
			//if(willItMisalign(pathToFollow.get(i), p1Temp, p2Temp))
			if (checkForMisAlignment2(pathToFollow.get(i), p1Temp, p2Temp))
			{
				misalign = true;
				ast = new AStar_2(p1Temp[0], p1Temp[1], p2Temp[0], p2Temp[1], kb_p1, kb_p2);
				newDegree = ast.get_show_degree();
				if(newDegree > currentDegree)
					valid = false;
				if(kb_p1[p1Temp[0]][p1Temp[1]]==2 || kb_p2[p2Temp[0]][p2Temp[1]]==2)
					valid = false;
			}
		}
	
		return valid;
	}

	private void initialize(int[][] aintViewL, int[][] aintViewR) {
		p = null;
		counter = 0;
		intDeltaX = 0;
		intDeltaY = 0;
		leftExitX = leftExitY = rightExitX = rightExitY = -1;
		started = true;
		sightRadius1 = (aintViewL[0].length - 1) / 2;
		sightRadius2 = (aintViewR[0].length - 1) / 2;
		kb_p1 = new int[2 * (MAX_SIZE + sightRadius1) - 1][2 * (MAX_SIZE + sightRadius1) - 1];
		kb_p2 = new int[2 * (MAX_SIZE + sightRadius2) - 1][2 * (MAX_SIZE + sightRadius2) - 1];

		p1Pos = new int[2];
		p2Pos = new int[2];
		p1Pos[0] = p2Pos[0] = MAX_SIZE - 1 + sightRadius1;
		p1Pos[1] = p2Pos[1] = MAX_SIZE - 1 + sightRadius2;
		for (int i = 0; i < kb_p1.length; ++i) {
			for (int j = 0; j < kb_p1.length; ++j) {
				kb_p1[i][j] = -5;
				kb_p2[i][j] = -5;
			}
		}
		// initialDir = 2;
		turn = -1;
		rightExitSet = false;
		leftExitSet = false;
		p1Exited = false;
		p2Exited = false;
		path = new ArrayList<Integer>();
	}

	private void setNewCurrentPosition(int direction, int[][] aintLocalViewL,
			int[][] aintLocalViewR) {

		intDeltaX = MUMap.aintDToM[direction][0];
		intDeltaY = MUMap.aintDToM[direction][1];

		// if the right player's next move is an empty space
		// update new position
		if(aintLocalViewL[sightRadius1][sightRadius1] == 2){
			p1Exited = true;
		} else if (aintLocalViewL[sightRadius1 + intDeltaY][sightRadius1 + intDeltaX] == 0) {
			p1Pos[0] += intDeltaX;
			p1Pos[1] += intDeltaY;
		} else if (aintLocalViewL[sightRadius1 + intDeltaY][sightRadius1
				+ intDeltaX] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p1Pos[0] += intDeltaX;
			// p1Pos[1] += intDeltaY;
		}

		// if the right player's next move is an empty space
		// update new position
		if(aintLocalViewR[sightRadius2][sightRadius2] == 2){
			p2Exited = true;
		} else if (aintLocalViewR[sightRadius2 + intDeltaY][sightRadius2 + intDeltaX] == 0) {
			p2Pos[0] += intDeltaX;
			p2Pos[1] += intDeltaY;
		} else if (aintLocalViewR[sightRadius2 + intDeltaY][sightRadius2
				+ intDeltaX] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p2Pos[0] += intDeltaX;
			// p2Pos[1] += intDeltaY;
		}
	}

	private boolean isDirectionCorrect(int currentDirection, int[][] aintViewL,
			int[][] aintViewR) {
		intDeltaX = MUMap.aintDToM[currentDirection][0];
		intDeltaY = MUMap.aintDToM[currentDirection][1];
		if (!leftExitSet) {
			if (aintViewL[sightRadius1 + intDeltaY][sightRadius1 + intDeltaX] == 1
					// || aintViewR[sightRadius2 + intDeltaY][sightRadius2 +
					// intDeltaX] == 1 //checks for misalignment on other player
					|| isDirectionExit(currentDirection, aintViewL, aintViewR)) {
				return false;
			}
			return true;
		} else {
			if (aintViewR[sightRadius2 + intDeltaY][sightRadius2 + intDeltaX] == 1
					// || aintViewL[sightRadius1 + intDeltaY][sightRadius1 +
					// intDeltaX] == 1 //checks for misalignment on other player
					|| isDirectionExit(currentDirection, aintViewL, aintViewR)) {
				return false;
			}
			return true;
		}
	}

	// isDirectionExit for either player
	private boolean isDirectionExit(int currentDirection, int[][] aintViewL,
			int[][] aintViewR) {
		intDeltaX = MUMap.aintDToM[currentDirection][0];
		intDeltaY = MUMap.aintDToM[currentDirection][1];
		if (aintViewL[sightRadius1 + intDeltaY][sightRadius1 + intDeltaX] == 2
				|| aintViewR[sightRadius2 + intDeltaY][sightRadius2 + intDeltaX] == 2) {
			return true;
		}
		return false;

	}

	/**
	 * Checks to see if there is nothing left to explore for a player's map
	 * 
	 * @param player
	 * @return
	 */
	private boolean isMapComplete(int player) {

		if (player == 1) {
			// loop through kb, looking for walls of 1 surrounded by -5
			for (int i = 0; i < kb_p1[0].length; i++) {
				for (int j = 0; j < kb_p1[0].length; j++) {

					if (kb_p1[i][j] == 0) {
						// start looking for 5's around that cell
						if (checkSurroundingCellsForFives(1, i, j) == true) {
							// you can still move somewhere on the board
							return false;
						}
					}
				}
			}

			return true;
		}

		else if (player == 2) {
			// loop through kb, looking for walls of 1 surrounded by -5
			for (int i = 0; i < kb_p2[0].length; i++) {
				for (int j = 0; j < kb_p2[0].length; j++) {

					if (kb_p2[i][j] == 0) {
						// start looking for 5's around that cell
						if (checkSurroundingCellsForFives(2, i, j) == true) {
							// you can still move somewhere on the board
							return false;
						}
					}
				}

			}

			return true;
		}

		else { // check to see if both players' maps are complete
				// check left player
				// loop through kb, looking for walls of 1 surrounded by -5
			for (int i = 0; i < kb_p1[0].length; i++) {
				for (int j = 0; j < kb_p1[0].length; j++) {

					if (kb_p1[i][j] == 0) {
						// start looking for 5's around that cell
						if (checkSurroundingCellsForFives(1, i, j) == true) {
							// you can still move somewhere on the board
							return false;
						}
					}
				}
			}

			// check right player
			// loop through kb, looking for walls of 1 surrounded by -5
			for (int i = 0; i < kb_p2[0].length; i++) {
				for (int j = 0; j < kb_p2[0].length; j++) {

					if (kb_p2[i][j] == 0) {
						// start looking for 5's around that cell
						if (checkSurroundingCellsForFives(2, i, j) == true) {
							// you can still move somewhere on the board
							return false;
						}
					}
				}
			}

			return true;
		}

	}

	/**
	 * Checks whether a particular cell had a -5 surrounding it
	 * 
	 * @param i
	 *            , x coordinate in kb
	 * @param j
	 *            , y coordinate in kb
	 * @return boolean whether any 0's had surrounding -5's
	 */
	private boolean checkSurroundingCellsForFives(int player, int i, int j) {
		if (player == 1) { // check player 1's surroundingCells
			for (int a = -1; a <= 1; a++) { // 3 to capture entire immediate
											// surroundings
				for (int b = -1; b <= 1; b++) {
					// skips checking current cell when past bounds in kb array
					if ((i + a < 0) || (i + a >= kb_p1.length) || (j + b < 0)
							|| (j + b >= kb_p1.length))
						continue;
					if (kb_p1[i + a][j + b] == -5)
						return true;
				}
			}
		} else if (player == 2) { // check player 2's surroundingCells
			for (int a = -1; a <= 1; a++) { // 3 to capture entire immediate
											// surroundings
				for (int b = -1; b <= 1; b++) {
					// skips checking current cell when past bounds in kb array
					if ((i + a < 0) || (i + a >= kb_p2.length) || (i + b < 0)
							|| (i + b >= kb_p2.length))
						continue;
					if (kb_p2[i + a][j + b] == -5)
						return true;
				}
			}
		} else {
			
			return true;
		}

		return false;
	}

	private class OurPoint extends Point implements Comparable {
		int dist;
		ArrayList<Integer> pathToFollow;
		int player;
		boolean setBySingle;
		int offset;

		public OurPoint(int x, int y, int currX, int currY, int player) {
			super(x, y);
			setBySingle = true;
			int[][] map;
			this.player = player;
			boolean ignoreOtherPlayer = p1Exited || p2Exited;
			if(player == 1){
				map = kb_p1;
			} else {
				map = kb_p2;
			}
			//dist = Math.max(Math.abs(x - currX), Math.abs(y - currY));
			AStar_Single as = new AStar_Single(currX, currY, x, y, map);
			Node_Single ns = as.findPath();
			if(ns == null){
				dist = Integer.MAX_VALUE;
			} else {
				pathToFollow = ns.getActionPath();
				if(!ignoreOtherPlayer && wouldEitherPlayerStepOnExit(pathToFollow)){
					dist = Integer.MAX_VALUE - 1000 + pathToFollow.size();
				} else {
					dist = pathToFollow.size();
				}
			}
			
			if(dist < Integer.MAX_VALUE - 1000){
				int[] p1 = new int[2];
				int[] p2 = new int[2];
				if(player == 1){
					p1[0] = x;
					p1[1] = y;
					p2 = ifPlayerFollowedPath(2, pathToFollow);
				} else {
					p2[0] = x;
					p2[1] = y;
					p1 = ifPlayerFollowedPath(1, pathToFollow);
				}
				int originalXDifference = p1Pos[0] - p2Pos[0];
				int originalYDifference = p1Pos[1] - p1Pos[1];
				int newXDifference = p1[0] - p2[0];
				int newYDifference = p1[1] - p2[1];
				int xOffset = originalXDifference - newXDifference;
				int yOffset = originalYDifference - newYDifference;
				offset = xOffset + yOffset;
			}
		}

		public String toString() {
			return "x: " + super.x + "  y: " + super.y + "  dist: " + dist;
		}

		@Override
		public int compareTo(Object o) {
			if (!(o instanceof OurPoint)) {
				return -1;
			}
			OurPoint p = (OurPoint) o;
			/*if (this.offset > p.offset){
				return 1;
			} else if (this.offset < p.offset){
				return -1;
			}else */if (this.dist > p.dist) {
				return 1;
			} else if (this.dist < p.dist) {
				return -1;
			}
			return 0;
		}
	}

	// So if there is more to explore in the map, then explore more.
	// Which node should you explore?
	// Let's try the one closest to you that's available
	private PriorityQueue<OurPoint> getNewSpace(int player) {
		PriorityQueue<OurPoint> points = new PriorityQueue<OurPoint>();
		PriorityQueue<OurPoint> badPoints = new PriorityQueue<OurPoint>();

		if (player != 2) {
			// loop through kb, starting near your current pos
			for (int i = p1Pos[1]; i < kb_p1.length; i++) {
				for (int j = p1Pos[0]; j < kb_p1.length; j++) {
					if (p1Pos[0] == j && p1Pos[1] == i)
						continue;
					if (kb_p1[i][j] == 0
							&& checkSurroundingCellsForFives(1, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p1Pos[0], p1Pos[1], 1);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
				for (int j = p1Pos[0] - 1; j >= 0; j--) {
					if (p1Pos[0] == j && p1Pos[1] == i)
						continue;
					if (kb_p1[i][j] == 0
							&& checkSurroundingCellsForFives(1, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p1Pos[0], p1Pos[1], 1);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
			}
			// loop through kb, starting near your current pos
			for (int i = p1Pos[1] - 1; i >= 0; i--) {
				for (int j = p1Pos[0]; j >= 0; j--) {
					if (p1Pos[0] == j && p1Pos[1] == i)
						continue;
					if (kb_p1[i][j] == 0
							&& checkSurroundingCellsForFives(1, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p1Pos[0], p1Pos[1], 1);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
				for (int j = p1Pos[0] + 1; j < kb_p1.length; j++) {
					if (p1Pos[0] == j && p1Pos[1] == i)
						continue;
					if (kb_p1[i][j] == 0
							&& checkSurroundingCellsForFives(1, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p1Pos[0], p1Pos[1], 1);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
			}
		}
		if (player != 1) {
			// loop through kb, starting near your current pos
			for (int i = p2Pos[1]; i < kb_p2.length; i++) {
				for (int j = p2Pos[0]; j < kb_p2.length; j++) {
					if (p2Pos[0] == j && p2Pos[1] == i)
						continue;
					if (kb_p2[i][j] == 0
							&& checkSurroundingCellsForFives(2, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p2Pos[0], p2Pos[1], 2);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
				for (int j = p2Pos[0] - 1; j >= 0; j--) {
					if (p2Pos[0] == j && p2Pos[1] == i)
						continue;
					if (kb_p2[i][j] == 0
							&& checkSurroundingCellsForFives(2, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p2Pos[0], p2Pos[1], 2);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
			}
			// loop through kb, starting near your current pos
			for (int i = p2Pos[1] - 1; i >= 0; i--) {
				for (int j = p2Pos[0]; j >= 0; j--) {
					if (p2Pos[0] == j && p2Pos[1] == i)
						continue;
					if (kb_p2[i][j] == 0
							&& checkSurroundingCellsForFives(2, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p2Pos[0], p2Pos[1], 2);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
				for (int j = p2Pos[0] + 1; j < kb_p2.length; j++) {
					if (p2Pos[0] == j && p2Pos[1] == i)
						continue;
					if (kb_p2[i][j] == 0
							&& checkSurroundingCellsForFives(2, i, j) == true) {
						OurPoint op = new OurPoint(j, i, p2Pos[0], p2Pos[1], 2);
						if(op.dist < Integer.MAX_VALUE - 1000){
							points.add(op);
						} else if(op.dist < Integer.MAX_VALUE){
							badPoints.add(op);
						}
					}
				}
			}
		}

		if(points.isEmpty()){
			while(!badPoints.isEmpty()){
				OurPoint op = badPoints.poll(); 
				do{
					op.pathToFollow.remove(op.pathToFollow.size() - 1);
				}while(!op.pathToFollow.isEmpty() && wouldEitherPlayerStepOnExit(op.pathToFollow));
				if(op.pathToFollow.isEmpty()){
					continue;
				}
				int[] p1 = new int[2];
				int[] p2 = new int[2];
				if(op.player == 1){
					p2 = ifPlayerFollowedPath(2, op.pathToFollow);
					p1[0] = op.x;
					p1[1] = op.y;
				} else {
					p1 = ifPlayerFollowedPath(1, op.pathToFollow);
					p2[0] = op.x;
					p2[1] = op.y;
				}
				if (debugging) {
					for (int y = 0; y < (6 * sightRadius1) - 1; ++y) {
						for (int x = 0; x < (6 * sightRadius1) - 1; ++x) {
							if (x == sightRadius1 * 3 && y == sightRadius1 * 3) {
								System.out.print("* ");
							} else if (p1Pos[1] - 3 * sightRadius1 + y == p1[1]
									&& p1Pos[0] - 3 * sightRadius1 + x == p1[0]) {
								System.out.print("@ ");
							} else {
								System.out.print(Math.abs(kb_p1[p1Pos[1] - 3
										* sightRadius1 + y][p1Pos[0] - 3
										* sightRadius1 + x])
										+ " ");
							}
						}
						System.out.print("\t");
						for (int x = 0; x < (6 * sightRadius1) - 1; ++x) {
							if (x == sightRadius1 * 3 && y == sightRadius1 * 3) {
								System.out.print("* ");
							} else if (p2Pos[1] - 3 * sightRadius1 + y == p2[1]
									&& p2Pos[0] - 3 * sightRadius1 + x == p2[0]) {
								System.out.print("@ ");
							} else {
								System.out.print(Math.abs(kb_p2[p2Pos[1] - 3
										* sightRadius1 + y][p2Pos[0] - 3
										* sightRadius1 + x])
										+ " ");
							}
						}
						System.out.println();
					}
					System.out.println("\n\n");
				}
				AStar_2 a2 = new AStar_2(p1Pos[0], p1Pos[1], p2Pos[0], p2Pos[1], kb_p1, kb_p2);
				a2.setExit1(p1[0], p1[1]);
				a2.setExit2(p2[0], p2[1]);
				a2.setGoingToExit(false);
				Node_2.setFocus(op.player);
				a2.setMaxNodes(32 * (op.dist - Integer.MAX_VALUE + 1000));
				op.pathToFollow = a2.findPath();
				op.setBySingle = false;
				points.add(op);
				break;
			}
			//OurPoint copOut = new OurPoint(p1Pos[0], p1Pos[1], p1Pos[0], p1Pos[1], 1);
			//points.add(copOut);
		}
		/*
					int[] spotToBe;
					AStar_2 a2 = new AStar_2(p1Pos[0], p1Pos[1], p2Pos[0], p2Pos[1], kb_p1, kb_p2);
					if(player == 1){
						spotToBe = ifPlayerFollowedPath(2, pathToFollow);
						a2.setExit1(x, y);
						a2.setExit2(spotToBe[0], spotToBe[1]);
					} else {
						spotToBe = ifPlayerFollowedPath(1, pathToFollow);
						a2.setExit2(x, y);
						a2.setExit1(spotToBe[0], spotToBe[1]);
					}
					pathToFollow = a2.findPath();
					if(pathToFollow.isEmpty()){
						dist = Integer.MAX_VALUE;
					} else {
						dist = pathToFollow.size();
					}*/
		return points;
	}

	private boolean checkForMisAlignment(int currentDir2,
			int[][] aintLocalViewL, int[][] aintLocalViewR) {

		int intDeltaX2 = MUMap.aintDToM[currentDir2][0];
		int intDeltaY2 = MUMap.aintDToM[currentDir2][1];
		int[] p1PosFuture = p1Pos.clone();
		int[] p2PosFuture = p2Pos.clone();

		// if the left player's next move is an empty space
		// update new position
		if (aintLocalViewL[sightRadius1 + intDeltaY2][sightRadius1 + intDeltaX2] == 0) {
			p1PosFuture[0] += intDeltaX2;
			p1PosFuture[1] += intDeltaY2;
		} else if (aintLocalViewL[sightRadius1 + intDeltaY2][sightRadius1
				+ intDeltaX2] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p1PosFuture[0] += intDeltaX2;
			// p1PosFuture[1] += intDeltaY2;
		}

		// if the right player's next move is an empty space
		// update new position
		if (aintLocalViewR[sightRadius2 + intDeltaY2][sightRadius2 + intDeltaX2] == 0) {
			p2PosFuture[0] += intDeltaX2;
			p2PosFuture[1] += intDeltaY2;
		} else if (aintLocalViewR[sightRadius2 + intDeltaY][sightRadius2
				+ intDeltaX] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p2PosFuture[0] += intDeltaX2;
			// p2PosFuture[1] += intDeltaY2;
		}

		int currentDir3 = currentDir2 + 4;
		currentDir3 = currentDir3 % 8;

		int intDeltaX3 = MUMap.aintDToM[currentDir3][0];
		int intDeltaY3 = MUMap.aintDToM[currentDir3][1];

		// if the right player's next move is an empty space
		// update new position
		if (kb_p1[p1PosFuture[1] + intDeltaY3][p1PosFuture[0] + intDeltaX3] == 0
				/*|| kb_p1[p1PosFuture[1] + intDeltaY3][p1PosFuture[0] + intDeltaX3] == 3*/) {
			p1PosFuture[0] += intDeltaX3;
			p1PosFuture[1] += intDeltaY3;
		} else if (kb_p1[p1PosFuture[1] + intDeltaY3][p1PosFuture[0]
				+ intDeltaX3] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p1PosFuture[0] += intDeltaX3;
			// p1PosFuture[1] += intDeltaY3;
		}

		// if the right player's next move is an empty space
		// update new position
		if (kb_p2[p2PosFuture[1] + intDeltaY3][p2PosFuture[0] + intDeltaX3] == 0) {
			p2PosFuture[0] += intDeltaX3;
			p2PosFuture[1] += intDeltaY3;
		} else if (kb_p2[p2PosFuture[1] + intDeltaY3][p2PosFuture[0]
				+ intDeltaX3] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p2PosFuture[0] += intDeltaX3;
			// p2PosFuture[1] += intDeltaY3;
		}

		if (p1Pos[0] == p1PosFuture[0] && p1Pos[1] == p1PosFuture[1]
				&& p2Pos[0] == p2PosFuture[0] && p2Pos[1] == p2PosFuture[1]) {
			// not misaligned
			return false;
		}

		return true;

	}

	private boolean checkForMisAlignment2(int currentDir2, 
			int[] p1PosTemp, int[] p2PosTemp){
		
		int intDeltaX2 = MUMap.aintDToM[currentDir2][0];
		int intDeltaY2 = MUMap.aintDToM[currentDir2][1];
		int[] p1PosFuture = p1PosTemp.clone();
		int[] p2PosFuture = p2PosTemp.clone();

		// if the left player's next move is an empty space
		// update new position
		if (kb_p1[p1PosFuture[1] + intDeltaY2][p1PosFuture[0] + intDeltaX2] == 0) {
			p1PosFuture[0] += intDeltaX2;
			p1PosFuture[1] += intDeltaY2;
		} else if (kb_p1[p1PosFuture[1] + intDeltaY2][p1PosFuture[0] + intDeltaX2] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p1PosFuture[0] += intDeltaX2;
			// p1PosFuture[1] += intDeltaY2;
		}

		// if the right player's next move is an empty space
		// update new position
		if (kb_p2[p2PosFuture[1] + intDeltaY2][p2PosFuture[0] + intDeltaX2] == 0) {
			p2PosFuture[0] += intDeltaX2;
			p2PosFuture[1] += intDeltaY2;
		} else if (kb_p2[p2PosFuture[1] + intDeltaY2][p2PosFuture[0] + intDeltaX2] == 0) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p2PosFuture[0] += intDeltaX2;
			// p2PosFuture[1] += intDeltaY2;
		}

		int currentDir3 = currentDir2 + 4;
		currentDir3 = currentDir3 % 8;

		int intDeltaX3 = MUMap.aintDToM[currentDir3][0];
		int intDeltaY3 = MUMap.aintDToM[currentDir3][1];

		// if the right player's next move is an empty space
		// update new position
		if (kb_p1[p1PosFuture[1] + intDeltaY2][p1PosFuture[0] + intDeltaX2] == 0){
				/*|| kb_p1[p1PosFuture[1] + intDeltaY3][p1PosFuture[0] + intDeltaX3] == 3*/
			p1PosFuture[0] += intDeltaX3;
			p1PosFuture[1] += intDeltaY3;
		} else if (kb_p1[p1PosFuture[1] + intDeltaY2][p1PosFuture[0] + intDeltaX2] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p1PosFuture[0] += intDeltaX3;
			// p1PosFuture[1] += intDeltaY3;
		}

		// if the right player's next move is an empty space
		// update new position
		if (kb_p2[p2PosFuture[1] + intDeltaY2][p2PosFuture[0] + intDeltaX2] == 0) {
			p2PosFuture[0] += intDeltaX3;
			p2PosFuture[1] += intDeltaY3;
		} else if (kb_p2[p2PosFuture[1] + intDeltaY2][p2PosFuture[0] + intDeltaX2] == 1) {
			// nothing changes, you couldn't move, and so you are in the same
			// place
		} else { // you hit the exit
			// p2PosFuture[0] += intDeltaX3;
			// p2PosFuture[1] += intDeltaY3;
		}

		if (p1Pos[0] == p1PosFuture[0] && p1Pos[1] == p1PosFuture[1]
				&& p2Pos[0] == p2PosFuture[0] && p2Pos[1] == p2PosFuture[1]) {
			// not misaligned
			return false;
		}

		return true;
	}
	
	public int checkRadiusForFives(int player, int px, int py) {
		int[][] map;
		int radius;
		if (player == 1) {
			if (leftExitSet) {
				return 0;
			}
			radius = sightRadius1;
			map = kb_p1;
		} else {
			if (rightExitSet) {
				return 0;
			}
			radius = sightRadius2;
			map = kb_p2;
		}

		int total = 0;
		for (int y = 0; y < (2 * radius) + 1; ++y) {
			for (int x = 0; x < (2 * radius) + 1; ++x) {
				if (map[py - radius + y][px - radius + x] == -5) {
					++total;
				}
			}
		}

		return total;

	}
	
	private boolean wouldEitherPlayerStepOnExit(ArrayList<Integer> aPath){
		int testingX = ifPlayerFollowedPath(1, aPath)[0];
		if(testingX == -1){
			return true;
		}
		testingX = ifPlayerFollowedPath(2, aPath)[0];
		return testingX == -1;
	}
	
	public boolean willItMisalign(int d, int[] p1predictPos, int[] p2predictPos)
	{
		boolean misaligning = false;
		int[] p1PosFuture = new int[2];
		int[] p2PosFuture = new int[2];

		p1PosFuture[0] = p1predictPos[0] + MUMap.aintDToM[d][0];
		p1PosFuture[1] = p1predictPos[1] + MUMap.aintDToM[d][1];
		p2PosFuture[0] = p2predictPos[0] + MUMap.aintDToM[d][0];
		p2PosFuture[1] = p2predictPos[1] + MUMap.aintDToM[d][1];
		
		int p1_going = kb_p1[p1PosFuture[0]][p1PosFuture[1]];
		int p2_going = kb_p2[p2PosFuture[0]][p2PosFuture[1]];
		if((p1_going == 1 || p2_going == 1) && (p1_going != 1 || p2_going != 1)) 
			misaligning = true;
		return misaligning;
	}

	public int[] ifPlayerFollowedPath(int player,
			ArrayList<Integer> pathToFollow) {
		int[][] map;
		int x;
		int y;
		if (player == 1) {
			map = kb_p1;
			x = p1Pos[0];
			y = p1Pos[1];
		} else {
			map = kb_p2;
			x = p2Pos[0];
			y = p2Pos[1];
		}

		for (int i = 0; i < pathToFollow.size(); ++i) {
			int changeX = MUMap.aintDToM[pathToFollow.get(i)][0];
			int changeY = MUMap.aintDToM[pathToFollow.get(i)][1];
			x += changeX;
			y += changeY;
			if (map[y][x] == 1 || map[y][x] == -5) {
				x -= changeX;
				y -= changeY;
			}
			if (map[y][x] == 2) {
				int[] temp = { -1, -1 };
				return temp;
			}
		}

		int[] toReturn = new int[2];
		toReturn[0] = x;
		toReturn[1] = y;

		return toReturn;
	}

}
