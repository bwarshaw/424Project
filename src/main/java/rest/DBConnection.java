/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;

/**
 *
 * @author bwarshaw
 *
 * Class which directly interacts with the MySQL database
 */
public class DBConnection {

    Connection conn;
    HashSet<String> visitedReach = new HashSet<String>();

    public DBConnection(String user, String pass, String database) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(database, user, pass);
        } catch (Exception ex) {
            Webservice.logger.log(Level.SEVERE, null, ex);
        }
    }

    /*
     * ====================================================================
     * Functions which write to the DB
     * ====================================================================
     */
    public boolean createNewDagr(Dagr dagr) {
        if (findDagrGivenName(dagr.name, dagr.user) != null) {
            return false;
        }
        try {
            Statement s = conn.createStatement();
            String query = "insert into dagrs values(" + dagr.dbInputString() + ");";
            s.executeUpdate(query);
            query = "insert into hasUser values(\'" + dagr.guid + "\',\'" + dagr.user + "\');";
            s.executeUpdate(query);
            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return false;
        }
        Webservice.logger.log(Level.INFO, "Inserted {0}", dagr.dbInputString());
        return true;
    }

    /*
     * isDagr parameter is true if the dagr being inserted is a user-created dagr.
     * if true, this dagr already exists in the database, so we don't need to
     * re-insert it; just set up the parent/child relationship in hasDagr table
     */
    public boolean insertNewDagr(Dagr child, String parentId, boolean isDagr) {
        try {
            Statement s = conn.createStatement();
            String query;
            if (isDagr) {
                if (findKids(child.guid).contains(parentId) || child.guid.equals(parentId)) {
                    return false;
                }
            }
            if (!isDagr) {
                query = "insert into dagrs values(" + child.dbInputString() + ");";
                s.executeUpdate(query);
                query = "insert into hasUser values(\'" + child.guid + "\',\'" + child.user + "\');";
                s.executeUpdate(query);
            }
            query = "insert into hasDagr values(" + "\'" + parentId + "\',\'" + child.guid + "\');";
            s.executeUpdate(query);

            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return false;
        }
        Webservice.logger.log(Level.INFO, "Inserted {0} into {1}", new Object[]{child.guid, parentId});
        return true;
    }

    public void deleteDagr(String dagrId) {
        int count = 0;
        try {
            Statement s = conn.createStatement();
            String query = "delete from dagrs where guid = '" + dagrId + "';";
            s.executeUpdate(query);
            query = "delete from hasDagr where parentId = '" + dagrId + "' or childId = '" + dagrId + "';";
            count = s.executeUpdate(query);
            query = "delete from hasUser where guid = '" + dagrId + "';";
            s.executeUpdate(query);
            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
        }
        Webservice.logger.log(Level.INFO, "Deleted {0}, had {1} refs", new Object[]{dagrId, count});
    }

    public void renameDagr(String dagrId, String name) {
        try {
            Statement s = conn.createStatement();
            String query = "update dagrs set name = '" + name + "' where guid = '" + dagrId + "';";
            s.executeUpdate(query);
            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
        }
        Webservice.logger.log(Level.INFO, "Renamed {0} -> {1}", new Object[]{dagrId, name});
    }

    public void annotateDagr(String dagrId, String annotations) {
        try {
            Statement s = conn.createStatement();
            String query = "update dagrs set annotations = '" + annotations + "' where guid = '" + dagrId + "';";
            s.executeUpdate(query);
            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
        }
        Webservice.logger.log(Level.INFO, "Added annotations to {0} -> {1}", new Object[]{dagrId, annotations});
    }

    /*
     * ====================================================================
     * Search functions
     * ====================================================================
     */
    public Dagr findDagrGivenName(String name, String user) {
        try {
            Statement s = conn.createStatement();
            String query = "select dagrs.guid from dagrs, hasUser where dagrs.guid = hasUser.guid and user_name = '" + user + "'";
            query += " and name = '" + name + "';";
            s.executeQuery(query);
            ResultSet rs = s.getResultSet();
            String dagrId = "";
            while (rs.next()) {
                dagrId = rs.getString("guid");
            }
            if (dagrId.equals("")) {
                return null;
            }
            Dagr retVal = buildTreeView(dagrId, user);
            s.close();
            return retVal;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    public ArrayList<String> searchByAttributes(String fromDate, String toDate, String name, String fileType, String user, String minSize, String maxSize, String[] keywords) {
        try {
            ArrayList<String> dagrs = new ArrayList<String>();
            Statement s = conn.createStatement();
            String query = "select name from dagrs, hasUser where dagrs.guid = hasUser.guid and user_name = '" + user + "'";
            if (name != null && !name.equals("")) {
                query += " and UPPER(name) like UPPER('%" + name + "%')";
            }
            if (fileType != null && !fileType.equals("")) {
                query += " and UPPER(type) like UPPER('%" + fileType + "%')";
            }

            if (fromDate != null && !fromDate.equals("")) {
                Date from = new Date(fromDate);
                query += " and date >= " + from.getTime();
            }
            if (toDate != null && !toDate.equals("")) {
                Date to = new Date(toDate);
                query += " and date <= " + to.getTime();
            }

            if (minSize != null && !minSize.equals("")) {
                query += " and size >= " + minSize;
            }
            if (maxSize != null && !maxSize.equals("")) {
                query += " and size <= " + maxSize;
            }
            if (keywords != null) {
                for (String t : keywords) {
                    if (!t.equals("")) {
                        query += " and annotations like UPPER('%" + t + "%')";
                    }
                }
            }

            query += ";";
            System.out.println(query);
            s.executeQuery(query);
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                dagrs.add(rs.getString("name"));
            }
            rs.close();
            s.close();
            return dagrs;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    public ArrayList<String> searchOrphans(String user) {
        try {
            ArrayList<String> ids = new ArrayList<String>();
            Statement s = conn.createStatement();
            String query = "select name from dagrs, hasUser where  dagrs.guid = hasUser.guid and hasUser.user_name = '" + user + "' and dagrs.guid not in (select childId from hasDagr);";
            s.executeQuery(query);
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                ids.add(rs.getString("name"));
            }
            rs.close();
            s.close();
            return ids;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    public ArrayList<String> searchSterile(String user) {
        try {
            ArrayList<String> ids = new ArrayList<String>();
            Statement s = conn.createStatement();
            String query = "select name from dagrs, hasUser where  dagrs.guid = hasUser.guid and hasUser.user_name = '" + user + "' and dagrs.guid not in (select parentId from hasDagr);";
            s.executeQuery(query);
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                ids.add(rs.getString("name"));
            }
            rs.close();
            s.close();
            return ids;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    /*
     * Helper function for findChildDagrs
     */
    private HashSet<String> findKids(String dagrId) {
        try {
            HashSet<String> ids = new HashSet<String>();
            HashSet<String> results = new HashSet<String>();
            Statement s = conn.createStatement();
            String origQuery = "select childId from hasDagr where parentId = '";
            String query = origQuery + dagrId + "';";
            s.executeQuery(query);
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                String child = rs.getString("childId");
                if (!visitedReach.contains(child)) {
                    visitedReach.add(child);
                    ids.add(child);
                }
            }
            results.addAll(ids);
            for (String t : ids) {
                results.addAll(findKids(t));
            }
            rs.close();
            s.close();
            return results;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    /*
     * Helper function for findParentDagrs
     */
    private HashSet<String> findParents(String dagrId) {
        try {
            HashSet<String> ids = new HashSet<String>();
            HashSet<String> results = new HashSet<String>();
            Statement s = conn.createStatement();
            String origQuery = "select parentId from hasDagr where childId = '";
            String query = origQuery + dagrId + "';";
            s.executeQuery(query);
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                String parent = rs.getString("parentId");
                if (!visitedReach.contains(parent)) {
                    visitedReach.add(parent);
                    ids.add(parent);
                }
            }
            results.addAll(ids);
            for (String t : ids) {
                results.addAll(findParents(t));
            }
            rs.close();
            s.close();
            return results;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    private String grabNameFromId(String id) {
        try {
            Statement s = conn.createStatement();
            s.executeQuery("select name from dagrs where guid = '" + id + "';");
            ResultSet rs = s.getResultSet();
            String name = "";
            while (rs.next()) {
                name = rs.getString("name");
            }
            rs.close();
            s.close();
            return name;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    public ArrayList<String> findChildDagrs(String dagrId) {
        HashSet<String> ids = findKids(dagrId);
        ArrayList<String> names = new ArrayList<String>();
        for (String s : ids) {
            names.add(grabNameFromId(s));
        }
        visitedReach.clear();
        return names;
    }

    public ArrayList<String> findParentDagrs(String dagrId) {
        HashSet<String> ids = findParents(dagrId);
        ArrayList<String> names = new ArrayList<String>();
        for (String s : ids) {
            names.add(grabNameFromId(s));
        }
        visitedReach.clear();
        return names;
    }

    public Dagr buildTreeView(String dagrId, String user) {
        try {
            Statement s = conn.createStatement();
            s.executeQuery("select * from dagrs where guid = '" + dagrId + "';");
            ResultSet rs = s.getResultSet();
            Dagr root = null;
            while (rs.next()) {
                String guid = rs.getString("guid");
                String name = rs.getString("name");
                String type = rs.getString("type");
                String annotations = rs.getString("annotations");
                String path = rs.getString("path");
                long date = rs.getLong("date");
                int size = rs.getInt("size");
                root = new Dagr(guid, name, annotations, type, path, user, date, size);
            }
            for (String id : directChildren(dagrId)) {
                root.children.add(buildTreeView(id, user));
            }
            rs.close();
            s.close();
            return root;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    private ArrayList<String> directChildren(String dagrId) {
        try {
            ArrayList<String> children = new ArrayList<String>();
            Statement s = conn.createStatement();
            s.executeQuery("select childId from hasDagr where parentId = '" + dagrId + "';");
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                children.add(rs.getString("childId"));
            }
            s.close();
            return children;
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
    }

    public HashSet<String> removeDuplicates() {
        HashSet<String> paths = findDuplicateRecords();
        for (String record : paths) {
            deleteDuplicateRecord(record);
        }
        return paths;
    }

    private HashSet<String> findDuplicateRecords() {
        HashSet<String> duplicates = new HashSet<String>();
        try {
            Statement s = conn.createStatement();
            String query = "select d1.path from dagrs d1, dagrs d2 ";
            query += "where d1.guid != d2.guid and d1.path != 'null' and d1.path = d2.path;";
            s.executeQuery(query);
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                duplicates.add(rs.getString("path"));
            }
            rs.close();
            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
            return null;
        }
        return duplicates;
    }

    private void deleteDuplicateRecord(String path) {
        try {
            Statement s = conn.createStatement();
            String query = "delete from dagrs where path = '" + path + "';";
            s.executeUpdate(query);
            Dagr uniqueCopy = new Dagr(path, "", "");
            String uniqueId = uniqueCopy.guid;
            query = "insert into dagrs values(" + uniqueCopy.dbInputString() + ");";
            s.executeUpdate(query);
            query = "update hasUser set guid = '" + uniqueId + "' where guid not in (select guid from dagrs);";
            s.executeUpdate(query);
            query = "update hasDagr set parentId = '" + uniqueId + "' where parentId not in (select guid from dagrs);";
            s.executeUpdate(query);
            query = "update hasDagr set childId = '" + uniqueId + "' where childId not in (select guid from dagrs);";
            s.executeUpdate(query);
            Webservice.logger.log(Level.INFO, "Deleted duplicate record {0}", path);
            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
        }
    }

    public void clearDB() {
        try {
            Statement s = conn.createStatement();
            s.executeUpdate("delete from hasUser where 1=1;");
            s.executeUpdate("delete from hasDagr where 1=1;");
            s.executeUpdate("delete from dagrs where 1=1;");
            s.close();
        } catch (SQLException sqe) {
            Webservice.logger.log(Level.SEVERE, null, sqe);
        }

    }

    public static void main(String args[]) throws Exception {
        DBConnection dbConn = new DBConnection("root", "root", "jdbc:mysql://localhost/MMDA");
        dbConn.clearDB();
        Dagr billDagr = new Dagr(null, "bill", "bills dagr");
        dbConn.createNewDagr(billDagr);
        Dagr subDagr = new Dagr(null, "bill", "sub");
        dbConn.createNewDagr(subDagr);
        System.out.println(dbConn.insertNewDagr(subDagr, billDagr.guid, true));
        System.out.println(dbConn.insertNewDagr(billDagr, billDagr.guid, true));
        Dagr tree = dbConn.buildTreeView(billDagr.guid, "bill");
        dbConn.conn.close();
    }
}
