package com.energyxxer.guardian.ui.explorer;

import com.energyxxer.guardian.events.events.FileDeletedEvent;
import com.energyxxer.guardian.events.events.FileRenamedEvent;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.guardian.ui.common.MenuItems;
import com.energyxxer.guardian.ui.dialogs.ConfirmDialog;
import com.energyxxer.guardian.ui.explorer.base.ExplorerFlag;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerSeparator;
import com.energyxxer.guardian.ui.modules.*;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.guardian.util.FileCommons;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ScalableGraphics2D;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.energyxxer.xswing.ScalableDimension.descaleEvent;

/**
 * Created by User on 5/16/2017.
 */
public class ProjectExplorerMaster extends StyledExplorerMaster implements DropTargetListener {
    private final ArrayList<ModuleToken> tokenSources = new ArrayList<>();

    private DraggableExplorerModuleToken[] draggingFiles = null;
    private final ArrayList<ExplorerElement> draggingRollover = new ArrayList<>();

    public static Preferences.SettingPref<Boolean> SAVE_EXPLORER_TREE = new Preferences.SettingPref<>("settings.behavior.save_explorer_tree", true, Boolean::parseBoolean);

    public static final ExplorerFlag
            FLATTEN_EMPTY_PACKAGES = new ExplorerFlag("Flatten Empty Packages"),
            SHOW_PROJECT_FILES = new ExplorerFlag("Show Project Files");

    public ProjectExplorerMaster() {
        explorerFlags.put(FLATTEN_EMPTY_PACKAGES, Preferences.get("explorer.flatten_empty_packages","true").equals("true"));
        explorerFlags.put(SHOW_PROJECT_FILES, Preferences.get("explorer.show_project_files","false").equals("true"));
        explorerFlags.put(ExplorerFlag.DEBUG_WIDTH, Preferences.get("explorer.debug_width","false").equals("true"));

        this.tokenSources.add(new WorkspaceRootModuleToken());
        this.tokenSources.add(new LibrariesRootModuleToken());

        this.setDropTarget(new DropTarget(this, TransferHandler.COPY_OR_MOVE, this));

        this.setTransferHandler(new TransferHandler("filepath") {
            @NotNull
            @Override
            protected Transferable createTransferable(JComponent c) {
                if(pressConsumed) return new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[0];
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return false;
                    }

                    @NotNull
                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        throw new UnsupportedFlavorException(flavor);
                    }
                };
                Collection<DraggableExplorerModuleToken> tokens = selectedItems.stream().filter(i -> i.getToken() instanceof DraggableExplorerModuleToken).map(i -> ((DraggableExplorerModuleToken) i.getToken())).collect(Collectors.toList());
                Object[] rawFlavors = tokens.stream().map(DraggableExplorerModuleToken::getDataFlavor).distinct().toArray();
                DataFlavor[] flavors = Arrays.copyOf(rawFlavors, rawFlavors.length, DataFlavor[].class);
                List<File> dragFileList = tokens
                        .stream()
                        .filter(t -> t instanceof FileModuleToken)
                        .map(t -> ((FileModuleToken) t).getTransferData())
                        .collect(Collectors.toList());
                draggingFiles = dragFileList.stream().map(FileModuleToken::new).toArray(DraggableExplorerModuleToken[]::new);

                return new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return flavors;
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor == DataFlavor.javaFileListFlavor;
                    }

