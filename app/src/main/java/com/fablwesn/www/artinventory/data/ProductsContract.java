package com.fablwesn.www.artinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the app.
 */
public class ProductsContract {

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Base Contract:
    */

    // Name for the content provider.
    static final String CONTENT_AUTHORITY = "com.fablwesn.www.artinventory";

    // base of all URI's to contact the provider
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
        possible paths (appended to base content URI for possible URI's) */

    // path containing the the table with the products stored
    // (content://com.fablwesn.www.artinventory/inventory)
    static final String PATH_SALE = "sale";

    /**
     * Empty constructor preventing accidental instantiation.
     */
    private ProductsContract() {
    }

    /*____________________________________________________________________________________________*/

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Inventory Table:
    */

    /**
     * Inner class that defines constant values for the product database table.
     * Each entry in the table represents a product for sale.
     */
    public static final class ProductEntry implements BaseColumns {

        // The content URI to access the product data in the provider.
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SALE);

        // The MIME type of the CONTENT_URI for a list of products.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE
                        + "/" + CONTENT_AUTHORITY + "/" + PATH_SALE;

        // The MIME type of the CONTENT_URI for a single product.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE
                        + "/" + CONTENT_AUTHORITY + "/" + PATH_SALE;

        // Name of database table for products.
        public final static String TABLE_NAME = "products";


        /*
           table columns */

        /* Unique ID integer for the product (only for use in the database table).
           Type: INTEGER PRIMARY KEY AUTOINCREMENT
         */
        public final static String _ID = BaseColumns._ID;

        /* Product's name.
           Type: TEXT NOT NULL
         */
        public static final String COLUMN_NAME = "name";


        /* Product's Brand
           Type: TEXT NOT NULL
         */
        public static final String COLUMN_BRAND = "brand";

        /* Product's Price
           Type: REAL NOT NULL
         */
        public static final String COLUMN_PRICE = "price";

        /* Discount (in %)
           Type: INTEGER DEFAULT 0
         */
        public static final String COLUMN_DISCOUNT = "discount";

        /* Stock available
           Type: INTEGER NOT NULL
         */
        public static final String COLUMN_STOCK = "quantity";

        /* Image Uri of the fruit
           Type:TEXT NOT NULL
         */
        public static final String COLUMN_IMAGE = "imgUri";

        /* Product's deliverer name
           Type:TEXT NOT NULL
         */
        public static final String COLUMN_SUPPLIER_NAME = "delivererName";

        /* Product's deliverer email address
           Type:TEXT NOT NULL
         */
        public static final String COLUMN_SUPPLIER_EMAIL = "delivererMail";

        /* Product's deliverer telephone number
           Type:TEXT NOT NULL
         */
        public static final String COLUMN_SUPPLIER_PHONE = "delivererTel";
    }

    /*____________________________________________________________________________________________*/
}
