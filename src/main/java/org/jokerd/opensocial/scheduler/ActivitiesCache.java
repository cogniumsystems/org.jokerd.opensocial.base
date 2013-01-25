package org.jokerd.opensocial.scheduler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;
import org.ubimix.commons.cursor.FilteringCursor;

/**
 * @author kotelnikov
 */
public abstract class ActivitiesCache implements IAcitivitiesCache {

    /**
     * @author kotelnikov
     */
    public static class FilteringActiviesCursor
        extends
        FilteringCursor<ActivityEntry, StreamException>
        implements
        IActivityCursor {

        private final Set<ObjectId> fObjectIds;

        public FilteringActiviesCursor(IActivityCursor cursor, ObjectId... ids) {
            super(cursor);
            fObjectIds = new HashSet<ObjectId>(Arrays.asList(ids));
        }

        public FilteringActiviesCursor(IActivityCursor cursor, Set<ObjectId> set) {
            super(cursor);
            fObjectIds = set;
        }

        @Override
        protected boolean accept(ActivityEntry current) throws StreamException {
            ObjectId sourceId = current.getActor().getId();
            return fObjectIds.contains(sourceId);
        }
    }

    public IActivityCursor getActivities(Set<ObjectId> sourceIds) {
        IActivityCursor cursor = getAllActivities();
        ActivitiesCache.FilteringActiviesCursor filteringCursor = new FilteringActiviesCursor(
            cursor,
            sourceIds);
        return filteringCursor;
    }

    protected abstract IActivityCursor getAllActivities();

}