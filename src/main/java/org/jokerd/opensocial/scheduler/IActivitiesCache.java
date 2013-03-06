package org.jokerd.opensocial.scheduler;

import java.util.Set;

import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;

public interface IActivitiesCache {

    IActivityCursor getActivities(Set<ObjectId> sourceIds)
        throws StreamException;

    void storeActivities(ObjectId sourceId, IActivityCursor cursor)
        throws StreamException;

}