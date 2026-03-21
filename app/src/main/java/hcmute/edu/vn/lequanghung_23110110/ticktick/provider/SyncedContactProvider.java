package hcmute.edu.vn.lequanghung_23110110.ticktick.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import hcmute.edu.vn.lequanghung_23110110.ticktick.database.ContactDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.provider.SyncedContactContract.ContactEntry;

public class SyncedContactProvider extends ContentProvider {

    private static final int CONTACTS = 100;
    private static final int CONTACT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(SyncedContactContract.AUTHORITY, SyncedContactContract.PATH_CONTACTS, CONTACTS);
        sUriMatcher.addURI(SyncedContactContract.AUTHORITY, SyncedContactContract.PATH_CONTACTS + "/#", CONTACT_ID);
    }

    private ContactDatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ContactDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                cursor = database.query(ContactEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CONTACT_ID:
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ContactEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return "vnd.android.cursor.dir/" + SyncedContactContract.AUTHORITY + "/" + SyncedContactContract.PATH_CONTACTS;
            case CONTACT_ID:
                return "vnd.android.cursor.item/" + SyncedContactContract.AUTHORITY + "/" + SyncedContactContract.PATH_CONTACTS;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        if (match == CONTACTS) {
            SQLiteDatabase database = mDbHelper.getWritableDatabase();
            long id = database.insert(ContactEntry.TABLE_NAME, null, values);
            if (id == -1) {
                return null;
            }
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return ContentUris.withAppendedId(uri, id);
        }
        throw new IllegalArgumentException("Insertion is not supported for " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                rowsDeleted = database.delete(ContactEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACT_ID:
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ContactEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                rowsUpdated = database.update(ContactEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CONTACT_ID:
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = database.update(ContactEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

        if (rowsUpdated != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
