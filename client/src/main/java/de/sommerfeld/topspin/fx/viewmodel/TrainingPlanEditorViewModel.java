package de.sommerfeld.topspin.fx.viewmodel;

import de.sommerfeld.topspin.export.PdfExportService;
import de.sommerfeld.topspin.plan.*;
import de.sommerfeld.topspin.plan.components.TrainingUnit;
import de.sommerfeld.topspin.plan.components.Weekday;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class TrainingPlanEditorViewModel {

    private final PdfExportService pdfExportService = new PdfExportService();

    private TrainingPlan trainingPlan; // The Model being edited

    private final StringProperty planName = new SimpleStringProperty();
    private final StringProperty planDescription = new SimpleStringProperty();

    private final ObservableList<TrainingUnitViewModel> trainingUnits = FXCollections.observableArrayList();

    private final ObjectProperty<TrainingUnitViewModel> selectedTrainingUnit = new SimpleObjectProperty<>();

    public TrainingPlanEditorViewModel() {
        loadPlan(new TrainingPlan("New Plan", ""));
        addUnitListSyncListeners();
    }

    public void loadPlan(TrainingPlan planToLoad) {
        if (planToLoad == null) {
            planToLoad = new TrainingPlan("Untitled Plan", "");
            System.err.println("Warning: Tried to load a null TrainingPlan. Created a default one.");
        }
        this.trainingPlan = planToLoad;

        planName.removeListener(planNameModelUpdater);
        planDescription.removeListener(planDescriptionModelUpdater);

        this.planName.set(trainingPlan.getName());
        this.planDescription.set(trainingPlan.getDescription());

        planName.addListener(planNameModelUpdater);
        planDescription.addListener(planDescriptionModelUpdater);


        java.util.List<TrainingUnit> modelUnits = trainingPlan.getTrainingUnits() != null
                ? trainingPlan.getTrainingUnits().getAll()
                : new java.util.ArrayList<>();

        this.trainingUnits.setAll(
                modelUnits.stream()
                        .map(TrainingUnitViewModel::new)
                        .collect(Collectors.toList())
        );

        this.selectedTrainingUnit.set(null);
    }

    private final javafx.beans.value.ChangeListener<String> planNameModelUpdater =
            (obs, oldVal, newVal) -> {
                if (trainingPlan != null) trainingPlan.setName(newVal);
            };
    private final javafx.beans.value.ChangeListener<String> planDescriptionModelUpdater =
            (obs, oldVal, newVal) -> {
                if (trainingPlan != null) trainingPlan.setDescription(newVal);
            };


    private void addUnitListSyncListeners() {
        this.trainingUnits.addListener((ListChangeListener<TrainingUnitViewModel>) c -> {
            if (trainingPlan == null || trainingPlan.getTrainingUnits() == null) {
                System.err.println("Warning: TrainingPlan or its TrainingUnits collection is null. Sync disabled.");
                return;
            }
            while (c.next()) {
                if (c.wasAdded()) {
                    for (TrainingUnitViewModel addedVm : c.getAddedSubList()) {
                        if (!trainingPlan.getTrainingUnits().getAll().contains(addedVm.getModel())) {
                            trainingPlan.getTrainingUnits().add(addedVm.getModel());
                        }
                    }
                }
                if (c.wasRemoved()) {
                    for (TrainingUnitViewModel removedVm : c.getRemoved()) {
                        trainingPlan.getTrainingUnits().remove(removedVm.getModel());
                    }
                }
                // TODO: Handle permutations (c.wasPermutated()) if list reordering is allowed
                // TODO: Handle updates (c.wasUpdated()) - usually handled by property bindings within TrainingUnitViewModel
            }
            // "plan changed" event if needed for preview updates
        });
    }

    /**
     * Initiates the process to export the current training plan to a PDF file using the PdfExportService.
     *
     * @param targetFile The file selected by the user where the PDF should be saved.
     *
     * @throws IOException If an error occurs during PDF generation or file writing.
     */
    public void exportPlanToPdf(File targetFile) throws IOException {
        TrainingPlan currentPlan = getTrainingPlanModel(); // Get the current model state
        if (currentPlan == null) {
            throw new IOException("Cannot export: No training plan data is loaded.");
        }

        System.out.println("ViewModel: Requesting PDF export via PdfExportService for plan '"
                + currentPlan.getName() + "' to " + targetFile.getName());

        pdfExportService.export(currentPlan, targetFile);

        System.out.println("ViewModel: PDF export request completed by service.");
    }

    public void addTrainingUnit() {
        TrainingUnit newUnit = new TrainingUnit("New Unit", "", Weekday.MONDAY);
        TrainingUnitViewModel newUnitViewModel = new TrainingUnitViewModel(newUnit);
        this.trainingUnits.add(newUnitViewModel);
    }

    public void removeSelectedTrainingUnit() {
        TrainingUnitViewModel selected = selectedTrainingUnit.get();
        if (selected != null) {
            this.trainingUnits.remove(selected);
            // The selectedTrainingUnit property in this ViewModel will update automatically
            // via the binding set up in the controller when the ListView selection changes.
            // If selection remains on removed index, it might become null or select next/previous.
        }
    }

    public void addExerciseToSelectedUnit() {
        TrainingUnitViewModel selectedUnit = selectedTrainingUnit.get();
        if (selectedUnit != null) {
            selectedUnit.addExercise();
        } else {
            System.err.println("Cannot add exercise: No Training Unit selected.");
        }
    }

    public void removeSelectedExerciseFromSelectedUnit() {
        TrainingUnitViewModel selectedUnit = selectedTrainingUnit.get();
        if (selectedUnit != null) {
            selectedUnit.removeSelectedExercise();
        } else {
            System.err.println("Cannot remove exercise: No Training Unit selected.");
        }
    }


    public StringProperty planNameProperty() {
        return planName;
    }

    public StringProperty planDescriptionProperty() {
        return planDescription;
    }

    public ObservableList<TrainingUnitViewModel> trainingUnitsProperty() {
        return trainingUnits;
    }

    public ObjectProperty<TrainingUnitViewModel> selectedTrainingUnitProperty() {
        return selectedTrainingUnit;
    }

    public TrainingPlan getTrainingPlanModel() {
        return trainingPlan;
    }
}
