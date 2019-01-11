# 2019_robot_software
TODO: write new VSCode setup instructions

## Getting Started
### Setting up required software
### Checking out and building the code
## Robots
This combined codebase has logic for multiple robots in it. 

Each robot has a package under `org.wildstang` e.g. `org.wildstang.year2016`, `org.wildstang.year2017`. Each robot also has a build file at project root e.g. `build_year2016.xml`, `build_year2017.xml`.To deploy to or build for a different robot from the default, right-click on that build XML file and select *Run as -> Ant Build...*. You should see a window options to clean, build, deploy, simulate, etc.

### year2016
2016 robot.

### year2017
2017 robot. WildStang used C++ in 2018, so as of late 2018 this is the most recent Java robot.

This robot uses CANbus motor controllers. The code was ported in late 2018 to a more recent CTRE and it is very alpha at this writing, so no bets on whether it works.

### devbase1
A practice drivebase.
