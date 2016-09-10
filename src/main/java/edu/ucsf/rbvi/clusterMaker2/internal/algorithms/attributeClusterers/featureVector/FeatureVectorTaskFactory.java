package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.featureVector;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class FeatureVectorTaskFactory extends AbstractClusterTaskFactory {
	FeatureVectorContext context = null;
	
	public FeatureVectorTaskFactory(ClusterManager clusterManager) {
		super(clusterManager);
		context = new FeatureVectorContext();
	}
	
	public String getShortName() {return FeatureVectorCluster.SHORTNAME;};
	public String getName() {return FeatureVectorCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.ATTRIBUTE); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new FeatureVectorCluster(context, clusterManager));
	}
	
}
	
	



