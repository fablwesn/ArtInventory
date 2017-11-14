package com.fablwesn.www.artinventory.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fablwesn.www.artinventory.data.ProductsContract.ProductEntry;
import com.fablwesn.www.artinventory.utility.LogMsgLib;

/**
 * {@link ContentProvider} for the app.
 */
public class ProductsProvider extends ContentProvider {
    /* Tag used for logging. */
    private static final String LOG_TAG = ProductsProvider.class.getSimpleName();

    /* used for building the selection working with specific entries by id */
    private static final String PRODUCT_ID_SECTION_APPENDIX = "=?";

    /* Database helper object */
    private ProductsDbHelper dbHelper;

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Implemented Methods:
    */

    /**
     * OnCreate:
     *
     * @return true after initializing the db helper object.
     */
    @Override
    public boolean onCreate() {
        dbHelper = new ProductsDbHelper(getContext());
        return true;
    }

    /**
     * Insert new data into the database with the given content values into the correct table.
     *
     * @param uri    where to insert
     * @param values what to insert
     * @return new content URI for that specific row in the database.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(PRODUCTS, uri, values);
            default:
                throw new IllegalArgumentException(LogMsgLib.PROVIDER_INSERT_URI_ERROR + uri);
        }
    }

    /**
     * Method to read from the database.
     *
     * @param uri           to query from
     * @param projection    the columns wanted
     * @param selection     where to look
     * @param selectionArgs arguments of where, preventing SQL injection
     * @param sortOrder     order in which the returned results should appear
     * @return Cursor object containing the requested data
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase sqlDatabase = dbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher matches the URI to a specific code and act accordingly
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case PRODUCTS:
                // query the whole products table.
                cursor = sqlDatabase.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // set the correct selection argument
                selection = ProductEntry._ID + PRODUCT_ID_SECTION_APPENDIX;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // query the single product in question from the products table.
                cursor = sqlDatabase.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(LogMsgLib.PROVIDER_QUERY_NO_MATCH + uri);
        }

        // Set notification URI on the Cursor
        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        else
            throw new NullPointerException(LogMsgLib.PROVIDER_NO_CONTEXT);

        // Return the cursor
        return cursor;
    }

    /**
     * Update data in the database with the given content values.
     *
     * @param uri           of where to update
     * @param values        new values to add
     * @param selection     where clause
     * @param selectionArgs arguments of the where clause
     * @return int of rows that have been updated
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // look for a match and act accordingly
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(PRODUCTS, uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + PRODUCT_ID_SECTION_APPENDIX;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(PRODUCTS, uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(LogMsgLib.PROVIDER_UPDATE_URI_ERROR + uri);
        }
    }

    /**
     * Delete multiple or single items from the products or vegetables table.
     *
     * @param uri           pointing to the product(s) to be deleted
     * @param selection     where to look
     * @param selectionArgs arguments for the where clause
     * @return int of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase sqlDatabase = dbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        // Look if the uri is valid and act accordingly.
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = sqlDatabase.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + PRODUCT_ID_SECTION_APPENDIX;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqlDatabase.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(LogMsgLib.PROVIDER_DELETE_URI_ERROR + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null);
            else
                throw new NullPointerException(LogMsgLib.PROVIDER_NO_CONTEXT);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Handles requests for the MIME type.
     *
     * @param uri to query
     * @return the MIME type of data in the content provider
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // check for match and act accordingly
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(LogMsgLib.PROVIDER_GET_TYPE_URI_ERROR + uri + LogMsgLib.PROVIDER_GET_TYPE_MATCH_ERROR + match);
        }
    }

    /*____________________________________________________________________________________________*/


    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* URI Matcher:
    */

    /* URI matcher code for the content uri for the products table. */
    private static final int PRODUCTS = 201;
    /* URI matcher code for the content uri for a single product from the products table. */
    private static final int PRODUCT_ID = 202;
    /* Placeholder for the id */
    private static final String PATH_ID_PLACEHOLDER = "/#";
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    /*
     * Initializer.
     */
    static {

        // add single and all products to the matcher
        URI_MATCHER.addURI(ProductsContract.CONTENT_AUTHORITY, ProductsContract.PATH_SALE, PRODUCTS);
        URI_MATCHER.addURI(ProductsContract.CONTENT_AUTHORITY, ProductsContract.PATH_SALE + PATH_ID_PLACEHOLDER, PRODUCT_ID);
    }

    /*____________________________________________________________________________________________*/

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Utility Methods:
    */

    /**
     * Helper method for adding a row inside a table
     *
     * @param table  which to add to
     * @param uri    of the table
     * @param values of data to be inserted
     * @return Uri object pointing to the row added
     */
    private Uri insertProduct(int table, Uri uri, ContentValues values) {
        // Get writable database
        SQLiteDatabase sqlDatabase = dbHelper.getWritableDatabase();

        // id for the newly created row
        long newId;

        // insert into the correct table
        switch (table) {
            case PRODUCTS:
                newId = sqlDatabase.insert(ProductEntry.TABLE_NAME, null, values);
                break;
            default:
                // -1 refers to an error
                newId = -1;
                break;
        }

        // check if there was an error adding the data
        if (newId == -1) {
            Log.e(LOG_TAG, LogMsgLib.PROVIDER_INSERT_ROW_ERROR + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the content URI
        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        else
            throw new NullPointerException(LogMsgLib.PROVIDER_NO_CONTEXT);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, newId);
    }

    /**
     * Helper method for updating a row in database
     *
     * @param uri           of the product
     * @param values        new values
     * @param selection     the where clause
     * @param selectionArgs arguments of the where clause
     * @return int of rows updated
     */
    private int updateProduct(int table, Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //Return early if no data is changed
        if (values.size() == 0) {
            return 0;
        }

        // get the db
        SQLiteDatabase sqlDatabase = dbHelper.getWritableDatabase();

        // will store the number of rows updated
        int rowsUpdated;

        // choose correct table
        switch (table) {
            case PRODUCTS:
                rowsUpdated = sqlDatabase.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                rowsUpdated = 0;
                break;
        }

        // if there was a change, notify
        if (rowsUpdated != 0) {
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null);
            else
                throw new NullPointerException(LogMsgLib.PROVIDER_NO_CONTEXT);
        }

        // return number of rows updated
        return rowsUpdated;
    }

    /*____________________________________________________________________________________________*/
}
