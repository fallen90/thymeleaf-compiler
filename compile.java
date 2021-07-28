///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.slf4j:slf4j-simple:1.6.1
//DEPS info.picocli:picocli:4.5.0
//DEPS org.thymeleaf:thymeleaf:3.0.12.RELEASE
//DEPS com.fasterxml.jackson.core:jackson-databind:2.11.1

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
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

    private void writeFile(String filename, String contents) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error IOException. " + e.toString());
        }
    }

    private LinkedHashMap parsePayload(String filename){
        try{ 
            File file = new File(filename);
            ObjectMapper mapper = new ObjectMapper(); 
            LinkedHashMap map = mapper.readValue(file, LinkedHashMap.class);
            return map;
        } catch (Exception e) {
            System.out.println("An error occurred. " + e.toString());
        }

        return null;
    }

    private FileTemplateResolver templateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        return resolver;
    }

    private LinkedHashMap getData(){
        LinkedHashMap data = this.parsePayload(this.payloadFile);
        return data;
    }

    private TemplateEngine createTemplateEngine(){
        final TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(this.templateResolver());
        return templateEngine;
    }

    @Override
    public Integer call() { // your business logic goes here...
         try {
            TemplateEngine templateEngine = this.createTemplateEngine();
            Context ctx = new Context();
            ctx.setVariable(this.entryKey, this.getData());
            String output = templateEngine.process(this.inputFile, ctx);
            writeFile(this.outputFile, output);
        } catch(org.thymeleaf.exceptions.TemplateProcessingException ex){
            System.out.println("\033[0;31m" + "[THYMELEAF]\n\n" + ex.getMessage() +  "\033[0m");
        }
        
        return 0;
    }
}
