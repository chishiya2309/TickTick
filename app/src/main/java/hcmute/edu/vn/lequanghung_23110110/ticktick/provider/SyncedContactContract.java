package hcmute.edu.vn.lequanghung_23110110.ticktick.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class SyncedContactContract {
    public static final String AUTHORITY = "hcmute.edu.vn.lequanghung_23110110.ticktick.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_CONTACTS = "synced_contacts";

    public static final class ContactEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACTS).build();

        public static final String TABLE_NAME = "synced_contacts";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE = "phone";
    }
}
