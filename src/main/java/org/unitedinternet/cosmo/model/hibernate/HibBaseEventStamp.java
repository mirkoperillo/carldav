/*
 * Copyright 2007 Open Source Applications Foundation
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

import net.fortuna.ical4j.model.Calendar;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.validation.constraints.NotNull;

@Entity
@SecondaryTable(name="event_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")},
        indexes = {
                @Index(name = "idx_startdt",columnList = "startDate"),
                @Index(name = "idx_enddt",columnList = "endDate"),
                @Index(name = "idx_floating",columnList = "isFloating"),
                @Index(name = "idx_recurring",columnList = "isrecurring")}
)
public class HibBaseEventStamp extends HibAuditableObject {

    @Column(table="event_stamp", name = "icaldata", length=102400000, nullable = false)
    @Type(type="calendar_clob")
    @NotNull
    private Calendar eventCalendar = null;

    @Embedded
    private HibEventTimeRangeIndex timeRangeIndex = null;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(targetEntity=HibItem.class, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "itemid", nullable = false)
    private HibItem item;

    @Column(name = "startdate", length=16)
    private String startDate;

    @Column(name = "enddate", length=16)
    private String endDate;

    @Column(name = "isfloating")
    private Boolean isFloating;

    @Column(name = "isrecurring")
    private Boolean isRecurring;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public HibItem getItem() {
        return item;
    }

    public void setItem(HibItem hibItem) {
        this.item = hibItem;
    }

    public HibEventTimeRangeIndex getTimeRangeIndex() {
        return timeRangeIndex;
    }

    public void setTimeRangeIndex(final HibEventTimeRangeIndex timeRangeIndex) {
        this.timeRangeIndex = timeRangeIndex;
    }

    public HibBaseEventStamp() {}

    public HibBaseEventStamp(HibItem hibItem) {
        setItem(hibItem);
    }

    public Calendar getEventCalendar() {
        return eventCalendar;
    }

    public void setEventCalendar(Calendar calendar) {
        this.eventCalendar = calendar;
    }

    public boolean isRecurring() {
        return getTimeRangeIndex().getIsRecurring();
    }



    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(final Boolean isFloating) {
        this.isFloating = isFloating;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(final Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
