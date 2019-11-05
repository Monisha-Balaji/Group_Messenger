package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    private static final String TAG = GroupMessengerProvider.class.getName();

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */

        // Files are used to store the <key,value> pairs
        String filename = values.get(KEY_FIELD).toString();
        String value = values.get(VALUE_FIELD).toString();

//        Log.i(TAG, "key received in insert methond: " + filename);
//        Log.i(TAG, "value received in insert methond : " + value);

        FileOutputStream outputStream;

        try {
            outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(values.get(VALUE_FIELD).toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "File write failed" + e.getMessage());
        }
        Log.v("insert", values.toString());
        return uri;
    }


    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //http://developer.android.com/reference/android/database/MatrixCursor.html
        // Reference: 1. https://www.androidinterview.com/android-internal-storage-read-and-write-text-file-example/
        //2. https://docs.oracle.com/javase/8/docs/api/?java/io/FileInputStream.html

        // Reads string from the file and stores the data as rows into the Matrixcursor
        MatrixCursor matrixCursor = null;
        try {

            FileInputStream filestream = getContext().openFileInput(selection);
            BufferedReader buf = new BufferedReader(new InputStreamReader(filestream));
            String value =buf.readLine();
            String[] rowdata = new String[]{selection,value};
            Log.i(TAG,"query key we got : " + selection);
            Log.i(TAG,"query- value we got : " +  value);
            matrixCursor= new MatrixCursor(new String[]{"key","value"});
            matrixCursor.addRow(rowdata);
            buf.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v("query", selection);
        return matrixCursor ;
    }
}
