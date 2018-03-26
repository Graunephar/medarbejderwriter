package lol.graunephar.android.medarbejderwriter.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import com.google.gson.Gson;

import java.io.IOException;

import lol.graunephar.android.medarbejderwriter.models.TagContentMessage;

/**
 * Created by daniel on 3/16/18.
 * https://stackoverflow.com/questions/12453658/reading-data-from-nfc-tag
 */


public class NFCReader {

    private static final String TAG = NFCReader.class.toString();
    private final Context mContext;
    private Gson gson = new Gson();

    public NFCReader(Context context) {
        this.mContext = context;
    }


    public TagContentMessage readFromTag(Intent intent, Tag tag) throws IOException {

        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {
            //As ndef is null, this is the only place were we fo not have to close the connection
            throw new NotSupportedContentException("This is a strange tag");
        }

        ndef.connect();
        NdefMessage ndefMessage;
        try {
            ndefMessage = ndef.getNdefMessage();

        } catch (FormatException e) {
            ndef.close();
            throw new FormatetWrongException("Format exception");
        }


        if (ndefMessage == null) {
            ndef.close(); //Always close the connection
            //Log.d(TAG, "Empty tag");
            throw new EmptytagException("The tag is Empty");
        }

        ndef.close();

        NdefRecord[] records = ndefMessage.getRecords();

        TagContentMessage res = getContent(records);

        return res;


    }

    private TagContentMessage getContent(NdefRecord[] records) throws NotSupportedContentException {


        if (!isThisOurTag(records)) {
            //Log.d(TAG, "Not our tag");
            throw new NotSupportedContentException("Tag is not out type");

        }

        NdefRecord record = records[0];
        byte[] payload = record.getPayload();
        String jsonstring = new String(payload);
        //Log.d(TAG, "IS ourtag");

        TagContentMessage res = gson.fromJson(jsonstring, TagContentMessage.class);

        return res;
    }

    private boolean isThisOurTag(NdefRecord[] records) {

        for (int i = 0; i < records.length; i++) {

            NdefRecord type = records[i];
            byte[] aar = type.getPayload();
            String aarcontent = new String(aar);
            if (aarcontent.equals((NFCWriter.SCANNER_PACKAGE_NAME))) return true;
        }

        return false;
    }

    class NFCReaderException extends IOException {
        private final String message;

        public NFCReaderException(String messsage) {
            super(messsage);
            this.message = messsage;
        }
    }

    public class FormatetWrongException extends NFCReaderException {
        public FormatetWrongException(String messsage) {
            super(messsage);
        }
    }

    public class NotSupportedContentException extends NFCReaderException {

        public NotSupportedContentException(String messsage) {
            super(messsage);
        }
    }

    public class EmptytagException extends NFCReaderException {

        public EmptytagException(String messsage) {
            super(messsage);
        }
    }


}


