$(document).ready(function() {

    // Función para formatear fechas para visualización (ej. "15 de junio de 2025")
    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString + 'T00:00:00');
        if (isNaN(date.getTime())) {
            return 'Fecha inválida';
        }
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return date.toLocaleDateString('es-ES', options);
    }



    // --- FUNCIÓN PARA NORMALIZAR CAPITALIZACIÓN (PARA VISUALIZACIÓN Y BACKEND) ---
    // Esta función convierte cualquier cadena (ej. "EN_PROCESO", "ALTA", "mi_texto")
    // a "Primera Letra Mayúscula y el resto minúsculas, sin guiones bajos".
    // Esto se asume que es el formato que TU BACKEND ESPERA Y PRODUCE.
    function normalizeCase(str) {
        if (!str) return '';
        // 1. Reemplaza cualquier guion bajo con un espacio (si los hubiera en la entrada).
        // 2. Convierte toda la cadena a minúsculas.
        // 3. Divide la cadena por espacios.
        // 4. Capitaliza la primera letra de cada palabra.
        // 5. Une las palabras de nuevo con espacios.
        return str.replace(/_/g, ' ') // Cambia guiones bajos por espacios si vinieran del backend
                  .toLowerCase() 
                  .split(' ')
                  .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                  .join(' ');
    }

    // Si el backend SIEMPRE espera y envía el formato "Primera Mayúscula, resto minúscula, sin guiones",
    // entonces no necesitamos una función de "desnormalización", ya que el formato de frontend y backend es el mismo.
    // La función `normalizeCase` se usa para asegurar que cualquier dato entrante o saliente
    // cumpla con este formato.



    // Función para obtener el token CSRF
    function getCsrfToken() {
        return $("meta[name='_csrf']").attr("content");
    }

    // Función para obtener el nombre del header CSRF
    function getCsrfHeader() {
        return $("meta[name='_csrf_header']").attr("content");
    }

    // Función para mostrar mensajes en un modal genérico
    function showMessageModal(message, isError = false) {
        const modalBody = $('#messageModalBody');
        modalBody.html(`<p>${message}</p>`);
        if (isError) {
            modalBody.addClass('text-danger');
        } else {
            modalBody.removeClass('text-danger');
        }
        $('#messageModal').modal('show');
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
                showMessageModal("Error al cargar usuarios: " + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
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
                        // Usamos normalizeCase para la visualización en la lista y para los data-attributes.
                        // Ahora, asumimos que el backend ya envía estos valores en el formato deseado
                        // (Primera Letra Mayúscula, resto minúsculas, sin guiones).
                        const displayPriority = normalizeCase(task.priority); // Para asegurar que cualquier caso inesperado se formatee
                        const displayStatus = normalizeCase(task.status);     // Para asegurar que cualquier caso inesperado se formatee

                        let statusBadgeClass = '';
                        // Las clases CSS se asignarán según el valor NORMALIZADO si el CSS usa ese formato,
                        // o deberías ajustar el switch si las clases CSS esperan el formato de guiones bajos.
                        // Aquí asumo que las clases CSS también están en el formato normalizado o que
                        // se usan los valores originales del backend (ej. "PENDIENTE") para la clase,
                        // y que esos valores originales son como "Pendiente".
                        switch(displayStatus) { 
                            case 'Pendiente': statusBadgeClass = 'badge-warning'; break;
                            case 'En Proceso': statusBadgeClass = 'badge-info'; break;
                            case 'Completada': statusBadgeClass = 'badge-success'; break;
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
                                        data-task-duedate="${task.dueDate || ''}"
                                        data-task-priority="${displayPriority || ''}"  
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
                showMessageModal("Error al cargar las tareas del usuario.", true);
            }
        });
    }

    // Cargar usuarios al inicio de la página de administración
    loadUsers();

    // Evento change para el selector de usuario
    $('#userSelect').change(function() {
        const userId = $(this).val();

        if (userId) {
            // Habilitar los botones de acción de usuario
            $('#assignAdminRoleBtn').prop('disabled', false);
            $('#deleteUserBtn').prop('disabled', false);
            
            // Mostrar las secciones de información y tareas
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
                        $('#userRole').text(normalizeCase(user.role)); // Normaliza el rol para mostrarlo
                        reloadUserTasks(userId);
                    } else {
                        showMessageModal('La información del usuario no está disponible.', true);
                        // Ocultar y deshabilitar todo si no hay usuario
                        $('#assignAdminRoleBtn').prop('disabled', true);
                        $('#deleteUserBtn').prop('disabled', true);
                        $('#userInfoSection').hide();
                        $('#userTasksSection').hide();
                    }
                },
                error: function(xhr, status, error) {
                    console.error('Error al cargar la información del usuario:', error);
                    // Ocultar y deshabilitar todo si hay un error
                    $('#assignAdminRoleBtn').prop('disabled', true);
                    $('#deleteUserBtn').prop('disabled', true);
                    $('#userInfoSection').hide();
                    $('#userTasksSection').hide();
                    showMessageModal('No se pudo cargar la información del usuario.', true);
                    $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
                }
            });
        } else {
            // Si no hay usuario seleccionado, deshabilitar los botones y ocultar las secciones
            $('#assignAdminRoleBtn').prop('disabled', true);
            $('#deleteUserBtn').prop('disabled', true);
            $('#userInfoSection').hide();
            $('#userTasksSection').hide();
            $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
        }
    });



    // --- Manejo del Modal para Confirmar Asignación de Rol Admin ---
    $('#confirmAssignAdminModal').on('show.bs.modal', function(event) {
        const userId = $('#userSelect').val();
        const username = $('#userSelect option:selected').text();

        if (!userId) {
            showMessageModal('Por favor, selecciona un usuario para darle el rol de administrador.', true);
            event.preventDefault(); // Evita que el modal se muestre
            return;
        }

        $('#userToAssignAdminUsername').text(username);
        $('#assignAdminUserId').val(userId);
    });

    $('#confirmAssignAdminButton').click(function() {
        const userId = $('#assignAdminUserId').val();
        const username = $('#userToAssignAdminUsername').text();

        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        $.ajax({
            url: `/admin/api/users/${userId}/assign-admin`,
            method: 'PUT',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function() {
                showMessageModal(`El usuario "${username}" ahora tiene el rol de ADMIN.`, false);
                $('#confirmAssignAdminModal').modal('hide');
                $('#userSelect').change(); // Recarga la información del usuario para actualizar el rol
                loadUsers(); // Recarga la lista de usuarios en el select
            },
            error: function(xhr, status, error) {
                console.error('Error al asignar rol de administrador:', error);
                showMessageModal('Error al asignar rol de administrador: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
            }
        });
    });



    // --- Manejo del Modal para Confirmar Eliminación de USUARIO ---

    // Cuando el modal de confirmación de eliminación de usuario se va a mostrar
    $('#confirmDeleteUserModal').on('show.bs.modal', function (event) {
        const userId = $('#userSelect').val();
        const username = $('#userSelect option:selected').text();

        if (!userId) {
            showMessageModal('Por favor, selecciona un usuario para eliminar.', true);
            event.preventDefault(); // Evita que el modal se muestre
            return;
        }

        // Rellenar el modal con el nombre del usuario
        $('#userToDeleteUsername').text(username);
        $('#deleteUserId').val(userId); // Guarda el ID del usuario en el input hidden
    });

    // Evento al hacer clic en el botón "Eliminar Usuario" dentro del modal de confirmación
    $('#confirmDeleteUserButton').click(function() {
        const userId = $('#deleteUserId').val(); // Obtiene el ID del usuario del input hidden
        const username = $('#userToDeleteUsername').text(); // Obtiene el nombre del usuario del span

        if (!userId) {
            showMessageModal('Error: No se pudo obtener el ID del usuario para eliminar.', true);
            return;
        }

        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        $.ajax({
            url: `/admin/api/users/${userId}`,
            method: 'DELETE',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function() {
                showMessageModal(`El usuario "${username}" ha sido eliminado exitosamente.`, false);
                $('#confirmDeleteUserModal').modal('hide'); // Oculta el modal de confirmación
                $('#userSelect').val(''); // Deselecciona el usuario
                $('#userSelect').change(); // Dispara el evento change para ocultar las secciones
                loadUsers(); // Recarga la lista de usuarios en el select
            },
            error: function(xhr, status, error) {
                console.error('Error al eliminar usuario:', error);
                showMessageModal('Error al eliminar usuario: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
            }
        });
    });



    // --- Manejo del Modal de Edición de Tareas ---

    // Delegación de eventos para los botones de editar tarea
    $(document).on('click', '.edit-task-btn', function() {
        const taskId = $(this).data('task-id');
        const taskTitle = $(this).data('task-title');
        const taskDescription = $(this).data('task-description');
        const taskDueDate = $(this).data('task-duedate'); 
        // Usamos directamente los valores de data-attributes, que ya deberían estar en el formato normalizado.
        const taskPriority = $(this).data('task-priority'); 
        const taskStatus = $(this).data('task-status');     
        const taskEtiqueta = $(this).data('task-etiqueta');
        
        // Rellenar el formulario del modal con los datos de la tarea
        $('#editTaskId').val(taskId);
        $('#editTaskTitle').val(taskTitle);
        $('#editTaskDescription').val(taskDescription || '');
        $('#editTaskDueDate').val(taskDueDate); 
        // Selecciona la opción correcta en el select usando el valor normalizado.
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

        // Los valores de priority y status de los selects ya vienen en el formato deseado
        // (Primera Letra Mayúscula, resto minúsculas, sin guiones),
        // así que se envían directamente.
        const updatedTask = {
            title: $('#editTaskTitle').val(),
            description: $('#editTaskDescription').val(),
            dueDate: $('#editTaskDueDate').val(), 
            priority: $('#editTaskPriority').val(), 
            status: $('#editTaskStatus').val(),     
            etiqueta: $('#editTaskEtiqueta').val()
        };

        // Validar que los campos obligatorios no estén vacíos
        if (!updatedTask.title || !updatedTask.dueDate || !updatedTask.priority || !updatedTask.status) {
            showMessageModal('Por favor, completa todos los campos obligatorios: Título, Fecha de Vencimiento, Prioridad y Estado.', true);
            return;
        }

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
                showMessageModal('Tarea actualizada exitosamente.', false);
                $('#editTaskModal').modal('hide');
                reloadUserTasks(userId);
            },
            error: function(xhr, status, error) {
                console.error('Error al actualizar la tarea:', error);
                showMessageModal('Error al actualizar la tarea: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
            }
        });
    });

    // Delegación de eventos para los botones de eliminar tarea
    $(document).on('click', '.delete-task-btn', function() {
        const taskId = $(this).data('task-id');
        $('#deleteTaskId').val(taskId);
    });

    // Evento al hacer clic en el botón "Eliminar" del modal de confirmación de tarea
    $('#confirmDeleteButton').click(function() {
        const taskId = $('#deleteTaskId').val();
        const userId = $('#userSelect').val();

        if (!taskId || !userId) {
            showMessageModal('Error: No se pudo obtener el ID de la tarea o del usuario para eliminar.', true);
            return;
        }

        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        $.ajax({
            url: `/admin/api/users/${userId}/tasks/${taskId}`,
            method: 'DELETE',
            contentType: 'application/json', 
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function() {
                showMessageModal('Tarea eliminada exitosamente.', false);
                $('#confirmDeleteModal').modal('hide');
                reloadUserTasks(userId);
            },
            error: function(xhr, status, error) {
                console.error('Error al eliminar la tarea:', error);
                if (xhr.status === 403) {
                    showMessageModal('No tienes permisos para eliminar esta tarea.', true);
                } else {
                    showMessageModal('Error al eliminar la tarea: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
                }
            }
        });
    });
});