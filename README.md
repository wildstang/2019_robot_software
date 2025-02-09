# 2019_robot_software

Further documentation is in the [`design_docs`](design_docs/) folder.

## Getting Started
### Setting up required software
*  These software setup instructions are currently Windows-specific. It is possible to do everything but run the driver's station from Linux or OS X; ask @zachary.mccord or #software if you're going down that road.
*  Follow instructions at https://wpilib.screenstepslive.com/s/currentCS/m/java/l/1027503-installing-c-and-java-development-tools-for-frc to install VSCode and WPIlib.
*  Follow instructions at https://wpilib.screenstepslive.com/s/currentCS/m/java/l/1027504-installing-the-frc-update-suite-all-languages to install driver's station.
*  Download and run the latest "CTRE Phoenix Framework Installer" CTRE installer at http://www.ctr-electronics.com/hro.html#product_tabs_technical_resources.
*  Download and run navX-XMP installer at https://www.kauailabs.com/public_files/navx-mxp/navx-mxp.zip. 
### Downloading the code
*  Install [Git for Windows](https://gitforwindows.org). Non-Windows users: you probably already have it, and if not it's in your package manager.
*  Quit VSCode if you have it open, otherwise the next steps will not work.
*  Open WPI VSCode (hexagonal icon with a W in it).
*  Open the command palette with F1, cntl-shift-P or cmd-shift-P. Search for "Git: Clone" and select it.
*  When prompted for a "repository URL", paste in this link: https://github.com/wildstang/2019_robot_software.git
*  You will be prompted for the location where you would like to save the project on your computer, which is up to you.
*  You may be prompted for your GitHub credentials, which you'll need to enter. Don't have a GitHub account? Sign up, and then ask on #software to be given access to GitHub.
 * TODO: Describe setting user name and emails
### Building and deploying to the robot
To build the code, open the command palette with F1, cntl-shift-P or cmd-shift-P. Search for "robot" and select "WPILib: Build robot code".

To build and deploy, open the command palette with F1, cntl-shift-P or cmd-shift-P. Search for "robot" and select "WPILib: Deploy robot code". Or just press shift-F5.

To debug the code on the robot, open the command palette with F1, cntl-shift-P or cmd-shift-P. Search for "robot" and select "WPILib: Debug robot code".

### Next Steps
You'll want to have a look below and at the detailed docs in the
[`design_docs`](design_docs/) folder for more information on where to go next.

## Robots
This combined codebase has logic for multiple robots in it. 

Each robot has a package under `org.wildstang` e.g. `org.wildstang.year2016`, `org.wildstang.year2017`. Each robot also has a build file at project root e.g. `build_year2016.xml`, `build_year2017.xml`.To deploy to or build for a different robot from the default, right-click on that build XML file and select *Run as -> Ant Build...*. You should see a window options to clean, build, deploy, simulate, etc.

### year2016
2016 robot.

### year2017
2017 robot. WildStang used C++ in 2018, so as of late 2018 this is the most recent Java robot.

This robot uses CANbus motor controllers. The code was ported in late 2018 to a more recent CTRE and it is very alpha at this writing, so no bets on whether it works.

### year2019
See [year2019 documentation](design_docs/year2019/README.md).

### devbase1
A practice drivebase.
