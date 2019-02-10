# Drivebase

## Interaction between auto and drivebase

Next person to work on drive autonomous code, grab a mentor and sit with
them to design the relationship between the drive and the autos. You should
work out with them the answers to these questions. Once you have made these
decisions, write them down here so that you and others can adhere to them.

You need to decide:

- What is the right way for the auto to
communicate a motion target to the drivebase?
Auto motions should probably happen under PID control, maybe Motion Magic.
Motion Magic is a mode in the Talon motor controllers where you can tell it
to go to a target value and it will plan a smooth acceleration, cruise and
deceleration motion profile for you. 
- How does an auto step find out when the drive is *done* going where it
needs to go?
- What is the right way to coordinate the left and right drive Talons? Would
it be appropriate to run a manual secondary pid overlaid on the hardware PID
by using the arbitrary feed-forward term in the [set() method](http://www.ctr-electronics.com/downloads/api/java/html/interfacecom_1_1ctre_1_1phoenix_1_1motorcontrol_1_1_i_motor_controller.html#ad34ab6c4fc37886a0e62a1dcb44e4645)?
We really ought to be controlling not only the speed on each side but the
difference between speeds.

Some additional notes:

- The drive should expose an interface expressed in inches or mm of travel
and degrees or radians of left-right rotation. If the interface exposes the
raw encoder values on the motor controller, or uses wheel rotations, then
changes to the wheels or to the encoder setup on the robot will break all the
auto code.
- Be thinking about the PathWeaver while designing the answers to these
questions. Some simple building-block drive auto steps may become special
cases of PathWeaver. Of course, it's unclear if we'll get PathWeaver working
in time for competition...
- The auto steps should probably not have very much code in them. Most of the
functionality is *drive* functionality and belongs in the drive subsystem. 
For example, consider an auto step that drives forward for a certain distance.
Driving forward is a property of the drive. Driving for a certain distance is
much easier to implement with Drive, because it requires motion planning and
will be intimately connected with the Talons. So all the auto step should be
doing is informing the drive that it needs to drive forward a certain distance
and then waiting for this to occur.

This is more complicated than I first realized it would be when I set this as
a task. - Zack