package com.transcend.otg.Utils;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class MimeTypeMapExt {
    private static MimeTypeMapExt mimeTypeMapExt;

    public static MimeTypeMapExt getSingleton() {
        if (null == mimeTypeMapExt) {
            mimeTypeMapExt = new MimeTypeMapExt();
            buildHashMapExt();
        }
        return mimeTypeMapExt;
    }

    private HashMap<String, String> mapMimeTypeToExtension;
    private HashMap<String, String> mapExtensionToMimeType;

    private MimeTypeMapExt() {
        mapMimeTypeToExtension = new HashMap<String, String>();
        mapExtensionToMimeType = new HashMap<String, String>();
    }

    /**
     * Return true if the given extension has a registered MIME type.
     *
     * @param extension A file extension without the leading '.'
     * @return True if there is an extension entry in the map.
     */

    public boolean hasExtension(String extension) {
        if (extension != null && extension.length() > 0) {
            return mapExtensionToMimeType.containsKey(extension);
        }
        return false;
    }

    /**
     * Return true if the given MIME type has an entry in the map.
     *
     * @param mimeType A MIME type (i.e. text/plain)
     * @return True iff there is a mimeType entry in the map.
     */

    public boolean hasMimeType(String mimeType) {
        if (mimeType != null && mimeType.length() > 0) {
            return mapMimeTypeToExtension.containsKey(mimeType);
        }
        return false;
    }

    /**
     * Return the registered extension for the given MIME type. Note that some
     * MIME types map to multiple extensions. This call will return the most
     * common extension for the given MIME type.
     *
     * @param mimeType A MIME type (i.e. text/plain)
     * @return The extension for the given MIME type or null iff there is none.
     */

    public String getExtensionFromMimeType(String mimeType) {
        if (mimeType != null && mimeType.length() > 0) {
            return mapMimeTypeToExtension.get(mimeType);
        }
        return null;
    }

    /**
     * Return the MIME type for the given extension.
     *
     * @param extension A file extension without the leading '.'
     * @return The MIME type for the given extension or null if there is none.
     */

    public String getMimeTypeFromExtension(String extension) {
        if (extension != null && extension.length() > 0) {
            return mapExtensionToMimeType.get(extension);
        }
        return null;
    }

    /**
     * Returns the file extension or an empty string iff there is no extension.
     * <p>
     * This method is a convenience method for obtaining the extension of a url
     * and has undefined results for other Strings.
     *
     * @param url
     * @return The file extension of the given url.
     */

    public static String getFileExtensionFromUrl(String url) {
        if (url != null && url.length() > 0) {
            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename =
                    0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:

            if (filename.length() > 0 &&
                    Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename)) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }
        return "";
    }

    // Static method called by jni.
    @SuppressWarnings("unused")
    private static String mimeTypeFromExtension(String extension) {
        return getSingleton().getMimeTypeFromExtension(extension);
    }

    /**
     * If we have an existing x --> y mapping, we do not want to
     * override it with another mapping x --> ?.
     * <p>
     * This is mostly because of the way the mime-type map below
     * is constructed (if a mime type maps to several extensions
     * the first extension is considered the most popular and is
     * added first; we do not want to overwrite it later).
     */

    private void put(String mimeType, String extension) {
        // mime -> ext
        if (!mapMimeTypeToExtension.containsKey(mimeType)) {
            mapMimeTypeToExtension.put(mimeType, extension);
        }
        // ext -> mime
        mapExtensionToMimeType.put(extension, mimeType);
    }

    /**
     * The following table is based on /etc/mime.types data minus
     * chemical/* MIME types and MIME types that don't map to any
     * file extensions.
     * <p>
     * We also exclude top-level domain names to deal with cases
     * like: mail.google.com/a/google.com
     * and "active" MIME types (due to potential security issues).
     */

    private static void buildHashMapExt() {
        mimeTypeMapExt.put("application/andrew-inset", "ez");
        mimeTypeMapExt.put("application/dsptype", "tsp");
        mimeTypeMapExt.put("application/futuresplash", "spl");
        mimeTypeMapExt.put("application/hta", "hta");
        mimeTypeMapExt.put("application/mac-binhex40", "hqx");
        mimeTypeMapExt.put("application/mac-compactpro", "cpt");
        mimeTypeMapExt.put("application/mathematica", "nb");
        mimeTypeMapExt.put("application/msaccess", "mdb");
        mimeTypeMapExt.put("application/oda", "oda");
        mimeTypeMapExt.put("application/ogg", "ogg");
        mimeTypeMapExt.put("application/pdf", "pdf");
        mimeTypeMapExt.put("application/pgp-keys", "key");
        mimeTypeMapExt.put("application/pgp-signature", "pgp");
        mimeTypeMapExt.put("application/pics-rules", "prf");
        mimeTypeMapExt.put("application/rar", "rar");
        mimeTypeMapExt.put("application/rdf+xml", "rdf");
        mimeTypeMapExt.put("application/rss+xml", "rss");
        mimeTypeMapExt.put("application/zip", "zip");
        mimeTypeMapExt.put("application/vnd.android.package-archive", "apk");
        mimeTypeMapExt.put("application/vnd.cinderella", "cdy");
        mimeTypeMapExt.put("application/vnd.ms-pki.stl", "stl");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.database", "odb");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.formula", "odf");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.graphics", "odg");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.graphics-template", "otg");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.image", "odi");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.spreadsheet", "ods");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.spreadsheet-template", "ots");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.text", "odt");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.text-master", "odm");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.text-template", "ott");
        mimeTypeMapExt.put("application/vnd.oasis.opendocument.text-web", "oth");
        mimeTypeMapExt.put("application/msword", "doc");
        mimeTypeMapExt.put("application/msword", "dot");
        mimeTypeMapExt.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        mimeTypeMapExt.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx");
        mimeTypeMapExt.put("application/vnd.ms-excel", "xls");
        mimeTypeMapExt.put("application/vnd.ms-excel", "xlt");
        mimeTypeMapExt.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        mimeTypeMapExt.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx");
        mimeTypeMapExt.put("application/vnd.ms-powerpoint", "ppt");
        mimeTypeMapExt.put("application/vnd.ms-powerpoint", "pot");
        mimeTypeMapExt.put("application/vnd.ms-powerpoint", "pps");
        mimeTypeMapExt.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
        mimeTypeMapExt.put("application/vnd.openxmlformats-officedocument.presentationml.template", "potx");
        mimeTypeMapExt.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx");
        mimeTypeMapExt.put("application/vnd.rim.cod", "cod");
        mimeTypeMapExt.put("application/vnd.smaf", "mmf");
        mimeTypeMapExt.put("application/vnd.stardivision.calc", "sdc");
        mimeTypeMapExt.put("application/vnd.stardivision.draw", "sda");
        mimeTypeMapExt.put("application/vnd.stardivision.impress", "sdd");
        mimeTypeMapExt.put("application/vnd.stardivision.impress", "sdp");
        mimeTypeMapExt.put("application/vnd.stardivision.math", "smf");
        mimeTypeMapExt.put("application/vnd.stardivision.writer", "sdw");
        mimeTypeMapExt.put("application/vnd.stardivision.writer", "vor");
        mimeTypeMapExt.put("application/vnd.stardivision.writer-global", "sgl");
        mimeTypeMapExt.put("application/vnd.sun.xml.calc", "sxc");
        mimeTypeMapExt.put("application/vnd.sun.xml.calc.template", "stc");
        mimeTypeMapExt.put("application/vnd.sun.xml.draw", "sxd");
        mimeTypeMapExt.put("application/vnd.sun.xml.draw.template", "std");
        mimeTypeMapExt.put("application/vnd.sun.xml.impress", "sxi");
        mimeTypeMapExt.put("application/vnd.sun.xml.impress.template", "sti");
        mimeTypeMapExt.put("application/vnd.sun.xml.math", "sxm");
        mimeTypeMapExt.put("application/vnd.sun.xml.writer", "sxw");
        mimeTypeMapExt.put("application/vnd.sun.xml.writer.global", "sxg");
        mimeTypeMapExt.put("application/vnd.sun.xml.writer.template", "stw");
        mimeTypeMapExt.put("application/vnd.visio", "vsd");
        mimeTypeMapExt.put("application/x-abiword", "abw");
        mimeTypeMapExt.put("application/x-apple-disk", "dmg");
        mimeTypeMapExt.put("application/x-bcpio", "bcpio");
        mimeTypeMapExt.put("application/x-bittorrent", "torrent");
        mimeTypeMapExt.put("application/x-cdf", "cdf");
        mimeTypeMapExt.put("application/x-cdlink", "vcd");
        mimeTypeMapExt.put("application/x-chess-pgn", "pgn");
        mimeTypeMapExt.put("application/x-cpio", "cpio");
        mimeTypeMapExt.put("application/x-debian-package", "deb");
        mimeTypeMapExt.put("application/x-debian-package", "udeb");
        mimeTypeMapExt.put("application/x-director", "dcr");
        mimeTypeMapExt.put("application/x-director", "dir");
        mimeTypeMapExt.put("application/x-director", "dxr");
        mimeTypeMapExt.put("application/x-dms", "dms");
        mimeTypeMapExt.put("application/x-doom", "wad");
        mimeTypeMapExt.put("application/x-dvi", "dvi");
        mimeTypeMapExt.put("application/x-flac", "flac");
        mimeTypeMapExt.put("application/x-font", "pfa");
        mimeTypeMapExt.put("application/x-font", "pfb");
        mimeTypeMapExt.put("application/x-font", "gsf");
        mimeTypeMapExt.put("application/x-font", "pcf");
        mimeTypeMapExt.put("application/x-font", "pcf.Z");
        mimeTypeMapExt.put("application/x-freemind", "mm");
        mimeTypeMapExt.put("application/x-futuresplash", "spl");
        mimeTypeMapExt.put("application/x-gnumeric", "gnumeric");
        mimeTypeMapExt.put("application/x-go-sgf", "sgf");
        mimeTypeMapExt.put("application/x-graphing-calculator", "gcf");
        mimeTypeMapExt.put("application/x-gtar", "gtar");
        mimeTypeMapExt.put("application/x-gtar", "tgz");
        mimeTypeMapExt.put("application/x-gtar", "taz");
        mimeTypeMapExt.put("application/x-hdf", "hdf");
        mimeTypeMapExt.put("application/x-ica", "ica");
        mimeTypeMapExt.put("application/x-internet-signup", "ins");
        mimeTypeMapExt.put("application/x-internet-signup", "isp");
        mimeTypeMapExt.put("application/x-iphone", "iii");
        mimeTypeMapExt.put("application/x-iso9660-image", "iso");
        mimeTypeMapExt.put("application/x-jmol", "jmz");
        mimeTypeMapExt.put("application/x-kchart", "chrt");
        mimeTypeMapExt.put("application/x-killustrator", "kil");
        mimeTypeMapExt.put("application/x-koan", "skp");
        mimeTypeMapExt.put("application/x-koan", "skd");
        mimeTypeMapExt.put("application/x-koan", "skt");
        mimeTypeMapExt.put("application/x-koan", "skm");
        mimeTypeMapExt.put("application/x-kpresenter", "kpr");
        mimeTypeMapExt.put("application/x-kpresenter", "kpt");
        mimeTypeMapExt.put("application/x-kspread", "ksp");
        mimeTypeMapExt.put("application/x-kword", "kwd");
        mimeTypeMapExt.put("application/x-kword", "kwt");
        mimeTypeMapExt.put("application/x-latex", "latex");
        mimeTypeMapExt.put("application/x-lha", "lha");
        mimeTypeMapExt.put("application/x-lzh", "lzh");
        mimeTypeMapExt.put("application/x-lzx", "lzx");
        mimeTypeMapExt.put("application/x-maker", "frm");
        mimeTypeMapExt.put("application/x-maker", "maker");
        mimeTypeMapExt.put("application/x-maker", "frame");
        mimeTypeMapExt.put("application/x-maker", "fb");
        mimeTypeMapExt.put("application/x-maker", "book");
        mimeTypeMapExt.put("application/x-maker", "fbdoc");
        mimeTypeMapExt.put("application/x-mif", "mif");
        mimeTypeMapExt.put("application/x-ms-wmd", "wmd");
        mimeTypeMapExt.put("application/x-ms-wmz", "wmz");
        mimeTypeMapExt.put("application/x-msi", "msi");
        mimeTypeMapExt.put("application/x-ns-proxy-autoconfig", "pac");
        mimeTypeMapExt.put("application/x-nwc", "nwc");
        mimeTypeMapExt.put("application/x-object", "o");
        mimeTypeMapExt.put("application/x-oz-application", "oza");
        mimeTypeMapExt.put("application/x-pkcs12", "p12");
        mimeTypeMapExt.put("application/x-pkcs7-certreqresp", "p7r");
        mimeTypeMapExt.put("application/x-pkcs7-crl", "crl");
        mimeTypeMapExt.put("application/x-quicktimeplayer", "qtl");
        mimeTypeMapExt.put("application/x-shar", "shar");
        mimeTypeMapExt.put("application/x-shockwave-flash", "swf");
        mimeTypeMapExt.put("application/x-stuffit", "sit");
        mimeTypeMapExt.put("application/x-sv4cpio", "sv4cpio");
        mimeTypeMapExt.put("application/x-sv4crc", "sv4crc");
        mimeTypeMapExt.put("application/x-tar", "tar");
        mimeTypeMapExt.put("application/x-texinfo", "texinfo");
        mimeTypeMapExt.put("application/x-texinfo", "texi");
        mimeTypeMapExt.put("application/x-troff", "t");
        mimeTypeMapExt.put("application/x-troff", "roff");
        mimeTypeMapExt.put("application/x-troff-man", "man");
        mimeTypeMapExt.put("application/x-ustar", "ustar");
        mimeTypeMapExt.put("application/x-wais-source", "src");
        mimeTypeMapExt.put("application/x-wingz", "wz");
        mimeTypeMapExt.put("application/x-webarchive", "webarchive");
        mimeTypeMapExt.put("application/x-x509-ca-cert", "crt");
        mimeTypeMapExt.put("application/x-x509-user-cert", "crt");
        mimeTypeMapExt.put("application/x-xcf", "xcf");
        mimeTypeMapExt.put("application/x-xfig", "fig");
        mimeTypeMapExt.put("application/xhtml+xml", "xhtml");
        mimeTypeMapExt.put("audio/3gpp", "3gpp");
        mimeTypeMapExt.put("audio/amr", "amr");
        mimeTypeMapExt.put("audio/basic", "snd");
        mimeTypeMapExt.put("audio/midi", "mid");
        mimeTypeMapExt.put("audio/midi", "midi");
        mimeTypeMapExt.put("audio/midi", "kar");
        mimeTypeMapExt.put("audio/midi", "xmf");
        mimeTypeMapExt.put("audio/mobile-xmf", "mxmf");
        mimeTypeMapExt.put("audio/mpeg", "mpga");
        mimeTypeMapExt.put("audio/mpeg", "mpega");
        mimeTypeMapExt.put("audio/mpeg", "mp2");
        mimeTypeMapExt.put("audio/mpeg", "mp3");
        mimeTypeMapExt.put("audio/mpeg", "m4a");
        mimeTypeMapExt.put("audio/mpegurl", "m3u");
        mimeTypeMapExt.put("audio/prs.sid", "sid");
        mimeTypeMapExt.put("audio/x-aiff", "aif");
        mimeTypeMapExt.put("audio/x-aiff", "aiff");
        mimeTypeMapExt.put("audio/x-aiff", "aifc");
        mimeTypeMapExt.put("audio/x-gsm", "gsm");
        mimeTypeMapExt.put("audio/x-mpegurl", "m3u");
        mimeTypeMapExt.put("audio/x-ms-wma", "wma");
        mimeTypeMapExt.put("audio/x-ms-wax", "wax");
        mimeTypeMapExt.put("audio/x-pn-realaudio", "ra");
        mimeTypeMapExt.put("audio/x-pn-realaudio", "rm");
        mimeTypeMapExt.put("audio/x-pn-realaudio", "ram");
        mimeTypeMapExt.put("audio/x-realaudio", "ra");
        mimeTypeMapExt.put("audio/x-scpls", "pls");
        mimeTypeMapExt.put("audio/x-sd2", "sd2");
        mimeTypeMapExt.put("audio/x-wav", "wav");
        mimeTypeMapExt.put("image/bmp", "bmp");
        mimeTypeMapExt.put("image/gif", "gif");
        mimeTypeMapExt.put("image/ico", "cur");
        mimeTypeMapExt.put("image/ico", "ico");
        mimeTypeMapExt.put("image/ief", "ief");
        mimeTypeMapExt.put("image/jpeg", "jpeg");
        mimeTypeMapExt.put("image/jpeg", "jpg");
        mimeTypeMapExt.put("image/jpeg", "jpe");
        mimeTypeMapExt.put("image/pcx", "pcx");
        mimeTypeMapExt.put("image/png", "png");
        mimeTypeMapExt.put("image/svg+xml", "svg");
        mimeTypeMapExt.put("image/svg+xml", "svgz");
        mimeTypeMapExt.put("image/tiff", "tiff");
        mimeTypeMapExt.put("image/tiff", "tif");
        mimeTypeMapExt.put("image/vnd.djvu", "djvu");
        mimeTypeMapExt.put("image/vnd.djvu", "djv");
        mimeTypeMapExt.put("image/vnd.wap.wbmp", "wbmp");
        mimeTypeMapExt.put("image/x-cmu-raster", "ras");
        mimeTypeMapExt.put("image/x-coreldraw", "cdr");
        mimeTypeMapExt.put("image/x-coreldrawpattern", "pat");
        mimeTypeMapExt.put("image/x-coreldrawtemplate", "cdt");
        mimeTypeMapExt.put("image/x-corelphotopaint", "cpt");
        mimeTypeMapExt.put("image/x-icon", "ico");
        mimeTypeMapExt.put("image/x-jg", "art");
        mimeTypeMapExt.put("image/x-jng", "jng");
        mimeTypeMapExt.put("image/x-ms-bmp", "bmp");
        mimeTypeMapExt.put("image/x-photoshop", "psd");
        mimeTypeMapExt.put("image/x-portable-anymap", "pnm");
        mimeTypeMapExt.put("image/x-portable-bitmap", "pbm");
        mimeTypeMapExt.put("image/x-portable-graymap", "pgm");
        mimeTypeMapExt.put("image/x-portable-pixmap", "ppm");
        mimeTypeMapExt.put("image/x-rgb", "rgb");
        mimeTypeMapExt.put("image/x-xbitmap", "xbm");
        mimeTypeMapExt.put("image/x-xpixmap", "xpm");
        mimeTypeMapExt.put("image/x-xwindowdump", "xwd");
        mimeTypeMapExt.put("model/iges", "igs");
        mimeTypeMapExt.put("model/iges", "iges");
        mimeTypeMapExt.put("model/mesh", "msh");
        mimeTypeMapExt.put("model/mesh", "mesh");
        mimeTypeMapExt.put("model/mesh", "silo");
        mimeTypeMapExt.put("text/calendar", "ics");
        mimeTypeMapExt.put("text/calendar", "icz");
        mimeTypeMapExt.put("text/comma-separated-values", "csv");
        mimeTypeMapExt.put("text/css", "css");
        mimeTypeMapExt.put("text/html", "htm");
        mimeTypeMapExt.put("text/html", "html");
        mimeTypeMapExt.put("text/h323", "323");
        mimeTypeMapExt.put("text/iuls", "uls");
        mimeTypeMapExt.put("text/mathml", "mml");
        // add it first so it will be the default for ExtensionFromMimeType
        mimeTypeMapExt.put("text/plain", "txt");
        mimeTypeMapExt.put("text/plain", "ini");
        mimeTypeMapExt.put("text/plain", "asc");
        mimeTypeMapExt.put("text/plain", "text");
        mimeTypeMapExt.put("text/plain", "diff");
        // reserve "pot" for vnd.ms-powerpoint
        mimeTypeMapExt.put("text/plain", "po");
        mimeTypeMapExt.put("text/richtext", "rtx");
        mimeTypeMapExt.put("text/rtf", "rtf");
        mimeTypeMapExt.put("text/texmacs", "ts");
        mimeTypeMapExt.put("text/text", "phps");
        mimeTypeMapExt.put("text/tab-separated-values", "tsv");
        mimeTypeMapExt.put("text/xml", "xml");
        mimeTypeMapExt.put("text/x-bibtex", "bib");
        mimeTypeMapExt.put("text/x-boo", "boo");
        mimeTypeMapExt.put("text/x-c++hdr", "h++");
        mimeTypeMapExt.put("text/x-c++hdr", "hpp");
        mimeTypeMapExt.put("text/x-c++hdr", "hxx");
        mimeTypeMapExt.put("text/x-c++hdr", "hh");
        mimeTypeMapExt.put("text/x-c++src", "c++");
        mimeTypeMapExt.put("text/x-c++src", "cpp");
        mimeTypeMapExt.put("text/x-c++src", "cxx");
        mimeTypeMapExt.put("text/x-chdr", "h");
        mimeTypeMapExt.put("text/x-component", "htc");
        mimeTypeMapExt.put("text/x-csh", "csh");
        mimeTypeMapExt.put("text/x-csrc", "c");
        mimeTypeMapExt.put("text/x-dsrc", "d");
        mimeTypeMapExt.put("text/x-haskell", "hs");
        mimeTypeMapExt.put("text/x-java", "java");
        mimeTypeMapExt.put("text/x-literate-haskell", "lhs");
        mimeTypeMapExt.put("text/x-moc", "moc");
        mimeTypeMapExt.put("text/x-pascal", "p");
        mimeTypeMapExt.put("text/x-pascal", "pas");
        mimeTypeMapExt.put("text/x-pcs-gcd", "gcd");
        mimeTypeMapExt.put("text/x-setext", "etx");
        mimeTypeMapExt.put("text/x-tcl", "tcl");
        mimeTypeMapExt.put("text/x-tex", "tex");
        mimeTypeMapExt.put("text/x-tex", "ltx");
        mimeTypeMapExt.put("text/x-tex", "sty");
        mimeTypeMapExt.put("text/x-tex", "cls");
        mimeTypeMapExt.put("text/x-vcalendar", "vcs");
        mimeTypeMapExt.put("text/x-vcard", "vcf");
        mimeTypeMapExt.put("video/3gpp", "3gpp");
        mimeTypeMapExt.put("video/3gpp", "3gp");
        mimeTypeMapExt.put("video/3gpp", "3g2");
        mimeTypeMapExt.put("video/dl", "dl");
        mimeTypeMapExt.put("video/dv", "dif");
        mimeTypeMapExt.put("video/dv", "dv");
        mimeTypeMapExt.put("video/fli", "fli");
        mimeTypeMapExt.put("video/m4v", "m4v");
        mimeTypeMapExt.put("video/mpeg", "mpeg");
        mimeTypeMapExt.put("video/mpeg", "mpg");
        mimeTypeMapExt.put("video/mpeg", "mpe");
        mimeTypeMapExt.put("video/mp4", "mp4");
        mimeTypeMapExt.put("video/mpeg", "vob");
        mimeTypeMapExt.put("video/quicktime", "qt");
        mimeTypeMapExt.put("video/quicktime", "mov");
        mimeTypeMapExt.put("video/vnd.mpegurl", "mxu");
        mimeTypeMapExt.put("video/x-la-asf", "lsf");
        mimeTypeMapExt.put("video/x-la-asf", "lsx");
        mimeTypeMapExt.put("video/x-mng", "mng");
        mimeTypeMapExt.put("video/x-ms-asf", "asf");
        mimeTypeMapExt.put("video/x-ms-asf", "asx");
        mimeTypeMapExt.put("video/x-ms-wm", "wm");
        mimeTypeMapExt.put("video/x-ms-wmv", "wmv");
        mimeTypeMapExt.put("video/x-ms-wmx", "wmx");
        mimeTypeMapExt.put("video/x-ms-wvx", "wvx");
        mimeTypeMapExt.put("video/x-msvideo", "avi");
        mimeTypeMapExt.put("video/x-sgi-movie", "movie");
        mimeTypeMapExt.put("x-conference/x-cooltalk", "ice");
        mimeTypeMapExt.put("x-epoc/x-sisx-app", "sisx");
        //*/ Extended MIME types
        mimeTypeMapExt.put("audio/vnd.rn-realaudio", "ra");
        mimeTypeMapExt.put("audio/vnd.rn-realaudio", "ram");
        mimeTypeMapExt.put("text/plain", "lrc");
        mimeTypeMapExt.put("video/vnd.rn-realmedia", "rmvb");
        mimeTypeMapExt.put("video/vnd.rn-realmedia", "rm");
        mimeTypeMapExt.put("video/vnd.rn-realvideo", "rv");
        mimeTypeMapExt.put("video/x-flv", "flv");
        mimeTypeMapExt.put("video/x-flv", "hlv");
        mimeTypeMapExt.put("video/x-matroska", "mkv");
        mimeTypeMapExt.put("video/x-divx", "divx");
        mimeTypeMapExt.put("video/x-evo", "evo");
        mimeTypeMapExt.put("video/x-f4v", "f4v");
        mimeTypeMapExt.put("application/gxf", "gxf");
        mimeTypeMapExt.put("audio/x-matroska", "mka");
        mimeTypeMapExt.put("video/x-matroska", "mks");
        mimeTypeMapExt.put("model/vnd.mts", "mts");
        mimeTypeMapExt.put("video/m2p", "m2p");
        mimeTypeMapExt.put("video/unkown", "m2t");
        mimeTypeMapExt.put("video/m2ts", "m2ts");
        mimeTypeMapExt.put("video/x-matroska", "mk3d");
        mimeTypeMapExt.put("video/x-ogm", "ogm");
        mimeTypeMapExt.put("application/postscript", "ps");
        mimeTypeMapExt.put("application/x-7z-compressed", "7z");
        mimeTypeMapExt.put("application/transcend", "enc");
        mimeTypeMapExt.put("audio/x-aac", "aac");
        mimeTypeMapExt.put("enc", "enc");
        //*/ Extended MIME types
    }
}
