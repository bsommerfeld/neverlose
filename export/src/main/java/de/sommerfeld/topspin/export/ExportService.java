package de.sommerfeld.topspin.export;

import de.sommerfeld.topspin.plan.TrainingPlan;

import java.io.File;
import java.io.IOException;

/**
 * Provides export functionality for training plans into various file formats. This service is responsible for
 * converting a {@link TrainingPlan} into a specific file format supported by the implementing class.
 */
public interface ExportService {

    /**
     * Exports the specified training plan into a file format supported by the implementing service.
     *
     * @param trainingPlan the training plan to be exported, which contains details about training units and their
     *                     exercises
     */
    void export(TrainingPlan trainingPlan, File targetFile) throws IOException;
}
