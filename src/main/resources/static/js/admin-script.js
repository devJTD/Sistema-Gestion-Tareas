$(document).ready(function() {

    // Función para formatear fechas para visualización (ej. "15 de junio de 2025")
    // Esta función se usa para mostrar la fecha en la lista, no para el input del modal.
    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        // Asegurarse de que el dateString es ISO (yyyy-MM-dd) para evitar problemas de zona horaria
        // Si el backend envía "YYYY-MM-DD", new Date() lo interpreta como UTC medianoche.
        // Añadir 'T00:00:00' lo fuerza a ser interpretado en la zona horaria local.
        const date = new Date(dateString + 'T00:00:00'); 
        if (isNaN(date.getTime())) { // Valida si la fecha es inválida
            return 'Fecha inválida';
        }
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return date.toLocaleDateString('es-ES', options);
    }

    // --- FUNCIÓN PARA NORMALIZAR CAPITALIZACIÓN ---
    // Convierte strings como "EN_PROCESO" o "alta" a "En proceso" o "Alta"
    function normalizeCase(str) {
        if (!str) return '';
        if (str.includes('_')) {
            str = str.replace(/_/g, ' ').toLowerCase();
        } else {
            str = str.toLowerCase();
        }
        return str.charAt(0).toUpperCase() + str.slice(1);
    }
    // ----------------------------------------------------

    // Función para obtener el token CSRF
    function getCsrfToken() {
        return $("meta[name='_csrf']").attr("content");
    }

    // Función para obtener el nombre del header CSRF
    function getCsrfHeader() {
        return $("meta[name='_csrf_header']").attr("content");
    }

    // Función para cargar los usuarios en el select
    function loadUsers() {
        $.ajax({
            url: '/admin/api/users',
            method: 'GET',
            success: function(users) {
                const userSelect = $('#userSelect');
                userSelect.empty();
                userSelect.append('<option value="">-- Selecciona un usuario --</option>');
                if (users && users.length > 0) {
                    users.forEach(function(user) {
                        userSelect.append(`<option value="${user.id}">${user.username}</option>`);
                    });
                } else {
                    userSelect.append('<option value="">-- No hay usuarios disponibles --</option>');
                }
            },
            error: function(xhr, status, error) {
                console.error("Error al cargar usuarios:", error);
                alert("Error al cargar usuarios: " + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error));
            }
        });
    }

    // Función para recargar las tareas de un usuario específico
    function reloadUserTasks(userId) {
        if (!userId) {
            $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
            return;
        }

        $('#userTasksList').empty();
        $('#userTasksList').append('<li class="list-group-item text-center text-muted">Cargando tareas...</li>');

        $.ajax({
            url: '/admin/api/users/' + userId + '/tasks',
            method: 'GET',
            success: function(tasks) {
                $('#userTasksList').empty();
                if (tasks.length > 0) {
                    tasks.forEach(function(task) {
                        // Normalizar priority y status para VISUALIZACIÓN en la lista y data-attributes
                        const displayPriority = normalizeCase(task.priority);
                        const displayStatus = normalizeCase(task.status);

                        let statusBadgeClass = '';
                        switch(displayStatus) {
                            case 'Pendiente': statusBadgeClass = 'badge-warning'; break;
                            case 'En proceso': statusBadgeClass = 'badge-info'; break;
                            case 'Completada': statusBadgeClass = 'badge-success'; break;
                            case 'Cancelada': statusBadgeClass = 'badge-danger'; break;
                            default: statusBadgeClass = 'badge-secondary';
                        }
                        
                        const tagsHtml = task.etiqueta ? `<span class="badge badge-info"><i class="fas fa-tag"></i> Etiquetas: ${task.etiqueta}</span>` : '';

                        const taskItemHtml = `
                            <li class="list-group-item task-item">
                                <h5 class="task-title">
                                    ${task.title} 
                                    <span class="badge ${statusBadgeClass} task-status">${displayStatus}</span>
                                </h5>
                                <p class="task-description">${task.description || 'Sin descripción.'}</p>
                                <div class="task-details">
                                    <span class="badge badge-secondary mr-2"><i class="far fa-calendar-alt"></i> Vence: ${formatDate(task.dueDate)}</span>
                                    <span class="badge badge-primary mr-2"><i class="fas fa-exclamation-triangle"></i> Prioridad: ${displayPriority || 'N/A'}</span>
                                    ${tagsHtml}
                                </div>
                                <div class="task-actions mt-2">
                                    <button class="btn btn-sm btn-outline-warning edit-task-btn"
                                        data-task-id="${task.id}"
                                        data-task-title="${task.title}"
                                        data-task-description="${task.description || ''}"
                                        data-task-duedate="${task.dueDate || ''}" data-task-priority="${displayPriority || ''}" 
                                        data-task-status="${displayStatus || ''}"
                                        data-task-etiqueta="${task.etiqueta || ''}"
                                        data-toggle="modal" data-target="#editTaskModal">
                                        <i class="fas fa-edit"></i> Editar
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger delete-task-btn" data-task-id="${task.id}" data-toggle="modal" data-target="#confirmDeleteModal">
                                        <i class="fas fa-trash-alt"></i> Eliminar
                                    </button>
                                </div>
                            </li>
                        `;
                        $('#userTasksList').append(taskItemHtml);
                    });
                } else {
                    $('#userTasksList').append('<li class="list-group-item text-center text-muted">Este usuario no tiene tareas agendadas.</li>');
                }
            },
            error: function(xhr, status, error) {
                console.error('Error al cargar las tareas del usuario:', error);
                $('#userTasksList').empty().append('<li class="list-group-item text-danger">Error al cargar las tareas.</li>');
            }
        });
    }

    // Cargar usuarios al inicio de la página de administración
    loadUsers();

    // Evento change para el selector de usuario
    $('#userSelect').change(function() {
        const userId = $(this).val();

        if (userId) {
            $('#userInfoSection').show();
            $('#userTasksSection').show();

            $.ajax({
                url: '/admin/api/users/' + userId,
                method: 'GET',
                success: function(user) {
                    if (user) {
                        $('#userId').text(user.id);
                        $('#userUsername').text(user.username);
                        $('#userEmail').text(user.email);
                        $('#userRole').text(user.role);
                        reloadUserTasks(userId);
                    } else {
                        alert('La información del usuario no está disponible.');
                        $('#userInfoSection').hide();
                        $('#userTasksSection').hide();
                    }
                },
                error: function(xhr, status, error) {
                    console.error('Error al cargar la información del usuario:', error);
                    $('#userInfoSection').hide();
                    $('#userTasksSection').hide();
                    alert('No se pudo cargar la información del usuario.');
                    $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
                }
            });
        } else {
            $('#userInfoSection').hide();
            $('#userTasksSection').hide();
            $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
        }
    });

    // --- Manejo del Modal de Edición de Tareas ---

    // Delegación de eventos para los botones de editar tarea
    $(document).on('click', '.edit-task-btn', function() {
        const taskId = $(this).data('task-id');
        const taskTitle = $(this).data('task-title');
        const taskDescription = $(this).data('task-description');
        const taskDueDate = $(this).data('task-duedate'); // Se toma directamente del data-attribute
        const taskPriority = $(this).data('task-priority'); 
        const taskStatus = $(this).data('task-status');     
        const taskEtiqueta = $(this).data('task-etiqueta');
        
        // Rellenar el formulario del modal con los datos de la tarea
        $('#editTaskId').val(taskId);
        $('#editTaskTitle').val(taskTitle);
        $('#editTaskDescription').val(taskDescription || '');
        $('#editTaskDueDate').val(taskDueDate); // Asigna directamente el valor del data-attribute, que debe ser yyyy-MM-dd
        $('#editTaskPriority').val(taskPriority); 
        $('#editTaskStatus').val(taskStatus);     
        
        if ($('#editTaskEtiqueta').length) {
            $('#editTaskEtiqueta').val(taskEtiqueta || '');
        }
    });

    // Evento al hacer clic en el botón "Guardar Cambios" del modal de edición
    $('#saveTaskChanges').click(function() {
        const taskId = $('#editTaskId').val();
        const userId = $('#userSelect').val();

        const updatedTask = {
            title: $('#editTaskTitle').val(),
            description: $('#editTaskDescription').val(),
            dueDate: $('#editTaskDueDate').val(), // Se envía el valor del input directamente
            priority: $('#editTaskPriority').val(), 
            status: $('#editTaskStatus').val(),     
            etiqueta: $('#editTaskEtiqueta').val()
        };

        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        $.ajax({
            url: `/admin/api/users/${userId}/tasks/${taskId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(updatedTask),
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function() {
                alert('Tarea actualizada exitosamente.');
                $('#editTaskModal').modal('hide');
                reloadUserTasks(userId);
            },
            error: function(xhr, status, error) {
                console.error('Error al actualizar la tarea:', error);
                alert('Error al actualizar la tarea: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error));
            }
        });
    });

    // --- Manejo del Modal de Eliminación de Tareas ---

    // Delegación de eventos para los botones de eliminar tarea
    $(document).on('click', '.delete-task-btn', function() {
        const taskId = $(this).data('task-id');
        $('#deleteTaskId').val(taskId);
    });

    // Evento al hacer clic en el botón "Eliminar" del modal de confirmación
    $('#confirmDeleteButton').click(function() {
        const taskId = $('#deleteTaskId').val();
        const userId = $('#userSelect').val();

        if (!taskId || !userId) {
            alert('Error: No se pudo obtener el ID de la tarea o del usuario para eliminar.');
            return;
        }

        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        $.ajax({
            url: `/admin/api/users/${userId}/tasks/${taskId}`,
            method: 'DELETE',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function() {
                alert('Tarea eliminada exitosamente.');
                $('#confirmDeleteModal').modal('hide');
                reloadUserTasks(userId);
            },
            error: function(xhr, status, error) {
                console.error('Error al eliminar la tarea:', error);
                if (xhr.status === 403) {
                    alert('No tienes permisos para eliminar esta tarea.');
                } else {
                    alert('Error al eliminar la tarea: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error));
                }
            }
        });
    });

});