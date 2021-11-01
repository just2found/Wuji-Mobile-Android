package net.sdvn.nascommon.utils;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public class MIMETypeUtils {
    /**
     * 声明各种类型文件的dataType
     **/
    public static final String DATA_TYPE_APK = "application/vnd.android.package-archive";
    public static final String DATA_TYPE_VIDEO = "video/*";
    public static final String DATA_TYPE_AUDIO = "audio/*";
    public static final String DATA_TYPE_HTML = "text/html";
    public static final String DATA_TYPE_IMAGE = "image/*";
    public static final String DATA_TYPE_PPT = "application/vnd.ms-powerpoint";
    public static final String DATA_TYPE_EXCEL = "application/vnd.ms-excel";
    public static final String DATA_TYPE_WORD = "application/msword";
    public static final String DATA_TYPE_CHM = "application/x-chm";
    public static final String DATA_TYPE_TXT = "text/plain";
    public static final String DATA_TYPE_PDF = "application/pdf";

//    private final static String[][] MIME_MAP_TABLE = {
//            {".3gp", "video/3gpp"},
//            {".apk", "application/vnd.android.package-archive"},
//            {".asf", "video/x-ms-asf"},
//            {".avi", "video/x-msvideo"},
//            {".bin", "application/octet-stream"},
//            {".bmp", "image/bmp"},
//            {".c", "text/plain"},
//            {".class", "application/octet-stream"},
//            {".conf", "text/plain"},
//            {".cpp", "text/plain"},
//            {".doc", "application/msword"},
//            {".exe", "application/octet-stream"},
//            {".gif", "image/gif"},
//            {".gtar", "application/x-gtar"},
//            {".gz", "application/x-gzip"},
//            {".h", "text/plain"},
//            {".htm", "text/html"},
//            {".html", "text/html"},
//            {".jar", "application/java-archive"},
//            {".java", "text/plain"},
//            {".jpeg", "image/jpeg"},
//            {".jpg", "image/jpeg"},
//            {".js", "application/x-javascript"},
//            {".log", "text/plain"},
//            {".m3u", "audio/x-mpegurl"},
//            {".m4a", "audio/mp4a-latm"},
//            {".m4b", "audio/mp4a-latm"},
//            {".m4p", "audio/mp4a-latm"},
//            {".m4u", "video/vnd.mpegurl"},
//            {".m4v", "video/x-m4v"},
//            {".mov", "video/quicktime"},
//            {".mp2", "audio/x-mpeg"},
//            {".mp3", "audio/x-mpeg"},
//            {".mp4", "video/mp4"},
//            {".mpc", "application/vnd.mpohun.certificate"},
//            {".mpe", "video/mpeg"},
//            {".mpeg", "video/mpeg"},
//            {".mpg", "video/mpeg"},
//            {".mpg4", "video/mp4"},
//            {".mpga", "audio/mpeg"},
//            {".msg", "application/vnd.ms-outlook"},
//            {".ogg", "audio/ogg"},
//            {".pdf", "application/pdf"},
//            {".png", "image/png"},
//            {".pps", "application/vnd.ms-powerpoint"},
//            {".ppt", "application/vnd.ms-powerpoint"},
//            {".prop", "text/plain"},
//            {".rar", "application/x-rar-compressed"},
//            {".rc", "text/plain"},
//            {".rmvb", "video/mp4"},
//            {".rtf", "application/rtf"},
//            {".sh", "text/plain"},
//            {".tar", "application/x-tar"},
//            {".tgz", "application/x-compressed"},
//            {".txt", "text/plain"},
//            {".wav", "audio/x-wav"},
//            {".wma", "audio/x-ms-wma"},
//            {".wmv", "video/x-ms-wmv"},
//            {".wps", "application/vnd.ms-works"},
//            {".xml", "text/plain"},
//            {".z", "application/x-compress"},
//            {".zip", "*/*"},
//            {"", "*/*"}
//    };

    // copy from: http://blog.csdn.net/piyell/article/details/53048757
    public static final String[][] MIME_MAP_TABLE = {
            {"application/andrew-inset", "ez"},
            {"application/dsptype", "tsp"},
            {"application/futuresplash", "spl"},
            {"application/hta", "hta"},
            {"application/mac-binhex40", "hqx"},
            {"application/mac-compactpro", "cpt"},
            {"application/mathematica", "nb"},
            {"application/msaccess", "mdb"},
            {"application/oda", "oda"},
            {"application/ogg", "ogg"},
            {"application/pdf", "pdf"},
            {"application/pgp-keys", "key"},
            {"application/pgp-signature", "pgp"},
            {"application/pics-rules", "prf"},
            {"application/rar", "rar"},
            {"application/rdf+xml", "rdf"},
            {"application/rss+xml", "rss"},
            {"application/zip", "zip"},
            {"application/vnd.android.package-archive", "apk"},
            {"application/vnd.cinderella", "cdy"},
            {"application/vnd.ms-pki.stl", "stl"},
            {"application/vnd.oasis.opendocument.database", "odb"},
            {"application/vnd.oasis.opendocument.formula", "odf"},
            {"application/vnd.oasis.opendocument.graphics", "odg"},
            {"application/vnd.oasis.opendocument.graphics-template", "otg"},
            {"application/vnd.oasis.opendocument.image", "odi"},
            {"application/vnd.oasis.opendocument.spreadsheet", "ods"},
            {"application/vnd.oasis.opendocument.spreadsheet-template", "ots"},
            {"application/vnd.oasis.opendocument.text", "odt"},
            {"application/vnd.oasis.opendocument.text-master", "odm"},
            {"application/vnd.oasis.opendocument.text-template", "ott"},
            {"application/vnd.oasis.opendocument.text-web", "oth"},
            {"application/vnd.google-earth.kml+xml", "kml"},
            {"application/vnd.google-earth.kmz", "kmz"},
            {"application/msword", "doc"},
            {"application/msword", "dot"},
            {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"},
            {"application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx"},
            {"application/vnd.ms-excel", "xls"},
            {"application/vnd.ms-excel", "xlt"},
            {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"},
            {"application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx"},
            {"application/vnd.ms-powerpoint", "ppt"},
            {"application/vnd.ms-powerpoint", "pot"},
            {"application/vnd.ms-powerpoint", "pps"},
            {"application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"},
            {"application/vnd.openxmlformats-officedocument.presentationml.template", "potx"},
            {"application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx"},
            {"application/vnd.rim.cod", "cod"},
            {"application/vnd.smaf", "mmf"},
            {"application/vnd.stardivision.calc", "sdc"},
            {"application/vnd.stardivision.draw", "sda"},
            {"application/vnd.stardivision.impress", "sdd"},
            {"application/vnd.stardivision.impress", "sdp"},
            {"application/vnd.stardivision.math", "smf"},
            {"application/vnd.stardivision.writer", "sdw"},
            {"application/vnd.stardivision.writer", "vor"},
            {"application/vnd.stardivision.writer-global", "sgl"},
            {"application/vnd.sun.xml.calc", "sxc"},
            {"application/vnd.sun.xml.calc.template", "stc"},
            {"application/vnd.sun.xml.draw", "sxd"},
            {"application/vnd.sun.xml.draw.template", "std"},
            {"application/vnd.sun.xml.impress", "sxi"},
            {"application/vnd.sun.xml.impress.template", "sti"},
            {"application/vnd.sun.xml.math", "sxm"},
            {"application/vnd.sun.xml.writer", "sxw"},
            {"application/vnd.sun.xml.writer.global", "sxg"},
            {"application/vnd.sun.xml.writer.template", "stw"},
            {"application/vnd.visio", "vsd"},
            {"application/x-abiword", "abw"},
            {"application/x-apple-diskimage", "dmg"},
            {"application/x-bcpio", "bcpio"},
            {"application/x-bittorrent", "torrent"},
            {"application/x-cdf", "cdf"},
            {"application/x-cdlink", "vcd"},
            {"application/x-chess-pgn", "pgn"},
            {"application/x-cpio", "cpio"},
            {"application/x-debian-package", "deb"},
            {"application/x-debian-package", "udeb"},
            {"application/x-director", "dcr"},
            {"application/x-director", "dir"},
            {"application/x-director", "dxr"},
            {"application/x-dms", "dms"},
            {"application/x-doom", "wad"},
            {"application/x-dvi", "dvi"},
            {"application/x-flac", "flac"},
            {"application/x-font", "pfa"},
            {"application/x-font", "pfb"},
            {"application/x-font", "gsf"},
            {"application/x-font", "pcf"},
            {"application/x-font", "pcf.Z"},
            {"application/x-freemind", "mm"},
            {"application/x-futuresplash", "spl"},
            {"application/x-gnumeric", "gnumeric"},
            {"application/x-go-sgf", "sgf"},
            {"application/x-graphing-calculator", "gcf"},
            {"application/x-gtar", "tgz"},
            {"application/x-gtar", "gtar"},
            {"application/x-gtar", "taz"},
            {"application/x-hdf", "hdf"},
            {"application/x-ica", "ica"},
            {"application/x-internet-signup", "ins"},
            {"application/x-internet-signup", "isp"},
            {"application/x-iphone", "iii"},
            {"application/x-iso9660-image", "iso"},
            {"application/x-jmol", "jmz"},
            {"application/x-kchart", "chrt"},
            {"application/x-killustrator", "kil"},
            {"application/x-koan", "skp"},
            {"application/x-koan", "skd"},
            {"application/x-koan", "skt"},
            {"application/x-koan", "skm"},
            {"application/x-kpresenter", "kpr"},
            {"application/x-kpresenter", "kpt"},
            {"application/x-kspread", "ksp"},
            {"application/x-kword", "kwd"},
            {"application/x-kword", "kwt"},
            {"application/x-latex", "latex"},
            {"application/x-lha", "lha"},
            {"application/x-lzh", "lzh"},
            {"application/x-lzx", "lzx"},
            {"application/x-maker", "frm"},
            {"application/x-maker", "maker"},
            {"application/x-maker", "frame"},
            {"application/x-maker", "fb"},
            {"application/x-maker", "book"},
            {"application/x-maker", "fbdoc"},
            {"application/x-mif", "mif"},
            {"application/x-ms-wmd", "wmd"},
            {"application/x-ms-wmz", "wmz"},
            {"application/x-msi", "msi"},
            {"application/x-ns-proxy-autoconfig", "pac"},
            {"application/x-nwc", "nwc"},
            {"application/x-object", "o"},
            {"application/x-oz-application", "oza"},
            {"application/x-pkcs12", "p12"},
            {"application/x-pkcs12", "pfx"},
            {"application/x-pkcs7-certreqresp", "p7r"},
            {"application/x-pkcs7-crl", "crl"},
            {"application/x-quicktimeplayer", "qtl"},
            {"application/x-shar", "shar"},
            {"application/x-shockwave-flash", "swf"},
            {"application/x-stuffit", "sit"},
            {"application/x-sv4cpio", "sv4cpio"},
            {"application/x-sv4crc", "sv4crc"},
            {"application/x-tar", "tar"},
            {"application/x-texinfo", "texinfo"},
            {"application/x-texinfo", "texi"},
            {"application/x-troff", "t"},
            {"application/x-troff", "roff"},
            {"application/x-troff-man", "man"},
            {"application/x-ustar", "ustar"},
            {"application/x-wais-source", "src"},
            {"application/x-wingz", "wz"},
            {"application/x-webarchive", "webarchive"},
            {"application/x-webarchive-xml", "webarchivexml"},
            {"application/x-x509-ca-cert", "crt"},
            {"application/x-x509-user-cert", "crt"},
            {"application/x-xcf", "xcf"},
            {"application/x-xfig", "fig"},
            {"application/xhtml+xml", "xhtml"},
            {"audio/3gpp", "3gpp"},
            {"audio/amr", "amr"},
            {"audio/basic", "snd"},
            {"audio/midi", "mid"},
            {"audio/midi", "midi"},
            {"audio/midi", "kar"},
            {"audio/midi", "xmf"},
            {"audio/mobile-xmf", "mxmf"},
            {"audio/mpeg", "mp3"},
            {"audio/*", "ape"},
            {"audio/*", "flac"},
            {"audio/*", "aac"},
            {"audio/*", "mpga"},
            {"audio/*", "mpega"},
            {"audio/*", "mp2"},
            {"audio/*", "m4a"},
            {"audio/mpegurl", "m3u"},
            {"audio/prs.sid", "sid"},
            {"audio/x-aiff", "aif"},
            {"audio/x-aiff", "aiff"},
            {"audio/x-aiff", "aifc"},
            {"audio/x-gsm", "gsm"},
            {"audio/x-mpegurl", "m3u"},
            {"audio/x-ms-wma", "wma"},
            {"audio/x-ms-wax", "wax"},
            {"audio/x-pn-realaudio", "ra"},
            {"audio/x-pn-realaudio", "rm"},
            {"audio/x-pn-realaudio", "ram"},
            {"audio/x-realaudio", "ra"},
            {"audio/x-scpls", "pls"},
            {"audio/x-sd2", "sd2"},
            {"audio/x-wav", "wav"},

            {"image/bmp", "bmp"},
            {"image/gif", "gif"},
            {"image/ico", "cur"},
            {"image/ico", "ico"},
            {"image/ief", "ief"},
            {"image/jpeg", "jpeg"},
            {"image/jpg", "jpg"},
            {"image/jpeg", "jpe"},
            {"image/pcx", "pcx"},
            {"image/png", "png"},
            {"image/svg+xml", "svg"},
            {"image/svg+xml", "svgz"},
            {"image/tiff", "tiff"},
            {"image/tiff", "tif"},
            {"image/vnd.djvu", "djvu"},
            {"image/vnd.djvu", "djv"},
            {"image/vnd.wap.wbmp", "wbmp"},
            {"image/x-cmu-raster", "ras"},
            {"image/x-coreldraw", "cdr"},
            {"image/x-coreldrawpattern", "pat"},
            {"image/x-coreldrawtemplate", "cdt"},
            {"image/x-corelphotopaint", "cpt"},
            {"image/x-icon", "ico"},
            {"image/x-jg", "art"},
            {"image/x-jng", "jng"},
            {"image/x-ms-bmp", "bmp"},
            {"image/x-photoshop", "psd"},
            {"image/x-portable-anymap", "pnm"},
            {"image/x-portable-bitmap", "pbm"},
            {"image/x-portable-graymap", "pgm"},
            {"image/x-portable-pixmap", "ppm"},
            {"image/x-rgb", "rgb"},
            {"image/x-xbitmap", "xbm"},
            {"image/x-xpixmap", "xpm"},
            {"image/x-xwindowdump", "xwd"},

            {"model/iges", "igs"},
            {"model/iges", "iges"},
            {"model/mesh", "msh"},
            {"model/mesh", "mesh"},
            {"model/mesh", "silo"},

            {"text/calendar", "ics"},
            {"text/calendar", "icz"},
            {"text/comma-separated-values", "csv"},
            {"text/css", "css"},
            {"text/html", "htm"},
            {"text/html", "html"},
            {"text/h323", "323"},
            {"text/iuls", "uls"},
            {"text/mathml", "mml"},
            {"text/plain", "txt"},
            {"text/plain", "asc"},
            {"text/plain", "text"},
            {"text/plain", "diff"},
            {"text/plain", "po"},
            {"text/richtext", "rtx"},
            {"text/rtf", "rtf"},
            {"text/texmacs", "ts"},
            {"text/text", "phps"},
            {"text/tab-separated-values", "tsv"},
            {"text/xml", "xml"},
            {"text/x-bibtex", "bib"},
            {"text/x-boo", "boo"},
            {"text/x-c++hdr", "hpp"},
            {"text/x-c++hdr", "h++"},
            {"text/x-c++hdr", "hxx"},
            {"text/x-c++hdr", "hh"},
            {"text/x-c++src", "cpp"},
            {"text/x-c++src", "c++"},
            {"text/x-c++src", "cc"},
            {"text/x-c++src", "cxx"},
            {"text/x-chdr", "h"},
            {"text/x-component", "htc"},
            {"text/x-csh", "csh"},
            {"text/x-csrc", "c"},
            {"text/x-dsrc", "d"},
            {"text/x-haskell", "hs"},
            {"text/x-java", "java"},
            {"text/x-literate-haskell", "lhs"},
            {"text/x-moc", "moc"},
            {"text/x-pascal", "p"},
            {"text/x-pascal", "pas"},
            {"text/x-pcs-gcd", "gcd"},
            {"text/x-setext", "etx"},
            {"text/x-tcl", "tcl"},
            {"text/x-tex", "tex"},
            {"text/x-tex", "ltx"},
            {"text/x-tex", "sty"},
            {"text/x-tex", "cls"},
            {"text/x-vcalendar", "vcs"},
            {"text/x-vcard", "vcf"},

            {"video/3gpp", "3gpp"},
            {"video/3gpp", "3gp"},
            {"video/3gpp", "3g2"},
            {"video/dl", "dl"},
            {"video/dv", "dif"},
            {"video/dv", "dv"},
            {"video/fli", "fli"},
            {"video/m4v", "m4v"},
            {"video/mpeg", "mpeg"},
            {"video/mpeg", "mpg"},
            {"video/mpeg", "mpe"},
            {"video/mp4", "mp4"},
            {"video/*", "rmvb"},
            {"video/*", "flv"},
            {"video/*", "mkv"},
            {"video/*", "ts"},
            {"video/mpeg", "VOB"},
            {"video/quicktime", "qt"},
            {"video/quicktime", "mov"},
            {"video/vnd.mpegurl", "mxu"},
            {"video/x-la-asf", "lsf"},
            {"video/x-la-asf", "lsx"},
            {"video/x-mng", "mng"},
            {"video/x-ms-asf", "asf"},
            {"video/x-ms-asf", "asx"},
            {"video/x-ms-wm", "wm"},
            {"video/x-ms-wmv", "wmv"},
            {"video/x-ms-wmx", "wmx"},
            {"video/x-ms-wvx", "wvx"},
            {"video/x-msvideo", "avi"},
            {"video/x-sgi-movie", "movie"},
            {"video/x-webex", "wrf"},
            {"x-conference/x-cooltalk", "ice"},
            {"x-epoc/x-sisx-app", "sisx"},
            {"image/*", "3fr"},
            {"image/*", "arw"},
            {"image/*", "avs"},
            {"image/*", "cals"},
            {"image/*", "cr2"},
            {"image/*", "crw"},
            {"image/*", "cut"},
            {"image/*", "dcm"},
            {"image/*", "dcx"},
            {"image/*", "dib"},
            {"image/*", "dng"},
            {"image/*", "dpx"},
            {"image/*", "emf"},
            {"image/*", "emz"},
            {"image/*", "eps"},
            {"image/*", "erf"},
            {"image/*", "fax"},
            {"image/*", "fits"},
            {"image/*", "fpx"},
            {"image/*", "heic"},
            {"image/*", "ithmb"},
            {"image/*", "j2c"},
            {"image/*", "j2k"},
            {"image/*", "jbig"},
            {"image/*", "jp2"},
            {"image/*", "jpc"},
            {"image/*", "kdc"},
            {"image/*", "mat"},
            {"image/*", "mef"},
            {"image/*", "miff"},
            {"image/*", "mos"},
            {"image/*", "mrw"},
            {"image/*", "mtv"},
            {"image/*", "mvg"},
            {"image/*", "nef"},
            {"image/*", "nrw"},
            {"image/*", "orf"},
            {"image/*", "otb"},
            {"image/*", "p7"},
            {"image/*", "palm"},
            {"image/*", "pam"},
            {"image/*", "pcd"},
            {"image/*", "pcds"},
            {"image/*", "pef"},
            {"image/*", "picon"},
            {"image/*", "pict"},
            {"image/*", "pix"},
            {"image/*", "ptif"},
            {"image/*", "raf"},
            {"image/*", "raw"},
            {"image/*", "rla"},
            {"image/*", "rle"},
            {"image/*", "rw2"},
            {"image/*", "sct"},
            {"image/*", "sfw"},
            {"image/*", "sgi"},
            {"image/*", "sr2"},
            {"image/*", "srf"},
            {"image/*", "sun"},
            {"image/*", "tga"},
            {"image/*", "thm"},
            {"image/*", "tim"},
            {"image/*", "vicar"},
            {"image/*", "viff"},
            {"image/*", "wmf"},
            {"image/*", "wpg"},
            {"image/*", "x3f"},
            {"image/*", "xv"},
            {"video/*", "261"},
            {"video/*", "4xm"},
            {"video/*", "anm"},
            {"video/*", "bfi"},
            {"video/*", "bmv"},
            {"video/*", "c93"},
            {"video/*", "cdg"},
            {"video/*", "cdxl"},
            {"video/*", "cin"},
            {"video/*", "dfa"},
            {"video/*", "drc"},
            {"video/*", "dxa"},
            {"video/*", "f4v"},
            {"video/*", "gxf"},
            {"video/*", "h261"},
            {"video/*", "h263"},
            {"video/*", "h264"},
            {"video/*", "ivf"},
            {"video/*", "lvf"},
            {"video/*", "mj2"},
            {"video/*", "mjpeg"},
            {"video/*", "mtv"},
            {"video/*", "mv"},
            {"video/*", "mvi"},
            {"video/*", "mxf"},
            {"video/*", "mxg"},
            {"video/*", "nsv"},
            {"video/*", "nut"},
            {"video/*", "nuv"},
            {"video/*", "p64"},
            {"video/*", "paf"},
            {"video/*", "pmp"},
            {"video/*", "pva"},
            {"video/*", "rl2"},
            {"video/*", "roq"},
            {"video/*", "rpl"},
            {"video/*", "smk"},
            {"video/*", "thp"},
            {"video/*", "txd"},
            {"video/*", "vc1"},
            {"video/*", "viv"},
            {"video/*", "vivo"},
            {"video/*", "vmd"},
            {"video/*", "vob"},
            {"video/*", "webm"},
            {"video/*", "wtv"},
            {"video/*", "xmv"},
            {"video/*", "yop"},
            {"video/*", "3gp2"},
            {"video/*", "amv"},
            {"video/*", "divx"},
            {"video/*", "gvi"},
            {"video/*", "ismv"},
            {"video/*", "m1v"},
            {"video/*", "m2v"},
            {"video/*", "m2t"},
            {"video/*", "m2ts"},
            {"video/*", "m3u8"},
            {"video/*", "mp2v"},
            {"video/*", "mp4v"},
            {"video/*", "mpeg1"},
            {"video/*", "mpeg2"},
            {"video/*", "mpeg4"},
            {"video/*", "mpv2"},
            {"video/*", "mts"},
            {"video/*", "ogm"},
            {"video/*", "ogv"},
            {"video/*", "ogx"},
            {"video/*", "ps"},
            {"video/*", "rec"},
            {"video/*", "tod"},
            {"video/*", "trp"},
            {"video/*", "tts"},
            {"video/*", "vro"},
            {"video/*", "xesc"},
            {"audio/*", "ac3"},
            {"audio/*", "act"},
            {"audio/*", "adts"},
            {"audio/*", "aea"},
            {"audio/*", "apc"},
            {"audio/*", "au"},
            {"audio/*", "caf"},
            {"audio/*", "dts"},
            {"audio/*", "eac3"},
            {"audio/*", "g722"},
            {"audio/*", "iff"},
            {"audio/*", "ircam"},
            {"audio/*", "iss"},
            {"audio/*", "mlp"},
            {"audio/*", "mpc"},
            {"audio/*", "oma"},
            {"audio/*", "pvf"},
            {"audio/*", "qcp"},
            {"audio/*", "rso"},
            {"audio/*", "sbg"},
            {"audio/*", "shn"},
            {"audio/*", "sol"},
            {"audio/*", "tta"},
            {"audio/*", "voc"},
            {"audio/*", "vqf"},
            {"audio/*", "w64"},
            {"audio/*", "wv"},
            {"audio/*", "xa"},
            {"audio/*", "xwma"},
            {"audio/*", "3ga"},
            {"audio/*", "a52"},
            {"audio/*", "adt"},
            {"audio/*", "aob"},
            {"audio/*", "awb"},
            {"audio/*", "it"},
            {"audio/*", "m4b"},
            {"audio/*", "m4p"},
            {"audio/*", "mka"},
            {"audio/*", "mod"},
            {"audio/*", "mpa"},
            {"audio/*", "mp1"},
            {"audio/*", "oga"},
            {"audio/*", "opus"},
            {"audio/*", "rmi"},
            {"audio/*", "s3m"},
            {"audio/*", "spx"},
            {"audio/*", "xm"},
            {"audio/*", "3ga"},
            {"audio/*", "a52"},
            {"audio/*", "adt"},
            {"audio/*", "aob"},
            {"audio/*", "awb"},
            {"audio/*", "it"},
            {"audio/*", "m4b"},
            {"audio/*", "m4p"},
            {"audio/*", "mka"},
            {"audio/*", "mod"},
            {"audio/*", "mpa"},
            {"audio/*", "mp1"},
            {"audio/*", "oga"},
            {"audio/*", "opus"},
            {"audio/*", "rmi"},
            {"audio/*", "s3m"},
            {"audio/*", "spx"},
            {"audio/*", "xm"},
            {"text/plain", "adb"},
            {"text/plain", "ads"},
            {"text/plain", "asa"},
            {"text/plain", "asax"},
            {"text/plain", "asm"},
            {"text/plain", "asp"},
            {"text/plain", "aspx"},
            {"text/plain", "au3"},
            {"text/plain", "aut"},
            {"text/plain", "bas"},
            {"text/plain", "bat"},
            {"text/plain", "bc"},
            {"text/plain", "bluebutton"},
            {"text/plain", "bsh"},
            {"text/plain", "cfg"},
            {"text/plain", "cgi"},
            {"text/plain", "cln"},
            {"text/plain", "cmake"},
            {"text/plain", "cnf"},
            {"text/plain", "conf"},
            {"text/plain", "cs"},
            {"text/plain", "csproj"},
            {"text/plain", "ctest"},
            {"text/plain", "ctl"},
            {"text/plain", "def"},
            {"text/plain", "dfm"},
            {"text/plain", "diz"},
            {"text/plain", "dlg"},
            {"text/plain", "dob"},
            {"text/plain", "docbook"},
            {"text/plain", "dpk"},
            {"text/plain", "dpr"},
            {"text/plain", "dsm"},
            {"text/plain", "dsr"},
            {"text/plain", "e"},
            {"text/plain", "erb"},
            {"text/plain", "erl"},
            {"text/plain", "es"},
            {"text/plain", "exp"},
            {"text/plain", "f"},
            {"text/plain", "f2k"},
            {"text/plain", "f90"},
            {"text/plain", "f95"},
            {"text/plain", "for"},
            {"text/plain", "g"},
            {"text/plain", "gd"},
            {"text/plain", "gi"},
            {"text/plain", "hrl"},
            {"text/plain", "htaccess"},
            {"text/plain", "htd"},
            {"text/plain", "idl"},
            {"text/plain", "iface"},
            {"text/plain", "impl"},
            {"text/plain", "inc"},
            {"text/plain", "inf"},
            {"text/plain", "ini"},
            {"text/plain", "ipp"},
            {"text/plain", "isl"},
            {"text/plain", "iss"},
            {"text/plain", "js"},
            {"text/plain", "jsp"},
            {"text/plain", "ksh"},
            {"text/plain", "log"},
            {"text/plain", "lst"},
            {"text/plain", "lua"},
            {"text/plain", "m"},
            {"text/plain", "mak"},
            {"text/plain", "manifest"},
            {"text/plain", "ml"},
            {"text/plain", "mli"},
            {"text/plain", "mp"},
            {"text/plain", "mpx"},
            {"text/plain", "nfo"},
            {"text/plain", "nsh"},
            {"text/plain", "nsi"},
            {"text/plain", "odl"},
            {"text/plain", "pag"},
            {"text/plain", "php"},
            {"text/plain", "php3"},
            {"text/plain", "phtml"},
            {"text/plain", "pl"},
            {"text/plain", "pm"},
            {"text/plain", "pod"},
            {"text/plain", "pov"},
            {"text/plain", "pp"},
            {"text/plain", "properties"},
            {"text/plain", "py"},
            {"text/plain", "pyw"},
            {"text/plain", "r"},
            {"text/plain", "rake"},
            {"text/plain", "rb"},
            {"text/plain", "rbw"},
            {"text/plain", "rc"},
            {"text/plain", "rc2"},
            {"text/plain", "reg"},
            {"text/plain", "rhtml"},
            {"text/plain", "rjs"},
            {"text/plain", "rsource"},
            {"text/plain", "s"},
            {"text/plain", "session"},
            {"text/plain", "sh"},
            {"text/plain", "shtml"},
            {"text/plain", "sign"},
            {"text/plain", "sma"},
            {"text/plain", "stm"},
            {"text/plain", "tab"},
            {"text/plain", "url"},
            {"text/plain", "v"},
            {"text/plain", "vala"},
            {"text/plain", "vb"},
            {"text/plain", "vbg"},
            {"text/plain", "vbp"},
            {"text/plain", "vbproj"},
            {"text/plain", "vbs"},
            {"text/plain", "vbw"},
            {"text/plain", "vh"},
            {"text/plain", "vhd"},
            {"text/plain", "vhdl"},
            {"text/plain", "xaml"},
            {"text/plain", "xmp"},
            {"text/plain", "yaml"},
            {"text/plain", "yml"}

    };

    public static String getMIMEType(String fName) {
        String type = "*/*";
        int doIndex = fName.lastIndexOf(".");
        if (doIndex < 0) {
            return type;
        }

        String suffix = fName.substring(doIndex + 1).toLowerCase();
        if (EmptyUtils.isEmpty(suffix)) {
            return type;
        }

        for (int i = 0; i < MIME_MAP_TABLE.length; i++) {
            if (suffix.equals(MIME_MAP_TABLE[i][1])) {
                type = MIME_MAP_TABLE[i][0];
            }
        }
        return type;
    }

    public static boolean isMultimediaFile(@NonNull String fileName) {
        final String mimeType = getMIMEType(fileName);
        if (mimeType != null && mimeType.length() > 0) {
            final String[] strings = mimeType.split("/");
            if (null != strings[0]) {
                switch (strings[0].toLowerCase()) {
                    case "audio":
                    case "video":
                    case "image":
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean isImageFile(@NonNull String fileName) {
        final String mimeType = getMIMEType(fileName);
        if (mimeType != null && mimeType.length() > 0) {
            final String[] strings = mimeType.split("/");
            if (null != strings[0]) {
                switch (strings[0].toLowerCase()) {
                    case "image":
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean isVideoFile(@NonNull String fileName) {
        final String mimeType = getMIMEType(fileName);
        if (mimeType != null && mimeType.length() > 0) {
            final String[] strings = mimeType.split("/");
            if (null != strings[0]) {
                switch (strings[0].toLowerCase()) {
                    case "video":
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean isAudioFile(@NonNull String fileName) {
        final String mimeType = getMIMEType(fileName);
        if (mimeType != null && mimeType.length() > 0) {
            final String[] strings = mimeType.split("/");
            if (null != strings[0]) {
                switch (strings[0].toLowerCase()) {
                    case "audio":
                        return true;
                }
            }
        }
        return false;
    }
    /**
     * @return 'True' if the mime type defines image
     */
    public static boolean isImage(String mimeType) {
        return mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("image/") &&
                !mimeType.toLowerCase(Locale.ROOT).contains("djvu");
    }

    /**
     * @return 'True' the mime type defines video
     */
    public static boolean isVideo(String mimeType) {
        return mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("video/");
    }

    /**
     * @return 'True' the mime type defines audio
     */
    public static boolean isAudio(String mimeType) {
        return mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("audio/");
    }
}
/*
image file
3FR File
ART File
ARW File
AVS File
BMP File
CALS File
CR2 File
CRW File
CUR File
CUT File
DCM File
DCR File
DCX File
DIB File
DNG File
DPX File
EMF File
EMZ File
EPS File
ERF File
FAX File
FITS File
FPX File
GIF File
HEIC File
ICO File
ITHMB File
J2C File
J2K File
JBIG File
JNG File
JP2 File
JPC File
JPEG File
JPG File
KDC File
MAT File
MEF File
MIFF File
MNG File
MOS File
MRW File
MTV File
MVG File
NEF File
NRW File
ORF File
OTB File
P7 File
PALM File
PAM File
PBM File
PCD File
PCDS File
PCX File
PEF File
PFA File
PFB File
PGM File
PICON File
PICT File
PIX File
PNG File
PNM File
PPM File
PTIF File
RAF File
RAS File
RAW File
RLA File
RLE File
RW2 File
SCT File
SFW File
SGI File
SR2 File
SRF File
SUN File
SVG File
TGA File
THM File
TIF File
TIFF File
TIM File
VICAR File
VIFF File
WBMP File
WMF File
WPG File
X3F File
XBM File
XCF File
XPM File
XV File

video file
261 Files
3G2 Files
3GP Files
4XM Files
ANM Files
ASF Files
AVI Files
BFI Files
BMV Files
C93 Files
CDG Files
CDXL Files
CIN Files
DFA Files
DRC Files
DV Files
DXA Files
F4V Files
FLV Files
GXF Files
H261 Files
H263 Files
H264 Files
IVF Files
LVF Files
M4V Files
MJ2 Files
MJPEG Files
MKV Files
MM Files
MMF Files
MOV Files
MP4 Files
MPEG Files
MPG Files
MTV Files
MV Files
MVI Files
MXF Files
MXG Files
NSV Files
NUT Files
NUV Files
P64 Files
PAF Files
PMP Files
PVA Files
RL2 Files
RM Files
RMVB Files
ROQ Files
RPL Files
SMK Files
SWF Files
THP Files
TXD Files
VC1 Files
VIV Files
VIVO Files
VMD Files
VOB Files
WEBM Files
WMV Files
WTV Files
XMV Files
YOP Files

audio file
AAC Files
AC3 Files
ACT Files
ADTS Files
AEA Files
AIFF Files
AMR Files
APC Files
APE Files
AU Files
CAF Files
DTS Files
EAC3 Files
FLAC Files
G722 Files
GSM Files
IFF Files
IRCAM Files
ISS Files
M4A Files
MLP Files
MP2 Files
MP3 Files
MPC Files
OGG Files
OMA Files
PVF Files
QCP Files
RSO Files
SBG Files
SHN Files
SOL Files
TTA Files
VOC Files
VQF Files
W64 Files
WAV Files
WMA Files
WV Files
XA Files
XWMA Files
3GA Files
A52 Files
AAC Files
AC3 Files
ADT Files
ADTS Files
AIF Files
AIFC Files
AIFF Files
AMR Files
AOB Files
APE Files
AWB Files
CAF Files
DTS Files
FLAC Files
IT  Files
M4A Files
M4B Files
M4P Files
MID Files
MKA Files
MLP Files
MOD Files
MPA Files
MP1 Files
MP2 Files
MP3 Files
MPC Files
MPGA Files
OGA Files
OGG Files
OMA Files
OPUS Files
RA  Files
RMI Files
S3M Files
SPX Files
TTA Files
VOC Files
VQF Files
W64 Files
WAV Files
WMA Files
WV  Files
XA  Files
XM  Files
3GA Files
A52 Files
AAC Files
AC3 Files
ADT Files
ADTS Files
AIF Files
AIFC Files
AIFF Files
AMR Files
AOB Files
APE Files
AWB Files
CAF Files
DTS Files
FLAC Files
IT  Files
M4A Files
M4B Files
M4P Files
MID Files
MKA Files
MLP Files
MOD Files
MPA Files
MP1 Files
MP2 Files
MP3 Files
MPC Files
MPGA Files
OGA Files
OGG Files
OMA Files
OPUS Files
RA  Files
RMI Files
S3M Files
SPX Files
TTA Files
VOC Files
VQF Files
W64 Files
WAV Files
WMA Files
WV  Files
XA  Files
XM  Files

text file
ADB File
ADS File
ASA File
ASAX File
ASM File
ASP File
ASPX File
AU3 File
AUT File
BAS File
BAT File
BC File
BlueButton File
BSH File
C File
CC File
CFG File
CGI File
CLN File
CLS File
CMAKE File
CNF File
CONF File
CPP File
CS File
CSPROJ File
CSS File
CTEST File
CTL File
CXX File
DEF File
DFM File
DIZ File
DLG File
DOB File
DOCBOOK File
DPK File
DPR File
DSM File
DSR File
E File
ERB File
ERL File
ES File
EXP File
F File
F2K File
F90 File
F95 File
FOR File
FRM File
G File
GD File
GI File
H File
HH File
HPP File
HRL File
HTA File
HTACCESS File
HTD File
HTM File
HTML File
HXX File
IDL File
IFACE File
IMPL File
INC File
INF File
INI File
IPP File
ISL File
ISS File
JAVA File
JS File
JSP File
KSH File
LOG File
LST File
LUA File
M File
MAK File
MANIFEST File
ML File
MLI File
MM File
MP File
MPX File
NFO File
NSH File
NSI File
ODL File
PAG File
PAS File
PHP File
PHP3 File
PHTML File
PL File
PM File
POD File
POV File
PP File
PROPERTIES File
PY File
PYW File
R File
RAKE File
RB File
RBW File
RC File
RC2 File
REG File
RHTML File
RJS File
RSOURCE File
S File
SESSION File
SH File
SHTML File
SIGN File
SMA File
STM File
TAB File
TCL File
TXT File
URL File
V File
VALA File
VB File
VBG File
VBP File
VBPROJ File
VBS File
VBW File
VH File
VHD File
VHDL File
XAML File
XML File
XMP File
YAML File
YML File
*/
