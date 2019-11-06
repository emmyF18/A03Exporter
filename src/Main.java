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
    private static List<String> linkList = new ArrayList<>();
    private static List<String> jsonList;
    private static boolean checkJson;

    public static void main(String[] args) //TODO at some point move a03 stuff to separate class
    {
        boolean error = false;
        getInput();
        UserAgent userAgent = new UserAgent();  //create new userAgent (headless browser)
        List<String> FirstList = new ArrayList<>();
        try //check username and find last page
        {
            FindFirstPage(userAgent, FirstList);
        }
        catch (ResponseException e)
        {
            System.out.println("Can't Find Username");
            error = true;
        }
        if (!error)
        {
            int lastPage = LastBookmarkPage(FirstList);
            for (int i = 1; i <= lastPage + 1; i++) //find links on all pages
            {
                FindLinks(i, userAgent);
            }
            linkList = getMatchingWorks(); // get all work links
            if (checkJson) System.out.println("Works found in JSON File: " + jsonList.size());
            System.out.println("Works found on A03: " + linkList.size());
            findMissingItems(); // look for items missing from json file
        }

    }

    private static void getInput()
    {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter Username for bookmark lookup: ");
        username = in.nextLine();
        System.out.print("Check JSON File for comparison? ");
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
        System.out.println("JSON URL's sent to " + outputFile);
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
                linkList.add(link.getAtString("href")); // add links to list for sorting through
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
        for (String currentLink : linkList)
        {
            m = works.matcher(currentLink);
            if (m.matches())
            {
                finalList.add(currentLink);
            }
        }
        writeToFile(finalList, "A03Links.txt");
        System.out.println("A03 Links sent to A03Links.txt");
        return finalList;
    }

    private static void writeToFile(List<String> finalList, String filename)
    {
        try
        {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            for (String link : finalList)
            {
                pw.println(link);
            }
            pw.close();
        }
        catch (Exception ex)
        {
            System.out.println("Writing to file failed");
            System.out.println(ex.toString());
        }
    }

    private static List<String> compareLists(List<String> firstList, List<String> secondList) //first list: firefox, second list: a03
    {
        List<String> temp = new ArrayList<>();
        for (String s : secondList)
        {
            if (!firstList.contains(s))
            {
                temp.add(s);
            }
        }
        return temp;
    }

    private static void findMissingItems()
    {
        if (checkJson)
        {
            List<String> temp = compareLists(jsonList, linkList);
            System.out.println("Missing Item Count: " + temp.size());
            writeToFile(temp,"missing.txt");
            System.out.print("Missing items sent to missing.txt");
        }
    }

}

