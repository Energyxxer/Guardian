package com.energyxxer.guardian.global;

import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.editor.EditorModule;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;

import java.io.File;
import java.util.Objects;

public class IndexingProcess extends AbstractProcess {
    private Project project;
    private ProjectSummarizer summarizer;

    public IndexingProcess(Project project) {
        super("Indexing");
        this.project = project;
        summarizer = project.createProjectSummarizer();
        summarizer.addCompletionListener(() -> {
            project.updateSummary(summarizer.getSummary());
            for(Tab tab : GuardianWindow.tabManager.openTabs) {
                if(tab.isSaved() && tab.module instanceof EditorModule) {
                    File tabFile = ((EditorModule) tab.module).getFileForAnalyzer();
                    PrismarineSummaryModule fileSummary = project.getSummary().getSummaryForFile(tabFile);
                    if(fileSummary != null) {
                        Debug.log("Updated file summary for tab " + tab.getName());
                        ((SuggestionDialog) ((EditorModule) tab.module).editorComponent.getSuggestionInterface()).setSummary(fileSummary, true);
                    }
                }
            }
        });
        summarizer.addCompletionListener(() -> {
            Debug.log("Finished indexing project: " + project.getName());
            this.updateStatus("");
            this.finalizeProcess(true);
        });
        initializeThread(this::startSummarizer);
    }

    private void startSummarizer() {
        summarizer.start();
        updateStatus("Updating indices [" + project.getRootDirectory().getName() + "]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexingProcess that = (IndexingProcess) o;
        return Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project);
    }
}
