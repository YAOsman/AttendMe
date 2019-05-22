package uoit.ca.attendme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "school.db";
    private static final String TABLE_1_NAME = "STUDENTS";
    private static final String TABLE_2_NAME = "ATTENDANCE";
    private static final String TABLE_3_NAME = "LOCALATTENDANCE";

    private static final String COL_1_0 = "STUDENT_ID";
    private static final String COL_1_1 = "STUDENT_NAME";
    private static final String COL_2_0 = "ATTENDANCE_ID";
    private static final String COL_2_1 = "DATE";
    private static final String COL_2_2 = "STUDENT_NAME";
    private static final String COL_3_0 = "DATE_ID";
    private static final String COL_3_1 = "DATE";




    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    //Creating DB
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_1_NAME + "(" +
                COL_1_0 + " Integer PRIMARY KEY AUTOINCREMENT," +
                COL_1_1 +  " Text)"+
                ";" ;
        db.execSQL(createTable);
        createTable = "CREATE TABLE " + TABLE_2_NAME + "(" +
                COL_2_0 + " Integer PRIMARY KEY AUTOINCREMENT," +
                COL_2_1 +  " Text,"+
                COL_2_2 + " Text)"+
                ";" ;
        db.execSQL(createTable);
        createTable = "CREATE TABLE " + TABLE_3_NAME + "(" +
                COL_3_0 + " Integer PRIMARY KEY," +
                COL_3_1 +  " Text)"+
                ";" ;
        db.execSQL(createTable);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop table " + TABLE_1_NAME + ";" );
        this.onCreate(db);
    }

    //Adding new record, takes a student as parameter
    public void addStudent(String name){

        ContentValues values= new ContentValues();

        values.put(COL_1_1,name);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_1_NAME,null,values);
        db.close();
    }

    //Deleting record, takes name of student as parameter
    public void deleteStudent(String nameInput){

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("Delete from " + TABLE_1_NAME + " where " + COL_1_1+ "='"  + nameInput +"';");
        db.close();
    }
    //Adding new record, takes a student as parameter
    public void addAttendance(String name){

        ContentValues values= new ContentValues();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        values.put(COL_2_1,date);
        values.put(COL_2_2,name);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_2_NAME,null,values);
        db.close();
    }
    //Updating local attendance, to make sure user doesn't attend twice in same day
    public void updateAttendance()
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values= new ContentValues();

        values.put(COL_3_0,0);
        values.put(COL_3_1,"");
        db.insert(TABLE_3_NAME,null,values);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        values.put(COL_3_1,date);
        db.update(TABLE_3_NAME,values,"DATE_ID=0",null);
        db.close();
    }
    //Finding student
    public boolean findStundent(String name)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query =" Select COUNT(*) from " + TABLE_1_NAME + " where "+COL_1_1+ "='" +name+"';";
        Cursor c = db.rawQuery(query,null);
        if(c!=null)
        {
            c.moveToFirst();
            if(c.getInt(0)==0) {
                db.close();
                return false;
            }
            else{
                db.close();
                return true;
            }
        }
        db.close();
        return false;

    }
    //Finding attendance
    public boolean findAttendance(String date)
    {
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("Delete from " + TABLE_3_NAME + " where " + COL_3_1+ "='"  + date +"';");
        String query =" Select COUNT(*) from " + TABLE_3_NAME + " where "+COL_3_1+ "='" +date+ "';";
        Cursor c = db.rawQuery(query,null);
        if(c!=null)
        {
            c.moveToFirst();
            if(c.getInt(0)==0) {
                db.close();
                return false;
            }
            else{
                db.close();
                return true;
            }
        }
        db.close();
        return false;
    }

    //For debugging
    public void getAllResults(){
        String result="";

        SQLiteDatabase db = getWritableDatabase();
        String query =" Select * from " + TABLE_3_NAME + ";";

        Cursor c = db.rawQuery(query,null);

        c.moveToFirst();
        int temp1;
        String temp2;
        while(!(c.isAfterLast())){
            temp1 = (c.getInt(c.getColumnIndex(COL_3_0)));
            temp2 =(c.getString(c.getColumnIndex(COL_3_1)));
            c.moveToNext();
            /*
            result += c.getString(c.getColumnIndex(COL_1));
            result+=" - ";
            result += c.getString(c.getColumnIndex(COL_2));
            result+=" - ";
            result += String.valueOf(c.getDouble(c.getColumnIndex(COL_3)));
            result+= " \n ";
            c.moveToNext();*/

        }
        db.close();

    }


}
