package org.jokerd.opensocial.testutils;

import org.jokerd.opensocial.api.model.ActivityObject;
import org.ubimix.commons.digests.Sha1Digest;
import org.ubimix.commons.json.JsonValue.IJsonValueFactory;
import org.ubimix.commons.json.ext.DateFormatter;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class OpenSocialTestUtils extends TestUtils {

    public OpenSocialTestUtils() {
        super();
    }

    protected String generateId(String token) {
        String str = token != null ? token : "";
        Sha1Digest digest = Sha1Digest.builder().update(str).build();
        return "uri:sha1:" + digest.toString();
    }

    protected FormattedDate generateRandomTime(long from, long to) {
        if (to < from) {
            throw new IllegalArgumentException();
        }
        long range = (to - from);
        if (range > 0) {
            range = Math.abs(getRandom().nextInt()) % range;
        } else {
            range = 0;
        }
        long time = from + range;
        FormattedDate result = DateFormatter.formatDate(time);
        return result;
    }

    /**
     * @return the period of time (in days) used to generate random timestamps
     */
    protected int getMaxRandomTimeDelayInDays() {
        return 10; // in days
    }

    protected String getRandomUrl(String base) {
        String str = base
            + (getRandomString(3) + "/" + getRandomString(5, 15)).trim();
        str = str.replaceAll("[\\s+]", "+");
        return str;
    }

    protected ActivityObject newActivityObject() {
        ActivityObject obj = new ActivityObject();
        String title = getLoremIpsum(15, 100);
        int maxRandomTimeDelay = getMaxRandomTimeDelayInDays();

        long stamp = now();
        FormattedDate publishTime = generateRandomTime(
            stamp - DateFormatter.getDays(maxRandomTimeDelay),
            stamp);
        FormattedDate updateTime = generateRandomTime(
            DateFormatter.getTime(publishTime),
            stamp);
        String entryId = generateId(updateTime + title);
        obj.setId(entryId);
        obj.setDisplayName(getLoremIpsum(5, 50));
        obj.setPublished(publishTime);
        obj.setUpdated(updateTime);
        obj.setUrl(getRandomUrl("http://www.foo.bar/"));
        obj.setContent(getLoremIpsum(10, 250));
        return obj;
    }

    protected <T extends ActivityObject> T newActivityObject(
        IJsonValueFactory<T> factory,
        String type) {
        ActivityObject obj = newActivityObject();
        obj.setObjectType(type);
        T result = factory.newValue(obj);
        return result;
    }

    protected long now() {
        long result = System.currentTimeMillis();
        return result;
    }

}