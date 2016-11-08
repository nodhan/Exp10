package com.nodhan.exp10;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/***
 * @author Nodhan Theerthala
 */
public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        ContactAdapter ca = new ContactAdapter(readContacts(20));
        recList.setAdapter(ca);

    }

    private List<ContactInfo> readContacts(int size) {

        ContentResolver cr = getContentResolver();
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, sort);

        List<ContactInfo> result = new ArrayList<>();
        if (pCur != null && pCur.getCount() > 0) {

            String prevName = "";

            while (pCur.moveToNext()) {

                String id = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts._ID));    //extract details from the cursor
                String name = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (prevName.equals(name)) {
                    continue;
                }

                if (Integer.parseInt(pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    ContactInfo ci = new ContactInfo();
                    ci.name = name;

                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);  //store phone details into another cursor
                    if (phoneCursor != null) {
                        pCur.moveToNext();
                        String phoneNumber;
                        try {
                            phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        } catch (CursorIndexOutOfBoundsException e) {
                            phoneNumber = "";
                        }
                        ci.number = phoneNumber;
                        phoneCursor.close();
                    }

                    Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (emailCursor != null) {
                        emailCursor.moveToNext();
                        String email;
                        try {
                            email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        } catch (CursorIndexOutOfBoundsException e) {
                            email = "";
                        }
                        ci.email = email;
                        emailCursor.close();
                    }

                    result.add(ci);
                    prevName = name;
                    if (--size < 0) break;
                }

            }
        }
        return result;
    }
}