/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.util.ArrayList;
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
    }

    /*  insert Dagr into db
     create new tab in UI for this dagr
     display this dagr in UI - need to send back DagrId  */
    @POST
    @Path("/createDagr")
    public String createDagr(String name) {
        Dagr dagr = new Dagr(null, getUser(), name);
        dbConn.createNewDagr(dagr);
        return dagr.getDagrInfo().toString();
    }

    /*  create Dagr from all files at given path
     display this dagr in UI  */
    @POST
    @Path("/bulkLoad")
    public String bulkLoad(String filePath) {
        throw new UnsupportedOperationException("bulkLoad" + " " + filePath);
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
        ArrayList<String> results = dbConn.searchByAttributes(fromDate, toDate, name, fileType, getUser(), minSize, maxSize, keywordArr);
        return encode(results);
    }

    /*  search db for any dagr without a parent
     return a list of dagrs matching this criteria  */
    @GET
    @Path("/searchOrphans")
    public String searchOrphans() {
        ArrayList<String> orphans = dbConn.searchOrphans(getUser());
        return encode(orphans);
    }

    /*  search db for any dagr without children
     return a list of dagrs matching this criteria  */
    @GET
    @Path("/searchSterile")
    public String searchSterile() {
        ArrayList<String> sterile = dbConn.searchSterile(getUser());
        return encode(sterile);
    }

    /*    search db for duplicate dagrs
     remove copies of these dagrs so that
     only one copy is left
     return a list of deleted dagrs  */
    @GET
    @Path("/removeDuplicates")
    public String removeDuplicates() {
        throw new UnsupportedOperationException("removeDuplicates");
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
        String[] arr = filePath.split("\\.");
        if (arr[arr.length - 1].equals("html")) {
            throw new UnsupportedOperationException("creation of html dagr");
        }
        if(!filePath.contains("\\.") && !filePath.contains("/")) {
            Dagr dagr = dbConn.findDagrGivenName(filePath, getUser());
            dbConn.insertNewDagr(dagr, dagrId, true);
            return "added dagr to dagr";
        }
        Dagr dagr = new Dagr(filePath, getUser(), "");
        dbConn.insertNewDagr(dagr, dagrId, false);
        return "added document to dagr";
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
        System.out.println(dagrId);
        ArrayList<String> children = dbConn.findChildDagrs(dagrId);
        return encode(children);
    }

    /* find all dagrs which eventually point to this dagr
     return list of these dagrs  */
    @GET
    @Path("/findAncestors")
    public String findAncestors(@QueryParam("dagrId") String dagrId) {
        ArrayList<String> ancestors = dbConn.findParentDagrs(dagrId);
        return encode(ancestors);
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
        dbConn.renameDagr(dagrId, name);
        return "renamed DAGR";
    }

    /* returns a view of a Dagr */
    @GET
    @Path("/dagrView")
    public String dagrView(@QueryParam("dagrId") String dagrId) {
        Dagr root = dbConn.buildTreeView(dagrId, getUser());
        JSONObject json = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        jsonArr.add(root.treeView());
        json.put("children", jsonArr);
        ((JSONObject )((JSONArray) json.get("children")).get(0)).put("expanded", true);
        return json.toString();
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
}
