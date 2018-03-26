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

import org.ndeftools.Message;
import org.ndeftools.MimeRecord;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
    public static final String SCANNER_PACKAGE_NAME = "lol.graunephar.android.nfc";

    public static void writeTag(Tag tag, String jsondata) throws NFCFormatException, NFCTagLostException, NFCUnknownIOException, UnsupportedJSONEncodingInData, NotWritableTagException {

        NdefMessage message = createMessage(jsondata);

        if (tag == null) {
            throw new NFCTagLostException("Tag not present");
        }
        Ndef ndeftag = Ndef.get(tag);

        if(ndeftag == null) throw new NotWritableTagException("We do not know this tag");
        try {
            ndeftag.connect();
            ndeftag.writeNdefMessage(message);
        } catch (IOException e) {
            //Log.e(TAG, "IOException");
            throw new NFCUnknownIOException("Unknown exception");
        } catch (FormatException e) {
            //Log.e(TAG, "Format exception");
            throw new NFCFormatException("Format exception");
        } finally {

            try {
                if (ndeftag != null) ndeftag.close();
            } catch (IOException e) {
                //Log.e(TAG, "IOException while closing Tag", e);
                throw new NFCUnknownIOException("Error while closing tag");
            }
        }
    }


    private static NdefMessage createMessage(String jsondata) throws UnsupportedJSONEncodingInData {
        AndroidApplicationRecord aar = new AndroidApplicationRecord();
        aar.setPackageName(SCANNER_PACKAGE_NAME);
        MimeRecord mimeRecord = new MimeRecord();
        mimeRecord.setMimeType("text/plain");

        //vnd.android.nfc://ext//graunephar.lol:nfc
        try {
            mimeRecord.setData(jsondata.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedJSONEncodingInData("JSON WRONG");
        }

        final Message message = new Message(); //  org.ndeftools.Message

        message.add(mimeRecord);
        message.add(aar);

        return message.getNdefMessage();
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
        IntentFilter empty = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        intentFiltersArray = new IntentFilter[]{ndef, empty};

        techListsArray = new String[][]{new String[]{NfcF.class.getName()}};

    }

    public void disableForegroundDispatch(WriterActivity writerActivity) {
        mAdapter.disableForegroundDispatch(writerActivity);
    }

    public void EnableForegroundDispatch(Activity activity) {
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListsArray);
        }
    }


    public static class NFCWriterException extends IOException {
        private final String message;

        NFCWriterException(String message) {
            this.message = message;
        }
    }

    public static class NotWritableTagException extends NFCWriterException {
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

    public static class NFCFormatException extends NFCWriterException {
        NFCFormatException(String message) {
            super(message);
        }
    }

    public static class UnsupportedJSONEncodingInData extends NFCWriterException {
        UnsupportedJSONEncodingInData(String message) {
            super(message);
        }
    }
}
