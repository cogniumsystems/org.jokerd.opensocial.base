/**
 * 
 */
package org.jokerd.opensocial.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.ActivityListCursor;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;
import org.jokerd.opensocial.testutils.RandomActivityBuilder;
import org.ubimix.commons.io.IOUtil;

/**
 * @author kotelnikov
 */
public class AcitivitiesCacheTest extends TestCase {

    public AcitivitiesCacheTest(String name) {
        super(name);
    }

    private void compare(IActivityCursor first, IActivityCursor second)
        throws StreamException {
        try {
            try {
                while (first.loadNext()) {
                    assertTrue(second.loadNext());
                    ActivityEntry a = first.getCurrent();
                    ActivityEntry b = second.getCurrent();
                    assertEquals(a, b);
                }
                assertFalse(second.loadNext());
            } finally {
                first.close();
            }
        } finally {
            second.close();
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void test() throws Exception {
        test(10, 1);
        test(10, 2);
        test(10, 100);
        test(30, 100);
        test(30, 100, 100, 100);
        test(300, 100, 100, 100);
        test(120, 100, 300, 100, 200, 300);
        test(120, 100, 300, 100, 200, 300, 500, 500, 500);
    }

    private void test(int historySize, int... chunks) throws StreamException {
        RandomActivityBuilder builder = new RandomActivityBuilder(historySize);

        File dir = new File("./tmp");
        IOUtil.delete(dir);
        FileBasedActiviesStore store = new FileBasedActiviesStore(
            dir,
            historySize);
        ObjectId sourceId = builder.newObjecId();

        List<List<ActivityEntry>> array = new ArrayList<List<ActivityEntry>>();
        for (int chunk : chunks) {
            List<ActivityEntry> entries = builder.newActivityList(chunk);
            IActivityCursor cursor = new ActivityListCursor(entries);
            store.storeActivities(sourceId, cursor);
            array.add(entries);
        }

        IActivityCursor[] cursors = new IActivityCursor[array.size()];
        for (int i = 0; i < array.size(); i++) {
            cursors[i] = new ActivityListCursor(array.get(i));
        }
        IActivityCursor cursor = new ActivitiesMergeCursor(cursors);
        IActivityCursor test = store.getAllActivities();
        compare(cursor, test);

    }
}
