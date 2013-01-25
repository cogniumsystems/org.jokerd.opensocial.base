package org.jokerd.opensocial.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.IActivityCursorProvider;
import org.jokerd.opensocial.scheduler.TaskManager.ITaskExecutor;
import org.jokerd.opensocial.scheduler.TaskManager.ITaskScheduler;
import org.jokerd.opensocial.scheduler.TaskManager.InMemoryTaskQueue;
import org.ubimix.commons.io.IOUtil;
import org.ubimix.commons.json.JsonArray;
import org.ubimix.commons.json.JsonObject;
import org.ubimix.commons.json.ext.DateFormatter;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class DynamicActivityCursorProvider {

    private static class DownloadTask extends JsonObject {

        public static IJsonValueFactory<DownloadTask> FACTORY = new IJsonValueFactory<DownloadTask>() {
            @Override
            public DownloadTask newValue(Object object) {
                DownloadTask task = new DownloadTask();
                task.setJsonObject(object);
                return task;
            }
        };

        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                return true;
            }
            if (!(obj instanceof DownloadTask)) {
                return false;
            }
            DownloadTask o = (DownloadTask) obj;
            ObjectId first = getId();
            ObjectId second = o.getId();
            if (first == null || second == null) {
                return first == second;
            }
            return first.equals(second);
        }

        public boolean generateNextUpdateTime() {
            FormattedDate date = getFormattedDate();
            if (date == null) {
                date = setNextUpdateTime(now());
            } else {
                int timeout = getTimeout();
                long time = now() + timeout;
                setNextUpdateTime(time);
            }
            return true;
        }

        public FormattedDate getFormattedDate() {
            return getValue("time", FormattedDate.FACTORY);
        }

        public ObjectId getId() {
            JsonObject obj = getValue("id", JsonObject.FACTORY);
            DomainName domainName = obj.getValue("domain", DomainName.FACTORY);
            String localName = obj.getString("localId");
            return new ObjectId(domainName, localName);
        }

        public Date getTime() {
            FormattedDate date = getFormattedDate();
            Date result = null;
            if (date != null) {
                result = DateFormatter.getDateTime(date);
            }
            return result;
        }

        public int getTimeout() {
            return getInteger("timeout", 100);
        }

        @Override
        public int hashCode() {
            ObjectId id = getId();
            return id != null ? id.hashCode() : 0;
        }

        private long now() {
            return System.currentTimeMillis();
        }

        public void setFormattedTime(FormattedDate date) {
            setValue("time", date);
        }

        public void setId(ObjectId objectId) {
            JsonObject obj = new JsonObject();
            obj.setValue("domain", objectId.getDomainName());
            obj.setValue("localId", objectId.getLocalIdDecoded());
            setValue("id", obj);
        }

        private FormattedDate setNextUpdateTime(long time) {
            FormattedDate date = DateFormatter.formatDate(time);
            setFormattedTime(date);
            return date;
        }

        public void setTimeout(int time) {
            setValue("timeout", time);
        }

    }

    /**
     * @author kotelnikov
     */
    private static class DownloadTaskQueue
        extends
        InMemoryTaskQueue<DownloadTask> {

        private final File fFile;

        public DownloadTaskQueue(
            ITaskScheduler<DownloadTask> scheduler,
            File file) {
            super(scheduler);
            this.fFile = file;
        }

        public DownloadTask getTaskById(ObjectId id) {
            DownloadTask result = null;
            List<DownloadTask> tasks = getTasks();
            for (DownloadTask task : tasks) {
                DownloadTask t = task;
                if (id.equals(t.getId())) {
                    result = t;
                    break;
                }
            }
            return result;
        }

        @Override
        protected Set<DownloadTask> loadAllTasks() {
            Set<DownloadTask> result = null;
            try {
                if (fFile.exists()) {
                    String str = IOUtil.readString(fFile);
                    JsonArray jsonArray = JsonArray.newValue(str);
                    result = jsonArray.getSet(DownloadTask.FACTORY);
                }
            } catch (Throwable t) {
                handleError("Can not read a JSON file '" + fFile + "'.", t);
            }
            if (result == null) {
                result = new HashSet<DownloadTask>();
            }
            return result;
        }

        @Override
        protected void saveAllTasks(Set<DownloadTask> set) {
            try {
                JsonArray array = new JsonArray();
                List<DownloadTask> list = new ArrayList<DownloadTask>(set);
                Collections.sort(list, new Comparator<DownloadTask>() {
                    @Override
                    public int compare(DownloadTask o1, DownloadTask o2) {
                        String str1 = "" + o1.getId();
                        String str2 = "" + o2.getId();
                        return str1.compareTo(str2);
                    }
                });
                array.setValues(list);
                String str = array.toString();
                fFile.getParentFile().mkdirs();
                IOUtil.writeString(fFile, str);
            } catch (Throwable t) {
                handleError("Can not write a JSON file '" + fFile + "'.", t);
            }
        }
    }

    private static Logger log = Logger
        .getLogger(DynamicActivityCursorProvider.class.getName());

    protected static void handleError(String msg, Throwable t) {
        log.log(Level.WARNING, msg, t);
    }

    private final IAcitivitiesCache fCache;

    private final IActivityCursorProvider fProvider;

    private final ITaskExecutor<DownloadTask> fTaskExecutor = new ITaskExecutor<DownloadTask>() {
        @Override
        public void execute(DownloadTask task, ITaskExecutor.ICallback callback) {
            try {
                DownloadTask downloadTask = task;
                ObjectId streamId = downloadTask.getId();
                try {
                    IActivityCursor cursor = fProvider.getCursor(streamId);
                    try {
                        fCache.storeActivities(streamId, cursor);
                    } finally {
                        cursor.close();
                    }
                } catch (Throwable t) {
                    handleError(
                        "Can not load and format a stream for this URL: "
                            + streamId,
                        t);
                }
            } finally {
                callback.finish(true);
            }
        }
    };

    private final TaskManager<DownloadTask> fTaskManager;

    private final DownloadTaskQueue fTaskQueue;

    private final ITaskScheduler<DownloadTask> fTaskScheduler = new ITaskScheduler<DownloadTask>() {

        @Override
        public Date getTime(DownloadTask task) {
            return task.getTime();
        }

        @Override
        public void updateTime(DownloadTask task) {
            task.generateNextUpdateTime();
        }
    };

    public DynamicActivityCursorProvider(
        final IActivityCursorProvider provider,
        File dir) {
        fProvider = provider;
        fCache = new FileBasedActiviesStore(dir, 10);
        final File file = new File(dir, "feeds.json");
        fTaskQueue = new DownloadTaskQueue(fTaskScheduler, file);
        fTaskManager = new TaskManager<DownloadTask>(
            fTaskScheduler,
            fTaskQueue,
            fTaskExecutor);
    }

    public void close() {
        fTaskManager.close();
    }

    public void open() {
        fTaskManager.open();
    }

    public void schedule(ObjectId id, int timeout) {
        DownloadTask task = fTaskQueue.getTaskById(id);
        if (task == null) {
            task = new DownloadTask();
            task.setId(id);
            task.setTimeout(timeout);
        }
        fTaskManager.schedule(task);
    }
}