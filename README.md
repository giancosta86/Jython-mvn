# Jython-mvn

*Runs Jython code within a Maven build*

Jython-mvn introduces scripting flexibility in the build lifecycle by executing instances of the Jython interpreter.


## Usage

First of all, add a reference to the [Hephaestus](https://bintray.com/giancosta86/Hephaestus) repository on Bintray:

```xml
<repositories>
    <repository>
        <id>hephaestus</id>
        <name>Hephaestus</name>
        <url>http://dl.bintray.com/giancosta86/Hephaestus</url>
    </repository>
</repositories>
```

then, declare the plugin within any `plugins` block of your POM:

```xml
<plugin>
    <groupId>info.gianlucacosta.jython-mvn</groupId>
    <artifactId>jython-mvn</artifactId>
    <version>2.0</version>
    
    <executions>
    (...)
    </executions>
</plugin>
```


Within the `executions` block, you can insert an arbitrary number of `execution` sub-blocks:

```xml
<execution>
    <!-- 
    If multiple executions of a plugin are defined, 
    id is mandatory and must be unique 
    --> 
    <id>executionId</id>

    <phase>compile</phase> <!-- or any another phase -->

    <goals>
        <goal>run</goal>
    </goals>


    <configuration>
        <!-- 
        Reads code from a script file. This is the recommended execution strategy 
        -->
        <scriptFile>test.py</scriptFile>
        
        <!-- 
        For very short snippets, you can use this parameter instead            
        -->
        <code>a = "Hello, world! ^__^"; print(a)</code>
        
        <!--
        The session name. 
        If omitted, the current plugin execution will create a new, dedicated interpreter.
        Plugin executions having the same session name will share the same interpreter.
        -->
        <session>testSession</session>
        
        <!--
        File encoding, used when opening a script file.
        The encoding must be a string compatible with Charset.forName(). 
        Default if omitted: utf-8
        -->
        <charset>utf-8</charset>
    </configuration>
</execution>
```


## Further references

* [Jython](http://www.jython.org/)
* [Python](https://www.python.org/)
