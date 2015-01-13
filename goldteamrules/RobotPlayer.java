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
	static int towerCount;
	

	public static void run(RobotController boop){
		rc = boop;
		rand = new Random(rc.getID());
		
		
		myRange = rc.getType().attackRadiusSquared;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		RobotInfo[] myRobots;
		
		// Get own tower locations
		towers = rc.senseTowerLocations();
		towerCount = towers.length;
		
		// Get own HQ location
		homebase = rc.senseHQLocation();
		// Get enemy HQ location
		enemybase = rc.senseEnemyHQLocation();
		
		// Generate  Fate
		int fate = rand.nextInt(100);
		System.out.println("Fate init: " + fate);
		
		// Hire
		if(rc.getType()==RobotType.BEAVER){
			fate = hireTim(fate);
		} else if(rc.getType()==RobotType.BASHER){
			// Not used
			fate = hireBasher(fate, towerCount);
		}
		
		// Last direction moved as int 0->7
		int lastDir = rand.nextInt(8);
		
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
					

					if (rc.isCoreReady() && rc.getTeamOre() >= 100 && tims < 10) {
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
						// Jong-Il the Wandering Miner
						if(fate == 0){
							// Choose mine or wander
							if(rc.isCoreReady()){
								if( rand.nextInt(10) >= 5)
								lastDir = wander(lastDir);
							else tryMine();
							}
						// Bob the Barracks Builder
						} else if(fate<7){
							MapLocation me = rc.getLocation();
							MapLocation tower = towers[fate-1];
							// Move to tower?
							if(rc.isCoreReady()&&me.distanceSquaredTo(tower) >= 10){
								tryMove(me.directionTo(tower));
							} 
							// If in range, build Barracks, assign new tower
							else if (rc.isCoreReady() && rc.getTeamOre() >= 500) {
								tryBuild(me.directionTo(tower), RobotType.BARRACKS);
								fate = 1 + rand.nextInt(towerCount);
							}
							
							
						}
					}

				}else if(rc.getType()==RobotType.BARRACKS){
					// Will trySpawn basher 1/10 of time
					if (rc.isCoreReady() && rc.getTeamOre() >= 80 && rand.nextInt(10) == 0) {
						trySpawn(rc.getLocation().directionTo(towers[getNearTower()]), RobotType.BASHER);
					}
				}else if(rc.getType()==RobotType.BASHER){
					// Move around closest tower
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			rc.yield();
		}
	}
	
	static int getNearTower(){
		int index = 0;
		int distance = 120*120;
		MapLocation me = rc.getLocation();
		for(int i = 0;i<towerCount;i++){
			if(me.distanceSquaredTo(towers[i])<distance)
				index = i;
		}
		return index;
	}
	
	static boolean isNearHome(int threshold){
		MapLocation me = rc.getLocation();
		return threshold * me.distanceSquaredTo(homebase) < me.distanceSquaredTo(enemybase);
	}
	
	static int wander(int lDir){
		int newDir;
		if( isNearHome(4))
			newDir = lDir - 3 + rand.nextInt(7);
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
	// 0 		Scout/Miner		Jong-Il
	// 1-6 		Barracks		Bob
	// 7-12		Helipad			Quincy
	// 13-18 	Aerospace		Jones
	// 19-24	Mine			Rick
	static int hireTim(int fate){
		System.out.println(fate);
		if(fate <= 33){
			System.out.println("Jong-Il");
			return 0;
		} else if(fate <= 59){
			System.out.println("Bob");
			return 1 + fate % towerCount;
		} else if( fate <= 67){
			System.out.println("Heli");
			return 7 + fate % towerCount;
		} else if( fate <= 82){
			System.out.println("Aero");
			return 13 + fate % towerCount;
		} else{
			System.out.println("Mine");
			return 19 + fate % towerCount;
		}
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
