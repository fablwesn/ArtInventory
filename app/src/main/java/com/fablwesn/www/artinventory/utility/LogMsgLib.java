package com.fablwesn.www.artinventory.utility;

/**
 * Holding all text constants used to display log warnings/errors/...
 */
public class LogMsgLib {

    /* No context found. */
    public final static String PROVIDER_NO_CONTEXT = "NullPointer Exception! No Context found.";

    /* insert errors inside the provider. */
    public final static String PROVIDER_INSERT_URI_ERROR = "Insertion is not supported for the following URI: ";
    public final static String PROVIDER_INSERT_ROW_ERROR = "Failed to insert row inside URI: ";

    /* query errors inside the provider. */
    public final static String PROVIDER_QUERY_NO_MATCH = "Cannot query unknown URI ";

    /* update errors inside the provider. */
    public final static String PROVIDER_UPDATE_URI_ERROR = "Updating is not supported for the following URI: ";

    /* delete errors inside the provider. */
    public final static String PROVIDER_DELETE_URI_ERROR = "Deleting impossible inside URI: ";

    /* getType errors inside the provider. */
    public final static String PROVIDER_GET_TYPE_URI_ERROR = "Unknown URI: ";
    public final static String PROVIDER_GET_TYPE_MATCH_ERROR = " with match: ";

    /* parse error inside the editor activity. */
    public final static String EDITOR_PARSE_DISCOUNT = "Error parsing string to int: ";
}
