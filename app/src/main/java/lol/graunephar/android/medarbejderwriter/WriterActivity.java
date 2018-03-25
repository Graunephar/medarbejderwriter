package lol.graunephar.android.medarbejderwriter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lol.graunephar.android.medarbejderwriter.models.TagContentMessage;
import lol.graunephar.android.medarbejderwriter.nfc.NFCReader;
import lol.graunephar.android.medarbejderwriter.nfc.NFCWriter;

public class WriterActivity extends AppCompatActivity {

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
    private boolean mTagHasBeenPlaces = false;
    private boolean mOnGoingWrite = false;
    private Gson gson = new Gson();
    private NFCReader mReader;
    private Tag detectedTag;
    private NFCWriter mWriter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writer);

        ButterKnife.bind(this);

        fixUI();

        mReader = new NFCReader(this);

        mWriter = new NFCWriter();
        mWriter.foregroundDispatch(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWriter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWriter.EnableForegroundDispatch(this);
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

    /* NFC methods */

    /**
     * Reads a tag when put near the phone
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!mOnGoingWrite) {
            checkIfTag(intent);
        } else {
            tellUser(getString(R.string.write_progress_please_message));
        }
    }

    private void checkIfTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            detectedTag = tag;
            mTagHasBeenPlaces = true;
            readNFCData();
        }
    }

    private void readNFCData() {

        String prefix = getString(R.string.found_message_prefix);
        String message;

        try {
            TagContentMessage content = mReader.readFromTag(getIntent(), detectedTag);
            message = content.getName();

        } catch (NFCReader.EmptytagException e) {
            message = getString(R.string.empty_tag_messaage);
        } catch (NFCReader.NotSupportedContentException e) {
            message = getString(R.string.unsupported_tag_message);
        } catch (IOException e) {
            message = getString(R.string.reader_unable_read_message);
        }

        tellUser(prefix + message);
    }

    /**
     * Write to the tag
     */
    @OnClick(R.id.writer_write_btn)
    public void startWritingToTag() {

        if (!mReadyToWrite) {
            tellUser(getString(R.string.write_fill_fields));
            return;
        } else if (!mTagHasBeenPlaces) {
            tellUser(getString(R.string.writer_tag_not_placed_message));
            return;
        } else if (mOnGoingWrite) {
            tellUser(getString(R.string.writer_write_in_progress_message));
            return;
        }
        
        tellUser(getString(R.string.write_started_message));

        String jsondata = gatherContentData();

        mOnGoingWrite = true; //TODO: Test two writes in a row

        try {
            NFCWriter.writeTag(detectedTag, jsondata);
            tellUser(getString(R.string.write_success_message));
        } catch (NFCWriter.NFCFormatException e) {
            tellUser(getString(R.string.format_error_message));
        } catch (NFCWriter.NFCTagLostException e) {
            tellUser(getString(R.string.taglost_error_message));
        } catch (NFCWriter.NFCUnknownIOException e) {
            tellUser(getString(R.string.unknown_write_error_message));
        } catch (NFCWriter.UnsupportedJSONEncodingInData e) {
            tellUser(getString(R.string.writer_not_working_encoding_message));
        } finally {
            mOnGoingWrite = false;
        }
    }

    private String gatherContentData() {

        String name = nameTxt.getText().toString().trim();
        String fact = funTxt.getText().toString();
        int points = Integer.parseInt(pointTxt.getText().toString());

        TagContentMessage content = new TagContentMessage(name, fact, points);

        String data = gson.toJson(content);

        return data;
    }
}
