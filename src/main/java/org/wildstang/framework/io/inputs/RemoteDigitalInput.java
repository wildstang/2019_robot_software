package org.wildstang.framework.io.inputs;

import edu.wpi.first.networktables.NetworkTable;

public class RemoteDigitalInput extends DigitalInput {

    NetworkTable remoteIOTable;

    public RemoteDigitalInput(String p_name, String p_networkTbl) {
        super(p_name);
        remoteIOTable = NetworkTable.getTable(p_networkTbl);

    }

    @Override
    public boolean readRawValue() {
        return remoteIOTable.getBoolean(getName(), false);
    }
}