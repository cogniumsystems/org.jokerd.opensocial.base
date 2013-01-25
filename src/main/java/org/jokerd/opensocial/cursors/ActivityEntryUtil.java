package org.jokerd.opensocial.cursors;

import java.util.Comparator;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.ubimix.commons.json.ext.FormattedDate;

public class ActivityEntryUtil {

    public static Comparator<ActivityEntry> ENTRY_COMPARATOR = new Comparator<ActivityEntry>() {
        @Override
        public int compare(ActivityEntry o1, ActivityEntry o2) {
            FormattedDate first = getEntryDate(o1);
            FormattedDate second = getEntryDate(o2);
            int result = first.compareTo(second);
            return -result; // inverted order; more recent entries go first
        }
    };

    public static FormattedDate getEntryDate(ActivityEntry entry) {
        FormattedDate date = entry.getUpdated();
        if (date == null) {
            date = entry.getPublished();
        }
        if (date == null) {
            date = new FormattedDate("1900-01-01T00:00:00Z");
        }
        return date;
    }

}