// ALIADA - Automatic publication under Linked Data paradigm
//          of library and museum data
//
// Component: aliada-rdfizer
// Responsible: ALIADA Consortiums
package eu.aliada.rdfizer.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.aliada.rdfizer.datasource.rdbms.JobConfiguration;
import eu.aliada.rdfizer.datasource.rdbms.JobConfigurationRepository;
import eu.aliada.rdfizer.datasource.rdfstore.AliadaRDFStoreDAO;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

/** 
 * A simple in-memory cache we used to avoid unnecessary datasource accesses.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
@Component
public class Cache {
	
	private static final String DEFAULT_ALIADA_CLASS = "http://erlangen-crm.org/120111/E19_Physical_Object";
	private final Map<String, String> crm2AliadaClasses = new HashMap<String, String>();
	{
		crm2AliadaClasses.put("E22", "http://erlangen-crm.org/current/E22_Man-Made_Object");
		crm2AliadaClasses.put("E25", "http://erlangen-crm.org/current/E25_Man-Made_Feature");
		crm2AliadaClasses.put("E78", "http://erlangen-crm.org/current/E78_Collection");	
		crm2AliadaClasses.put("E78", "http://erlangen-crm.org/current/E78_Collection");	

	}
	
	private final Map<String, String> crm2AliadaEventTypes = new HashMap<String, String>();
	{
		crm2AliadaEventTypes.put("http://www.cidoc-crm.org/crm-concepts/E65", "http://erlangen-crm.org/current/E65_Creation");
		crm2AliadaEventTypes.put("http://terminology.lido-schema.org/lido00012", "http://erlangen-crm.org/current/E65_Creation");
	}
	
	final ConcurrentMap<Integer, JobConfiguration> activeJobConfigurations 
		= new ConcurrentLinkedHashMap
			.Builder<Integer, JobConfiguration>()
			.maximumWeightedCapacity(50)
			.build();
	
	@Autowired
	private AliadaRDFStoreDAO rdfStore;
	
	@Autowired
	protected JobConfigurationRepository jobConfigurationRepository;
	
	/**
	 * Returns the ALIADA class corresponding to the given CIDOC-CRM class.
	 * 
	 * @param crmClass the CIDOC-CRM class.
	 * @return the ALIADA class that corresponds to the given input class.
	 */
	public String getAliadaClassFrom(final String crmClass) {
		String uri = crm2AliadaClasses.get(crmClass);
		if (uri == null) {
			uri = rdfStore.crm2AliadaClass(crmClass);
		}
		return uri != null ? uri : DEFAULT_ALIADA_CLASS;
	}
	
	/**
	 * Returns the ALIADA event type class corresponding to the given CIDOC-CRM class.
	 * 
	 * @param crmClass the CIDOC-CRM event type class.
	 * @return the ALIADA class that corresponds to the given input class.
	 */
	public String getAliadaEventTypeClassFrom(final String crmClass) {
		String uri = crm2AliadaClasses.get(crmClass);
		if (uri == null) {
			uri = rdfStore.crm2AliadaClass(crmClass);
		}
		return uri;
	}
	
	
	
	/**
	 * Returns the job configuration associated with a given identifier.
	 * 
	 * @param id the job identifier.
	 * @return the job configuration associated with a given identifier.
	 */
	public JobConfiguration getJobConfiguration(final Integer id) {
		JobConfiguration configuration = activeJobConfigurations.get(id);
		if (configuration == null) {
			configuration = jobConfigurationRepository.findOne(id);
		}
		return configuration;
	}
}