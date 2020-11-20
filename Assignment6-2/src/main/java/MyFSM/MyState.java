package MyFSM;

import Fsm.State;
import Enums.StateEnum;

public class MyState extends State {

    private StateEnum stateName;

    public MyState(StateEnum name) {
        super(name.name());
        this.stateName = name;
    }

    public StateEnum getStateName() {
        return stateName;
    }

    public String toString() {
        return new String("State(" + this.stateName + ")");
    }

}
