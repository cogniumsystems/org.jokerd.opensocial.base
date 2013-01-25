package org.jokerd.opensocial.scheduler;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.ActivityObject;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.ActivitySectionsCursor;
import org.jokerd.opensocial.cursors.ActivitySectionsCursor.TimeGroupListener;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class ActivityFormatterCursor extends DelegateActivityCursor {

    public ActivityFormatterCursor(IActivityCursor cursor) {
        cursor = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.GroupListener<ActivityEntry, StreamException>() {

                @Override
                public void beginGroup(ActivityEntry entry) {
                    ObjectId id = getStreamId(entry.getTarget());
                    System.out.println();
                    System.out.println();
                    System.out
                        .println("=======================================================");
                    System.out.println(id.getLocalIdDecoded());
                    System.out
                        .println("=======================================================");
                }

                private ObjectId getStreamId(ActivityObject object) {
                    ObjectId result = null;
                    if (object != null) {
                        result = object.getId();
                    }
                    if (result == null) {
                        result = new ObjectId("");
                    }
                    return result;
                }

                @Override
                public boolean sameGroup(
                    ActivityEntry prev,
                    ActivityEntry current) throws StreamException {
                    if (prev == null || current == null) {
                        return false;
                    }
                    ObjectId first = getStreamId(prev.getTarget());
                    ObjectId second = getStreamId(current.getTarget());
                    return first.equals(second);
                }
            });
        cursor = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.TimeGroupListener(
                TimeGroupListener.Level.DAY) {
                @Override
                public void beginGroup(ActivityEntry entry) {
                    FormattedDate date = getDate(entry);
                    System.out.println("============="
                        + date.getDay()
                        + "/"
                        + date.getMonth()
                        + "/"
                        + date.getYear()
                        + "=============");
                }
            });
        cursor = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.TimeGroupListener(
                TimeGroupListener.Level.HOUR) {
                @Override
                public void beginGroup(ActivityEntry entry) {
                    FormattedDate date = getDate(entry);
                    System.out.println("-------------"
                        + (date.getHour() + 1)
                        + "h00"
                        + "-------------");
                }
            });
        cursor = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.TimeGroupListener(
                TimeGroupListener.Level.HOUR) {

                @Override
                public void beginGroup(ActivityEntry entry) {
                    String target = getTarget(entry);
                    System.out.println("* " + target);
                }

                private String getTarget(ActivityEntry activityEntry) {
                    ActivityObject target = activityEntry.getTarget();
                    String str = target != null ? target.getDisplayName() : "";
                    return str;
                }

                @Override
                public boolean sameGroup(
                    ActivityEntry prev,
                    ActivityEntry current) {
                    if (!super.sameGroup(prev, current)) {
                        return false;
                    }
                    String first = getTarget(prev);
                    String second = getTarget(current);
                    return first.equals(second);
                }

            });
        setCursor(cursor);
    }

    @Override
    public boolean loadNext() throws StreamException {
        boolean result = super.loadNext();
        if (result) {
            ActivityEntry activityEntry = getCurrent();
            String title = activityEntry.getTitle();
            FormattedDate date = ActivitySectionsCursor.TimeGroupListener
                .getDate(activityEntry);
            String by = activityEntry.getActor().getDisplayName();
            String shift = " ";
            System.out.println(shift
                + "["
                + date.getHour()
                + "h"
                + date.getMinutes()
                + "] "
                + title);
            System.out.println(shift + "Published by '" + by + "'.");
            String content = activityEntry.getObject().getContent();
            System.out.println(shift + content);
            System.out.println();
        }
        return result;
    }

}