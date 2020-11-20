package Enums;

public enum StateEnum {

    // common states
    CLOSED,
    ESTABLISHED,

    // server's states
    LISTEN,
    SYN_RCVD,
    CLOSE_WAIT,
    LAST_ACK,

    // client's states
    SYN_SENT,
    FIN_WAIT_1,
    FIN_WAIT_2,
    CLOSING,
    TIME_WAIT,
    ;


}
