import com.jaunt.JNode;
import com.jaunt.UserAgent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONFilters
{
    private PrintWriter pw ;
    public List<String> getUrls(String jsonFile, String outputFile )
    {
        UserAgent userAgent= new UserAgent();
        JNode searchResults = userAgent.json;
        try
        {
            userAgent.openJSON(new File(jsonFile));
            searchResults = userAgent.json.findEvery("uri");
        }
        catch (Exception ex) { ex.printStackTrace(); }
        List<String> urls = new ArrayList<>();
        for (JNode node : searchResults)
        {
            urls.add(node.toString());
        }
        urls = ao3LinksOnly(urls);
        try {
            pw = new PrintWriter(new FileWriter(outputFile));
            for (String url : urls)
            {
                System.out.println("Writing: " + url);
                pw.println(url);
            }
            pw.close();
        } catch (Exception e) { e.printStackTrace(); }

        System.out.println("JSON URL Count:" + urls.size());
        return urls;
    }
    private List<String> ao3LinksOnly(List<String> fullURLS)
    {
        Pattern works = Pattern.compile("https://archiveofourown\\.org/works/\\d{5,}");
        Matcher m;
        List<String> a03List = new ArrayList<>();
        int count =0;
        for(String url : fullURLS)
        {
            m = works.matcher(url);
            if(m.matches())
            {
                String temp = url.replaceAll("\\\\","");
                a03List.add(temp);
                count++;
            }
        }
        System.out.println("A03 Bookmark Count in JSON:"+ count);
        return  a03List;
    }
}
