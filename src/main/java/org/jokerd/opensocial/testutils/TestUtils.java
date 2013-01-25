package org.jokerd.opensocial.testutils;

import java.util.Random;

public class TestUtils {
    protected static char[] CHARS = (""
        + "0123456789"
        + "abcdefghijklmnopqrstuvwxyz"
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        + "").toCharArray();

    protected static String LOREM_IPSUM = "\n"
        + "Aliquam erat volutpat. Vivamus ut orci quis nulla faucibus posuere. "
        + "Pellentesque malesuada, nisl in suscipit tempus, libero erat "
        + "vestibulum elit, sed consequat quam lorem quis dolor. Nam massa "
        + "felis, congue ut mattis a, congue in diam. Proin ac risus arcu, "
        + "sed pellentesque magna. Proin elit eros, adipiscing nec porttitor a, "
        + "commodo vitae massa. Ut interdum volutpat ipsum eget ultrices. "
        + "Fusce eros tortor, condimentum ut accumsan a, convallis quis lacus. "
        + "Nunc varius convallis ullamcorper. Etiam tincidunt justo a dui "
        + "faucibus pharetra sed nec mauris.\n"
        + "\n"
        + "Donec in sapien mi, eu aliquet augue. Vestibulum in tellus id orci "
        + "sagittis tincidunt. Quisque et elit eget lacus molestie congue vel "
        + "et erat. Phasellus ornare leo eu nibh accumsan pellentesque. "
        + "Quisque imperdiet tincidunt varius. Duis in justo eros, et porta "
        + "turpis. Ut accumsan felis ac dolor ultricies condimentum sed id "
        + "ipsum. Sed sodales, leo eget tempor dignissim, justo mi pharetra "
        + "nibh, vel vulputate augue ipsum id massa. Phasellus tristique "
        + "facilisis metus non laoreet. Vestibulum id arcu id enim mattis "
        + "iaculis. Phasellus viverra dignissim luctus.\n"
        + "\n"
        + "Cras feugiat risus tristique libero euismod vitae faucibus leo "
        + "semper. Nunc purus arcu, faucibus non ornare quis, vehicula ac "
        + "augue. Cum sociis natoque penatibus et magnis dis parturient "
        + "montes, nascetur ridiculus mus. Vestibulum viverra, purus quis "
        + "tincidunt feugiat, nunc tellus condimentum justo, eu placerat dolor "
        + "magna nec massa. Vestibulum ante ipsum primis in faucibus orci "
        + "luctus et ultrices posuere cubilia Curae; Nulla commodo purus "
        + "sollicitudin lacus semper convallis. Suspendisse varius blandit "
        + "mattis.\n"
        + "\n"
        + "Integer arcu neque, congue vel varius eget, luctus vitae leo. "
        + "Donec auctor ullamcorper rutrum. Maecenas id sodales velit. Quisque "
        + "sit amet sollicitudin sapien. Aliquam dapibus odio sit amet erat "
        + "pharetra sed gravida lectus iaculis. Nam suscipit eleifend leo, "
        + "vitae volutpat purus volutpat volutpat. In venenatis eleifend massa "
        + "non varius. Suspendisse venenatis sem et neque pharetra tincidunt. "
        + "Phasellus purus odio, faucibus non porttitor porttitor, posuere "
        + "non est. Cras iaculis suscipit mauris id lacinia. Aenean facilisis "
        + "ultrices purus, at pharetra sapien semper venenatis. In pulvinar "
        + "gravida augue vitae pretium. Curabitur vestibulum fermentum lacus, "
        + "ac dictum lorem consectetur eu. Nam accumsan orci in justo tempor "
        + "id dictum enim suscipit.\n"
        + "\n"
        + "Curabitur blandit, sapien volutpat pretium pretium, leo nibh "
        + "convallis diam, at vulputate massa urna id nibh. Mauris a leo in "
        + "tortor fringilla mattis. Mauris vitae ligula vitae velit fringilla "
        + "ornare id ac dolor. Donec nisl ipsum, semper sit amet commodo vel, "
        + "tempor vitae neque. Suspendisse urna sem, imperdiet id molestie a, "
        + "porta ut nisi. Suspendisse tempus metus leo, a vulputate urna. "
        + "Aliquam accumsan, est et adipiscing ultrices, erat enim semper mi, "
        + "faucibus pharetra mauris tellus et ante. Donec vestibulum lectus ac "
        + "dui hendrerit eu tincidunt nisl consequat. Quisque vehicula risus eu "
        + "erat venenatis pellentesque. Etiam nec nisi justo. Nulla facilisi. "
        + "Etiam lobortis odio sit amet tellus pharetra interdum vitae sed "
        + "est. Aliquam tincidunt turpis dolor, id ullamcorper lectus. Proin "
        + "ut elit vitae velit pulvinar porttitor. Vivamus pulvinar, nisl sit "
        + "amet tristique molestie, risus velit sollicitudin ante, id "
        + "consectetur odio ipsum vitae metus. Vestibulum at arcu vitae erat "
        + "aliquam lobortis ut sed tellus.\n";

