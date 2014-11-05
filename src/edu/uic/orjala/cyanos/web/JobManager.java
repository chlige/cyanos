/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author George Chlipala
 *
 */
public class JobManager {

	private final ConcurrentSkipListMap<String,Job> activeJobs = new ConcurrentSkipListMap<String,Job>();
	
	/**
	 * 
	 */
	public JobManager() { }
	
	public Collection<Job> getActiveJobs() {
		return this.activeJobs.values();
	}
	
	public void addJob(Job job) {
		this.activeJobs.put(job.getID(), job);
	}
	
	public void removeJob(Job job) {
		this.activeJobs.remove(job.getID());
	}
	
	public void pruneJobs() {
		for ( Job job : this.activeJobs.values() ) {
			if ( job.getEndDate() != null ) {
				this.activeJobs.remove(job.getID());
			}
		}
	}
	
	public boolean hasActiveJobs() {
		for ( Job job : this.activeJobs.values() ) {
			if ( job.isWorking() ) return true;
		}
		return false;
	}
	
	public boolean hasCompletedJobs() {
		for ( Job job : this.activeJobs.values() ) {
			if ( job.isDone() ) return true;
		}
		return false;
	}
	
	public Job getJob(String jobid) {
		return this.activeJobs.get(jobid);
	}
}
