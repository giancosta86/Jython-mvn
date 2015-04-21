/*
 * ==========================================================================%%#
 * Jython-mvn
 * ===========================================================================%%
 * Copyright (C) 2014 - 2015 Gianluca Costa
 * ===========================================================================%%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==========================================================================%##
 */
package info.gianlucacosta.jythonmvn;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Runs the Jython interpreter.
 * <p>
 * The interpreter exposes a few additional global variables to Python code:
 * <ul>
 * <li><i>project</i>: the current Maven project as an object.</li>
 * <li><i>properties</i>: the Maven properties of the current project. Very useful for inspecting and changing them.</li>
 * </ul>
 */
@Mojo(
        name = "run",
        requiresProject = true
)
public class JythonRun extends AbstractMojo {
    private static final Map<String, ScriptEngine> engines = new HashMap<>();

    /**
     * The source code as a string run by the Jython interpreter.
     */
    @Parameter
    private String code;

    /**
     * The Jython script file to be executed.
     */
    @Parameter
    private String scriptFile;

    /**
     * The session name.
     * <p>
     * If omitted, the Jython code runs in a brand-new interpreter;
     * otherwise, the interpreter will be kept in memory and shared by plugin
     * executions that, in the context of the same build lifecycle,
     * have the same session name.
     */
    @Parameter
    private String session;

    /**
     * The charset of the script file.
     * <p>
     * Unused when the <i>code</i> parameter is set.
     */
    @Parameter(defaultValue = "utf-8")
    private String charset;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateParameters();

        ScriptEngine engine;

        if (session != null) {
            engine = engines.get(session);

            if (engine == null) {
                engine = createScriptEngine();
                engines.put(session, engine);
            }
        } else {
            engine = createScriptEngine();
        }


        String actualCode;
        try {
            actualCode = getActualCode();
        } catch (IOException ex) {
            throw new MojoFailureException("Cannot retrieve the Jython code", ex);
        }

        try {
            engine.eval(actualCode);
        } catch (ScriptException ex) {
            throw new MojoFailureException("Cannot execute the Jython code", ex);
        }
    }


    private void validateParameters() throws MojoExecutionException {
        if (code == null && scriptFile == null) {
            throw new MojoExecutionException("Either code or scriptFile must be specified");
        }

        if (code != null && scriptFile != null) {
            throw new MojoExecutionException("Cannot specify both code and scriptFile");
        }

        if (scriptFile != null) {
            File sourceFileObject = new File(scriptFile);

            if (!sourceFileObject.exists()) {
                throw new MojoExecutionException("The script file must exist");
            }
        }
    }


    private ScriptEngine createScriptEngine() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");

        MavenProject project = (MavenProject) getPluginContext().get("project");
        engine.put("project", project);

        Properties properties = project.getProperties();
        engine.put("properties", properties);

        return engine;
    }


    private String getActualCode() throws IOException {
        if (code != null) {
            return code;
        }

        return new String(
                Files.readAllBytes(Paths.get(scriptFile)),
                Charset.forName(charset)
        );
    }
}
