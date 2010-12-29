/*
 * Helma License Notice
 *
 * The contents of this file are subject to the Helma License
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://adele.helma.org/download/helma/license.txt
 *
 * Copyright 1998-2003 Helma Software. All Rights Reserved.
 *
 * $RCSfile: WebRepository.java,v $
 * $Author: hannes $
 * $Revision: 1.14 $
 * $Date: 2006/04/07 14:37:11 $
 */

package freefall.node;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import org.ringojs.repository.*;
import java.util.logging.Logger;

/**
 * Repository implementation for URLs providing web resources
 */
public class WebRepository extends AbstractRepository implements Repository
{
    private static final Logger log = Logger.getLogger(WebRepository.class.getName());
  
    // URL serving sub-repositories and web resources
    protected final URL directory;
    
    protected AbstractRepository parent;
    protected String name;
    protected String path;

    protected long lastModified = -1;
    protected long lastChecksum = 0;
    protected long lastChecksumTime = 0;

    /**
     * Defines how long the checksum of the repository will be cached
     */
    final long cacheTime = 1000L;

    /**
     * Constructs a WebRepository using the given argument
     * @param path absolute path to the directory
     * @throws IOException if canonical path couldn't be resolved
     */
    public WebRepository(String path) throws IOException {
        this(new URL(path));
    }

    /**
     * Constructs a WebRepository using the given directory as top-level
     * repository
     * @param dir directory
     * @throws IOException if canonical path couldn't be resolved
     */
    public WebRepository(URL dir) throws IOException {
        this(dir, null);
    }

    /**
     * Constructs a WebRepository using the given directory and top-level
     * repository
     * @param dir directory
     * @param parent top-level repository
     * @throws IOException if canonical path couldn't be resolved
     */
    public WebRepository(URL dir, WebRepository p) throws IOException {
        directory = dir;

        parent = p;
        name = directory.getFile(); //FIXME -- probably not what we want, how is name actually used?
        path = directory.getPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
    }

    public String getRelativePath()
    {
      return path;
    }
    
    public void setRoot()
    {
    }

    public AbstractRepository getChildRepository(String name) throws MalformedURLException, IOException
    {
      return new WebRepository(new URL(directory.toString()+name));
    }

    /**
     * Check whether the repository exists.
     * @return true if the repository exists.
     */
    public boolean exists() {
      return true; // FIXME - how do we actually check this?
//        return directory.isDirectory();
    }

    /**
     * Create a child repository with the given name
     * @param name the name of the repository
     * @return the child repository
     */
    public AbstractRepository createChildRepository(String name) throws IOException {
        URL f =new URL(directory.toString()+name+"/");
        return new WebRepository(f, this);
    }

    /**
     * Get this repository's parent repository.
     */
    @Override
    public AbstractRepository getParentRepository() {
        return parent;
    }

    /**
     * Returns the date the repository was last modified.
     *
     * @return last modified date
     */
    public long lastModified() {
      return 0; // FIXME - How do we actually check this?
//        return directory.lastModified();
    }

    /**
     * Checksum of the repository and all its contained resources. Implementations
     * should make sure to return a different checksum if any contained resource
     * has changed.
     *
     * @return checksum
     */
    public synchronized long getChecksum() throws IOException {
        // delay checksum check if already checked recently
        /*
        if (System.currentTimeMillis() > lastChecksumTime + cacheTime) {
            long checksum = lastModified;

            for (Resource res: resources.values()) {
                checksum += res.lastModified();
            }

            lastChecksum = checksum;
            lastChecksumTime = System.currentTimeMillis();
        }

        return lastChecksum;
        */

        return 0; // FIXME - how do we actually check this?
    }

    /**
     * Called to create a child resource for this repository
     */
    @Override
    protected Resource lookupResource(String name) throws IOException {
      /*
        AbstractResource res = resources.get(name);
        if (res == null) {
            res = new WebResource(new URL(directory.toString()+name), this);
            resources.put(name, res);
        }
        return res;
      */
      
      return null;
    }

    public Resource[] getResources(String s, boolean b)
    {
      return new Resource[0];
    }

    public Resource[] getResources(boolean b)
    {
      return new Resource[0];
    }

    public Resource[] getResources()
    {
      return new Resource[0];
    }
    
    public Resource getResource(String s) throws IOException
    {
      log.info("Get web resource: "+s);
      if(s.startsWith("./"))
      {
        s=s.substring(2, s.length());
      }
      return new WebResource(directory.toString()+s);
    }
    
    public boolean isAbsolute()
    {
      return true;
    }
    
    public void setAbsolute(boolean b)
    {
      return;
    }
    
    public String getModuleName()
    {
      return null;
    }
    
    public Repository getRootRepository()
    {
      return this;
    }
    
    public String getName()
    {
      return name;
    }
    
    public String getPath()
    {
      return path;
    }
    
    protected void getResources(List<Resource> list, boolean recursive)
            throws IOException {
              /*
        File[] dir = directory.listFiles();

        for (File file: dir) {
            if (file.isFile()) {
                Resource resource = lookupResource(file.getName());
                list.add(resource);
            } else if (recursive && file.isDirectory()) {
                AbstractRepository repo = lookupRepository(file.getName());
                repo.getResources(list, true);
            }
        }
        */
        return; // FIXME - how do we actually check this?
    }

    public Repository[] getRepositories() throws IOException {
        List<Repository> list = new ArrayList<Repository>(0);
/*        File[] dir = directory.listFiles();

        for (File file: dir) {
            if (file.isDirectory()) {
                list.add(lookupRepository(file.getName()));
            }
        }
*/        
        return list.toArray(new Repository[list.size()]);
    }

    public URL getUrl() throws MalformedURLException {
        // Trailing slash on directories is required for ClassLoaders
        return directory;
    }

    @Override
    public int hashCode() {
        return 17 + (37 * directory.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WebRepository &&
               directory.equals(((WebRepository) obj).directory);
    }

    @Override
    public String toString() {
        return new StringBuffer("WebRepository[").append(path).append("]").toString();
    }
}
