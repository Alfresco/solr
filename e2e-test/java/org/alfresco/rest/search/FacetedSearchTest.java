/*
 * Copyright (C) 2017 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.search;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Faceted search test.
 * @author Michael Suzuki
 *
 */
public class FacetedSearchTest extends AbstractSearchTest
{

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH  }, executionType = ExecutionType.REGRESSION,
              description = "Checks facet queries for the Search api")
    /**
     * Perform the below facet query.
     * {
     *    "query": {
     *      "query": "cars",
     *      "language": "afts"
     *    },
     *      "facetQueries": [
     *          {"query": "content.size:[o TO 102400]", "label": "small"},
     *          {"query": "content.size:[102400 TO 1048576]", "label": "medium"},
     *          {"query": "content.size:[1048576 TO 16777216]", "label": "large"}
     *    ],
     *      "facetFields": {"facets": [{"field": "'content.size'"}]}
     * }
     * 
     * Expected response
     * {"list": {
     *     "entries": [... All the results],
     *     "pagination": {
     *        "maxItems": 100,
     *        "hasMoreItems": false,
     *        "totalItems": 61,
     *        "count": 61,
     *        "skipCount": 0
     *     },
     *     "context": {
     *        "consistency": {"lastTxId": 512},
     *        "facetQueries": [
     *           {
     *              "count": 61,
     *              "label": "small",
     *              "filterQuery": "content.size:[o TO 102400]"
     *           },
     *           {
     *              "count": 0,
     *              "label": "large",
     *              "filterQuery": "content.size:[o TO 102400]"
     *           },
     *           {
     *              "count": 0,
     *              "label": "medium",
     *              "filterQuery": "content.size:[o TO 102400]"
     *           }
     *        ]
     *     }
     * }}
     * @throws Exception
     */
    public void searchWithFaceting() throws Exception
    {        
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq =  new RestRequestQueryModel();
        queryReq.setQuery("cars");
        query.setQuery(queryReq);

        List<FacetQuery> facets = new ArrayList<FacetQuery>();
        facets.add(new FacetQuery("content.size:[0 TO 102400]", "small"));
        facets.add(new FacetQuery("content.size:[102400 TO 1048576]", "medium"));
        facets.add(new FacetQuery("content.size:[1048576 TO 16777216]", "large"));
        query.setFacetQueries(facets);
        
        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<Object> list = new ArrayList<Object>();
        list.add(new FacetFieldQuery("'content.size'"));
        facetFields.setFacets(list);
        
        query.setFacetFields(facetFields);
        
        SearchResponse response =  query(query);
        response.assertThat().entriesListIsNotEmpty();
        response.getContext().assertThat().field("facetQueries").isNotEmpty();
        FacetFieldBucket facet = response.getContext().getFacetQueries().get(0);
        facet.assertThat().field("label").contains("small").and().field("count").isGreaterThan(0);
        facet.assertThat().field("label").contains("small").and().field("filterQuery").is("content.size:[0 TO 102400]");
        response.getContext().getFacetQueries().get(1).assertThat().field("label").contains("large")
                    .and().field("count").isLessThan(1)
                    .and().field("filterQuery").is("content.size:[1048576 TO 16777216]");
        response.getContext().getFacetQueries().get(2).assertThat().field("label").contains("medium")
                    .and().field("count").isLessThan(1)
                    .and().field("filterQuery").is("content.size:[102400 TO 1048576]");
      //We don't expect to see the FacetFields if group is being used.
        Assert.assertNull(response.getContext().getFacetsFields());
        Assert.assertNull(response.getContext().getFacetIntervals());
    }
    /**
     * * Perform a group by faceting, below test groups the facet by group name foo.
     * {
     *    "query": {
     *      "query": "cars",
     *      "language": "afts"
     *    },
     *      "facetQueries": [
     *          {"query": "content.size:[o TO 102400]", "label": "small","group":"foo"},
     *          {"query": "content.size:[102400 TO 1048576]", "label": "medium","group":"foo"},
     *          {"query": "content.size:[1048576 TO 16777216]", "label": "large","group":"foo"}
     *    ],
     *      "facetFields": {"facets": [{"field": "'content.size'"}]}
     * }
     * 
     * Expected response
     * {"list": {
     *     "entries": [... All the results],
     *     "pagination": {
     *        "maxItems": 100,
     *        "hasMoreItems": false,
     *        "totalItems": 61,
     *        "count": 61,
     *        "skipCount": 0
     *     },
     *     "context": {
     *        "consistency": {"lastTxId": 512},
     *        //Added below as part of SEARCH-374
     *        "facetsFields": [
     *          {  "label": "foo",
     *             "buckets": [
     *               { "label": "small", "count": 61, "filterQuery": "content.size:[o TO 102400]"},
     *               { "label": "large", "count": 0, "filterQuery": "content.size:[1048576 TO 16777216]"},
     *               { "label": "medium", "count": 61, "filterQuery": "content.size:[102400 TO 1048576]"}
     *             ]
     *          }
     *     }
     * }}
     * @throws Exception 
     */
    public void searchFacetGroup() throws Exception
    {
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq =  new RestRequestQueryModel();
        queryReq.setQuery("cars");
        query.setQuery(queryReq);

        List<FacetQuery> facets = new ArrayList<FacetQuery>();
        facets.add(new FacetQuery("content.size:[0 TO 102400]", "small", "foo"));
        facets.add(new FacetQuery("content.size:[102400 TO 1048576]", "medium","foo"));
        facets.add(new FacetQuery("content.size:[1048576 TO 16777216]", "large","foo"));
        query.setFacetQueries(facets);
        
        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<Object> list = new ArrayList<Object>();
        list.add(new FacetFieldQuery("'content.size'"));
        facetFields.setFacets(list);
        
        query.setFacetFields(facetFields);
        
        SearchResponse response =  query(query);
        //We don't expect to see the FacetQueries if group is being used.
        Assert.assertTrue(response.getContext().getFacetQueries().isEmpty());
        //Validate the facet field structure is correct.
        Assert.assertFalse(response.getContext().getFacetsFields().isEmpty());
        RestResultBucketsModel bucket = response.getContext().getFacetsFields().get(0);
        bucket.assertThat().field("label").isNotEmpty();
        bucket.assertThat().field("count").isNotEmpty();
        bucket.assertThat().field("filterQuery").isNotEmpty();
        Assert.assertEquals(bucket.getLabel(), "foo");
        bucket.assertThat().field("label").contains("small").and().field("filterQuery").is("content.size:[0 TO 102400]");
        bucket.assertThat().field("label").contains("medium").and().field("filterQuery").is("content.size:[1048576 TO 16777216]");
        bucket.assertThat().field("label").contains("large").and().field("filterQuery").is("content.size:[102400 TO 1048576]");
        
    }
    
}
