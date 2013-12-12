/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author bwarshaw
 */
public class LocalUploader {

    static File cache = new File("/home/bwarshaw/Projects/CMSC424/MMDA424/src/main/webapp/localWrites");

    public static void clearCache() {
        try {
            FileUtils.cleanDirectory(cache);
        } catch (IOException ex) {
            Webservice.logger.log(Level.SEVERE, null, ex);
        }
    }

    public static void uploadFile(String path) {
        File file = new File(path);
        try {
            FileUtils.copyFileToDirectory(file, cache);
        } catch (IOException ex) {
            Webservice.logger.log(Level.SEVERE, null, ex);
        }
        Webservice.logger.log(Level.INFO, "Cached {0}", path);
    }
}
