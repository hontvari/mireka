package mireka.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptApi {
    private static final Logger logger = LoggerFactory
            .getLogger(ScriptApi.class);
    public static ScriptEngine engine;
    public static final Stack<File> includeStack = new Stack<>();

    public static Object include(String fileName) throws IOException,
            ScriptException {
        File file = new File(fileName);
        String oldSourceFileName = (String) engine.get(ScriptEngine.FILENAME);
        engine.put(ScriptEngine.FILENAME, file.toString());
        includeStack.push(file);
        try (InputStreamReader reader =
                new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            logger.debug("Evaluating " + file.toString() + "...");
            Object result = engine.eval(reader);
            logger.debug("Completed " + file.toString());
            includeStack.pop();
            engine.put(ScriptEngine.FILENAME, oldSourceFileName);
            return result;
        }
    }

    public static Object manage(Object object) {
        Lifecycle.addManagedObject(object);
        return object;
    }

}
