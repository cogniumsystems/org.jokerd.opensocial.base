package org.jokerd.opensocial.scheduler;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.cursors.ActivityEntryUtil;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;
import org.ubimix.commons.cursor.MergeCursor;

public class ActivitiesMergeCursor
    extends
    MergeCursor<ActivityEntry, StreamException> implements IActivityCursor {
    public ActivitiesMergeCursor(IActivityCursor... cursors) {
        super(ActivityEntryUtil.ENTRY_COMPARATOR, cursors);
    }
}