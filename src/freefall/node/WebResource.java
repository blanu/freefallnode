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
 * $RCSfile: WebResource.java,v $
 * $Author: hannes $
 * $Revision: 1.8 $
 * $Date: 2006/04/07 14:37:11 $
 */

package freefall.node;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.ringojs.repository.*;
import java.util.logging.Logger;

public class WebResource extends AbstractResource
{
    private static final Logger log = Logger.getLogger(WebResource.class.getName());
    URL url;
    String contents;

    public WebResource(String path) throws IOException {
        this(new URL(path), null);
    }

    public WebResource(URL u) throws IOException {
        this(u, null);
    }

    protected WebResource(URL u, WebRepository rep) throws IOException {
        log.info("WebResource("+u+", "+rep+")");
        url = u;

//        try
//        {
//        repository = rep == null ? new WebRepository(url) : rep;
        repository=null;
        String s=url.toString();
        int lastSlash=s.lastIndexOf('/');
        path = s.substring(0, lastSlash);
        name = s.substring(lastSlash, s.length());

        // base name is short name with extension cut off
        int lastDot = name.lastIndexOf(".");
        baseName = (lastDot == -1) ? name : name.substring(0, lastDot);

        contents=getContent();
        /*
        StringBuffer sb=new StringBuffer();
        char[] buff=new char[1024];
        BufferedReader reader=new BufferedReader(new InputStreamReader(url.openStream()));
        int read=reader.read(buff, 0, buff.length);
        while(read!=-1)
        {
          sb.append(buff, 0, read);
          read=reader.read(buff, 0, buff.length);
        }
        contents=sb.toString();
        log.info("contents: "+contents.length());
        }
        catch(Exception e)
        {
          log.info("Exception fetching web resource");
        }
        */
        log.info("contents: "+contents.length());
    }

    public InputStream getInputStream() throws IOException {
//        return new ByteArrayInputStream(contents.getBytes());
      return url.openStream();
    }

    public URL getUrl() throws MalformedURLException {
        return url;
    }

    public long lastModified() {
//        return file.lastModified();
      return 0;
    }

    public long getLength() {
      return contents.length();
    }

    public boolean exists() {
      return contents!=null;
    }

    @Override
    public int hashCode() {
        return 17 + url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WebResource && url.equals(((WebResource)obj).url);
    }

    @Override
    public String toString() {
        return url.toString();
    }
}
