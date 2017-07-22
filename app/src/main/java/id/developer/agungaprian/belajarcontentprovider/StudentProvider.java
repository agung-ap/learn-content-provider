package id.developer.agungaprian.belajarcontentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by agungaprian on 22/07/17.
 */

public class StudentProvider extends ContentProvider {
    static final String PROVIDER_NAME = "id.developer.agungaprian.belajarcontentprovider";
    static final String URL = "content://" + PROVIDER_NAME + "/students";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String GRADE = "grade";

    private static HashMap<String,String> STUDENT_PROJECTION_MAP;

    static final int STUDENT = 1;
    static final int STUDENT_ID = 2;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,"student", STUDENT);
        uriMatcher.addURI(PROVIDER_NAME,"student/#", STUDENT_ID);
    }

    /**
     * data base constant declaration
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "College";
    static final String STUDENTS_TABLE_NAME = "students";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + STUDENTS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " name TEXT NOT NULL, " +
                    " grade TEXT NOT NULL);";

    /**
     * helper class that can manager and creates the provider underlying data repository
     */

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXIST " + STUDENTS_TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        //create and write database with content provider
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }


    @Override
    public Cursor query( Uri uri,  String[] projection,
                         String selection,  String[] selectionArgs,
                         String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(STUDENTS_TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case STUDENT:
                queryBuilder.setProjectionMap(STUDENT_PROJECTION_MAP);
                break;
            case STUDENT_ID:
                queryBuilder.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
        }

        if (sortOrder == null || sortOrder == ""){
            //default sort to student name
            sortOrder = NAME;
        }

        Cursor cursor = queryBuilder.query(db,	projection,	selection,
                selectionArgs,null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType( Uri uri) {
        return null;
    }

    @Override
    public Uri insert( Uri uri,  ContentValues contentValues) {
        //add new student record
        long rowID = db.insert(STUDENTS_TABLE_NAME, "" , contentValues);
        //if record is update succesfully
        if (rowID > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri,null);
            return _uri;
        }
        throw new SQLException("failed to add data to " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case STUDENT:
                count = db.delete(STUDENTS_TABLE_NAME, selection, selectionArgs);
                break;

            case STUDENT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( STUDENTS_TABLE_NAME, _ID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? " " +
                       "AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case STUDENT:
                count = db.update(STUDENTS_TABLE_NAME, values, selection, selectionArgs);
                break;

            case STUDENT_ID:
                count = db.update(STUDENTS_TABLE_NAME, values,
                        _ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " " +
                        "AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
