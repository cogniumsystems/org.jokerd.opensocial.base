package org.jokerd.opensocial.cursors;

import java.util.List;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.ubimix.commons.cursor.MergeCursor;

/**
 * @author kotelnikov
 */
public class ActivityMergeCursor
    extends
    MergeCursor<ActivityEntry, StreamException> implements IActivityCursor {

    public ActivityMergeCursor(IActivityCursor... cursors)
        throws StreamException {
        super(ActivityEntryUtil.ENTRY_COMPARATOR, cursors);
    }

    public ActivityMergeCursor(List<IActivityCursor> cursors) {
        super(ActivityEntryUtil.ENTRY_COMPARATOR, cursors);
    }
}