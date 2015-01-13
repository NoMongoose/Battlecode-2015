Drone
    bool hasLoc
    location destination
    
in while loop:
    updatePhase()
    updateAtTower()
    updatePathFound()

    double distanceTraveledThisTurn = 0
    
    if phase == defensive
        if !pathFound
            if !hasLoc
                destination = getLocToMapTo()
                hasLoc = true
            else
                if this.location == destination
                    hasLoc = false
                else
                    //pathfind
                    while (distanceTraveledThisTurn^2 <= 24)
                        distanceTraveledThisTurn += beeLineTo(destination)    
        else //path has been found already
            if !hasLoc
                fate = rand()%towersCt
                destination = senseTowerLocations()[fate]
            else
                //might need to eventually make sure that it  doesnt overlap other drones
                while hasEnoughSupply && location != destination
                    beeLineTo(destination)
    else if phase == offensive
        if enemyNearby()
            chaseNearestEnemy()
        else
            if !hasLoc
                fate = rand()%enemyTowersCt
                destination = senseEnemyTowerLocations()[fate]
            else
                //might need to eventually make sure that it  doesnt overlap other drones
                while hasEnoughSupply && location != destination
                    beeLineTo(destination)
                    if enemyNearby() 
                        break
    else if phase == control
        if enemyBuildingNearby()
            chaseNearestEnemyBuilding()
        else
            if knowLocationOfAnEnemyBuilding()
                destination = locationOfNearestEnemyBuilding
            else if enemyNearby()
                chaseNearestEnemy(-1) //any distance
                callForHelp()
            else
                wander()
            

beeLineTo(destination)
    int distanceTraveledThisTurn = 0

    if destination.x > this.location.x && 
       destination.y == this,location.y
        tryMove(EAST)
        distanceTraveledThisTurn = 1
    else if destination.x > this.location.x &&
           destination.y > this.location.y
        tryMove(NORTHEAST)
        distanceTraveledThisTurn = 1.4
    ...

    return distanceTraveledThisTurn
    
