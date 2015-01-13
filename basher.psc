Basher
    updatePhase()
    updateAtTower()
    updatePathFound()
    
    if phase == defensive
        if !atTower
            if pathFound
                pathfindToTower(fate)
            else
                wanderToTower(fate)
        if enemyNearby()
            chaseNearestEnemy(10) //up to 10 squares from starting position
    else if phase == offensive
        if pathFound
            pathfindToTower(closestEnemyTower())
        else
            wanderToTower(closesEnemyTower())
        if atTower
            attackNearestTower()
        else if enemyBuildingNearby()
            chaseNearestStructure()
        else if enemyNearby()
            chaseNearestEnemy(10)
    else if phase == control
        if knowLocationOfAnEnemyBuilding()
            if pathFound
                pathfindToNearestBuilding()
            else
                chaseNearestStructure()
        else if enemyNearby()
            chaseNearestEnemy(-1) //any distance
            callForHelp()
        else
            wander()
            
    if hasEnoughSupply()
        continue
    else
        yield //no current plans for refilling supply

pathfindTo(locaiton)
    int direction;
    curDijkstraNum = getDijkstraNumAt(this.xPos, this.yPos)
    //potentially more efficient to use if/else than loop here
    for (y = -1; y < 2; y++)
        for (x = -1; x < 2; x++)
            if x == 0 || y == 0 //dont go diagonally
                if getDijkstraNumAt(this.xPos+x, this.yPos+y) < curDijkstraNum
                    direction = coordsToDirection(x, y);
    tryMove(direction) //fine to go in a close direction
    
pathfindToTower(int fate)
    location = senseTowers()[fate]
    pathfindTo(location)

wanderToTower(int fate)
    wanderTo(senseTowerLocations[fate])

wanderTo(location)
    if floor(rand()*100) < 33
        wander()
    else
        x = this.xPos-location.xpos
        x /= abs(x)

        y = this.yPos-location.ypos
        y /= abs(y)

        direction = coordsToDirection(x, y)
        tryMove(direction) //fine to go in a close direction

chaseNearestEnemy(int distance)
    if this.startLoc != (-1,-1)
        this.startLoc = this.location
    location = senseEnemyLocations()[0]
    if distanceBetween(this.location, location) > this.range
        if pathFound
            pathfindTo(location)
        else
            wanderTo(location)
        chaseNearestEnemy() //recursion!
    else if (type != basher) //bashers attack automatically
        attackSomething('unit')
    if (distance != -1)
        if distanceBetween(this.startLoc, this.location) > distance^2
        yield()
            

chaseNearestBuilding()
    //essentially same as chaseNearestEnemy

enemyNearby()
    locations = senseEnemyLocations;
    for (i = 0; i < locations.length; i++)
        dist = distanceBetween(this.location, locations[i])
        if dist < SOME_CONST
            return locations[i]
    return NULL

enemyBuildingNearby()
    //essentially same as enemyNearby()

callForHelp()
    broadcastMessage(this.location) //todo: find a way to encode this well
    //hq will store broadcasts and send orders to units based on them

enoughSupply()
    //caleb's job

coordsToDirection(x, y)
    int direction;
    if (x > 0 && y > 0)
        direction = NORTHEAST
    else if (x > 0 && y == 0)
        direction = EAST
    ...
    
    return direction

//change attackSomething to take a string of what to attack
//  unit: only units
//  building: only buildings
//  tower: only towers
//  hq: only headquarters
//  structure: towers and hqs
//  robots: units and buildings
//  siege: units buildings and towers
//  finish: units buildings and headquarters
//  all: anything
