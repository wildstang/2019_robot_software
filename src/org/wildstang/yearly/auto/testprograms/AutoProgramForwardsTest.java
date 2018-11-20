/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wildstang.yearly.auto.testprograms;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.framework.auto.steps.control.AutoStepDelay;
import org.wildstang.yearly.auto.steps.drivebase.StepDriveManual;

/**
 *
 * @author coder65535
 */
/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
public class AutoProgramForwardsTest extends AutoProgram {

	public void defineSteps() {
		addStep(new StepDriveManual(1.0, 0.0));
		addStep(new AutoStepDelay(500));
		addStep(new StepDriveManual(0.0, 0.0));
	}

	public String toString() {
		return "Test by driving forwards for 10 seconds";
	}
}
