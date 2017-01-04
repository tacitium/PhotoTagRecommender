// MMSA4 Assessed Exercise 2016
// Done by: Terence Tan Boon Kiat
// SIT Matriculation ID: 15AC083B
// Glasgow ID: 2228167T
// This is my own work as defined in the Academic Ethics agreement I have signed.

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class tagRecommender {

    static HashMap<String, HashMap<String, Double>> coOccurrenceMatrix = new HashMap<>(); // for storing co-occurence values
    static HashMap<Integer, ArrayList<String>> photoTagsMap = new HashMap(); // for storing phototags file data
    static HashMap<String, Double> tagsMap = new HashMap(); // for storing tags file data

    public static void main(String[] args) {
        String photoTagsCSV = "photos_tags.csv";
        String tagsCSV = "tags.csv";
        String outputCSV = "tagsMatrix.csv";
        try {
            // begin task 1
            System.out.println("Beginning task one...");
            readTagsFile(tagsCSV); // call method to save all tags.csv data into tagsMap
            readPhotoTagsFile(photoTagsCSV); // call method to save all photos_tags.csv data into photoTagsMap
            processPhotoTags(); // call method to process photo tags into co-occurrence matrix
            writeToFile(outputCSV); // output co-occurrence matrix to csv file
            System.out.println("Task one completed.\n");
            // end of task 1
            // begin task 2
            System.out.println("Beginning task two...");
            processTopFiveTags("Water");
            processTopFiveTags("People");
            processTopFiveTags("London");
            System.out.println("Task two completed.\n");
            // end of task 2
            // begin task 3
            System.out.println("Beginning task three...");
            processTopFiveTagsByIDF("Water");
            processTopFiveTagsByIDF("People");
            processTopFiveTagsByIDF("London");
            System.out.println("Task three completed.\n");
            // end of task 3
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------- begin task 1 ---------------- //
    // simple method to read all tags and its count value into tagsMap
    private static void readTagsFile(String name) throws FileNotFoundException, IOException {
        File tagsFile = getFile(name); // get tags file from directory

        if (tagsFile != null) { // if file exists then proceed to read data
            FileReader fReader = new FileReader(tagsFile); // create a filereader to read tags file
            BufferedReader bReader = new BufferedReader(fReader); // create a bufferedreader to read each line in file
            String line = ""; // initialise line

            HashMap<String, Double> tempTagsMap = new HashMap(); // temporary hashmap to initialise co-occurrence matrix

            while ((line = bReader.readLine()) != null) { // read every line while there are more lines in file
                String tagName = line.split(",")[0]; // split line and get tag name from first array position
                double numPhotos = Double.parseDouble(line.split(",")[1]); // split line and get count from second array position
                tagsMap.put(tagName, numPhotos); // populate tags map
                tempTagsMap.put(tagName, 0.0); // initialise to 0 for matrix count later
                coOccurrenceMatrix.put(tagName, tempTagsMap); // empty co-occurrence matrix populating to 0 for count later
            }
            bReader.close(); // close bufferedreader
        }
    }

    // simple method to read all photo ids and tags that are used the photos into photoTagsMap
    private static void readPhotoTagsFile(String name) throws FileNotFoundException, IOException {
        File photoTagsFile = getFile(name); // get photos_tags file from directory

        if (photoTagsFile != null) { // check if file exists
            FileReader fReader = new FileReader(photoTagsFile); // create a filereader to read photos_tags file
            BufferedReader bReader = new BufferedReader(fReader); // create a bufferedreader to read each line in file
            String line = ""; // initialise line

            while ((line = bReader.readLine()) != null) { // read every line while there are more lines in file
                int photoID = Integer.parseInt(line.split(",")[0]); // split line and get photo id from first array position
                String tagName = line.split(",")[1]; // split line and get tag name from second array position

                if (!photoTagsMap.containsKey(photoID)) { // check if first time adding photoID into map
                    ArrayList<String> newPhotoTagsList = new ArrayList(); // create a new arraylist for storing all the photoID's tags
                    newPhotoTagsList.add(tagName); // add first tag into list
                    photoTagsMap.put(photoID, newPhotoTagsList); // put the list mapped to photoID as key into photoTagsMap
                } else { // the photoID is already in the map
                    ArrayList<String> existingPhotoTagsList = photoTagsMap.get(photoID); // get a existing arraylist from the map
                    existingPhotoTagsList.add(tagName); // add tag into the existing list
                    photoTagsMap.put(photoID, existingPhotoTagsList); // put the list mapped to photoID as key into photoTagsMap
                }
            }
            bReader.close(); // close bufferedreader
        }
    }

    // method to process all tags into co-occurrence matrix
    private static void processPhotoTags() {
        for (Entry<Integer, ArrayList<String>> entry : photoTagsMap.entrySet()) { // loop through each entry in the photoTagsMap
            ArrayList<String> tagsList = entry.getValue(); // get tagsList of current photoID from each entry
            for (String tag_i : tagsList) { // first loop through all tags in the tagsList
                for (String tag_j : tagsList) { // second loop to check if tag is itself (cannot count as co-occur)
                    HashMap<String, Double> tagCount = new HashMap<>(coOccurrenceMatrix.get(tag_i)); // temporary hashmap to count tag occurrences of current photoID's tag
                    if (tag_i.equalsIgnoreCase(tag_j)) { // same values
                        tagCount.put(tag_j, -1.0); // if same tag name, replace value to -1
                        coOccurrenceMatrix.put(tag_i, tagCount); // save value to matrix
                    } else { // different values
                        tagCount.put(tag_j, tagCount.get(tag_j) + 1.0); // different tags, add one to count
                        coOccurrenceMatrix.put(tag_i, tagCount); // save value to matrix
                    }
                }
            }
        }
    }

    // simple filewriter to write co-occurrence matrix to csv file
    private static void writeToFile(String fileName) throws IOException {
        FileWriter fWriter = new FileWriter(fileName); // create filewriter

        for (Entry<String, Double> tag : tagsMap.entrySet()) { // loop through all entries in tagsMap
            fWriter.append("," + tag.getKey()); // populate header with tag names
        }
        for (Entry<String, HashMap<String, Double>> entry : coOccurrenceMatrix.entrySet()) { // loop through all entries in co-occurrence matrix
            HashMap<String, Double> tagCountMap = entry.getValue(); // get tagCount map (of other tags to current entry tag) from co-occurrence matrix
            fWriter.append("\n" + entry.getKey()); // populate first column with tag names
            for (Entry<String, Double> entryTag : tagCountMap.entrySet()) {
                if (entryTag.getValue() < 0) { // check for same tag name where value was set to -1 previously
                    fWriter.append(",-"); // populate with a '-' for redundant value
                } else {
                    fWriter.append("," + entryTag.getValue()); // populate next column with tag count
                }
            }
        }
        fWriter.close(); // close filewriter
        System.out.println("Co-Occurrence Matrix written to CSV file: " + fileName);
    }

    // simple method to check if file exists and return file else return null
    private static File getFile(String inputName) {
        File tempFile = new File(inputName);

        if (tempFile.exists() && !tempFile.isDirectory()) { // if exist and not a folder then return the file
            return tempFile;
        }
        return null;
    }
    // ---------------- end of task 1 ---------------- //

    // ---------------- begin task 2 ---------------- //
    private static void processTopFiveTags(String tagName) {
        LinkedHashMap<String, Double> topFiveMap = getTopFive(coOccurrenceMatrix.get(tagName.toLowerCase())); // call getTopFive method to sort the co-occurrence values into top 5
        System.out.println("Top 5 most popular tags for <" + tagName + "> are " + topFiveMap.keySet() + " with values respectively: " + topFiveMap.values());
    }

    // method to get top five values in a hashmap by sorting using comparator
    private static LinkedHashMap<String, Double> getTopFive(HashMap<String, Double> map) {
        ArrayList<Entry<String, Double>> tempList = new ArrayList<Entry<String, Double>>(map.entrySet()); // store map entries to a temporary arraylist for using comparator later

        Comparator desc = new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> entryOne, Entry<String, Double> entryTwo) {
                return entryTwo.getValue().compareTo(entryOne.getValue());
            }
        }; // comparator for comparing second value with first value to get descending order

        Collections.sort(tempList, desc); // sort the list in descending order based on values

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap(); // linkedhashmap to store by insertion order
        for (Entry<String, Double> entry : tempList.subList(0, 5)) { // loop through the sorted tempList
            sortedMap.put(entry.getKey(), entry.getValue()); // insert tempList's first 5 values into map
        }

        return sortedMap;
    }
    // ---------------- end of task 2 ---------------- //

    // ---------------- begin task 3 ---------------- //
    // simple IDF calculator method
    private static double calIDF(double xTag, int collectionSize) {
        double xIDF = Math.log(collectionSize / xTag); // collectionSize is the count of all tags
        return xIDF;
    }
    
    // simple method to compute IDF multiply co-occurrence value to get top five
    private static void processTopFiveTagsByIDF(String tagName) {
        HashMap<String, Double> tagsIDFMap = new HashMap(coOccurrenceMatrix.get(tagName.toLowerCase())); // save tagName's co-occurred tags and their values into a map
        for (Entry<String, Double> entry : tagsIDFMap.entrySet()) { // loop through each co-occurred tag
            double tagIDF = calIDF(tagsMap.get(entry.getKey()), photoTagsMap.size()); // for each co-occurred tag's count (from tags.csv), calculate the IDF
            entry.setValue(tagIDF * entry.getValue()); // repalce the original co-occurrence value with IDF * co-occurrence value
        }
        LinkedHashMap<String, Double> topFiveIDFMap = getTopFive(tagsIDFMap); // call getTopFive method to sort the map into top 5
        System.out.println("Top 5 tags (based on popularity and significance) for <" + tagName + "> are: " + topFiveIDFMap.keySet() + " with values respectively: " + topFiveIDFMap.values());
    }

    // ---------------- end of task 3 ---------------- //
}
