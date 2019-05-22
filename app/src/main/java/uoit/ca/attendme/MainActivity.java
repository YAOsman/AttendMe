package uoit.ca.attendme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    Button register;
    Button submit;
    EditText nameText;
    DBHelper dbHelper;
    TextView errorText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this,null, null, 1);
        register=(Button) findViewById(R.id.registerBtn);
        register.setEnabled(false);
        submit=(Button) findViewById(R.id.submitBtn);
        nameText = (EditText) findViewById(R.id.nameTxt);
        errorText=(TextView) findViewById(R.id.errorTxt);
    }

    public void submitName(View v)
    {
        boolean isNameExists = dbHelper.findStundent(nameText.getText().toString());
        if(isNameExists)
        {
            Constants.studentName=nameText.getText().toString();
            Intent intent = new Intent(this, WelcomeScreen.class);
            startActivity(intent);
        }
        else
        {
            errorText.setText("Name doesn't exist! Please click register first.");
            register.setEnabled(true);
        }
    }
    public void registerName(View v)
    {
        String name = nameText.getText().toString();
        dbHelper.addStudent(name);
        errorText.setText("Student added!");
        register.setEnabled(false);
    }
}
