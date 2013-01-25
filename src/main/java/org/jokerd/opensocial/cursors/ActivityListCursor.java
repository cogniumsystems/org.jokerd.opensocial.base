/**
 * 
 */
package org.jokerd.opensocial.cursors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.ubimix.commons.cursor.IteratorBasedCursor;

/**
 * @author kotelnikov
 */
public class ActivityListCursor
    extends
    IteratorBasedCursor<ActivityEntry, StreamException>
    implements
    IActivityCursor {

    protected ActivityListCursor() {
        super();
    }

    public ActivityListCursor(List<? extends ActivityEntry> list) {
        super();
        setList(list);
    }

    protected void setList(
        Comparator<ActivityEntry> comparator,
        List<? extends ActivityEntry> list) {
        Collections.sort(list, comparator);
        setIterator(list.iterator());
    }

    protected void setList(List<? extends ActivityEntry> list) {
        setList(ActivityEntryUtil.ENTRY_COMPARATOR, list);
    }

}
