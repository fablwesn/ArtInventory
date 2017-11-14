package com.fablwesn.www.artinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fablwesn.www.artinventory.data.ProductsContract.ProductEntry;
import com.fablwesn.www.artinventory.databinding.ActivityDetailsBinding;
import com.fablwesn.www.artinventory.utility.Utils;

import java.text.NumberFormat;

/**
 * Displayed when clicking on a list item inside {@link CatalogActivity} displaying it's details
 * displays all information available about the selected Product, also giving the user 6 possibilities of interaction:
 * <p>
 * - Edit db entry, starting the {@link EditorActivity}
 * - Delete the entry inside the db
 * - decrease stock from the displayed item
 * - increase stock from the displayed item
 * - start mail intent contacting the supplier
 * - start phone intent contacting the supplier
 */
public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Activity binding to eliminate the need of findViewById
    private ActivityDetailsBinding layoutBinding;

    // Uri of the selected product, if coming from the DetailsActivity
    private Uri selectedProduct;

    // The product quantity available in stock
    private int stockNumber;

    // Supplier contact info
    private String supplierMail;
    private String supplierPhone;

    // used for mailing/calling intents
    final private String MAIL_INTENT_TYPE = "*/*";
    final private String PHONE_INTENT_URI_PREFIX = "tel:";

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Override methods:
    */

    /**
     * OnCreate:
     * <p>
     * - binds layout
     * - sets cursorAdapter and emptyView to the list
     * - starts loader
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load binding layout
        layoutBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_details);

        //Get the URI of the product clicked from previous activity
        Intent intent = getIntent();
        selectedProduct = intent.getData();

        //Start loader
        getLoaderManager().initLoader(2, null, this);
    }

    /**
     * onCreateOptionsMenu:
     * <p>
     * - Inflate the correct menu (adding action icons to the tool bar)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    /**
     * onOptionsItemSelected:
     * <p>
     * 2 action available:
     * <p>
     * - edit selected item (starting {@link EditorActivity})
     * - delete selected item (returning to {@link CatalogActivity})
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_update:
                // open the editor activity and pass in the product's db uri
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(selectedProduct);
                startActivity(intent);
                return true;
            case R.id.menu_item_delete:
                // ask the user for confirmation and delete the entry and close the activity on confirmation
                Utils.confirmSingleDeletion(this, selectedProduct);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onCreateLoader:
     * <p>
     * - creates a projection String[] containing the query params we want and starts loading it from the db
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We want all columns this time
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_BRAND,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_DISCOUNT,
                ProductEntry.COLUMN_STOCK,
                ProductEntry.COLUMN_IMAGE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_SUPPLIER_PHONE
        };
        // start loading with set uri
        return new CursorLoader(this, selectedProduct, projection, null, null, null);
    }

    /**
     * onLoadFinished:
     * <p>
     * - fill in the views with correct data from the db query
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // return early if there's nothing to show
        if (cursor == null || cursor.getCount() < 1)
            return;

        //If the cursor has some data, move through the entries and set all the views accordingly
        if (cursor.moveToFirst()) {
            // set image
            layoutBinding.detailsCircImage.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE))));

            // set name
            layoutBinding.detailsTextName.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME)));
            // set brand
            layoutBinding.detailsTextBrand.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_BRAND)));

            // get currency
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
            // set price
            layoutBinding.detailsTextPrice.setText(currencyInstance.format(cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE))));


            // set discount if there is one, otherwise hide the view
            int discount = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_DISCOUNT));
            if (discount == 0)
                layoutBinding.detailsTextDiscount.setVisibility(View.GONE);
            else {
                layoutBinding.detailsTextDiscount.setVisibility(View.VISIBLE);
                layoutBinding.detailsTextDiscount.setText(getString(R.string.details_discount, discount));
            }

            // set the stock number
            stockNumber = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_STOCK));
            layoutBinding.detailsTextStock.setText(String.valueOf(stockNumber));

            // set Supplier info
            layoutBinding.detailsTextSuppName.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME)));
            supplierMail = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL));
            layoutBinding.detailsTextSuppMail.setText(supplierMail);
            supplierPhone = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE));
            layoutBinding.detailsTextSuppPhone.setText(supplierPhone);
        }
    }

    /**
     * onLoaderReset:
     * empty
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /*____________________________________________________________________________________________*/

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Public XML onClick methods:
    */


    /**
     * Increment- and Decrement-Button's listener doing their respective job
     *
     * @param btn +/- stock
     */
    public void editStock(View btn) {
        // depending on which was pressed, update the stock accordingly
        switch (btn.getId()) {
            case R.id.details_img_incr:
                stockNumber++;
                break;
            case R.id.details_img_decr:
                // don't let it go below 0
                if (stockNumber != 0)
                    stockNumber--;
                break;
        }

        // update the db with the new values
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_STOCK, stockNumber);
        getContentResolver().update(selectedProduct, contentValues, null, null);
    }

    /**
     * opens the user-preferred mail app to contact the supplier
     *
     * @param mailBtn inside the layout
     */
    public void mailSupplier(View mailBtn) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MAIL_INTENT_TYPE);
        // add the recipient
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{supplierMail});
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

    /**
     * opens the phone app to contact the supplier
     *
     * @param callBtn inside the layout
     */
    public void callSupplier(View callBtn) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(PHONE_INTENT_URI_PREFIX + supplierPhone));
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

    /*____________________________________________________________________________________________*/
}
