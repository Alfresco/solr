package org.alfresco.rest.ratings;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.requests.RestRatingsApi;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "rest-api", "ratings", "sanity" })
public class GetRatingSanityTests extends RestTest
{
    @Autowired
    RestRatingsApi ratingsApi;

    private UserModel userModel;
    private SiteModel siteModel;
    private UserModel adminUser;
    private FolderModel folderModel;
    private FileModel document;
    private ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        userModel = dataUser.createUser(RandomStringUtils.randomAlphanumeric(20));
        adminUser = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        ratingsApi.useRestClient(restClient);

        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @BeforeClass
    public void setUp() throws Exception
    {
        folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUser);
        ratingsApi.likeDocument(document);
        ratingsApi.rateStarsToDocument(document, 5);
        restClient.disconnect();
    }

    @TestRail(section = { "rest-api",
            "ratings" }, executionType = ExecutionType.SANITY, description = "Verify user with Manager role is able to retrieve document ratings")
    public void managerIsAbleToRetrieveDocumentRatings() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        ratingsApi.getRatings(document);
        ratingsApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { "rest-api",
            "ratings" }, executionType = ExecutionType.SANITY, description = "Verify user with Collaborator role is able to retrieve document ratings")
    public void collaboratorIsAbleToRetrieveDocumentRatings() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        ratingsApi.getRatings(document);
        ratingsApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { "rest-api",
            "ratings" }, executionType = ExecutionType.SANITY, description = "Verify user with Contributor role is able to retrieve document ratings")
    public void contributorIsAbleToRetrieveDocumentRatings() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        ratingsApi.getRatings(document);
        ratingsApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { "rest-api",
            "ratings" }, executionType = ExecutionType.SANITY, description = "Verify user with Consumer role is able to retrieve document ratings")
    public void consumerIsAbleToRetrieveDocumentRatings() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        ratingsApi.getRatings(document);
        ratingsApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { "rest-api", "ratings" }, executionType = ExecutionType.SANITY, description = "Verify admin user is able to retrieve document ratings")
    public void adminIsAbleToRetrieveDocumentRatings() throws Exception
    {
        restClient.authenticateUser(adminUser);
        ratingsApi.getRatings(document);
        ratingsApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { "rest-api",
            "ratings" }, executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is not able to retrieve document ratings")
    @Bug(id = "MNT-16904")
    public void unauthenticatedUserIsNotAbleToRetrieveDocumentRatings() throws Exception
    {
        restClient.authenticateUser(new UserModel("random user", "random password"));
        ratingsApi.getRatings(document);
        ratingsApi.usingRestWrapper().assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}