package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.body.SiteMembershipRequest;
import org.alfresco.rest.requests.RestPeopleApi;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.social.alfresco.api.entities.Site.Visibility;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for getSiteMembershipRequests (get /people/{personId}/site-membership-requests) RestAPI call
 * 
 * @author Cristina Axinte
 */
@Test(groups = { "rest-api", "people", "sanity" })
public class GetSiteMembershipRequestsSanityTests extends RestTest
{
    @Autowired
    RestPeopleApi peopleApi;

    UserModel userModel;
    SiteModel siteModel;
    UserModel newMember;
    SiteMembershipRequest siteMembershipRequest;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        String siteId = RandomData.getRandomName("site");
        siteModel = dataSite.usingUser(userModel).createSite(new SiteModel(Visibility.MODERATED, siteId, siteId, siteId, siteId));
        newMember = dataUser.createRandomTestUser();
        siteMembershipRequest = new SiteMembershipRequest("Please accept me", siteModel.getId(), "New request");
        restClient.authenticateUser(newMember);
        peopleApi.useRestClient(restClient);
        peopleApi.addSiteMembershipRequest(newMember, siteMembershipRequest);
        peopleApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.CREATED);

        peopleApi.useRestClient(restClient);
    }

    @Bug(id = "MNT-16557")
    @TestRail(section = { "rest-api", "people" }, executionType = ExecutionType.SANITY, description = "Verify manager user gets all site membership requests of a specific person with Rest API and response is successful (200)")
    public void managerUserGetsSiteMembershipRequestsWithSuccess() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        
        restClient.authenticateUser(managerUser);
        peopleApi.getSiteMembershipRequests(newMember).assertEntriesListIsNotEmpty();
        peopleApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.OK);
    }
    
    @Bug(id = "MNT-16557")
    @TestRail(section = { "rest-api", "people" }, executionType = ExecutionType.SANITY, description = "Verify collaborator user fails to get all site membership requests of another user with Rest API (403)")
    public void collaboratorUserFailsToGetSiteMembershipRequestsOfAnotherUser() throws Exception
    {
        UserModel collaboratorrUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(collaboratorrUser, siteModel, UserRole.SiteCollaborator);
        
        restClient.authenticateUser(collaboratorrUser);
        peopleApi.getSiteMembershipRequests(newMember).assertEntriesListIsNotEmpty();
        peopleApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.FORBIDDEN);
    }
}
