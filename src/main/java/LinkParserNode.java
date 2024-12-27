import java.util.concurrent.CopyOnWriteArraySet;

public class LinkParserNode {
    private String url;
    private volatile LinkParserNode parent;
    private volatile int depth;
    private volatile CopyOnWriteArraySet<LinkParserNode> sublinks;

    public LinkParserNode(String url) {
        this.url = url;
        sublinks = new CopyOnWriteArraySet<>();
        depth = 0;
        parent = null;
    }

    public void addSublinks(LinkParserNode sublink) {
        if (!sublinks.contains(sublink) && sublink.getUrl().startsWith(url)) {
            this.sublinks.add(sublink);
            sublink.setParent(this);
        }
    }

    private void setParent(LinkParserNode siteMapNode) {
        synchronized (this) {
            this.parent = siteMapNode;
            this.depth = setDepth();
        }
    }

    public int getDepth() {
        return depth;
    }

    private int setDepth() {
        if (parent == null) {
            return 0;
        }
        return 1 + parent.getDepth();
    }

    public CopyOnWriteArraySet<LinkParserNode> getSublinks() {
        return sublinks;
    }

    public String getUrl() {
        return url;
    }
}