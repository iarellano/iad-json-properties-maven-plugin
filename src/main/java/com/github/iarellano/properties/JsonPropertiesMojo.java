package com.github.iarellano.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.util.*;

@Mojo( name = "load-json-properties", defaultPhase = LifecyclePhase.INITIALIZE)
public class JsonPropertiesMojo extends AbstractMojo
{


    /**
     *  Value to be prefixed to property names. e.g Given a json
     *
     *  Defaults to empty string
     */
    @Parameter
    private String prefix = "";

    /**
     * List of json files to load properties from.
     */
    @Parameter(property = "files", alias = "files")
    private JsonFile[] files = new JsonFile[0];

    /**
     * Configuration for JsonPath extraction.
     */
    @Parameter(property = "jsonPath", alias = "jsonPath")
    private JsonFile[] jsonPaths = new JsonFile[0];

    /**
     * Inidicates if current execution of this plugin must be skipped.
     *
     * Defaults to false
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    private Properties properties = new Properties();

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution as per skip=true");
            return;
        }

        try {
            if (files != null && files.length > 0) {
                validateFiles(files);
            }

            if (jsonPaths != null && jsonPaths.length > 0) {
                validateJsonPaths(jsonPaths);
            }

            if (files != null && files.length > 0) {
                processFiles(files);
            }

            if (jsonPaths != null && jsonPaths.length > 0) {
                processJsonPaths(jsonPaths);
            }

            System.getProperties().putAll(properties);
        } catch (MojoExecutionException mee) {
            throw mee;
        } catch (IOException ioe) {
            // Should never happen since files are already validated
        }

        if (getLog().isDebugEnabled()) {
            StringWriter sw = new StringWriter();
            System.getProperties().list(new PrintWriter(sw));
            getLog().info(sw.getBuffer().toString());
        }
    }

    private void processFiles(JsonFile[] files) throws FileNotFoundException {
        int filesCount = 0;
        int newProperties = 0;
        int filesSkipped = 0;
        for (int i = 0; i < files.length; i++) {
            JsonFile jsonFile = files[i];
            if (jsonFile.getFilePath().exists()) {
                filesCount++;
                Properties properties = new Properties();
                FileInputStream fis = new FileInputStream(jsonFile.getFilePath());
                JsonElement jsonElement = new JsonParser().parse(new InputStreamReader(fis));
                loadProperties(jsonElement, this.prefix + files[i].getPrefix(), properties);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("File " + jsonFile.getFilePath() + ". " + properties.size() + " properties loaded");
                }
                newProperties += properties.size();
                this.properties.putAll(properties);
            } else {
                filesSkipped++;
                getLog().warn("File not found " + jsonFile.getFilePath() + ". Skipping!");
            }
        }
        getLog().info("Files processed " + filesCount + ". New properties " + newProperties);
        if (filesSkipped > 0) {
            getLog().info("Files skipped " + filesSkipped);
        }
    }

    private void processJsonPaths(JsonFile[] jsonFiles) throws IOException {
        int filesCount = 0;
        int newProperties = 0;
        int filesSkipped = 0;
        for (int i = 0; i < jsonFiles.length; i++) {
            JsonFile jsonFile = jsonFiles[i];
            if (jsonFile.getFilePath().exists()) {
                Properties properties = new Properties();
                String json = FileUtils.fileRead(jsonFile.getFilePath(), jsonFile.getCharset());
                Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
                for (int j = 0; j < jsonFile.getLookups().length; j++) {
                    Lookup lookup = jsonFile.getLookups()[j];
                    if (!lookup.isFailFast()) {
                        try {
                            String value = JsonPath.read(document, lookup.getJsonPath()).toString();
                            properties.put(this.prefix + jsonFile.getPrefix() + lookup.getPropertyName(), value);
                        } catch (PathNotFoundException e) {
                            if (getLog().isDebugEnabled()) {
                                getLog().error("JsonPath error with path " + lookup.getJsonPath() + " at file " + jsonFile.getFilePath());
                            }
                        }
                    } else {
                        String value = JsonPath.read(document, lookup.getJsonPath()).toString();
                        properties.put(this.prefix + jsonFile.getPrefix() + lookup.getPropertyName(), value);
                    }
                }
                if (getLog().isDebugEnabled()) {
                    getLog().debug("File " + jsonFile.getFilePath() + ". " + properties.size() + " properties loaded");
                }
                filesCount++;
                newProperties += properties.size();
                this.properties.putAll(properties);

            } else {
                filesSkipped++;
                getLog().warn("File not found " + jsonFile.getFilePath() + ". Skipping!");
            }
            if (filesSkipped > 0) {
                getLog().info("Files skipped " + filesSkipped);
            }
        }
        getLog().info("Files processed " + filesCount + ". New properties from JsonPath " + newProperties);
    }

    private void loadProperties(JsonElement jsonElement, String path, Properties properties) {

        if (jsonElement.isJsonObject()) {
            JsonObject jo = jsonElement.getAsJsonObject();
            for (String propName: jo.keySet()) {
                JsonElement child = jo.get(propName);
                if (child.isJsonObject()) {
                    loadProperties(child, path + propName + ".", properties);
                } else if (child.isJsonArray()) {
                    loadProperties(child, path + propName, properties);
                } else if (child.isJsonPrimitive()) {
                    properties.put(path + propName, child.getAsString());
                } else if (child.isJsonNull()) {
                    properties.put(path + propName, "");
                }
            }
        }

        if (jsonElement.isJsonArray()) {
            JsonArray ja = jsonElement.getAsJsonArray();
            for (int i = 0, size = ja.size(); i < size; i++) {
                JsonElement item  = ja.get(i);
                if (item.isJsonPrimitive()) {
                    properties.put((path.endsWith(".") ? path : path + ".") + i, item.getAsString());
                } else if (item.isJsonNull()) {
                    properties.put((path.endsWith(".") ? path : path + ".") + i, "");
                } else {
                    loadProperties(item, (path.endsWith(".") ? path : path + ".") + i + ".", properties);
                }
            }
        }
    }

    private void validateFiles(JsonFile[] files) throws MojoExecutionException {
        for (int i = 0; i < files.length; i++) {
            JsonFile jsonFile = files[i];
            File source = jsonFile.getFilePath();
            if (jsonFile.isFailIfFileNotFound() && !source.exists() || !source.isFile()) {
                throw new MojoExecutionException("File not found " + source.getPath());
            }
        }
    }

    private void validateJsonPaths(JsonFile[] files) throws MojoExecutionException {
        validateFiles(files);
        for (int i = 0; i < files.length; i++) {
            JsonFile jsonFile = files[i];
            for (int j = 0; j < jsonFile.getLookups().length; j++) {
                Lookup lookup = jsonFile.getLookups()[j];
                if (StringUtils.isEmpty(lookup.getPropertyName())) {
                    throw new MojoExecutionException("Empty or missing propertyName");
                }
                if (StringUtils.isBlank(lookup.getJsonPath())) {
                    throw new MojoExecutionException("Empty or missing jsonPath");
                }
                JsonPath.compile(lookup.getJsonPath());
            }
        }
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setFiles(JsonFile[] files) {
        this.files = files;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void setJsonPaths(JsonFile[] jsonPaths) {
        this.jsonPaths = jsonPaths;
    }
}