    protected static String[] LOREM_IPSUM_PHRASES = LOREM_IPSUM
        .split("[\\.]\\s+");
    static {
        for (int i = 0; i < LOREM_IPSUM_PHRASES.length; i++) {
            LOREM_IPSUM_PHRASES[i] = LOREM_IPSUM_PHRASES[i].trim() + ".";
        }
    }

    /**
     * This field should not be used directly. Use the {@link #getRandom()}
     * method instead.
     */
    private Random fRandom;

    protected long fStart = System.currentTimeMillis();

    public TestUtils() {
    }

    public String getLoremIpsum(int maxLen) {
        return getLoremIpsum(0, maxLen);
    }

    public String getLoremIpsum(int minLen, int maxLen) {
        int len = 0;
        int delta = maxLen - minLen;
        if (delta <= 0) {
            len = minLen;
        } else {
            len = minLen + getRandom().nextInt(delta);
        }
        StringBuilder buf = new StringBuilder();
        while (len > 0) {
            if (buf.length() > 0) {
                buf.append(" ");
                len--;
            }
            if (len > 0) {
                String phrase = getLoremIpsumPhrase(len);
                buf.append(phrase);
                len -= phrase.length();
            }
        }
        return buf.toString();
    }

    public String getLoremIpsumPhrase(int maxLen) {
        int pos = getRandom().nextInt(LOREM_IPSUM_PHRASES.length);
        String str = LOREM_IPSUM_PHRASES[pos];
        while (str.length() > maxLen) {
            int idx = str.lastIndexOf(' ');
            if (idx > 0) {
                str = str.substring(0, idx);
            } else {
                str = str.substring(0, maxLen);
            }
        }
        if (str.length() > 0 && !str.endsWith(".")) {
            str = str.substring(0, str.length() - 1) + ".";
        }
        return str;
    }

    protected Random getRandom() {
        if (fRandom == null) {
            long base = isFixedRandom() ? 1 : System.currentTimeMillis();
            fRandom = new Random(base);
        }
        return fRandom;
    }

    public String getRandomString(char[] chars, int minLen, int maxLen) {
        Random rnd = getRandom();
        int len = 0;
        int delta = maxLen - minLen;
        if (delta <= 0) {
            len = minLen;
        } else {
            len = minLen + rnd.nextInt(delta);
        }
        byte[] array = new byte[len];
        rnd.nextBytes(array);
        for (int i = 0; i < array.length; i++) {
            int code = array[i] & 0xFFFF;
            array[i] = (byte) chars[(code % chars.length)];
        }
        return new String(array);
    }

    public String getRandomString(int maxLen) {
        return getRandomString(CHARS, 1, maxLen);
    }

    public String getRandomString(int minLen, int maxLen) {
        return getRandomString(CHARS, minLen, maxLen);
    }

    protected boolean isFixedRandom() {
        return false;
    }
}