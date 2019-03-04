package org.alfresco.service.search.backup;

import org.testng.annotations.Test;

/**
 * 
 * Documentation: http://docs.alfresco.com/6.0/tasks/solr6-backup.html
 * 
 * @author Paul Brodner
 *
 */
public class SearchServiceOnBackupTests extends AbstractBackupTest {

	@Test
	public void deleteBackupFolder() throws Exception {
		
		/**
		 * Site: siteForBackupTesting
		 * 			> documentLibrary
		 * 				| siteForBackupTesting
		 *              |- fileBackedUp.txt
		 */
		
		cmisWrapper.authenticateUser(dataSite.getAdminUser())		
				   .usingSite(testSite).setLastContentModel(folder);
		cmisWrapper.deleteFolderTree().and().assertThat().doesNotExistInRepo();
	}
}
