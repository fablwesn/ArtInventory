package com.fablwesn.www.artinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fablwesn.www.artinventory.data.ProductsContract.ProductEntry;

/**
 * Database helper for the app. Manages database creation and version management.
 */
class ProductsDbHelper extends SQLiteOpenHelper {

    /* Tag used for logging. */
    private static final String LOG_TAG = ProductsDbHelper.class.getSimpleName();

    /* Name of the database file. */
    private static final String DATABASE_NAME = "artinventory.db";

    /* Database version. If you change the database schema, you must increment the database version. */
    private static final int DATABASE_VERSION = 1;

    /* SQL Command to delete a table (user for different db versions inside onUpgrade). */
    private static final String SQL_COMMAND_DELETE_TABLE = "DROP TABLE IF EXISTS ";


    /* Basic SQL-Command chars */
    private static final String SQL_COMMAND_SEPARATOR = ",";
    private static final String SQL_COMMAND_START_SCHEME = " (";
    private static final String SQL_COMMAND_END_SCHEME = ");";

    /* SQL Command to create a table */
    private static final String SQL_COMMAND_CREATE_TABLE = "CREATE TABLE ";


    /* SQL Command Types */
    private static final String SQL_COMMAND_TYPE_INTEGER = " INTEGER ";
    private static final String SQL_COMMAND_TYPE_TEXT = " TEXT ";
    private static final String SQL_COMMAND_TYPE_REAL = " REAL ";


    /* SQL Command Primary Key */
    private static final String SQL_COMMAND_PRIMARY_KEY = "PRIMARY KEY AUTOINCREMENT";
    /* SQL Command not null condition */
    private static final String SQL_COMMAND_NOT_NULL = "NOT NULL";
    /* SQL Command default 0 condition */
    private static final String SQL_COMMAND_DEFAULT_0 = "DEFAULT 0";

    /**
     * Constructs a new instance of {@link ProductsDbHelper}.
     *
     * @param context of the app
     */
    ProductsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the command.
        final String sqlCommand = createSqlTablesCommand();
        // Execute the SQL statements to create the table.
        db.execSQL(sqlCommand);
        // Log the command for debugging
        Log.v(LOG_TAG, sqlCommand);
    }

    /**
     * Called when the database scheme needs to be updated.
     * IMPORTANT: increment constant DATABASE_VERSION before updating!!
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_COMMAND_DELETE_TABLE + ProductEntry.TABLE_NAME);
        onCreate(db);
    }

    /**
     * @return String containing SQLite Command creating the products table.
     */
    private String createSqlTablesCommand() {

        return SQL_COMMAND_CREATE_TABLE + ProductEntry.TABLE_NAME + SQL_COMMAND_START_SCHEME
                + ProductEntry._ID + SQL_COMMAND_TYPE_INTEGER + SQL_COMMAND_PRIMARY_KEY + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_NAME + SQL_COMMAND_TYPE_TEXT + SQL_COMMAND_NOT_NULL + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_BRAND + SQL_COMMAND_TYPE_TEXT + SQL_COMMAND_NOT_NULL + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_PRICE + SQL_COMMAND_TYPE_REAL + SQL_COMMAND_NOT_NULL + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_DISCOUNT + SQL_COMMAND_TYPE_INTEGER + SQL_COMMAND_DEFAULT_0 + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_STOCK + SQL_COMMAND_TYPE_INTEGER + SQL_COMMAND_DEFAULT_0 + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_IMAGE + SQL_COMMAND_TYPE_TEXT + SQL_COMMAND_NOT_NULL + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_SUPPLIER_NAME + SQL_COMMAND_TYPE_TEXT + SQL_COMMAND_NOT_NULL + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_SUPPLIER_EMAIL + SQL_COMMAND_TYPE_TEXT + SQL_COMMAND_NOT_NULL + SQL_COMMAND_SEPARATOR
                + ProductEntry.COLUMN_SUPPLIER_PHONE + SQL_COMMAND_TYPE_TEXT + SQL_COMMAND_NOT_NULL
                + SQL_COMMAND_END_SCHEME;
    }
}
