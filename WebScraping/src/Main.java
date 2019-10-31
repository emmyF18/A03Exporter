import com.jaunt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    private static String username;
    private static List<String> LinkList = new ArrayList<>();
    public static void main(String[] args)
    {
        int lastPage;
        Scanner in = new Scanner(System.in);
        System.out.println("Enter Username for bookmark lookup: ");
        username= in.nextLine();
        System.out.println("Check JSON File for comparison? ");
        String jsonCheck = in.nextLine();
        List<String> jsonList = new ArrayList<>();
        if(jsonCheck.equals("Yes")) {
            JSONFilters json = new JSONFilters();
            jsonList = json.getUrls("fanficBookMarks.json", "FirefoxLinks.txt");
        }
        UserAgent userAgent = new UserAgent();  //create new userAgent (headless browser)
        List<String> FirstList = new ArrayList<>();
        FindFirstPage(userAgent,FirstList); // find first page links in order to find the number of bookmark pages
        lastPage = LastBookmarkPage(FirstList);
        for(int i = 1; i <= lastPage+1; i++) //find links on all pages
        {
            //System.out.println("Finding Links on Page: "+ i);
            FindLinks(i,userAgent);
        }
        List<String> finalList = getMatchingWorks(LinkList); // display all work links
        if(jsonCheck.equals("Yes"))
        {
            List<String> temp = compareLists(jsonList, finalList);
            for (String link : temp) {
                System.out.println("Missing Item! "+link);
            }
            System.out.println("Missing Item Count: " + temp.size());
        }

    }
    private static void FindFirstPage(UserAgent userAgent, List<String> FirstList)
    {
        try {
            userAgent.visit("https://archiveofourown.org/users/"+username+"/bookmarks"); //visit url
            Elements links = userAgent.doc.findEvery("<a href"); //find all links
            for (Element link : links)
            {
                FirstList.add(link.getAtString("href"));
            }
        }
        catch (JauntException ex)
        {
            System.out.println("URL Exception: "+ ex);
        }
    }
    private static void FindLinks(int page, UserAgent userAgent)
    {
        try {
            userAgent.visit("https://archiveofourown.org/users/"+username+"/bookmarks/?page="+page); //visit url
            Elements links = userAgent.doc.findEvery("<a href"); //find all links
            for (Element link : links) {
                LinkList.add(link.getAtString("href")); // add links to list for sorting through
            }
        }
        catch (JauntException ex)
        {
            System.out.println("URL Exception: "+ ex);
        }
    }
    private static int LastBookmarkPage(List<String> LinkList)
    {
        int lastPage = 1;
        int tempPage;
        int index;
        Pattern works = Pattern.compile("https://archiveofourown\\.org/users/"+username+"/bookmarks\\?page=\\d+");
        Matcher m;
        for(String currentLink : LinkList)
        {
            m = works.matcher(currentLink);
            if(m.matches())
            {
                index= currentLink.lastIndexOf('=')+1;
               tempPage = Integer.parseInt(currentLink.substring(index));
                if(tempPage > lastPage)
               {
                   lastPage = tempPage;
               }
            }
        }
        return lastPage;
    }
    private static List<String> getMatchingWorks(List<String> LinkList)
    {
        Pattern works = Pattern.compile("https://archiveofourown\\.org/works/\\d{5,}");
        Matcher m;
        List<String> finalList = new ArrayList<>();
        int count =0;
        for(String currentLink : LinkList)
        {
            m = works.matcher(currentLink);
            if(m.matches())
            {
                //System.out.println("Work Found! " + currentLink);
                finalList.add(currentLink);
                count++;
            }
        }
        writeToFile(finalList);
        System.out.println("Works found on A03: " + count);
        return finalList;
    }
    private static void writeToFile(List<String> finalList)
    {
        try
        {
            PrintWriter pw = new PrintWriter(new FileWriter("links.txt"));
            for (String link : finalList)
            {
               pw.println(link);
            }
            pw.close();
        }
         catch(Exception ex)
         {
             System.out.println(ex.toString());
         }
        System.out.println("Links sent to links.txt file!");
    }
    private static List<String> compareLists(List<String> firstList, List<String> secondList ) //first list: firefox, second list: a03
    {
        List<String> temp = new ArrayList<>();
        for (String s : secondList) {
            if (!firstList.contains(s))
            {
                temp.add(s);
            }
        }
        return temp;
    }
}

