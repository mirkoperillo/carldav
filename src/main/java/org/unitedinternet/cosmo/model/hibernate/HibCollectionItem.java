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
package org.unitedinternet.cosmo.model.hibernate;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("collection")
public class HibCollectionItem extends HibItem {

    private Set<HibItem> items = new HashSet<>();

    @OneToMany(targetEntity=HibItem.class, mappedBy="collection", fetch=FetchType.LAZY, orphanRemoval=true)
    public Set<HibItem> getItems() {
        return items;
    }

    public void setItems(Set<HibItem> items) {
        this.items = items;
    }

    //TODO remove @Transient as soon as HibCollectionItem does not derive from HibItem
    @Transient
    public User getOwner() {
        return super.getOwner();
    }

    public void setOwner(User owner) {
        super.setOwner(owner);
    }

    //TODO remove @Transient as soon as HibCollectionItem does not derive from HibItem
    @Transient
    public HibCollectionItem getParent() {
        return getCollection();
    }

    public void setParent(final HibCollectionItem item) {
        setCollection(item);
    }
}
