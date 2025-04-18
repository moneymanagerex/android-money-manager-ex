package com.money.manager.ex.core.docstorage;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.utils.MmxDate;

import timber.log.Timber;

import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

/**
 * Metadata for the file selected in the document storage, using Storage Access Framework.
 */
public class DocFileMetadata {
    public String Uri;
    public String Name;
    public long Size;
    public MmxDate lastModified;

    /**
     * Retrieves metadata for a document using its URI.
     *
     * @param context The context for accessing content resolver.
     * @param uri     The URI of the document.
     * @return A DocFileMetadata object containing file details.
     */
    public static DocFileMetadata fromUri(Context context, Uri uri) {
        DocFileMetadata result = new DocFileMetadata();
        result.Uri = uri.toString();
        result.lastModified = new MmxDate(0); // Default value

        try {
            context.getContentResolver().notifyChange(uri, null);
            Timber.d("Metadata refresh triggered for URI: %s", uri);
        } catch (Exception e) {
            Timber.w(e, "Failed to refresh metadata for URI: %s", uri);
        }

        // Try with DocumentFile first
        try {
            DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
            if (documentFile != null && documentFile.exists()) {
                result.Name = documentFile.getName() != null ? documentFile.getName() : "Unknown";
                result.Size = documentFile.isFile() ? documentFile.length() : -1;
                result.lastModified = new MmxDate(documentFile.lastModified());
                return result;
            }
        } catch (Exception e) {
            Timber.w(e, "Failed to retrieve metadata using DocumentFile for URI: %s", uri);
        }

        // Fallback to cursor-based retrieval
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                Timber.w("Cursor is null or empty for URI: %s", uri);
                return result;
            }

            // Extract metadata
            int displayNameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
            int lastModifiedIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED);

            result.Name = displayNameIndex != -1 ? cursor.getString(displayNameIndex) : "Unknown";
            result.Size = (sizeIndex != -1 && !cursor.isNull(sizeIndex)) ? cursor.getLong(sizeIndex) : -1;
            result.lastModified = (lastModifiedIndex != -1 && !cursor.isNull(lastModifiedIndex))
                    ? new MmxDate(cursor.getLong(lastModifiedIndex))
                    : new MmxDate(0);
        } catch (Exception e) {
            Timber.e(e, "Error retrieving metadata for URI: %s", uri);
        }

        return result;
    }

    // New method for retrieving metadata from a DatabaseMetadata object
    public static DocFileMetadata fromDatabaseMetadata(Context context, DatabaseMetadata metadata) {
        Uri remoteUri = android.net.Uri.parse(metadata.remotePath);
        return fromUri(context, remoteUri);
    }
}
