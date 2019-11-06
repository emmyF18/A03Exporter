import com.jaunt.JNode;
import com.jaunt.UserAgent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JSONFilters
{
    List<String> getUrls(String jsonFile, String outputFile, Boolean A03Only)
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
        if(A03Only)
        {
            urls = ao3LinksOnly(urls);
        }
        writeToFile(urls, outputFile);
        return urls;
    }
    private List<String> ao3LinksOnly(List<String> fullURLS)
    {
        Pattern works = Pattern.compile("https://archiveofourown\\.org/works/\\d{5,}");
        Matcher m;
        List<String> a03List = new ArrayList<>();

        for(String url : fullURLS)
        {
            String newUrl = url.replaceAll("\\\\","");
            m = works.matcher(newUrl);
            if(m.matches())
            {
                a03List.add(newUrl);
            }
        }
        return  a03List;
    }
    private void writeToFile(List<String> urls, String outputFile)
    {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
            for (String url : urls)
            {
                pw.println(url);
            }
            pw.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

}
