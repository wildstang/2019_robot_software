# `2019_robot_software`
Java code to run the 2019 robot. Based on `2018_fall_refactor` repo which is based on a merge of core framework and previous years' code. This repo DOES NOT DEPEND on the `core_framework` repo.


GETTING STARTED:


-   Have Windows on your computer. If you do not have Windows you cannot run the driver station program, but you may be able to build and deploy code to the robot. *TODO: verify whether this is actually the case.* In any event, using a non-Windows operating system is not a recommended configuration.


-   Follow [these instructions](http://wpilib.screenstepslive.com/s/4485) to get set up with Eclipse and WPILib. Make sure you use the version of Eclipse stipulated in the instructions! As of this writing that is Eclipse Oxygen. Later versions of Eclipse MAY NOT WORK.


-   *TODO: driver station setup instructions.*


-   Install the CTRE toolsuite by following the links at the bottom of [this page](http://www.ctr-electronics.com/control-system/hro.html#product_tabs_technical_resources). Instructions and the installer are available on that page. Use the "No Installer" package if you're not on Windows.


-   TODO Eclipse/Github setup instructions. *TODO: SSH key instructions.*


-   Clone the repository in Eclipse:

    -   Copy this URL to your clipboard: `git@github.com:wildstang/2019_robot_software.git`

    -   Open the git perspective in Eclipse.

    -   There should be a panel named "Repositories". At the top of the panel there will be a button with a small green arrow on it. This is the button to clone a git repository. Click this button.

    -   *FIXME this step doesn't work without setting up the SSH key with github!* Eclipse will fill in repository details automatically. Click next. Click next again. On the next dialog there should be a checkbox to have Eclipse import the project for you automatically; check that box.


-   Once the project is imported from your local git working tree, return to the Java perspective. Right click on the `2019_robot_software` project and select *Run as > WPILib Java Deploy*. It should successfully build and then fail to deploy the code to the robot. If the project does not build for you, please post details in the software channel of the slack or otherwise get help from a software mentor so we can figure out why it doesn't work for some people.