/**
 * 
 */
package org.jokerd.opensocial.cursors;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.ubimix.commons.cursor.AbstractRangeCursor;
import org.ubimix.commons.cursor.ICursor;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class ActivityRangeCursor
    extends
    AbstractRangeCursor<ActivityEntry, StreamException>
    implements
    IActivityCursor {

    private final FormattedDate fMaxDate;

    private final int fMaxNumber;

    public ActivityRangeCursor(
        ICursor<ActivityEntry, StreamException> cursor,
        int maxNumber,
        FormattedDate maxDate) {
        super(cursor);
        fMaxDate = maxDate;
        fMaxNumber = maxNumber;
    }

    @Override
    protected boolean isAfter(
        int pos,
        ICursor<ActivityEntry, StreamException> cursor) throws StreamException {
        if (pos >= fMaxNumber) {
            return true;
        }
        ActivityEntry entry = cursor.getCurrent();
        FormattedDate updated = entry.getUpdated();
        return (updated != null && updated.compareTo(fMaxDate) < 0);
    }

    @Override
    protected boolean isBefore(
        int pos,
        ICursor<ActivityEntry, StreamException> cursor) throws StreamException {
        return false;
    }

}
