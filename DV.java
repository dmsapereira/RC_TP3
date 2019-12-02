import java.util.HashMap;
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
	}

	public void setRouterObject(Router obj) {
		thisRouter = obj;
		this.routingTable = new HashMap<>();
		this.pReverse = false;
		this.expire = false;
	}

	public void setUpdateInterval(int u) {
		updateInterval = u;
	}

	public void setAllowPReverse(boolean flag) {
		this.pReverse = flag;
	}

	public void setAllowExpire(boolean flag) {
		this.expire = flag;
	}

	public void initalise() {
		for(Link link : this.thisRouter.getLinks()){
			int destination = link.getRouter(1);
			int inter = link.getInterface(0);
			int weight = link.getInterfaceWeight(0);
			this.routingTable.put(destination, new DVRoutingTableEntry(destination, inter, weight,this.thisRouter.getCurrentTime()));
		}
	}

	public int getNextHop(int destination) {

		DVRoutingTableEntry next = this.routingTable.get(destination);

		if (next == null)
			return UNKNOWN;
	
		return next.getInterface();
	}

	public void tidyTable() {

	}

	public Packet generateRoutingPacket(int iface) {
	}

	public void processRoutingPacket(Packet p, int iface) {
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
		String table_format = "Destination: %d -- Interface: %d -- Metric: %d -- Time: %d";
		return String.format(table_format, this.destination, this.iface, this.metric, this.time);
	}
}
