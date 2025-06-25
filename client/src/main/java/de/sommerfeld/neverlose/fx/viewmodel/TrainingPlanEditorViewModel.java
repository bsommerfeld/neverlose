package de.sommerfeld.neverlose.fx.viewmodel;

import de.sommerfeld.neverlose.export.PdfExportService;
import de.sommerfeld.neverlose.plan.TrainingPlan;
import de.sommerfeld.neverlose.plan.components.TrainingUnit;
import de.sommerfeld.neverlose.plan.components.Weekday;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel for the Training Plan Editor view.
 * Manages the state of the editor, including the plan details and its units,
 * and provides properties for UI binding and methods for actions like adding/removing units/exercises
 * and exporting the plan.
 * It handles the synchronization between the ViewModel state and the underlying TrainingPlan model.
 */
public class TrainingPlanEditorViewModel {

    private final PdfExportService pdfExportService = new PdfExportService();
    private final StringProperty planName = new SimpleStringProperty();
    private final StringProperty planDescription = new SimpleStringProperty();
    private final ObservableList<TrainingUnitViewModel> trainingUnits = FXCollections.observableArrayList();
    private final ObjectProperty<TrainingUnitViewModel> selectedTrainingUnit = new SimpleObjectProperty<>();

    private TrainingPlan trainingPlan; // The underlying model being edited

    private final ChangeListener<String> planNameModelUpdater =
            (obs, oldVal, newVal) -> {
                if (trainingPlan != null) trainingPlan.setName(newVal);
            };
    private final ChangeListener<String> planDescriptionModelUpdater =
            (obs, oldVal, newVal) -> {
                if (trainingPlan != null) trainingPlan.setDescription(newVal);
            };
    private final ListChangeListener<TrainingUnitViewModel> trainingUnitsModelUpdater = c -> {
        if (trainingPlan == null || trainingPlan.getTrainingUnits() == null) {
            // System.err.println("Warning: TrainingPlan or its TrainingUnits collection is null. Sync disabled.");
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
        }
    };


    /**
     * Constructs the ViewModel, initializing it with a default empty TrainingPlan.
     */
    public TrainingPlanEditorViewModel() {
        loadPlan(new TrainingPlan("New Plan", ""));
    }

    /**
     * Loads a new TrainingPlan into the ViewModel using a robust reset strategy.
     * Removes listeners, explicitly clears the main list to reset UI state,
     * sets the new model, updates properties, repopulates the list, and re-adds listeners.
     *
     * @param planToLoad The TrainingPlan to load. If null, a default empty plan is created.
     */
    public void loadPlan(TrainingPlan planToLoad) {
        if (planToLoad == null) {
            planToLoad = new TrainingPlan("Untitled Plan", "");
            System.err.println("Warning: Tried to load a null TrainingPlan. Created a default one.");
        }

        planName.removeListener(planNameModelUpdater);
        planDescription.removeListener(planDescriptionModelUpdater);
        trainingUnits.removeListener(trainingUnitsModelUpdater);

        trainingUnits.clear();

        this.trainingPlan = planToLoad;

        this.planName.set(trainingPlan.getName());
        this.planDescription.set(trainingPlan.getDescription());

        List<TrainingUnit> modelUnits = trainingPlan.getTrainingUnits() != null
                ? trainingPlan.getTrainingUnits().getAll()
                : new ArrayList<>();

        List<TrainingUnitViewModel> newUnitViewModels = modelUnits.stream()
                .map(TrainingUnitViewModel::new)
                .collect(Collectors.toList());

        this.trainingUnits.setAll(newUnitViewModels);

        planName.addListener(planNameModelUpdater);
        planDescription.addListener(planDescriptionModelUpdater);
        trainingUnits.addListener(trainingUnitsModelUpdater);
    }

    /**
     * Sets the TrainingPlan to be edited by this ViewModel.
     * Delegates to {@link #loadPlan(TrainingPlan)}.
     *
     * @param trainingPlan The new TrainingPlan to load and edit.
     */
    public void setTrainingPlan(TrainingPlan trainingPlan) {
        loadPlan(trainingPlan);
    }

    /**
     * Initiates the process to export the current training plan to a PDF file
     * using the {@link PdfExportService}.
     */
    public void exportPlanToPdf(File targetFile) throws IOException {
        TrainingPlan currentPlan = getTrainingPlanModel();
        if (currentPlan == null) {
            throw new IOException("Cannot export: No training plan data is loaded.");
        }
        pdfExportService.export(currentPlan, targetFile);
    }

    /**
     * Adds a new, default TrainingUnit to the current training plan.
     */
    public void addTrainingUnit() {
        if (trainingPlan == null) {
            System.err.println("Cannot add unit: No training plan loaded.");
            return;
        }
        TrainingUnit newUnit = new TrainingUnit("New Unit", "", Weekday.MONDAY);
        TrainingUnitViewModel newUnitViewModel = new TrainingUnitViewModel(newUnit);
        this.trainingUnits.add(newUnitViewModel);
    }

    /**
     * Removes the currently selected TrainingUnit from the training plan.
     */
    public void removeSelectedTrainingUnit() {
        TrainingUnitViewModel selected = selectedTrainingUnit.get();
        if (selected != null) {
            this.trainingUnits.remove(selected);
        } else {
            System.err.println("Cannot remove unit: No unit selected.");
        }
    }

    /**
     * Adds a new, default Exercise to the currently selected TrainingUnit.
     */
    public void addExerciseToSelectedUnit() {
        TrainingUnitViewModel selectedUnit = selectedTrainingUnit.get();
        if (selectedUnit != null) {
            selectedUnit.addExercise();
        } else {
            System.err.println("Cannot add exercise: No Training Unit selected.");
        }
    }

    /**
     * Removes the currently selected Exercise from the currently selected TrainingUnit.
     */
    public void removeSelectedExerciseFromSelectedUnit() {
        TrainingUnitViewModel selectedUnit = selectedTrainingUnit.get();
        if (selectedUnit != null) {
            selectedUnit.removeSelectedExercise();
        } else {
            System.err.println("Cannot remove exercise: No Training Unit selected.");
        }
    }

    public void removeTrainingUnit(TrainingUnitViewModel unit) {
        if (unit != null) {
            this.trainingUnits.remove(unit);
        } else {
            System.err.println("Cannot remove unit: No unit selected.");
        }
    }

    public void removeExerciseFromSelectedUnit(ExerciseViewModel exercise) {
        TrainingUnitViewModel selectedUnit = selectedTrainingUnit.get();
        if (selectedUnit != null) {
            selectedUnit.removeExercise(exercise);
        } else {
            System.err.println("Cannot remove exercise: No Training Unit selected.");
        }
    }

    /**
     * @return The StringProperty for the training plan's name.
     */
    public StringProperty planNameProperty() {
        return planName;
    }

    /**
     * @return The StringProperty for the training plan's description.
     */
    public StringProperty planDescriptionProperty() {
        return planDescription;
    }

    /**
     * @return The ObservableList containing the {@link TrainingUnitViewModel}s for the plan.
     */
    public ObservableList<TrainingUnitViewModel> trainingUnitsProperty() {
        return trainingUnits;
    }

    /**
     * @return The ObjectProperty holding the currently selected {@link TrainingUnitViewModel}.
     */
    public ObjectProperty<TrainingUnitViewModel> selectedTrainingUnitProperty() {
        return selectedTrainingUnit;
    }

    /**
     * @return The current {@link TrainingPlan} model instance, or null if none is loaded.
     */
    public TrainingPlan getTrainingPlanModel() {
        return trainingPlan;
    }
}