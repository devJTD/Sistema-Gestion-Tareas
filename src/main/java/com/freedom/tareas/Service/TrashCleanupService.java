package com.freedom.tareas.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Repository.TaskRepository;

@Service
public class TrashCleanupService {

    private final TaskRepository taskRepository;

    public TrashCleanupService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Tarea programada para limpiar automáticamente las tareas en la papelera
     * que superen los 20 días desde su eliminación.
     * Se ejecuta cada día a las 2:00 AM (hora del servidor).
     */
    @Scheduled(cron = "0 0 2 * * ?") // Cron expression: segundos, minutos, horas, día del mes, mes, día de la semana
    @Transactional // Asegura que la operación de eliminación se realice de forma transaccional
    public void cleanupOldTasksInTrash() {
        System.out.println("LOG: *** INICIANDO TAREA PROGRAMADA DE LIMPIEZA DE PAPELERA ***");

        // Calculamos la fecha límite: 20 días antes de la fecha actual.
        // Cualquier tarea eliminada en o antes de esta fecha será elegible para eliminación.
        LocalDate twentyDaysAgo = LocalDate.now().minusDays(20);
        System.out.println("LOG: *** Fecha de hoy: " + LocalDate.now() + " | Fecha límite para eliminación (20 días atrás): " + twentyDaysAgo + " ***");

        // *** CAMBIO CRÍTICO AQUÍ: USAMOS "off" EN LUGAR DE "trash" ***
        List<Task> tasksToDelete = taskRepository.findByActiveOnPageAndDeletedAtBefore("off", twentyDaysAgo);

        int deletedCount = 0;
        if (tasksToDelete.isEmpty()) {
            System.out.println("LOG: *** No se encontraron tareas elegibles para eliminar de la papelera. ***");
        } else {
            System.out.println("LOG: *** Se encontraron " + tasksToDelete.size() + " tareas en papelera para evaluar. ***");
            for (Task task : tasksToDelete) {
                if (task.getDeletedAt() != null) {
                    long daysInTrash = ChronoUnit.DAYS.between(task.getDeletedAt(), LocalDate.now());

                    System.out.println("LOG: *** Evaluando tarea ID: " + task.getId() + ", Título: '" + task.getTitle() + "', Eliminada el: " + task.getDeletedAt() + ", Días en papelera: " + daysInTrash + " ***");

                    if (daysInTrash >= 20) {
                        System.out.println("LOG: *** ELIMINANDO PERMANENTEMENTE TAREA ID: " + task.getId() + " - Título: '" + task.getTitle() + "' (Superó los 20 días: " + daysInTrash + " días) ***");
                        taskRepository.delete(task); // Elimina la tarea de la base de datos
                        deletedCount++;
                    } else {
                        System.out.println("LOG: *** Tarea ID: " + task.getId() + " - NO se elimina (Días en papelera: " + daysInTrash + ", menos de 20). ***");
                    }
                } else {
                    System.out.println("LOG: *** ADVERTENCIA: Tarea ID: " + task.getId() + " en papelera sin fecha 'deletedAt'. No se procesa para eliminación. ***");
                }
            }
        }
        System.out.println("LOG: *** TAREA PROGRAMADA DE LIMPIEZA FINALIZADA. Tareas eliminadas en esta ejecución: " + deletedCount + " ***");
    }
}