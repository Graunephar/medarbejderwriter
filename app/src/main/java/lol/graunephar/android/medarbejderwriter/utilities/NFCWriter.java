package lol.graunephar.android.medarbejderwriter.utilities;

import java.io.IOException;

import org.ndeftools.Message;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;

/**
 * Created by daniel on 3/11/18.
 * Based on the code from ndeftools
 * found here: https://github.com/skjolber/ndef-tools-for-android/blob/master/ndeftools-util/src/org/ndeftools/util/activity/NfcTagWriterActivity.java
 */

public class NFCWriter {

    private static final String TAG = NFCWriter.class.getName();
    private NFCWriterCallback mCallback;

    public NFCWriter(NFCWriterCallback callback) {

        this.mCallback = callback;
    }

    public boolean write(Message message, Intent intent) {
        return write(message.getNdefMessage(), intent);
    }

    public boolean write(NdefMessage rawMessage, Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefFormatable format = NdefFormatable.get(tag);
        if (format != null) {
            Log.d(TAG, "Write unformatted tag");
            try {
                format.connect();
                format.format(rawMessage);

                mCallback.writeNdefSuccess();

                return true;
            } catch (Exception e) {
                mCallback.writeNdefFailed(e);
            } finally {
                try {
                    format.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            Log.d(TAG, "Cannot write unformatted tag");
        } else {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                try {
                    Log.d(TAG, "Write formatted tag");

                    ndef.connect();
                    if (!ndef.isWritable()) {
                        Log.d(TAG, "Tag is not writeable");

                        mCallback.writeNdefNotWritable();

                        return false;
                    }

                    if (ndef.getMaxSize() < rawMessage.toByteArray().length) {
                        Log.d(TAG, "Tag size is too small, have " + ndef.getMaxSize() + ", need " + rawMessage.toByteArray().length);

                        mCallback.writeNdefTooSmall(rawMessage.toByteArray().length, ndef.getMaxSize());

                        return false;
                    }
                    ndef.writeNdefMessage(rawMessage);

                    mCallback.writeNdefSuccess();

                    return true;
                } catch (Exception e) {
                    mCallback.writeNdefFailed(e);
                } finally {
                    try {
                        ndef.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            } else {
                mCallback.writeNdefCannotWriteTech();
            }
            Log.d(TAG, "Cannot write formatted tag");
        }

        return false;
    }

    public int getMaxNdefSize(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        NdefFormatable format = NdefFormatable.get(tag);
        if (format != null) {
            Log.d(TAG, "Format tag with empty message");
            try {
                if (!format.isConnected()) {
                    format.connect();
                }
                format.format(new NdefMessage(new NdefRecord[0]));
            } catch (Exception e) {
                Log.d(TAG, "Problem checking tag size", e);

                return -1;
            }
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                if (!ndef.isConnected()) {
                    ndef.connect();
                }

                if (!ndef.isWritable()) {
                    Log.d(TAG, "Capacity of non-writeable tag is zero");

                    mCallback.writeNdefNotWritable();

                    return 0;
                }

                int maxSize = ndef.getMaxSize();

                ndef.close();

                return maxSize;
            } catch (Exception e) {
                Log.d(TAG, "Problem checking tag size", e);
            }
        } else {
            mCallback.writeNdefCannotWriteTech();
        }
        Log.d(TAG, "Cannot get size of tag");

        return -1;
    }
}
