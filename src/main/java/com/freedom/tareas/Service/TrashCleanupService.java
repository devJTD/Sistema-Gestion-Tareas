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

    // Inyección del repositorio para interactuar con la base de datos de tareas.
    private final TaskRepository taskRepository;

    // Constructor para que Spring inyecte automáticamente TaskRepository.
    public TrashCleanupService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Este método se ejecuta automáticamente cada día a las 2:00 AM.
    // Todas las operaciones dentro son parte de una única transacción.
    @Scheduled(cron = "0 0 2 * * ?") 
    @Transactional 
    public void cleanupOldTasksInTrash() {
        System.out.println("LOG: *** INICIANDO TAREA PROGRAMADA DE LIMPIEZA DE PAPELERA ***");

        // Calcula la fecha de corte: 20 días atrás.
        LocalDate twentyDaysAgo = LocalDate.now().minusDays(20);
        System.out.println("LOG: *** Fecha de hoy: " + LocalDate.now() + " | Fecha límite para eliminación (20 días atrás): " + twentyDaysAgo + " ***");

        // Busca tareas en la papelera ('activeOnPage' = "off") que fueron eliminadas antes de la fecha de corte.
        List<Task> tasksToDelete = taskRepository.findByActiveOnPageAndDeletedAtBefore("off", twentyDaysAgo);

        int deletedCount = 0;
        if (tasksToDelete.isEmpty()) {
            System.out.println("LOG: *** No se encontraron tareas elegibles para eliminar de la papelera. ***");
        } else {
            System.out.println("LOG: *** Se encontraron " + tasksToDelete.size() + " tareas en papelera para evaluar. ***");
            // Itera sobre las tareas candidatas.
            for (Task task : tasksToDelete) {
                // Verifica que la tarea tenga una fecha de eliminación.
                if (task.getDeletedAt() != null) {
                    // Calcula los días que la tarea lleva en la papelera.
                    long daysInTrash = ChronoUnit.DAYS.between(task.getDeletedAt(), LocalDate.now());

                    System.out.println("LOG: *** Evaluando tarea ID: " + task.getId() + ", Título: '" + task.getTitle() + "', Eliminada el: " + task.getDeletedAt() + ", Días en papelera: " + daysInTrash + " ***");

                    // Si la tarea lleva 20 días o más en la papelera, la elimina permanentemente.
                    if (daysInTrash >= 20) {
                        System.out.println("LOG: *** ELIMINANDO PERMANENTEMENTE TAREA ID: " + task.getId() + " - Título: '" + task.getTitle() + "' (Superó los 20 días: " + daysInTrash + " días) ***");
                        taskRepository.delete(task); 
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