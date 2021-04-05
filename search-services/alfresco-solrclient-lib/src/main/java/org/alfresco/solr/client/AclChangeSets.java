/*
 * #%L
 * Alfresco Search Services
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.solr.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Andy
 *
 */
public class AclChangeSets
{
    private final List<AclChangeSet> aclChangeSets;
    
    private Long maxChangeSetCommitTime;
    
    private Long maxChangeSetId;
    
    AclChangeSets(List<AclChangeSet> aclChangeSets, Long maxChangeSetCommitTime, Long maxChangeSetId)
    {
        this.aclChangeSets = (aclChangeSets == null ? null : new ArrayList<>(aclChangeSets));
        this.maxChangeSetCommitTime = maxChangeSetCommitTime;
        this.maxChangeSetId = maxChangeSetId;
    }

    public AclChangeSets(List<AclChangeSet> aclChangeSets)
    {
        this.aclChangeSets = aclChangeSets;
        if (!aclChangeSets.isEmpty())
        {
            this.maxChangeSetCommitTime = aclChangeSets.stream()
                    .max(Comparator.comparing(AclChangeSet::getCommitTimeMs)).get().getCommitTimeMs();
            this.maxChangeSetId = aclChangeSets.stream()
                    .max(Comparator.comparing(AclChangeSet::getId)).get().getId();
        }
    }

    public List<AclChangeSet> getAclChangeSets()
    {
        return Collections.unmodifiableList(aclChangeSets);
    }

    public Long getMaxChangeSetCommitTime()
    {
        return maxChangeSetCommitTime;
    }
    
    public Long getMaxChangeSetId()
    {
        return maxChangeSetId;
    }
    
}
