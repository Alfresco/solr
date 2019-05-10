package org.alfresco.test.search.functional.searchServices.cmis;

import org.alfresco.utility.Utility;
import org.alfresco.utility.data.provider.XMLDataConfig;
import org.alfresco.utility.data.provider.XMLTestData;
import org.alfresco.utility.data.provider.XMLTestDataProvider;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.QueryModel;
import org.alfresco.search.TestGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SolrSearchByPropertyTests extends AbstractCmisE2ETest
{
    /** Logger for the class. */
    private static Logger LOGGER = LoggerFactory.getLogger(SolrSearchByPropertyTests.class);
    private XMLTestData testData;

    @BeforeClass(alwaysRun = true)
    public void deployCustomModel() throws TestConfigurationException
    {
        deployCustomModel("model/tas-model.xml");
        cmisApi.authenticateUser(dataUser.getAdminUser());
    }
    
    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment()
    {
        if (testData != null)
        {
            testData.cleanup(dataContent);
        }
        else
        {
            LOGGER.warn("testData is inexplicably null - skipping clean up.");
        }
    }

    @Test(dataProviderClass = XMLTestDataProvider.class, dataProvider = "getAllData")
    @XMLDataConfig(file = "src/test/resources/testdata/search-by-property.xml")
    public void prepareDataForSearchWithProperty(XMLTestData testData) throws Exception
    {
        this.testData = testData;
        this.testData.createUsers(dataUser);
        this.testData.createSitesStructure(dataSite, dataContent, dataUser);
        // wait for solr index
        Utility.waitToLoopTime(getSolrWaitTimeInSeconds());
    }

    @Test(groups = { TestGroup.CMIS },
            dataProviderClass = XMLTestDataProvider.class, dataProvider = "getQueriesData", dependsOnMethods = "prepareDataForSearchWithProperty")
    @XMLDataConfig(file = "src/test/resources/testdata/search-by-property.xml")
    public void executeSortedSearchByID(QueryModel query) throws Exception
    {
        cmisApi.withQuery(query.getValue())
            .applyNodeRefsFrom(testData).assertResultsCount().equals(query.getResults());
    }
}
