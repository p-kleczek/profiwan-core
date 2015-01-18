package pkleczek.profiwan.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.joda.time.DateTime;

public class Clustering {
	
	public static List<DateTime> chooseClustering(List<List<DateTime>> clusterings, long timespanBetweenRevisions) {
		 List<List<DateTime>> admissableClusterings = new ArrayList<List<DateTime>>();
		 for (List<DateTime> clustering : clusterings) {
			 if (minimalPeriodBetweenCentroids(clustering) > timespanBetweenRevisions) {
				 admissableClusterings.add(clustering);
			 }
		 }
		 
		 if (admissableClusterings.isEmpty()) {
			 return null;
		 }
		 
		 List<DateTime> clustering = admissableClusterings.get((new Random()).nextInt(admissableClusterings.size()));
		 return clustering;
	}
	
	public static double minimalPeriodBetweenCentroids(List<DateTime> centroids) {
		long minimal = Long.MAX_VALUE;
		
		Collections.sort(centroids);
		for (int i = 1; i < centroids.size(); i++) {
			long diff = Math.abs(centroids.get(i-1).getMillis() - centroids.get(i).getMillis());
			minimal = Math.min(minimal, diff);
		}
		
		return minimal;
	}

	public static List<DateTime> clusterize(int k, List<DateTime> data) {
		 // Centroid center as time in millis.
		 long[] centroids = new long[Math.min(k, data.size())];
		 
		 // 1. Randomly choose k items and make them as initial centroids.
		 List<DateTime> list = new ArrayList<DateTime>(data);
		 Collections.shuffle(list, new Random());
		 for (int i = 0; i < centroids.length; i++) {
			 centroids[i] = list.get(i).getMillis();
		 }
		 
		 Map<Long, Integer> assignment = new HashMap<Long, Integer>();
		 for (DateTime t : list) {
			 assignment.put(t.getMillis(), 0);
		 }
		 
			 //	4. Repeats steps 2 and 3, till no point switches clusters.
		 int counter = 0;
		 boolean wasSwitch = true;
		 while (wasSwitch) {
			 counter++;
			 if (counter == 1000) {
				 System.err.println("Too many interations!\n");
				 break;
			 }
			 
			 wasSwitch = false;
			 // 2. For each point, find the nearest centroid and assign the point to the cluster associated with the nearest centroid.
			 for (Long t : assignment.keySet()) {
				 for (int cInx = 0; cInx < centroids.length; cInx++) {
					 long oldDistance = Math.abs(centroids[assignment.get(t)] - t);
					 long newDistance = Math.abs(centroids[cInx] - t);
					 if (newDistance < oldDistance) {
						 assignment.put(t, cInx);
						 wasSwitch = true;
					 }
				 }
			 }
			 
			 // 3. Update the centroid of each cluster based on the items in that cluster. Typically, the new centroid will be the average of all points in the cluster. 
			 // Count chunks for each day of week.
			 int[] nChunks = new int[centroids.length];
			 for (Integer i : assignment.values()) {
				 nChunks[i]++;
			 }
			 // Compute average.
			 centroids = new long[centroids.length];
			 for (Entry<Long, Integer> e : assignment.entrySet()) {
				 centroids[e.getValue()] += e.getKey() / nChunks[e.getValue()];
			 }
		 }
		 
//		 System.out.println(assignment);
		 
		 // DEBUG: compute variance
//		 long variance = 0;
//		 for (Entry<Long, Integer> e : assignment.entrySet()) {
//			 variance += Math.abs(e.getKey() - centroids[e.getValue()]);
//		 }
//		 System.out.println("variance = " + variance);
		 
		 List<DateTime> times = new ArrayList<DateTime>();
		 for (long c : centroids) {
			 times.add(new DateTime(c));
		 }
		 return times;
	}
}
