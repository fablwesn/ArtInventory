package com.fablwesn.www.artinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.fablwesn.www.artinventory.data.ProductsContract.ProductEntry;
import com.fablwesn.www.artinventory.databinding.ActivityEditorBinding;
import com.fablwesn.www.artinventory.utility.LogMsgLib;
import com.fablwesn.www.artinventory.utility.Utils;

/**
 * TODO
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Activity binding to eliminate the need of findViewById
    private ActivityEditorBinding layoutBinding;

    // Stores the URI of product which is selected, if editing (null otherwise)
    private Uri currentlySelectedUri;

    // Stores the URI of the image picked for the product to add
    private Uri selectedImg;

    // changed by a touch listener to true, to see if changes have been made to an existing entry
    private boolean inputEntered = false;

    // path constant to set the image intent-type to
    private final static String INTENT_IMAGE_TYPE_PATH = "image/*";

    // Code to check before changing the image for the product to add
    private final static int REQUEST_CODE_IMG = 171;

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Override methods:
    */

    /**
     * OnCreate:
     * - set the layout binding
     * - check if in adding or editing state
     * - add listener for view touch for exit verification
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load binding layout
        layoutBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_editor);

        // Get the URI of selected product, if there is one
        Intent intent = getIntent();
        currentlySelectedUri = intent.getData();

        // Check if we are adding a new product or editing an existing one and set the title and menu actions accordingly
        if (currentlySelectedUri == null) {
            // set title
            setTitle(getString(R.string.editor_act_label_add));
            //Hide actions not required for this mode
            invalidateOptionsMenu();
        } else {
            // set title
            setTitle(getString(R.string.editor_act_label_edit));
            // start loading the data
            getLoaderManager().initLoader(1, null, this);
        }

        // remove focus from the EditTexts so the activity doesn't start with displayed input keyboard
        layoutBinding.editorCircImg.requestFocus();

        // check if one of the input fields have been touched (and therefore likely edited)
        setTouchListener();
    }

    /**
     * onPrepareOptionsMenu:
     * <p>
     * - hide the delete action adding and not editing
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //Hide delete action if mode is "Create new product"
        if (currentlySelectedUri == null) {
            MenuItem deleteItem = menu.findItem(R.id.menu_item_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    /**
     * onCreateOptionsMenu:
     * <p>
     * - Inflate the correct menu (adding action icons to the tool bar)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * onOptionsItemSelected:
     * <p>
     * 2 action available:
     * <p>
     * - save selected item ({@link CatalogActivity} and saving entry to db)
     * - delete selected item (returning to {@link CatalogActivity} and deleting entry)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                // validate and save if all input is correct
                saveEntry();
                return true;
            case R.id.menu_item_delete:
                // ask the user for confirmation and delete the entry and close the activity on confirmation
                Utils.confirmSingleDeletion(this, currentlySelectedUri);
                return true;
            case android.R.id.home:
                // ask the user for confirmation without saving anything
                if (!inputEntered) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                } else
                    confirmCancelInput();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onBackPressed:
     * called when the user wants to exit the activity
     */
    @Override
    public void onBackPressed() {
        // If nothing has changed for sure, exit as usual
        if (!inputEntered) {
            super.onBackPressed();
            return;
        }

        // if there might have been some input, ask if user wants to discard them when leaving
        confirmCancelInput();
    }

    /**
     * onActivityResult:
     * Run after the user returns from the product image selection (device's image library)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // safely add the selected image or inform the user about an error
        if (requestCode == REQUEST_CODE_IMG && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                layoutBinding.editorEmptyPicText.setVisibility(View.VISIBLE);
                Toast.makeText(this, getString(R.string.editor_error_img_toast), Toast.LENGTH_SHORT).show();
                return;
            }

            // hide the error
            layoutBinding.editorEmptyPicText.setVisibility(View.GONE);
            //Store the URI of picked image
            selectedImg = data.getData();
            //Set it on the ImageView
            layoutBinding.editorCircImg.setImageURI(selectedImg);
        }
    }

    /**
     * onCreateLoader:
     * <p>
     * - creates a projection String[] containing all query params and starts loading it from the db
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //We need all columns
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

        return new CursorLoader(this, currentlySelectedUri, projection, null, null, null);
    }

    /**
     * onLoadFinished:
     * <p>
     * - fill in the views with correct data from the db query
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            //Present data to user
            layoutBinding.editorEdtName.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME)));
            layoutBinding.editorEdtBrand.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_BRAND)));
            layoutBinding.editorEdtPrice.setText(String.valueOf(cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE))));
            layoutBinding.editorEdtStock.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_STOCK))));

            int discount = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_DISCOUNT));

            // check the cb
            layoutBinding.editorCbDiscount.setChecked(true);
            Utils.toggleSpinner(layoutBinding.editorSpinnerDiscount, true);

            // set the correct selection or uncheck when empty
            switch (discount) {
                case 0:
                    layoutBinding.editorCbDiscount.setChecked(false);
                    Utils.toggleSpinner(layoutBinding.editorSpinnerDiscount, false);
                    break;
                case 10:
                    layoutBinding.editorSpinnerDiscount.setSelection(0);
                    break;
                case 25:
                    layoutBinding.editorSpinnerDiscount.setSelection(1);
                    break;
                case 50:
                    layoutBinding.editorSpinnerDiscount.setSelection(2);
                    break;
                case 75:
                    layoutBinding.editorSpinnerDiscount.setSelection(3);
                    break;
                default:
                    break;
            }

            Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE)));
            layoutBinding.editorCircImg.setImageURI(uri);
            selectedImg = uri;

            layoutBinding.editorEdtSuppName.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME)));
            layoutBinding.editorEdtSuppMail.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL)));
            layoutBinding.editorEdtSuppPhone.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE)));
        }

    }

    /**
     * onLoaderReset:
     * empty all views
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        layoutBinding.editorEdtName.setText("");
        layoutBinding.editorEdtBrand.setText("");
        layoutBinding.editorEdtPrice.setText("");
        layoutBinding.editorEdtStock.setText("");

        layoutBinding.editorCbDiscount.setChecked(false);

        layoutBinding.editorCircImg.setImageURI(null);

        layoutBinding.editorEdtSuppName.setText("");
        layoutBinding.editorEdtSuppMail.setText("");
        layoutBinding.editorEdtSuppPhone.setText("");
    }

    /*____________________________________________________________________________________________*/

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Public XML onClick methods:
    */

    /**
     * opens the devices media library to add a photo for the single product
     *
     * @param circImgView image view getting the listener
     */
    public void addImage(View circImgView) {
        // Create a new intent
        Intent intent;

        // Depending on the API Level the device is running on, prepare the intent
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(INTENT_IMAGE_TYPE_PATH);
        // start the new activity (device's image library) and store the result (image picked)
        startActivityForResult(intent, REQUEST_CODE_IMG);
    }

    /**
     * onClick for the CB
     * enable the discount spinner when checked
     * disable the discount when unchecked
     *
     * @param discountCb discount checkbox
     */
    public void toggleDiscount(View discountCb) {
        if (((CheckBox) discountCb).isChecked()) {
            Utils.toggleSpinner(layoutBinding.editorSpinnerDiscount, true);
        } else {
            Utils.toggleSpinner(layoutBinding.editorSpinnerDiscount, false);
        }
    }

    /*____________________________________________________________________________________________*/

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Private helper methods:
    */

    /**
     * Add a touch listener on all views, setting the inputEntered variable to true so we can ask for confirmation when exiting
     */
    private void setTouchListener() {
        View.OnTouchListener inputTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                inputEntered = true;
                return false;
            }
        };

        // set on all views
        layoutBinding.editorCircImg.setOnTouchListener(inputTouchListener);
        layoutBinding.editorEdtName.setOnTouchListener(inputTouchListener);
        layoutBinding.editorEdtBrand.setOnTouchListener(inputTouchListener);
        layoutBinding.editorEdtPrice.setOnTouchListener(inputTouchListener);
        layoutBinding.editorCbDiscount.setOnTouchListener(inputTouchListener);
        layoutBinding.editorSpinnerDiscount.setOnTouchListener(inputTouchListener);
        layoutBinding.editorEdtStock.setOnTouchListener(inputTouchListener);
        layoutBinding.editorEdtSuppName.setOnTouchListener(inputTouchListener);
        layoutBinding.editorEdtSuppMail.setOnTouchListener(inputTouchListener);
        layoutBinding.editorEdtSuppPhone.setOnTouchListener(inputTouchListener);
    }

    /**
     * check the input and if it's valid, prepare them for storage into the db and store them
     * exits the activity on success
     */
    private void saveEntry() {
        // validate the input and store them in variables
        String name = Utils.validateTextInput(this, layoutBinding.editorEdtName);
        String brand = Utils.validateTextInput(this, layoutBinding.editorEdtBrand);
        String supplierName = Utils.validateTextInput(this, layoutBinding.editorEdtSuppName);

        float regularPrice = Utils.validatePriceInput(this, layoutBinding.editorEdtPrice);
        int stockQuantity = Utils.validateStockInput(this, layoutBinding.editorEdtStock);

        String supplierMail = Utils.validateMailInput(this, layoutBinding.editorEdtSuppMail);

        String supplierPhone = Utils.validatePhoneInput(this, layoutBinding.editorEdtSuppPhone);

        int discount = 0;

        /* check for invalid input and return early when at least one needed entry was invalid/missing */

        // Text Verification
        if (name == null ||
                brand == null ||
                supplierName == null ||
                supplierMail == null ||
                supplierPhone == null)
            return;

        // Numbers Verification
        if (regularPrice == -1 || stockQuantity == -1)
            return;

        // Image Verification
        if (selectedImg == null) {
            Toast.makeText(this, R.string.editor_edt_empty_img, Toast.LENGTH_SHORT).show();
            layoutBinding.editorEmptyPicText.setVisibility(View.VISIBLE);
            return;
        } else
            layoutBinding.editorEmptyPicText.setVisibility(View.GONE);

        // if discount has been selected, check it's value and assign it to the var
        if (layoutBinding.editorCbDiscount.isChecked()) {
            try {
                discount = Integer.parseInt(layoutBinding.editorSpinnerDiscount.getSelectedItem().toString().replace("%", ""));
            } catch (NumberFormatException nfe) {
                System.out.println(LogMsgLib.EDITOR_PARSE_DISCOUNT + nfe);
            }
        }

        /* if all inputs are valid, create a key-value par for the db with them as the values */
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_BRAND, brand);
        values.put(ProductEntry.COLUMN_PRICE, regularPrice);
        values.put(ProductEntry.COLUMN_DISCOUNT, discount);
        values.put(ProductEntry.COLUMN_STOCK, stockQuantity);
        values.put(ProductEntry.COLUMN_IMAGE, selectedImg.toString());
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierMail);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);

        // check if we are creating or updating
        if (currentlySelectedUri == null)
        // insert new data and inform the user about the result
        {
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (uri != null) {
                Toast.makeText(this, getString(R.string.editor_save_succeeded_toast), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_save_failed_toast), Toast.LENGTH_SHORT).show();
            }
        } else
        // update data and inform the user about the result
        {
            int numRows = getContentResolver().update(currentlySelectedUri, values, null, null);

            if (numRows > 0) {
                Toast.makeText(this, getString(R.string.editor_update_succeeded_toast), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_failed_toast), Toast.LENGTH_SHORT).show();
            }
        }
        // Exit activity
        finish();
    }


    /**
     * asks the user if he really wishes to leave the activity without saving anything, if any of the views have been touched
     * <p>
     * - Positive -> return to {@link CatalogActivity without saving}
     * - Negative -> close dialog and do nothing
     */
    private void confirmCancelInput() {
        DialogInterface.OnClickListener confirmDiscardInput = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
            }
        };

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.editor_confirmation_dismiss_changes);
        alertBuilder.setPositiveButton(R.string.editor_confirmation_dismiss_changes_pos, confirmDiscardInput);
        alertBuilder.setNegativeButton(R.string.editor_confirmation_dismiss_changes_neg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog dismissDialog = alertBuilder.create();
        dismissDialog.show();
    }


    /*____________________________________________________________________________________________*/
}
