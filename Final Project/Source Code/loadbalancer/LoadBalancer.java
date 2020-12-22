package edu.nyu.cs.sdn.apps.loadbalancer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFType;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.instruction.OFInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.nyu.cs.sdn.apps.sps.InterfaceShortestPathSwitching;
import edu.nyu.cs.sdn.apps.util.ArpServer;
import edu.nyu.cs.sdn.apps.util.Methods;
import edu.nyu.cs.sdn.apps.util.SwitchCommands;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch.PortChangeType;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.ImmutablePort;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.internal.DeviceManagerImpl;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.util.MACAddress;

public class LoadBalancer implements IFloodlightModule, IOFSwitchListener,
		IOFMessageListener
{
	public static final String MODULE_NAME = LoadBalancer.class.getSimpleName();
	
	private static final byte TCP_FLAG_SYN = 0x02;
	
	private static final short IDLE_TIMEOUT = 20;
	
	// Interface to the logging system
    private static Logger log = LoggerFactory.getLogger(MODULE_NAME);
    
    // Interface to Floodlight core for interacting with connected switches
    private IFloodlightProviderService floodlightProv;
    
    // Interface to device manager service
    private IDeviceService deviceProv;
    
    // Interface to InterfaceShortestPathSwitching application
    private InterfaceShortestPathSwitching l3RoutingApp;
    
    // Switch table in which rules should be installed
    private byte table;
    
    // Set of virtual IPs and the load balancer instances they correspond with
    private Map<Integer,LoadBalancerInstance> instances;

    /**
     * Loads dependencies and initializes data structures.
     */
	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException 
	{
		log.info(String.format("Initializing %s...", MODULE_NAME));
		
		// Obtain table number from config
		Map<String,String> config = context.getConfigParams(this);
        this.table = Byte.parseByte(config.get("table"));
        
        // Create instances from config
        this.instances = new HashMap<Integer,LoadBalancerInstance>();
        String[] instanceConfigs = config.get("instances").split(";");
        for (String instanceConfig : instanceConfigs)
        {
        	String[] configItems = instanceConfig.split(" ");
        	if (configItems.length != 3)
        	{ 
        		log.error("Ignoring bad instance config: " + instanceConfig);
        		continue;
        	}
        	LoadBalancerInstance instance = new LoadBalancerInstance(
        			configItems[0], configItems[1], configItems[2].split(","));
            this.instances.put(instance.getVirtualIP(), instance);

            log.info("Added load balancer instance: " + instance);
			log.info("load balancer's virtual IP is: " + instance.getVirtualIP());
        }
        
		this.floodlightProv = context.getServiceImpl(
				IFloodlightProviderService.class);
        this.deviceProv = context.getServiceImpl(IDeviceService.class);
        this.l3RoutingApp = context.getServiceImpl(InterfaceShortestPathSwitching.class);

        /*********************************************************************/
        /* TODO: Initialize other class variables, if necessary              */
        
        /*********************************************************************/
	}

	/**
     * Subscribes to events and performs other startup tasks.
     */
	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException 
	{
		log.info(String.format("Starting %s...", MODULE_NAME));
		this.floodlightProv.addOFSwitchListener(this);
		this.floodlightProv.addOFMessageListener(OFType.PACKET_IN, this);
	
		/*********************************************************************/
		/* TODO: Perform other tasks, if necessary                           */
		
		/*********************************************************************/
	}
	
	/**
     * Event handler called when a switch joins the network.
     * @param DPID for the switch
     */
	@Override
	public void switchAdded(long switchId) 
	{
		IOFSwitch curSwitch = this.floodlightProv.getSwitch(switchId);
		log.info(String.format("Switch s%d added", switchId));
		
		/*********************************************************************/
		/* TODO: Install rules to send:                                      */
		/*       (1) packets from new connections to each virtual load       */
		/*       balancer IP to the controller                               */
		/*       (2) ARP packets to the controller, and                      */
		/*       (3) all other packets to the next rule table in the switch  */
		
		/*********************************************************************/

		for (Integer virtualIpAddr : instances.keySet()) {

			List<OFInstruction> instrs = Methods.redirectToPort(OFPort.OFPP_CONTROLLER.getValue());

			OFMatch desiredArp = new OFMatch(), desiredTcp = new OFMatch();
			desiredArp.setDataLayerType(OFMatch.ETH_TYPE_ARP);
			desiredArp.setNetworkDestination(virtualIpAddr);
			desiredTcp.setDataLayerType(OFMatch.ETH_TYPE_IPV4);
			desiredTcp.setNetworkDestination(virtualIpAddr);
			desiredTcp.setNetworkProtocol(OFMatch.IP_PROTO_TCP);

			SwitchCommands.removeRules(curSwitch, table, desiredArp);
			SwitchCommands.removeRules(curSwitch, table, desiredTcp);
			SwitchCommands.installRule(curSwitch, table, (byte) 2, desiredArp, instrs);
			SwitchCommands.installRule(curSwitch, table, (byte) 2, desiredTcp, instrs);
		}

		SwitchCommands.installRule(curSwitch, table, SwitchCommands.DEFAULT_PRIORITY, new OFMatch(), Methods.getInstr());

	}
	
	/**
	 * Handle incoming packets sent from switches.
	 * @param sw switch on which the packet was received
	 * @param msg message from the switch
	 * @param cntx the Floodlight context in which the message should be handled
	 * @return indication whether another module should also process the packet
	 */
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) 
	{
		// We're only interested in packet-in messages
		if (msg.getType() != OFType.PACKET_IN)
		{ return Command.CONTINUE; }
		OFPacketIn pktIn = (OFPacketIn)msg;
		
		// Handle the packet
		Ethernet ethPkt = new Ethernet();
		ethPkt.deserialize(pktIn.getPacketData(), 0,
				pktIn.getPacketData().length);
		
		/*********************************************************************/
		/* TODO: Send an ARP reply for ARP requests for virtual IPs; for TCP */
		/*       SYNs sent to a virtual IP, select a host and install        */
		/*       connection-specific rules to rewrite IP and MAC addresses;  */
		/*       for all other TCP packets sent to a virtual IP, send a TCP  */
		/*       reset; ignore all other packets                             */
		
		/*********************************************************************/
		if (OFMatch.ETH_TYPE_ARP == ethPkt.getEtherType()) {
			processArp(sw, ethPkt, (short) pktIn.getInPort());
		} else if (OFMatch.ETH_TYPE_IPV4 == ethPkt.getEtherType()) {
			processIpv4(sw, ethPkt);
		}

		return Command.CONTINUE;
	}

	private void processArp(IOFSwitch sw, Ethernet inEthPkt, short inPort){
		log.info("Enter processArp method!!!");
		ARP inArpPkt = (ARP) inEthPkt.getPayload();
		if (inArpPkt.getOpCode() != ARP.OP_REQUEST) return;
		int ip = IPv4.toIPv4Address(inArpPkt.getTargetProtocolAddress());
		if (!instances.containsKey(ip)) return; 
		byte[] virtualMacAddr = instances.get(ip).getVirtualMAC();

		ARP arpOutPkt = new ARP();
		arpOutPkt.setHardwareType(ARP.HW_TYPE_ETHERNET);
		arpOutPkt.setProtocolType(ARP.PROTO_TYPE_IP);
		arpOutPkt.setHardwareAddressLength((byte) Ethernet.DATALAYER_ADDRESS_LENGTH);
		arpOutPkt.setProtocolAddressLength((byte) MACAddress.MAC_ADDRESS_LENGTH);
		arpOutPkt.setOpCode(ARP.OP_REPLY);
		arpOutPkt.setTargetHardwareAddress(inArpPkt.getSenderHardwareAddress());
		arpOutPkt.setTargetProtocolAddress(inArpPkt.getTargetProtocolAddress());
		arpOutPkt.setSenderHardwareAddress(virtualMacAddr);
		arpOutPkt.setSenderProtocolAddress(ip);

		Ethernet outEthPkt = (Ethernet) new Ethernet();
		outEthPkt.setEtherType(Ethernet.TYPE_ARP);
		outEthPkt.setSourceMACAddress(virtualMacAddr);
		outEthPkt.setDestinationMACAddress(inEthPkt.getSourceMACAddress());
		outEthPkt.setPayload(arpOutPkt);

		SwitchCommands.sendPacket(sw, inPort, outEthPkt);
		System.out.println("ARP reply from switch: " + sw.getId() + " on port: " + inPort);
	}

	private void processIpv4(IOFSwitch sw, Ethernet inEthPkt) {
		log.info("Enter processIpv4 method!!!");
		IPv4 inIpPkt = (IPv4) inEthPkt.getPayload();
		if (inIpPkt.getProtocol() != IPv4.PROTOCOL_TCP) return;
		TCP inTcpPkt = (TCP) inIpPkt.getPayload();
		if (inTcpPkt.getFlags() != TCP_FLAG_SYN) return;
		if (!instances.containsKey(inIpPkt.getDestinationAddress())) return;
		LoadBalancerInstance instance = instances.get(inIpPkt.getDestinationAddress());

		int clientIpAddr = inIpPkt.getSourceAddress();
		int hostIpAddr = instance.getNextHostIP();
		int virtualIpAddr = inIpPkt.getDestinationAddress();

		short clientPort = inTcpPkt.getSourcePort();
		short hostPort = inTcpPkt.getDestinationPort();

		byte protocol = inIpPkt.getProtocol();
		byte[] virtualMacAddr = instance.getVirtualMAC();
		byte[] hostMacAddr = getHostMACAddress(hostIpAddr);

		OFMatch desiredToHost = new OFMatch();
		desiredToHost.setDataLayerType(OFMatch.ETH_TYPE_IPV4);
		desiredToHost.setNetworkProtocol(protocol);
		desiredToHost.setNetworkSource(clientIpAddr);
		desiredToHost.setNetworkDestination(virtualIpAddr);
		desiredToHost.setTransportSource(clientPort);
		desiredToHost.setTransportDestination(hostPort);

		OFMatch desiredFromHost = new OFMatch();
		desiredFromHost.setDataLayerType(OFMatch.ETH_TYPE_IPV4);
		desiredFromHost.setNetworkProtocol(protocol);
		desiredFromHost.setNetworkSource(hostIpAddr);
		desiredFromHost.setNetworkDestination(clientIpAddr);
		desiredFromHost.setTransportSource(hostPort);
		desiredFromHost.setTransportDestination(clientPort);

		SwitchCommands.installRule(sw, table, SwitchCommands.MAX_PRIORITY, desiredToHost, Methods.rewrite(hostMacAddr, hostIpAddr, true), (short) 0, (short) 20);
		SwitchCommands.installRule(sw, table, SwitchCommands.MAX_PRIORITY, desiredFromHost, Methods.rewrite(virtualMacAddr, virtualIpAddr, false), (short) 0, (short) 20);
	}
	
	/**
	 * Returns the MAC address for a host, given the host's IP address.
	 * @param hostIPAddress the host's IP address
	 * @return the hosts's MAC address, null if unknown
	 */
	private byte[] getHostMACAddress(int hostIPAddress)
	{
		Iterator<? extends IDevice> iterator = this.deviceProv.queryDevices(
				null, null, hostIPAddress, null, null);
		if (!iterator.hasNext())
		{ return null; }
		IDevice device = iterator.next();
		return MACAddress.valueOf(device.getMACAddress()).toBytes();
	}

	/**
	 * Event handler called when a switch leaves the network.
	 * @param DPID for the switch
	 */
	@Override
	public void switchRemoved(long switchId) 
	{ /* Nothing we need to do, since the switch is no longer active */ }

	/**
	 * Event handler called when the controller becomes the master for a switch.
	 * @param DPID for the switch
	 */
	@Override
	public void switchActivated(long switchId)
	{ /* Nothing we need to do, since we're not switching controller roles */ }

	/**
	 * Event handler called when a port on a switch goes up or down, or is
	 * added or removed.
	 * @param DPID for the switch
	 * @param port the port on the switch whose status changed
	 * @param type the type of status change (up, down, add, remove)
	 */
	@Override
	public void switchPortChanged(long switchId, ImmutablePort port,
			PortChangeType type) 
	{ /* Nothing we need to do, since load balancer rules are port-agnostic */}

	/**
	 * Event handler called when some attribute of a switch changes.
	 * @param DPID for the switch
	 */
	@Override
	public void switchChanged(long switchId) 
	{ /* Nothing we need to do */ }
	
    /**
     * Tell the module system which services we provide.
     */
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() 
	{ return null; }

	/**
     * Tell the module system which services we implement.
     */
	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> 
			getServiceImpls() 
	{ return null; }

	/**
     * Tell the module system which modules we depend on.
     */
	@Override
	public Collection<Class<? extends IFloodlightService>> 
			getModuleDependencies() 
	{
		Collection<Class<? extends IFloodlightService >> floodlightService =
	            new ArrayList<Class<? extends IFloodlightService>>();
        floodlightService.add(IFloodlightProviderService.class);
        floodlightService.add(IDeviceService.class);
        return floodlightService;
	}

	/**
	 * Gets a name for this module.
	 * @return name for this module
	 */
	@Override
	public String getName() 
	{ return MODULE_NAME; }

	/**
	 * Check if events must be passed to another module before this module is
	 * notified of the event.
	 */
	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) 
	{
		return (OFType.PACKET_IN == type 
				&& (name.equals(ArpServer.MODULE_NAME) 
					|| name.equals(DeviceManagerImpl.MODULE_NAME))); 
	}

	/**
	 * Check if events must be passed to another module after this module has
	 * been notified of the event.
	 */
	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) 
	{ return false; }
}
