package top.tjunet.loctag;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@SuppressLint("DefaultLocale")
public class FileHandler {
    public List<Integer> mFileNameComponents;
    public int mFileNameCursor;
    public static final String NUMBER_FORMAT = "%02d";
    private File baseDir;
    public String ext = ".txt";
    private BufferedWriter bw = null;

    /**
     * Construct a FileHandler object with the path: {baseDir}/{category}/{filename}{ext}
     * example: for FileHandler(getContext().getExternalFilesDir(null), "csi" , "01/01", ".txt"),
     *          the absolute path is "/emulated/0/Android/data/top.tjunet.loctag/csi/01/01.txt"
     */
    public FileHandler(File baseDir, String category, String filename) {
        setFileName(filename);
        this.baseDir = new File(baseDir, category);
    }

    public void setFileName(String filename) throws NumberFormatException{
        int dotPos = filename.lastIndexOf(".");
        if (dotPos>=0) {
            this.ext = filename.substring(dotPos);
            filename = filename.substring(0, dotPos);
        }
        String[] paths = filename.split("/");
        List<Integer> filenameComponents = new ArrayList<>();
        for (String str: paths) {
            if (!str.matches("\\d+"))
                throw new NumberFormatException("Only numbers allowed in filename");
            filenameComponents.add(Integer.parseInt(str));
        }
        mFileNameComponents = filenameComponents;
        mFileNameCursor = filenameComponents.size()-1;
    }

    public Spanned getSpannedFilename() {
        List<String> filenameComponents = new ArrayList<>();
        for (int i=0; i<mFileNameComponents.size(); i++) {
            if (i==mFileNameCursor) {
                filenameComponents.add(String.format("<font color='red'><b>%02d</b></font>", mFileNameComponents.get(i)));
            } else {
                filenameComponents.add(String.format("%02d", mFileNameComponents.get(i)));
            }
        }
        return Html.fromHtml(TextUtils.join("/", filenameComponents) + ext);
    }

    public String getFilename() {
        List<String> filenameComponents = new ArrayList<>();
        for (Integer d: mFileNameComponents) {
                filenameComponents.add(String.format("%02d", d));
        }
        return TextUtils.join("/", filenameComponents) + ext;
    }

    public Uri getFileUri() {
        String filename = getFilename();
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;
        File file = new File(baseDir, filename);
        if(!file.exists())
            return null;
        return Uri.fromFile(file);
    }

    public int moveFileNameCursor(int direction) {
        int pos = mFileNameCursor+ direction;
        if (pos<0)
            return -1;
        if (pos>=mFileNameComponents.size())
            return 1;
        mFileNameCursor = pos;
        return 0;
    }

    public void updateFileName(int increment) {
        int pos = mFileNameCursor;
        int d = mFileNameComponents.get(pos) + increment;
        if (d<0)
            d = 0;
        mFileNameComponents.set(pos, d);
    }

    public void createFile() throws IOException{
        closeFile();
        String filename = getFilename();
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                throw new IOException("media exception");
        String path = filename.substring(0, filename.lastIndexOf("/"));
        File dir = new File(baseDir, path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(baseDir, filename);
        bw = new BufferedWriter(new FileWriter(file, false));
    }

    public String fileStatue(){
        String filename = getFilename();
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return "Media exception";
        File file = new File(baseDir, filename);
        if(!file.exists())
            return "new file";
        float len = file.length();
        if (len<1024)
            return String.format("%.0f B", len);
        if (len<1024*1024)
            return String.format("%.1f KB", len/1024);
        if (len<1024*1024*1024)
            return String.format("%.1f MB", len/(1024*1024));
        return "Too big";
    }

    public void writeText(String lines) throws IOException {
        bw.write(lines);
        bw.flush();
    }
    public void closeFile() {
        try {
            if (bw!=null)
                bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
