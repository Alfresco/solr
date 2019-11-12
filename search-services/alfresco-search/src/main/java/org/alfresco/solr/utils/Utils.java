/*
 * Copyright (C) 2005-2019 Alfresco Software Limited.
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
package org.alfresco.solr.utils;

import java.util.Collection;
import java.util.Collections;

public abstract class Utils
{
    /**
     * Returns the same input collection if that is not null, otherwise a new empty collection.
     * Provides a safe way for iterating over a returned collection (which could be null).
     *
     * @param values the collection.
     * @param <T> the collection type.
     * @return the same input collection if that is not null, otherwise a new empty collection.
     */
    public static <T> Collection<T> notNullOrEmpty(Collection<T> values)
    {
        return values != null ? values : Collections.emptyList();
    }

    /**
     * Converts the given input in an Integer, otherwise it returns null.
     *
     * @param value the numeric string.
     * @return the corresponding Integer or null in case the input is NaN.
     */
    public static Integer toIntOrNull(String value)
    {
        try
        {
            return Integer.valueOf(value);
        }
        catch(NumberFormatException nfe)
        {
            return null;
        }
    }
}