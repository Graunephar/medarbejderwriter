package lol.graunephar.android.medarbejderwriter;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.nfc.FormatException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ndeftools.Message;
import org.ndeftools.MimeRecord;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import java.io.UnsupportedEncodingException;

import be.appfoundry.nfclibrary.activities.NfcActivity;
import be.appfoundry.nfclibrary.exceptions.InsufficientCapacityException;
import be.appfoundry.nfclibrary.exceptions.ReadOnlyTagException;
import be.appfoundry.nfclibrary.exceptions.TagNotPresentException;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncOperationCallback;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncUiCallback;
import be.appfoundry.nfclibrary.utilities.async.WriteCallbackNfcAsync;
import be.appfoundry.nfclibrary.utilities.interfaces.NfcWriteUtility;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lol.graunephar.android.medarbejderwriter.utilities.NFCWriter;
import lol.graunephar.android.medarbejderwriter.utilities.NFCWriterCallback;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class WriterActivity extends NfcActivity implements AsyncUiCallback, NFCWriterCallback {

    @BindView(R.id.writer_fun_label)
    TextView funLabel;
    @BindView(R.id.writer_name_label)
    TextView nameLabel;
    @BindView(R.id.writer_points_label)
    TextView pointLabel;
    @BindView(R.id.writer_fun_txt)
    EditText funTxt;
    @BindView(R.id.writer_name_txt)
    EditText nameTxt;
    @BindView(R.id.writer_points_num)
    EditText pointTxt;
    @BindView(R.id.writer_write_btn)
    Button writeBtn;

    private boolean mReadyToWrite = false;
    private String TAG = WriterActivity.class.getName();
    NFCWriter mWriter;
    private boolean mTagHasBeenPlaces = false;


    public WriterActivity() {
        this.mWriter = new NFCWriter(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writer);

        Nammu.init(getApplicationContext());
        ButterKnife.bind(this);

        askForPermissions();

        fixUI();
    }


    private void fixUI() {

        funLabel.setText(R.string.writer_fun_label_text);
        nameLabel.setText(R.string.writer_name_label_text);
        pointLabel.setText(R.string.writer_point_label_text);
        greyoutButton(true);

        addChangeListeners();

    }

    private void addChangeListeners() {
        funTxt.addTextChangedListener(generateButtonWatcher());
        nameTxt.addTextChangedListener(generateButtonWatcher());
        pointTxt.addTextChangedListener(generateButtonWatcher());
    }

    private void checkInput() {
        String name = nameTxt.getText().toString().trim();
        String fact = funTxt.getText().toString().trim();
        String point = pointTxt.getText().toString().trim();

        if (checkEmpty(name, fact, point)) {
            greyoutButton(true);
            mReadyToWrite = false;
        } else {
            greyoutButton(false);
            mReadyToWrite = true;
        }
    }

    private void greyoutButton(boolean state) {
        if (state)
            writeBtn.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC);
        else
            writeBtn.getBackground().setColorFilter(null);
    }

    /**
     * Checks if ANY strings is empty
     *
     * @param strings
     * @return true if any of the strings is empty
     */
    private boolean checkEmpty(String... strings) {

        for (String s : strings) {
            if (s.isEmpty() || s.length() == 0 || s.equals("") || s == null) {
                return true;
            }
        }
        return false;
    }


    private TextWatcher generateButtonWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkInput();
            }
        };
    }

    private void tellUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /* Permissions */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void askForPermissions() {

        Nammu.askForPermission(this, Manifest.permission.NFC, new PermissionCallback() {
            @Override
            public void permissionGranted() {

            }

            @Override
            public void permissionRefused() {
                askForPermissions();
            }
        });

    }


    /* NFC methods */

    /**
     * Reada a tag when put near the phone
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(getApplicationContext(), getString(R.string.writer_message_new_tag_found) + "content", Toast.LENGTH_SHORT).show();
        mTagHasBeenPlaces = true;

    }

    /**
     * Write to the tag
     */
    @OnClick(R.id.writer_write_btn)
    public void startWritingToTag() {

        if (!mReadyToWrite) {
            tellUser(getString(R.string.write_fill_fields));
            return;
        } else if(!mTagHasBeenPlaces) {
            tellUser(getString(R.string.writer_tag_not_placed_message));
            return;
        }

        AndroidApplicationRecord aar = new AndroidApplicationRecord();
        aar.setPackageName("org.ndeftools.boilerplate");
        MimeRecord mimeRecord = new MimeRecord();
        mimeRecord.setMimeType("text/plain");
        try {
            mimeRecord.setData("This is my data".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final Message message = new Message(); //  org.ndeftools.Message
        message.add(aar);
        message.add(mimeRecord);


            AsyncOperationCallback writecallback= new AsyncOperationCallback() {

                @Override
                public boolean performWrite(NfcWriteUtility writeUtility) throws ReadOnlyTagException, InsufficientCapacityException, TagNotPresentException, FormatException {
                    return writeUtility.writeNdefMessageToTagFromIntent( message.getNdefMessage(),getIntent());
                }
            };

            new WriteCallbackNfcAsync(this, writecallback).executeWriteOperation();

    }

    /* AsyncUiCallback methods */

    @Override
    public void callbackWithReturnValue(Boolean result) {
        String message = result ? getString(R.string.write_success_message) : getString(R.string.write_fail_message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressUpdate(Boolean... booleans) {
        Toast.makeText(this, booleans[0] ? getString(R.string.write_started_message) : getString(R.string.write_not_able_message), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, getString(R.string.write_error), Toast.LENGTH_SHORT).show();
    }


    /** NFC Writer Callback methods **/
    @Override
    public void writeNdefSuccess() {
        Toast.makeText(getApplicationContext(), "SUCCCESSSSSS", Toast.LENGTH_LONG).show();

    }

    @Override
    public void writeNdefFailed(Exception e) {
        Log.d(TAG, e.getMessage());
    }

    @Override
    public void writeNdefNotWritable() {
        Log.d(TAG, "NDEF NOT WRITABLE");
    }

    @Override
    public void writeNdefTooSmall(int length, int maxSize) {
        Log.d(TAG, "NDEF TO SMALL");
    }

    @Override
    public void writeNdefCannotWriteTech() {
        Log.d(TAG, "CANNOT WRITE TECH");
    }
}
