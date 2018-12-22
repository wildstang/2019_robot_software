# `2018_fall_refactor`
Based on a merge of core framework and previous years' code. This repo DOES NOT DEPEND on the `core_framework` repo.


## Getting Started
FYI Many of the files you'll need for these instructions are (also) available in [this big archive file](https://drive.google.com/file/d/1uvs3eQmzUT0qR6m5IIYKfawEX-y0o_qg/view?usp=sharing).

### Setting up required software
-   Have Windows on your computer. If you do not have Windows you cannot run the driver station program, but you may be able to build and deploy code to the robot. *TODO: verify whether this is actually the case.* In any event, using a non-Windows operating system is not a recommended configuration.


-   Follow [these instructions](http://wpilib.screenstepslive.com/s/4485) to get set up with Eclipse and WPILib. Make sure you use the version of Eclipse stipulated in the instructions! As of this writing that is Eclipse Oxygen. Later versions of Eclipse MAY NOT WORK.


-   Install the Eclipse [Checkstyle plugin](http://checkstyle.org/eclipse-cs/#!/). This is not absolutely required, strictly speaking, but your co-contributors will be happier with you if you do.


-   Install the FRC Driver Station software. The installer is available in the [big archive file](https://drive.google.com/file/d/1uvs3eQmzUT0qR6m5IIYKfawEX-y0o_qg/view?usp=sharing) mentioned in the first step. If you can't find a copy of the driver station to install, you can skip it. Without the driver station you can still develop for and load code onto the robot. You just can't drive it.


-   Install the CTRE toolsuite by following the links at the bottom of [this page](http://www.ctr-electronics.com/control-system/hro.html#product_tabs_technical_resources). Instructions and the installer are available on that page. Use the "No Installer" package if you're not on Windows. BTW, we're not using the Hero board that this software is for, but our CANBus motor controllers require the CTRE software.


### Recommended configurations
In addition to making all configuration changes recommended in the instructions above for installing Eclipse, here are some other Eclipse settings you may wish to change:

-   Under General -> Perspectives, check "Open perspectives in new window". This makes it easier to flip back and forth between git and Java perspectives.
-   Under the Project menu, check "Build automatically". This allows the Checkstyle plugin to work correctly.
-   In Eclipse's Problems view, open filter settings (icon w/ three arrows in upper right). Uncheck "Show all problems" and check "Warnings on Selection" and "All Errors on Workspace". This will prevent you from being bothered by warnings related to code you're not working on, but will still show you all the errors.
-   *TODO: fill out this list.*


### Checking out and building the code
-   *TODO: Eclipse/Github setup instructions.* *TODO: SSH key instructions.*


-   Clone the repository in Eclipse:

    -   Copy this URL to your clipboard: `git@github.com:wildstang/2018_fall_refactor.git`.

    -   Open the git perspective in Eclipse (*Window -> Perspective -> Open Perspective -> Other...*)

    -   There should be a panel named "Repositories". At the top of the panel there will be a button with a small green arrow on it. This is the button to clone a git repository. Click this button.

    -   *FIXME this step doesn't work without setting up the SSH key with github!* Eclipse will fill in repository details automatically. Click next. On the next dialog click next again. On the next dialog there should be a checkbox to have Eclipse import the project for you automatically; check that box.


-   Once the project is imported from your local git working tree, return to the Java perspective. Right click on the `2018_fall_refactor` project and select *Run as -> WPILib Java Deploy*. It should successfully build and then fail to deploy the code to the robot. If the project does not build for you, please post details in the software channel of the slack or otherwise get help from a software mentor so we can figure out why it doesn't work for some people.


-   If you want help understanding what's going on in the code, you can select "Generate JavaDoc" from the Project menu at the top of Eclipse's window. Most of that is not useful as of this writing but we'll work on it.