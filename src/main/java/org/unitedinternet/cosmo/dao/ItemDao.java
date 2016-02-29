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
package org.unitedinternet.cosmo.dao;

import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.Set;

/**
 * Interface for DAO that provides base functionality for items stored in the
 * server.
 *
 */
public interface ItemDao {

    /**
     * Find an item with the specified path. The return type will be one of
     * ContentItem, NoteItem, CollectionItem.
     *
     * @param path
     *            path of item to find
     * @return item represented by path
     */
    HibItem findItemByPath(String path);

    /**
     * Get the root item for a user
     *
     * @param user The user for get the root item.
     * @return home collection item.
     */
    HibHomeCollectionItem getRootItem(User user);

    /**
     * Create the root item for a user.
     * @param user The user for create the root item.
     * @return Home collection item.
     */
    HibHomeCollectionItem createRootItem(User user);

    /**
     * Remove an item.
     *
     * @param hibItem
     *            item to remove
     */
    void removeItem(HibItem hibItem);

    /**
     * Remove item from a collection.
     *
     * @param hibItem the item
     * @param collection the collection to remove from
     */
    void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection);

    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    Set<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem);

    HibItem save(HibItem item);

    Set<HibItem> findCollectionFileItems(HibCollectionItem hibCollectionItem);
}
