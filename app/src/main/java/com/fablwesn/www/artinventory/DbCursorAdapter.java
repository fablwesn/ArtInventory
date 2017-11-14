package com.fablwesn.www.artinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fablwesn.www.artinventory.data.ProductsContract.ProductEntry;

import java.text.NumberFormat;

/**
 * Adapter used to fill the list inside the {@link CatalogActivity} using data from the database {@link com.fablwesn.www.artinventory.data.ProductsContract}
 */
class DbCursorAdapter extends CursorAdapter {

    /**
     * Constructor
     *
     * @param context of the activity
     * @param cursor  to add the adapter to
     */
    DbCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Instantiate a empty view to use
     *
     * @param context of the activity
     * @param cursor  used
     * @param parent  where to attach the layout to
     * @return empty list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Add data to the list view
     *
     * @param view    list item selected
     * @param context of the activity
     * @param cursor  used
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // get the data we want to attach
        final String name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME));
        final float price = cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_STOCK));
        final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

        // get the views that will be holding the data
        final TextView nameText = view.findViewById(R.id.list_item_name_text);
        final TextView priceText = view.findViewById(R.id.list_item_price_text);
        final TextView quantText = view.findViewById(R.id.list_item_quantity_text);

        // set the name
        nameText.setText(name);

        // set the price with the correct currency
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
        priceText.setText(currencyInstance.format(price));

        // set the quantity
        quantText.setText(String.valueOf(quantity));

        // set the listener to decrement stock button
        View soldBtn = view.findViewById(R.id.list_item_sold_btn);
        soldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 0) {
                    int newQuantity = quantity - 1;
                    Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);

                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_STOCK, newQuantity);
                    context.getContentResolver().update(productUri, values, null, null);
                    Toast.makeText(context, context.getString(R.string.catalog_sold_success_toast), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.catalog_sold_error_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}