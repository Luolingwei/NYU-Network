package MyFSM;

import Fsm.FSM;
import Fsm.State;

public class MyFSM extends FSM {

    private int RDATA_NUM;
    private int SDATA_NUM;
    private final String fsmName;

    public MyFSM(String fsmName, State start){
       super(fsmName, start);
       this.fsmName = fsmName;
    }

    public void addRDATA() {
        this.RDATA_NUM ++;
    }

    public void addSDATA() {
        this.SDATA_NUM ++;
    }

    public int getRDATA_NUM() {
        return RDATA_NUM;
    }

    public int getSDATA_NUM() {
        return SDATA_NUM;
    }

    public String getFsmName() {
        return fsmName;
    }


}
