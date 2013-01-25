/**
 * 
 */
package org.jokerd.opensocial.cursors;

import junit.framework.TestCase;

import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.ObjectId;

/**
 * @author kotelnikov
 */
public class CompositeCursorProviderTest extends TestCase {

    /**
     * @param name
     */
    public CompositeCursorProviderTest(String name) {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void test() throws Exception {
        CompositeActivityCursorProvider composite = new CompositeActivityCursorProvider();
        final IActivityCursor fhCursor = new ActivityListCursor();
        final IActivityCursor wtCursor = new ActivityListCursor();
        composite.setCursorProvider(
            new DomainName("facehook.com"),
            new IActivityCursorProvider() {
                @Override
                public IActivityCursor getCursor(ObjectId streamId) {
                    return fhCursor;
                }
            });
        composite.setCursorProvider(
            new DomainName("weeter.com"),
            new IActivityCursorProvider() {
                @Override
                public IActivityCursor getCursor(ObjectId streamId) {
                    return wtCursor;
                }
            });
        IActivityCursor cursor = composite.getCursor(new ObjectId(
            "facehook.com",
            "john.doe"));
        assertSame(fhCursor, cursor);

        cursor = composite.getCursor(new ObjectId("weeter.com", "boo.foo"));
        assertSame(wtCursor, cursor);

    }
}
