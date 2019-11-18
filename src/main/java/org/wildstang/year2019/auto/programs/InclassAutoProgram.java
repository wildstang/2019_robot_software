package org.wildstang.year2019.auto.programs;

import org.wildstang.framework.auto.AutoProgram;

/**
 * Class:       InclassAutoProgram.java
 * Description: This is the template class for an in-class project on Nov 17 2019, a follow up to InclassDrive.java.
 * 
 *              You, the programmer, are tasked with designing a series of autonomous steps and programs for driving the
 *              robot around the field, without driver input. To implement this you will need to design various autonomous
 *              steps for the programs, as well as, extend the InclassDrive.java subsystem you created last week. You can
 *              use any of the drive systems you were tasked with creating. If you weren't able to complete any of the
 *              tasks from last week, focus on completing those first.
 * 
 *              There are 3 programs you are tasked with creating:
 *              1:  Drive in a straight line for 15 feet.
 *              2:  Extend the program to drive slow in the first 5 feet, medium in the second, and third in the final.
 *              2:  Drive in a straight line for 5 feet, turn 90 degrees right, drive 5 feet, turn 90 degrees left, then
 *                  drive 10 feet.
 *              3.  Drive in a circle with a radius of 5 feet.
 *              4:  Extend the program to start driving at 10% speed and evenly increase speed such that it reaches full
 *                  speed when it completes the circle.
 * 
 *              Notes:  You can use as many or as few steps as you find necessary to complete these programs.
 *                      An additional template for an auto step is provided as InclassAutoStep.java.
 *                      Again you aren't expected to complete the whole project in one class, nor does it need to be completed
 *                      and turned in. If you do not complete the project today, feel free to work on it in your free time,
 *                      we may or may not come back to it in a future class.
 */
public class InclassAutoProgram extends AutoProgram {

    @Override
    protected void defineSteps() {
    }

    @Override
    public String toString() {
        return "In-Class Drive Auto Program";
    }
}