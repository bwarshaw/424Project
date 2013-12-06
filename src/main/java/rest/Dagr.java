/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author bwarshaw
 *
 * Class which represents a DAGR as stored in the database. SHOULD NOT BE STORED
 * IN MEMORY FOR LONG - only use to parse database results
 */
public class Dagr {

    String guid;
    String name;
    String annotations;
    String type;
    String filePath;
    String user;
    long date;
    int size;
    ArrayList<Dagr> children;
    public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /*  used to build a new Dagr  */
    public Dagr(String filePath, String user, String name) {
        if (filePath == null) {
            this.date = System.currentTimeMillis();
        } else {
            this.date = getDateLastModified(filePath);
        }
        this.guid = generateNewGuid();
        this.name = name;
        if (name.equals("")) {
            this.name = filePath;
        }
        this.type = getFileType(filePath);
        if (type.length() > 10) {
            this.type = "?";
        }
        this.size = getFileSize(filePath);
        this.annotations = type;
        this.filePath = filePath;
        this.user = user;
        this.children = new ArrayList<Dagr>();
    }

    /*/ used to represent a Dagr pulled from db  */
    public Dagr(String guid, String name, String annotations, String type, String filePath, String user, long date, int size) {
        this.guid = guid;
        this.name = name;
        this.annotations = annotations;
        this.type = type;
        this.filePath = filePath;
        this.user = user;
        this.size = size;
        this.date = date;
        children = new ArrayList<Dagr>();
    }

    /* return a string that you can pass straight to 'insert into ... values (STRING) '  */
    public String dbInputString() {
        String s = "";
        s += "\'" + guid + "\',";
        s += "\'" + name + "\',";
        s += "\'" + type + "\',";
        s += "\'" + filePath + "\',";
        s += "\'" + date + "\',";
        s += "\'" + size + "\',";
        s += "\'" + annotations + "\'";
        return s;
    }

    public JSONObject treeView() {
        JSONObject json = new JSONObject();
        json.put("text", name);
        json.put("guid", guid);
        json.put("type", type);
        json.put("date", df.format(date));
        json.put("size", size);
        json.put("annotations", annotations);
        if (children.isEmpty()) {
            json.put("leaf", true);
        } else {
            json.put("leaf", false);
            json.put("children", new JSONArray());
            for (Dagr d : children) {
                json.getJSONArray("children").add(d.treeView());
            }
        }
        return json;
    }

    public JSONObject getDagrInfo() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("guid", guid);
        json.put("path", filePath);
        json.put("type", type);
        json.put("size", size + "");
        json.put("date", df.format(date));
        json.put("annotations", annotations);
        return json;
    }

    private String generateNewGuid() {
        return UUID.randomUUID().toString();
    }

    private String getFileType(String path) {
        if (path == null) {
            return "DAGR";
        }
        String[] arr = path.split("\\.");
        return arr[arr.length - 1];
    }

    /*
     * Returns 0 for a user-created Dagr
     * hopefully size of file for remote file
     * size of file for a local file
     */
    private int getFileSize(String path) {
        if (path == null) {
            return 0;
        }
        if (path.substring(0, 4).equals("http")) {
            return -1;
//            try {
//                HttpURLConnection connection = (HttpURLConnection) new URL(path).openConnection();
//                int pageSize = connection.getContentLength();
//                connection.disconnect();
//                return pageSize;
//            } catch (IOException ioe) {
//                Webservice.logger.log(Level.SEVERE, null, ioe);
//                return -1;
//            }
        }
        File file = new File(path);
        return (int) file.length();
    }

    private long getDateLastModified(String path) {
        if (path.substring(0, 4).equals("http")) {
            return -1;
//            try {
//                HttpURLConnection connection = (HttpURLConnection) (new URL(path)).openConnection();
//                connection.connect();
//                long time = connection.getLastModified();
//                connection.disconnect();
//                if (time > 0) {
//                    return time;
//                } else {
//                    return System.currentTimeMillis();
//                }
//            } catch (IOException ioe) {
//                Webservice.logger.log(Level.SEVERE, null, ioe);
//                return System.currentTimeMillis();
//            }
        }

        File file = new File(path);
        return file.lastModified();
    }
}
