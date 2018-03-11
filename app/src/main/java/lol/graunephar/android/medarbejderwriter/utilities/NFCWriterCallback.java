package lol.graunephar.android.medarbejderwriter.utilities;

/**
 * Created by daniel on 3/11/18.
 */

public interface NFCWriterCallback {
    void writeNdefSuccess();

    void writeNdefFailed(Exception e);

    void writeNdefNotWritable();

    void writeNdefTooSmall(int length, int maxSize);

    void writeNdefCannotWriteTech();
}
