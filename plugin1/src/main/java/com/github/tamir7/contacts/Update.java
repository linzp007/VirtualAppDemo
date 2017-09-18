package com.github.tamir7.contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

public class Update {
    private final ContentResolver mContentResolver;

    public Update(Context context) {
        this.mContentResolver = context.getContentResolver();
    }

    public long insert(IContact contact) {
        ContentValues values = new ContentValues();
        Uri rawContactUri = mContentResolver.insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        ContentValues valuesData2 = new ContentValues();
        valuesData2.put(Data.RAW_CONTACT_ID, rawContactId);
        valuesData2.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        valuesData2.put(StructuredName.DISPLAY_NAME, contact.getDisplayName());
        mContentResolver.insert(Data.CONTENT_URI, valuesData2);

        ContentValues valuesData1 = new ContentValues();
        valuesData1.put(Data.RAW_CONTACT_ID, rawContactId);
        valuesData1.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        valuesData1.put(Phone.NUMBER, contact.getPhoneNumber());
        mContentResolver.insert(Data.CONTENT_URI, valuesData1);
        return rawContactId;
    }

    public int deleteAll() {
        int rs = mContentResolver.delete(RawContacts.CONTENT_URI, null, null);
        return mContentResolver.delete(Data.CONTENT_URI, null, null);
    }

    public int delete(IContact contact) {
        int rs = mContentResolver.delete(RawContacts.CONTENT_URI, RawContacts._ID + "=?",
                new String[]{String.valueOf(contact.getContactId())});
        rs += mContentResolver.delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=?",
                new String[]{String.valueOf(contact.getContactId())});
//        Cursor cursor = mContentResolver.query(Data.CONTENT_URI, null, null, null, null);
//        Logs.d("test", "cursor:" + cursor.getCount());
//        cursor.close();
        return (rs > 0) ? 1 : 0;
    }
}
