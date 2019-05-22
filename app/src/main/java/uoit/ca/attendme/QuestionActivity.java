package uoit.ca.attendme;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    TextView question1;
    TextView question2;
    Button submit;
    Spinner answers1;
    Spinner answers2;
    String question1String = "What is called when a view is first created in Android?";
    List<String> answers1List = new ArrayList<String>();
    String question2String = "Which intent do you use to call a system default intent like dialing a number?";
    List<String> answers2List = new ArrayList<String>();
    String correctAnswer1;
    String correctAnswer2;
    TextView result;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        result=(TextView) findViewById(R.id.resultTxt);
        question1= (TextView) findViewById(R.id.question1Txt);
        question2= (TextView) findViewById(R.id.question2Txt);
        submit=(Button) findViewById(R.id.submitBtn);
        answers1=(Spinner) findViewById(R.id.answer1Spn);
        answers2=(Spinner) findViewById(R.id.answer2Spn);
        answers1List.add("onCreate()");
        answers1List.add("onDestroy()");
        answers2List.add("Implicit Intent");
        answers2List.add("Explicit Intent");
        correctAnswer1="onCreate()";
        correctAnswer2="Implicit Intent";
        question1.setText(question1String);
        question2.setText(question2String);
        ArrayAdapter<String> answers1Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, answers1List);
        answers1.setAdapter(answers1Adapter);
        ArrayAdapter<String> answers2Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, answers2List);
        answers2.setAdapter(answers2Adapter);
        dbHelper = new DBHelper(this,null, null, 1);
    }

    public void onSubmit(View v)
    {
        if(answers1.getSelectedItem().toString()!=null && answers2.getSelectedItem().toString()!=null)
        {
            if(answers1.getSelectedItem().toString()==correctAnswer1 || answers2.getSelectedItem().toString()==correctAnswer2)
            {
                result.setText("Attendance Recorded!");
                result.setTextColor(Color.parseColor("#00FF00"));
                dbHelper.addAttendance(Constants.studentName);
                dbHelper.updateAttendance();
            }
            else
            {
                result.setText("Incorrect answers, contact your professor with a screenshot of this screen.");
                result.setTextColor(Color.parseColor("#FF0000"));
            }
            submit.setEnabled(false);
        }
    }
}
