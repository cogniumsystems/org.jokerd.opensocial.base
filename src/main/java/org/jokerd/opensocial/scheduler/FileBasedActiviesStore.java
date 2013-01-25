package org.jokerd.opensocial.scheduler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.ActivityEntryUtil;
import org.jokerd.opensocial.cursors.ActivityListCursor;
import org.jokerd.opensocial.cursors.ActivitySectionsCursor;
import org.jokerd.opensocial.cursors.ActivitySectionsCursor.TimeGroupListener;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;
import org.ubimix.commons.cursor.ICursor;
import org.ubimix.commons.cursor.SequentialCursor;
import org.ubimix.commons.io.IOUtil;
import org.ubimix.commons.json.JsonArray;
import org.ubimix.commons.json.ext.DateFormatter;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class FileBasedActiviesStore extends ActivitiesCache {

    /**
     * @author kotelnikov
     */
    public static class SequentialActivityCursor
        extends
        SequentialCursor<ActivityEntry, StreamException>
        implements
        IActivityCursor {

        private FormattedDate fDate;

        private final File fDir;

        private FormattedDate fEndDate;

        private final int fHistorySizeInDays;

        private final FormattedDate fInitialDate;

        private FormattedDate fStartDate;

        public SequentialActivityCursor(
            File dir,
            FormattedDate initialDate,
            int historySizeInDays) {
            fDir = dir;
            fHistorySizeInDays = historySizeInDays;
            fInitialDate = initialDate;
        }

        /**
         * @return the maximal history length (in days) to show
         */
        protected int getHistorySizeInDays() {
            return fHistorySizeInDays;
        }

        protected FormattedDate getPrevDate(FormattedDate date, int deltaInDays) {
            long delta = -1 * DateFormatter.getDays(deltaInDays);
            FormattedDate nextDate = DateFormatter.getNextFormattedDate(
                date,
                delta);
            nextDate.setHour(0);
            nextDate.setMinutes(0);
            nextDate.setSeconds(0);
            return nextDate;
        }

        @Override
        protected IActivityCursor loadNextCursor(
            ICursor<ActivityEntry, StreamException> cursor)
            throws StreamException {
            if (fStartDate == null) {
                fStartDate = getPrevDate(fInitialDate, 0);
                int historyLengthInDays = getHistorySizeInDays();
                fEndDate = getPrevDate(fStartDate, historyLengthInDays);
                fDate = fStartDate;
            }
            // FIXME: use the full list of files in the directory instead...
            IActivityCursor result = null;
            while (result == null && fDate.compareTo(fEndDate) >= 0) {
                File file = FileBasedActiviesStore.getFile(fDir, fDate);
                try {
                    if (file.exists()) {
                        String str = IOUtil.readString(file);
                        JsonArray array = JsonArray.FACTORY.newValue(str);
                        List<ActivityEntry> list = array
                            .getList(ActivityEntry.FACTORY);
                        result = new ActivityListCursor(list);
                    }
                } catch (IOException e) {
                    throw new StreamException("Can not read a JSON file "
                        + file, e);
                }
                fDate = getPrevDate(fDate, 1);
            }
            return result;
        }
    }

    protected static File getFile(File dir, FormattedDate stamp) {
        String name = stamp.toString();
        name = name.substring(0, 10) + ".json";
        File file = new File(dir, name);
        return file;
    }

    private final File fDir;

    private final int fHistorySizeInDays;

    public FileBasedActiviesStore(File dir, int historySizeInDays) {
        fDir = dir;
        fHistorySizeInDays = historySizeInDays;
    }

    public IActivityCursor getActivities(
        FormattedDate initialDate,
        int historySizeInDays) {
        return new SequentialActivityCursor(
            fDir,
            initialDate,
            historySizeInDays);
    }

    @Override
    public IActivityCursor getAllActivities() {
        FormattedDate now = DateFormatter
            .formatDate(System.currentTimeMillis());
        int historySizeInDays = fHistorySizeInDays;
        return getActivities(now, historySizeInDays);
    }

    protected void setAllActivities(IActivityCursor cursor)
        throws StreamException {
        IActivityCursor wrapper = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.TimeGroupListener(
                TimeGroupListener.Level.DAY) {

                private boolean fFirst;

                private FileWriter fOut;

                @Override
                public void beginGroup(ActivityEntry entry) {
                    FormattedDate stamp = ActivityEntryUtil.getEntryDate(entry);
                    File file = getFile(fDir, stamp);
                    try {
                        fFirst = true;
                        file.getParentFile().mkdirs();
                        fOut = new FileWriter(file);
                        fOut.write("[\n");
                    } catch (IOException e) {
                        handleError("Can not open a new output stream", e);
                    }
                    writeEntry(entry);
                }

                @Override
                public void endGroup(ActivityEntry entry) {
                    try {
                        fOut.write("\n]");
                        fOut.close();
                        fOut = null;
                    } catch (Throwable t) {
                        handleError("Can not close the stream", t);
                    }
                }

                private void handleError(String msg, Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public boolean sameGroup(
                    ActivityEntry prev,
                    ActivityEntry current) {
                    boolean sameGroup = super.sameGroup(prev, current);
                    if (sameGroup) {
                        if (!prev.getId().equals(current.getId())) {
                            writeEntry(current);
                        }
                    }
                    return sameGroup;
                }

                private void writeEntry(ActivityEntry entry) {
                    try {
                        if (!fFirst) {
                            fOut.write(",");
                        }
                        fOut.write("\n");
                        fOut.write(entry.toString());
                        fFirst = false;
                    } catch (Throwable t) {
                        handleError("Can not close the stream", t);
                    }
                }
            });
        while (wrapper.loadNext()) {
            wrapper.getCurrent();
        }
        wrapper.close();
    }

    public void storeActivities(ObjectId sourceId, IActivityCursor cursor)
        throws StreamException {
        IActivityCursor fullCursor = getAllActivities();
        ActivitiesMergeCursor mergeCursor = new ActivitiesMergeCursor(
            fullCursor,
            cursor);
        setAllActivities(mergeCursor);
    }

}