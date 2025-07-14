$(document).ready(function() {
    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString + 'T00:00:00');
        if (isNaN(date.getTime())) {
            return 'Fecha inválida';
        }
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return date.toLocaleDateString('es-ES', options);
    }

    function normalizeCase(str) {
        if (!str) return '';
        return str.replace(/_/g, ' ')
                    .toLowerCase()
                    .split(' ')
                    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                    .join(' ');
    }

    function getCsrfToken() {
        return $("meta[name='_csrf']").attr("content");
    }

    function getCsrfHeader() {
        return $("meta[name='_csrf_header']").attr("content");
    }

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

    function loadUsers() {
        console.log("JS LOG: Cargando usuarios...");
        $.ajax({
            url: '/admin/api/users',
            method: 'GET',
            success: function(users) {
                console.log("JS LOG: Usuarios cargados exitosamente.", users);
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
                console.error("JS ERROR: Error al cargar usuarios:", error, xhr.responseText);
                showMessageModal("Error al cargar usuarios: " + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
            }
        });
    }

    function reloadUserTasks(userId) {
        console.log("JS LOG: Recargando tareas para el usuario ID:", userId);
        if (!userId) {
            $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
            console.log("JS LOG: userId es nulo o vacío, no se cargan tareas.");
            return;
        }
        $('#userTasksList').empty();
        $('#userTasksList').append('<li class="list-group-item text-center text-muted">Cargando tareas...</li>');
        $.ajax({
            url: '/admin/api/users/' + userId + '/tasks',
            method: 'GET',
            success: function(tasks) {
                console.log("JS LOG: Tareas cargadas exitosamente para el usuario ID:", userId, tasks);
                $('#userTasksList').empty();
                if (tasks.length > 0) {
                    tasks.forEach(function(task) {
                        const displayPriority = normalizeCase(task.priority);
                        const displayStatus = normalizeCase(task.status);
                        let statusBadgeClass = '';
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
                console.error('JS ERROR: Error al cargar las tareas del usuario:', error, xhr.responseText);
                $('#userTasksList').empty().append('<li class="list-group-item text-danger">Error al cargar las tareas.</li>');
                showMessageModal("Error al cargar las tareas del usuario: " + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
            }
        });
    }

    loadUsers();

    $('#userSelect').change(function() {
        const userId = $(this).val();
        console.log("JS LOG: Usuario seleccionado, ID:", userId);
        if (userId) {
            $('#assignAdminRoleBtn').prop('disabled', false);
            $('#deleteUserBtn').prop('disabled', false);
            $('#userInfoSection').show();
            $('#userTasksSection').show();
            $.ajax({
                url: '/admin/api/users/' + userId,
                method: 'GET',
                success: function(user) {
                    if (user) {
                        console.log("JS LOG: Información del usuario cargada:", user);
                        $('#userId').text(user.id);
                        $('#userUsername').text(user.username);
                        $('#userEmail').text(user.email);
                        $('#userRole').text(normalizeCase(user.role));
                        reloadUserTasks(userId);
                    } else {
                        console.warn("JS WARN: La información del usuario no está disponible para ID:", userId);
                        showMessageModal('La información del usuario no está disponible.', true);
                        $('#assignAdminRoleBtn').prop('disabled', true);
                        $('#deleteUserBtn').prop('disabled', true);
                        $('#userInfoSection').hide();
                        $('#userTasksSection').hide();
                    }
                },
                error: function(xhr, status, error) {
                    console.error('JS ERROR: Error al cargar la información del usuario:', error, xhr.responseText);
                    $('#assignAdminRoleBtn').prop('disabled', true);
                    $('#deleteUserBtn').prop('disabled', true);
                    $('#userInfoSection').hide();
                    $('#userTasksSection').hide();
                    showMessageModal('No se pudo cargar la información del usuario: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
                    $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
                }
            });
        } else {
            console.log("JS LOG: Selección de usuario vacía, ocultando secciones y deshabilitando botones.");
            $('#assignAdminRoleBtn').prop('disabled', true);
            $('#deleteUserBtn').prop('disabled', true);
            $('#userInfoSection').hide();
            $('#userTasksSection').hide();
            $('#userTasksList').empty().append('<li class="list-group-item text-center text-muted">Selecciona un usuario para ver sus tareas.</li>');
        }
    });

    $('#confirmAssignAdminModal').on('show.bs.modal', function(event) {
        const userId = $('#userSelect').val();
        const username = $('#userSelect option:selected').text();
        console.log("JS LOG: Preparando modal para asignar rol ADMIN a usuario ID:", userId);
        if (!userId) {
            showMessageModal('Por favor, selecciona un usuario para darle el rol de administrador.', true);
            event.preventDefault();
            return;
        }
        $('#userToAssignAdminUsername').text(username);
        $('#assignAdminUserId').val(userId);
    });

    $('#confirmAssignAdminButton').click(function() {
        const userId = $('#assignAdminUserId').val();
        const username = $('#userToAssignAdminUsername').text();
        console.log("JS LOG: Enviando solicitud para asignar rol ADMIN a usuario ID:", userId);
        $.ajax({
            url: `/admin/api/users/${userId}/asignar-admin`,
            method: 'PUT',
            success: function() {
                console.log("JS LOG: Rol ADMIN asignado exitosamente a usuario ID:", userId);
                showMessageModal(`El usuario "${username}" ahora tiene el rol de ADMIN.`, false);
                $('#confirmAssignAdminModal').modal('hide');
                $('#userSelect').val('');
                $('#userSelect').change();
                loadUsers();
            },
            error: function(xhr, status, error) {
                console.error('JS ERROR: Error al asignar rol de administrador:', error, xhr.responseText);
                showMessageModal('Error al asignar rol de administrador: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
            }
        });
    });

    $('#deleteUserBtn').click(function() {
        const userId = $('#userSelect').val();
        const username = $('#userSelect option:selected').text();
        console.log("JS LOG: Clic en el botón 'Eliminar Usuario'. ID:", userId, "Username:", username);

        if (!userId) {
            console.warn("JS WARN: No hay usuario seleccionado para eliminar.");
            showMessageModal('Por favor, selecciona un usuario para eliminar.', true);
            return;
        }

        $('#userToDeleteUsername').text(username);
        $('#deleteUserId').val(userId);
        $('#confirmDeleteUserModal').modal('show');
        console.log("JS LOG: Modal de confirmación de eliminación de usuario mostrado.");
    });


    $('#confirmDeleteUserButton').click(function() {
        const userId = $('#deleteUserId').val();
        const username = $('#userToDeleteUsername').text();
        console.log("JS LOG: Confirmando eliminación de usuario ID:", userId);
        if (!userId) {
            console.error("JS ERROR: ID de usuario para eliminar es nulo o vacío en la confirmación.");
            showMessageModal('Error: No se pudo obtener el ID del usuario para eliminar.', true);
            return;
        }
        $.ajax({
            url: `/admin/api/users/${userId}`,
            method: 'DELETE',
            contentType: 'application/json',
            success: function() {
                console.log("JS LOG: Usuario ID " + userId + " eliminado exitosamente.");
                showMessageModal(`El usuario "${username}" ha sido eliminado exitosamente.`, false);
                $('#confirmDeleteUserModal').modal('hide');
                $('#userSelect').val('');
                $('#userSelect').change();
                loadUsers();
            },
            error: function(xhr, status, error) {
                console.error('JS ERROR: Error al eliminar usuario:', error, xhr.responseText);
                let errorMessage = 'Error al eliminar usuario.';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMessage += ': ' + xhr.responseJSON.message;
                } else if (xhr.responseText) {
                    errorMessage += ': ' + xhr.responseText;
                } else if (error) {
                    errorMessage += ': ' + error;
                }
                showMessageModal(errorMessage, true);
            }
        });
    });

    $(document).on('click', '.edit-task-btn', function() {
        const taskId = $(this).data('task-id');
        const taskTitle = $(this).data('task-title');
        const taskDescription = $(this).data('task-description');
        const taskDueDate = $(this).data('task-duedate');
        const taskPriority = $(this).data('task-priority');
        const taskStatus = $(this).data('task-status');
        const taskEtiqueta = $(this).data('task-etiqueta');
        console.log("JS LOG: Preparando modal para editar tarea ID:", taskId);
        $('#editTaskId').val(taskId);
        $('#editTaskTitle').val(taskTitle);
        $('#editTaskDescription').val(taskDescription || '');
        $('#editTaskDueDate').val(taskDueDate);
        $('#editTaskPriority').val(taskPriority);
        $('#editTaskStatus').val(taskStatus);
        if ($('#editTaskEtiqueta').length) {
            $('#editTaskEtiqueta').val(taskEtiqueta || '');
        }
    });

    $('#saveTaskChanges').click(function() {
        const taskId = $('#editTaskId').val();
        const userId = $('#userSelect').val();
        const updatedTask = {
            title: $('#editTaskTitle').val(),
            description: $('#editTaskDescription').val(),
            dueDate: $('#editTaskDueDate').val(),
            priority: $('#editTaskPriority').val(),
            status: $('#editTaskStatus').val(),
            etiqueta: $('#editTaskEtiqueta').val()
        };
        console.log("JS LOG: Enviando solicitud para guardar cambios de tarea ID:", taskId, "para usuario ID:", userId, "Datos:", updatedTask);

        if (!updatedTask.title || !updatedTask.dueDate || !updatedTask.priority || !updatedTask.status) {
            showMessageModal('Por favor, completa todos los campos obligatorios: Título, Fecha de Vencimiento, Prioridad y Estado.', true);
            return;
        }
        $.ajax({
            url: `/admin/api/users/${userId}/tasks/${taskId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(updatedTask),
            success: function() {
                console.log("JS LOG: Tarea ID " + taskId + " actualizada exitosamente.");
                showMessageModal('Tarea actualizada exitosamente.', false);
                $('#editTaskModal').modal('hide');
                reloadUserTasks(userId);
            },
            error: function(xhr, status, error) {
                console.error('JS ERROR: Error al actualizar la tarea:', error, xhr.responseText);
                showMessageModal('Error al actualizar la tarea: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
            }
        });
    });

    $(document).on('click', '.delete-task-btn', function() {
        const taskId = $(this).data('task-id');
        console.log("JS LOG: Preparando modal para eliminar tarea ID:", taskId);
        $('#deleteTaskId').val(taskId);
    });

    $('#confirmDeleteButton').click(function() {
        const taskId = $('#deleteTaskId').val();
        const userId = $('#userSelect').val();
        console.log("JS LOG: Enviando solicitud para eliminar tarea ID:", taskId, "para usuario ID:", userId);

        if (!taskId || !userId) {
            console.error("JS ERROR: ID de tarea o usuario para eliminar es nulo o vacío.");
            showMessageModal('Error: No se pudo obtener el ID de la tarea o del usuario para eliminar.', true);
            return;
        }
        $.ajax({
            url: `/admin/api/users/${userId}/tasks/${taskId}`,
            method: 'DELETE',
            contentType: 'application/json',
            success: function() {
                console.log("JS LOG: Tarea ID " + taskId + " eliminada exitosamente.");
                showMessageModal('Tarea eliminada exitosamente.', false);
                $('#confirmDeleteModal').modal('hide');
                reloadUserTasks(userId);
            },
            error: function(xhr, status, error) {
                console.error('JS ERROR: Error al eliminar la tarea:', error, xhr.responseText);
                if (xhr.status === 403) {
                    showMessageModal('No tienes permisos para eliminar esta tarea.', true);
                } else {
                    showMessageModal('Error al eliminar la tarea: ' + (xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : xhr.responseText || error), true);
                }
            }
        });
    });
});