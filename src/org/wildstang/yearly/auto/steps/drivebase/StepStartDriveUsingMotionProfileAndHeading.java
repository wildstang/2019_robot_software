/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wildstang.yearly.auto.steps.drivebase;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.yearly.robot.WSSubsystems;
import org.wildstang.yearly.subsystems.DriveBase;

/**
 *
 * @author Nathan
 */
public class StepStartDriveUsingMotionProfileAndHeading extends AutoStep
{

   double distance;
   double goal_velocity;
   double heading;

   public StepStartDriveUsingMotionProfileAndHeading(double distance,
         double goal_velocity, double heading)
   {
      this.distance = distance;
      this.goal_velocity = goal_velocity;
      this.heading = heading;
   }

   @Override
   public void initialize()
   {
//      ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).startMoveWithHeadingAndMotionProfile(distance, goal_velocity, heading);
      setFinished(true);
   }

   @Override
   public void update()
   {
   }

   @Override
   public String toString()
   {
      return "Start the drive using motion profile for " + distance
            + " inches and reach going " + goal_velocity
            + " inches/second at a heading of " + heading;
   }
}
