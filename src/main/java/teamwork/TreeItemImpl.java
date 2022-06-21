package teamwork;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author KKoishi_
 */
public class TreeItemImpl extends TreeItem<String> {
    boolean initialized = false;
    Path path;

    public TreeItemImpl (Path path) {
        super(path.toString());
        this.path = path;
    }

    private TreeItemImpl (String title) {
        super(title);
    }

    static TreeItemImpl diskRoot = new TreeItemImpl("/") {
        @Override
        public void upgrade () {
            if (initialized) {
                return;
            }
            initialized = true;
            final var cpy = super.getChildren();
            for (final Path directory : Utils.FS.getRootDirectories()) {
                cpy.add(new TreeItemImpl(directory));
            }
        }

        @Override
        public boolean isLeaf () {
            return false;
        }
    };

    static TreeItemImpl diskRoot (Collection<Path> paths) {
        return new TreeItemImpl("Search Result") {
            @Override
            public void upgrade () {
                if (initialized) {
                    return;
                }
                initialized = true;
                final var cpy = super.getChildren();
                for (final Path directory : paths) {
                    cpy.add(new TreeItemImpl(directory));
                }
            }

            @Override
            public boolean isLeaf () {
                return false;
            }
        };
    }

    public void upgrade () {
        if (!Files.isDirectory(path)) {
            return;
        }
        final var values = super.getChildren();
        values.clear();
        try {
            Files.list(path).map(TreeItemImpl::new).forEach(values::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final ObservableList<TreeItem<String>> getChildren () {
        return super.getChildren();
    }

    @Override
    public boolean isLeaf () {
        return !Files.isDirectory(path);
    }

    @Override
    public String toString () {
        return "TreeItemImpl{" +
                "initialized=" + initialized +
                ", path=" + path +
                '}';
    }
}
