import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * My Distance-Vector Routing Implementation
 */
public class DV implements RoutingAlgorithm {
	static final int LOCAL = -1; // local destination/interface
	static final int UNKNOWN = -2; // unknown destination
	static final int BROADCAST = 255; // broadcast address
	static final int INFINITY = 60; // "infinity" metric
	static final int DEFAULT_UPDATE_INTERVAL = 200;

	Router thisRouter = null; // the router were this algorithm is running

	Map<Integer, DVRoutingTableEntry> routingTable;

	int updateInterval;
	boolean pReverse;
	boolean expire;
	
	// declare your routing table here using DVRoutingTableEntry (see end of this
	// file)

	public DV() {
		this.updateInterval = DEFAULT_UPDATE_INTERVAL;
		this.pReverse = false;
		this.expire = false;
	}

	public void setRouterObject(Router obj) {
		this.thisRouter = obj;
		this.routingTable = new HashMap<>();
	}

	public void setUpdateInterval(int u) {
		this.updateInterval = u;
	}

	public void setAllowPReverse(boolean flag) {
		this.pReverse = flag;
	}

	public void setAllowExpire(boolean flag) {
		this.expire = flag;
	}

	public void initalise() {
		this.routingTable.put(
			this.thisRouter.getId(), new DVRoutingTableEntry(this.thisRouter.getId(), 
															LOCAL, 
															0, 
															this.thisRouter.getCurrentTime()));
	}

	public int getNextHop(int destination) {

		DVRoutingTableEntry next = this.routingTable.get(destination);

		if (next == null || next.getMetric() == INFINITY)
			return UNKNOWN;
	
		return next.getInterface();
	}

	private void linkDown(int iface){
		for(DVRoutingTableEntry entry : this.routingTable.values()){
			if(entry.getInterface() == iface){
				entry.setMetric(INFINITY);
				entry.setTime(this.thisRouter.getCurrentTime());
			}
		}
	}

	public void tidyTable() {
	for(Link zelda : this.thisRouter.getLinks()){
			int thisSide = zelda.getRouter(0) == this.thisRouter.getId() ? 0 : 1;

			if(!zelda.isUp())
				this.linkDown(zelda.getInterface(thisSide));
	}
}

	private Payload buildTableInformation(int iface){
		String entryFormat = "%s %s %s";
		Payload load = new Payload();
		for(DVRoutingTableEntry entry : this.routingTable.values()){
				load.addEntry(String.format(entryFormat, 
											entry.getDestination(),
											entry.getInterface(),
											entry.getInterface() == iface ? INFINITY : entry.getMetric()));
		}
		return load;
	}

	public Packet generateRoutingPacket(int iface) {
		Packet packet = new RoutingPacket(this.thisRouter.getId(), BROADCAST);
		packet.setPayload(buildTableInformation(iface));
		return packet; 
	}

	public void processRoutingPacket(Packet p, int iface) {
		DVRoutingTableEntry entry;

		Iterator<Object> iterator = p.getPayload().getData().iterator();

		int otherSide = this.thisRouter.getLinks()[iface].getRouter(0) == this.thisRouter.getId() ? 1 : 0;

		if(!this.routingTable.containsKey(p.getSource())){
			this.routingTable.put(p.getSource(), 
				new DVRoutingTableEntry(p.getSource(), 
										iface, 
										this.thisRouter.getLinks()[iface].getInterfaceWeight(this.thisRouter.getId()), 
										this.thisRouter.getCurrentTime()));
		}

		while(iterator.hasNext()){
			String data = (String)iterator.next();
			int destination = Integer.parseInt(data.split(" ")[0]);
			int otherIface = Integer.parseInt(data.split(" ")[1]);
			int metric = Integer.parseInt(data.split(" ")[2]);
			int cost = this.thisRouter.getLinks()[iface].isUp() ? 
						this.thisRouter.getLinks()[iface].getInterfaceWeight(this.thisRouter.getId()) :
						this.routingTable.get(p.getSource()).getMetric();


			if((entry = this.routingTable.get(destination)) == null){
				this.routingTable.put(destination, 
					new DVRoutingTableEntry(destination, 
											iface, 
											metric + cost,
											this.thisRouter.getCurrentTime()));
			}else if(metric == INFINITY && entry.getInterface() == iface){
				entry.setMetric(metric);
				entry.setTime(this.thisRouter.getCurrentTime());
			}else if(entry.getMetric() > metric + cost && 
					 !(entry.getMetric() == INFINITY &&
					   this.thisRouter.getLinks()[iface].getInterface(otherSide) == otherIface)){
				entry.setInterface(iface);
				entry.setMetric(metric + cost);
				entry.setTime(this.thisRouter.getCurrentTime());
			}
		}
	}

	public void showRoutes() {
		System.out.println("Router " + thisRouter.getId());
		for (DVRoutingTableEntry entry : this.routingTable.values())
			System.out.println(entry);
	}
}

/*
 * My Routing Table Entry
 */
class DVRoutingTableEntry implements RoutingTableEntry {
	int destination, iface, metric, time;

	public DVRoutingTableEntry(int d, int i, int m, int t) {
		this.destination = d;
		this.iface = i;
		this.metric = m;
		this.time = t;
	}

	public int getDestination() {
		return this.destination;
	}

	public void setDestination(int d) {
		this.destination = d;
	}

	public int getInterface() {
		return this.iface;
	}

	public void setInterface(int i) {
		this.iface = i;
	}

	public int getMetric() {
		return this.metric;
	}

	public void setMetric(int m) {
		this.metric = m;
	}

	public int getTime() {
		return this.time;
	}

	public void setTime(int t) {
		this.time = t;
	}

	public String toString() {
		String table_format = "d %d i %d m %d";
		return String.format(table_format, this.destination, this.iface, this.metric);
	}
}
