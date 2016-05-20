package model.types;

/**
 * Google api mime types
 *
 * David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class MimeType {
    public static final String AUDIO = "application/vnd.google-apps.audio";
    public static final String DOCUMENT = "application/vnd.google-apps.document";
    public static final String DRAWING = "application/vnd.google-apps.drawing";
    public static final String FILE = "application/vnd.google-apps.file";
    public static final String FOLDER = "application/vnd.google-apps.folder";
    public static final String FORM = "application/vnd.google-apps.form";
    public static final String FUSIONTABLE = "application/vnd.google-apps.fusiontable";
    public static final String PHOTO = "application/vnd.google-apps.photo";
    public static final String PRESENTATION = "application/vnd.google-apps.presentation";
    public static final String SCRIPTS = "application/vnd.google-apps.script";
    public static final String SITES = "application/vnd.google-apps.sites";
    public static final String SPREADSHIT = "application/vnd.google-apps.spreadsheet";
    public static final String UNKNOW = "application/vnd.google-apps.unknown";
    public static final String VIDEO = "application/vnd.google-apps.video";

    /**
     * Get all mime types
     *
     * @return Set
     */
    public static java.util.Set<String> all() {
        java.util.Set<String> set = new java.util.HashSet<String>();
        set.add(AUDIO);
        set.add(DOCUMENT);
        set.add(DRAWING);
        set.add(FILE);
        set.add(FOLDER);
        set.add(FORM);
        set.add(FUSIONTABLE);
        set.add(PHOTO);
        set.add(PRESENTATION);
        set.add(SCRIPTS);
        set.add(SPREADSHIT);
        set.add(SITES);
        set.add(UNKNOW);
        set.add(VIDEO);
        return java.util.Collections.unmodifiableSet(set);
    }
}
