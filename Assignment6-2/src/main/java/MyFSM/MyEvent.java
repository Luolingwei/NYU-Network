package MyFSM;

import Enums.EventEnum;
import Fsm.Event;

public class MyEvent extends Event {

    private EventEnum eventName;

    public MyEvent(EventEnum name) {
        super(name.name());
        this.eventName = name;
    }

    public EventEnum getEventName() {
        return eventName;
    }

}
