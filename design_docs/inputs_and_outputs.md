# Inputs and outputs

The WS framework has a concept of *inputs* and *outputs*. An *output* is a signal from the RIO to another device; an input is a signal from another device to the RIO. An input could be, for example, a sensor or a network message from another device. An output could be, for example, a motor, or a network message to another device.

## Types

Here described for Output; Inputs are the same.

An Output is the object that's actually used to interact with the output during robot run.
An Outputs is a configuration object hardcoded into the source that specifies what name and type the output is, and contains an OutputConfig.
An OutputConfig is a configuration object hardcoded into the source that contains type-specific information on how this output should be set up; e.g. for a Victor motor output, motor parameters would go in a WSVictorConfig that implements OutputConfig.

We should probably rename something here, but I'm really not sure what. Perhaps the Outputs -> OutputConfig relationship should be refactored from membership to inheritance, so that the type currently known as Outputs can instead be called OutputConfig.

## Configuration
The outputs available on the robot are defined in the org.wildstang.yearXXXX.robot.WSOutputs enum.

The inputs available on the robot are defined in the org.wildstang.yearXXXX.robot.WSInputs enum.

Each Inputs or Outputs in the enum contains an InputConfig or OutputConfig object inside it that will be passed to the Input or Output that will be created during initialization.

## Initialization
During robot startup, Robot.robotInit calls Core.createOutputs in org.wildstang.framework.core.Core.

For each Outputs in WSOutputs, createOutputs creates an Output (not to be confused with Outputs) and uses the OutputManager's addOutput to add the Output (not Outputs) to the OutputManager.

The parallel is true for input.

## Operation
Robot.autonomousPeriodic and Robot.teleopPeriodic periodically (duh) call Core.executeUpdate(). Core.executeUpdate calls update() methods of InputManager and OutputManager, which call the update() methods of individual inputs and outputs.