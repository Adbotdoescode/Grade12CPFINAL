## **Outbreak Game**

**Concept**: A zombie survival situation, one player starts out as the zombie and infects nearby robots. These infected zombies become hostile, and seek out non infected zombies. Zombies win if all robots are infected, survivors win if survivors can collect a specific amount of things before they are all transformed. A medic robot with hit points exists to assist the non-infected, if more non-infected exist than infected, then the medic will assist with collecting things, if there are too many infected, the robot will switch to healing zombies.

* **Partner A (Zombie AI):** This AI's primary goal is to tag other robots. It should evaluate all other robots on the field and use a sorting algorithm to prioritize targets based on their distance and randomized movement speed.

* **Partner B (Survivor AI):** This AI’s goal is to evade and gather resources. Its primary purpose is to scan the fenced-off area for things and calculate the most efficient path to collect them to reach the team’s win condition. It needs to rely on its randomized dodging and speed attributes to evaluate the board, constantly adjusting its route to stay out of infection range of zombies. 

* **Partner C (Medic AI):** This AI is a dynamic support character. The primary goal is to evaluate the current state of the game and actively change its strategy based on the ratio of infected to non infected robots. If survivors are winning, it runs a strategy to help collect things. If the outbreak gets too large, it switches to healing, using its hit points to intercept and cure zombies. (when its health is below max the things it picks up are used to refill its health. 

* **Split into thirds (to decide later), outbreakApp core engine:** Sets up 13x24 city, spawning robots, continuously spawning things, building the game loop and managing turns and allowances. Each turn, responsible for passing the current game state to all the robots, and then deciding on the win/loss condition 

**Rules**: 

- Win Case:  
  - The survivors reach the threshold of things   
- Loss Case:   
  - The zombies infect all survivors   
- Survivors and medic can only pick up one thing at a time.  
- Zombies move double the speed of survivors and medics.  
- Medic does not count as a survivor.  
  - E.g case where every survivor is turned except the medic, the game is still lost.  
  -   
- Must be at least one survivor left for the medic to perform its role.  
- Things Spawning Rules:   
  - Max number of things at any point during game \= number of players \- n  
  - When enough things have been removed from the game and a certain threshold is reached, more things will start spawning every other turn at a random spot on the map   
  - Things must be dropped off by survivors at an enclosed Safe Zone that is inaccessible to zombies   
  - If survivors are infected while holding a thing, the thing is removed from the game   
  - Medics with only one health remaining will consume things to replenish their health   
    - Otherwise, they will help players gather things and drop them off in the Safe Zone