                    @NotNull
                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                        if(flavor != DataFlavor.javaFileListFlavor) throw new UnsupportedFlavorException(flavor);
                        return dragFileList;
                    }
                };
            }

            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                super.exportDone(source, data, action);
                draggingFiles = null;
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

        });

        setSingleClickInteractAllowed(true);

        refresh();

        Guardian.events.addEventHandler(FileRenamedEvent.class, (FileRenamedEvent evt) -> {
            queueRefresh();
        });
        Guardian.events.addEventHandler(FileDeletedEvent.class, (FileDeletedEvent evt) -> {
            queueRefresh();
        });
    }

    private boolean refreshQueued = false;
    public void queueRefresh() {
        if(refreshQueued) return;
        SwingUtilities.invokeLater(this::refresh);
        refreshQueued = true;
    }

    @Override
    public void refresh() {
        Debug.log("Refreshing workspace");
        clearSelected();
        refresh(this.getExpandedElements().stream().map(ModuleToken::getIdentifier).distinct().collect(Collectors.toCollection(ArrayList::new)));
    }

    public void refresh(ArrayList<String> toOpen) {
        refreshQueued = false;
        children.clear();
        flatList.clear();
        this.getExpandedElements().clear();

        for(ModuleToken source : tokenSources) {
            Collection<? extends ModuleToken> subTokens = source.getSubTokens();
            if(!subTokens.isEmpty() || source instanceof WorkspaceRootModuleToken) {
                this.children.add(new ExplorerSeparator(source.getTitle(), this));
            }
            for(ModuleToken token : subTokens) {
                this.children.add(new StandardExplorerItem(token, this, toOpen));
            }
        }

        repaint();
    }

    @Override
    protected void selectionUpdated() {
        super.selectionUpdated();
        Commons.updateActiveFile();
    }

    public void saveExplorerTree() {
        if(!SAVE_EXPLORER_TREE.get()) return;
        StringBuilder sb = new StringBuilder();
        Collection<ModuleToken> expandedElements = this.getExpandedElements();
        for(ModuleToken elem : expandedElements) {
            sb.append(elem.getIdentifier());
            sb.append(File.pathSeparator);
        }
        Debug.log("Saving: " + sb);
        Preferences.put("open_tree", sb.toString());
    }

    public void openExplorerTree() {
        if(!SAVE_EXPLORER_TREE.get()) return;
        String openTree = Preferences.get("open_tree",null);
        if(openTree != null) {
            Debug.log("Opening: " + openTree);
            refresh(new ArrayList<>(Arrays.asList(openTree.split(Pattern.quote(File.pathSeparator)))));
        }
    }


    @Override
    public void dragEnter(DropTargetDragEvent e) {
        dragOver(e);
    }

    @Override
    public void dragOver(DropTargetDragEvent e) {
        if(!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            e.rejectDrag();
            return;
        }
        e.acceptDrag(e.getDropAction());

        clearDragRollover();

        Component parent = this.getParent();
        if(parent instanceof JViewport) {
            Point dropLocation = new Point(e.getLocation());

            Rectangle visibleRect = ((JViewport) parent).getViewRect();
            int margin = 50;
            Rectangle safeRect = new Rectangle(visibleRect.x + margin, visibleRect.y + margin, visibleRect.width - 2*margin, visibleRect.height - 2*margin);
            if(!safeRect.contains(dropLocation)) {
                handleDragOverEdge(dropLocation, visibleRect, safeRect, (JViewport) parent);
            }
        }

        Point location = e.getLocation();
        location.setLocation(location.x / ScalableGraphics2D.SCALE_FACTOR, location.y / ScalableGraphics2D.SCALE_FACTOR);

        ExplorerElement targetElement = getElementAtMousePos(location);
        boolean canImport = false;
        if(targetElement != null && targetElement.getToken() instanceof FileModuleToken) {
            FileModuleToken rolloverToken = ((FileModuleToken) targetElement.getToken());
            Path destination = rolloverToken.getDragDestination().toPath();

            boolean isValidDestination = true;
            if(draggingFiles != null) {
                if(e.getDropAction() == TransferHandler.MOVE) {
                    isValidDestination = new FileModuleToken(rolloverToken.getDragDestination()).canAcceptMove(draggingFiles);
                } else { //COPY
                    isValidDestination = new FileModuleToken(rolloverToken.getDragDestination()).canAcceptCopy(draggingFiles);
                    if(isValidDestination) {
                        for(DraggableExplorerModuleToken token : draggingFiles) {
                            if(((FileModuleToken) targetElement.getToken()).getFile().equals(((FileModuleToken) token).getFile())) {
                                isValidDestination = false;
                                break;
                            }
                        }
                    }
                }
            }

            if(isValidDestination) {
                canImport = true;
                for(ExplorerElement element : flatList) {
                    if(element.getToken() instanceof FileModuleToken && ((FileModuleToken) element.getToken()).getFile().toPath().startsWith(destination)) {
                        if(!draggingRollover.contains(element)) {
                            draggingRollover.add(element);
                        }
                        element.setRollover(true);
                    }
                }
                if(draggingRollover.isEmpty()) {
                    canImport = false;
                }
            }
        }

        if(!canImport) e.rejectDrag();
        else e.acceptDrag(e.getDropAction());
        repaint();
    }

    private void handleDragOverEdge(Point location, Rectangle visibleRect, Rectangle safeRect, JViewport viewport) {
        int range = 50;
        viewport.scrollRectToVisible(new Rectangle(location.x - visibleRect.x - range, location.y - visibleRect.y - range, 2*range, 2*range));
    }

    private void clearDragRollover() {
        for(ExplorerElement element : draggingRollover) {
            element.setRollover(false);
        }
        draggingRollover.clear();
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent e) {
    }

    @Override
    public void dragExit(DropTargetEvent e) {
        clearDragRollover();
        repaint();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drop(DropTargetDropEvent e) {
        clearDragRollover();
        repaint();
        e.acceptDrop(e.getDropAction());

        Transferable t = e.getTransferable();

        Point location = e.getLocation();
        location.setLocation(location.x / ScalableGraphics2D.SCALE_FACTOR, location.y / ScalableGraphics2D.SCALE_FACTOR);

        ExplorerElement targetElement = getElementAtMousePos(location);
        if(targetElement == null) return;
        FileModuleToken rolloverToken = ((FileModuleToken) targetElement.getToken());
        Path destination = rolloverToken.getDragDestination().toPath();

        try {
            if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

                ArrayList<Path> distinctFiles = new ArrayList<>();
                for(File file : files) {
                    boolean unique = true;
                    for(int i = 0; i < distinctFiles.size(); i++) {
                        if(distinctFiles.get(i).startsWith(file.toPath())) {
                            if(!distinctFiles.contains(file.toPath())) {
                                distinctFiles.set(i, file.toPath());
                            } else {
                                distinctFiles.remove(i);
                                i--;
                            }
                            unique = false;
                        } else if(file.toPath().startsWith(distinctFiles.get(i))) {
                            unique = false;
                        }
                    }
                    if(unique) {
                        distinctFiles.add(file.toPath());
                    }
                }

                String howmanyfiles = distinctFiles.size() + " file" + (distinctFiles.size() == 1 ? "" : "s");
                StringBuilder promptString = new StringBuilder("<html>Are you sure you want to " + (e.getDropAction() == TransferHandler.MOVE ? "move" : "copy") + " ");
                if(distinctFiles.size() > 1) {
                    promptString.append("the following ").append(distinctFiles.size()).append(" files ");
                } else {
                    promptString.append("'").append(distinctFiles.get(0).getFileName()).append("' ");
                }
                promptString.append("to ").append(destination.getFileName()).append("?<br>");

                if(distinctFiles.size() > 1) {
                    for(Path path : distinctFiles) {
                        promptString.append("<br>");
                        promptString.append(path.getFileName());
                    }
                }
                promptString.append("</html>");

                if(new ConfirmDialog((e.getDropAction() == TransferHandler.MOVE ? "Move" : "Copy") + " " + howmanyfiles + "?", promptString.toString()).result) {
                    if(e.getDropAction() == TransferHandler.MOVE) {
                        FileCommons.moveFiles(distinctFiles.stream().map(Path::toFile).toArray(File[]::new), destination.toFile());
                    } else if(e.getDropAction() == TransferHandler.COPY) {
                        FileCommons.copyFiles(distinctFiles.stream().map(Path::toFile).toArray(File[]::new), destination.toFile());
                    }
                    clearSelected();
                    ArrayList<String> toOpen = this.getExpandedElements().stream().map(ModuleToken::getIdentifier).distinct().collect(Collectors.toCollection(ArrayList::new));
                    toOpen.add(new FileModuleToken(destination.toFile()).getIdentifier());
                    this.refresh(toOpen);
                }
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        e = descaleEvent(e);
        if(e.isPopupTrigger() && getElementAtMousePos(e) == null) {
            showMenu(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        e = descaleEvent(e);
        if(e.isPopupTrigger() && getElementAtMousePos(e) == null) {
            showMenu(e);
        }
    }

    private void showMenu(MouseEvent e) {
        StyledPopupMenu menu = new StyledPopupMenu();

        menu.add(MenuItems.newMenu("New", type -> true, ProjectManager.getWorkspaceDir(), false));

        menu.addSeparator();

        StyledMenuItem openInSystemItem = new StyledMenuItem("Show in System Explorer", "explorer");
        openInSystemItem.addActionListener(a -> Commons.showInSystemExplorer(Preferences.getWorkspace().toString()));
        menu.add(openInSystemItem);

        menu.show(this, (int) (e.getX() * ScalableGraphics2D.SCALE_FACTOR), (int) (e.getY() * ScalableGraphics2D.SCALE_FACTOR));
    }
}
