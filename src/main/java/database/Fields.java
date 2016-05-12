package database;

/**
 * Database Node fields
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class Fields {
    //File node
    public static final String ID             = "identifier";
    public static final String TITLE          = "title";
    public static final String PATH           = "path";
    public static final String CREATED_DATE   = "createdDate";
    public static final String MODIFIED_DATE  = "modifiedDate";
    public static final String MIME_TYPE      = "mimeType";
    public static final String FILE           = "file";
    public static final String PARENT_ID      = "parentId";
    public static final String IS_ROOT        = "IsRoot";
    public static final String IS_TRASHED     = "isTrashed";
    public static final String LAST_CHANGE_ID = "lastChangeId";
    public static final String PROCESSED      = "processed";
    public static final String VERSION        = "version";

    //Change node
    public static final String FILE_ID        = "fileId";
    public static final String SELF_LINK      = "selfLink";
    public static final String DELETED        = "deleted";
    public static final String TRASHED        = "trashed";
}
