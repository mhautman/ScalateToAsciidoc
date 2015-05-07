package eu.integrify.migration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;

public class Migrate {

    private static final Logger logger = LoggerFactory.getLogger(Migrate.class);
    private File folder;
    
    public static void main(String[] args){
        File file = new File(args[0]);
        Migrate migrate = new Migrate();
        migrate.listFilesForFolder(file);
    }


    public void listFilesForFolder(final File folder) {
        this.folder = folder;
        if (folder != null) {
        Iterator<File> iterator= FileUtils.iterateFiles(folder, new String[]{"conf"}, true);
            while (iterator.hasNext()) {
                File nextFile = iterator.next();
                if (nextFile.isDirectory()) {
                    listFilesForFolder(nextFile);
                } else {
                    try {
                        logger.debug(nextFile.getPath());
                        convertToAsciidoc(nextFile);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
    }

    private void convertToAsciidoc(File f) throws IOException {

        File tmp = new File(f.getParent()+ "\\" + FilenameUtils.removeExtension(f.getName()) + ".adoc");

        BufferedReader br = new BufferedReader(new FileReader(f));
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));

        String currentLine;

        while((currentLine = br.readLine())!=null)
        {
            if(currentLine.contains("{{")){
                currentLine = currentLine.replace("{{","_");
            }
            if(currentLine.contains("}}")){
                currentLine = currentLine.replace("}}","_");
            }
            if(currentLine.contains("h1.")){
                currentLine = currentLine.replace("h1.","=");
            }
            if(currentLine.contains("h2.")){
                currentLine = currentLine.replace("h2.","==");
            }
            if(currentLine.contains("h3.")){
                currentLine = currentLine.replace("h3.","===");
            }
            if(currentLine.contains("h4.")){
                currentLine = currentLine.replace("h4.","====");
            }
            if(currentLine.contains("{code}")){
                currentLine = currentLine.replace("{code}","----");
            }
            if(currentLine.contains("{code:lang=xml}")){
                currentLine = currentLine.replace("{code:lang=xml}","[source,xml]\n----");
            }
            if(currentLine.contains("{code:lang=java}")){
                currentLine = currentLine.replace("{code:lang=java}","[source,java]\n----");
            }
            if(currentLine.contains("{pygmentize}")){
                currentLine = currentLine.replace("{pygmentize}","----");
            }
            if(currentLine.contains("{pygmentize:xml}")){
                currentLine = currentLine.replace("{pygmentize:xml}","[source,xml]\n----");
            }
            if(currentLine.contains("{pygmentize:java}")){
                currentLine = currentLine.replace("{pygmentize:java}","[source,java] \n----");
            }
            if(currentLine.contains("{noformat}")){
                currentLine = currentLine.replace("{noformat}","----");
            }
            if(currentLine.startsWith("!") && currentLine.endsWith("!") ){
                String path = currentLine.contains("|")? currentLine.substring(1, currentLine.indexOf("|")) : currentLine.substring(1, currentLine.length()-1);
                String newLine = "image::" + path + "[]";
                currentLine = currentLine.replace(currentLine, newLine);
            }
            if(currentLine.contains("[") && currentLine.contains("]")){
                if(currentLine.contains("http") || currentLine.contains("https")) {
                    if (currentLine.contains("|")) {
                        String text = currentLine.substring(currentLine.indexOf("[") + 1, currentLine.indexOf("|"));
                        currentLine = currentLine.replace("|", "");
                        currentLine = currentLine.replace("]", "[" + text + "]");
                    }
                    currentLine = currentLine.replace("[", "");
                    currentLine = currentLine.replace("]", "");
                }
                else{
                    if (currentLine.contains("|")) {
                        if(currentLine.indexOf("|") > currentLine.indexOf("[") && currentLine.indexOf("|") < currentLine.indexOf("]")) {
                            String text = currentLine.substring(currentLine.indexOf("[") + 1, currentLine.indexOf("|"));
                            String path = currentLine.substring(currentLine.indexOf("|") + 1, currentLine.indexOf("]"));
                            currentLine = currentLine.replace(currentLine, currentLine.substring(0, currentLine.indexOf("[")) + "link:" + path + ".adoc" +"[" + text + "]");
                        }
                    }
                }
            }
            if(currentLine.contains("{include:")){
                currentLine = currentLine.replace("{include:","include::");
                if(currentLine.contains(".conf}")) currentLine = currentLine.replace(".conf}",".adoc[]");
                else{
                    currentLine = currentLine.replace("}",".adoc[]");
                }
            }
            bw.write(String.format("%s%n", currentLine));
        }
        br.close();
        bw.close();
    }
}

