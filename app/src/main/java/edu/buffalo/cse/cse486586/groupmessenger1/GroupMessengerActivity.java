package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final int SERVER_PORT = 10000;
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    String[] PORTS= new String[]{"11108","11112","11116","11120","11124"};
    public static final String KEY_FIELD = "key";
    public static final String VALUE_FIELD = "value";
    public static int seq = 0;
    public Uri uri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView tv = (TextView) findViewById(R.id.textView1);
                tv.append(msg+'\n');
                //tv.setMovementMethod(new ScrollingMovementMethod());
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });

    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            String ack="okay";
            ServerSocket serverSocket = sockets[0];
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Accepts the client's connection request
                    Log.i(TAG, "SErver accepted");
                    //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Input stream that helps server read data from socket
                    String data ;
                    if ((data=in.readLine()) != null) { // Receive data when not null
                        Log.i(TAG, data + " message received");

                        publishProgress(data); //Publishes updates on the UI. Invokes onProgressUpdate() every time publishProgress() is called.

                        // Reference: https://developer.android.com/reference/android/os/AsyncTask
                        //Server sends an ACK message after successfully reading the client message
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // Output Stream that client uses to send messages
                        Log.i(TAG, "Acknowledgement sending: " + ack);
                        out.println(data); //Display message on the screen
                        Log.i(TAG, "Acknowledgement sent");

                        in.close();
                        out.flush();
                        out.close();

                        clientSocket.close();
                        Log.i(TAG, "Client socket closed");
                    }
                    // Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/definition.html

                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
//                try {
//                    serverSocket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            }
        }
        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(strReceived + "\t\n");
            // PA2 documentation
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_FIELD,Integer.toString(seq));
            contentValues.put(VALUE_FIELD,strReceived);
            getContentResolver().insert(uri,contentValues);
            seq++;
        }
    }


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            for (int i = 0; i < 5; i++) {
                try {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(PORTS[i])); // Establishes a new socket creation for client/server communication
                        String msgToSend = msgs[0].trim();
                        Log.d(TAG, "Status of socket" + socket.isConnected());
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Output Stream that client uses to send messages
                        Log.i(TAG, "Message to send : " + msgToSend);
                        out.println(msgToSend); //Sends the message
                        out.flush();

                        // Reads the ACK message from the server
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String ack = in.readLine();
                        if(ack.equals("okay")){
                            Log.i(TAG, "ACK received"+ ack );
                            in.close();
                            out.close();
                            socket.close();
                            Log.i(TAG,"client-side socket closed");
                        }
                        // Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/definition.html

                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException" + e.getMessage());
                }
            }
            return null;
        }

    }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
            return true;
        }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

}

