package com.fablwesn.www.artinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.fablwesn.www.artinventory.data.ProductsContract.ProductEntry;
import com.fablwesn.www.artinventory.databinding.ActivityCatalogBinding;

/**
 * HomeActivity of the app.
 * displays a list of all available db entries and gives 4 interactive possibilities:
 * <p>
 * - Add db entry, starting the {@link EditorActivity}
 * - clear all entries inside the db
 * - decrease stock from any list item displayed by 1
 * - open a detailed view of the item clicked {@link DetailsActivity}
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Activity binding to eliminate the need of findViewById
    private ActivityCatalogBinding layoutBinding;

    // Adapter attached to the listView
    private DbCursorAdapter cursorAdapter;

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
                DataBindingUtil.setContentView(this, R.layout.activity_catalog);

        cursorAdapter = new DbCursorAdapter(this, null);
        layoutBinding.catalogListView.setAdapter(cursorAdapter);
        layoutBinding.catalogListView.setEmptyView(layoutBinding.catalogEmptyLinlay);

        //Start loader
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * onCreateOptionsMenu:
     * <p>
     * - Inflate the correct menu (adding action icons to the tool bar)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    /**
     * onOptionsItemSelected:
     * <p>
     * - only 1 action available, clearing the whole db
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {
            clearDbConfirmation();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    /**
     * onCreateLoader:
     * <p>
     * - creates a projection String[] containing the query params we want and starts loading it from the db
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Only for columns required for our ListView
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_STOCK
        };
        return new CursorLoader(getApplicationContext(), ProductEntry.CONTENT_URI, projection,
                null, null, null);

    }

    /**
     * onLoadFinished:
     * <p>
     * - fill the adapter with the new data
     * - set click listener on each list item from the list, opening the {@link DetailsActivity}
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);

        layoutBinding.catalogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Uri selectedUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, l);
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
                intent.setData(selectedUri);
                startActivity(intent);
            }
        });
    }

    /**
     * onLoaderReset:
     * <p>
     * - clear the adapter
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    /*____________________________________________________________________________________________*/

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Public XML onClick methods:
    */

    /**
     * FAB's public onClick method from the layout, starting the {@link EditorActivity}
     *
     * @param fab at the bottom right of the catalog activity
     */

    public void addEntry(View fab) {
        startActivity(new Intent(CatalogActivity.this, EditorActivity.class));
    }

    /*____________________________________________________________________________________________*/

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Private helper methods:
    */

    /**
     * Displays a confirmation dialog asking the user if he really wishes to delete all entries inside the db
     * <p>
     * - Positive -> clears the db
     * - Negative -> Closes dialog with no consequences
     */
    private void clearDbConfirmation() {
        // start building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set message displayed
        builder.setMessage(R.string.catalog_confirmation_delete_all);
        // delete on positive
        builder.setPositiveButton(R.string.catalog_confirmation_delete_all_pos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int numRows = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);

                if (numRows > 0)
                    Toast.makeText(getApplicationContext(), R.string.catalog_data_cleared_toast, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.catalog_data_clear_failed_toast, Toast.LENGTH_SHORT).show();

            }
        });
        // return on negative
        builder.setNegativeButton(R.string.confirmation_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create an show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /*____________________________________________________________________________________________*/

}
