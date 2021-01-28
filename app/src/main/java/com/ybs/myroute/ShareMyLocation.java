package com.ybs.myroute;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ShareMyLocation extends AppCompatActivity {
    private ListView contactsListView;
    private ArrayList<String> contactsList;
    private ArrayAdapter contactsAdapter;
    private String myLocation, phoneNumber ="";
    private Button btnFind, btnSend;
    private String nameEdt;
    private EditText edtSend1;
    private String name, phone;
    private TextView txvMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_my_location);
        Bundle extras = getIntent().getExtras();
        txvMyLocation = findViewById(R.id.txvLocation);

        if(extras != null){
            myLocation = extras.getString("myLocation");
            txvMyLocation.setText(myLocation);
            Log.d("debud","my location: "+myLocation);
        }
        Log.d("debud","edt1");

        //Toast t = Toast.makeText(this," "+myLocation,Toast.LENGTH_LONG).show();

        contactsListView = findViewById(R.id.listviewID);
        btnFind = findViewById(R.id.btnFindC);
        btnSend = findViewById(R.id.btnSendSMS);
        contactsList = new ArrayList<String>();
        edtSend1 = findViewById(R.id.edtSend);
        Log.d("debud","edt2");
        //click to send
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMSMessage();
                txvMyLocation.setText("Your location send with SMS");
            }
        });
        //click to find a contact by the name with the edit text
        btnFind.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!edtSend1.getText().toString().equals("")){
                    btnSend.setVisibility(View.VISIBLE);
                    btnFind.setVisibility(View.GONE);
                }
                nameEdt= edtSend1.getText().toString();
                loadContacts();
                contactsAdapter = new ArrayAdapter<String>(ShareMyLocation.this, android.R.layout.simple_list_item_1, contactsList);
                contactsListView.setAdapter(contactsAdapter);
                Log.d("debud","load2 ");

            }
        });

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()//chack 1 of the list that return by the contact list
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                String i= contactsList.get(position);
                edtSend1.setText(i);
                contactsList.removeAll(contactsList);
                //contactsList.remove(position);
                contactsAdapter.notifyDataSetChanged();
            }

        });
    }
    //send my location wuth SMS
    public void sendSMSMessage()
    {
        if(isPermissionToSms()) {
            Log.d("debud","sms p");

            SmsManager smsManager = SmsManager.getDefault();
            Log.d("debud","sms p4"+phoneNumber+" "+myLocation);

            smsManager.sendTextMessage(phoneNumber, null, myLocation, null, null);
            Log.d("debud","sms p5");

        }
        else
        {
            Log.d("debud","sms p1");

            showCenteredToast("NO Permission to Send SMS!");
        }

    }

    public boolean isPermissionToSms() {
        // check if permission for SEND_SMS is granted ?
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            Log.d("debud", "sms p2");
            return true;
        }
        else {
            // show requestPermissions dialog
            Log.d("debud","sms p3");
            ActivityCompat.requestPermissions(ShareMyLocation.this, new String[]{Manifest.permission.SEND_SMS}, 111);
            return false;
        }
    }

    // Read all contacts from Content Provider in Contacts App.
    public void loadContacts()
    {
        Log.d("debud","load4 ");

        if(isPermissionToReadContactsOK())
        {
            Log.d("debud","load5 ");

            contactsList.clear();


            ContentResolver resolver = getContentResolver();
            Uri contactsTableUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            Cursor cursor = resolver.query(contactsTableUri, null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToNext())
                {
                    // there is at least ONE contact
                    do
                    {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        Log.d("debud","edt5 "+" "+nameEdt+" "+name);
                        phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.d("debud","edt6 "+phone);
                        if(nameEdt.equals(name)){
                            Log.d("debud","edt4"+ name+" "+nameEdt);
                            contactsList.add(name + " : " + phone);
                            phoneNumber=phone;
                            Log.d("debud","edt7"+ phoneNumber+" "+phone);

                        }

                    }
                    while(cursor.moveToNext());

                    cursor.close();
                }
                else
                    // Empty - No contacts
                    showCenteredToast("No Contacts!");
            }
            else
                // problem with resolver query
                showCenteredToast("Resolver Query Error!");
        }
        else
            showCenteredToast("NO Permission to Read Contacts!");
    }

    // Check Runtime Permission for READ_CONTACTS
    public boolean isPermissionToReadContactsOK()
    {
        // check if permission for READ_CONTACTS is granted ?
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
        {
            // show requestPermissions dialog
            ActivityCompat.requestPermissions(ShareMyLocation.this, new String[]{Manifest.permission.READ_CONTACTS}, 111);
            return false;
        }
    }


    // show centered toast
   public void showCenteredToast(String msg)
    {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}