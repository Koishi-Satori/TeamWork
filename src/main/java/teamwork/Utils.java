package teamwork;

import javafx.beans.value.ObservableValueBase;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;

import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class Utils {
    public static final record HistoryData(String name, Path location) {
        @Override
        public String name () {
            return name;
        }

        @Override
        public Path location () {
            return location;
        }

        @Override
        public String toString () {
            return "HistoryData{" +
                    "name='" + name + '\'' +
                    ", location=" + location +
                    '}';
        }
    }

    static {
        System.setProperty("content.types.user.table", "<path-to-file>");
    }

    static final FileSystemView F = FileSystemView.getFileSystemView();
    static final FileSystem FS = FileSystems.getDefault();
    static final FileNameMap M = URLConnection.getFileNameMap();


    public static Image getSystemIcon (Path path) {
        final var src = F.getSystemIcon(path.toFile());
        return null;
    }

    public static String resolveSize (long size) {
        final StringBuilder sb = new StringBuilder();
        final long[] digits = new long[4];
        for (int i = 0; i < 4; i++) {
            digits[i] = size % 1024;
            size /= 1024;
        }
        if (digits[0] != 0) {
            sb.append(digits[0]).append("B");
        }
        if (digits[1] != 0) {
            sb.append(digits[1]).append("KB");
        }
        if (digits[2] != 0) {
            sb.append(digits[2]).append("MB");
        }
        if (digits[3] != 0) {
            sb.append(digits[3]).append("GB");
        }
        if (sb.isEmpty()) {
            sb.append("0B");
        }
        return sb.toString();
    }

    public enum CompType {
        ICON,
        INFORMATION,
        CONTENT
    }

    public static final class CellContainer extends FlowPane {
        CompType type;
        boolean useAccuracy;
        Consumer<Path> action;

        public CellContainer (CompType type, boolean useAccuracy, Path... paths) throws IOException {
            this.type = type;
            this.useAccuracy = useAccuracy;
            super.setPrefSize(820, 629);
            init(paths);
        }

        public void setAction (Consumer<Path> action) {
            this.action = action;
        }

        @SuppressWarnings("unchecked")
        public Path getSelect () {
            if (type == CompType.INFORMATION) {
                return ((TableView<Path>) this.getChildren().get(0)).getSelectionModel().getSelectedItem();
            }
            throw new RuntimeException("Not finished.");
        }

        @SuppressWarnings("EnhancedSwitchMigration")
        private void init (Path... paths) throws IOException {
            switch (type) {
                case ICON: {

                    break;
                }
                case INFORMATION: {
                    final TableView<Path> tableView = new TableView<>();
                    tableView.setOnMouseClicked(me -> {
                        if (me.getClickCount() == 2) {
                            final var path = tableView.getSelectionModel().getSelectedItem();
                            if (path != null) {
                                action.accept(path);
                            }
                        }
                    });
                    final TableColumn<Path, String> name = new TableColumn<>("Name");
                    final TableColumn<Path, String> time = new TableColumn<>("Last Modify Time");
                    final TableColumn<Path, String> type = new TableColumn<>("Type");
                    final TableColumn<Path, String> size = new TableColumn<>("Size");
                    name.setCellValueFactory(df -> new ObservableValueBase<>() {
                        @Override
                        public String getValue () {
                            final var fileName = df.getValue().getFileName();
                            return fileName == null ? df.getValue().toString() : fileName.toString();
                        }
                    });
                    time.setCellValueFactory(df -> new ObservableValueBase<>() {
                        @Override
                        public String getValue () {
                            try {
                                return Files.getLastModifiedTime(df.getValue()).toString();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    type.setCellValueFactory(df -> new ObservableValueBase<>() {
                        @Override
                        public String getValue () {
                            try {
                                return df.getValue().toUri().toURL().openConnection().getContentType();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    size.setCellValueFactory(df -> new ObservableValueBase<>() {
                        @Override
                        public String getValue () {
                            final long l = df.getValue().toFile().getTotalSpace();
                            return useAccuracy ? resolveSize(l) : l / 1024 + "KB";
                        }
                    });
                    tableView.setPrefSize(this.getPrefWidth(), this.getPrefHeight());
                    //noinspection unchecked
                    tableView.getColumns().addAll(name, time, type, size);
                    final var items = tableView.getItems();
                    //noinspection ManualArrayToCollectionCopy
                    for (final Path path : paths) {
                        //noinspection UseBulkOperation
                        items.add(path);
                    }
                    super.getChildren().add(tableView);
                    break;
                }
                default: {

                    break;
                }
            }
        }

        public CompType type () {
            return type;
        }
    }
}
