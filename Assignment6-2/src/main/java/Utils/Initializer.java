package Utils;

import Enums.EventEnum;
import Enums.StateEnum;
import Fsm.FSM;
import Fsm.FsmException;
import Fsm.Transition;
import MyFSM.MyAction;
import MyFSM.MyEvent;
import MyFSM.MyState;

public class Initializer {


    public void init(FSM fsm) throws FsmException {

            // client
            fsm.addTransition(new Transition(new MyState(StateEnum.CLOSED), new MyEvent(EventEnum.ActiveOpen), new MyState(StateEnum.SYN_SENT), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.SYN_SENT), new MyEvent(EventEnum.Close), new MyState(StateEnum.ESTABLISHED), new MyAction()));

            fsm.addTransition(new Transition(new MyState(StateEnum.SYN_SENT), new MyEvent(EventEnum.SYNACKReceived), new MyState(StateEnum.ESTABLISHED), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.SYN_SENT), new MyEvent(EventEnum.SYNReceived), new MyState(StateEnum.SYN_RCVD), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.SYN_RCVD), new MyEvent(EventEnum.ACKReceived), new MyState(StateEnum.ESTABLISHED), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.SYN_RCVD), new MyEvent(EventEnum.Close), new MyState(StateEnum.FIN_WAIT_1), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.ESTABLISHED), new MyEvent(EventEnum.DataToSend), new MyState(StateEnum.ESTABLISHED), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.ESTABLISHED), new MyEvent(EventEnum.DataReceived), new MyState(StateEnum.ESTABLISHED), new MyAction()));

            fsm.addTransition(new Transition(new MyState(StateEnum.ESTABLISHED), new MyEvent(EventEnum.Close), new MyState(StateEnum.FIN_WAIT_1), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.FIN_WAIT_1), new MyEvent(EventEnum.ACKReceived), new MyState(StateEnum.FIN_WAIT_2), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.FIN_WAIT_2), new MyEvent(EventEnum.FINReceived), new MyState(StateEnum.TIME_WAIT), new MyAction()));

            fsm.addTransition(new Transition(new MyState(StateEnum.FIN_WAIT_1), new MyEvent(EventEnum.FINReceived), new MyState(StateEnum.CLOSING), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.CLOSING), new MyEvent(EventEnum.ACKReceived), new MyState(StateEnum.TIME_WAIT), new MyAction()));

            fsm.addTransition(new Transition(new MyState(StateEnum.TIME_WAIT), new MyEvent(EventEnum.TimedWaitEnd), new MyState(StateEnum.CLOSED), new MyAction()));

            // server
            fsm.addTransition(new Transition(new MyState(StateEnum.CLOSED), new MyEvent(EventEnum.PassiveOpen), new MyState(StateEnum.LISTEN), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.LISTEN), new MyEvent(EventEnum.Close), new MyState(StateEnum.CLOSED), new MyAction()));

            fsm.addTransition(new Transition(new MyState(StateEnum.LISTEN), new MyEvent(EventEnum.SYNReceived), new MyState(StateEnum.SYN_RCVD), new MyAction()));

            fsm.addTransition(new Transition(new MyState(StateEnum.ESTABLISHED), new MyEvent(EventEnum.FINReceived), new MyState(StateEnum.CLOSE_WAIT), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.CLOSE_WAIT), new MyEvent(EventEnum.Close), new MyState(StateEnum.LAST_ACK), new MyAction()));
            fsm.addTransition(new Transition(new MyState(StateEnum.LAST_ACK), new MyEvent(EventEnum.ACKReceived), new MyState(StateEnum.CLOSED), new MyAction()));

        }

}
