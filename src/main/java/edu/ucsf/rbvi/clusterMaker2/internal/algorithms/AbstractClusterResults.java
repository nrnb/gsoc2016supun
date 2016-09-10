package edu.ucsf.rbvi.clusterMaker2.internal.algorithms;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * This class calculates a number of cluster statistics on a set of
 * of node clusters.  The node clusters passed as a list of lists of
 * CyNodes, where each list of CyNodes represents a cluster.  Currently
 * the calculated statistics include:
 *	Number of clusters
 *	Average cluster size
 *	Maximum cluster size
 *	Minimum cluster size
 *	Modularity, defined as:
 *        M = sum(I(Ci)/|E| - (D(Ci)/2*|E|)**2
 *    where Ci is a cluster,
 *          I(Ci) is the number of internal edges in the cluster
 *          D(Ci) is the degree (number of external edges) of the cluster
 *          |E| is the total number of edges in the network
 *
 * Modularity for each cluster defined as:
 * 	Mi = I(Ci)/|E| - {(2*I(Ci) - D(Ci))/ 2*|E|}^2
 *
 * (see Community Detection via Maximization of Modularity and Its Variants.
 * Mingming Chen, Konstantin Kuzmin, Boleslaw K. Szymanski)
 */

public class AbstractClusterResults implements ClusterResults {
	
	private List<List<CyNode>> clusters;
	private CyNetwork network;
	private int clusterCount;
	private double averageSize;
	private int maxSize;
	private int minSize;
	private double clusterCoefficient;
	private List<Double> modularityList;
	private List<Double> scoreList;
	private double modularity;
	private String extraText = null;

	public AbstractClusterResults(CyNetwork network, List<List<CyNode>> cl, 
	                              List<Double> algorithmScores, String extraInformation) { 
		this.network = network;
		clusters = cl; 
		extraText = extraInformation;
		modularityList = new ArrayList<Double>();
		calculate();
		scoreList = algorithmScores;
	}

	public AbstractClusterResults(CyNetwork network, List<List<CyNode>> cl, String extraInformation) { 
		this(network, cl, null, extraInformation);
	}

	public AbstractClusterResults(CyNetwork network, List<List<CyNode>> cl) { 
		this(network, cl, null, null);
	}

	public String toString() {
		NumberFormat nf = NumberFormat.getInstance();
		String result = "  Clusters: "+clusterCount+"\n";
		result += "  Average size: "+nf.format(averageSize)+"\n";
		result += "  Maximum size: "+maxSize+"\n";
		result += "  Minimum size: "+minSize+"\n";
		result += "  Modularity: "+nf.format(modularity);
		if (extraText != null)
			result += "  "+extraText;
		return result;
	}

	public String toHTML() {
		NumberFormat nf = NumberFormat.getInstance();
		String result = "<ul style=\"margin-top:0px;margin-bottom:5px;";
		result+= "padding-left:5px;margin-left:5px;list-style-type:none\">";
		result += "<li><b>Clusters:</b> "+clusterCount+"</li>";
		result += "<li><b>Average size:</b> "+nf.format(averageSize)+"</li>";
		result += "<li><b>Maximum size:</b> "+maxSize+"</li>";
		result += "<li><b>Minimum size:</b> "+minSize+"</li>";
		result += "<li><b>Modularity:</b> "+nf.format(modularity)+"</li>";
		if (extraText != null)
			result += "<li>"+extraText+"</li>";
		result += "</ul>";
		return result;
	}


	public double getScore() { return modularity; }

	public List<List<CyNode>> getClusters() {
		return clusters;
	}

	public List<Double> getModularityList(){
		return modularityList;
	}

	/**
	 * Return the algorithm-provided score list (if any).  If no scores
	 * were provided by the algorithm, this will return null.
	 */
	public List<Double> getScoreList(){
		return scoreList;
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class))
			return toString();
		return clusters;
	}

	private void calculate() {
		clusterCount = clusters.size();
		averageSize = 0.0;
		maxSize = -1;
		minSize = Integer.MAX_VALUE;
		clusterCoefficient = 0.0;
		modularity = 0.0;
		// double edgeCount = (double)network.getEdgeCount();
		double edgeCount = getReducedEdgeCount();

		int clusterNumber = 0;
		for (List<CyNode> cluster: clusters) {
			averageSize += (double)cluster.size() / (double)clusterCount;
			maxSize = Math.max(maxSize, cluster.size());
			minSize = Math.min(minSize, cluster.size());
			double innerEdges = (double)getInnerEdgeCount(cluster);
			double outerEdges = (double)getOuterEdgeCount(cluster);
			clusterCoefficient += (innerEdges / (innerEdges+outerEdges)) / (double)(clusterCount);

			// double percentEdgesInCluster = innerEdges/edgeCount;
			// double percentEdgesTouchingCluster = (innerEdges+outerEdges)/edgeCount;
			// modularity += percentEdgesInCluster - percentEdgesTouchingCluster*percentEdgesTouchingCluster;

 			// 	Mi = I(Ci)/|E| - {(2*I(Ci) - D(Ci))/ 2*|E|}^2
			double proportionEdgesInCluster = innerEdges/edgeCount; // I(Ci)/|E|
			double proportionEdgesOutCluster = outerEdges/edgeCount;

			double clusterModularity = proportionEdgesInCluster - Math.pow((2*innerEdges - outerEdges)/(2*edgeCount), 2);
			modularityList.add(clusterModularity);
			modularity += clusterModularity;
			//modularity += proportionEdgesInCluster - (proportionEdgesOutCluster/2)*(proportionEdgesOutCluster/2);
			clusterNumber++;
		}
	}

	private int getInnerEdgeCount(List<CyNode> cluster) {
		return ModelUtils.getConnectingEdges(network, cluster).size();
	}

	private int getOuterEdgeCount(List<CyNode> cluster) {
		Set<CyEdge> innerEdgeSet = new HashSet<CyEdge>(ModelUtils.getConnectingEdges(network, cluster));
		List<CyEdge> outerEdges = new ArrayList<CyEdge>();
		for (CyNode node: cluster) {
			for (CyEdge edge: network.getAdjacentEdgeList(node, CyEdge.Type.ANY)) {
				if (!innerEdgeSet.contains(edge))
					outerEdges.add(edge);
			}
		}
		return outerEdges.size();
	}

	private double getReducedEdgeCount() {
		int edges = 0;
		for (List<CyNode> cluster: clusters) {
			for (CyNode node: cluster) {
				edges += network.getAdjacentEdgeList(node, CyEdge.Type.ANY).size();
			}
		}
		return (double)edges/2.0;
	}

}
