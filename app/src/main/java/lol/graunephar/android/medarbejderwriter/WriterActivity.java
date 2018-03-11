package lol.graunephar.android.medarbejderwriter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;

public class WriterActivity extends AppCompatActivity {

    @BindView(R.id.writer_fun_label) TextView funLabel;
    @BindView(R.id.writer_name_label) TextView nameLabel;
    @BindView(R.id.writer_points_label) TextView pointLabel;
    @BindView(R.id.writer_fun_txt) EditText funTxt;
    @BindView(R.id.writer_name_txt) EditText nameTxt;
    @BindView(R.id.writer_points_num) EditText pointTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writer);


        fixUI();
    }

    private void fixUI() {

        funLabel.setText(R.string.writer_fun_label_text);
        nameLabel.setText(R.string.writer_name_label_text);
        pointLabel.setText(R.string.writer_point_label_text);

        addChangeListeners();

    }

    private void addChangeListeners() {

    }
}
