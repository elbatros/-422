
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * This program matches a product listing from a 3rd party retailer, 
 * e.g. “Nikon D90 12.3MP Digital SLR Camera (Body Only)”
 * against a set of known products, e.g. “Nikon D90”.
 * 
 * There's a set of products and a set of price listings matching some of those products. 
 * The task is to match each listing to the correct product with high precision.
 * 
 * @author Kleanthi Tupe - May 10, 2017
 */

public class SortableSolution
{
    public static void main(String[] args)
    {   
        List<ProductsClass> productsArray = new ArrayList<>();
        List<String> listingsObjects = new ArrayList<>();
        productsArray = readJsonFile("products.txt");
        listingsObjects = readListingsFile("listings.txt");
        Collections.sort(listingsObjects.subList(1, listingsObjects.size()));
        compareEachElement(productsArray, listingsObjects);      
    }  
    
    public static void compareEachElement(List<ProductsClass> productsArray, List<String> listingsObjects){
         // After creating two array lists of objects accordingly, iterate through listings array and check for matches
        // in the products array. Add the matches to the results file.
        BufferedWriter bw = null;
        FileWriter fw = null;
        int countMatches = 0;
        try {
            File file = new File("Results.txt");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            String productName = "";
            String matchingObjects = "";
            String pName = "{\"product_name\":";

            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            
            // iterate through the array lists and find the matches, one listing matches maximum one product
            List<MatchesToPrint> matchesArray = new ArrayList<>();
            Boolean foundMatch = false;
            
            for (ProductsClass productPrint : productsArray) {
                matchingObjects = "";
                foundMatch = false;
                
                for (int i=0; i<listingsObjects.size(); i++) {
                    //replace all '_' chars from product name with an empty space to do the string comparisons
                    productName = productPrint.getProductName().replaceAll("_", " ");
                    
                    if (listingsObjects.get(i).toLowerCase().contains(productName.toLowerCase())) {
                        if (containsExactly(listingsObjects.get(i).toLowerCase(), productName.toLowerCase()) && !foundMatch) {
                            foundMatch = true;  
                            countMatches++;
                            matchingObjects = listingsObjects.get(i);
                            listingsObjects.set(i, "");
                        }else if (containsExactly(listingsObjects.get(i).toLowerCase(), productName.toLowerCase()) && foundMatch) {  
                            countMatches++;
                            matchingObjects = matchingObjects + ", " + listingsObjects.get(i);
                            listingsObjects.set(i, "");
                        } 
                    } 
                }
                if (foundMatch){
                matchesArray.add(new MatchesToPrint(productPrint.getProductName() , matchingObjects));
                bw.write(pName + String.format("\"%s\",", productPrint.getProductName()) 
                                        + "\"listings\"" + ":[" + matchingObjects
                                        + "]}\n");
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
            }
            System.out.println("Finished processing files. The number of matched listings is: " + countMatches);
        }
    }

    //reads the file that contains JSON lines and creates the objects accordingly
    public static List<ProductsClass> readJsonFile(String fileName) {
        List<ProductsClass> productsArray = new ArrayList<>();
        
        BufferedReader br = null;
        JSONParser parser = new JSONParser();

        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {
                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    String pName = (String) jsonObject.get("product_name");
                    String pManufact = (String) jsonObject.get("manufacturer");
                    String pModel = (String) jsonObject.get("model");
                    productsArray.add(new ProductsClass(pName));
                } catch (ParseException e) {
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
            }
        }
        return productsArray;
    }
    
    //Function that reads the listings file and create Listings objects accordingly
    public static List<String> readListingsFile(String listingsFile){
        List<String> listingsObjects = new ArrayList<>();
         BufferedReader br = null;
        FileReader fr = null;

        try {
            fr = new FileReader(listingsFile);
            br = new BufferedReader(fr);

            String sCurrentLine;

            br = new BufferedReader(new FileReader(listingsFile));
           
            while ((sCurrentLine = br.readLine()) != null) {  
                listingsObjects.add(sCurrentLine);
            }

        } catch (IOException e) {
        } finally {
            try {

                if (br != null) {
                    br.close();
                }

                if (fr != null) {
                    fr.close();
                }

            } catch (IOException ex) {
            }
        }
        return listingsObjects;
    }
    
    //function that checks if there's an exact match, and not a partial match
    private static boolean containsExactly(String listingTitle, String productName) {
        String pattern = "\\b" + productName + "\\b";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(listingTitle);
        return m.find();
    }
}
 



class ListingsClass {
    public String title;
    public String wholeJSON;
    
    public ListingsClass()
    {
        title = "";
        wholeJSON = "";
    }
    
    public ListingsClass(String line){
        wholeJSON = line;
    }
    
    public String getTitle(){
       return wholeJSON;
    }
    
    public void setTitle(String line){
        wholeJSON = line;
    }    
}


class MatchesToPrint {
    public String productName;
    public String listings;
    
    public MatchesToPrint(){
    productName = "";
    listings = "";
    }
    
    public MatchesToPrint(String t, String l){
    productName = t;
    listings = l;
    }
    
    public void setProductName(String t){
        productName = t;
    }
    public void setListing(String l){
        listings = l;
    }
    
    public String getProductName(){
        return productName;
    }
    
    public String getListing(){
    return listings;
    }
    
    public String getPairs(){
        return productName + listings;
    }
}



class ProductsClass {
    public String product_name;
    
    public ProductsClass(){
        product_name = "";
    }
    
    public ProductsClass(String p){
        product_name = p;
    }
    
    public String getProductName(){
       return product_name;
    }
        
    public void setProductName(String p){
        product_name = p;
    }
}
