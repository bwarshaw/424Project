/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;

/**
 *
 * @author bwarshaw
 */
@Path("/webservice")
public class Webservice {

    static DBConnection dbConn;
    static final Logger logger = Logger.getLogger(Webservice.class.getName());

    @POST
    @Path("/initConnection")
    public void initConnection() {
        dbConn = new DBConnection("root", "root", "jdbc:mysql://localhost/MMDA");
        LocalUploader.clearCache();
    }

    /*
     * Inserts new user-created DAGR into database
     * User-created DAGRs cannot contain '.' or '/'
     */
    @POST
    @Path("/createDagr")
    public String createDagr(String name) {
        Dagr dagr = new Dagr(null, getUser(), name);
        if (name.contains("\\.") || name.contains("/")) {
            return "Error: names of user-created DAGRs cannot contain '.' or '/'";
        }
        if (!dbConn.createNewDagr(dagr)) {
            return "Error: dagr with name " + name + " already exists";
        }
        return jsonify(dbConn.buildTreeView(dagr.guid, getUser()));
    }

    @POST
    @Path("initTab")
    public String initTab(String name) {
        Dagr dagr = dbConn.findDagrGivenName(name, getUser());
        if (dagr == null) {
            return "Error: no dagr with name " + name;
        }
        return jsonify(dbConn.buildTreeView(dagr.guid, getUser()));
    }

    /*
     * Create a DAGR containing all files from a given path.
     * Can NOT be used on a remote path
     */
    @POST
    @Path("/bulkLoad")
    public String bulkLoad(String filePath) {
        if (filePath.substring(0, 4).equals("http")) {
            return "Error: bulk load for remote files not supported";
        }
        File rootFile = new File(filePath);
        if (!rootFile.exists()) {
            return "Error: " + filePath + " does not exist locally";
        }
        if (!rootFile.isDirectory()) {
            return "Error: " + filePath + " is not a directory";
        }
        Dagr root = new Dagr(null, getUser(), filePath);
        if (!dbConn.createNewDagr(root)) {
            return "Error: dagr with name " + filePath + " already exists";
        }
        for (String file : BulkLoader.findAllFilesInTree(filePath)) {
            if (file.endsWith("html")) {
                Dagr htmlDagr = HtmlUtil.buildHtmlDagr(file, getUser(), "");
                dbConn.insertNewDagr(htmlDagr, root.guid, false);
                for (Dagr subElement : htmlDagr.children) {
                    dbConn.insertNewDagr(subElement, htmlDagr.guid, false);
                }
            } else {
                Dagr fileDagr = new Dagr(file, getUser(), "");
                dbConn.insertNewDagr(fileDagr, root.guid, false);
            }
        }
        return jsonify(dbConn.buildTreeView(root.guid, getUser()));
    }
    
    /*
    Loads a local file into a public cache so that Google Docs viewer can see it
    */
    @POST
    @Path("/cacheDagr") 
    public String cacheDagr(String path) {
        LocalUploader.uploadFile(path);
        String[] components = path.split("/");
        return components[components.length - 1];
    }

    /*   search db based on criteria passed
     return a list of dagrs matching criteria to UI  */
    @GET
    @Path("/searchDagr")
    public String searchDagr(@QueryParam("fromDate") String fromDate, @QueryParam("toDate") String toDate, @QueryParam("name") String name, @QueryParam("type") String fileType, @QueryParam("minSize") String minSize, @QueryParam("maxSize") String maxSize, @QueryParam("keywords") String keywords) {
        String[] keywordArr = keywords.split(",");
        if (keywordArr.length == 0) {
            keywordArr = null;
        }
        ArrayList<Dagr> results = dbConn.searchByAttributes(fromDate, toDate, name, fileType, getUser(), minSize, maxSize, keywordArr);
        return buildJsonList(results);
    }

    /*  search db for any dagr without a parent
     return a list of dagrs matching this criteria  */
    @GET
    @Path("/searchOrphans")
    public String searchOrphans() {
        ArrayList<Dagr> orphans = dbConn.searchOrphans(getUser());
        return buildJsonList(orphans);
    }

    /*  search db for any dagr without children
     return a list of dagrs matching this criteria  */
    @GET
    @Path("/searchSterile")
    public String searchSterile() {
        ArrayList<Dagr> sterile = dbConn.searchSterile(getUser());
        return buildJsonList(sterile);
    }

    /*    search db for duplicate dagrs
     remove copies of these dagrs so that
     only one copy is left
     return a list of deleted dagrs  */
    @GET
    @Path("/removeDuplicates")
    public String removeDuplicates() {
        HashSet<String> duplicates = dbConn.removeDuplicates();
        ArrayList<String> arrDuplicates = new ArrayList<String>();
        for (String s : duplicates) {
            arrDuplicates.add(s);
        }
        return encode(arrDuplicates);
    }

