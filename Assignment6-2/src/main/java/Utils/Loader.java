package Utils;


import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Loader {

    private List<File> files = new ArrayList<>();
    private List<String> fileNames = new ArrayList<>();

    public void getTxtFiles(File file){
        if (!file.isDirectory()){
            String filePath = file.toString();
            if (filePath.endsWith(".txt")){
                files.add(file);
                String[] paths = filePath.split("/");
                fileNames.add(paths[paths.length-1]);
            }
        } else {
            for (File subfile: file.listFiles()){
                getTxtFiles(subfile);
            }
        }
    }

    public List<File> readFiles(String path){
        files.clear();
        File rootPath = new File(path);
        getTxtFiles(rootPath);
        System.out.format("We found %d txt files in total\n",files.size());
        System.out.println("==========================================================================");
        return files;
    }

    public List<String> parseFile (File file) throws IOException {
        List<String> words = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) { // read line by line
            if (!line.isEmpty()){
                for (String word: line.split("\\s+"))
                    words.add(word);
            }
        }
        br.close();
        return words;
    }

    public List<File> getFiles() {
        return files;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

}
