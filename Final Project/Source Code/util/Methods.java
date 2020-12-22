package edu.nyu.cs.sdn.apps.util;

import edu.nyu.cs.sdn.apps.sps.ShortestPathSwitching;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFOXMField;
import org.openflow.protocol.OFOXMFieldType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionSetField;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoTable;


public class Methods {

    private static List<OFInstruction> rewriteFunc(OFOXMFieldType macType, byte[] macAddr, OFOXMFieldType ipType, int ip) {
        return Arrays.asList(
                new OFInstructionApplyActions().setActions(Arrays.asList(
                        (OFAction) new OFActionSetField().setField(new OFOXMField(macType, macAddr)),
                        (OFAction) new OFActionSetField().setField(new OFOXMField(ipType, ip))
                )),
                new OFInstructionGotoTable(ShortestPathSwitching.table)
        );
    }

    public static List<OFInstruction> rewrite(byte[] macAddr, int ip, boolean isDest) {
        if (isDest)
            return rewriteFunc(OFOXMFieldType.ETH_DST, macAddr, OFOXMFieldType.IPV4_DST, ip);
        else
            return rewriteFunc(OFOXMFieldType.ETH_SRC, macAddr, OFOXMFieldType.IPV4_SRC, ip);
    }

    public static List<OFInstruction> redirectToPort(int portNumber) {
        return Arrays.asList(
                (OFInstruction) new OFInstructionApplyActions().setActions(Arrays.asList((OFAction) new OFActionOutput().setPort(portNumber)))
        );
    }

    public static List<OFInstruction> getInstr() {
        return Arrays.asList((OFInstruction) new OFInstructionGotoTable(ShortestPathSwitching.table));
    }
}
