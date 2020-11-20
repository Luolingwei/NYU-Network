package MyFSM;

import Enums.EventEnum;
import Fsm.Action;
import Fsm.Event;
import Fsm.FSM;

public class MyAction extends Action {


    @Override
    public void execute(FSM fsm, Event event) {
        MyFSM myFSM = (MyFSM) fsm;
        MyEvent myEvent = (MyEvent) event;
        if (myEvent.getEventName().equals(EventEnum.DataReceived)){
            myFSM.addRDATA();
            System.out.printf("[%s]: DATA received %d\n", myFSM.getFsmName(), myFSM.getRDATA_NUM());
        } else if (myEvent.getEventName().equals(EventEnum.DataToSend)){
            myFSM.addSDATA();
            System.out.printf("[%s]: DATA sent %d\n", myFSM.getFsmName(), myFSM.getSDATA_NUM());
        } else {
            System.out.printf("[%s]: Event [%s] received, current State is [%s]\n", myFSM.getFsmName(), myEvent.getEventName().name(), myFSM.currentState().getName());
        }
    }

}
