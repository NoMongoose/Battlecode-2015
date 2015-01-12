package goldteamrules;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {

	static RobotController rc;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static Team myTeam;
	static Team enemyTeam;
	static int myRange;
	static Random rand;
	static MapLocation homebase;
	static MapLocation enemybase;
	static MapLocation[] towers;
	

	public static void run(RobotController boop){
		rc = boop;
		rand = new Random(rc.getID());
		
		myRange = rc.getType().attackRadiusSquared;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		RobotInfo[] myRobots;
		
		// Get own tower locations
		towers = rc.senseTowerLocations();
		int towerCount = towers.length;
		
		// Get own HQ location
		homebase = rc.senseHQLocation();
		// Get enemy HQ location
		enemybase = rc.senseEnemyHQLocation();
		
		// Generate  Fate
		int fate = (int) (100 * Math.random());
		
		// Hire
		if(rc.getType()==RobotType.BEAVER){
			fate = hireTim(fate);
		} else if(rc.getType()==RobotType.BASHER){
			fate = hireBasher(fate, towerCount);
		}
		
		// Last direction moved as int 0->7
		int lastDir = (int)(8*Math.random());
		
		while(true){
			
			try {
				if(rc.getType()==RobotType.HQ){
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					
					myRobots = rc.senseNearbyRobots(999999, myTeam);
					int tims = 0;
					for (RobotInfo r : myRobots) {
						RobotType type = r.type;
						if (type == RobotType.BEAVER) {
							tims++;
						}
					}
					

					if (rc.isCoreReady() && rc.getTeamOre() >= 100 && tims < 20) {
						trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
					}
				}else if(rc.getType()==RobotType.TOWER){
					if (rc.isWeaponReady()) {
						attackSomething();
					}
				}else if(rc.getType()==RobotType.BEAVER){
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						// Jong-Il
						if(fate == 0 || true){
							// Choose mine or wander
							if( Math.random() > .5)
								lastDir = wander(lastDir);
							else tryMine();
						}
					}

					
					
				}else if(rc.getType()==RobotType.AEROSPACELAB){
					
				}else if(rc.getType()==RobotType.LAUNCHER){
					
				} else if(rc.getType()==RobotType.MISSILE){
					
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			rc.yield();
		}
	}
	
	
	static boolean isNearHome(int threshold){
		MapLocation me = rc.getLocation();
		return threshold * me.distanceSquaredTo(homebase) < me.distanceSquaredTo(enemybase);
	}
	
	static int wander(int lDir){
		int newDir;
		if( isNearHome(4))
			newDir = lDir - 3 + (int) (6*Math.random());
		else{
			newDir = directionToInt(rc.getLocation().directionTo(homebase));
		}
		tryMove(intToDirection(newDir));
		return newDir;
	}

	// This method will attack an enemy in sight, if there is one
	static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}
	
	static void tryMove(Direction d){
		try {
		
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			int dirint = directionToInt(d);
			while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				
					rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
		} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
	
	static void tryMine(){
		try{
			if(rc.canMine() && rc.isCoreReady()){

				
				rc.mine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	static void tryBuild(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}
	static void trySpawn(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}

	static int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}
	
	static Direction intToDirection(int d) {
		switch(d) {
			case 0:
				return Direction.NORTH;
			case 1:
				return Direction.NORTH_EAST;
			case 2:
				return Direction.EAST;
			case 3:
				return Direction.SOUTH_EAST;
			case 4:
				return Direction.SOUTH;
			case 5:
				return Direction.SOUTH_WEST;
			case 6:
				return Direction.WEST;
			case 7:
				return Direction.NORTH_WEST;
			default:
				return Direction.NONE;
		}
	}
	
	// Precondition: fate: (0,99)
	// Returns Job
	// Jobs:
	// 0 Scout/Miner	Jong-Il
	// 1 Barracks		Bob
	// 2 Helipad		Quincy
	// 3 Aerospace		Jones
	// 4 Mine			Rick
	static int hireTim(int fate){
		if(fate <= 33){
			return 0;
		} else if(fate <= 59){
			return 1;
		} else if( fate <= 67){
			return 2;
		} else if( fate <= 82){
			return 3;
		} else return 4;
	}
	
	static int hireBasher(int fate, int towerCount){
		if(fate<10){
			// Wanderer
			return -1;
		} else{
			// Defender
			// fate = index of tower
			return fate % towerCount;
		}
			
	}
	
	
}
