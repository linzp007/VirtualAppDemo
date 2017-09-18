package com.github.tamir7.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class QueryPlus {
    private Context mContext;
    private ContentResolver mContentResolver;

    public QueryPlus(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public int count() {
        int count = 0;
        Cursor c = null;
        try {
            c = mContentResolver.query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.RawContacts.CONTACT_ID},
                    null,
                    null,
                    null);
        } catch (Exception e) {
        }
        if (c != null) {
            count = c.getCount();
            c.close();
        }
        return count;
    }
}
