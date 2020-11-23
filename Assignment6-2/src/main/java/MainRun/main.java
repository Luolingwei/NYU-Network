package MainRun;

import Enums.EventEnum;
import Fsm.FSM;
import Fsm.FsmException;
import MyFSM.MyEvent;
import MyFSM.MyFSM;
import MyFSM.MyState;
import Utils.Initializer;
import Utils.Loader;
import Enums.StateEnum;
import Enums.InstrEnum;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class main {

//    private static final String tempPath = "/Users/luolingwei/Desktop/Program/Classes/NetWork/NYU-Network/Assignment6-2/src/main/java/TestCases";

    public static void main(String[] args) throws IOException, FsmException {

        // Get Input Parameters
        System.out.println("==========================================================================");
        System.out.println("Mini TCP FSM, Author: Lingwei Luo (lingweiluo@nyu.edu)");
        System.out.println("Please enter your txt file path parameter in the command line");
        System.out.println("==========================================================================");
        Scanner sc = new Scanner(System.in);
        System.out.println("Please Enter Absolute Path of Test Cases (txt file required): ");
        String path = sc.nextLine();
        sc.close();

        Loader loader = new Loader();
        List<File> files = loader.readFiles(path);
        List<String> fileNames = loader.getFileNames();

        for (int i=0; i<files.size(); i++) {
            System.out.printf("Processing file: %s\n", fileNames.get(i));
            List<String> instrs = loader.parseFile(files.get(i));
            executeInstrs(instrs);
            System.out.println("==========================================================================");
        }

    }


    public static void executeInstrs (List<String> instrs) throws FsmException {

        FSM myFSM = new MyFSM("myFSM", new MyState(StateEnum.CLOSED));

        Initializer initializer = new Initializer();
        initializer.init(myFSM);

        for (String instr: instrs) {

            if (instr.equals(InstrEnum.ACTIVE.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.ActiveOpen));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.PASSIVE.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.PassiveOpen));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.SYN.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.SYNReceived));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.SYNACK.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.SYNACKReceived));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.ACK.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.ACKReceived));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.RDATA.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.DataReceived));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.SDATA.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.DataToSend));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.FIN.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.FINReceived));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.CLOSE.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.Close));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.TIMEOUT.name())) {
                try {
                    myFSM.doEvent(new MyEvent(EventEnum.TimedWaitEnd));
                } catch (FsmException e) {
                    System.out.printf("Invalid instruction [%s] cause exception [%s]\n", instr, e.toString());
                }
            } else if (instr.equals(InstrEnum.SEND.name())){
                System.out.printf("[%s]: Event [%s] will be ignored!\n", "myFSM", instr);
            } else {
                System.out.printf("[%s]: Error: unexpected Event [%s]\n", "myFSM", instr);
            }

        }
    }
}
