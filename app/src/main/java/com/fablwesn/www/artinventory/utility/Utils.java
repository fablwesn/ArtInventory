package com.fablwesn.www.artinventory.utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Spinner;
import android.widget.Toast;

import com.fablwesn.www.artinventory.R;

/**
 * Misc. helper methods needed fpr the application.
 */
public class Utils {

    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* View-related helper methods:
    */

    /**
     * enable or disable a spinner
     *
     * @param spinner   spinner to make the changes to
     * @param isVisible boolean deciding to hide or show
     */
    public static void toggleSpinner(Spinner spinner, boolean isVisible) {
        if (isVisible) {
            spinner.setEnabled(true);
            spinner.setClickable(true);
            spinner.setAlpha(1.0f);
        } else {
            spinner.setEnabled(false);
            spinner.setClickable(false);
            spinner.setAlpha(0.1f);
        }
    }

    /**
     * Creates an AlertDialog asking for confirmation to delete a single db entry
     * <p>
     * - Positive -> delete entry and exit activity
     * - Negative -> closes dialog with no consequences
     *
     * @param context         of the activity
     * @param selectedProduct uri to delete
     */
    public static void confirmSingleDeletion(final Context context, final Uri selectedProduct) {
        // create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // set the correct message
        builder.setMessage(context.getResources().getString(R.string.catalog_confirmation_delete_single));

        // delete on positive response
        builder.setPositiveButton(context.getResources().getString(R.string.catalog_confirmation_delete_single_pos), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int rowsDeleted = context.getContentResolver().delete(selectedProduct, null, null);
                // Show a toast message depending on whether or not the delete was successful.
                if (rowsDeleted == 0) {
                    // If no rows were deleted, then display an error
                    Toast.makeText(context, context.getResources().getString(R.string.catalog_data_clear_failed_toast),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, display a success messaGE
                    Toast.makeText(context, context.getResources().getString(R.string.catalog_data_cleared_toast),
                            Toast.LENGTH_SHORT).show();
                }
                // finish activity
                ((Activity) context).finish();
            }
        });

        // return on negative response
        builder.setNegativeButton(R.string.confirmation_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /*____________________________________________________________________________________________*/


    /*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯*/
    /* Input Validation:
    */

    /**
     * check if the inputs inside the Name, Brand or Supplier-Name EditText are valid
     *
     * @param context  to get the resource strings
     * @param editText to verify the input from
     * @return accepted input or null if not accepted
     */
    public static String validateTextInput(Context context, TextInputEditText editText) {
        // store input
        final String rawText = editText.getText().toString().trim();

        if (TextUtils.isEmpty(rawText)) {
            editText.setError(context.getResources().getString(R.string.editor_edt_empty_input));
            return null;
        }

        // check for invalid characters
        final String pattern = "^[a-zA-Z0-9öäü ]*$";

        // if invalid chars were inside, inform the user
        if (!rawText.matches(pattern)) {
            editText.setError(context.getResources().getString(R.string.editor_edt_invalid_input));
            return null;
        } else {
            return rawText;
        }
    }

    /**
     * check if a valid price has been entered
     *
     * @param context  to get the resource strings
     * @param editText to verify the input from
     * @return accepted input or -1 if not accepted
     */
    public static float validatePriceInput(Context context, TextInputEditText editText) {
        // store trimmed raw input as a string
        final String rawPrice = editText.getText().toString().trim();

        // check for emptiness and signal user about it
        if (TextUtils.isEmpty(rawPrice)) {
            editText.setError(context.getResources().getString(R.string.editor_edt_empty_input));
            return -1;
        }

        // save the input as a float (we check for numbers in xml through inputType)
        try {
            float floatPrice = Float.parseFloat(rawPrice);

            if (floatPrice <= 0) {
                editText.setError(context.getResources().getString(R.string.editor_edt_invalid_input));
                return 0;
            }

            return floatPrice;

        } catch (NumberFormatException e) {
            editText.setError(context.getResources().getString(R.string.editor_edt_invalid_input));
            return -1;
        }
    }

    /**
     * check if no invalid input has been entered (no input accepted, will use 0 as a default value)
     *
     * @param context  to get the resource strings
     * @param editText to verify the input from
     * @return accepted input or 0 if not accepted
     */
    public static int validateStockInput(Context context, TextInputEditText editText) {
        // store input
        final String rawQuantity = editText.getText().toString().trim();

        // return 0 as a default value
        if (TextUtils.isEmpty(rawQuantity))
            return 0;

        try {
            int quantityInt = Integer.parseInt(editText.getText().toString().trim());
            if (quantityInt < 0) {
                editText.setError(context.getResources().getString(R.string.editor_edt_invalid_input));
                return -1;
            }
            return quantityInt;
        } catch (NumberFormatException nfe) {
            editText.setError(context.getResources().getString(R.string.editor_edt_invalid_input));
            return -1;
        }
    }

    /**
     * check if a valid email address has been entered
     *
     * @param context  to get the resource strings
     * @param editText to verify the input from
     * @return accepted input or null if not accepted
     */
    public static String validateMailInput(Context context, TextInputEditText editText) {
        // store input
        String rawMail = editText.getText().toString().trim();

        if (TextUtils.isEmpty(rawMail)) {
            editText.setError(context.getResources().getString(R.string.editor_edt_empty_input));
            return null;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(rawMail).matches()) {
            editText.setError(context.getResources().getString(R.string.editor_edt_invalid_input));
            return null;
        }

        return rawMail;
    }

    /**
     * verify if a correct phone number format has been entered
     *
     * @param context  to get the resource strings
     * @param editText to verify the input from
     * @return accepted input or null if not accepted
     */
    public static String validatePhoneInput(Context context, TextInputEditText editText) {
        // store input
        String rawPhone = editText.getText().toString().trim();

        if (TextUtils.isEmpty(rawPhone)) {
            editText.setError(context.getResources().getString(R.string.editor_edt_empty_input));
            return null;
        }

        if (!Patterns.PHONE.matcher(rawPhone).matches()) {
            editText.setError(context.getResources().getString(R.string.editor_edt_invalid_input));
            return null;
        }

        return rawPhone;
    }

    /*____________________________________________________________________________________________*/
}
