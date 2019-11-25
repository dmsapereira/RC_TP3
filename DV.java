
/*
 * My Distance-Vector Routing Implementation
 */
public class DV implements RoutingAlgorithm
{
	static final int LOCAL = -1;		// local destination/interface
	static final int UNKNOWN = -2;		// unknown destination
	static final int BROADCAST = 255;	// broadcast address
	static final int INFINITY = 60;		// "infinity" metric
	
	Router thisRouter = null;  // the router were this algorithm is running

	Map<Integer, DVRoutingTableEntry> routingTable;
	// declare your routing table here using DVRoutingTableEntry (see end of this file)

	public DV()
	{
		routingTable = new HashMap<>();
	}

	public void setRouterObject(Router obj)
	{
		thisRouter = obj;

	}

	public void setUpdateInterval(int u)
	{
	}

	public void setAllowPReverse(boolean flag)
	{
	}

	public void setAllowExpire(boolean flag)
	{
	}

	public void initalise()
	{
	}

	public int getNextHop(int destination)
	{
		return UNKNOWN;
	}

	public void tidyTable()
	{
	}

	public Packet generateRoutingPacket(int iface)
	{
		return null;
	}

	public void processRoutingPacket(Packet p, int iface)
	{
	}

	public void showRoutes()
	{
		System.out.println("Router "+thisRouter.getId() );

	}
}



/*
 * My Routing Table Entry
 */
class DVRoutingTableEntry implements RoutingTableEntry
{	
	int destination;
	int iface;
	int metric;
	int time;

	public DVRoutingTableEntry(int d, int i, int m, int t)
	{
		this.destination = d;
		this.iface = i;
		this.metric = m;
		this.time = t;
	}

	public int getDestination() { return this.destination; }

	public void setDestination(int d) { this.destination = d; }

	public int getInterface() { return this.iface; }

	public void setInterface(int i) { this.iface = i; }

	public int getMetric() { return this.metric; }

	public void setMetric(int m) { this.metric = m; } 

	public int getTime() { return this.time;}

	public void setTime(int t) {  this.time = t;}

	public String toString() 
	{	
		String table_format = "Destination: %d\n Interface: %d\n Metric: %d\n Time: %d\n";
		return String.format(table_format, this.destination, this.iface, this.metric, this.time);
	}
}

