package org.jokerd.opensocial.testutils;

import java.util.ArrayList;
import java.util.List;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.ActivityObject;
import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.api.model.Person;
import org.jokerd.opensocial.cursors.ActivityListCursor;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.ubimix.commons.json.ext.DateFormatter;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class RandomActivityBuilder extends OpenSocialTestUtils {

    private ArrayList<Person> fAuthors;

    private final int fHistorySizeInDays;

    public RandomActivityBuilder(int historySizeInDays) {
        fHistorySizeInDays = historySizeInDays;
    }

    private Person getAuthorObject(ActivityEntry result) {
        if (fAuthors == null) {
            fAuthors = new ArrayList<Person>();
            int authorsNumber = getAuthorsNumber();
            for (int i = 0; i < authorsNumber; i++) {
                Person obj = newActivityObject(Person.FACTORY, "person");
                fAuthors.add(obj);
            }
        }
        int pos = getRandom().nextInt(fAuthors.size());
        return fAuthors.get(pos);
    }

    protected int getAuthorsNumber() {
        return 10;
    }

    private FormattedDate getLatestTime(ActivityObject obj) {
        FormattedDate updated = obj.getUpdated();
        FormattedDate published = obj.getPublished();
        return getLatestTime(updated, published);
    }

    private FormattedDate getLatestTime(
        FormattedDate first,
        FormattedDate second) {
        return first.compareTo(second) > 0 ? first : second;
    }

    @Override
    protected int getMaxRandomTimeDelayInDays() {
        return fHistorySizeInDays;
    }

    @Override
    protected boolean isFixedRandom() {
        return true;
    }

    public IActivityCursor newActivityCursor(final int count) {
        List<ActivityEntry> list = newActivityList(count);
        return new ActivityListCursor(list);
    }

    public List<ActivityEntry> newActivityList(final int count) {
        List<ActivityEntry> list = new ArrayList<ActivityEntry>();
        for (int i = 0; i < count; i++) {
            ActivityEntry entry = nextEntry();
            list.add(entry);
        }
        return list;
    }

    public DomainName newDomainName() {
        String str = getRandomString(5, 15);
        return new DomainName(str);
    }

    public ObjectId newObjecId() {
        return newObjectId(newDomainName());
    }

    public ObjectId newObjectId(DomainName domainName) {
        String str = generateId(now() + "");
        return new ObjectId(domainName, str);
    }

    public ActivityEntry nextEntry() {
        ActivityEntry result = new ActivityEntry();

        Person author = getAuthorObject(result);
        ActivityObject activityObject = newActivityObject();
        ActivityObject targetObject = newActivityObject();
        FormattedDate maxDate = getLatestTime(
            getLatestTime(activityObject),
            getLatestTime(targetObject));

        FormattedDate updateTime = generateRandomTime(
            DateFormatter.getTime(maxDate),
            now());
        String title = getLoremIpsum(15, 100);

        String entryId = generateId(updateTime + title);
        result.setId(entryId);

        result.setPublished(updateTime);
        result.setTitle(title);
        result.setActor(author);

        result.setVerb("post");

        result.setObject(activityObject);

        result.setTarget(targetObject);

        return result;
    }

}