    @POST
    @Path("/saveHtml")
    public String saveHtmlFromBrowser(String html) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd--HH:mm:ss");
        String date = df.format(System.currentTimeMillis());
        String pathName = "/home/bwarshaw/Projects/CMSC424/htmlwrites/" + getUser() + "_" + date + ".html";
        try {
            PrintWriter wtr = new PrintWriter(new File(pathName));
            wtr.println(html);
            wtr.flush();
            wtr.close();
            Dagr htmlDagr = HtmlUtil.buildHtmlDagr(pathName, getUser(), getUser() + "_" + date);
            if (!dbConn.createNewDagr(htmlDagr)) {
                return "Error: could not create dagr with name " + pathName;
            }
            for (Dagr subElement : htmlDagr.children) {
                dbConn.insertNewDagr(subElement, htmlDagr.guid, false);
            }
            return jsonify(dbConn.buildTreeView(htmlDagr.guid, getUser()));
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
            return "Error: could not create dagr";
        }
    }

    /*
     * ========================================================================
     * Individual DAGR calls
     * ========================================================================
     */
    /*   insert dagr into db, as a child
     return new view of parent dagr  */
    @GET
    @Path("/addDagr")
    public String addDagr(@QueryParam("dagrId") String dagrId, @QueryParam("path") String filePath) {
        if (filePath.length() > 255) {
            return "Error: file path is too long, must be less than 256 characters";
        }
        // user-created DAGR, check if it exists
        if (!filePath.contains("\\.") && !filePath.contains("/")) {
            Dagr dagr = dbConn.findDagrGivenName(filePath, getUser());
            if (dagr == null) {
                return "Error: dagr does not exist: " + filePath;
            }
            if (dbConn.insertNewDagr(dagr, dagrId, true)) {
                return "added dagr to dagr";
            } else {
                return "Error: cannot construct a dagr with loops: "
                        + filePath + " has " + dbConn.grabDagrFromId(dagrId, getUser()).name + " as a child";
            }
        }

        // check if local file, and if so check if it exists
        if (!filePath.substring(0, 4).equals("http")) {
            File file = new File(filePath);
            if (!file.exists()) {
                return "Error: " + filePath + " does not exist";
            }
            if (file.isDirectory()) {
                return "Error: " + filePath + " is a directory";
            }
        } // file is remote, check if it is valid
        else {
            try {
                URL url = new URL(filePath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 404) {
                    return "Error: invalid url: " + filePath;
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, null, ioe);
                return "Error: could not retrieve " + filePath;
            }
        }

        if (filePath.endsWith("html")) {
            Dagr htmlDagr = HtmlUtil.buildHtmlDagr(filePath, getUser(), "");
            dbConn.insertNewDagr(htmlDagr, dagrId, false);
            for (Dagr subElement : htmlDagr.children) {
                dbConn.insertNewDagr(subElement, htmlDagr.guid, false);
            }
            return "added html document to dagr";
        } else {
            Dagr dagr = new Dagr(filePath, getUser(), "");
            dbConn.insertNewDagr(dagr, dagrId, false);
            return "added document to dagr";
        }

    }

    /*    remove specified dagr from db
     return new view of parent dagr
     MUST CALL findChildren / findAncestors first to compose a 
     list of affected dagrs  */
    @GET
    @Path("/deleteDagr")
    public String deleteDagr(@QueryParam("dagrId") String toDeleteId) {
        dbConn.deleteDagr(toDeleteId);
        return "deleted dagr";
    }

    /*  find all dagrs that are descended from this dagr
     return list of these dagrs  */
    @GET
    @Path("/findChildren")
    public String findChildren(@QueryParam("dagrId") String dagrId) {
        ArrayList<Dagr> children = dbConn.findChildDagrs(dagrId, getUser());
        return buildJsonList(children);
    }

    /* find all dagrs which eventually point to this dagr
     return list of these dagrs  */
    @GET
    @Path("/findAncestors")
    public String findAncestors(@QueryParam("dagrId") String dagrId) {
        ArrayList<Dagr> ancestors = dbConn.findParentDagrs(dagrId, getUser());
        return buildJsonList(ancestors);
    }

    /*   add comments to a dagr
     return new view of dagr  */
    @GET
    @Path("/annotateDagr")
    public String annotateDagr(@QueryParam("dagrId") String dagrId, @QueryParam("comments") String comments) {
        dbConn.annotateDagr(dagrId, comments);
        return "annotated";
    }

    /*  rename dagr
     return new view of dagr */
    @GET
    @Path("/renameDagr")
    public String renameDagr(@QueryParam("dagrId") String dagrId, @QueryParam("name") String name) {
        if (dbConn.findDagrGivenName(name, getUser()) != null) {
            return "Error: dagr with name " + name + " already exists";
        }
        dbConn.renameDagr(dagrId, name);
        return "renamed DAGR";
    }

    /* returns a view of a Dagr */
    @GET
    @Path("/dagrView")
    public String dagrView(@QueryParam("dagrId") String dagrId) {
        Dagr root = dbConn.buildTreeView(dagrId, getUser());
        return jsonify(root);
    }

    /* returns the shiro user */
    private String getUser() {
        return SecurityUtils.getSubject().getPrincipal().toString();
    }

    /*
     * converts an arraylist to a string which can be passed to UI
     */
    private String encode(ArrayList<String> strings) {
        StringBuilder encoded = new StringBuilder("");
        for (String s : strings) {
            encoded.append(s);
            encoded.append("@@@");
        }
        return encoded.toString();
    }

    /*
     * Converts a Dagr into a JSON string
     */
    private String jsonify(Dagr root) {
        if (root == null) {
            return "Error: treeview could not be built";
        }
        JSONObject json = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        jsonArr.add(root.treeView());
        json.put("children", jsonArr);
        ((JSONObject) ((JSONArray) json.get("children")).get(0)).put("expanded", true);
        return json.toString();
    }

    private String buildJsonList(List<Dagr> dagrs) {
        JSONArray itemsArr = new JSONArray();
        for (Dagr d : dagrs) {
            itemsArr.add(d.getDagrInfo());
        }
        JSONObject wrapper = new JSONObject();
        wrapper.put("items", itemsArr);
        return wrapper.toString();
    }
}
