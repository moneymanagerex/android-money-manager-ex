package com.money.manager.ex.core.docstorage;

import com.money.manager.ex.utils.MmxDate;

/**
 * Metadata for the file selected in the document storage, using Storage Access Framework.
 */
public class DocFileMetadata {
    public String Uri;
    public String Name;
    public long Size;
    public MmxDate lastModified;
}
