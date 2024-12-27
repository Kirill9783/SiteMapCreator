import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class LinkParserNodeRecursiveAction extends RecursiveAction {
    private LinkParserNode url;
    private LinkParserNode rootUrl;
    private static CopyOnWriteArraySet<String> allLinks = new CopyOnWriteArraySet<>();

    public LinkParserNodeRecursiveAction(LinkParserNode url) {
        this.url = url;
    }

    public LinkParserNodeRecursiveAction(LinkParserNode url, LinkParserNode rootUrl) {
        this.url = url;
        this.rootUrl = rootUrl;
    }

    @Override
    protected void compute() {
        Set<LinkParserNodeRecursiveAction> taskList = new HashSet<>();
        try {
            sleep(500);
            Document doc = Jsoup.connect(url.getUrl()).timeout(100000).ignoreContentType(true).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (isCorrected(absUrl)) {
                    url.addSublinks(new LinkParserNode(absUrl));
                    allLinks.add(absUrl);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        for (LinkParserNode link : url.getSublinks()) {
            LinkParserNodeRecursiveAction task = new LinkParserNodeRecursiveAction(link, rootUrl);
            task.fork();
            taskList.add(task);
        }

        for (LinkParserNodeRecursiveAction task : taskList) {
            task.join();
        }
    }

    private boolean isCorrected(String url) {
        return (!url.isEmpty() && url.startsWith(rootUrl.getUrl())
                && !allLinks.contains(url) && !url.contains("#")
                && !url.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)"));
    }
}