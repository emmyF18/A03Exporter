import com.jaunt.JNode;
import com.jaunt.UserAgent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
        try {
            pw = new PrintWriter(new FileWriter(outputFile));
            for (String url : urls)
            {
                String temp = url.replaceAll("\\\\","");
                //System.out.println(temp);
                pw.println(temp);
            }
            pw.close();
        } catch (Exception e) { e.printStackTrace(); }

        System.out.println("JSON URL Count:" + urls.size());
        return urls;
    }
}
