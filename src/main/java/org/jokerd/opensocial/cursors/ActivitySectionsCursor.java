package org.jokerd.opensocial.cursors;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.ubimix.commons.cursor.GroupCursor;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class ActivitySectionsCursor
    extends
    GroupCursor<ActivityEntry, StreamException> implements IActivityCursor {

    public static class TimeGroupListener
        extends
        GroupListener<ActivityEntry, StreamException> {

        public enum Level {
            DAY, HOUR, MIN, MONTH, SEC, YEAR
        }

        public static FormattedDate getDate(ActivityEntry entry) {
            FormattedDate date = entry.getUpdated();
            if (date == null) {
                date = entry.getPublished();
            }
            return date;
        }

        private final Level fLevel;

        public TimeGroupListener(Level level) {
            fLevel = level;
        }

        @Override
        public void beginGroup(ActivityEntry entry) {
        }

        @Override
        public void endGroup(ActivityEntry entry) {
        }

        @Override
        public boolean sameGroup(ActivityEntry prev, ActivityEntry current) {
            FormattedDate prevDate = getDate(prev);
            FormattedDate currentDate = getDate(current);
            return sameGroup(prevDate, currentDate);
        }

        protected boolean sameGroup(FormattedDate prev, FormattedDate current) {
            if (prev.getYear() != current.getYear()) {
                return false;
            }
            if (fLevel == Level.YEAR) {
                return true;
            }
            if (prev.getMonth() != current.getMonth()) {
                return false;
            }
            if (fLevel == Level.MONTH) {
                return true;
            }

            if (prev.getDay() != current.getDay()) {
                return false;
            }
            if (fLevel == Level.DAY) {
                return true;
            }

            if (prev.getHour() != current.getHour()) {
                return false;
            }
            if (fLevel == Level.HOUR) {
                return true;
            }

            if (prev.getMinutes() != current.getMinutes()) {
                return false;
            }
            if (fLevel == Level.MIN) {
                return true;
            }

            if (prev.getSeconds() != current.getSeconds()) {
                return false;
            }
            if (fLevel == Level.SEC) {
                return true;
            }

            return true;
        }
    }

    public ActivitySectionsCursor(
        IActivityCursor cursor,
        IGroupListener<ActivityEntry, StreamException> groupListener) {
        super(cursor, groupListener);
    }

}
