///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//DEPS org.thymeleaf:thymeleaf:3.0.12.RELEASE
//DEPS com.fasterxml.jackson.core:jackson-databind:2.11.1

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;
  
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;

@Command(name = "compile", mixinStandardHelpOptions = true, version = "compile 0.1",
    description = "compile thymeleaf templates")
class compile implements Callable<Integer> {

    @Parameters(index = "0", description = "Input file to compile")
    private String inputFile;

    @Parameters(index = "1", description = "Output location", defaultValue = "./output.html")
    private String outputFile;

    @Parameters(index = "2", description = "Payload")
    private String payloadFile;

    @Option(names = { "-e", "--entry" }, description = "the key used when injecting data", defaultValue="data")
    private String entryKey;


    public static void main(String...args) {
        int exitCode = new CommandLine(new compile()).execute(args);
        System.exit(exitCode);
    }

    private String readFile(String filename) {
        String data = "";
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data += myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return data;
    }

    private void writeFile(String filename, String contents) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private LinkedHashMap parsePayload(String filename){
        try{ 
            File file = new File(filename);
            ObjectMapper mapper = new ObjectMapper(); 
            LinkedHashMap map = mapper.readValue(file, LinkedHashMap.class);
            return map;
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        final TemplateEngine templateEngine = new TemplateEngine();
        Context ctx = new Context();
        LinkedHashMap data = this.parsePayload(this.payloadFile);

        ctx.setVariable(this.entryKey, data);

        String output = templateEngine.process(readFile(this.inputFile), ctx);
        writeFile(this.outputFile, output);
        return 0;
    }
}
