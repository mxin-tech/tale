package com.tale.scrf;

import com.blade.kit.StringKit;

import java.util.HashMap;
import java.util.Map;

import static com.blade.kit.StringKit.isEmpty;

/**
 * @author <a href="mailto:mx2913@foxmail.com">xin.yang</a>
 */
public class TrieRouter {
    public class Node {
        private String path;
        private String segment;
        private Map<String, Node> staticRouters;
        private Node dynamicRouter;
        private boolean isWildcard;
    }

    public static String strip(String str, final String stripChars) {
        if (isEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }
    public static String stripStart(final String str, final String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
                start++;
            }
        }
        return str.substring(start);
    }

    public static String stripEnd(final String str, final String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    private final Node root;

    public TrieRouter() {
        this.root = new Node();
        this.root.path = "/";
        this.root.segment = "/";
    }

    // add route
    public TrieRouter addRoute(String path) {
        if (isEmpty(path)) {
            return this;
        }
        String strippedPath = strip(path, "/");
        String[] strings = strippedPath.split("/");
        if (strings.length != 0) {
            Node node = root;
            // split by /
            for (String segment : strings) {
                node = addNode(node, segment);
                if ("**".equals(segment)) {
                    break;
                }
            }
            // At the end, set the path of the child node
            node.path = path;
        }
        return this;
    }

    // add note
    private Node addNode(Node node, String segment) {
        // If it is a wildcard node, return the current node directly:
        if ("**".equals(segment)) {
            node.isWildcard = true;
            return node;
        }
        // if it is a dynamic route,
        // create a child node and then hang the child node under the current node:
        if (segment.startsWith(":")) {
            Node childNode = new Node();
            childNode.segment = segment;
            node.dynamicRouter = childNode;
            return childNode;
        }

        Node childNode;
        // Static route, put in a Map,
        // the key of the Map is the URL segment, value is the new child node:
        if (node.staticRouters == null) {
            node.staticRouters = new HashMap<>();
        }
        if (node.staticRouters.containsKey(segment)) {
            childNode = node.staticRouters.get(segment);
        } else {
            childNode = new Node();
            childNode.segment = segment;
            node.dynamicRouter = childNode;
            node.staticRouters.put(segment, childNode);
        }
        return childNode;
    }

    // match route
    public String matchRoute(String path) {
        if (isEmpty(path)) {
            return null;
        }
        String strippedPath = strip(path, "/");
        String[] strings = strippedPath.split("/");
        if (strings.length != 0) {
            Node node = root;
            // split by /
            for (String segment : strings) {
                node = matchNode(node, segment);
                // if no route is matched or a wildcard route is used, break:
                if (node == null || node.isWildcard) {
                    break;
                }
            }
            if (node != null) {
                return node.path;
            }
        }
        return null;
    }

    public boolean match(String path) {
        return matchRoute(path) != null;
    }

    // match child node
    private Node matchNode(Node node, String segment) {
        if (node.staticRouters != null && node.staticRouters.containsKey(segment)) {
            return node.staticRouters.get(segment);
        }
        if (node.dynamicRouter != null)
            return node.dynamicRouter;
        if (node.isWildcard)
            return node;
        return null;
    }
}
