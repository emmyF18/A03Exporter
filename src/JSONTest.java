import java.util.List;

public class JSONTest
{
    public static void main(String[] args)
    {
        JSONFilters json = new JSONFilters();
        //noinspection SpellCheckingInspection
        List<String> jsonList = json.getUrls("fanficBookMarks.json", "FirefoxLinks.txt", true);
        System.out.println("List Count: " + jsonList.size());

    }
}
