package lol.graunephar.android.medarbejderwriter.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.io.IOException;

import lol.graunephar.android.medarbejderwriter.WriterActivity;

/**
 * Created by daniel on 3/24/18.
 */

public class NFCWriter {

    private NfcAdapter mAdapter;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private PendingIntent pendingIntent;

    private static final String TAG = NFCWriter.class.getSimpleName();

    public static void writeTag(Tag tag, NdefMessage message) throws NFCFormatException, NFCTagLostException, NFCUnknownIOException {
        if(tag == null) {
            throw new NFCTagLostException("Tag not present");
        }
        Ndef ndeftag = Ndef.get(tag);

        try {
            ndeftag.connect();
            ndeftag.writeNdefMessage(message);
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            throw new NFCUnknownIOException("Unknown exception");
        } catch (FormatException e) {
            Log.e(TAG, "Format exception");
            throw new NFCFormatException("Format exception");
        } finally {

            try {
                ndeftag.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing Tag", e);
                throw new NFCUnknownIOException("Error while closing tag");
            }
        }
    }

    //Using the foreground dispatch system to take priority over AAR
    //So the reader  does not open when writing to tag
    //https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc.html
    public void foregroundDispatch(Context context) {

        mAdapter = NfcAdapter.getDefaultAdapter(context);

        pendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //TODO: Specify text
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        intentFiltersArray = new IntentFilter[]{ndef,};

        techListsArray = new String[][]{new String[]{NfcF.class.getName()}};

    }

    public void disableForegroundDispatch(WriterActivity writerActivity) {
        mAdapter.disableForegroundDispatch(writerActivity);
    }

    public void EnableForegroundDispatch(Activity activity) {
        if(mAdapter != null) {
            mAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListsArray);
        }
    }


    public static class NFCWriterException extends IOException {
        private final String message;
        NFCWriterException(String message) {
            this.message = message;
        }
    }

    static class NotWritableTagException extends NFCWriterException {
        NotWritableTagException(String message) {
            super(message);
        }
    }

    private static class NotEnoghSpaceOnTagException extends NFCWriterException {
        NotEnoghSpaceOnTagException(String message) {
            super(message);
        }
    }

    public static class NFCTagLostException extends NFCWriterException {
        NFCTagLostException(String message) {
            super(message);
        }
    }

    public static class NFCUnknownIOException extends NFCWriterException {
        NFCUnknownIOException(String message) {
            super(message);
        }
    }

    public static class NFCFormatException extends NFCWriterException{
        NFCFormatException(String message) {
            super(message);
        }
    }
}
