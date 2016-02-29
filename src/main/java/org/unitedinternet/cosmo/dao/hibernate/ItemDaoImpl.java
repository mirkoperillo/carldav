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
package org.unitedinternet.cosmo.dao.hibernate;

import org.hibernate.Query;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ItemDaoImpl extends AbstractDaoImpl implements ItemDao {

    private final ItemPathTranslator itemPathTranslator;
    private final ItemFilterProcessor itemFilterProcessor;

    public ItemDaoImpl(final ItemPathTranslator itemPathTranslator, ItemFilterProcessor itemFilterProcessor) {
        Assert.notNull(itemPathTranslator, "itemPathTranslator is null");
        Assert.notNull(itemFilterProcessor, "itemFilterProcessor is null");
        this.itemPathTranslator = itemPathTranslator;
        this.itemFilterProcessor = itemFilterProcessor;

    }

    public HibItem findItemByPath(String path) {
        return itemPathTranslator.findItemByPath(path);
    }

    public void removeItem(HibItem hibItem) {
        if (hibItem == null) {
            throw new IllegalArgumentException("item cannot be null");
        }

        getSession().refresh(hibItem);
        hibItem.setCollection(null);
        getSession().delete(hibItem);
        getSession().flush();
    }

    public void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection) {
        removeItemFromCollectionInternal(hibItem, collection);
        getSession().flush();
    }

    /**
     * find the set of collection items as children of the given collection item.
     *
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem){
        HashSet<HibCollectionItem> children = new HashSet<HibCollectionItem>();
        Query hibQuery = getSession().getNamedQuery("collections.children.by.parent")
                .setParameter("parent", hibCollectionItem);

        List<?> results = hibQuery.list();
        for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
            HibCollectionItem content = (HibCollectionItem) it.next();
            children.add(content);
        }
        return children;
    }

    public Set<HibItem> findCollectionFileItems(HibCollectionItem hibCollectionItem){
        HashSet<HibItem> children = new HashSet<HibItem>();
        Query hibQuery = getSession().getNamedQuery("collections.files.by.parent")
                .setParameter("parent", hibCollectionItem);

        List<?> results = hibQuery.list();
        for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
            HibItem content = (HibItem) it.next();
            children.add(content);
        }
        return children;
    }

    protected void removeItemFromCollectionInternal(HibItem hibItem, HibCollectionItem collection) {

        getSession().update(collection);
        getSession().update(hibItem);

        // do nothing if item doesn't belong to collection
        if (hibItem.getCollection().getId() != collection.getId()) {
            return;
        }

        hibItem.setCollection(null);
        getSession().delete(hibItem);
        getSession().refresh(collection);
    }

    @Override
    public HibItem save(HibItem item) {
        getSession().saveOrUpdate(item);
        getSession().flush();
        return item;
    }

    public Set<HibICalendarItem> findCalendarItems(ItemFilter itemFilter) {
        Set results = itemFilterProcessor.processFilter(itemFilter);
        return (Set<HibICalendarItem>) results;
    }
}
