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
    private static final List<String> LinkList = new ArrayList<>();
    private static List<String> jsonList;
    private static boolean checkJson;

    public static void main(String[] args)
    {
        boolean error = false;
        int lastPage;
        getInput();
        UserAgent userAgent = new UserAgent();  //create new userAgent (headless browser)
        List<String> FirstList = new ArrayList<>();
        try //check username and find last page
        {
            FindFirstPage(userAgent, FirstList);
        }
        catch (Exception e)
        {
            System.out.println("Can't Find Username");
            error = true;
        }
        if (!error)
        {
            lastPage = LastBookmarkPage(FirstList);
            for (int i = 1; i <= lastPage + 1; i++) //find links on all pages
            {
                //System.out.println("Finding Links on Page: "+ i);
                FindLinks(i, userAgent);
            }
            List<String> finalList = getMatchingWorks(); // get all work links
            if (checkJson)
            {
                List<String> temp = compareLists(jsonList, finalList);
                for (String link : temp)
                {
                    System.out.println("Missing Item! " + link);
                }
                System.out.println("Missing Item Count: " + temp.size());
            }
        }

    }

    private static void getInput()
    {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter Username for bookmark lookup: ");
        username = in.nextLine();
        System.out.println("Check JSON File for comparison? ");
        String jsonCheck = in.nextLine();
        if (jsonCheck.toUpperCase().equals("YES"))
        {
            jsonInput(in);
        }
        in.close();
    }

    private static void jsonInput(Scanner in)
    {
        JSONFilters json = new JSONFilters();
        checkJson = true;
        System.out.print("JSON File Path: ");
        String jsonFile = in.nextLine();
        System.out.print("Output File Path: ");
        String outputFile = in.nextLine();
        System.out.print("Only Find A03 Links? ");
        String a03Answer = in.nextLine();

        if (a03Answer.toUpperCase().equals("YES"))
        {
            jsonList = json.getUrls(jsonFile, outputFile, true);
        } else
        {
            jsonList = json.getUrls(jsonFile, outputFile, false);
        }
    }

    private static void FindFirstPage(UserAgent userAgent, List<String> FirstList) throws ResponseException
    {
        userAgent.visit("https://archiveofourown.org/users/" + username + "/bookmarks");
        Elements links = userAgent.doc.findEvery("<a href"); //find all links
        for (Element link : links)
        {
            FirstList.add(link.getAtString("href"));
        }
    }

    private static void FindLinks(int page, UserAgent userAgent)
    {
        try
        {
            userAgent.visit("https://archiveofourown.org/users/" + username + "/bookmarks/?page=" + page); //visit url
            Elements links = userAgent.doc.findEvery("<a href"); //find all links
            for (Element link : links)
            {
                LinkList.add(link.getAtString("href")); // add links to list for sorting through
            }
        }
        catch (JauntException ex)
        {
            System.out.println("URL Exception: " + ex);
        }
    }

    private static int LastBookmarkPage(List<String> LinkList)
    {
        int lastPage = 1;
        int tempPage;
        int index;
        Pattern works = Pattern.compile("https://archiveofourown\\.org/users/" + username + "/bookmarks\\?page=\\d+");
        Matcher m;
        for (String currentLink : LinkList)
        {
            m = works.matcher(currentLink);
            if (m.matches())
            {
                index = currentLink.lastIndexOf('=') + 1;
                tempPage = Integer.parseInt(currentLink.substring(index));
                if (tempPage > lastPage)
                {
                    lastPage = tempPage;
                }
            }
        }
        return lastPage;
    }

    private static List<String> getMatchingWorks()
    {
        Pattern works = Pattern.compile("https://archiveofourown\\.org/works/\\d{5,}");
        Matcher m;
        List<String> finalList = new ArrayList<>();
        int count = 0;
        for (String currentLink : LinkList)
        {
            m = works.matcher(currentLink);
            if (m.matches())
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
        catch (Exception ex)
        {
            System.out.println(ex.toString());
        }
        System.out.println("Links sent to links.txt file!");
    }

    private static List<String> compareLists(List<String> firstList, List<String> secondList) //first list: firefox, second list: a03
    {
        List<String> temp = new ArrayList<>();
        for (String s : secondList)
        {
            if (!firstList.contains(s))
            {
                System.out.println("Comparing URL: " + s + "Contains Result: " + firstList.contains(s));
                temp.add(s);
            }
        }
        return temp;
    }
}

