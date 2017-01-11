package org.alfresco.rest.people;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActivityModel;
import org.alfresco.rest.model.RestActivityModelsCollection;
import org.alfresco.rest.model.RestActivitySummaryModel;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.ActivityType;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 
 * @author Cristina Axinte
 *
 */
public class GetPeopleActivitiesFullTests extends RestTest
{
    UserModel userModel, adminUser, managerUser;
    SiteModel siteModel1, siteModel2;
    FileModel fileInSite1, fileInSite2;
    FolderModel folderInSite2;
    private RestActivityModelsCollection restActivityModelsCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel1 = dataSite.usingUser(userModel).createPublicRandomSite();
        fileInSite1 = dataContent.usingUser(userModel).usingSite(siteModel1).createContent(DocumentType.TEXT_PLAIN);
        
        siteModel2 = dataSite.usingUser(userModel).createPublicRandomSite();
        folderInSite2 = dataContent.usingUser(userModel).usingSite(siteModel2).createFolder(); 
        fileInSite2 = dataContent.usingAdmin().usingSite(siteModel2).createContent(DocumentType.TEXT_PLAIN);   
        
        managerUser = dataUser.createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel2, UserRole.SiteManager);
        
        // only once the activity list is checked with retry in order not to wait the entire list in each test
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getPersonActivitiesWithRetry();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("4");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its activities using me with Rest API and response is successful")
    public void userGetsItsPeopleActivitiesUsingMe() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("4");
             
        List<RestActivitySummaryModel> allActivitySummary = new ArrayList<RestActivitySummaryModel>();
        for (RestActivityModel activityModel: restActivityModelsCollection.getEntries())
        {
            allActivitySummary.add(activityModel.onModel().getActivitySummary());
        }
       
        assertActivitySummaryTitleIsPresent(allActivitySummary, fileInSite1.getName());
        assertActivitySummaryTitleIsPresent(allActivitySummary, folderInSite2.getName());
        assertActivitySummaryTitleIsPresent(allActivitySummary, fileInSite2.getName());
        restActivityModelsCollection.assertThat().entriesListContains("activityType", ActivityType.USER_JOINED.toString());
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify activity summary from user gets activities response with Rest API")
    public void userGetPeopleActivitiesWithActivitySummaryCheck() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("4");
        RestActivitySummaryModel summary = restActivityModelsCollection.getEntries().get(0).onModel().getActivitySummary();
                summary.assertThat().field("firstName").is(managerUser.getUsername() + " FirstName")
                .and().field("lastName").is("LN-" + managerUser.getUsername())
                .and().field("memberFirstName").is(managerUser.getUsername() + " FirstName")
                .and().field("role").is(managerUser.getUserRole())
                .and().field("memberLastName").is("LN-" + managerUser.getUsername())
                .and().field("title").is(String.format("%s FirstName LN-%s (%s)", managerUser.getUsername(), managerUser.getUsername(), managerUser.getUsername()))
                .and().field("memberPersonId").is(managerUser.getUsername());
    }
    
    @Bug(id = "ACE-5460")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user cannot get activities for empty user with Rest API and response is 400")
    public void userCannotGetPeopleActivitiesForEmptyPersonId() throws Exception
    {
        UserModel emptyUserName = new UserModel("", "password");
        
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(emptyUserName).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                                .containsSummary(RestErrorModel.LOCAL_NAME_CONSISTANCE)
                                .stackTraceIs(RestErrorModel.STACKTRACE)
                                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities successfully using parameter 'who' with 'me' value with Rest API")
    public void userGetsPeopleActivitiesUsingMeForWhoParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).usingParams("who=me").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("2");
        restActivityModelsCollection.assertThat().entriesListContains("postPersonId", userModel.getUsername().toLowerCase())
                .and().entriesListDoesNotContain("postPersonId", adminUser.getUsername().toLowerCase())
                .and().entriesListDoesNotContain("postPersonId", managerUser.getUsername().toLowerCase());
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its activities for siteId specified in siteId parameter using me with Rest API and response is successful")
    public void userGetItsPeopleActivitiesForASpecificSite() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).usingParams(String.format("siteId=%s", siteModel1.getId())).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("1");
        restActivityModelsCollection.assertThat().entriesListContains("siteId", siteModel1.getId())
                .and().entriesListDoesNotContain("siteId", siteModel2.getId());
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities for user with no activities with Rest API and response is successful")
    public void userGetPeopleActivitiesForUserWithNoActivities() throws Exception
    {
        UserModel userNoActivities = dataUser.createRandomTestUser();
        dataSite.usingUser(userNoActivities).createPublicRandomSite();
        
        restActivityModelsCollection = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userNoActivities).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("0");
        restActivityModelsCollection.assertThat().entriesListIsEmpty();
    }
    
    private void assertActivitySummaryTitleIsPresent(List<RestActivitySummaryModel> allActivitySummary, String title)
    {
        for (RestActivitySummaryModel activitySummary: allActivitySummary)
        {
            if (activitySummary.getTitle().equals(title))
            {
                Assert.assertEquals(activitySummary.getTitle(), title, String.format("%s title was not found in Activity Summary", title));
                break;
            }
        }
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user cannot get activities for siteId that user doesn't have access to with Rest API and response is not found")
    public void userGetPeopleActivitiesForASiteWithNoAccess() throws Exception
    {
        SiteModel siteNoAccess = dataSite.usingUser(managerUser).createPrivateRandomSite();
        
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).usingParams(String.format("siteId=%s", siteNoAccess.getId())).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, siteNoAccess.getId()));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user cannot get activities for another user with Rest API and response is permission denied")
    public void userGetPeopleActivitiesForAnotherUser() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(managerUser).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities with properties parameter applied with Rest API and response is successful")
    public void userGetPeopleActivitiesUsingPropertiesParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().usingParams("properties=postPersonId").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("4");
        restActivityModelsCollection.assertThat().entriesListContains("postPersonId", userModel.getUsername().toLowerCase())
                .and().entriesListContains("postPersonId", adminUser.getUsername().toLowerCase())
                .and().entriesListContains("postPersonId", managerUser.getUsername().toLowerCase())
                .and().entriesListDoesNotContain("postedAt")
                .and().entriesListDoesNotContain("feedPersonId")
                .and().entriesListDoesNotContain("siteId")
                .and().entriesListDoesNotContain("activitySummary")
                .and().entriesListDoesNotContain("id")
                .and().entriesListDoesNotContain("activityType");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities with skipCount parameter applied with Rest API and response is successful")
    public void userGetPeopleActivitiesUsingSkipCountParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().usingParams("skipCount=2").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("2");
        restActivityModelsCollection.getEntries().get(0).onModel().assertThat().field("postPersonId").is(userModel.getUsername().toLowerCase())
                .and().field("activityType").is(ActivityType.FOLDER_ADDED.toString());
        restActivityModelsCollection.getEntries().get(1).onModel().assertThat().field("postPersonId").is(userModel.getUsername().toLowerCase())
                .and().field("activityType").is(ActivityType.FILE_ADDED.toString());
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.FULL })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities with maxItems parameter applied with Rest API and response is successful")
    public void userGetPeopleActivitiesUsingMaxItemsParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().usingParams("maxItems=2").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("2");
        restActivityModelsCollection.getEntries().get(0).onModel().assertThat().field("postPersonId").is(managerUser.getUsername().toLowerCase())
                .and().field("activityType").is(ActivityType.USER_JOINED.toString());
        restActivityModelsCollection.getEntries().get(1).onModel().assertThat().field("postPersonId").is(adminUser.getUsername().toLowerCase())
                .and().field("activityType").is(ActivityType.FILE_ADDED.toString());
    }
}
