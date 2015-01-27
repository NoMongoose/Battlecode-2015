package goldteamrules;

import java.util.ArrayList;
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
	static MapLocation[] enemyTowers;
	static int towerCount;
	static int lastDir;
	static MapLocation startLoc = new MapLocation(-1, -1);
	static boolean pathFound = false;
	static String phase = "defensive";
	static int fate;
	//array list of primitive arrays (one for each tower, ours and enemies)
	static ArrayList<MapLocation[]> dijkstraMaps;
	static int MAP_WD;
	static int MAP_HG;
	static double ore;
	

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
		
		enemyTowers = rc.senseEnemyTowerLocations();
		
		// Get own HQ location
		homebase = rc.senseHQLocation();
		// Get enemy HQ location
		enemybase = rc.senseEnemyHQLocation();
		
		// Generate  Fate
		fate = rand.nextInt(100);
		
		// Hire
		if(rc.getType()==RobotType.BEAVER){
			fate = hireTim(fate);
		} else if(rc.getType()==RobotType.BASHER){
			// Not used
			fate = hireBasher(fate, towerCount);
		}
		
		// Last direction moved as int 0->7
		lastDir = rand.nextInt(8);
		
		while(true){
			
			try {
				ore = rc.getTeamOre();
				
				if(rc.getType()==RobotType.HQ){
					/*
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
					*/
					HQAI();
				}else if(rc.getType()==RobotType.TOWER){
					if (rc.isWeaponReady()) {
						attackSomething();
					}
				}else if(rc.getType()==RobotType.BEAVER){
					if (rc.isWeaponReady()) {
						attackSomething();
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
								else if(ore>=200)
									tryBuild(Direction.NORTH,RobotType.HANDWASHSTATION);
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
						/*
						// Rick the Mine Builder
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
						*/
						
					}

				}else if(rc.getType()==RobotType.BARRACKS){
					// Will trySpawn basher 1/15 of time
					if (rc.isCoreReady() && ore >= 80 && rand.nextInt(15) == 0) {
						trySpawn(rc.getLocation().directionTo(towers[getNearTower()]), RobotType.BASHER);
					}
				}else if(rc.getType()==RobotType.BASHER){
					/*// Pursue enemy else move to closest tower
					RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
					int enemyCount = enemies.length;
					MapLocation me = rc.getLocation();
					if (rc.isCoreReady()&&enemyCount > 0) {
						tryMove(me.directionTo(enemies[rand.nextInt(enemyCount)].location));
					} else if(rc.isCoreReady()){
						tryMove(me.directionTo(towers[getNearTower()]));
					}*/
					doBasher();
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
			if (offsetIndex < 5 && rc.isCoreReady()) {
				
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
	// 0 		Wandering Janitor	Jong-Il
	// 1-6 		Barracks			Bob
	// 7-12		Helipad				Quincy
	// 13-18 	Aerospace			Jones
	// 19-24	Mine				Rick
	static int hireTim(int fate){
		if(fate <= 33){
			return 0;
		} else if(fate <= 59){
			return 1 + fate % towerCount;
		} else if( fate <= 67){
			return 7 + fate % towerCount;
		} else if( fate <= 82){
			return 13 + fate % towerCount;
		} else{
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
	
	static void pathfindTo(MapLocation location){
    int direction;
    MapLocation me = rc.getLocation();
    int xPos = me.x;
    int yPos = me.y;
    double curDijkstraNum = getDijkstraNumAt(xPos, yPos);
    //potentially more efficient to use if/else than loop here
    for (int y = -1; y < 2; y++)
        for (int x = -1; x < 2; x++)
            if( x == 0 || y == 0 )//dont go diagonally
                if (getDijkstraNumAt(xPos+x, yPos+y) < curDijkstraNum)
                    direction = coordsToDirection(x, y);
    if(rc.isCoreReady())
    	tryMove(intToDirection(direction)); //fine to go in a close direction
	
	}

	static void pathfindToTower(int fate){
		pathfindTo(towers[fate % towerCount]);
	}

	static void wanderToTower(int fate){
		wanderTo(towers[fate % towerCount]);
	}

	static void wanderTo(MapLocation location){
	    if( rand.nextInt(100) < 33 )
	        wander(lastDir);
	    else{
	        int x = rc.getLocation().x-location.x;
	        if(x!=0)
	        	x /= Math.abs(x);
	
	        int y = rc.getLocation().y-location.y;
	        if(y!=0)
	        	y /= Math.abs(y);
	        

	
	        Direction dir = coordsToDirection(x, y);
	        tryMove(dir); //fine to go in a close direction
	    }
	}
	
	static void chaseNearestEnemy(int distance){
	    if (startLoc.x != -1 && startLoc.y != -1)
	        startLoc = rc.getLocation();
	    MapLocation location = rc.senseEnemyLocations()[0];
	    if (distanceBetween(rc.getLocation(), location) > myRange){
	        if (pathFound)
	            pathfindTo(location);
	        else
	            wanderTo(location);
	        chaseNearestEnemy(distance); //recursion!
	    }
	    else if (rc.getType() != RobotType.BASHER && rc.isWeaponReady()) //bashers attack automatically
	        attackSomething();//("unit");
	    if (distance != -1)
	        if( distanceBetween(startLoc, rc.getLocation()) > distance * distance)
	        	rc.yield();
        
	}
	
	static void chaseNearestBuilding(int distance){
		asdfasdf
	}
	
	static MapLocation nearbyEnemyLoc(){
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
	    for (int i = 0; i < enemies.length; i++){
	        double dist = distanceBetween(rc.getLocation(), enemies[i].location);
	        if( dist < myRange/.66)
	            return enemies[i].location;
	    }
	    return new MapLocation(-1,-1);
	}
	
	static boolean enemyNearby(){
	    RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
	    for (int i = 0; i < enemies.length; i++){
	        double dist = distanceBetween(rc.getLocation(), enemies[i].location);
	        if( dist < myRange/.66)
	            return true;
	    }
	    return false;
	}
	
	static MapLocation enemyBuildingNearby(){
	asdfasdf
	}
	
	static void callForHelp(){	
		//rc.broadcast(rc.getLocation()); //todo: find a way to encode this well
		//hq will store broadcasts and send orders to units based on them
	}
	
	
	static boolean enoughSupply(){
		// Caleb
		return true;
	}
	
	static Direction coordsToDirection(int x, int y){
	    return rc.getLocation().directionTo(new MapLocation(x, y));
	    		
	}
	
	static int distanceBetween(MapLocation a, MapLocation b){
		int dx = a.x - b.x;
		int dy = a.y - b.y;
		return dx * dx + dy * dy;
	}
	
	static boolean isAtTower(){
		return rc.getLocation().distanceSquaredTo(towers[getNearTower()]) <= 16;
	}
	
	static MapLocation closestEnemyTower(){
		MapLocation me = rc.getLocation();
		int i = 0;
		int d = 1000000000;
		for(int j = 0; j<towerCount;j++){
			if(me.distanceSquaredTo(enemyTowers[j])<d)
				i = j;
		}
		
		return enemyTowers[i];
	}

	
	static int doBasher(){
    updatePhase();
    boolean atTower = isAtTower();
    
    if( phase.equals("defensive"))
        if( !atTower)
            if( pathFound)
                pathfindToTower(fate);
            else
                wanderToTower(fate);
        if( enemyNearby())
            chaseNearestEnemy(10); //up to 10 squares from starting position
    else if (phase.equals("Offensive"))
        if( pathFound )
            pathfindToTower(closestEnemyTower());
        else
            wanderToTower(closestEnemyTower());
        if( atTower)
            attackNearestTower();
        else if (enemyBuildingNearby())
            chaseNearestBuilding();
        else if( enemyNearby() )
            chaseNearestEnemy(10);
    else if (phase.equals("control"))
        if( knowLocationOfAnEnemyBuilding())
            if(pathFound)
                pathfindToNearestBuilding();
            else
                chaseNearestStructure();
        else if( enemyNearby())
            chaseNearestEnemy(-1); //any distance
            callForHelp();
        else wander(lastDir);
            
    if( hasEnoughSupply())
        continue;
    else
        rc.yield(); //no current plans for refilling supply
        
	}
	
	
	
	
	


	public void HQAI(){
	    //defend the hearth
	    if (rc.isWeaponReady()){
	        attackSomething();
	    }
	    
	    //make the tims
	    RobotInfo[] myRobots = rc.senseNearbyRobots(999999, myTeam);
	    int tims = 0;
	    for (RobotInfo r : myRobots) {
	        if (r.type == RobotType.BEAVER) {
	            tims++;
	        }
	    }
	    
	    if (rc.isCoreReady() && ore >= 100 && tims < 10) {
	        trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
	    }
	    
	    //find the path
	    if (!mapFinished){
	        pollDronesForMap();
	    } else if (!pathFound){ //no dijkstra maps yet, find paths
	    
	        //this alg will not end until a path is found, hq will be open to attack
	        // but hopefully the algorithm will finish before the other team can get
	        // a decent army together (rough estimate 200-400 turns for pathfinding)
	        // upside is that once that hurdle is passed, no further pathfinding is
	        // ever required
	        for (int i = 0; i < NumTowers*2; i++){ 
	            //for each tower (friendly and enemy)
	            dijkstraMaps.add(new MapLocation[MAP_WD*MAP_HG]); //initialize to Math.infinity

	            //int unvisited_ct = MAP_WD*MAP_HG;
	            //boolean[] unvisited = new boolean[MAP_WD*MAP_HG]; //initialized to all true
	            ArrayList<Integer> queue;

	            //unvisited_ct--;
	            //int cur_dij = mapLocToInt(SenseTowers()[i]); //current loc in dij alg
	            int cur_x = towers[i].x; //sensetowers might be wrong here
	            int cur_y = towers[i].y;
	            //unvisited[cur_dij] = false; //probably wrong syntax
	            
	            setAt(i, cur_dij, 0, 0); //can use "long" position as x and 0 as y

	            while (queue.size() != 0){ //not sure about this syntax
	                cur_x = intToMapPos(queue.get(0)).x;
	                cur_y = intToMapPos(queue.get(0)).y;
	                cur_num = getAt(i, cur_x, cur_y);
	                
	                for (int x = -1; x < 2; x++){
	                    for (int y = -1; y < 2; y++){
	                        if (getAt(i, cur_x+x, cur_y+y) > cur_num 
	                         && walkable(cur_x+x, cur_y+y)){
	                            setAt(i, cur_x+x, cur_y+y, cur_num+1);
	                            //unvisited_ct--;
	                            int c_pos = cur_x+x+(cur_y+y)*MAP_WD;
	                            //unvisited[c_pos] = false;
	                            queue.add(new Integer(c_pos));
	                        }
	                    }
	                }

	                queue.remove(0); //remove first element (just dealt with)
	            }
	        }
	    }
	}

	public int mapLocToInt(MapLocation loc){
	    return loc.x+loc.y*MAP_WD;
	}

	public Integer getAt(int mapNum, int x, int y){
	    return mapLocToInt(dijkstraMaps.get(mapNum)[x+y*MAP_WD]); //not sure about this syntax
	}

	public void setAt(int mapNum, int x, int y, int n){
	    dijkstraMaps.get(mapNum)[x+y*MAP_WD] = n; //not sure about this syntax
	}

	public void pollDronesForMap(){
	    //todo: unpseudocodeify this
	    foreach (drone in drones){
	        String data = Integer.toString(readBroadcast(drone.channel));
	        //loop through drones and check for new map data they have found
	        MapLocation loc = new MapLocation(data.substring(1, 4), data.substring(4, 7));
	        boolean isVoid = data.substring(0, 1);

	        pos = loc.x+loc.y*MAP_WD;

	        if (map[pos] == 0){
	            if (isVoid){
	                map[pos] = -1; //void
	            } else {
	                map[pos] = 1; //air
	            }
	        }
	    }
	}

	public boolean walkable(int x, int y){
	    return map[x+y*MAP_WD] == 1; //1 is air tile
	}
	
}
