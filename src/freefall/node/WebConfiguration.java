/*
 *  Copyright 2008 Hannes Wallnoefer <hannes@helma.at>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package freefall.node;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.ringojs.engine.RingoWrapFactory;
import org.ringojs.util.StringUtils;
import org.ringojs.repository.*;
import org.mozilla.javascript.ClassShutter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.ringojs.tools.*;

/**
 * This class describes the configuration for a RingoJS application or shell session.
 * @author hannes
 */
public class WebConfiguration extends RingoConfiguration {
    /**
     * Create a new Ringo web configuration and sets up its module search path.
     *
     * @param ringoHome the ringo installation directory
     * @param modulePath the module search path as comma separated string
     * @param systemModules system module path to append to module path, or null
     * @throws FileNotFoundException if a moudule path item does not exist
     */
     private static Logger log=Logger.getLogger(WebConfiguration.class.getName());
     private Resource mainResource;
     
    public WebConfiguration(Repository ringoHome, String[] modulePath, String systemModules) throws IOException
    {
      super(ringoHome, null, null);
     
//      addModuleRepository(ringoHome);
      
        // append system modules path relative to ringo home
        if (systemModules != null) {
            addModuleRepository(resolveRootRepository(systemModules));
        }
        
      
        addModuleRepository(resolveRootRepository("packages"));
        addModuleRepository(resolveRootRepository("packages/appengine/lib"));
        
        if (modulePath != null) {
            for (String aModulePath : modulePath) {
                String path = aModulePath.trim();
                addModuleRepository(resolveRootRepository(path));
            }
        }        
    }

    /**
     * Resolve a module repository path.
     * @param path the path
     * @return a repository
     * @throws FileNotFoundException if the path couldn't be resolved
     */
    public Repository resolveRootRepository(String path) throws IOException
    {
//      log.info("Resolving "+path);
      
      try
      {
        return super.resolveRootRepository(path);
      }
      catch(FileNotFoundException e)
      {
        log.info("Resolving web "+path);
        return new WebRepository(path);
      }
    }

    /**
     * Get a resource from our script repository
     * @param path the resource path
     * @return the resource
     * @throws IOException an I/O error occurred
     */
    public Resource getResource(String path) throws IOException
    {
        log.info("WebConfiguration.getResource("+path+")");
        log.info("repositories: "+getRepositories());
        if(getRepositories()!=null)
        {
          for(Repository repo: getRepositories())
          {
              log.info(repo+".getResource("+path+")");
              Resource res = repo.getResource(path);
              log.info("resource: "+res+" "+res.exists());
              if (res != null && res.exists())
              {
                return res;
              }
          }
        }
        return new NotFound(path);
    }

    /**
     * Set the main script for this configuration. If the scriptName argument is not null,
     * we check whether the script is already contained in the ringo module path.
     *
     * If not, the script's parent repository is prepended to the module path. If scriptName is
     * null, we prepend the current working directory to the module path.
     * @param scriptName the name of the script, or null.
     * @throws FileNotFoundException if the script repository does not exist
     */
    public void setMainScript(String scriptName) throws IOException
    { 
      Resource script = getResource(scriptName);
      if (!script.exists()) {
        // no luck resolving the script name, give up
        throw new FileNotFoundException("Can't find file " + scriptName);
      }
      // found the script, so set mainModule
      mainResource = script;
    }
}
