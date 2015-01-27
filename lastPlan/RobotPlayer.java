package lastPlan;

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
	static boolean madeMine = false;
	static int minersSpawned = 0;
	static int handsWashed = 0;
	

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
				double ore = rc.getTeamOre();
				
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
					

					if (rc.isCoreReady() && ore >= 100 && tims < 10) {
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
					
						if(ore >= 500 && !madeMine && rc.isCoreReady()){
							tryBuild(Direction.NORTH, RobotType.MINERFACTORY);
							madeMine = true;
						}
					if (rc.isCoreReady()) {
						// Jong-Il the Wandering Janitor
						if(fate == 0){
							// Choose mine or wander or wash
							if(rc.isCoreReady()){
								int n = rand.nextInt(20);
								if( n >= 11)
									lastDir = wander(lastDir);
								else if(n >=1)
									tryMine();
								else if(ore>=200 && handsWashed < 2){
									tryBuild(Direction.NORTH,RobotType.HANDWASHSTATION);
									handsWashed++;
								}
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
							else if (rc.isCoreReady() && ore >= 500) {
								tryBuild(me.directionTo(tower), RobotType.BARRACKS);
								fate = 1 + rand.nextInt(towerCount);
							}
						} 
						/*// Rick the Mine Builder
						else if(fate >=19 && fate <=24){
							// Go towards high ore or build mine
							if(rc.isCoreReady()){
								int n = rand.nextInt(20);
								// Build
								if(ore >= 500 && n==0){
									tryBuild(Direction.NORTH, RobotType.MINERFACTORY);
								}
								// Move
								else if(rc.isCoreReady()){
									// Randomly choose 10 locations, get best
									MapLocation best = rc.getLocation();
									double load = 0;
									// Get all in range
									MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(best, myRange);
									
									for(int i = 0; i<10;i++){
										// Generate Location within range
										MapLocation loc = locs[rand.nextInt(locs.length)];
										// If normal terrain, check if better
										if(rc.senseTerrainTile(loc) == TerrainTile.NORMAL){
											double locLoad = rc.senseOre(loc);
											if(locLoad>load){
												best = loc;
												load = locLoad;
											}	
										}
									}
									
									// Move
									tryMove(rc.getLocation().directionTo(best));
										
								}
							}
								
						}*/
						
						
					}

				}else if(rc.getType()==RobotType.BARRACKS){
					// Will trySpawn basher 1/15 of time
					if (rc.isCoreReady() && ore >= 80 && rand.nextInt(15) == 0) {
						trySpawn(rc.getLocation().directionTo(towers[getNearTower()]), RobotType.BASHER);
					}
				}else if(rc.getType()==RobotType.BASHER){
					// Pursue enemy else move to closest tower
					RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
					int enemyCount = enemies.length;
					MapLocation me = rc.getLocation();
					MapLocation nearestTower = towers[getNearTower()];
					if (rc.isCoreReady()&&enemyCount > 0) {
						tryMove(me.directionTo(enemies[rand.nextInt(enemyCount)].location));
					} else if(rc.isCoreReady()&&me.distanceSquaredTo(nearestTower)>35){
						tryMove(me.directionTo(towers[getNearTower()]));
					} else if(rc.isCoreReady()&&me.distanceSquaredTo(towers[getNearTower()])<6){
						tryMove(intToDirection((directionToInt(me.directionTo(nearestTower)) + 4 )));
					}
				}else if(rc.getType()==RobotType.MINERFACTORY){
					int n = rand.nextInt(10);
					if( n < 3 && rc.isCoreReady() && ore >=60 && minersSpawned < 10){
						trySpawn(Direction.NORTH, RobotType.MINER);
						minersSpawned++;
					}
				}else if(rc.getType()==RobotType.MINER){
					int n = rand.nextInt(3);
					
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					
					if(rc.isCoreReady() && n<2)
						tryMine();
					else if(rc.isCoreReady()){
							// Randomly choose 10 locations, get best
							MapLocation best = rc.getLocation();
							double load = 0;
							// Get all in range
							MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(best, myRange);
							
							for(int i = 0; i<10;i++){
								// Generate Location within range
								MapLocation loc = locs[rand.nextInt(locs.length)];
								// If normal terrain, check if better
								if(rc.senseTerrainTile(loc) == TerrainTile.NORMAL){
									double locLoad = rc.senseOre(loc);
									if(locLoad>load){
										best = loc;
										load = locLoad;
									}	
								}
							}
							
							// Move
							tryMove(rc.getLocation().directionTo(best));
								
						}
					
					
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
		switch(d % 8) {
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
	// 0 		Wandering Janitor	Jong-Il
	// 1-6 		Barracks			Bob
	// 7-12		Helipad				Quincy
	// 13-18 	Aerospace			Jones
	// 19-24	Mine				Rick
	static int hireTim(int fate){
		if(fate <= 15){
			return 0;
		} else return 1 + fate % towerCount;
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

