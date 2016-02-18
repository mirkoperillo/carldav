/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.service.impl;

import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.service.ContentService;

import java.util.Set;

/**
 * Standard implementation of <code>ContentService</code>.
 *
 * @see ContentService
 * @see ContentDao
 */
public class StandardContentService implements ContentService {

    private final ContentDao contentDao;

    public StandardContentService(final ContentDao contentDao) {
        Assert.notNull(contentDao, "contentDao is null");
        this.contentDao = contentDao;
    }

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     */
    public HibItem findItemByPath(String path) {
        return contentDao.findItemByPath(path);
    }

    /**
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param hibItem item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection) {
        contentDao.removeItemFromCollection(hibItem, collection);
        contentDao.updateCollectionTimestamp(collection);
    }

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    public HibCollectionItem createCollection(HibCollectionItem parent,
                                           HibCollectionItem collection) {
        return contentDao.createCollection(parent, collection);
    }

    /**
     * Update collection item
     * 
     * @param collection
     *            collection item to update
     * @return updated collection
     */
    public HibCollectionItem updateCollection(HibCollectionItem collection) {
        return contentDao.updateCollection(collection);
    }

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(HibCollectionItem collection) {
        // prevent HomeCollection from being removed (should only be removed
        // when user is removed)
        if(collection instanceof HibHomeCollectionItem) {
            throw new IllegalArgumentException("cannot remove home collection");
        }
        contentDao.removeCollection(collection);
    }

    /**
     * Create new content item. A content item represents a piece of content or
     * file.
     * 
     * @param parent
     *            parent collection of content. If null, content is assumed to
     *            live in the top-level user collection
     * @param content
     *            content to create
     * @return newly created content
     */
    public HibItem createContent(HibCollectionItem parent, HibItem content) {
        content = contentDao.createContent(parent, content);
        contentDao.updateCollectionTimestamp(content.getCollection());
        return content;
    }

    /**
     * Create new content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param hibContentItems
     *            content items to create
     */
    public void createContentItems(HibCollectionItem parent, Set<HibItem> hibContentItems) {
        for(HibItem content : hibContentItems) {
            contentDao.createContent(parent, content);
        }

        contentDao.updateCollectionTimestamp(parent);
    }

    /**
     * Update content items.  This includes creating new items, removing
     * existing items, and updating existing items.  ContentItem deletion is
     * represented by setting ContentItem.isActive to false.  ContentItem deletion
     * removes item from system, not just from the parent collections.
     * ContentItem creation adds the item to the specified parent collections.
     * 
     * @param parents
     *            parents that new content items will be added to.
     * @param hibContentItems to update
     */
    public void updateContentItems(Set<HibCollectionItem> parents, Set<HibItem> hibContentItems) {
       for(HibItem content: hibContentItems) {
           if(content.getId()== null) {
               contentDao.createContent(parents, content);
           }
           else {
               contentDao.updateContent(content);
           }
           contentDao.updateCollectionTimestamp(content.getCollection());
       }
    }

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    public HibItem updateContent(HibItem content) {
        content = contentDao.updateContent(content);
        contentDao.updateCollectionTimestamp(content.getCollection());
        return content;
    }

    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem) {
        return contentDao.findCollectionItems(hibCollectionItem);
    }
}
