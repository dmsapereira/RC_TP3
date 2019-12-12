import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	static final int TIMEOUT = 3;

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
			this.thisRouter.getId(), 
			new DVRoutingTableEntry(
				this.thisRouter.getId(), 
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

	//Checks, for every entry, if the link is up. If not, set the metric to INFINITY
	private void checkLinks(){
		for(DVRoutingTableEntry entry : this.routingTable.values()){
			if(!this.thisRouter.getInterfaceState(entry.getInterface()) && 
			entry.getMetric() != INFINITY){
				entry.setMetric(INFINITY);
				entry.setTime(this.thisRouter.getCurrentTime());
			}
		}
	}

	//Checks all entries for expired ones. The ones that are found are deleted
	private void cleanExpired(){
		List<Integer> keys = new LinkedList<>();

		//Not very efficient, but cant delete entries while iterating with a for each....
		keys.addAll(this.routingTable.keySet());

		for(int key : keys){
			DVRoutingTableEntry entry = this.routingTable.get(key);
			int timeElapsed = this.thisRouter.getCurrentTime() - entry.getTime();
			if(entry.getMetric() == INFINITY && timeElapsed > TIMEOUT * this.updateInterval)
				this.routingTable.remove(key);
		}
	}

	public void tidyTable() {
		this.checkLinks();

		if(this.expire)
			this.cleanExpired();
	}

	private Payload buildTableInformation(int iface){
		String entryFormat = "%s %s";
		Payload load = new Payload();

		//Not very easy on the eyes, but havin the fors nested in the ifs removes the overhead
		//of checking for pReverse on everytime in the ternary operator
		if(this.pReverse){
			for(DVRoutingTableEntry entry : this.routingTable.values()){
					load.addEntry(String.format(
						entryFormat, 
						entry.getDestination(),
						entry.getInterface() == iface ? INFINITY : entry.getMetric()));
			}
		}else{
			for(DVRoutingTableEntry entry : this.routingTable.values()){
				load.addEntry(String.format(
					entryFormat, 
					entry.getDestination(),
					entry.getMetric()));
			}
		}

		return load;
	}

	public Packet generateRoutingPacket(int iface) {
		Packet packet = new RoutingPacket(this.thisRouter.getId(), BROADCAST);
		packet.setPayload(buildTableInformation(iface));

		return packet; 
	}

	//Given data about an entry received via a RoutingPacket, executes the appropiate logic 
	private void processEntryData(int destination, int metric, int cost, int iface){
		DVRoutingTableEntry entry = this.routingTable.get(destination);

		//Nested because otherwise the else would be triggered even if the
		//entry was null, leading to NullPointers
		if(entry == null){
			if(metric != INFINITY){
				this.routingTable.put(destination, 
					new DVRoutingTableEntry(destination, 
											iface, 
											metric + cost,
											this.thisRouter.getCurrentTime()));
			}
		}else if(iface == entry.getInterface()){
			int metricPre = entry.getMetric();
			entry.setMetric(metric + cost > INFINITY ? INFINITY :  metric + cost);

			//basically, if it was already INFINITY, then don't mess with the time
			//only used for INFINITY, but we don't need the extra logic of checking for it
			if(metricPre != metric)
				entry.setTime(this.thisRouter.getCurrentTime());
		}else if(metric + cost < entry.getMetric()){
			entry.setInterface(iface);
			entry.setMetric(metric + cost);
			entry.setTime(this.thisRouter.getCurrentTime());
		}
	}

	public void processRoutingPacket(Packet p, int iface) {
		Iterator<Object> iterator = p.getPayload().getData().iterator();

		int cost = this.thisRouter.getLinks()[iface].getInterfaceWeight(this.thisRouter.getId());

		while(iterator.hasNext()){
			String data = String.valueOf(iterator.next());
			int destination = Integer.parseInt(data.split(" ")[0]);
			int metric = Integer.parseInt(data.split(" ")[1]);

			this.processEntryData(destination, metric, cost, iface);		
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

	@Override
	public String toString() {
		String table_format = "d %d i %d m %d";
		return String.format(table_format, this.destination, this.iface, this.metric);
	}
}