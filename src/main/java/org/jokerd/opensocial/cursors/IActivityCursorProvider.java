/**
 * 
 */
package org.jokerd.opensocial.cursors;

import org.jokerd.opensocial.api.model.ObjectId;
import org.ubimix.commons.cursor.ICursorProvider;

/**
 * @author kotelnikov
 */
public interface IActivityCursorProvider
    extends
    ICursorProvider<ObjectId, StreamException, IActivityCursor> {

}
